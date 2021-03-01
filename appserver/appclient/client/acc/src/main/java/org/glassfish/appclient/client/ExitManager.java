/*
 * Copyright (c) 2010, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.appclient.client;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Encapsulates the logic related to intercepting exceptions and VM exits in a test environment and creating a disk file
 * which records the results. Typically a test script would wait for the file to appear and then read it to find out the
 * results of the Java Web Start launch.
 * <p>
 * The report file format contains one line with the exit status value, then possibly additional lines containing stack
 * trace information.
 * <p>
 * The status is written to a temp file, then that file is renamed to the selected status file. This prevents a script
 * waiting for the status file to appear from seeing the file appear before it has been written and closed and thereby
 * not being able to read the status file's contents correctly.
 *
 * @author Tim Quinn
 */
class ExitManager implements Runnable {

    private final File tempStatusFile;
    private final File statusFile;

    private PrintWriter reportWriter;
    private CommentWriter commentWriter;
    private Throwable reportedFailure = null;
    private AtomicInteger reportedStatus = new AtomicInteger(0);

    private static final Logger logger = Logger.getLogger(ExitManager.class.getName());

    ExitManager(final String testReportLocation) {
        statusFile = new File(statusFileName(testReportLocation));
        tempStatusFile = new File(statusTempFileName(testReportLocation));

        prepareReportWriter(tempStatusFile);
        Runtime.getRuntime().addShutdownHook(new Thread(this));
        logger.log(Level.FINE, "ExitManager initialized");
    }

    /**
     * Executes when shutdown is in progress. Writes any recorded exit status to the status file and, if a failure has been
     * reported, writes the stack trace to the file as well.
     * <p>
     * The file is readable as a properties file. (The stack trace is written as comments.)
     */
    @Override
    public void run() {
        logger.log(Level.FINE, "ExitManager writing output");
        reportWriter.println("jws.exit.status=" + reportedStatus);
        if (reportedFailure != null) {
            reportedFailure.printStackTrace(commentWriter);
        }
        reportWriter.close();
        if (!tempStatusFile.renameTo(statusFile)) {
            throw new RuntimeException(
                    "Could not rename temp status file from " + tempStatusFile.getAbsolutePath() + " to " + statusFile.getAbsolutePath());
        }
    }

    private void prepareReportWriter(final File tempTestReportFile) {
        try {
            reportWriter = new PrintWriter(tempTestReportFile);
            commentWriter = new CommentWriter(reportWriter);
            logger.log(Level.FINE, "PrintWriter for temp exit file {0} ready", tempTestReportFile.getAbsolutePath());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String statusFileName(final String testReportLocation) {
        return testReportLocation + ".status";
    }

    private String statusTempFileName(final String testReportLocation) {
        return statusFileName(testReportLocation) + ".tmp";
    }

    void recordFailure(final Throwable t) {
        logger.log(Level.FINE, "Recording failure", t);
        reportedFailure = t;
        recordExit(1);
    }

    void recordExit(final int status) {
        logger.log(Level.FINE, "Recording exit {0}", status);
        reportedStatus.set(status);

    }

    private static class CommentWriter extends PrintWriter {

        private CommentWriter(final PrintWriter delegate) {
            super(delegate);
        }

        @Override
        public void println() {
            super.println();
            print("#");
        }
    }
}
