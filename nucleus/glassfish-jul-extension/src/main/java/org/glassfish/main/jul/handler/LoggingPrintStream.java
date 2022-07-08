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

package org.glassfish.main.jul.handler;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * {@link LoggingPrintStream} has it's own {@link LoggingOutputStream}.
 * Once it is set as the System.out or System.err, all outputs to these
 * PrintStreams will end up in {@link LoggingOutputStream} which will log these on a flush.
 * <p>
 * This simple behaviour has a negative side effect that stack traces are logged with
 * each line being a new log record. The reason for above is that
 * {@link Throwable#printStackTrace(PrintStream)} converts each
 * line into a separate println, causing a flush at the end of each.
 * <p>
 * One option that was thought of to smooth this over was to see if the caller of println is
 * <code>Throwable.[some set of methods]</code>. Unfortunately, there are others who
 * interpose on System.out and err (like jasper) which makes that check untenable.
 * Hence the logic currently used is to see if there is a println(Throwable)
 * and create a standard log record of it and then prevent subsequent printline calls
 * done by the {@link Throwable#printStackTrace(PrintStream)} method.
 * This is possible because Throwable locks the stream to avoid collisions.
 */
public class LoggingPrintStream extends PrintStream {

    private final LogManager logManager = LogManager.getLogManager();
    private final ThreadLocal<StackTrace> perThreadStackTraces = new ThreadLocal<>();
    private final Logger logger;

    public static LoggingPrintStream create(final Logger logger, final Level level, final int bufferCapacity,
        final Charset charset) {
        return new LoggingPrintStream(logger, level, bufferCapacity, charset);
    }


    private LoggingPrintStream(final Logger logger, final Level level, final int bufferCapacity,
        final Charset charset) {
        super(null, false, charset);
        this.logger = logger;
        this.out = new LoggingOutputStream(logger, level, bufferCapacity, charset);
    }


    private LoggingOutputStream getOutputStream() {
        return (LoggingOutputStream) this.out;
    }


    @Override
    public void println(Object object) {
        if (!checkLocks()) {
            return;
        }

        if (object instanceof Throwable) {
            getOutputStream().addRecord((Throwable) object);
            StackTrace stackTrace = new StackTrace((Throwable) object);
            perThreadStackTraces.set(stackTrace);
        } else {
            // No special processing if it is not an exception.
            println(String.valueOf(object));
        }

    }


    @Override
    public PrintStream printf(String str, Object... args) {
        return format(str, args);
    }


    @Override
    public PrintStream printf(Locale locale, String str, Object... args) {
        return format(locale, str, args);
    }


    @Override
    public PrintStream format(String format, Object... args) {
        StringBuilder sb = new StringBuilder();
        try (Formatter formatter = new Formatter(sb, Locale.getDefault())) {
            formatter.format(format, args);
        }
        print(sb.toString());
        return this;
    }


    @Override
    public PrintStream format(Locale locale, String format, Object... args) {
        StringBuilder sb = new StringBuilder();
        try (Formatter formatter = new Formatter(sb, locale)) {
            formatter.format(format, args);
        }
        print(sb.toString());
        return this;
    }


    @Override
    public void print(String x) {
        if (checkLocks()) {
            super.print(x);
        }
    }


    @Override
    public void print(Object x) {
        if (checkLocks()) {
            super.print(x);
        }
    }


    @Override
    public void print(boolean x) {
        if (checkLocks()) {
            super.print(x);
        }
    }


    @Override
    public void print(double x) {
        if (checkLocks()) {
            super.print(x);
        }
    }


    @Override
    public void print(char x) {
        if (checkLocks()) {
            super.print(x);
        }
    }


    @Override
    public void print(int x) {
        if (checkLocks()) {
            super.print(x);
        }
    }


    @Override
    public void print(long x) {
        if (checkLocks()) {
            super.print(x);
        }
    }


    @Override
    public void print(float x) {
        if (checkLocks()) {
            super.print(x);
        }
    }


    @Override
    public void print(char[] x) {
        if (checkLocks()) {
            super.print(x);
        }
    }


    @Override
    public void println(boolean x) {
        if (checkLocks()) {
            super.println(x);
        }
    }


    @Override
    public void println(char x) {
        if (checkLocks()) {
            super.println(x);
        }
    }

    @Override
    public void println(int x) {
        if (checkLocks()) {
            super.println(x);
        }
    }


    @Override
    public void println(long x) {
        if (checkLocks()) {
            super.println(x);
        }
    }


    @Override
    public void println(float x) {
        if (checkLocks()) {
            super.println(x);
        }
    }


    @Override
    public void println(double x) {
        if (checkLocks()) {
            super.println(x);
        }
    }


    @Override
    public void println(char[] x) {
        if (checkLocks()) {
            super.println(x);
        }
    }


    @Override
    public void println() {
        // ignored as it would produce an emptz record.
    }


    @Override
    public void println(String str) {
        if (!checkLocks()) {
            return;
        }

        final StackTrace recentStacktrace = perThreadStackTraces.get();
        if (recentStacktrace == null) {
            super.println(str);
            flush();
            return;
        }

        if (!recentStacktrace.isStackTraceElement(str)) {
            perThreadStackTraces.set(null);
            super.println(str);
            flush();
            return;
        }

        if (recentStacktrace.isCompleted()) {
            perThreadStackTraces.set(null);
            return;
        }
    }


    @Override
    public void write(byte[] buf, int off, int len) {
        try {
            synchronized (this) {
                if (out == null) {
                    throw new IOException("Stream closed");
                }
                out.write(buf, off, len);
                out.flush();
            }
        } catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        } catch (IOException x) {
            setError();
        }
    }


    @Override
    public void write(int b) {
        if (checkLocks()) {
            super.write(b);
        }
    }


    @Override
    public String toString() {
        return super.toString() + " using " + this.out.toString();
    }


    /**
     * LoggingPrintStream class is to support the java System.err and System.out
     * redirection to server.log file.
     * <p>
     * When Java IO is redirected and System.out.println(...) is invoked by a thread with
     * LogManager or Logger(SYSTEMERR_LOGGER,SYSTEOUT_LOGGER) locked, all kind of dead
     * locks among threads will happen.
     * <p>
     * These dead locks are easily reproduced when jvm system properties
     * "-Djava.security.manager" and "-Djava.security.debug=access,failure" are defined.
     * These dead locks are basically because each thread has its own sequence of
     * acquiring lock objects(LogManager,Logger,FileHandler and SysLogHandler, the buffer
     * inside LoggingPrintStream).
     * <p>
     * There is no obvious way to define the lock hierarchy and control the lock sequence;
     * Trylock is not a strightforward solution either.Beside they both create heavy
     * dependence on the detail implementation of JDK and Appserver.
     * <p>
     * This method(checkLocks) is to find which locks current thread has and
     * LoggingPrintStream object will decide whether to continue to do printing or
     * give ip up to avoid the dead lock.
     */
    private boolean checkLocks() {
        return !Thread.holdsLock(logger) && !Thread.holdsLock(logManager);
    }


    /**
     * {@link StackTrace} keeps track of a throwable printed
     * by a thread as a result of {@link Throwable#printStackTrace(PrintStream)}
     * and it keeps track of subsequent println(String) to
     * avoid duplicate logging of stacktrace
     * <p>
     * Note that caller methods can be overriden.
     */
    private static final class StackTrace {

        private final String[] stackTrace;
        private int lastIndex;

        private StackTrace(Throwable throwable) {
            this.stackTrace = Arrays.stream(throwable.getStackTrace()).map(StackTraceElement::toString)
                .toArray(String[]::new);
        }


        boolean isStackTraceElement(String printedLine) {
            if (printedLine.contains(stackTrace[lastIndex])) {
                lastIndex++;
                return true;
            }
            return false;
        }


        boolean isCompleted() {
            return lastIndex >= stackTrace.length;
        }
    }
}
