/*
 * Copyright (c) 2010, 2016, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.anatawa12.nashorn.internal.codegen.types.Type;
import com.anatawa12.nashorn.internal.ir.annotations.Immutable;
import com.anatawa12.nashorn.internal.ir.visitor.NodeVisitor;
import com.anatawa12.nashorn.internal.parser.Lexer.LexerToken;
import com.anatawa12.nashorn.internal.parser.Token;
import com.anatawa12.nashorn.internal.parser.TokenType;
import com.anatawa12.nashorn.internal.runtime.JSType;

/**
 * Literal nodes represent JavaScript values.
 *
 * @param <T> the literal type
 */
@Immutable
public abstract class LiteralNode<T> extends Expression implements PropertyKey {
    private static final long serialVersionUID = 1L;

    /** Literal value */
    protected final T value;

    /**
     * Constructor
     *
     * @param token   token
     * @param finish  finish
     * @param value   the value of the literal
     */
    protected LiteralNode(final long token, final int finish, final T value) {
        super(token, finish);
        this.value = value;
    }

    /**
     * Copy constructor
     *
     * @param literalNode source node
     */
    protected LiteralNode(final LiteralNode<T> literalNode) {
        this(literalNode, literalNode.value);
    }

    /**
     * A copy constructor with value change.
     * @param literalNode the original literal node
     * @param newValue new value for this node
     */
    protected LiteralNode(final LiteralNode<T> literalNode, final T newValue) {
        super(literalNode);
        this.value = newValue;
    }

    /**
     * Check if the literal value is null
     * @return true if literal value is null
     */
    public boolean isNull() {
        return value == null;
    }

    @Override
    public String getPropertyName() {
        return JSType.toString(getObject());
    }

    /**
     * Fetch boolean value of node.
     *
     * @return boolean value of node.
     */
    public boolean getBoolean() {
        return JSType.toBoolean(value);
    }

    /**
     * Fetch Object value of node.
     *
     * @return Object value of node.
     */
    public Object getObject() {
        return value;
    }

    /**
     * Test if the value is an array
     *
     * @return True if value is an array
     */
    public boolean isArray() {
        return false;
    }

    public List<Expression> getElementExpressions() {
        return null;
    }

    /**
     * Test if the value is a boolean.
     *
     * @return True if value is a boolean.
     */
    public boolean isBoolean() {
        return value instanceof Boolean;
    }

    /**
     * Test if the value is a string.
     *
     * @return True if value is a string.
     */
    public boolean isString() {
        return value instanceof String;
    }

    /**
     * Test if tha value is a number
     *
     * @return True if value is a number
     */
    public boolean isNumeric() {
        return value instanceof Number;
    }

