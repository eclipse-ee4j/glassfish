/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.glassfish.microprofile.telemetry.runtime;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.glassfish.microprofile.telemetry.TelemetryInstances;

import static io.opentelemetry.semconv.ExceptionAttributes.EXCEPTION_MESSAGE;
import static io.opentelemetry.semconv.ExceptionAttributes.EXCEPTION_STACKTRACE;
import static io.opentelemetry.semconv.ExceptionAttributes.EXCEPTION_TYPE;

/**
 * Bridges {@code java.util.logging} records to the OpenTelemetry Logs Bridge API.
 * <p>
 * The record goes to the instance of the application running in the current thread, or to the
 * runtime instance if it is enabled. Records of OpenTelemetry itself and of the standard
 * output and error streams are skipped: the logging exporters print to the standard output,
 * which GlassFish redirects back to {@code java.util.logging}.
 */
public final class OpenTelemetryLogHandler extends Handler {

    private static final String[] IGNORED_LOGGERS = {
        "io.opentelemetry.",
        "jakarta.enterprise.logging.stdout",
        "jakarta.enterprise.logging.stderr",
        "jdk.internal.httpclient",
    };

    private static final Formatter MESSAGE_FORMATTER = new Formatter() {

        @Override
        public String format(LogRecord logRecord) {
            return formatMessage(logRecord);
        }
    };

    private static final ThreadLocal<Boolean> PUBLISHING = new ThreadLocal<>();

    /**
     * Adds a handler to the root logger, unless it already has one.
     */
    public static void install() {
        Logger root = Logger.getLogger("");
        synchronized (OpenTelemetryLogHandler.class) {
            for (Handler handler : root.getHandlers()) {
                if (handler instanceof OpenTelemetryLogHandler) {
                    return;
                }
            }
            root.addHandler(new OpenTelemetryLogHandler());
        }
    }

    /**
     * Removes the handlers added by {@link #install()}.
     */
    public static void uninstall() {
        Logger root = Logger.getLogger("");
        synchronized (OpenTelemetryLogHandler.class) {
            for (Handler handler : root.getHandlers()) {
                if (handler instanceof OpenTelemetryLogHandler) {
                    root.removeHandler(handler);
                }
            }
        }
    }

    @Override
    public void publish(LogRecord logRecord) {
        if (logRecord == null || isIgnored(logRecord.getLoggerName()) || PUBLISHING.get() != null) {
            return;
        }
        // Logging must not create the SDK: it may happen in the middle of a deployment
        OpenTelemetry openTelemetry = TelemetryInstances
            .forClassLoaderIfCreated(Thread.currentThread().getContextClassLoader());
        if (!TelemetryInstances.isEnabled(openTelemetry)) {
            return;
        }
        PUBLISHING.set(Boolean.TRUE);
        try {
            String loggerName = logRecord.getLoggerName() == null ? "" : logRecord.getLoggerName();
            LogRecordBuilder builder = openTelemetry.getLogsBridge().get(loggerName).logRecordBuilder()
                .setTimestamp(logRecord.getInstant())
                .setSeverity(toSeverity(logRecord.getLevel()))
                .setSeverityText(logRecord.getLevel().getName())
                .setContext(Context.current());
            String message = MESSAGE_FORMATTER.formatMessage(logRecord);
            if (message != null) {
                builder.setBody(message);
            }
            Throwable thrown = logRecord.getThrown();
            if (thrown != null) {
                builder.setAttribute(EXCEPTION_TYPE, thrown.getClass().getName());
                if (thrown.getMessage() != null) {
                    builder.setAttribute(EXCEPTION_MESSAGE, thrown.getMessage());
                }
                StringWriter stackTrace = new StringWriter();
                thrown.printStackTrace(new PrintWriter(stackTrace));
                builder.setAttribute(EXCEPTION_STACKTRACE, stackTrace.toString());
            }
            builder.emit();
        } catch (RuntimeException e) {
            reportError("Failed to bridge a log record to OpenTelemetry.", e, 0);
        } finally {
            PUBLISHING.remove();
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

    private static boolean isIgnored(String loggerName) {
        if (loggerName == null) {
            return false;
        }
        for (String ignored : IGNORED_LOGGERS) {
            if (loggerName.startsWith(ignored)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Same mapping as the OpenTelemetry Java agent instrumentation of {@code java.util.logging}.
     */
    static Severity toSeverity(Level level) {
        int value = level.intValue();
        if (value >= Level.SEVERE.intValue()) {
            return Severity.ERROR;
        }
        if (value >= Level.WARNING.intValue()) {
            return Severity.WARN;
        }
        if (value >= Level.INFO.intValue()) {
            return Severity.INFO;
        }
        if (value >= Level.CONFIG.intValue()) {
            return Severity.DEBUG4;
        }
        if (value >= Level.FINE.intValue()) {
            return Severity.DEBUG;
        }
        if (value >= Level.FINER.intValue()) {
            return Severity.TRACE4;
        }
        if (value >= Level.FINEST.intValue()) {
            return Severity.TRACE;
        }
        return Severity.UNDEFINED_SEVERITY_NUMBER;
    }
}
