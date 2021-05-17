/*
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

package com.sun.enterprise.admin.monitor.stats;

/**
 *
 * @author  nsegura
 */
import com.sun.enterprise.admin.monitor.stats.StringStatistic;
import org.glassfish.j2ee.statistics.CountStatistic;
import org.glassfish.j2ee.statistics.Stats;

/**
 * ConnectionQueue information shows the number of sessions in the queue
 * and the average delay before the connection is accepted
 */
public interface PWCConnectionQueueStats extends Stats {

    /**
     * Gets the ID of the connection queue
     *
     * @return The ID of the connection queue
     */
    public StringStatistic getId();

    /**
     * Gets the total number of connections that have been accepted.
     *
     * @return Total number of connections that have been accepted.
     */
    public CountStatistic getCountTotalConnections();

    /**
     * Gets the number of connections currently in the queue
     *
     * @return Number of connections currently in the queue
     */
    public CountStatistic getCountQueued();

    /**
     * Gets the largest number of connections that were in the queue
     * simultaneously.
     *
     * @return Largest number of connections that were in the queue
     * simultaneously
     */
    public CountStatistic getPeakQueued();

    /**
     * Gets the maximum size of the connection queue
     *
     * @return Maximum size of the connection queue
     */
    public CountStatistic getMaxQueued();

    /**
     * Gets the number of times the queue has been too full to accommodate
     * a connection
     *
     * @return Number of times the queue has been too full to accommodate
     * a connection
     */
    public CountStatistic getCountOverflows();

    /**
     * Gets the total number of connections that have been queued.
     *
     * A given connection may be queued multiple times, so
     * <code>counttotalqueued</code> may be greater than or equal to
     * <code>counttotalconnections</code>.
     *
     * @return Total number of connections that have been queued
     */
    public CountStatistic getCountTotalQueued();

    /**
     * Gets the total number of ticks that connections have spent in the
     * queue.
     *
     * A tick is a system-dependent unit of time.
     *
     * @return Total number of ticks that connections have spent in the
     * queue
     */
    public CountStatistic getTicksTotalQueued();

    /**
     * Gets the average number of connections queued in the last 1 minute
     *
     * @return Average number of connections queued in the last 1 minute
     */
    public CountStatistic getCountQueued1MinuteAverage();

    /**
     * Gets the average number of connections queued in the last 5 minutes
     *
     * @return Average number of connections queued in the last 5 minutes
     */
    public CountStatistic getCountQueued5MinuteAverage();

    /**
     * Gets the average number of connections queued in the last 15 minutes
     *
     * @return Average number of connections queued in the last 15 minutes
     */
    public CountStatistic getCountQueued15MinuteAverage();

}
