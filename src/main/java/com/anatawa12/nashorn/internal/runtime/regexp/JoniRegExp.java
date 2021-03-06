/*
 * Copyright (c) 2010, 2019, Oracle and/or its affiliates. All rights reserved.
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

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import com.anatawa12.nashorn.internal.runtime.ParserException;
import org.jcodings.Encoding;
import org.jcodings.specific.UTF16BEEncoding;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Syntax;
import org.joni.exception.JOniException;

/**
 * Regular expression implementation based on the Joni engine from the JRuby project.
 */
public class JoniRegExp extends RegExp {

    /**
     * Construct a Regular expression from the given {@code pattern} and {@code flags} strings.
     *
     * @param pattern RegExp pattern string
     * @param flags RegExp flag string
     * @throws ParserException if flags is invalid or pattern string has syntax error.
     */
    public JoniRegExp(final String pattern, final String flags) throws ParserException {
        super(pattern, flags);

        int option = Option.SINGLELINE;

        if (this.isIgnoreCase()) {
            option |= Option.IGNORECASE;
        }
        if (this.isMultiline()) {
            option &= ~Option.SINGLELINE;
            option |= Option.NEGATE_SINGLELINE;
        }

        try {
            RegExpScanner parsed;

            try {
                parsed = RegExpScanner.scan(pattern);
            } catch (final PatternSyntaxException e) {
                // refine the exception with a better syntax error, if this
                // passes, just rethrow what we have
                Pattern.compile(pattern, 0);
                throw e;
            }

            Encoding encode = UTF16BEEncoding.INSTANCE;
            final byte[] javaPattern = parsed.getJavaPattern().getBytes(StandardCharsets.UTF_16BE);
            new Regex(javaPattern, 0, javaPattern.length, option, encode, Syntax.ECMAScript);
        } catch (final PatternSyntaxException | JOniException e2) {
            throwParserException("syntax", e2.getMessage());
        } catch (StackOverflowError e3) {
            throw new RuntimeException(e3);
        }
    }

}
