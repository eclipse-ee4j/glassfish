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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.glassfish.main.jul.tracing.GlassFishLoggingTracer;

/**
 * The buffer for log records.
 * <p>
 * If it is full and another record is comming to the buffer, the record will wait until the
 * buffer would have a free capacity, but only for a maxWait seconds.
 * <p>
 * If the buffer would not have free capacity even after the maxWait time, the buffer will be
 * automatically cleared, the incomming record will be lost and there will be a stacktrace in
 * standard error output - but that may be redirected to JUL again, so this must be reliable.
 * <ul>
 * <li>After this error handling procedure the logging will be available again in full capacity
 * but it's previous unprocessed log records would be lost.
 * <li>If the maxWait is lower than 1, the calling thread would be blocked until some records would
 * be processed. It may remain blocked forever.
 * </ul>
 *
 * @author David Matejcek
 */
class LogRecordBuffer {

    private final BlockingQueue<GlassFishLogRecord> pendingRecords = new LinkedBlockingQueue<>();
    private final CapacitySemaphore availableCapacity;

    private volatile int capacity;
    private volatile int maxWait;


    /**
     * The buffer for log records.
     * <p>
     * If it is full and another record is comming to the buffer, the record will wait until the
     * buffer would have a free capacity, maybe forever.
     * <p>
     * See also the another constructor.
     *
     * @param capacity capacity of the buffer.
     */
    LogRecordBuffer(final int capacity) {
        this(capacity, 0);
    }


    /**
     * The buffer for log records.
     * <p>
     * If it is full and another record is comming to the buffer, the record will wait until the
     * buffer would have a free capacity, but only for a maxWait seconds.
     * <p>
     * If the buffer would not have free capacity even after the maxWait time, the buffer will be
     * automatically cleared, the incomming record will be lost and there will be a stacktrace in
     * standard error output - but that may be redirected to JUL again, so this must be reliable.
     * <ul>
     * <li>After this error handling procedure the logging will be available again in full capacity
     * but it's previous unprocessed log records would be lost.
     * <li>If the maxWait is lower than 1, the calling thread would be blocked until some records would
     * be processed. It may remain blocked forever.
     * </ul>
     *
     * @param capacity capacity of the buffer.
     * @param maxWait maximal time in seconds to wait for the free capacity. If &lt; 1, can wait
     *            forever.
     */
    LogRecordBuffer(final int capacity, final int maxWait) {
        this.capacity = capacity;
        this.maxWait = maxWait;
        this.availableCapacity = new CapacitySemaphore(capacity);
    }


    /**
     * Reconfigures the buffer.
     *
     * @param newCapacity capacity of the buffer.
     * @param newMaxWait maximal time in seconds to wait for the free capacity. If &lt; 1, can wait
     *            forever.
     */
    public synchronized void reconfigure(final int newCapacity, final int newMaxWait) {
        if (this.maxWait != newMaxWait) {
            this.maxWait = newMaxWait;
        }

        final int permitsToAdd = newCapacity - capacity;
        if (permitsToAdd == 0) {
            // no need to change the capacity
            return;
        }
        if (permitsToAdd > 0) {
            this.availableCapacity.release(permitsToAdd);
        } else {
            this.availableCapacity.reducePermits(-permitsToAdd);
        }
        this.capacity = newCapacity;
    }


    /**
     * @return true if there are not pending records to provide.
     */
    public boolean isEmpty() {
        return pendingRecords.isEmpty();
    }


    /**
     * @return count of records in the buffer waiting to be processed.
     */
    public int getSize() {
        return pendingRecords.size();
    }


    /**
     * @return maximal count of records in the buffer waiting to be processed.
     */
    public int getCapacity() {
        return capacity;
    }


    /**
     * Waits for a record or thread interrupt signal
     *
     * @return {@link GlassFishLogRecord} or null if interrupted.
     */
    public GlassFishLogRecord pollOrWait() {
        try {
            GlassFishLogRecord logRecord = pendingRecords.take();
            if (availableCapacity.availablePermits() < this.capacity) {
                availableCapacity.release();
            }
            return logRecord;
        } catch (InterruptedException e) {
            return null;
        }
    }

    /**
     * @return null if there are no pending records, first in the buffer otherwise.
     */
    public GlassFishLogRecord poll() {
        GlassFishLogRecord logRecord = pendingRecords.poll();
        if (logRecord != null && availableCapacity.availablePermits() < this.capacity) {
            availableCapacity.release();
        }
        return logRecord;
    }


    /**
     * Adds the record to the buffer.
     *
     * @param record
     */
    public void add(final GlassFishLogRecord record) {
        if (maxWait > 0) {
            addWithTimeout(record);
        } else {
            addWithUnlimitedWaiting(record);
        }
    }


    /**
     * This prevents deadlock - when the waiting is not successful, it forcibly drops all waiting records.
     * Logs an error after that.
     */
    private void addWithTimeout(final GlassFishLogRecord record) {
        try {
            if (availableCapacity.tryAcquire(maxWait, TimeUnit.SECONDS)) {
                pendingRecords.add(record);
                availableCapacity.release();
                return;
            }
        } catch (final InterruptedException e) {
            GlassFishLoggingTracer.stacktrace(getClass(),
                "addWithTimeout - interrupted, adding waiting records before shutdown.");
            pendingRecords.add(record);
            return;
        }

        availableCapacity.drainPermits();
        pendingRecords.clear();

        // Note: the record is not meaningful for the message. The cause is in another place.
        pendingRecords.add(new GlassFishLogRecord(Level.SEVERE, //
                this + ": The buffer was forcibly cleared after " + maxWait + " s timeout for adding another log record." //
                        + " Log records were lost." //
                        + " It might be caused by a recursive deadlock," //
                        + " you can increase the capacity or the timeout to avoid this.", false));

        availableCapacity.release(capacity - 1);
    }


    /**
     * This prevents losing any records, but may end up in deadlock if the capacity is reached.
     */
    private void addWithUnlimitedWaiting(final GlassFishLogRecord record) {
        try {
            availableCapacity.acquire();
            pendingRecords.add(record);
            availableCapacity.release();
        } catch (final InterruptedException e) {
            GlassFishLoggingTracer.stacktrace(getClass(),
                "addWithUnlimitedWaiting - interrupted, adding waiting records before shutdown.");
            pendingRecords.add(record);
        }
    }


    /**
     * Returns simple name of this class and size/capacity
     *
     * @return ie.: LogRecordBuffer@2b488078[usage=5/10000, maxWaitTime=60 s]
     */
    @Override
    public String toString() {
        return super.toString() + "[usage=" + getSize() + "/" + getCapacity() + ", maxWaitTime=" + maxWait + " s]";
    }

    private static class CapacitySemaphore extends Semaphore {

        private static final long serialVersionUID = -4575150599241117311L;

        private CapacitySemaphore(int capacity) {
            super(capacity, true);
        }

        @Override
        protected void reducePermits(int reduction) {
            super.reducePermits(reduction);
        }


        @Override
        public String toString() {
            return super.toString() + "[availablePermits=" + availablePermits() + "][waitingThreadsForPermits="
                + getQueueLength() + "]";
        }
    }
}
