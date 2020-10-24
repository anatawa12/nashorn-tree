/*
 * Copyright (c) 2010, 2017, Oracle and/or its affiliates. All rights reserved.
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

import com.anatawa12.nashorn.internal.codegen.Label;
import com.anatawa12.nashorn.internal.ir.annotations.Immutable;
import com.anatawa12.nashorn.internal.ir.visitor.NodeVisitor;

/**
 * IR representation for a list of statements.
 */
@Immutable
public class Block extends Node implements BreakableNode, Terminal, Flags<Block> {
    private static final long serialVersionUID = 1L;

    /** List of statements */
    protected final List<Statement> statements;

    /** Entry label. */
    private final Label entryLabel;

    /** Break label. */
    private final Label breakLabel;

    /** Does the block/function need a new scope? Is this synthetic? */
    protected final int flags;

    /** Flag indicating that this block needs scope */
    public static final int NEEDS_SCOPE        = 1 << 0;

    /**
     * Is this block tagged as terminal based on its contents
     * (usually the last statement)
     */
    public static final int IS_TERMINAL        = 1 << 2;

    /**
     * Is this block the eager global scope - i.e. the original program. This isn't true for the
     * outermost level of recompiles
     */
    public static final int IS_GLOBAL_SCOPE    = 1 << 3;

    /**
     * Is this block a synthetic one introduced by Parser?
     */
    public static final int IS_SYNTHETIC       = 1 << 4;

    /**
     * Is this the function body block? May not be the first, if parameter list contains expressions.
     */
    public static final int IS_BODY            = 1 << 5;

    /**
     * Is this the parameter initialization block? If present, must be the first block, immediately wrapping the function body block.
     */
    public static final int IS_PARAMETER_BLOCK = 1 << 6;

    /**
     * Marks the variable declaration block for case clauses of a switch statement.
     */
    public static final int IS_SWITCH_BLOCK    = 1 << 7;

    /**
     * Is this block tagged as breakable based on its contents
     * (block having labelled break statement)
     */
    public static final int IS_BREAKABLE       = 1 << 8;

    /**
     * Constructor
     *
     * @param token      The first token of the block
     * @param finish     The index of the last character
     * @param flags      The flags of the block
     * @param statements All statements in the block
     */
    public Block(final long token, final int finish, final int flags, final Statement... statements) {
        super(token, finish);

        this.statements = Arrays.asList(statements);
        this.entryLabel = new Label("block_entry");
        this.breakLabel = new Label("block_break");
        final int len = statements.length;
        final int terminalFlags = len > 0 && statements[len - 1].hasTerminalFlags() ? IS_TERMINAL : 0;
        this.flags = terminalFlags | flags;
    }

    /**
     * Constructs a new block
     *
     * @param token The first token of the block
     * @param finish The index of the last character
     * @param statements All statements in the block
     */
    public Block(final long token, final int finish, final Statement...statements){
        this(token, finish, IS_SYNTHETIC, statements);
    }

    /**
     * Constructs a new block
     *
     * @param token The first token of the block
     * @param finish The index of the last character
     * @param statements All statements in the block
     */
    public Block(final long token, final int finish, final List<Statement> statements){
        this(token, finish, IS_SYNTHETIC, statements);
    }

    /**
     * Constructor
     *
     * @param token      The first token of the block
     * @param finish     The index of the last character
     * @param flags      The flags of the block
     * @param statements All statements in the block
     */
    public Block(final long token, final int finish, final int flags, final List<Statement> statements) {
        this(token, finish, flags, statements.toArray(new Statement[0]));
    }

    private Block(final Block block, final int finish, final List<Statement> statements, final int flags) {
        super(block, finish);
        this.statements = statements;
        this.flags      = flags;
        this.entryLabel = new Label(block.entryLabel);
        this.breakLabel = new Label(block.breakLabel);
    }

    /**
     * Is this block the outermost eager global scope - i.e. the primordial program?
     * Used for global anchor point for scope depth computation for recompilation code
     * @return true if outermost eager global scope
     */
    public boolean isGlobalScope() {
        return getFlag(IS_GLOBAL_SCOPE);
    }

