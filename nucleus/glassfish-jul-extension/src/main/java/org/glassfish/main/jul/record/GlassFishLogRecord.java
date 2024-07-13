/*
 * Copyright (c) 2022, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul.record;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.ErrorManager;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * This class provides additional attributes not supported by JUL LogRecord.
 *
 * @author David Matejcek
 */
public class GlassFishLogRecord extends LogRecord {

    private static final long serialVersionUID = -4820672332353631291L;

    private static final ZoneId TIME_ZONE = ZoneId.systemDefault();

    private final LogRecord record;
    private final String threadName;
    private String messageKey;


    /**
     * Creates new record.
     * <p>
     * Source class and method will be autodetected now or set after this constructor ends.
     *
     * @param level a logging level value
     * @param message the logging message
     * @param autodetectSource autodetect source class and method
     */
    public GlassFishLogRecord(final Level level, final String message, final boolean autodetectSource) {
        this(new LogRecord(level, message), autodetectSource);
    }


    /**
     * Wraps the log record.
     *
     * @param record the log record
     * @param autodetectSource autodetect source class and method
     */
    public GlassFishLogRecord(final LogRecord record, final boolean autodetectSource) {
        super(record.getLevel(), null);
        this.threadName = Thread.currentThread().getName();
        this.record = record;
        if (autodetectSource) {
            SourceDetector.detectClassAndMethod(record);
        }
    }


    /**
     * @return the message identifier (generally not unique, may be {@code null})
     */
    public String getMessageKey() {
        return messageKey;
    }


    /**
     * This is called just to remember the original message value after it was translated using
     * the resource bundle.
     *
     * @param messageKey the message identifier (generally not unique, may be {@code null})
     */
    void setMessageKey(final String messageKey) {
        this.messageKey = messageKey;
    }


    /**
     * @return name of the thread which created this log record.
     */
    public String getThreadName() {
        return threadName;
    }


    @Override
    public Level getLevel() {
        return this.record.getLevel();
    }


    @Override
    public void setLevel(final Level level) {
        this.record.setLevel(level);
    }


    @Override
    public long getSequenceNumber() {
        return this.record.getSequenceNumber();
    }


    @Override
    public void setSequenceNumber(final long seq) {
        this.record.setSequenceNumber(seq);
    }


    @Override
    public String getLoggerName() {
        return this.record.getLoggerName();
    }


    @Override
    public void setLoggerName(final String name) {
        this.record.setLoggerName(name);
    }


    @Override
    public String getSourceClassName() {
        return this.record.getSourceClassName();
    }


    @Override
    public void setSourceClassName(final String className) {
        this.record.setSourceClassName(className == null || className.isEmpty() ? null : className);
    }


    @Override
    public String getSourceMethodName() {
        return this.record.getSourceMethodName();
    }


    @Override
    public void setSourceMethodName(final String methodName) {
        this.record.setSourceMethodName(methodName == null || methodName.isEmpty() ? null : methodName);
    }


    @Override
    public String getMessage() {
        return this.record.getMessage();
    }


    @Override
    public void setMessage(final String message) {
        this.record.setMessage(message == null || message.isEmpty() ? null : message);
    }


    @Override
    public Object[] getParameters() {
        return this.record.getParameters();
    }


    @Override
    public void setParameters(final Object[] parameters) {
        this.record.setParameters(parameters);
    }


    @Override
    public int getThreadID() {
        return this.record.getThreadID();
    }


    @Override
    public void setThreadID(final int threadID) {
        this.record.setThreadID(threadID);
    }


    @Override
    public long getMillis() {
        return this.record.getMillis();
    }


    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public void setMillis(final long millis) {
        this.record.setMillis(millis);
    }


    @Override
    public void setInstant(final Instant instant) {
        this.record.setInstant(instant);
    }


    @Override
    public Instant getInstant() {
        return this.record.getInstant();
    }


    @Override
    public Throwable getThrown() {
        return this.record.getThrown();
    }


    @Override
    public void setThrown(final Throwable thrown) {
        this.record.setThrown(thrown);
    }


    @Override
    public ResourceBundle getResourceBundle() {
        return this.record.getResourceBundle();
    }


    @Override
    public void setResourceBundle(final ResourceBundle bundle) {
        this.record.setResourceBundle(bundle);
    }


    @Override
    public String getResourceBundleName() {
        return this.record.getResourceBundleName();
    }


    @Override
    public void setResourceBundleName(final String name) {
        this.record.setResourceBundleName(name);
    }


    /**
     * @return {@link #getMillis()} converted to {@link OffsetDateTime} in local time zone.
     */
    public OffsetDateTime getTime() {
        return OffsetDateTime.ofInstant(getInstant(), TIME_ZONE);
    }


    /**
     * @return printed stacktrace of {@link #getThrown()} or {@code null}
     */
    public String getThrownStackTrace() {
        if (getThrown() == null) {
            return null;
        }
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            getThrown().printStackTrace(pw);
            return sw.toString();
        } catch (final IOException e) {
            new ErrorManager().error("Cannot print stacktrace!", e, ErrorManager.FORMAT_FAILURE);
            return null;
        }
    }


    @Override
    public String toString() {
        return getMessage();
    }

    private static class SourceDetector {

        private static final Set<String> LOGGING_CLASSES = Set.of(
            "org.glassfish.main.jul.GlassFishLogger",
            "org.glassfish.main.jul.GlassFishLoggerWrapper",
            // see LogDomains in GlassFish sources
            "com.sun.logging.LogDomainsLogger",
            // remaining classes are in the JDK
            "java.util.logging.Logger",
            "java.util.logging.LoggingProxyImpl",
            // see LoggingPrintStream
            "java.lang.Throwable"
        );

        private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.SHOW_REFLECT_FRAMES);

        static void detectClassAndMethod(final LogRecord wrappedRecord) {
            STACK_WALKER
                .walk(stackFrames ->
                    stackFrames.dropWhile(SourceDetector::isSourceStackFrame)
                        .filter(SourceDetector::isSourceStackFrame)
                        .findFirst())
                .ifPresent(frame -> {
                    wrappedRecord.setSourceClassName(frame.getClassName());
                    wrappedRecord.setSourceMethodName(frame.getMethodName());
                });
        }

        /**
         * @param stackFrame the stack frame
         * @return {@code true} if the {@code stackFrame} will be used as a source
         */
        private static boolean isSourceStackFrame(StackWalker.StackFrame stackFrame) {
            return !isLoggingStackFrame(stackFrame)
                && !isReflectionStackFrame(stackFrame);
        }

        /**
         * @param stackFrame the stack frame
         * @return {@code true} if the {@code stackFrame} is logging frame
         */
        private static boolean isLoggingStackFrame(final StackWalker.StackFrame stackFrame) {
            final String sourceClassName = stackFrame.getClassName();
            return LOGGING_CLASSES.contains(sourceClassName)
                || sourceClassName.startsWith("sun.util.logging.");
        }

        /**
         * @param stackFrame the stack frame
         * @return {@code true} if the {@code stackFrame} is reflection frame
         */
        private static boolean isReflectionStackFrame(final StackWalker.StackFrame stackFrame) {
            final String sourceClassName = stackFrame.getClassName();
            return sourceClassName.startsWith("jdk.internal.reflect.")
                || sourceClassName.startsWith("java.lang.reflect.")
                || sourceClassName.startsWith("java.lang.invoke.")
                || sourceClassName.startsWith("sun.reflect.");
        }
    }
}
