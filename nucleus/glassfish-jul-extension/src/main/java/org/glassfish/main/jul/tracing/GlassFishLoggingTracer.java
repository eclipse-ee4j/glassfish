/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul.tracing;

import java.io.PrintStream;
import java.util.function.Supplier;

/**
 * Useful to trace logging, what it does, how it is configured, what initialized it.
 * Useful for server developers.
 *
 * @author David Matejcek
 */
public final class GlassFishLoggingTracer {

    private static final String JVM_OPT_LOGGING_TRACING_ENABLED = "org.glassfish.main.jul.tracing.enabled";
    private static final PrintStream OUT = System.out;
    private static final PrintStream ERR = System.err;
    private static volatile boolean tracingEnabled = Boolean.getBoolean(JVM_OPT_LOGGING_TRACING_ENABLED);

    private GlassFishLoggingTracer() {
        // hidden constructor
    }


    /**
     * Call this method to enable/disable tracing of the logging system. The effect is immediate.
     *
     * @param tracingEnabled
     */
    public static void setTracingEnabled(final boolean tracingEnabled) {
        GlassFishLoggingTracer.tracingEnabled = tracingEnabled;
    }


    /**
     * @return true if the tracing of the logging system is enabled.
     */
    public static boolean isTracingEnabled() {
        return tracingEnabled;
    }


    /**
     * Logs the message to STDOUT if the tracing is enabled.
     *
     * @param source
     * @param message
     */
    public static synchronized void trace(final Class<?> source, final Supplier<String> message) {
        if (tracingEnabled) {
            trace(source, message.get());
        }
    }


    /**
     * Logs the message to STDOUT if the tracing is enabled.
     *
     * @param source
     * @param message
     */
    public static synchronized void trace(final Class<?> source, final String message) {
        if (tracingEnabled) {
            OUT.println(source.getCanonicalName() + ": " + message);
            OUT.flush();
        }
    }


    /**
     * Logs a "DON'T PANIC" message and generated RuntimeException with the message parameter
     * to STDOUT if the tracing is enabled.
     *
     * @param source
     * @param exceptionMessage
     */
    public static synchronized void stacktrace(final Class<?> source, final String exceptionMessage) {
        if (tracingEnabled) {
            OUT.println(
                source.getCanonicalName() + ": Don't panic, following stacktrace is only to see what invoked this!");
            new RuntimeException(exceptionMessage).printStackTrace(OUT);
            OUT.flush();
        }
    }


    /**
     * Logs the message to STDERR.
     *
     * @param source
     * @param message
     */
    public static synchronized void error(final Class<?> source, final String message) {
        ERR.println(source.getCanonicalName() + ": " + message);
        ERR.flush();
    }


    /**
     * Logs the message and the exception's stacktrace to STDERR.
     *
     * @param source
     * @param message
     * @param cause
     */
    public static synchronized void error(final Class<?> source, final String message, final Throwable cause) {
        ERR.println(source.getCanonicalName() + ": " + message);
        cause.printStackTrace(ERR);
        ERR.flush();
    }
}
