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

package com.anatawa12.nashorn.internal.codegen;

import java.io.Serializable;

import com.anatawa12.nashorn.internal.ir.CompileUnitHolder;

/**
  * Used to track split class compilation. Note that instances of the class are serializable, but all fields are
  * transient, making the serialized version of the class only useful for tracking the referential topology of other
  * AST nodes referencing the same or different compile units. We do want to preserve this topology though as
  * {@link CompileUnitHolder}s in a deserialized AST will undergo reinitialization.
  */
public final class CompileUnit implements Comparable<CompileUnit>, Serializable {
    private static final long serialVersionUID = 1L;

    /** Current class name */
    private transient final String className;

    private static int emittedUnitCount;

    CompileUnit(final String className) {
        this.className    = className;
    }

    /**
     * Get the amount of emitted compile units so far in the system
     * @return emitted compile unit count
     */
    public static int getEmittedUnitCount() {
        return emittedUnitCount;
    }

    /**
     * Get the class name for this compile unit
     * @return the class name
     */
    public String getUnitClassName() {
        return className;
    }

    @Override
    public int compareTo(final CompileUnit o) {
        return className.compareTo(o.className);
    }
}
