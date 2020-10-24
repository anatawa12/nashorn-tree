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
 * This class represents constant names of variables, methods and fields in
 * the compiler
 */

public enum CompilerConstants {
    /** the __FILE__ variable */
    __FILE__,

    /** the __DIR__ variable */
    __DIR__,

    /** the __LINE__ variable */
    __LINE__,

    /** eval name */
    EVAL("eval"),

    /** function prefix for anonymous functions */
    ANON_FUNCTION_PREFIX("L:"),

    /** separator for method names of nested functions */
    NESTED_FUNCTION_SEPARATOR("#"),

    /** separator for making method names unique by appending numeric ids */
    ID_FUNCTION_SEPARATOR("-"),

    /** method name for Java method that is the program entry point */
    PROGRAM(":program"),

    /** the arguments variable (visible to function body). Initially set to ARGUMENTS, but can be reassigned by code in
     * the function body.*/
    ARGUMENTS_VAR("arguments", Object.class);

    /** To save memory - intern the compiler constant symbol names, as they are frequently reused */
    static {
        for (final CompilerConstants c : values()) {
            final String symbolName = c.symbolName();
            if (symbolName != null) {
                symbolName.intern();
            }
        }
    }

    /**
     * Prefix used for internal methods generated in script classes.
     */
    private static final String INTERNAL_METHOD_PREFIX = ":";

    private final String symbolName;
    private final Class<?> type;
    private final int slot;

    private CompilerConstants() {
        this.symbolName = name();
        this.type = null;
        this.slot = -1;
    }

    private CompilerConstants(final String symbolName) {
        this(symbolName, -1);
    }

    private CompilerConstants(final String symbolName, final int slot) {
        this(symbolName, null, slot);
    }

    private CompilerConstants(final String symbolName, final Class<?> type) {
        this(symbolName, type, -1);
    }

    private CompilerConstants(final String symbolName, final Class<?> type, final int slot) {
        this.symbolName = symbolName;
        this.type       = type;
        this.slot       = slot;
    }

    /**
     * Return the tag for this compile constant. Deliberately avoiding "name" here
     * not to conflate with enum implementation. This is the master string for the
     * constant - every constant has one.
     *
     * @return the tag
     */
    public final String symbolName() {
        return symbolName;
    }

    /**
     * Return the type for this compile constant
     *
     * @return type for this constant's instances, or null if N/A
     */
    public final Class<?> type() {
        return type;
    }

    /**
     * Return the slot for this compile constant
     *
     * @return byte code slot where constant is stored or -1 if N/A
     */
    public final int slot() {
        return slot;
    }

    /**
     * Returns true if the passed string looks like a method name of an internally generated Nashorn method. Basically,
     * if it starts with a colon character {@code :} but is not the name of the program method {@code :program}.
     * Program function is not considered internal as we want it to show up in exception stack traces.
     * @param methodName the name of a method
     * @return true if it looks like an internal Nashorn method name.
     * @throws NullPointerException if passed null
     */
    public static boolean isInternalMethodName(final String methodName) {
        return methodName.startsWith(INTERNAL_METHOD_PREFIX) && !methodName.equals(PROGRAM.symbolName);
     }

}
