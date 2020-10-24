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

/**
 * Abstraction for labels, separating a label from the underlying
 * byte code emitter. Also augmenting label with e.g. a name
 * for easier debugging and reading code
 *
 * see -Dnashorn.codegen.debug, --log=codegen
 */
public final class Label implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Next id for debugging purposes, remove if footprint becomes unmanageable */
    private static int nextId = 0;

    /** Name of this label */
    private final String name;

    /** Id for debugging purposes, remove if footprint becomes unmanageable */
    private final int id;

    /**
     * Constructor
     *
     * @param name name of this label
     */
    public Label(final String name) {
        super();
        this.name = name;
        this.id   = nextId++;
    }

    /**
     * Copy constructor
     *
     * @param label a label to clone
     */
    public Label(final Label label) {
        super();
        this.name = label.name;
        this.id   = label.id;
    }

    private String str;

    @Override
    public String toString() {
        if (str == null) {
            str = name + '_' + id;
        }
        return str;
    }
}
