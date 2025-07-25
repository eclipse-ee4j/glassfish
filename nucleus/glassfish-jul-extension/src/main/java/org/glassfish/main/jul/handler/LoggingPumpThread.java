/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

import java.util.logging.Handler;

import org.glassfish.main.jul.record.GlassFishLogRecord;

import static org.glassfish.main.jul.tracing.GlassFishLoggingTracer.error;
import static org.glassfish.main.jul.tracing.GlassFishLoggingTracer.trace;

/**
 * The logging pump is a special thread with high priority, processing {@link GlassFishLogRecord}
 * instances in the {@link LogRecordBuffer} of the {@link Handler}
 *
 * @author David Matejcek
 */
abstract class LoggingPumpThread extends Thread {

    private final LogRecordBuffer buffer;


    protected LoggingPumpThread(final String threadName, final LogRecordBuffer buffer) {
        super(threadName);
        setDaemon(true);
        setPriority(Thread.MAX_PRIORITY);
        this.buffer = buffer;
    }

    /**
     * @return count of records processed until the {@link #flushOutput()} is called.
     */
    protected abstract int getFlushFrequency();

    /**
     * @param record null or record to log
     * @return false if the record was null, so the buffer is empty or waiting was interrupted
     */
    protected abstract boolean logRecord(final GlassFishLogRecord record);

    /**
     * Unconditionally flushes the output
     */
    protected abstract void flushOutput();


    @Override
    public final void run() {
        trace(GlassFishLogHandler.class, () -> "Logging pump for " + buffer + " started.");
        while (!isInterrupted()) {
            try {
                publishBatchFromBuffer();
            } catch (final Exception e) {
                error(getClass(), "Log record not published.", e);
                // Continue the loop without exiting
            }
        }
    }


    /**
     * Retrieves the LogRecord from our Queue and store them in the file
     */
    private void publishBatchFromBuffer() {
        if (!logRecord(buffer.pollOrWait())) {
            return;
        }
        if (getFlushFrequency() > 1) {
            // starting from 1, one record was already published
            for (int i = 1; i < getFlushFrequency(); i++) {
                if (!logRecord(buffer.poll())) {
                    break;
                }
            }
        }
        flushOutput();
    }
}
