/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.zip.GZIPOutputStream;

import org.glassfish.main.jul.tracing.GlassFishLoggingTracer;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

/**
 * LogFileArchiver manages history of log files, compresses them into gz files, removes old files.
 */
class LogFileArchiver {
    private static final Logger LOG = System.getLogger(LogFileArchiver.class.getName());
    private static final String GZIP_EXTENSION = ".gz";

    private final File mainLogFile;
    private final boolean compressOldLogFiles;
    private final int maxCountOfOldLogFiles;


    LogFileArchiver(File mainLogFile, boolean compressOldLogFiles, final int maxCountOfOldLogFiles) {
        this.mainLogFile = mainLogFile;
        this.compressOldLogFiles = compressOldLogFiles;
        this.maxCountOfOldLogFiles = maxCountOfOldLogFiles;
    }


    /**
     * @return file with a name ie. server.log_2024-01-13T11-45-37.gz
     */
    File getGzArchiveFile(final File rotatedFile) {
        return new File(rotatedFile.getParentFile(), rotatedFile.getName() + GZIP_EXTENSION);
    }


    /**
     * There is no need to block processing of new log records with this time consuming action,
     * so this starts a parallel thread.
     * However the cleanup is done in synchronized method to avoid collisions in a case when
     * there would be more parallel slow threads.
     *
     * @param archivedFile
     */
    void archive(File archivedFile) {
        final Runnable cleanup = () -> cleanUpHistoryLogFiles(archivedFile);
        new Thread(cleanup, "old-log-files-cleanup-" + mainLogFile.getName()).start();
    }


    private synchronized void cleanUpHistoryLogFiles(final File rotatedFile) {
        if (this.compressOldLogFiles) {
            compressFile(rotatedFile);
        }
        deleteOldLogFiles();
    }


    private void compressFile(final File rotatedFile) {
        final long start = System.currentTimeMillis();
        final File outFile = getGzArchiveFile(rotatedFile);
        final boolean compressed = gzipFile(rotatedFile, outFile);
        if (!compressed) {
            logError("Could not compress log file: " + rotatedFile.getAbsolutePath());
            return;
        }
        final long time = System.currentTimeMillis() - start;
        LOG.log(INFO, "File {0} of size {1} has been archived to file {2} of size {3} in {4} ms",
            rotatedFile, rotatedFile.length(), outFile, outFile.length(), time);
        final boolean deleted = rotatedFile.delete();
        if (!deleted) {
            logError("Could not delete uncompressed log file: " + rotatedFile.getAbsolutePath());
        }
    }


    private boolean gzipFile(final File inputFile, final File outputFile) {
        try (
            FileInputStream fis = new FileInputStream(inputFile);
            FileOutputStream fos = new FileOutputStream(outputFile);
            GZIPOutputStream gzos = new GZIPOutputStream(fos)
        ) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                gzos.write(buffer, 0, len);
            }
            gzos.finish();
            return true;
        } catch (IOException e) {
            final String message = "Error gzipping log file " + inputFile;
            GlassFishLoggingTracer.error(getClass(), message, e);
            LOG.log(ERROR, message, e);
            return false;
        }
    }


    private void deleteOldLogFiles() {
        if (this.maxCountOfOldLogFiles == 0) {
            return;
        }

        final File dir = this.mainLogFile.getParentFile();
        final String logFileName = this.mainLogFile.getName();
        if (dir == null) {
            return;
        }
        final FileFilter filter = f -> f.isFile() && !f.getName().equals(logFileName)
            && f.getName().startsWith(logFileName);
        Arrays.stream(dir.listFiles(filter)).sorted(Comparator.comparing(File::getName).reversed())
            .skip(this.maxCountOfOldLogFiles).forEach(this::deleteFile);
    }


    private void deleteFile(final File file) {
        final boolean delFile = file.delete();
        if (!delFile) {
            logError("Could not delete the log file: " + file);
        }
    }


    private void logError(final String message) {
        GlassFishLoggingTracer.error(getClass(), message);
        LOG.log(ERROR, message);
    }
}
