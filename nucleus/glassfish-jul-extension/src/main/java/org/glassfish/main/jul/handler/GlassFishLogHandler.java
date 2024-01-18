/*
 * Copyright (c) 2022, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Timer;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.glassfish.main.jul.cfg.GlassFishLoggingConstants;
import org.glassfish.main.jul.env.LoggingSystemEnvironment;
import org.glassfish.main.jul.formatter.LogFormatDetector;
import org.glassfish.main.jul.formatter.UniformLogFormatter;
import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.glassfish.main.jul.record.MessageResolver;
import org.glassfish.main.jul.rotation.DailyLogRotationTimerTask;
import org.glassfish.main.jul.rotation.LogFileManager;
import org.glassfish.main.jul.rotation.LogRotationTimerTask;
import org.glassfish.main.jul.rotation.PeriodicalLogRotationTimerTask;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.logging.Level.ALL;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.BUFFER_CAPACITY;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.BUFFER_TIMEOUT;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.DEFAULT_BUFFER_CAPACITY;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.DEFAULT_BUFFER_TIMEOUT;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.DEFAULT_ROTATION_LIMIT_MB;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.ENABLED;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.ENCODING;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.FLUSH_FREQUENCY;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.LEVEL;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.MINIMUM_ROTATION_LIMIT_MB;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.OUTPUT_FILE;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.REDIRECT_STANDARD_STREAMS;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.ROTATION_COMPRESS;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.ROTATION_LIMIT_SIZE;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.ROTATION_LIMIT_TIME;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.ROTATION_MAX_HISTORY;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.ROTATION_ON_DATE_CHANGE;
import static org.glassfish.main.jul.tracing.GlassFishLoggingTracer.error;
import static org.glassfish.main.jul.tracing.GlassFishLoggingTracer.trace;

/**
 * GlassFish log handler
 * <ul>
 * <li>can redirect output going through STDOUT and STDERR
 * <li>buffers log records
 * </ul>
 * <b>WARNING</b>: If you configure this handler to redirect standard output, you have to prevent
 * the situation when any other handler would use it.
 *
 * @author David Matejcek
 * @author Jerome Dochez (original concepts of GFFileHandler)
 * @author Carla Mott (original concepts of GFFileHandler)
 */
public class GlassFishLogHandler extends Handler implements ExternallyManagedLogHandler {

    private static final String LOGGER_NAME_STDOUT = "jakarta.enterprise.logging.stdout";
    private static final String LOGGER_NAME_STDERR = "jakarta.enterprise.logging.stderr";
    private static final Logger STDOUT_LOGGER = Logger.getLogger(LOGGER_NAME_STDOUT);
    private static final Logger STDERR_LOGGER = Logger.getLogger(LOGGER_NAME_STDERR);
    private static final MessageResolver MSG_RESOLVER = new MessageResolver();

    private LoggingPrintStream stdoutStream;
    private LoggingPrintStream stderrStream;

    private final LogRecordBuffer logRecordBuffer;
    private LogRotationTimerTask rotationTimerTask;

    private GlassFishLogHandlerConfiguration configuration;

    private final Timer rotationTimer = new Timer("log-rotation-timer-for-" + getClass().getSimpleName());

    private volatile GlassFishLogHandlerStatus status;
    private LoggingPump pump;
    private LogFileManager logFileManager;

    private boolean doneHeader;

