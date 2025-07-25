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

package org.glassfish.main.jul.rotation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.glassfish.main.jul.tracing.GlassFishLoggingTracer;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static org.glassfish.main.jul.tracing.GlassFishLoggingTracer.trace;


/**
 * Manages the logging file, it's rotations, packing of rolled log file, etc.
 * <p>
 * Note about logging in this class - it is mixed JUL logging and private {@link GlassFishLoggingTracer}
 * which prints errors to the original standard error output. The reason is practical - if everything
 * works fine, JUL logging and all it's handlers work properly. If they don't, you would not see
 * errors in any log.
 * So if this class cannot delete or pack log files, it is probably a sign of a really serious problem
 * - then the standard error output is more reliable way to see logs.
 *
 * @author David Matejcek
 */
public class LogFileManager {

    private static final DateTimeFormatter SUFFIX_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");

    private final ReentrantLock lock = new ReentrantLock(true);
    private final File logFile;
    private final LogFileArchiver archiver;
    private final Charset fileEncoding;
    private final long maxFileSize;

    private MeteredFileWriter writer;


    /**
     * Creates the manager and initializes it with given parameters. It only creates the manager but
     * does not enable the output. Call {@link #enableOutput()} for that.
     *
     * @param logFile - output logging file path
     * @param fileEncoding
     * @param maxFileSize - if the size of the file crosses this value, the file is renamed to the
     *            logFile name with added suffix ie. <code>server.log_2020-05-01T16-28-27</code>
     * @param compressOldLogFiles - if true, rolled file is packed to GZIP (so the file will have a name
     *            ie. <code>server.log_2020-05-01T21-50-09.gz</code>)
     * @param maxCountOfOldLogFiles - if the count of rolled files with logFile's file name prefix
     *            crosses this value, old files will be permanently deleted.
     */
    public LogFileManager(final File logFile, Charset fileEncoding, //
        final long maxFileSize, final boolean compressOldLogFiles, final int maxCountOfOldLogFiles //
    ) {
        this.logFile = logFile;
        this.fileEncoding = fileEncoding;
        this.maxFileSize = maxFileSize;
        this.archiver = new LogFileArchiver(logFile, compressOldLogFiles, maxCountOfOldLogFiles);
    }


    /**
     * Writes the text to the log file.
     *
     * @param text
     * @throws IllegalStateException if the output is disabled.
     */
    public void write(String text) throws IllegalStateException {
        lock.lock();
        try {
            if (!isOutputEnabled()) {
                throw new IllegalStateException("The file output is disabled!");
            }
            try {
                writer.write(text);
            } catch (Exception e) {
                GlassFishLoggingTracer.error(getClass(), "Could not write to the output stream.", e);
            }
        } finally {
            lock.unlock();
        }
    }


    /**
     * Flushed the file writer and if the file is too large, rolls the file.
     */
    public void flush() {
        lock.lock();
        try {
            if (isOutputEnabled()) {
                try {
                    writer.flush();
                } catch (IOException e) {
                    GlassFishLoggingTracer.error(getClass(), "Could not flush the writer.", e);
                }
            }
            rollIfFileTooBig();
        } finally {
            lock.unlock();
        }
    }


    /**
     * @return the size of the logFile in bytes. The value is obtained from the outputstream, only
     *         if the output stream is closed, this method will check the file system.
     */
    public long getFileSize() {
        lock.lock();
        try {
            return this.writer == null ? this.logFile.length() : this.writer.getBytesWritten();
        } finally {
            lock.unlock();
        }
    }


    /**
     * Calls {@link #roll()} if the file is bigger than limit given in constructor.
     */
    public void rollIfFileTooBig() {
        lock.lock();
        try {
            if (isRollFileSizeLimitReached()) {
                roll();
            }
        } finally {
            lock.unlock();
        }
    }


    /**
     * Calls {@link #roll()} if the file is not empty.
     */
    public void rollIfFileNotEmpty() {
        lock.lock();
        try {
            if (getFileSize() > 0) {
                roll();
            }
        } finally {
            lock.unlock();
        }
    }


    /**
     * Rolls the file regardless of it's size and if it is currently used for output.
     * <p>
     * But if the output was enabled, the output is suspended first, then file rolls
     * and finally if the output was enabled before this method call, it is enabled
     * again.
     */
    public void roll() {
        lock.lock();
        try (AsyncLogger logger = new AsyncLogger()) {
            final boolean wasOutputEnabled = isOutputEnabled();
            final String logMsg = "Rolling the file " + this.logFile + "; output was originally enabled: " + wasOutputEnabled;
            trace(LogFileManager.class, logMsg);
            logger.logInfo(logMsg);
            disableOutput();
            File archivedFile = null;
            try {
                if (this.logFile.createNewFile()) {
                    return;
                }
                archivedFile = prepareAchivedLogFileTarget();
                trace(LogFileManager.class, "Archived file: " + archivedFile);
                moveFile(logFile, archivedFile, logger);
                if (!logFile.createNewFile()) {
                    logger.logError("Error, could not create a new log file " + logFile + "!", null);
                }
            } catch (Exception e) {
                logger.logError("Error, could not rotate log file " + logFile, e);
            } finally {
                if (wasOutputEnabled) {
                    enableOutput();
                }
                if (archivedFile != null) {
                    archiver.archive(archivedFile);
                }
            }
        } finally {
            lock.unlock();
        }
    }


