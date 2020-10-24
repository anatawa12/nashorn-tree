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

import com.anatawa12.nashorn.internal.runtime.doubleconv.DoubleConversion;

/**
 * Representation for ECMAScript types - this maps directly to the ECMA script standard
 */
public enum JSType {

    ;

    // Minimum and maximum range between which every long value can be precisely represented as a double.
    private static final long MAX_PRECISE_DOUBLE = 1L << 53;

    /**
     * Constructor
     *
     */
    private JSType() {
    }

    /**
     * Returns true if double number can be represented as an int. Note that it returns true for negative
     * zero. If you need to excl
     * ude negative zero, use {@link #isStrictlyRepresentableAsInt(double)}.
     *
     * @param number a double to inspect
     *
     * @return true for int representable doubles
     */
    public static boolean isRepresentableAsInt(final double number) {
        return (int)number == number;
    }

    /**
     * Returns true if double number can be represented as an int. Note that it returns false for negative
     * zero. If you don't need to distinguish negative zero, use {@link #isRepresentableAsInt(double)}.
     *
     * @param number a double to inspect
     *
     * @return true for int representable doubles
     */
    public static boolean isStrictlyRepresentableAsInt(final double number) {
        return isRepresentableAsInt(number) && isNotNegativeZero(number);
    }

    /**
     * Returns true if the number is not the negative zero ({@code -0.0d}).
     * @param number the number to test
     * @return true if it is not the negative zero, false otherwise.
     */
    private static boolean isNotNegativeZero(final double number) {
        return Double.doubleToRawLongBits(number) != 0x8000000000000000L;
    }

    /**
     * Check whether an object is primitive
     *
     * @param obj an object
     *
     * @return true if object is primitive (includes null and undefined)
     */
    public static boolean isPrimitive(final Object obj) {
        return obj == null ||
               isString(obj) ||
               isNumber(obj) ||
               obj instanceof Boolean;
    }

    /**
     * Primitive converter for an object including type hint
     * See ECMA 9.1 ToPrimitive
     *
     * @param obj  an object
     * @return the primitive form of the object
     */
    public static Object toPrimitive(final Object obj, final Class<?> hint) {
        if (isPrimitive(obj)) {
            return obj;
        } else if (hint == Number.class && obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        return obj.toString();
    }

    /**
     * JavaScript compliant conversion of Object to boolean
     * See ECMA 9.2 ToBoolean
     *
     * @param obj an object
     *
     * @return a boolean
     */
    public static boolean toBoolean(final Object obj) {
        if (obj instanceof Boolean) {
            return (Boolean)obj;
        }

        if (nullOrUndefined(obj)) {
            return false;
        }

        if (obj instanceof Number) {
            final double num = ((Number)obj).doubleValue();
            return num != 0 && !Double.isNaN(num);
        }

        if (isString(obj)) {
            return ((CharSequence)obj).length() > 0;
        }

        return true;
    }


    /**
     * JavaScript compliant converter of Object to String
     * See ECMA 9.8 ToString
     *
     * @param obj an object
     *
     * @return a string
     */
    public static String toString(final Object obj) {
        return toStringImpl(obj);
    }

    /**
     * Returns true if object represents a primitive JavaScript string value.
     * @param obj the object
     * @return true if the object represents a primitive JavaScript string value.
     */
    public static boolean isString(final Object obj) {
        return obj instanceof String;
    }

    /**
     * Returns true if object represents a primitive JavaScript number value. Note that we only
     * treat wrapper objects of Java primitive number types as objects that can be fully represented
     * as JavaScript numbers (doubles). This means we exclude {@code long} and special purpose Number
     * instances such as {@link java.util.concurrent.atomic.AtomicInteger}, as well as arbitrary precision
     * numbers such as {@link java.math.BigInteger}.
     *
     * @param obj the object
     * @return true if the object represents a primitive JavaScript number value.
     */
    public static boolean isNumber(final Object obj) {
        if (obj != null) {
            final Class<?> c = obj.getClass();
            return c == Integer.class || c == Double.class || c == Float.class || c == Short.class || c == Byte.class;
        }
        return false;
    }

    /**
     * JavaScript compliant conversion of number to String
     * See ECMA 9.8.1
     *
     * @param num a number
     *
     * @return a string
     */
    public static String toString(final double num) {
        if (isRepresentableAsInt(num)) {
            return Integer.toString((int)num);
        }

        if (num == Double.POSITIVE_INFINITY) {
            return "Infinity";
        }

        if (num == Double.NEGATIVE_INFINITY) {
            return "-Infinity";
        }

        if (Double.isNaN(num)) {
            return "NaN";
        }

        return DoubleConversion.toShortestString(num);
    }

    /**
     * Check if an object is null or undefined
     *
     * @param obj object to check
     *
     * @return true if null or undefined
     */
    public static boolean nullOrUndefined(final Object obj) {
        return obj == null;
    }

    static String toStringImpl(final Object obj) {
        if (obj instanceof String) {
            return (String)obj;
        }

        if (isNumber(obj)) {
            return toString(((Number)obj).doubleValue());
        }

        if (obj == null) {
            return "null";
        }

        if (obj instanceof Boolean) {
            return obj.toString();
        }

        return toString(toPrimitive(obj, String.class));
    }
}
