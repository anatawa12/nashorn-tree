/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.anatawa12.nashorn.internal.ir;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.anatawa12.nashorn.internal.codegen.CompileUnit;
import com.anatawa12.nashorn.internal.codegen.Namespace;
import com.anatawa12.nashorn.internal.codegen.types.Type;
import com.anatawa12.nashorn.internal.ir.annotations.Immutable;
import com.anatawa12.nashorn.internal.ir.visitor.NodeVisitor;
import com.anatawa12.nashorn.internal.runtime.Source;

/**
 * IR representation for function (or script.)
 */
@Immutable
public final class FunctionNode extends LexicalContextExpression implements Flags<FunctionNode>, CompileUnitHolder {
    private static final long serialVersionUID = 1L;

    /** Function kinds */
    public enum Kind {
        /** a normal function - nothing special */
        NORMAL,
        /** a script function */
        SCRIPT,
        /** a getter */
        GETTER,
        /** a setter */
        SETTER,
        /** an arrow function */
        ARROW,
        /** a generator function */
        GENERATOR,
        /** a module function */
        MODULE
    }

    /** Source of entity. */
    private transient final Source source;

    /**
     * Opaque object representing parser state at the end of the function. Used when reparsing outer functions
     * to skip parsing inner functions.
     */
    private final Object endParserState;

    /** External function identifier. */
    private final IdentNode ident;

    /** The body of the function node */
    private final Block body;

    /** Internal function name. */
    private final String name;

    /** Compilation unit. */
    private final CompileUnit compileUnit;

    /** Function kind. */
    private final Kind kind;

    /** List of parameters. */
    private final List<IdentNode> parameters;

    /** Map of ES6 function parameter expressions. */
    private final Map<IdentNode, Expression> parameterExpressions;

    /** First token of function. **/
    private final long firstToken;

    /** Last token of function. **/
    private final long lastToken;

    /** Method's namespace. */
    private transient final Namespace namespace;

    /** Number of properties of "this" object assigned in this function */
    private final int thisProperties;

    /** Function flags. */
    private final int flags;

    /** Line number of function start */
    private final int lineNumber;

    /** Root class for function */
    private final Class<?> rootClass;

    /** The ES6 module */
    private final Module module;

    /** The debug flags */
    private final int debugFlags;

    /** Is anonymous function flag. */
    public static final int IS_ANONYMOUS                = 1 << 0;

    /** Is the function created in a function declaration (as opposed to a function expression) */
    public static final int IS_DECLARED                 = 1 << 1;

    /** is this a strict mode function? */
    public static final int IS_STRICT                   = 1 << 2;

    /** Does the function use the "arguments" identifier ? */
    public static final int USES_ARGUMENTS              = 1 << 3;

    /** Does the function call eval? If it does, then all variables in this function might be get/set by it and it can
     * introduce new variables into this function's scope too.*/
    public static final int HAS_EVAL                    = 1 << 5;

    /** Does a nested function contain eval? If it does, then all variables in this function might be get/set by it. */
    public static final int HAS_NESTED_EVAL             = 1 << 6;

    /** Does this function have any blocks that create a scope? This is used to determine if the function needs to
     * have a local variable slot for the scope symbol. */
    public static final int HAS_SCOPE_BLOCK             = 1 << 7;

    /**
     * Flag this function as one that defines the identifier "arguments" as a function parameter or nested function
     * name. This precludes it from needing to have an Arguments object defined as "arguments" local variable. Note that
     * defining a local variable named "arguments" still requires construction of the Arguments object (see
     * ECMAScript 5.1 Chapter 10.5).
     * //@see #needsArguments()
     */
    public static final int DEFINES_ARGUMENTS           = 1 << 8;

    /** Does this function have nested declarations? */
    public static final int HAS_FUNCTION_DECLARATIONS   = 1 << 10;

    /**
     * Is this function the top-level program?
     */
    public static final int IS_PROGRAM                  = 1 << 13;

    /** Does this function use the "this" keyword? */
    public static final int USES_THIS                   = 1 << 15;


    /**
     * Does this function contain a super call? (cf. ES6 14.3.5 Static Semantics: HasDirectSuper)
     */
    public static final int ES6_HAS_DIRECT_SUPER        = 1 << 19;

