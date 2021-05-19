/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.enterprise.web.logger;

import com.sun.enterprise.util.logging.IASLevel;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is an adapter of java.util.logging.Logger to org.apache.catalina.Logger.
 *
 * @author Shing Wai Chan
 *
 */
public final class CatalinaLogger extends LoggerBase {
    private Logger logger = null;

    /**
     * Construct a new instance of this class, that uses the specified
     * logger instance.
     *
     * @param logger The logger to send log messages to
     */
    public CatalinaLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    protected void write(String msg, int verbosity) {

        if (logger == null) {
            return;
        }

        Level level = Level.INFO;

        if (verbosity == FATAL) {
            level = (Level)IASLevel.FATAL;
        } else if (verbosity == ERROR) {
            level = Level.SEVERE;
        } else if (verbosity == WARNING) {
            level = Level.WARNING;
        } else if (verbosity == INFORMATION) {
            level = Level.INFO;
        } else if (verbosity == DEBUG) {
            level = Level.FINER;
        }

        logger.log(level, msg);
    }

    /**
     * Set the verbosity level of this logger.  Messages logged with a
     * higher verbosity than this level will be silently ignored.
     *
     * @param logLevel The new verbosity level, as a string
     */
    public void setLevel(String logLevel) {
        if ("SEVERE".equalsIgnoreCase(logLevel)) {
            logger.setLevel(Level.SEVERE);
        } else if ("WARNING".equalsIgnoreCase(logLevel)) {
            logger.setLevel(Level.WARNING);
        } else if ("INFO".equalsIgnoreCase(logLevel)) {
            logger.setLevel(Level.INFO);
        } else if ("CONFIG".equalsIgnoreCase(logLevel)) {
            logger.setLevel(Level.CONFIG);
        } else if ("FINE".equalsIgnoreCase(logLevel)) {
            logger.setLevel(Level.FINE);
        } else if ("FINER".equalsIgnoreCase(logLevel)) {
            logger.setLevel(Level.FINER);
        } else if ("FINEST".equalsIgnoreCase(logLevel)) {
            logger.setLevel(Level.FINEST);
        } else {
            logger.setLevel(Level.INFO);
        }
    }
}
