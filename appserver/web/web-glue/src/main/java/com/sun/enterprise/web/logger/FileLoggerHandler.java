/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.web.logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * An implementation of FileLoggerHandler which logs to virtual-server property
 * log-file when enabled
 */
public class FileLoggerHandler extends Handler {
    private static final int LOG_QUEUE_SIZE = 5000;
    private static final int FLUSH_FREQUENCY = 1;

    private volatile PrintWriter printWriter;
    private final String logFile;

    private final AtomicInteger association = new AtomicInteger(0);
    private final AtomicBoolean done = new AtomicBoolean(false);
    private final BlockingQueue<LogRecord> pendingRecords = new ArrayBlockingQueue<>(LOG_QUEUE_SIZE);
    private final Thread pump;

    FileLoggerHandler(String logFile) {
        setLevel(Level.ALL);
        this.logFile = logFile;

        try {
            printWriter = new PrintWriter(new FileOutputStream(logFile, true));
        } catch (IOException e) {
            // throw new RuntimeException(e);
        }

        pump = new Thread() {

            @Override
            public void run() {
                try {
                    while (!done.get()) {
                        log();
                    }
                } catch (RuntimeException ex) {
                }
            }
        };
        pump.start();
    }


    private void writeLogRecord(LogRecord record) {
        if (printWriter != null) {
            printWriter.write(getFormatter().format(record));
            printWriter.flush();
        }
    }


    private void log() {
        // write the first record
        try {
            writeLogRecord(pendingRecords.take());
        } catch (InterruptedException e) {
            // ignore
        }

        // write FLUSH_FREQUENCY record(s) more
        List<LogRecord> list = new ArrayList<>();
        int numOfRecords = pendingRecords.drainTo(list, FLUSH_FREQUENCY);
        for (int i = 0; i < numOfRecords; i++) {
            writeLogRecord(list.get(i));
        }
        flush();
    }


    /**
     * Increment the associations and return the result.
     */
    public int associate() {
        return association.incrementAndGet();
    }


    /**
     * Decrement the associations and return the result.
     */
    public int disassociate() {
        return association.decrementAndGet();
    }


    public boolean isAssociated() {
        return association.get() > 0;
    }


    /**
     * Overridden method used to capture log entries
     *
     * @param record The log record to be written out.
     */
    @Override
    public void publish(LogRecord record) {
        if (done.get()) {
            return;
        }

        // first see if this entry should be filtered out
        // the filter should keep anything
        if (getFilter() != null) {
            if (!getFilter().isLoggable(record)) {
                return;
            }
        }

        try {
            pendingRecords.add(record);
        } catch (IllegalStateException e) {
            // queue is full, start waiting
            try {
                pendingRecords.put(record);
            } catch (InterruptedException ex) {
                // too bad, record is lost...
            }
        }
    }


    /**
     * Called to close this log handler.
     */
    @Override
    public void close() {
        done.set(true);

        pump.interrupt();

        int size = pendingRecords.size();
        if (size > 0) {
            List<LogRecord> records = new ArrayList<>(size);
            pendingRecords.drainTo(records, size);
            for (LogRecord record : records) {
                writeLogRecord(record);
            }
        }

        if (printWriter != null) {
            try {
                printWriter.flush();
            } catch (Exception ex) {
                // ignore
            } finally {
                printWriter.close();
            }
        }
    }


    /**
     * Called to flush any cached data that
     * this log handler may contain.
     */
    @Override
    public void flush() {
        if (printWriter != null) {
            printWriter.flush();
        }
    }


    /**
     * Return location of log file associated to this handler.
     */
    public String getLogFile() {
        return logFile;
    }
}
