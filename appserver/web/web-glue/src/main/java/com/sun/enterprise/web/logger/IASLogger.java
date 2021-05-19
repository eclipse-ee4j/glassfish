/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * An implementation of <b>Logger</b> that writes log messages
 * using JDK 1.4's logging API.
 *
 * @author Arvind Srinivasan
 * @author Neelam Vaidya
 * @version $Revision: 1.4 $
 */

public final class IASLogger extends LoggerBase {

    // ----------------------------------------------------- Instance Variables

    /**
     * The server wide log message handler.
     */
    Logger _logger = null;

    /**
     * Classname of the object invoking the log method.
     */
    private String _classname;

    /**
     * Name of the method invoking the log method.
     */
    private String _methodname;

    /**
     * The descriptive information about this implementation.
     */
    private static final String info =
        "com.sun.enterprise.web.logger.IASLogger/1.0";


    // ----------------------------------------------------------- Constructors

    /**
     * Deny void construction.
     */
    private IASLogger() {
        super();
    }

    /**
     * Construct a new instance of this class, that uses the specified
     * logger instance.
     *
     * @param logger The logger to send log messages to
     */
    public IASLogger(Logger logger) {
        _logger = logger;
    }

    // ------------------------------------------------------ Protected Methods

    /**
     * Logs the message to the JDK 1.4 logger that handles all log
     * messages for the iPlanet Application Server.
     */
    protected void write(String msg, int verbosity) {

        if (_logger == null)
            return;

        Level level = Level.INFO;

        if (verbosity == FATAL)
            level = (Level)IASLevel.FATAL;
        else if (verbosity == ERROR)
            level = Level.SEVERE;
        else if (verbosity == WARNING)
            level = Level.WARNING;
        else if (verbosity == INFORMATION)
            level = Level.INFO;
        else if (verbosity == DEBUG)
            level = Level.FINER;

        inferCaller();
        _logger.logp(level, _classname, _methodname, msg);
    }

    // ------------------------------------------------------ Private Methods

    /**
     * Examine the call stack and determine the name of the method and the
     * name of the class logging the message.
     */
    private void inferCaller() {
        // Get the stack trace.
        StackTraceElement stack[] = (new Throwable()).getStackTrace();
        _classname = "";
        _methodname = "";
        for (int ix = 0; ix < stack.length; ix++) {
            StackTraceElement frame = stack[ix];
            _classname = frame.getClassName();
            if (!_classname.startsWith("com.sun.enterprise.web.logger")) {
                // We've found the relevant frame. Get Method Name.
                _methodname = frame.getMethodName();
                return;
            }
        }
    }
}