    /**
     * Does this function use the super binding?
     */
    public static final int ES6_USES_SUPER              = 1 << 20;

    /**
     * Is this function a (class or object) method?
     */
    public static final int ES6_IS_METHOD               = 1 << 21;

    /**
     * Is this the constructor method?
     */
    public static final int ES6_IS_CLASS_CONSTRUCTOR    = 1 << 22;

    /** Is this the constructor of a subclass (i.e., a class with an extends declaration)? */
    public static final int ES6_IS_SUBCLASS_CONSTRUCTOR = 1 << 23;

    /** Does this function use new.target? */
    public static final int ES6_USES_NEW_TARGET         = 1 << 25;

    /** Does this function have expression as its body? */
    public static final int HAS_EXPRESSION_BODY         = 1 << 26;

    // callsite tracing, profiling within this function

    /** What is the return type of this function? */
    public Type returnType = Type.UNKNOWN;

    /**
     * Constructor
     *
     * @param source     the source
     * @param lineNumber line number
     * @param token      token
     * @param finish     finish
     * @param firstToken first token of the function node (including the function declaration)
     * @param lastToken  lastToken
     * @param namespace  the namespace
     * @param ident      the identifier
     * @param name       the name of the function
     * @param parameters parameter list
     * @param paramExprs the ES6 function parameter expressions
     * @param kind       kind of function as in {@link FunctionNode.Kind}
     * @param flags      initial flags
     * @param body       body of the function
     * @param endParserState The parser state at the end of the parsing.
     * @param module     the module
     * @param debugFlags the debug flags
     */
    public FunctionNode(
        final Source source,
        final int lineNumber,
        final long token,
        final int finish,
        final long firstToken,
        final long lastToken,
        final Namespace namespace,
        final IdentNode ident,
        final String name,
        final List<IdentNode> parameters,
        final Map<IdentNode, Expression> paramExprs,
        final FunctionNode.Kind kind,
        final int flags,
        final Block body,
        final Object endParserState,
        final Module module,
        final int debugFlags) {
        super(token, finish);

        this.source           = source;
        this.lineNumber       = lineNumber;
        this.ident            = ident;
        this.name             = name;
        this.kind             = kind;
        this.parameters       = parameters;
        this.parameterExpressions = paramExprs;
        this.firstToken       = firstToken;
        this.lastToken        = lastToken;
        this.namespace        = namespace;
        this.flags            = flags;
        this.compileUnit      = null;
        this.body             = body;
        this.thisProperties   = 0;
        this.rootClass        = null;
        this.endParserState   = endParserState;
        this.module           = module;
        this.debugFlags       = debugFlags;
    }

    private FunctionNode(
        final FunctionNode functionNode,
        final long lastToken,
        final Object endParserState,
        final int flags,
        final String name,
        final Type returnType,
        final CompileUnit compileUnit,
        final Block body,
        final List<IdentNode> parameters,
        final int thisProperties,
        final Class<?> rootClass,
        final Source source, final Namespace namespace) {
        super(functionNode);

        this.endParserState    = endParserState;
        this.lineNumber       = functionNode.lineNumber;
        this.flags            = flags;
        this.name             = name;
        this.returnType       = returnType;
        this.compileUnit      = compileUnit;
        this.lastToken        = lastToken;
        this.body             = body;
        this.parameters       = parameters;
        this.parameterExpressions = functionNode.parameterExpressions;
        this.thisProperties   = thisProperties;
        this.rootClass        = rootClass;
        this.source           = source;
        this.namespace        = namespace;

        // the fields below never change - they are final and assigned in constructor
        this.ident           = functionNode.ident;
        this.kind            = functionNode.kind;
        this.firstToken      = functionNode.firstToken;
        this.module          = functionNode.module;
        this.debugFlags      = functionNode.debugFlags;
    }

