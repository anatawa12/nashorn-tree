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

package com.anatawa12.nashorn.internal.runtime;

/**
 * Unique instance of this class is used to represent JavaScript undefined.
 */
public final class Undefined extends DefaultPropertyAccess {

    private Undefined() {
    }

    private static final Undefined UNDEFINED = new Undefined();
    private static final Undefined EMPTY     = new Undefined();

    /**
     * Get the value of {@code undefined}, this is represented as a global singleton
     * instance of this class. It can always be reference compared
     *
     * @return the undefined object
     */
    public static Undefined getUndefined() {
        return UNDEFINED;
    }

    /**
     * Get the value of {@code empty}. This is represented as a global singleton
     * instanceof this class. It can always be reference compared.
     * <p>
     * We need empty to differentiate behavior in things like array iterators
     * <p>
     * @return the empty object
     */
    public static Undefined getEmpty() {
        return EMPTY;
    }
}