    /**
     * Creates the configuration object for this class or it's descendants.
     *
     * @param handlerClass
     * @return the configuration parsed from the property file, usable to call
     *         the {@link #reconfigure(GlassFishLogHandlerConfiguration)} method.
     */
    public static GlassFishLogHandlerConfiguration createGlassFishLogHandlerConfiguration(
        final Class<? extends GlassFishLogHandler> handlerClass) {
        final HandlerConfigurationHelper helper = HandlerConfigurationHelper.forHandlerClass(handlerClass);
        final GlassFishLogHandlerConfiguration configuration = new GlassFishLogHandlerConfiguration();
        configuration.setLevel(helper.getLevel(LEVEL, ALL));
        configuration.setEncoding(helper.getCharset(ENCODING, UTF_8));
        configuration.setEnabled(helper.getBoolean(ENABLED, true));
        configuration.setLogFile(helper.getFile(OUTPUT_FILE, null));
        configuration.setRedirectStandardStreams(helper.getBoolean(REDIRECT_STANDARD_STREAMS, Boolean.FALSE));

        configuration.setFlushFrequency(helper.getNonNegativeInteger(FLUSH_FREQUENCY, 1));
        configuration.setBufferCapacity(helper.getInteger(BUFFER_CAPACITY, DEFAULT_BUFFER_CAPACITY));
        configuration.setBufferTimeout(helper.getInteger(BUFFER_TIMEOUT, DEFAULT_BUFFER_TIMEOUT));

        final Integer rotationLimitMB = helper.getInteger(ROTATION_LIMIT_SIZE, DEFAULT_ROTATION_LIMIT_MB);
        final long rotationLimitB = GlassFishLoggingConstants.BYTES_PER_MEGABYTES
            * (rotationLimitMB >= MINIMUM_ROTATION_LIMIT_MB ? rotationLimitMB : DEFAULT_ROTATION_LIMIT_MB);
        configuration.setRotationSizeLimitBytes(rotationLimitB);
        configuration.setCompressionOnRotation(helper.getBoolean(ROTATION_COMPRESS, Boolean.FALSE));
        configuration.setRotationOnDateChange(helper.getBoolean(ROTATION_ON_DATE_CHANGE, Boolean.FALSE));
        configuration.setRotationTimeLimitMinutes(helper.getNonNegativeInteger(ROTATION_LIMIT_TIME, 0));
        configuration.setMaxArchiveFiles(helper.getNonNegativeInteger(ROTATION_MAX_HISTORY, 10));

        final Formatter formatter = helper.getFormatter(UniformLogFormatter.class);
        configuration.setFormatterConfiguration(formatter);
        return configuration;
    }


    public GlassFishLogHandler() {
        this(createGlassFishLogHandlerConfiguration(GlassFishLogHandler.class));
    }


    public GlassFishLogHandler(final GlassFishLogHandlerConfiguration configuration) {
        trace(GlassFishLogHandler.class, () -> "GlassFishLogHandler(configuration=" + configuration + ")");
        // parent StreamHandler already set level, filter, encoding and formatter.
        setLevel(configuration.getLevel());
        setEncoding(configuration.getEncoding());

        this.logRecordBuffer = new LogRecordBuffer(
            configuration.getBufferCapacity(), configuration.getBufferTimeout());

        reconfigure(configuration);
    }


    @Override
    public boolean isReady() {
        return status == GlassFishLogHandlerStatus.ON || !this.configuration.isEnabled();
    }


    private void setEncoding(final Charset encoding) {
        try {
            super.setEncoding(encoding.name());
        } catch (final SecurityException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Reached unreachable exception.", e);
        }
    }


    /**
     * @return clone of the internal configuration
     */
    public GlassFishLogHandlerConfiguration getConfiguration() {
        return this.configuration.clone();
    }


    /**
     * Reconfigures the handler: first cancels scheduled rotation of the output file,
     * then stops the output.
     * After that replaces the original configuration with the argument and starts
     * the output again - if it fails, turns off completely including accepting
     * new records and throws the exception.
     *
     * @param newConfiguration
     */
    public synchronized void reconfigure(final GlassFishLogHandlerConfiguration newConfiguration) {
        trace(GlassFishLogHandler.class, () -> "reconfigure(configuration=" + newConfiguration + ")");
        // stop using output, but allow collecting records. Logging system can continue to work.
        this.status = GlassFishLogHandlerStatus.ACCEPTING;
        if (this.rotationTimerTask != null) {
            // to avoid another task from last configuration runs it's action.
            this.rotationTimerTask.cancel();
            this.rotationTimerTask = null;
        }
        // stop pump. If reconfiguration would fail, it is better to leave it down.
        // records from the buffer will be processed if the last configuration was valid.
        stopPump();
        this.configuration = newConfiguration;

        try {
            this.status = startLoggingIfPossible();
        } catch (final Exception e) {
            this.status = GlassFishLogHandlerStatus.OFF;
            throw e;
        }
    }


