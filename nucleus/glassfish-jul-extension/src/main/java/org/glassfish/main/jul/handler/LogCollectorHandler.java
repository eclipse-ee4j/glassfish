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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.glassfish.main.jul.record.MessageResolver;


/**
 * This special {@link Handler} can be used for testing purposes.
 * It collects log records passing through the given {@link Logger} instance.
 *
 * @author David Matejcek
 */
public class LogCollectorHandler extends Handler {

    private static final MessageResolver RESOLVER = new MessageResolver();
    private final LogRecordBuffer buffer;
    private final Logger logger;

    /**
     * Creates a {@link LogRecord} collector handler with the capacity of 100 records and maximal
     * wait time 5 seconds.
     *
     * @param loggerToFollow this handler will be added to this logger.
     * @see LogRecordBuffer
     */
    public LogCollectorHandler(final Logger loggerToFollow) {
        this(loggerToFollow, 100, 5);
    }

    /**
     * @param loggerToFollow this handler will be added to this logger.
     * @param capacity capacity of the buffer.
     * @param maxWait maximal time in seconds to wait for the free capacity. If &lt; 1, can wait
     *            forever.
     * @see LogRecordBuffer
     */
    public LogCollectorHandler(final Logger loggerToFollow, int capacity, int maxWait) {
        buffer = new LogRecordBuffer(capacity, maxWait);
        logger = loggerToFollow;
        logger.addHandler(this);
    }

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            this.buffer.add(RESOLVER.resolve(record));
        }
    }


    @Override
    public void flush() {
        // nothing
    }


    /**
     * Unattaches the handler from the logger and drops all collected log records.
     */
    @Override
    public void close() throws SecurityException {
        this.logger.removeHandler(this);
        reset();
    }


    /**
     * Removes the first record in the buffer and returns it.
     *
     * @return the first {@link GlassFishLogRecord} in the buffer or null if the buffer is empty.
     */
    public GlassFishLogRecord pop() {
        return this.buffer.poll();
    }


    /**
     * Creates a list of all records and resets the buffer.
     *
     * @return all collected records
     */
    public List<GlassFishLogRecord> getAll() {
        final List<GlassFishLogRecord> list = new ArrayList<>(this.buffer.getSize());
        while (!this.buffer.isEmpty()) {
            list.add(this.buffer.poll());
        }
        return list;
    }


    /**
     * Creates a list of all records and resets the buffer.
     * @param mappingFunction
     * @param <R> expected item type
     *
     * @return all collected records
     */
    public <R> List<R> getAll(Function<LogRecord, R> mappingFunction) {
        return getAll().stream().map(mappingFunction).collect(Collectors.toList());
    }


    /**
     * Drops all collected records.
     */
    public void reset() {
        while (!this.buffer.isEmpty()) {
            this.buffer.poll();
        }
    }
}
