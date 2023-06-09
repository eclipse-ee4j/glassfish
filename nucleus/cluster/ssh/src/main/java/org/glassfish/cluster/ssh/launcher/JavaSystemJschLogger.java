/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.glassfish.cluster.ssh.launcher;

import com.jcraft.jsch.JSch;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

public class JavaSystemJschLogger implements com.jcraft.jsch.Logger {

    private final Logger logger;

    public JavaSystemJschLogger() {
        this.logger = System.getLogger(JSch.class.getName());
    }

    public JavaSystemJschLogger(String loggerName) {
        this.logger = System.getLogger(loggerName);
    }

    @Override
    public boolean isEnabled(int jschLevel) {
        return logger.isLoggable(getSystemLoggerLevel(jschLevel));
    }

    @Override
    public void log(int jschLevel, String message) {
        logger.log(getSystemLoggerLevel(jschLevel), message);
    }

    @Override
    public void log(int jschLevel, String message, Throwable cause) {
        if (cause != null) {
            logger.log(getSystemLoggerLevel(jschLevel), message, cause);
        } else {
            logger.log(getSystemLoggerLevel(jschLevel), message);
        }
    }

    static Level getSystemLoggerLevel(int jschLevel) {
        switch (jschLevel) {
            case DEBUG:
                return Level.TRACE;
            case INFO:
                return Level.DEBUG;
            case WARN:
                return Level.INFO;
            case ERROR:
                return Level.WARNING;
            case FATAL:
                return Level.ERROR;
            default:
                return Level.TRACE;
        }
    }

}
