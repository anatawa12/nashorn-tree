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

package com.anatawa12.nashorn.internal.runtime.regexp;

import java.util.*;

import com.anatawa12.nashorn.internal.runtime.ParserException;

public class RegExpFactory {
    private static final Set<String> REGEXP_CACHE =
            Collections.synchronizedSet(new HashSet<String>());

    /**
     * Compile a regexp with the given {@code source} and {@code flags}.
     *
     * @param pattern RegExp pattern string
     * @param flags   flag string
     * @return new RegExp
     * @throws ParserException if invalid source or flags
     */
    public static void create(final String pattern, final String flags) {
        final String key = pattern + "/" + flags;
        if (!REGEXP_CACHE.contains(key)) {
            new JoniRegExp(pattern, flags);
            REGEXP_CACHE.add(key);
        }
    }

    /**
     * Validate a regexp with the given {@code source} and {@code flags}.
     *
     * @param pattern RegExp pattern string
     * @param flags  flag string
     *
     * @throws ParserException if invalid source or flags
     */
    public static void validate(final String pattern, final String flags) throws ParserException {
        create(pattern, flags);
    }
}
