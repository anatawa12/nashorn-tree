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

import static com.anatawa12.nashorn.internal.codegen.CompilerConstants.__DIR__;
import static com.anatawa12.nashorn.internal.codegen.CompilerConstants.__FILE__;
import static com.anatawa12.nashorn.internal.codegen.CompilerConstants.__LINE__;

import com.anatawa12.nashorn.internal.codegen.types.Type;
import com.anatawa12.nashorn.internal.ir.annotations.Immutable;
import com.anatawa12.nashorn.internal.ir.visitor.NodeVisitor;
import com.anatawa12.nashorn.internal.parser.Token;

/**
 * IR representation for an identifier.
 */
@Immutable
public final class IdentNode extends Expression implements PropertyKey, Optimistic, JoinPredecessor {
    private static final long serialVersionUID = 1L;

    private static final int PROPERTY_NAME     = 1 << 0;
    private static final int INITIALIZED_HERE  = 1 << 1;
    private static final int FUNCTION          = 1 << 2;
    private static final int FUTURESTRICT_NAME = 1 << 3;
    private static final int IS_DECLARED_HERE  = 1 << 4;
    private static final int IS_DEAD           = 1 << 5;
    private static final int DIRECT_SUPER      = 1 << 6;
    private static final int REST_PARAMETER    = 1 << 7;
    private static final int PROTO_PROPERTY    = 1 << 8;
    private static final int DEFAULT_PARAMETER = 1 << 9;
    private static final int DESTRUCTURED_PARAMETER = 1 << 10;

    /** Identifier. */
    private final String name;

    /** Optimistic type */
    private final Type type;

    private final int flags;


    /**
     * Constructor
     *
     * @param token   token
     * @param finish  finish position
     * @param name    name of identifier
     */
    public IdentNode(final long token, final int finish, final String name) {
        super(token, finish);
        this.name = name;
        this.type = null;
        this.flags = 0;
    }

    private IdentNode(final IdentNode identNode, final String name, final Type type, final int flags) {
        super(identNode);
        this.name = name;
        this.type = type;
        this.flags = flags;
    }

    /**
     * Copy constructor - create a new IdentNode for the same location
     *
     * @param identNode  identNode
     */
    public IdentNode(final IdentNode identNode) {
        super(identNode);
        this.name = identNode.getName();
        this.type = identNode.type;
        this.flags = identNode.flags;
    }

