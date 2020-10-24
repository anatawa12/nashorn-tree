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

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import com.anatawa12.nashorn.internal.codegen.Namespace;
import com.anatawa12.nashorn.internal.runtime.options.LoggingOption;
import com.anatawa12.nashorn.internal.runtime.options.LoggingOption.LoggerInfo;
import com.anatawa12.nashorn.internal.runtime.options.Options;

/**
 * Script environment consists of command line options, arguments, script files
 * and output and error writers, top level Namespace etc.
 */
public final class ScriptEnvironment {

    /** Error writer for this environment */
    private final PrintWriter err;

    /** Top level namespace. */
    private final Namespace namespace;

    /** Accept "const" keyword and treat it as variable. Interim feature */
    public final boolean _const_as_var;

    /** Put all variables in scopes to make them debuggable */
    public final boolean _debug_scopes;

    /** Display stack trace upon error, default is false */
    public final boolean _dump_on_error;

    /** Invalid lvalue expressions should be reported as early errors */
    public final boolean _early_lvalue_error;

    /** Empty statements should be preserved in the AST */
    public final boolean _empty_statements;

    /** Enable experimental ECMAScript 6 features. */
    public final boolean _es6;

    /**
     *  Behavior when encountering a function declaration in a lexical context where only statements are acceptable
     * (function declarations are source elements, but not statements).
     */
    public enum FunctionStatementBehavior {
        /**
         * Accept the function declaration silently and treat it as if it were a function expression assigned to a local
         * variable.
         */
        ACCEPT,
        /**
         * Log a parser warning, but accept the function declaration and treat it as if it were a function expression
         * assigned to a local variable.
         */
        WARNING,
        /**
         * Raise a {@code SyntaxError}.
         */
        ERROR
    }

    /**
     * Behavior when encountering a function declaration in a lexical context where only statements are acceptable
     * (function declarations are source elements, but not statements).
     */
    public final FunctionStatementBehavior _function_statement;

    /** Do not support non-standard syntax extensions. */
    public final boolean _no_syntax_extensions;

    /** Only parse the source code, do not compile */
    public final boolean _parse_only;

    /** is this environment in scripting mode? */
    public final boolean _scripting;

    /** is this environment in strict mode? */
    public final boolean _strict;

    /**
     * Constructor
     *
     * @param options a Options object
     * @param out output print writer
     * @param err error print writer
     */
    @SuppressWarnings("unused")
    public ScriptEnvironment(final Options options, final PrintWriter out, final PrintWriter err) {
        this.err = err;
        this.namespace = new Namespace();

        _const_as_var         = options.getBoolean("const.as.var");
        _debug_scopes         = options.getBoolean("debug.scopes");
        _dump_on_error        = options.getBoolean("doe");
        _early_lvalue_error   = options.getBoolean("early.lvalue.error");
        _empty_statements     = options.getBoolean("empty.statements");
        if (options.getBoolean("function.statement.error")) {
            _function_statement = FunctionStatementBehavior.ERROR;
        } else if (options.getBoolean("function.statement.warning")) {
            _function_statement = FunctionStatementBehavior.WARNING;
        } else {
            _function_statement = FunctionStatementBehavior.ACCEPT;
        }
        _no_syntax_extensions = options.getBoolean("no.syntax.extensions");
        _parse_only           = options.getBoolean("parse.only");
        _scripting            = options.getBoolean("scripting");
        _strict               = options.getBoolean("strict");

        final String language = options.getString("language");
        if (language == null || language.equals("es5")) {
            _es6 = false;
        } else if (language.equals("es6")) {
            _es6 = true;
        } else {
            throw new RuntimeException("Unsupported language: " + language);
        }
    }

    /**
     * Get the error stream for this environment
     * @return error print writer
     */
    public PrintWriter getErr() {
        return err;
    }

    /**
     * Get the namespace for this environment
     * @return namespace
     */
    public Namespace getNamespace() {
        return namespace;
    }
}
