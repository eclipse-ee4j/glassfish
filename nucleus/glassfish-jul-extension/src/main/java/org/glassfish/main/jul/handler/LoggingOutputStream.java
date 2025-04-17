/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.glassfish.main.jul.GlassFishLogManager;
import org.glassfish.main.jul.record.GlassFishLogRecord;


/**
 * Implementation of a {@link ByteArrayOutputStream} that flush the records to a {@link Logger}.
 * This is useful to redirect stderr and stdout to loggers.
 *
 * @author Jerome Dochez
 * @author Carla Mott
 * @author David Matejcek
 */
final class LoggingOutputStream extends ByteArrayOutputStream {

    private final String lineSeparator;
    private final Level logRecordLevel;
    private final LogRecordBuffer logRecordBuffer;
    private final String loggerName;
    private final Pump pump;
    private final AtomicBoolean closed = new AtomicBoolean();
    private final Charset charset;

    /**
     * Constructor
     *
     * @param logger Logger to write to
     * @param logRecordLevel  Level at which to write the log message
     * @param bufferCapacity maximal count of unprocessed records.
     * @param charset
     */
    LoggingOutputStream(final Logger logger, final Level logRecordLevel, final int bufferCapacity,
        final Charset charset) {
        this.lineSeparator = System.lineSeparator();
        this.loggerName = logger.getName();
        this.logRecordLevel = logRecordLevel;
        this.charset = charset;
        this.logRecordBuffer = new LogRecordBuffer(bufferCapacity);
        this.pump = new Pump(logger, this.logRecordBuffer);
    }


    void addRecord(final Throwable throwable) {
        if (closed.get()) {
            return;
        }
        final GlassFishLogRecord record = new GlassFishLogRecord(logRecordLevel, "", isSourceDetectionEnabled());
        record.setThrown(throwable);
        record.setLoggerName(this.loggerName);
        logRecordBuffer.add(record);
    }


    /**
     * Upon flush() write the existing contents of the OutputStream
     * to the logger as a log record.
     *
     * @throws IOException in case of error
     */
    @Override
    public void flush() throws IOException {
        if (closed.get()) {
            return;
        }
        final String message = getMessage();
        if (message.isEmpty() || lineSeparator.equals(message)) {
            // avoid empty records
            return;
        }
        final GlassFishLogRecord record = new GlassFishLogRecord(logRecordLevel, message, isSourceDetectionEnabled());
        record.setLoggerName(this.loggerName);
        logRecordBuffer.add(record);
    }

    private synchronized String getMessage() throws IOException {
        super.flush();
        final String logMessage = super.toString(charset).trim();
        super.reset();
        return logMessage;
    }


    private boolean isSourceDetectionEnabled() {
        final GlassFishLogManager manager = GlassFishLogManager.getLogManager();
        return manager == null ? false : manager.getConfiguration().isClassAndMethodDetectionEnabled();
    }


    /**
     * Shutdown the internal logging pump.
     */
    @Override
    public void close() throws IOException {
        flush();
        closed.set(true);
        pump.shutdown();
        super.close();
    }


    /**
     * Paren't {@link ByteArrayOutputStream#toString()} is synchronized. This method isn't.
     *
     * @return name of the class and information about the logger
     */
    @Override
    public String toString() {
        return getClass().getName() + " redirecting messages to the logger " + loggerName;
    }


    private static final class Pump extends Thread {

        private final LogRecordBuffer buffer;
        private final Logger logger;
        private volatile boolean pumpClosed;

        private Pump(final Logger logger, final LogRecordBuffer buffer) {
            this.buffer = buffer;
            this.logger = logger;
            setName("Logging pump for '" + logger.getName() + "'");
            setDaemon(true);
            setPriority(Thread.MAX_PRIORITY);
            start();
        }


        @Override
        public void run() {
            // the thread will be interrupted by it's owner finally
            while (!pumpClosed) {
                try {
                    logAllPendingRecordsOrWait();
                } catch (final Exception e) {
                    // Continue the loop without exiting
                    // Something is broken, but we cannot log it
                }
            }
        }


        /**
         * Kindly asks the pump to closed it's service. If the pump is locked waiting
         * on the buffer, interrupts the thread.
         * <p>
         * The pump can be locked, waiting
         */
        void shutdown() {
            pumpClosed = true;
            this.interrupt();
            // we interrupted waiting or working thread, now we have to process remaining records.
            logAllPendingRecords();
        }


        /**
         * Retrieves all log records from the buffer and logs them or waits for some.
         */
        private void logAllPendingRecordsOrWait() {
            if (!logRecord(buffer.pollOrWait())) {
                return;
            }
            logAllPendingRecords();
        }


        private void logAllPendingRecords() {
            while (true) {
                if (!logRecord(buffer.poll())) {
                    // end if there was nothing more to log
                    return;
                }
            }
        }


        /**
         * @return false if the record was null
         */
        private boolean logRecord(final LogRecord record) {
            if (record == null) {
                return false;
            }
            logger.log(record);
            return true;
        }
    }
}
