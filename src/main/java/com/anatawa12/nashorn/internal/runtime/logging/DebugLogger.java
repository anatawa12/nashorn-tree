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

package com.anatawa12.nashorn.internal.runtime.logging;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.LoggingPermission;

/**
 * Wrapper class for Logging system. This is how you are supposed to register a logger and use it
 */

public final class DebugLogger {

    /** Disabled logger used for all loggers that need an instance, but shouldn't output anything */
    public static final DebugLogger DISABLED_LOGGER = new DebugLogger("disabled", Level.OFF, false);

    private final Logger  logger;
    private final boolean isEnabled;

    private int indent;

    /** A quiet logger only logs RuntimeEvents and does't output any text, regardless of level */
    private final boolean isQuiet;

    /**
     * Constructor
     *
     * A logger can be paired with a property, e.g. {@code --log:codegen:info} is equivalent to {@code -Dnashorn.codegen.log}
     *
     * @param loggerName  name of logger - this is the unique key with which it can be identified
     * @param loggerLevel level of the logger
     * @param isQuiet     is this a quiet logger, i.e. enabled for things like e.g. RuntimeEvent:s, but quiet otherwise
     */
    public DebugLogger(final String loggerName, final Level loggerLevel, final boolean isQuiet) {
        this.logger  = instantiateLogger(loggerName, loggerLevel);
        this.isQuiet = isQuiet;
        assert logger != null;
        this.isEnabled = getLevel() != Level.OFF;
    }

    private static Logger instantiateLogger(final String name, final Level level) {
        final Logger logger = java.util.logging.Logger.getLogger(name);
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                for (final Handler h : logger.getHandlers()) {
                    logger.removeHandler(h);
                }

                logger.setLevel(level);
                logger.setUseParentHandlers(false);
                final Handler c = new ConsoleHandler();

                c.setFormatter(new Formatter() {
                    @Override
                    public String format(final LogRecord record) {
                        final StringBuilder sb = new StringBuilder();

                        sb.append('[')
                        .append(record.getLoggerName())
                        .append("] ")
                        .append(record.getMessage())
                        .append('\n');

                        return sb.toString();
                    }
                });
                logger.addHandler(c);
                c.setLevel(level);
                return null;
            }
        }, createLoggerControlAccCtxt());

        return logger;
    }

    /**
     * Do not currently support chaining this with parent logger. Logger level null
     * means disabled
     * @return level
     */
    public Level getLevel() {
        return logger.getLevel() == null ? Level.OFF : logger.getLevel();
    }

    /**
     * Check if the logger is enabled
     * @return true if enabled
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Shorthand for outputting a log string as log level
     * {@link java.util.logging.Level#INFO} on this logger
     * @param str the string to log
     */
    public void info(final String str) {
        log(Level.INFO, str);
    }

    /**
     * Shorthand for outputting a log string as log level
     * {@link java.util.logging.Level#FINE} on this logger
     * @param objs object array to log - use this to perform lazy concatenation to avoid unconditional toString overhead
     */
    public void info(final Object... objs) {
        log(Level.INFO, objs);
    }

    /**
     * Output log line on this logger at a given level of verbosity
     * @see java.util.logging.Level
     *
     * @param level minimum log level required for logging to take place
     * @param str   string to log
     */
    public void log(final Level level, final String str) {
        if (isEnabled && !isQuiet && logger.isLoggable(level)) {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0 ; i < indent ; i++) {
                sb.append(' ');
            }
            sb.append(str);
            logger.log(level, sb.toString());
        }
    }

    /**
     * Output log line on this logger at a given level of verbosity
     * @see java.util.logging.Level
     *
     * @param level minimum log level required for logging to take place
     * @param objs  objects for which to invoke toString and concatenate to log
     */
    public void log(final Level level, final Object... objs) {
        if (isEnabled && !isQuiet && logger.isLoggable(level)) {
            final StringBuilder sb = new StringBuilder();
            for (final Object obj : objs) {
                sb.append(obj);
            }
            log(level, sb.toString());
        }
    }

    /**
     * Access control context for logger level and instantiation permissions
     * @return access control context
     */
    private static AccessControlContext createLoggerControlAccCtxt() {
        final Permissions perms = new Permissions();
        perms.add(new LoggingPermission("control", null));
        return new AccessControlContext(new ProtectionDomain[] { new ProtectionDomain(null, perms) });
    }

}
