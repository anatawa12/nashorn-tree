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
import com.anatawa12.nashorn.internal.parser.TokenType;

/**
 * BinaryNode nodes represent two operand operations.
 */
@Immutable
public final class BinaryNode extends Expression implements Assignment<Expression>, Optimistic {
    private static final long serialVersionUID = 1L;

    /** Left hand side argument. */
    private final Expression lhs;

    private final Expression rhs;

    private final Type type;

    /**
     * Constructor
     *
     * @param token  token
     * @param lhs    left hand side
     * @param rhs    right hand side
     */
    public BinaryNode(final long token, final Expression lhs, final Expression rhs) {
        super(token, lhs.getStart(), rhs.getFinish());
        assert !(isTokenType(TokenType.AND) || isTokenType(TokenType.OR)) || lhs instanceof JoinPredecessorExpression;
        this.lhs   = lhs;
        this.rhs   = rhs;
        this.type = null;
    }

    private BinaryNode(final BinaryNode binaryNode, final Expression lhs, final Expression rhs, final Type type) {
        super(binaryNode);
        this.lhs = lhs;
        this.rhs = rhs;
        this.type = type;
    }

    /**
     * Returns true if the token type represents a logical operation.
     * @param tokenType the token type
     * @return true if the token type represents a logical operation.
     */
    public static boolean isLogical(final TokenType tokenType) {
        switch (tokenType) {
        case AND:
        case OR:
            return true;
        default:
            return false;
        }
    }

    /**
     * Check if this node is an assignment
     *
     * @return true if this node assigns a value
     */
    @Override
    public boolean isAssignment() {
        switch (tokenType()) {
        case ASSIGN:
        case ASSIGN_ADD:
        case ASSIGN_BIT_AND:
        case ASSIGN_BIT_OR:
        case ASSIGN_BIT_XOR:
        case ASSIGN_DIV:
        case ASSIGN_MOD:
        case ASSIGN_MUL:
        case ASSIGN_SAR:
        case ASSIGN_SHL:
        case ASSIGN_SHR:
        case ASSIGN_SUB:
           return true;
        default:
           return false;
        }
    }

    @Override
    public Expression getAssignmentDest() {
        return isAssignment() ? lhs() : null;
    }

    @Override
    public Expression getAssignmentSource() {
        return rhs();
    }

    /**
     * Assist in IR navigation.
     * @param visitor IR navigating visitor.
     */
    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterBinaryNode(this)) {
            return visitor.leaveBinaryNode(setLHS((Expression)lhs.accept(visitor)).setRHS((Expression)rhs.accept(visitor)));
        }

        return this;
    }

    @Override
    public boolean isAlwaysFalse() {
        switch (tokenType()) {
        case COMMARIGHT:
            return rhs.isAlwaysFalse();
        default:
            return false;
        }
    }

    @Override
    public boolean isAlwaysTrue() {
        switch (tokenType()) {
        case COMMARIGHT:
            return rhs.isAlwaysTrue();
        default:
            return false;
        }
    }

    /**
     * Get the left hand side expression for this node
     * @return the left hand side expression
     */
    public Expression lhs() {
        return lhs;
    }

    /**
     * Get the right hand side expression for this node
     * @return the left hand side expression
     */
    public Expression rhs() {
        return rhs;
    }

    /**
     * Set the left hand side expression for this node
     * @param lhs new left hand side expression
     * @return a node equivalent to this one except for the requested change.
     */
    public BinaryNode setLHS(final Expression lhs) {
        if (this.lhs == lhs) {
            return this;
        }
        return new BinaryNode(this, lhs, rhs, type);
    }

    /**
     * Set the right hand side expression for this node
     * @param rhs new right hand side expression
     * @return a node equivalent to this one except for the requested change.
     */
    public BinaryNode setRHS(final Expression rhs) {
        if (this.rhs == rhs) {
            return this;
        }
        return new BinaryNode(this, lhs, rhs, type);
    }

}
