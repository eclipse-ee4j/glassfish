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

package org.glassfish.main.jul.handler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.glassfish.main.jul.record.GlassFishLogRecord;

/**
 * @author David Matejcek
 */
class LogRecordBuffer {

    private final int capacity;
    private final int maxWait;
    private final ArrayBlockingQueue<GlassFishLogRecord> pendingRecords;


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
    public LogRecordBuffer(final int capacity) {
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
    public LogRecordBuffer(final int capacity, final int maxWait) {
        this.capacity = capacity;
        this.maxWait = maxWait;
        this.pendingRecords = new ArrayBlockingQueue<>(capacity);
    }


    /**
     * @return true if there are not pending records to provide.
     */
    public boolean isEmpty() {
        return this.pendingRecords.isEmpty();
    }


    public int getSize() {
        return this.pendingRecords.size();
    }


    public int getCapacity() {
        return this.capacity;
    }


    /**
     * Waits for a record or thread interrupt signal
     *
     * @return {@link GlassFishLogRecord} or null if interrupted.
     */
    public GlassFishLogRecord pollOrWait() {
        try {
            return this.pendingRecords.take();
        } catch (final InterruptedException e) {
            return null;
        }
    }

    /**
     * @return null if there are no pending records, first in the buffer otherwise.
     */
    public GlassFishLogRecord poll() {
        return this.pendingRecords.poll();
    }


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
            if (this.pendingRecords.offer(record)) {
                return;
            }
            if (this.pendingRecords.offer(record, this.maxWait, TimeUnit.SECONDS)) {
                return;
            }
        } catch (final InterruptedException e) {
            // do nothing
        }

        this.pendingRecords.clear();
        // note: the record is not meaningful for the message. The cause is in another place.
        this.pendingRecords.offer(new GlassFishLogRecord(Level.SEVERE, //
            this + ": The buffer was forcibly cleared after " + maxWait + " s timeout for adding another log record." //
                + " Log records were lost." //
                + " It might be caused by a recursive deadlock," //
                + " you can increase the capacity or the timeout to avoid this.", false));
    }


    /**
     * This prevents losing any records, but may end up in deadlock if the capacity is reached.
     */
    private void addWithUnlimitedWaiting(final GlassFishLogRecord record) {
        if (this.pendingRecords.offer(record)) {
            return;
        }
        try {
            Thread.yield();
            this.pendingRecords.put(record);
        } catch (final InterruptedException e) {
            // do nothing
        }
    }


    /**
     * Returns simple name of this class and size/capacity
     *
     * @return ie.: LogRecordBuffer@2b488078[5/10000]
     */
    @Override
    public String toString() {
        return super.toString() + "[" + getSize() + "/" + getCapacity() + "]";
    }
}
