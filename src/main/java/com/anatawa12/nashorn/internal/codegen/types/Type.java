/*
 * Copyright (c) 2010, 2016, Oracle and/or its affiliates. All rights reserved.
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

package com.anatawa12.nashorn.internal.codegen.types;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.anatawa12.nashorn.internal.runtime.ScriptObject;
import com.anatawa12.nashorn.internal.runtime.Undefined;

/**
 * This is the representation of a JavaScript type, disassociated from java
 * Classes, with the basis for conversion weight, mapping to ASM types
 * and implementing the ByteCodeOps interface which tells this type
 * how to generate code for various operations.
 *
 * Except for ClassEmitter, this is the only class that has to know
 * about the underlying byte code generation system.
 *
 * The different types know how to generate bytecode for the different
 * operations, inherited from BytecodeOps, that they support. This avoids
 * if/else chains depending on type in several cases and allows for
 * more readable and shorter code
 *
 * The Type class also contains logic used by the type inference and
 * for comparing types against each other, as well as the concepts
 * of narrower to wider types. The widest type is an object. Ideally we
 * would like as narrow types as possible for code to be efficient, e.g
 * INTs rather than OBJECTs
 */

public abstract class Type implements Comparable<Type>, Serializable {
    private static final long serialVersionUID = 1L;

    /** Human readable name for type */
    private transient final String name;

    /** Descriptor for type */
    private transient final String descriptor;

    /** The "weight" of the type. Used for picking widest/least specific common type */
    private transient final int weight;

    /** The class for this type */
    private final Class<?> clazz;

    /** Weights are used to decide which types are "wider" than other types */
    protected static final int MIN_WEIGHT = -1;

    /** Set way below Integer.MAX_VALUE to prevent overflow when adding weights. Objects are still heaviest. */
    protected static final int MAX_WEIGHT = 20;

    /**
     * Constructor
     *
     * @param clazz       class for type
     * @param weight      weight - higher is more generic
     * @param slots       how many bytecode slots the type takes up
     */
    Type(final String name, final Class<?> clazz, final int weight, final int slots) {
        this.name         = name;
        this.clazz        = clazz;
        this.descriptor   = computeDescriptor(clazz);
        this.weight       = weight;
        assert weight >= MIN_WEIGHT && weight <= MAX_WEIGHT : "illegal type weight: " + weight;
    }

    private static String computeDescriptor(final Class<?> c) {
        if (c.isPrimitive()) {
            if (c == Integer.TYPE) {
                return "I";
            } else if (c == Void.TYPE) {
                return "V";
            } else if (c == Boolean.TYPE) {
                return "Z";
            } else if (c == Byte.TYPE) {
                return "B";
            } else if (c == Character.TYPE) {
                return "C";
            } else if (c == Short.TYPE) {
                return "S";
            } else if (c == Double.TYPE) {
                return "D";
            } else if (c == Float.TYPE) {
                return "F";
            } else /* if (c == Long.TYPE) */{
                return "J";
            }
        }
        return c.getName().replace('.', '/');
    }

    /**
     * Get the Class representing this type
     * @return the class for this type
     */
    public Class<?> getTypeClass() {
        return clazz;
    }

    /**
     * Returns the weight of a type, used for type comparison
     * between wider and narrower types
     *
     * @return the weight
     */
    int weight() {
        return weight;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Return the (possibly cached) Type object for this class
     *
     * @param clazz the class to check
     *
     * @return the Type representing this class
     */
    public static Type typeFor(final Class<?> clazz) {
        return cache.computeIfAbsent(clazz, (keyClass) -> {
            assert !keyClass.isPrimitive() || keyClass == void.class;
            return keyClass.isArray() ? new ArrayType(keyClass) : new ObjectType(keyClass);
        });
    }

    @Override
    public int compareTo(final Type o) {
        return o.weight() - weight();
    }

    /** Mappings between java classes and their Type singletons */
    private static final ConcurrentMap<Class<?>, Type> cache = new ConcurrentHashMap<>();


    /**
     * This is the object singleton, used for all object types
     */
    public static final Type OBJECT = putInCache(new ObjectType());

    /**
     * This is the singleton for ScriptObjects
     */
    public static final Type SCRIPT_OBJECT = putInCache(new ObjectType(ScriptObject.class));

    private static interface Unknown {
        // EMPTY - used as a class that is absolutely not compatible with a type to represent "unknown"
    }

    private abstract static class ValueLessType extends Type {
        private static final long serialVersionUID = 1L;

        ValueLessType(final String name) {
            super(name, Unknown.class, MIN_WEIGHT, 1);
        }
    }

    /**
     * This is the unknown type which is used as initial type for type
     * inference. It has the minimum type width
     */
    public static final Type UNKNOWN = new ValueLessType("<unknown>") {
        private static final long serialVersionUID = 1L;

    };

    private static <T extends Type> T putInCache(final T type) {
        cache.put(type.getTypeClass(), type);
        return type;
    }

    /**
     * Read resolve
     * @return resolved type
     */
    protected final Object readResolve() {
        return Type.typeFor(clazz);
    }
}
