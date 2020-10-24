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

/**
 * Is this a node that can be optimistically typed? This means that it
 * has a probable type but it's not available through static analysis
 *
 * The follow nodes are optimistic, with reasons therefore given within
 * parenthesis
 *
 * @see IndexNode  (dynamicGetIndex)
 * @see BinaryNode (local calculations to strongly typed bytecode)
 * @see UnaryNode  (local calculations to strongly typed bytecode)
 * @see CallNode   (dynamicCall)
 * @see AccessNode (dynamicGet)
 * @see IdentNode  (dynamicGet)
 */
public interface Optimistic {

}
