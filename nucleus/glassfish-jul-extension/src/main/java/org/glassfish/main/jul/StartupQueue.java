/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.LogRecord;
import java.util.stream.Stream;

/**
 * This queue collects all {@link LogRecord} instances until the {@link GlassFishLogManager} is fully
 * configured and flushes this queue, which means that all collected records will be processed
 * with respect to the configuration of the logging system.
 * <p>
 * This is possible because {@link Logger} instances are respected in reconfigurations of
 * the logging system, so we can bind them and log records for a while.
 *
 * @author David Matejcek
 */
final class StartupQueue {

    private static final StartupQueue INSTANCE = new StartupQueue();
    private final ConcurrentLinkedQueue<DeferredRecord> queue = new ConcurrentLinkedQueue<>();

    private StartupQueue() {
        // hidden
    }

    /**
     * @return a singleton instance.
     */
    public static StartupQueue getInstance() {
        return INSTANCE;
    }

    /**
     * Adds the record to the queue.
     *
     * @param logger - logger used to log the record.
     * @param record
     */
    public void add(final GlassFishLogger logger, final LogRecord record) {
        queue.add(new DeferredRecord(logger, record));
    }

    /**
     * @return a sorted {@link Stream} of {@link DeferredRecord} instances
     */
    public Stream<DeferredRecord> toStream() {
        return queue.stream().sorted();
    }


    /**
     * Clears content of the queue.
     */
    public void reset() {
        this.queue.clear();
    }


    /**
     * @return actual count of log records.
     */
    public int getSize() {
        return queue.size();
    }


    /**
     * This class is used to bind the {@link LogRecord} and the {@link GlassFishLogger}.
     * Instances are comparable by their sequence numbers.
     */
    static final class DeferredRecord implements Comparable<DeferredRecord> {
        private final GlassFishLogger logger;
        private final LogRecord record;

        DeferredRecord(final GlassFishLogger logger, final LogRecord record) {
            this.logger = logger;
            this.record = record;
        }


        /**
         * @return logger used to log the record
         */
        public GlassFishLogger getLogger() {
            return logger;
        }


        /**
         * @return record containing informations to log.
         */
        public LogRecord getRecord() {
            return record;
        }


        @Override
        public int compareTo(final DeferredRecord another) {
            if (this.record.getSequenceNumber() < another.getRecord().getSequenceNumber()) {
                return -1;
            } else if (this.record.getSequenceNumber() > another.getRecord().getSequenceNumber()) {
                return 1;
            }
            return 0;
        }


        /** Useful for debugging */
        @Override
        public String toString() {
            return super.toString() + "[seq=" + this.record.getSequenceNumber() + ", level=" + this.record.getLevel()
                + ", message=" + this.record.getMessage() + "]";
        }
    }
}
