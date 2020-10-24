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

package com.anatawa12.nashorn.internal.runtime;

/**
 * Base class for generic JavaScript objects.
 * <p>
 * Notes:
 * <ul>
 * <li>The map is used to identify properties in the object.</li>
 * <li>If the map is modified then it must be cloned and replaced.  This notifies
 *     any code that made assumptions about the object that things have changed.
 *     Ex. CallSites that have been validated must check to see if the map has
 *     changed (or a map from a different object type) and hence relink the method
 *     to call.</li>
 * <li>Modifications of the map include adding/deleting attributes or changing a
 *     function field value.</li>
 * </ul>
 */
// only for class leteral
public abstract class ScriptObject {

    public ScriptObject() {
    }
}