    /**
     * Assist in IR navigation.
     *
     * @param visitor IR navigating visitor.
     */
    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterLiteralNode(this)) {
            return visitor.leaveLiteralNode(this);
        }

        return this;
    }

    /**
     * Get the literal node value
     * @return the value
     */
    public final T getValue() {
        return value;
    }

    private static Expression[] valueToArray(final List<Expression> value) {
        return value.toArray(new Expression[0]);
    }

    /**
     * Create a new null literal
     *
     * @param token   token
     * @param finish  finish
     *
     * @return the new literal node
     */
    public static LiteralNode<Object> newInstance(final long token, final int finish) {
        return new NullLiteralNode(token, finish);
    }

    /**
     * Create a new null literal based on a parent node (source, token, finish)
     *
     * @param parent parent node
     *
     * @return the new literal node
     */
    public static LiteralNode<Object> newInstance(final Node parent) {
        return new NullLiteralNode(parent.getToken(), parent.getFinish());
    }

    /**
     * Super class for primitive (side-effect free) literals.
     *
     * @param <T> the literal type
     */
    public static class PrimitiveLiteralNode<T> extends LiteralNode<T> {
        private static final long serialVersionUID = 1L;

        private PrimitiveLiteralNode(final long token, final int finish, final T value) {
            super(token, finish, value);
        }

        /**
         * Check if the literal value is boolean true
         * @return true if literal value is boolean true
         */
        public boolean isTrue() {
            return JSType.toBoolean(value);
        }

        @Override
        public boolean isAlwaysFalse() {
            return !isTrue();
        }

        @Override
        public boolean isAlwaysTrue() {
            return isTrue();
        }
    }

    @Immutable
    private static final class BooleanLiteralNode extends PrimitiveLiteralNode<Boolean> {
        private static final long serialVersionUID = 1L;

        private BooleanLiteralNode(final long token, final int finish, final boolean value) {
            super(Token.recast(token, value ? TokenType.TRUE : TokenType.FALSE), finish, value);
        }

        @Override
        public boolean isTrue() {
            return value;
        }

    }

    /**
     * Create a new boolean literal
     *
     * @param token   token
     * @param finish  finish
     * @param value   true or false
     *
     * @return the new literal node
     */
    public static LiteralNode<Boolean> newInstance(final long token, final int finish, final boolean value) {
        return new BooleanLiteralNode(token, finish, value);
    }

    @Immutable
    private static final class NumberLiteralNode extends PrimitiveLiteralNode<Number> {
        private static final long serialVersionUID = 1L;

        private NumberLiteralNode(final long token, final int finish, final Number value) {
            super(Token.recast(token, TokenType.DECIMAL), finish, value);
        }

    }
    /**
     * Create a new number literal
     *
     * @param token   token
     * @param finish  finish
     * @param value   literal value
     *
     * @return the new literal node
     */
    public static LiteralNode<Number> newInstance(final long token, final int finish, final Number value) {
        assert !(value instanceof Long);
        return new NumberLiteralNode(token, finish, value);
    }

    /**
     * Create a new number literal based on a parent node (source, token, finish)
     *
     * @param parent parent node
     * @param value  literal value
     *
     * @return the new literal node
     */
    public static LiteralNode<?> newInstance(final Node parent, final Number value) {
        return new NumberLiteralNode(parent.getToken(), parent.getFinish(), value);
    }

    @Immutable
    private static class StringLiteralNode extends PrimitiveLiteralNode<String> {
        private static final long serialVersionUID = 1L;

        private StringLiteralNode(final long token, final int finish, final String value) {
            super(Token.recast(token, TokenType.STRING), finish, value);
        }
    }

    /**
     * Create a new string literal
     *
     * @param token   token
     * @param finish  finish
     * @param value   string value
     *
     * @return the new literal node
     */
    public static LiteralNode<String> newInstance(final long token, final int finish, final String value) {
        return new StringLiteralNode(token, finish, value);
    }

    /**
     * Create a new String literal based on a parent node (source, token, finish)
     *
     * @param parent parent node
     * @param value  string value
     *
     * @return the new literal node
     */
    public static LiteralNode<?> newInstance(final Node parent, final String value) {
        return new StringLiteralNode(parent.getToken(), parent.getFinish(), value);
    }

    @Immutable
    private static class LexerTokenLiteralNode extends LiteralNode<LexerToken> {
        private static final long serialVersionUID = 1L;

        private LexerTokenLiteralNode(final long token, final int finish, final LexerToken value) {
            super(Token.recast(token, TokenType.STRING), finish, value); //TODO is string the correct token type here?
        }

    }

    /**
     * Create a new literal node for a lexer token
     *
     * @param token   token
     * @param finish  finish
     * @param value   lexer token value
     *
     * @return the new literal node
     */
    public static LiteralNode<LexerToken> newInstance(final long token, final int finish, final LexerToken value) {
        return new LexerTokenLiteralNode(token, finish, value);
    }

    private static final class NullLiteralNode extends PrimitiveLiteralNode<Object> {
        private static final long serialVersionUID = 1L;

        private NullLiteralNode(final long token, final int finish) {
            super(Token.recast(token, TokenType.OBJECT), finish, null);
        }

        @Override
        public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
            if (visitor.enterLiteralNode(this)) {
                return visitor.leaveLiteralNode(this);
            }

            return this;
        }

    }

    /**
     * Array literal node class.
     */
    @Immutable
    public static final class ArrayLiteralNode extends LiteralNode<Expression[]> implements LexicalContextNode, Splittable {
        private static final long serialVersionUID = 1L;

        /** Array element type. */
        private final Type elementType;

        /** Preset constant array. */
        private final Object presets;

        /** Indices of array elements requiring computed post sets. */
        private final int[] postsets;

        /** Does this array literal have a spread element? */
        private final boolean hasSpread;

        /** Does this array literal have a trailing comma?*/
        private final boolean hasTrailingComma;

        @Override
        public boolean isArray() {
            return true;
        }


        /**
         * Constructor
         *
         * @param token   token
         * @param finish  finish
         * @param value   array literal value, a Node array
         */
        protected ArrayLiteralNode(final long token, final int finish, final Expression[] value) {
            this(token, finish, value, false, false);
        }

        /**
         * Constructor
         *
         * @param token   token
         * @param finish  finish
         * @param value   array literal value, a Node array
         * @param hasSpread true if the array has a spread element
         * @param hasTrailingComma true if the array literal has a comma after the last element
         */
        protected ArrayLiteralNode(final long token, final int finish, final Expression[] value, final boolean hasSpread, final boolean hasTrailingComma) {
            super(Token.recast(token, TokenType.ARRAY), finish, value);
            this.elementType = Type.UNKNOWN;
            this.presets     = null;
            this.postsets    = null;
            this.hasSpread        = hasSpread;
            this.hasTrailingComma = hasTrailingComma;
        }

        /**
         * Copy constructor
         * @param node source array literal node
         */
        private ArrayLiteralNode(final ArrayLiteralNode node, final Expression[] value, final Type elementType, final int[] postsets, final Object presets) {
            super(node, value);
            this.elementType = elementType;
            this.postsets    = postsets;
            this.presets     = presets;
            this.hasSpread        = node.hasSpread;
            this.hasTrailingComma = node.hasTrailingComma;
        }

        /**
         * Returns {@code true} if this array literal has a spread element.
         * @return true if this literal has a spread element
         */
        public boolean hasSpread() {
            return hasSpread;
        }

        /**
         * Returns {@code true} if this array literal has a trailing comma.
         * @return true if this literal has a trailing comma
         */
        public boolean hasTrailingComma() {
             return hasTrailingComma;
        }

        /**
         * Returns a list of array element expressions. Note that empty array elements manifest themselves as
         * null.
         * @return a list of array element expressions.
         */
        @Override
        public List<Expression> getElementExpressions() {
            return Collections.unmodifiableList(Arrays.asList(value));
        }

        /**
         * Get indices of arrays containing computed post sets. post sets
         * are things like non literals e.g. "x+y" instead of i or 17
         * @return post set indices
         */
        public int[] getPostsets() {
            assert postsets != null : this + " elementType=" + elementType + " has no postsets";
            return postsets;
        }

        @Override
        public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
            return Acceptor.accept(this, visitor);
        }

        @Override
        public Node accept(final LexicalContext lc, final NodeVisitor<? extends LexicalContext> visitor) {
            if (visitor.enterLiteralNode(this)) {
                final List<Expression> oldValue = Arrays.asList(value);
                final List<Expression> newValue = Node.accept(visitor, oldValue);
                return visitor.leaveLiteralNode(oldValue != newValue ? setValue(lc, newValue) : this);
            }
            return this;
        }

        private ArrayLiteralNode setValue(final LexicalContext lc, final Expression[] value) {
            if (this.value == value) {
                return this;
            }
            return Node.replaceInLexicalContext(lc, this, new ArrayLiteralNode(this, value, elementType, postsets, presets));
        }

        private ArrayLiteralNode setValue(final LexicalContext lc, final List<Expression> value) {
            return setValue(lc, value.toArray(new Expression[0]));
        }
    }

    /**
     * Create a new array literal of Nodes from a list of Node values
     *
     * @param token   token
     * @param finish  finish
     * @param value   literal value list
     *
     * @return the new literal node
     */
    public static LiteralNode<Expression[]> newInstance(final long token, final int finish, final List<Expression> value) {
        return new ArrayLiteralNode(token, finish, valueToArray(value));
    }

    /*
     * Create a new array literal of Nodes from a list of Node values
     *
     * @param token token
     * @param finish finish
     * @param value literal value list
     * @param hasSpread true if the array has a spread element
     * @param hasTrailingComma true if the array literal has a comma after the last element
     *
     * @return the new literal node
     */
    public static LiteralNode<Expression[]> newInstance(final long token, final int finish, final List<Expression> value,
                                                        final boolean hasSpread, final boolean hasTrailingComma) {
        return new ArrayLiteralNode(token, finish, valueToArray(value), hasSpread, hasTrailingComma);
    }


}
