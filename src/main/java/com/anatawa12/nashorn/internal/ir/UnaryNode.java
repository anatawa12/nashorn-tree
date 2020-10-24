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

import com.anatawa12.nashorn.internal.codegen.types.Type;
import com.anatawa12.nashorn.internal.ir.annotations.Immutable;
import com.anatawa12.nashorn.internal.ir.visitor.NodeVisitor;
import com.anatawa12.nashorn.internal.parser.Token;

/**
 * UnaryNode nodes represent single operand operations.
 */
@Immutable
public final class UnaryNode extends Expression implements Assignment<Expression>, Optimistic {
    private static final long serialVersionUID = 1L;

    /** Right hand side argument. */
    private final Expression expression;

    private final Type type;

    /**
     * Constructor
     *
     * @param token  token
     * @param rhs    expression
     */
    public UnaryNode(final long token, final Expression rhs) {
        this(token, Math.min(rhs.getStart(), Token.descPosition(token)), Math.max(Token.descPosition(token) + Token.descLength(token), rhs.getFinish()), rhs);
    }

    /**
     * Constructor
     *
     * @param token      token
     * @param start      start
     * @param finish     finish
     * @param expression expression
     */
    public UnaryNode(final long token, final int start, final int finish, final Expression expression) {
        super(token, start, finish);
        this.expression   = expression;
        this.type = null;
    }


    private UnaryNode(final UnaryNode unaryNode, final Expression expression, final Type type) {
        super(unaryNode);
        this.expression   = expression;
        this.type = type;
    }

    /**
     * Is this an assignment - i.e. that mutates something such as a++
     *
     * @return true if assignment
     */
    @Override
    public boolean isAssignment() {
        switch (tokenType()) {
        case DECPOSTFIX:
        case DECPREFIX:
        case INCPOSTFIX:
        case INCPREFIX:
            return true;
        default:
            return false;
        }
    }

    @Override
    public Expression getAssignmentDest() {
        return isAssignment() ? getExpression() : null;
    }

    @Override
    public Expression getAssignmentSource() {
        return getAssignmentDest();
    }

    /**
     * Assist in IR navigation.
     * @param visitor IR navigating visitor.
     */
    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterUnaryNode(this)) {
            return visitor.leaveUnaryNode(setExpression((Expression)expression.accept(visitor)));
        }

        return this;
    }

    /**
     * Get the right hand side of this if it is inherited by a binary expression,
     * or just the expression itself if still Unary
     *
     * @see BinaryNode
     *
     * @return right hand side or expression node
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Reset the right hand side of this if it is inherited by a binary expression,
     * or just the expression itself if still Unary
     *
     * @see BinaryNode
     *
     * @param expression right hand side or expression node
     * @return a node equivalent to this one except for the requested change.
     */
    public UnaryNode setExpression(final Expression expression) {
        if (this.expression == expression) {
            return this;
        }
        return new UnaryNode(this, expression, type);
    }

}
