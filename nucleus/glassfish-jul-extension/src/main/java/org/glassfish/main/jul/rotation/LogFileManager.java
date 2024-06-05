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

package org.glassfish.main.jul.rotation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.lang.System.Logger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.glassfish.main.jul.tracing.GlassFishLoggingTracer;

//import static java.lang.System.Logger.Level.DEBUG;
//import static java.lang.System.Logger.Level.ERROR;
//import static java.lang.System.Logger.Level.INFO;


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
//    private static final Logger LOG = System.getLogger(LogFileManager.class.getName());

    private static final DateTimeFormatter SUFFIX_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");

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
    public synchronized void write(String text) throws IllegalStateException {
        if (!isOutputEnabled()) {
            throw new IllegalStateException("The file output is disabled!");
        }
        try {
            writer.write(text);
        } catch (Exception e) {
            GlassFishLoggingTracer.error(getClass(), "Could not write to the output stream.", e);
        }
    }


    /**
     * Flushed the file writer and if the file is too large, rolls the file.
     */
    public synchronized void flush() {
        if (isOutputEnabled()) {
            try {
                writer.flush();
            } catch (IOException e) {
                GlassFishLoggingTracer.error(getClass(), "Could not flush the writer.", e);
            }
        }
        rollIfFileTooBig();
    }


    /**
     * @return the size of the logFile in bytes. The value is obtained from the outputstream, only
     *         if the output stream is closed, this method will check the file system.
     */
    public synchronized long getFileSize() {
        return this.writer == null ? this.logFile.length() : this.writer.getBytesWritten();
    }


    /**
     * Calls {@link #roll()} if the file is bigger than limit given in constructor.
     */
    public synchronized void rollIfFileTooBig() {
        if (isRollFileSizeLimitReached()) {
            roll();
        }
    }


    /**
     * Calls {@link #roll()} if the file is not empty.
     */
    public synchronized void rollIfFileNotEmpty() {
        if (getFileSize() > 0) {
            roll();
        }
    }


    /**
     * Rolls the file regardless of it's size and if it is currently used for output.
     * <p>
     * But if the output was enabled, the output is suspended first, then file rolls
     * and finally if the output was enabled before this method call, it is enabled
     * again.
     */
    public synchronized void roll() {
        final boolean wasOutputEnabled = isOutputEnabled();
//        LOG.log(INFO, "Rolling the file {0}; output was originally enabled: {1}", this.logFile, wasOutputEnabled);
        disableOutput();
        Exception failure = null;
        File archivedFile = null;
        try {
            if (this.logFile.createNewFile()) {
                return;
            }
            archivedFile = prepareAchivedLogFileTarget();
            moveFile(logFile, archivedFile);
            forceOSFilesync(logFile);
            return;
        } catch (Exception e) {
            failure = e;
        } finally {
            if (wasOutputEnabled) {
                enableOutput();
            }
            if (archivedFile != null) {
                archiver.archive(archivedFile);
            }
        }
//        LOG.log(ERROR, "Error, could not rotate log file " + logFile, failure);
    }


    /**
     * @return true if the handler owning this instance can write to the outputstream.
     */
    public synchronized boolean isOutputEnabled() {
        return this.writer != null;
    }


    /**
     * Creates the file, initializes the MeteredStream and calls the stream setter given in
     * constructor.
     * <p>
     * Redundant calls do nothing.
     * @throws IllegalStateException if the output could not be enabled (IO issues)
     */
    public synchronized void enableOutput() {
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
//            LOG.log(DEBUG, "Output to {0} enabled again.", this.logFile);
        } catch (Exception e) {
            throw new IllegalStateException("Could not open the log file for writing: " + this.logFile, e);
        }
    }


    /**
     * Calls the close method given in constructor, then closes the output stream.
     * <p>
     * Redundant calls do nothing.
     */
    public synchronized void disableOutput() {
        if (!isOutputEnabled()) {
            return;
        }
        try {
//            LOG.log(DEBUG, "Closing writer: {0}", writer);
            this.writer.close();
        } catch (final IOException e) {
            GlassFishLoggingTracer.error(getClass(), "Could not close the output stream.", e);
        }
        this.writer = null;
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


    /**
     * Make sure that server.log contents are flushed out to start from a clean file again after
     * the rename...
     *
     * @param file
     * @throws IOException
     */
    private void forceOSFilesync(final File file) throws IOException {
        new FileOutputStream(file).close();
    }


    private void moveFile(final File logFileToArchive, final File target) throws IOException {
        GlassFishLoggingTracer.trace(getClass(),
            () -> "moveFile(logFileToArchive=" + logFileToArchive + ", target=" + target + ")");
        try {
            Files.move(logFileToArchive.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE);
        } catch (UnsupportedOperationException | IOException e) {
            // If we don't succeed with file rename which most likely can happen on
            // Windows because of multiple file handles opened. We go through Plan B to
            // copy bytes explicitly to a renamed file.
            // Can happen on some windows file systems - then we try non-atomic version at least.
            GlassFishLoggingTracer.error(getClass(), String.format(
                "File %s could not be renamed to %s atomically, now trying to move it without this request.",
                logFileToArchive, target), e);
            Files.move(logFileToArchive.toPath(), target.toPath());
        }
    }
}