    /**
     * Assist in IR navigation.
     *
     * @param visitor IR navigating visitor.
     */
    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterIdentNode(this)) {
            return visitor.leaveIdentNode(this);
        }

        return this;
    }

    /**
     * Get the name of the identifier
     * @return  IdentNode name
     */
    public String getName() {
        return name;
    }

    @Override
    public String getPropertyName() {
        return getName();
    }

    /**
     * Check if this IdentNode is a property name
     * @return true if this is a property name
     */
    public boolean isPropertyName() {
        return (flags & PROPERTY_NAME) == PROPERTY_NAME;
    }

    /**
     * Flag this IdentNode as a property name
     * @return a node equivalent to this one except for the requested change.
     */
    public IdentNode setIsPropertyName() {
        if (isPropertyName()) {
            return this;
        }
        return new IdentNode(this, name, type, flags | PROPERTY_NAME);
    }

    /**
     * Check if this IdentNode is a future strict name
     * @return true if this is a future strict name
     */
    public boolean isFutureStrictName() {
        return (flags & FUTURESTRICT_NAME) == FUTURESTRICT_NAME;
    }

    /**
     * Flag this IdentNode as a future strict name
     * @return a node equivalent to this one except for the requested change.
     */
    public IdentNode setIsFutureStrictName() {
        if (isFutureStrictName()) {
            return this;
        }
        return new IdentNode(this, name, type, flags | FUTURESTRICT_NAME);
    }

    /**
     * Helper function for local def analysis.
     * @return true if IdentNode is initialized on creation
     */
    public boolean isInitializedHere() {
        return (flags & INITIALIZED_HERE) == INITIALIZED_HERE;
    }

    /**
     * Flag IdentNode to be initialized on creation
     * @return a node equivalent to this one except for the requested change.
     */
    public IdentNode setIsInitializedHere() {
        if (isInitializedHere()) {
            return this;
        }
        return new IdentNode(this, name, type, flags | INITIALIZED_HERE);
    }

    /**
     * Is this a LET or CONST identifier used before its declaration?
     *
     * @return true if identifier is dead
     */
    public boolean isDead() {
        return (flags & IS_DEAD) != 0;
    }

    /**
     * Flag this IdentNode as a LET or CONST identifier used before its declaration.
     *
     * @return a new IdentNode equivalent to this but marked as dead.
     */
    public IdentNode markDead() {
        return new IdentNode(this, name, type, flags | IS_DEAD);
    }

    /**
     * Is this IdentNode declared here?
     *
     * @return true if identifier is declared here
     */
    public boolean isDeclaredHere() {
        return (flags & IS_DECLARED_HERE) != 0;
    }

    /**
     * Flag this IdentNode as being declared here.
     *
     * @return a new IdentNode equivalent to this but marked as declared here.
     */
    public IdentNode setIsDeclaredHere() {
        if (isDeclaredHere()) {
            return this;
        }
        return new IdentNode(this, name, type, flags | IS_DECLARED_HERE);
    }

    /**
     * Check if the name of this IdentNode is same as that of a compile-time property (currently __DIR__, __FILE__, and
     * __LINE__).
     *
     * @return true if this IdentNode's name is same as that of a compile-time property
     */
    public boolean isCompileTimePropertyName() {
        return name.equals(__DIR__.symbolName()) || name.equals(__FILE__.symbolName()) || name.equals(__LINE__.symbolName());
    }

    /**
     * Is this an internal symbol, i.e. one that starts with ':'. Those can
     * never be optimistic.
     * @return true if internal symbol
     */
    public boolean isInternal() {
        assert name != null;
        return name.charAt(0) == ':';
    }

    /**
     * Checks if this is a direct super identifier
     *
     * @return true if the direct super flag is set
     */
    public boolean isDirectSuper() {
        return (flags & DIRECT_SUPER) != 0;
    }

    /**
     * Return a new identifier with the direct super flag set.
     *
     * @return the new identifier
     */
    public IdentNode setIsDirectSuper() {
        return new IdentNode(this, name, type, flags | DIRECT_SUPER);
    }

    /**
     * Checks if this is a rest parameter
     *
     * @return true if the rest parameter flag is set
     */
    public boolean isRestParameter() {
        return (flags & REST_PARAMETER) != 0;
    }

    /**
     * Return a new identifier with the rest parameter flag set.
     *
     * @return the new identifier
     */
    public IdentNode setIsRestParameter() {
        return new IdentNode(this, name, type, flags | REST_PARAMETER);
    }

    /**
     * Checks if this is a proto property name.
     *
     * @return true if this is the proto property name
     */
    public boolean isProtoPropertyName() {
        return (flags & PROTO_PROPERTY) != 0;
    }

    /**
     * Return a new identifier with the proto property name flag set.
     *
     * @return the new identifier
     */
    public IdentNode setIsProtoPropertyName() {
        return new IdentNode(this, name, type, flags | PROTO_PROPERTY);
    }

    /**
     * Checks whether this is a default parameter.
     *
     * @return true if this is a default parameter
     */
    public boolean isDefaultParameter() {
        return (flags & DEFAULT_PARAMETER) != 0;
    }

    /**
     * Return a new identifier with the default parameter flag set.
     *
     * @return the new identifier
     */
    public IdentNode setIsDefaultParameter() {
        return new IdentNode(this, name, type, flags | DEFAULT_PARAMETER);
    }

    /**
     * Checks whether this is a destructured parameter.
     *
     * @return true if this is a destructured parameter
     */
    public boolean isDestructuredParameter() {
        return (flags & DESTRUCTURED_PARAMETER) != 0;
    }

    /**
     * Return a new identifier with the destructured parameter flag set.
     *
     * @return the new identifier
     */
    public IdentNode setIsDestructuredParameter() {
        return new IdentNode(this, name, type, flags | DESTRUCTURED_PARAMETER);
    }

    /**
     * Checks whether the source code for this ident contains a unicode escape sequence by comparing
     * the length of its name with its length in source code.
     *
     * @return true if ident source contains a unicode escape sequence
     */
    public boolean containsEscapes() {
        return Token.descLength(getToken()) != name.length();
    }
}
