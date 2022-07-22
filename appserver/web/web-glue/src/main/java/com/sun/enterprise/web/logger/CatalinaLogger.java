/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
 * @author David Matejcek
 */
public final class CatalinaLogger extends LoggerBase {
    private final Logger logger;

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

        final Level level;
        switch (verbosity) {
            case FATAL:
                level = IASLevel.FATAL;
                break;
            case ERROR:
                level = Level.SEVERE;
                break;
            case WARNING:
                level = Level.WARNING;
                break;
            case INFORMATION:
                level = Level.INFO;
                break;
            case DEBUG:
                level = Level.FINER;
                break;
            default:
                level = Level.INFO;
                break;
        }

        logger.log(level, msg);
    }
}
