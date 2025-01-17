/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
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

/**
 * JSCH triggers a lot of unimportant messages, which have a lower level than issued by JSCH.
 * For example, for a single SSH connection, it triggers 1 WARNING about adding a host to the list
 * of known hosts, 47 INFO messages, and 6 DEBUG messages. Therefore we decrease their level to
 * match their real severity.
 * <p>
 * The log levels from JSCH are mapped to these levels in GlassFish:
 * <ul>
 * <li>JSCH DEBUG level -> TRACE level in GlassFish
 * <li>JSCH INFO level -> DEBUG level in GlassFish
 * <li>JSCH WARN level -> INFO level in GlassFish
 * <li>JSCH ERROR level -> WARNING level in GlassFish
 * <li>JSCH FATAL level -> ERROR/SEVERE level in GlassFish
 * </ul>
 */
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

    private static Level getSystemLoggerLevel(int jschLevel) {
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
