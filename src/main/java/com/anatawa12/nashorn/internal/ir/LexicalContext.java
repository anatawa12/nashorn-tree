/*
 * Copyright (c) 2010, 2015, Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.util.Iterator;
import java.util.NoSuchElementException;
import com.anatawa12.nashorn.internal.runtime.Debug;
import com.anatawa12.nashorn.internal.runtime.Source;

/**
 * A class that tracks the current lexical context of node visitation as a stack
 * of {@link Block} nodes. Has special methods to retrieve useful subsets of the
 * context.
 *
 * This is implemented with a primitive array and a stack pointer, because it
 * really makes a difference performance-wise. None of the collection classes
 * were optimal.
 */
public class LexicalContext {
    private LexicalContextNode[] stack;

    private int[] flags;
    private int sp;

    /**
     * Creates a new empty lexical context.
     */
    public LexicalContext() {
        stack = new LexicalContextNode[16];
        flags = new int[16];
    }

    /**
     * Pushes a new block on top of the context, making it the innermost open
     * block.
     *
     * @param <T> the type of the new node
     * @param node the new node
     *
     * @return the node that was pushed
     */
    public <T extends LexicalContextNode> T push(final T node) {
        assert !contains(node);
        if (sp == stack.length) {
            final LexicalContextNode[] newStack = new LexicalContextNode[sp * 2];
            System.arraycopy(stack, 0, newStack, 0, sp);
            stack = newStack;

            final int[] newFlags = new int[sp * 2];
            System.arraycopy(flags, 0, newFlags, 0, sp);
            flags = newFlags;

        }
        stack[sp] = node;
        flags[sp] = 0;

        sp++;

        return node;
    }

    /**
     * Is the context empty?
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return sp == 0;
    }

    /**
     * @return the depth of the lexical context.
     */
    public int size() {
        return sp;
    }

    /**
     * Pops the innermost block off the context and all nodes that has been
     * contributed since it was put there.
     *
     * @param <T> the type of the node to be popped
     * @param node the node expected to be popped, used to detect unbalanced
     *        pushes/pops
     *
     * @return the node that was popped
     */
    @SuppressWarnings("unchecked")
    public <T extends Node> T pop(final T node) {
        --sp;
        final LexicalContextNode popped = stack[sp];
        stack[sp] = null;
        if (popped instanceof Flags) {
            return (T)((Flags<?>)popped).setFlag(this, flags[sp]);
        }

        return (T)popped;
    }

    /**
     * Check if a node is in the lexical context.
     *
     * @param node node to check for
     *
     * @return {@code true} if in the context
     */
    public boolean contains(final LexicalContextNode node) {
        for (int i = 0; i < sp; i++) {
            if (stack[i] == node) {
                return true;
            }
        }
        return false;
    }

    /**
     * Replace a node on the lexical context with a new one. Normally
     * you should try to engineer IR traversals so this isn't needed
     *
     * @param oldNode old node
     * @param newNode new node
     *
     * @return the new node
     */
    public LexicalContextNode replace(final LexicalContextNode oldNode, final LexicalContextNode newNode) {
        for (int i = sp - 1; i >= 0; i--) {
            if (stack[i] == oldNode) {
                assert i == sp - 1 : "violation of contract - we always expect to find the replacement node on top of the lexical context stack: " + newNode + " has " + stack[i + 1].getClass() + " above it";
                stack[i] = newNode;
                break;
            }
         }
        return newNode;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("[ ");
        for (int i = 0; i < sp; i++) {
            final Object node = stack[i];
            sb.append(node.getClass().getSimpleName());
            sb.append('@');
            sb.append(Debug.id(node));
            sb.append(':');
            if (node instanceof FunctionNode) {
                final FunctionNode fn = (FunctionNode)node;
                final Source source = fn.getSource();
                String src = source.toString();
                if (src.contains(File.pathSeparator)) {
                    src = src.substring(src.lastIndexOf(File.pathSeparator));
                }
                src += ' ';
                src += fn.getLineNumber();
                sb.append(src);
            }
            sb.append(' ');
        }
        sb.append(" ==> ]");
        return sb.toString();
    }

}
