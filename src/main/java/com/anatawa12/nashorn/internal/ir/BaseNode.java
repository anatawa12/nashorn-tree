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

/**
 * IR base for accessing/indexing nodes.
 *
 * @see AccessNode
 * @see IndexNode
 */
@Immutable
public abstract class BaseNode extends Expression implements Optimistic {
    private static final long serialVersionUID = 1L;

    /** Base Node. */
    protected final Expression base;

    private final boolean isFunction;

    /** Callsite type for this node, if overridden optimistically or conservatively depending on coercion */
    protected final Type type;

    /** Super property access. */
    private final boolean isSuper;

    /**
     * Constructor
     *
     * @param token  token
     * @param finish finish
     * @param base   base node
     * @param isFunction is this a function
     * @param isSuper is this a super property access
     */
    public BaseNode(final long token, final int finish, final Expression base, final boolean isFunction, final boolean isSuper) {
        super(token, base.getStart(), finish);
        this.base           = base;
        this.isFunction     = isFunction;
        this.type = null;
        this.isSuper        = isSuper;
    }

    /**
     * Copy constructor for immutable nodes
     * @param baseNode node to inherit from
     * @param base base
     * @param isFunction is this a function
     * @param callSiteType  the callsite type for this base node, either optimistic or conservative
     * @param isSuper is this a super property access
     */
    protected BaseNode(final BaseNode baseNode, final Expression base, final boolean isFunction, final Type callSiteType, final boolean isSuper) {
        super(baseNode);
        this.base           = base;
        this.isFunction     = isFunction;
        this.type = callSiteType;
        this.isSuper        = isSuper;
    }

    /**
     * Get the base node for this access
     * @return the base node
     */
    public Expression getBase() {
        return base;
    }

    public boolean isFunction() {
        return isFunction;
    }

    /**
     * @return {@code true} if a SuperProperty access.
     */
    public boolean isSuper() {
        return isSuper;
    }

    /**
     * Mark this node as being a SuperProperty access.
     *
     * @return  a base node identical to this one in all aspects except with its super flag set.
     */
    public abstract BaseNode setIsSuper();
}
