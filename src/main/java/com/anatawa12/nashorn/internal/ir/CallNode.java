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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import com.anatawa12.nashorn.internal.codegen.types.Type;
import com.anatawa12.nashorn.internal.ir.annotations.Immutable;
import com.anatawa12.nashorn.internal.ir.visitor.NodeVisitor;

/**
 * IR representation for a function call.
 */
@Immutable
public final class CallNode extends LexicalContextExpression implements Optimistic {
    private static final long serialVersionUID = 1L;

    /** Function identifier or function body. */
    private final Expression function;

    /** Call arguments. */
    private final List<Expression> args;

    /** Is this a "new" operation */
    private static final int IS_NEW = 1 << 0;

    private final int flags;

    private final int lineNumber;

    private final Type optimisticType;

    /**
     * Arguments to be passed to builtin {@code eval} function
     */
    public static class EvalArgs implements Serializable {
        private static final long serialVersionUID = 1L;
        private final List<Expression> args;

        /** location string for the eval call */
        private final String location;

        /**
         * Constructor
         *
         * @param args     arguments to eval
         * @param location location for the eval call
         */
        public EvalArgs(final List<Expression> args, final String location) {
            this.args = args;
            this.location = location;
        }

        /**
         * Return the code that is to be eval:ed by this eval function
         * @return code as an AST node
         */
        public List<Expression> getArgs() {
            return Collections.unmodifiableList(args);
        }

        private EvalArgs setArgs(final List<Expression> args) {
            if (this.args == args) {
                return this;
            }
            return new EvalArgs(args, location);
        }

        /**
         * Get the human readable location for this eval call
         * @return the location
         */
        public String getLocation() {
            return this.location;
        }
    }

    /** arguments for 'eval' call. Non-null only if this call node is 'eval' */
    private final EvalArgs evalArgs;

    /**
     * Constructors
     *
     * @param lineNumber line number
     * @param token      token
     * @param finish     finish
     * @param function   the function to call
     * @param args       args to the call
     * @param isNew      true if this is a constructor call with the "new" keyword
     */
    public CallNode(final int lineNumber, final long token, final int finish, final Expression function, final List<Expression> args, final boolean isNew) {
        super(token, finish);

        this.function       = function;
        this.args           = args;
        this.flags          = isNew ? IS_NEW : 0;
        this.evalArgs       = null;
        this.lineNumber     = lineNumber;
        this.optimisticType = null;
    }

    private CallNode(final CallNode callNode, final Expression function, final List<Expression> args, final int flags, final Type optimisticType, final EvalArgs evalArgs) {
        super(callNode);
        this.lineNumber = callNode.lineNumber;
        this.function = function;
        this.args = args;
        this.flags = flags;
        this.evalArgs = evalArgs;
        this.optimisticType = optimisticType;
    }

    /**
     * Returns the line number.
     * @return the line number.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Assist in IR navigation.
     *
     * @param visitor IR navigating visitor.
     *
     * @return node or replacement
     */
    @Override
    public Node accept(final LexicalContext lc, final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterCallNode(this)) {
            final CallNode newCallNode = (CallNode)visitor.leaveCallNode(
                    setFunction((Expression)function.accept(visitor)).
                    setArgs(Node.accept(visitor, args)).
                    setEvalArgs(evalArgs == null ?
                            null :
                            evalArgs.setArgs(Node.accept(visitor, evalArgs.getArgs()))));
            // Theoretically, we'd need to instead pass lc to every setter and do a replacement on each. In practice,
            // setType from TypeOverride can't accept a lc, and we don't necessarily want to go there now.
            if (this != newCallNode) {
                return Node.replaceInLexicalContext(lc, this, newCallNode);
            }
        }

        return this;
    }

    /**
     * Get the arguments for the call
     * @return a list of arguments
     */
    public List<Expression> getArgs() {
        return Collections.unmodifiableList(args);
    }

    /**
     * Reset the arguments for the call
     * @param args new arguments list
     * @return new callnode, or same if unchanged
     */
    public CallNode setArgs(final List<Expression> args) {
        if (this.args == args) {
            return this;
        }
        return new CallNode(this, function, args, flags, optimisticType, evalArgs);
    }

    /**
     * Set the EvalArgs structure for this call, if it has been determined it is an
     * {@code eval}
     *
     * @param evalArgs eval args
     * @return same node or new one on state change
     */
    public CallNode setEvalArgs(final EvalArgs evalArgs) {
        if (this.evalArgs == evalArgs) {
            return this;
        }
        return new CallNode(this, function, args, flags, optimisticType, evalArgs);
    }

    /**
     * Return the function expression that this call invokes
     * @return the function
     */
    public Expression getFunction() {
        return function;
    }

    /**
     * Reset the function expression that this call invokes
     * @param function the function
     * @return same node or new one on state change
     */
    public CallNode setFunction(final Expression function) {
        if (this.function == function) {
            return this;
        }
        return new CallNode(this, function, args, flags, optimisticType, evalArgs);
    }

}
