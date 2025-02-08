/*
 * Copyright (c) 2022, 2025 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul;

import java.lang.System.Logger;
import java.lang.System.LoggerFinder;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import org.glassfish.main.jul.tracing.GlassFishLoggingTracer;

/**
 * Implementation of {@link LoggerFinder} using {@link GlassFishLogManager}.
 *
 * @author David Matejcek
 */
public class GlassFishLoggerFinder extends LoggerFinder {

    @Override
    public Logger getLogger(String name, Module module) {
        GlassFishLoggingTracer.trace(getClass(), () -> "getLogger name: " + name + ", module: " + module.getName());
        return new GlassFishSystemLogger(java.util.logging.Logger.getLogger(name));
    }

    private static class GlassFishSystemLogger implements Logger {

        private final java.util.logging.Logger logger;

        private GlassFishSystemLogger(final java.util.logging.Logger logger) {
            this.logger = logger;
        }


        @Override
        public String getName() {
            return logger.getName();
        }


        @Override
        public boolean isLoggable(Level level) {
            return logger.isLoggable(toJUL(level));
        }


        @Override
        public void log(Level level, ResourceBundle bundle, String msg, Throwable thrown) {
            logger.logrb(toJUL(level), bundle, msg, thrown);
        }


        @Override
        public void log(Level level, ResourceBundle bundle, String format, Object... params) {
            logger.logrb(toJUL(level), bundle, format, params);
        }


        @Override
        public void log(Level level, Supplier<String> msg, Throwable thrown) {
            logger.log(toJUL(level), thrown, msg);
        }


        @Override
        public void log(Level level, Supplier<String> msg) {
            logger.log(toJUL(level), msg);
        }


        private java.util.logging.Level toJUL(Level level) {
            switch (level) {
                case TRACE:
                    return java.util.logging.Level.FINER;
                case DEBUG:
                    return java.util.logging.Level.FINE;
                case INFO:
                    return java.util.logging.Level.INFO;
                case WARNING:
                    return java.util.logging.Level.WARNING;
                case ERROR:
                    return java.util.logging.Level.SEVERE;
                case ALL:
                    return java.util.logging.Level.ALL;
                case OFF:
                    return java.util.logging.Level.OFF;
                default:
                    throw new IllegalStateException("Unknown level enum: " + level);
            }
        }
    }
}