    /**
     * Assist in IR navigation.
     *
     * @param visitor IR navigating visitor.
     * @return new or same node
     */
    @Override
    public Node accept(final LexicalContext lc, final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterBlock(this)) {
            return visitor.leaveBlock(setStatements(lc, Node.accept(visitor, statements)));
        }

        return this;
    }

    @Override
    public int getFlags() {
        return flags;
    }

    @Override
    public Label getBreakLabel() {
        return breakLabel;
    }

    /**
     * Get the list of statements in this block
     *
     * @return a list of statements
     */
    public List<Statement> getStatements() {
        return Collections.unmodifiableList(statements);
    }

    /**
     * Returns the number of statements in the block.
     * @return the number of statements in the block.
     */
    public int getStatementCount() {
        return statements.size();
    }

    /**
     * Returns the line number of the first statement in the block.
     * @return the line number of the first statement in the block, or -1 if the block has no statements.
     */
    public int getFirstStatementLineNumber() {
        if(statements == null || statements.isEmpty()) {
            return -1;
        }
        return statements.get(0).getLineNumber();
    }

    /**
     * Returns the last statement in the block.
     * @return the last statement in the block, or null if the block has no statements.
     */
    public Statement getLastStatement() {
        return statements.isEmpty() ? null : statements.get(statements.size() - 1);
    }

    /**
     * Reset the statement list for this block
     *
     * @param lc lexical context
     * @param statements new statement list
     * @return new block if statements changed, identity of statements == block.statements
     */
    public Block setStatements(final LexicalContext lc, final List<Statement> statements) {
        if (this.statements == statements) {
            return this;
        }
        int lastFinish = 0;
        if (!statements.isEmpty()) {
            lastFinish = statements.get(statements.size() - 1).getFinish();
        }
        return Node.replaceInLexicalContext(lc, this, new Block(this, Math.max(finish, lastFinish), statements, flags));
    }

    /**
     * Check whether scope is necessary for this Block
     *
     * @return true if this function needs a scope
     */
    public boolean needsScope() {
        return (flags & NEEDS_SCOPE) == NEEDS_SCOPE;
    }

    /**
     * Check whether this block is synthetic or not.
     *
     * @return true if this is a synthetic block
     */
    public boolean isSynthetic() {
        return (flags & IS_SYNTHETIC) == IS_SYNTHETIC;
    }

    @Override
    public Block setFlags(final LexicalContext lc, final int flags) {
        if (this.flags == flags) {
            return this;
        }
        return Node.replaceInLexicalContext(lc, this, new Block(this, finish, statements, flags));
    }

    @Override
    public Block clearFlag(final LexicalContext lc, final int flag) {
        return setFlags(lc, flags & ~flag);
    }

    @Override
    public Block setFlag(final LexicalContext lc, final int flag) {
        return setFlags(lc, flags | flag);
    }

    @Override
    public boolean getFlag(final int flag) {
        return (flags & flag) == flag;
    }

    /**
     * Set the needs scope flag.
     * @param lc lexicalContext
     * @return new block if state changed, otherwise this
     */
    public Block setNeedsScope(final LexicalContext lc) {
        if (needsScope()) {
            return this;
        }

        return Node.replaceInLexicalContext(lc, this, new Block(this, finish, statements, flags | NEEDS_SCOPE));
    }

    /**
     * Determine whether this block needs to provide its scope object creator for use by its child nodes.
     * This is only necessary for synthetic parent blocks of for-in loops with lexical declarations.
     *
     * @see ForNode#needsScopeCreator()
     * @return true if child nodes need access to this block's scope creator
     */
    public boolean providesScopeCreator() {
        return needsScope() && isSynthetic()
                && (getLastStatement() instanceof ForNode)
                && ((ForNode) getLastStatement()).needsScopeCreator();
    }

    @Override
    public boolean isBreakableWithoutLabel() {
        return false;
    }

    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        return Acceptor.accept(this, visitor);
    }

    /**
     * Checks if this is a function body.
     *
     * @return true if the function body flag is set
     */
    public boolean isFunctionBody() {
        return getFlag(IS_BODY);
    }
}
