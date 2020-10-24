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

package com.anatawa12.nashorn.api.scripting;

import com.anatawa12.nashorn.internal.runtime.ECMAErrors;

/**
 * This is base exception for all Nashorn exceptions. These originate from
 * user's ECMAScript code. Example: script parse errors, exceptions thrown from
 * scripts. Note that ScriptEngine methods like "eval", "invokeMethod",
 * "invokeFunction" will wrap this as ScriptException and throw it. But, there
 * are cases where user may need to access this exception (or implementation
 * defined subtype of this). For example, if java interface is implemented by a
 * script object or Java access to script object properties via java.util.Map
 * interface. In these cases, user code will get an instance of this or
 * implementation defined subclass.
 *
// * @deprecated Nashorn JavaScript script engine and APIs, and the jjs tool
// * are deprecated with the intent to remove them in a future release.
 *
 * @since 1.8u40
 */
@SuppressWarnings("serial")
public abstract class NashornException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    // script file name
    private String fileName;
    // script line number
    private int line;
    // are the line and fileName unknown?
    private boolean lineAndFileNameUnknown;
    // script column number
    private int column;

    /**
     * Constructor to initialize error message, file name, line and column numbers.
     *
     * @param msg       exception message
     * @param fileName  file name
     * @param line      line number
     * @param column    column number
     */
    protected NashornException(final String msg, final String fileName, final int line, final int column) {
        this(msg, null, fileName, line, column);
    }

    /**
     * Constructor to initialize error message, cause exception, file name, line and column numbers.
     *
     * @param msg       exception message
     * @param cause     exception cause
     * @param fileName  file name
     * @param line      line number
     * @param column    column number
     */
    protected NashornException(final String msg, final Throwable cause, final String fileName, final int line, final int column) {
        super(msg, cause == null ? null : cause);
        this.fileName = fileName;
        this.line = line;
        this.column = column;
    }

    /**
     * Get the source file name for this {@code NashornException}
     *
     * @return the file name
     */
    public final String getFileName() {
        ensureLineAndFileName();
        return fileName;
    }

    /**
     * Set the source file name for this {@code NashornException}
     *
     * @param fileName the file name
     */
    public final void setFileName(final String fileName) {
        this.fileName = fileName;
        lineAndFileNameUnknown = false;
    }

    /**
     * Get the line number for this {@code NashornException}
     *
     * @return the line number
     */
    public final int getLineNumber() {
        ensureLineAndFileName();
        return line;
    }

    /**
     * Set the line number for this {@code NashornException}
     *
     * @param line the line number
     */
    public final void setLineNumber(final int line) {
        lineAndFileNameUnknown = false;
        this.line = line;
    }

    /**
     * Get the column for this {@code NashornException}
     *
     * @return the column number
     */
    public final int getColumnNumber() {
        return column;
    }

    /**
     * Set the column for this {@code NashornException}
     *
     * @param column the column number
     */
    public final void setColumnNumber(final int column) {
        this.column = column;
    }

    private void ensureLineAndFileName() {
        if (lineAndFileNameUnknown) {
            for (final StackTraceElement ste : getStackTrace()) {
                if (ECMAErrors.isScriptFrame(ste)) {
                    // Whatever here is compiled from JavaScript code
                    fileName = ste.getFileName();
                    line = ste.getLineNumber();
                    return;
                }
            }

            lineAndFileNameUnknown = false;
        }
    }
}
