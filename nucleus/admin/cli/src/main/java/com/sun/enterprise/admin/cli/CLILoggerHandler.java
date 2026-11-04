/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.cli;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;

/**
 * The log handler of the asadmin process.
 * <p>
 * Records are split by level:
 * <ul>
 * <li>{@link java.util.logging.Level#FINER} and finer records are diagnostics. They are printed
 * to the standard error output, formatted by the diagnostic formatter, so they never mix with
 * the output parsed by scripts.
 * <li>{@link java.util.logging.Level#FINE} and coarser records are messages for the user,
 * printed as plain text: {@link java.util.logging.Level#SEVERE} to the standard error output,
 * the rest to the standard output. The diagnostic formatter is not used for them.
 * </ul>
 * New code should use {@link AsadminOutput} for messages for the user instead of logging.
 */
final class CLILoggerHandler extends Handler {

    private final Formatter userFormatter;

    /**
     * @param trace true to print stack traces of exceptions attached to messages for the user.
     * @param diagnosticFormatter formatter of diagnostic records; if null, a plain formatter is used.
     */
    CLILoggerHandler(final boolean trace, final Formatter diagnosticFormatter) {
        this.userFormatter = new PlainFormatter(trace);
        setFormatter(diagnosticFormatter == null ? userFormatter : diagnosticFormatter);
    }

    @Override
    public void publish(final LogRecord logRecord) {
        if (!isLoggable(logRecord)) {
            return;
        }
        final int level = logRecord.getLevel().intValue();
        final PrintStream stream;
        final Formatter formatter;
        if (level < FINE.intValue()) {
            stream = System.err;
            formatter = getFormatter();
        } else {
            stream = level >= SEVERE.intValue() ? System.err : System.out;
            formatter = userFormatter;
        }
        stream.print(formatter.format(logRecord));
        stream.flush();
    }

    @Override
    public void flush() {
        System.out.flush();
        System.err.flush();
    }

    @Override
    public void close() {
        flush();
    }

    /**
     * Prints just the message, one record per line.
     */
    static final class PlainFormatter extends Formatter {

        private final boolean trace;

        PlainFormatter(final boolean trace) {
            this.trace = trace;
        }

        @Override
        public synchronized String format(final LogRecord record) {
            final Throwable thrown = record.getThrown();
            if (thrown == null) {
                return formatMessage(record) + System.lineSeparator();
            }
            // Some messages use the exception as a parameter.
            // If we don't print stacktraces, the cause would be lost.
            final Object[] parameters;
            if (record.getParameters() == null) {
                parameters = new Object[] {thrown};
            } else {
                parameters = new Object[record.getParameters().length + 1];
                System.arraycopy(record.getParameters(), 0, parameters, 0, parameters.length - 1);
                parameters[parameters.length - 1] = thrown;
            }
            record.setParameters(parameters);
            if (trace) {
                final StringWriter stackTrace = new StringWriter();
                thrown.printStackTrace(new PrintWriter(stackTrace));
                return formatMessage(record) + System.lineSeparator() + stackTrace;
            }
            return formatMessage(record) + " " + thrown.getLocalizedMessage() + System.lineSeparator();
        }
    }
}
