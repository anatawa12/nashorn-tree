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

/**
 * This is the main function responsible for emitting method code
 * in a class. It maintains a type stack and keeps track of control
 * flow to make sure that the registered instructions don't violate
 * byte code verification.
 *
 * Running Nashorn with -ea will assert as soon as a type stack
 * becomes corrupt, for easier debugging
 *
 * Running Nashorn with -Dnashorn.codegen.debug=true will print
 * all generated bytecode and labels to stderr, for easier debugging,
 * including bytecode stack contents
 */
public class MethodEmitter {

    /** Threshold in chars for when string constants should be split */
    static final int LARGE_STRING_THRESHOLD = 32 * 1024;

}