    /**
     * @return true if the handler owning this instance can write to the outputstream.
     */
    public boolean isOutputEnabled() {
        lock.lock();
        try {
            return this.writer != null;
        } finally {
            lock.unlock();
        }
    }


    /**
     * Creates the file, initializes the MeteredStream and calls the stream setter given in
     * constructor.
     * <p>
     * Redundant calls do nothing.
     * @throws IllegalStateException if the output could not be enabled (IO issues)
     */
    public void enableOutput() {
        lock.lock();
        try {
            if (isOutputEnabled()) {
                throw new IllegalStateException("Output is already enabled!");
            }
            // check that the parent directory exists.
            final File parent = this.logFile.getParentFile();
            // if the file instance doesn't use parent, we don't care about it.
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("Failed to create the parent directory " + parent.getAbsolutePath());
            }
            try {
                final FileOutputStream fout = new FileOutputStream(this.logFile, true);
                final BufferedOutputStream bout = new BufferedOutputStream(fout);
                final MeteredStream stream = new MeteredStream(bout, this.logFile.length());
                this.writer = new MeteredFileWriter(stream, fileEncoding);
                trace(LogFileManager.class, () -> "Output enabled to " + this.logFile);
            } catch (Exception e) {
                throw new IllegalStateException("Could not open the log file for writing: " + this.logFile, e);
            }
        } finally {
            lock.unlock();
        }
    }


    /**
     * Calls the close method given in constructor, then closes the output stream.
     * <p>
     * Redundant calls do nothing.
     */
    public void disableOutput() {
        lock.lock();
        try {
            if (!isOutputEnabled()) {
                return;
            }
            try {
                trace(LogFileManager.class, () -> "Closing writer: " + writer);
                this.writer.close();
            } catch (final IOException e) {
                GlassFishLoggingTracer.error(getClass(), "Could not close the output stream.", e);
            }
            this.writer = null;
            trace(LogFileManager.class, () -> "Output disabled to " + this.logFile);
        } finally {
            lock.unlock();
        }
    }


    private boolean isRollFileSizeLimitReached() {
        if (this.maxFileSize <= 0) {
            return false;
        }
        final long fileSize = getFileSize();
        return fileSize >= this.maxFileSize;
    }


    private File prepareAchivedLogFileTarget() {
        final String archivedFileNameBase = logFile.getName() + "_" + SUFFIX_FORMATTER.format(LocalDateTime.now());
        int counter = 1;
        String archivedFileName = archivedFileNameBase;
        while (true) {
            final File archivedLogFile = new File(logFile.getParentFile(), archivedFileName);
            // We have to avoid collisions with archives too
            final File archivedGzLogFile = archiver.getGzArchiveFile(archivedLogFile);
            if (!archivedLogFile.exists() && !archivedGzLogFile.exists()) {
                return archivedLogFile;
            }
            counter++;
            archivedFileName = archivedFileNameBase + "_" + counter;
        }
    }


    private void moveFile(final File logFileToArchive, final File target, final AsyncLogger logger) throws IOException {
        logger.logInfo("Archiving file " + logFileToArchive + " to " + target);
        try {
            Files.move(logFileToArchive.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE);
        } catch (UnsupportedOperationException | IOException e) {
            // If we don't succeed with file rename which most likely can happen on
            // Windows because of multiple file handles opened. We go through Plan B to
            // copy bytes explicitly to a renamed file.
            // Can happen on some windows file systems - then we try non-atomic version at least.
            logger.logError(String.format(
                "File %s could not be renamed to %s atomically, now trying to move it without this request.",
                logFileToArchive, target), e);
            Files.move(logFileToArchive.toPath(), target.toPath());
        }
    }


    /**
     * This logs in a separate thread to avoid deadlocks. The separate thread can be blocked when
     * the LogRecordBuffer is full while the LogFileManager is still locked and doesn't process
     * any records until it finishes rolling the file.
     * <p>
     * The count of messages is limited, so we can do this.
     * <p>
     * However it is not suitable for all errors - if we cannot write to the file, this would just create
     * another record which could not be written.
     */
    private static class AsyncLogger extends Thread implements AutoCloseable {

        private final AtomicBoolean stop;
        private final ConcurrentLinkedQueue<AsyncLogRecord> queue;
        private final Logger logger;

        private AsyncLogger() {
            super("LogFileManagerAsyncLogger");
            setDaemon(true);
            this.queue = new ConcurrentLinkedQueue<>();
            this.stop = new AtomicBoolean();
            this.logger = Logger.getLogger(LogFileManager.class.getName(), null);
            start();
        }

        void logInfo(final String message) {
            trace(getClass(), message);
            queue.add(new AsyncLogRecord(INFO, message, null));
        }

        void logError(final String message, final Exception exception) {
            GlassFishLoggingTracer.error(getClass(), message, exception);
            queue.add(new AsyncLogRecord(SEVERE, message, exception));
        }

        @Override
        public void close() {
            this.stop.set(true);
        }

        @Override
        public void run() {
            while(!stop.get()) {
                drainQueue();
                Thread.onSpinWait();
            }
            drainQueue();
        }

        private void drainQueue() {
            while (true) {
                AsyncLogRecord record = queue.poll();
                if (record == null) {
                    break;
                }
                logger.log(record);
            }
        }
    }


    private static class AsyncLogRecord extends GlassFishLogRecord {

        private static final long serialVersionUID = -8159574547676058852L;

        AsyncLogRecord(Level level, String message, Throwable error) {
            super(level, message, true);
            setThrown(error);
        }
    }
}