    /**
     * Does not publish the record, but puts it into the queue buffer to be processed by an internal
     * thread.
     */
    @Override
    public void publish(final LogRecord record) {
        if (this.status == GlassFishLogHandlerStatus.OFF) {
            return;
        }
        if (this.status == GlassFishLogHandlerStatus.ACCEPTING) {
            // The configuration is incomplete, but acceptation can start.
            // This prevents deadlocks.
            // At this state we cannot decide if the record is loggable
            logRecordBuffer.add(MSG_RESOLVER.resolve(record));
            return;
        }
        if (!isLoggable(record)) {
            return;
        }

        final GlassFishLogRecord enhancedLogRecord = MSG_RESOLVER.resolve(record);
        logRecordBuffer.add(enhancedLogRecord);
    }


    @Override
    public boolean isLoggable(final LogRecord record) {
        // pump might be closed, super.isLoggable would refuse all records then.
        return this.configuration.isEnabled()
            && (this.status == GlassFishLogHandlerStatus.ACCEPTING || super.isLoggable(record));
    }


    @Override
    public void flush() {
        if (logFileManager != null) {
            logFileManager.flush();
        }
    }


    /**
     * Explicitly rolls the log file.
     */
    public synchronized void roll() {
        trace(GlassFishLogHandler.class, "roll()");
        final PrivilegedAction<Void> action = () -> {
            this.logFileManager.roll();
            updateRollSchedule();
            return null;
        };
        AccessController.doPrivileged(action);
    }


    /**
     * First stops all dependencies using this handler (changes status to
     * {@link GlassFishLogHandlerStatus#OFF}, then closes all resources managed
     * by this handler and finally closes the output stream.
     */
    @Override
    public synchronized void close() {
        trace(GlassFishLogHandler.class, "close()");
        this.status = GlassFishLogHandlerStatus.OFF;
        if (this.rotationTimerTask != null) {
            this.rotationTimerTask.cancel();
            this.rotationTimerTask = null;
        }
        this.rotationTimer.cancel();
        try {
            LoggingSystemEnvironment.resetStandardOutputs();
            if (this.stdoutStream != null) {
                this.stdoutStream.close();
                this.stdoutStream = null;
            }

            if (this.stderrStream != null) {
                this.stderrStream.close();
                this.stderrStream = null;
            }
        } catch (final RuntimeException e) {
            error(GlassFishLogHandler.class, "close partially failed!", e);
        }

        stopPump();
    }


    @Override
    public String toString() {
        return super.toString() + "[status=" + status + ", buffer=" + this.logRecordBuffer //
            + ", file=" + this.configuration.getLogFile() + "]";
    }


    private GlassFishLogHandlerStatus startLoggingIfPossible() {
        trace(GlassFishLogHandler.class, "startLoggingIfPossible()");

        if (!this.configuration.isEnabled()) {
            trace(GlassFishLogHandler.class, "Output is disabled, the handler will not process any records.");
            return GlassFishLogHandlerStatus.OFF;
        }
        if (this.configuration.getLogFile() == null) {
            trace(GlassFishLogHandler.class, "Output file is not set, but acceptation will start.");
            return GlassFishLogHandlerStatus.ACCEPTING;
        }

        this.logFileManager = new LogFileManager(this.configuration.getLogFile(), this.configuration.getEncoding(),
            this.configuration.getRotationSizeLimitBytes(), this.configuration.isCompressionOnRotation(),
            this.configuration.getMaxArchiveFiles());

        final Formatter formatter = configuration.getFormatterConfiguration();
        setFormatter(formatter);
        if (isRollRequired(configuration.getLogFile(), formatter)) {
            logFileManager.roll();
        }
        // Output is disabled after the creation of the LogFileManager.
        logFileManager.enableOutput();
        updateRollSchedule();

        // enable only if everything else was ok to prevent situation when
        // something would break and we would redirect STDOUT+STDERR
        if (this.configuration.isRedirectStandardStreams()) {
            initStandardStreamsLogging();
        } else {
            LoggingSystemEnvironment.resetStandardOutputs();
        }

        this.pump = new LoggingPump("GlassFishLogHandler log pump", this.logRecordBuffer);
        this.pump.start();
        return GlassFishLogHandlerStatus.ON;
    }


