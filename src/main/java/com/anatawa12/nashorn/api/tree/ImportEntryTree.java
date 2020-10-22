/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
package com.anatawa12.nashorn.api.tree;

import java.util.List;

/**
 * A Tree node for import entry of <a href="http://www.ecma-international.org/ecma-262/6.0/#sec-modules">Module information</a>.
 *
// * @deprecated Nashorn JavaScript script engine and APIs, and the jjs tool
// * are deprecated with the intent to remove them in a future release.
 */
public interface ImportEntryTree extends Tree {
    /**
     * Returns the entry's module request.
     *
     * @return the module request
     */
    public IdentifierTree getModuleRequest();

    /**
     * Returns the entry's import name.
     *
     * @return the import name
     */
    public IdentifierTree getImportName();

    /**
     * Returns the entry's local name.
     *
     * @return the local name
     */
    public IdentifierTree getLocalName();
}