    @Override
    public Node accept(final LexicalContext lc, final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterFunctionNode(this)) {
            return visitor.leaveFunctionNode(setBody(lc, (Block)body.accept(visitor)));
        }
        return this;
    }

    /**
     * Get the source for this function
     * @return the source
     */
    public Source getSource() {
        return source;
    }

    /**
     * Sets the source and namespace for this function. It can only set a non-null source and namespace for a function
     * that currently has both a null source and a null namespace. This is used to re-set the source and namespace for
     * a deserialized function node.
     * @param source the source for the function.
     * @param namespace the namespace for the function
     * @return a new function node with the set source and namespace
     * @throws IllegalArgumentException if the specified source or namespace is null
     * @throws IllegalStateException if the function already has either a source or namespace set.
     */
    public FunctionNode initializeDeserialized(final Source source, final Namespace namespace) {
        if (source == null || namespace == null) {
            throw new IllegalArgumentException();
        } else if (this.source == source && this.namespace == namespace) {
            return this;
        } else if (this.source != null || this.namespace != null) {
            throw new IllegalStateException();
        }
        return new FunctionNode(
            this,
            lastToken,
            endParserState,
            flags,
            name,
            returnType,
            compileUnit,
            body,
            parameters,
            thisProperties,
            rootClass, source, namespace);
    }

    /**
     * get source name - sourceURL or name derived from Source.
     *
     * @return name for the script source
     */
    public String getSourceName() {
        return getSourceName(source);
    }

    /**
     * Static source name getter
     *
     * @param source the source
     * @return source name
     */
    public static String getSourceName(final Source source) {
        final String explicitURL = source.getExplicitURL();
        return explicitURL != null ? explicitURL : source.getName();
    }

    /**
     * Returns the line number.
     * @return the line number.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public int getFlags() {
        return flags;
    }

    @Override
    public boolean getFlag(final int flag) {
        return (flags & flag) != 0;
    }

    @Override
    public FunctionNode setFlags(final LexicalContext lc, final int flags) {
        if (this.flags == flags) {
            return this;
        }
        return Node.replaceInLexicalContext(
                lc,
                this,
                new FunctionNode(
                        this,
                        lastToken,
                        endParserState,
                        flags,
                        name,
                        returnType,
                        compileUnit,
                        body,
                        parameters,
                        thisProperties,
                        rootClass, source, namespace));
    }

    @Override
    public FunctionNode clearFlag(final LexicalContext lc, final int flag) {
        return setFlags(lc, flags & ~flag);
    }

    @Override
    public FunctionNode setFlag(final LexicalContext lc, final int flag) {
        return setFlags(lc, flags | flag);
    }


    /**
     * Get the identifier for this function, this is its symbol.
     * @return the identifier as an IdentityNode
     */
    public IdentNode getIdent() {
        return ident;
    }

    /**
     * Get the function body
     * @return the function body
     */
    public Block getBody() {
        return body;
    }

    /**
     * Reset the function body
     * @param lc lexical context
     * @param body new body
     * @return new function node if body changed, same if not
     */
    public FunctionNode setBody(final LexicalContext lc, final Block body) {
        if (this.body == body) {
            return this;
        }
        return Node.replaceInLexicalContext(
                lc,
                this,
                new FunctionNode(
                        this,
                        lastToken,
                        endParserState,
                        flags |
                            (body.needsScope() ?
                                    FunctionNode.HAS_SCOPE_BLOCK :
                                    0),
                        name,
                        returnType,
                        compileUnit,
                        body,
                        parameters,
                        thisProperties,
                        rootClass, source, namespace));
    }

    /**
     * Return the kind of this function
     * @see FunctionNode.Kind
     * @return the kind
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * Get the parameters to this function
     * @return a list of IdentNodes which represent the function parameters, in order
     */
    public List<IdentNode> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    /**
     * Get the ES6 style parameter expressions of this function. This may be null.
     *
     * @return a Map of parameter IdentNode to Expression node (for ES6 parameter expressions)
     */
    public Map<IdentNode, Expression> getParameterExpressions() {
        return parameterExpressions;
    }

    /**
     * Check if this function is created as a function declaration (as opposed to function expression)
     * @return true if function is declared.
     */
    public boolean isDeclared() {
        return getFlag(IS_DECLARED);
    }

    /**
     * Check if this function is anonymous
     * @return true if function is anonymous
     */
    public boolean isAnonymous() {
        return getFlag(IS_ANONYMOUS);
    }

    /**
     * Check if the function is generated in strict mode
     * @return true if strict mode enabled for function
     */
    public boolean isStrict() {
        return getFlag(IS_STRICT);
    }

    /**
     * Returns the functions's ES6 module.
     *
     * @return the module, or null if this function is not part of one
     */
    public Module getModule() {
        return module;
    }

}