    private synchronized void stopPump() {
        trace(GlassFishLogHandler.class, "stopPump()");
        if (this.pump != null) {
            this.pump.interrupt();
            this.pump = null;
        }

        if (logFileManager == null) {
            return;
        }

        // we cannot publish anything if we don't have the stream configured.
        if (this.logFileManager.isOutputEnabled()) {
            drainLogRecords();
        }
        this.logFileManager.disableOutput();
        this.logFileManager = null;
    }


    private void drainLogRecords() {
        // The counter protects us from the risk that this thread will not be fast enough to process
        // all records and more are still coming. Records which would come after this process
        // started will not be processed.
        long counter = this.logRecordBuffer.getSize();
        while (counter-- >= 0) {
            if (!publishRecord(this.logRecordBuffer.poll())) {
                return;
            }
        }
    }


    private void initStandardStreamsLogging() {
        trace(GlassFishLogHandler.class, "initStandardStreamsLogging()");
        this.stdoutStream = LoggingPrintStream.create(STDOUT_LOGGER, INFO, 5000, configuration.getEncoding());
        this.stderrStream = LoggingPrintStream.create(STDERR_LOGGER, SEVERE, 1000, configuration.getEncoding());
        System.setOut(this.stdoutStream);
        System.setErr(this.stderrStream);
    }


    private void updateRollSchedule() {
        trace(GlassFishLogHandler.class, "updateRollSchedule()");
        if (rotationTimerTask != null) {
            rotationTimerTask.cancel();
            rotationTimerTask = null;
        }
        if (this.configuration.isRotationOnDateChange()) {
            this.rotationTimerTask = new DailyLogRotationTimerTask(this::scheduledRoll);
            this.rotationTimer.schedule(rotationTimerTask, rotationTimerTask.computeDelayInMillis());
        } else if (this.configuration.getRotationTimeLimitMinutes() > 0) {
            final long delayInMillis = this.configuration.getRotationTimeLimitMinutes() * 60 * 1000L;
            this.rotationTimerTask = new PeriodicalLogRotationTimerTask(this::scheduledRoll, delayInMillis);
            this.rotationTimer.schedule(rotationTimerTask, rotationTimerTask.computeDelayInMillis());
        }
    }


    /**
     * If the file is not empty, rolls. Then updates the next roll schedule.
     */
    private synchronized void scheduledRoll() {
        this.logFileManager.rollIfFileNotEmpty();
        updateRollSchedule();
    }


    /**
     * Really publishes record via super.publish method call.
     *
     * @param record
     * @return true if the record was not null, false if nothing was done.
     */
    private boolean publishRecord(final GlassFishLogRecord record) {
        if (record == null) {
            return false;
        }
        if (!isLoggable(record)) {
            return true;
        }
        final String msg;
        try {
            msg = getFormatter().format(record);
        } catch (Exception ex) {
            // We don't want to throw an exception here, but we
            // report the exception to any registered ErrorManager.
            reportError(null, ex, ErrorManager.FORMAT_FAILURE);
            return true;
        }

        if (!doneHeader) {
            logFileManager.write(getFormatter().getHead(this));
            doneHeader = true;
        }
        logFileManager.write(msg);
        return true;
    }


    private static boolean isRollRequired(final File logFile, final Formatter formatter) {
        if (logFile.length() == 0) {
            return false;
        }
        final String detectedFormatterName = new LogFormatDetector().detectFormatter(logFile);
        return detectedFormatterName == null || !formatter.getClass().getName().equals(detectedFormatterName);
    }

    private final class LoggingPump extends LoggingPumpThread {

        private LoggingPump(String threadName, LogRecordBuffer buffer) {
            super(threadName, buffer);
        }


        @Override
        protected boolean isShutdownRequested() {
            return !configuration.isEnabled() || !isReady();
        }


        @Override
        protected int getFlushFrequency() {
            return configuration.getFlushFrequency();
        }

        @Override
        protected boolean logRecord(final GlassFishLogRecord record) {
            return publishRecord(record);
        }

        @Override
        protected void flushOutput() {
            flush();
        }
    }

    private enum GlassFishLogHandlerStatus {
        /** Closed of after failure, no records accepted. */
        OFF,
        /** Partially configured, accepting records, but doesn't push them to the output */
        ACCEPTING,
        /** Full service, accepting and processing records */
        ON
    }
}
