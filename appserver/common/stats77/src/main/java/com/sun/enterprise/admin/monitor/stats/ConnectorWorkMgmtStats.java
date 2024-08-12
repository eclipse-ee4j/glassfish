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

import org.glassfish.j2ee.statistics.CountStatistic;
import org.glassfish.j2ee.statistics.RangeStatistic;
import org.glassfish.j2ee.statistics.Stats;

/**
 * A Stats interface to represent the statistical data about
 * Work Management in the Connector Module
 *
 * @author  Murali Vempaty
 * @since   SJSAS8.1
 */
public interface ConnectorWorkMgmtStats extends Stats {

    /**
     * returns the current, low & high counts of the work objects executed for
     * a connector module since the last reset. This is an aggregate of all the
     * doWork, doSchedule, and doStart work objects initiated by the connector
     * module
     * @return RangeStatistic
     */
    RangeStatistic getActiveWorkCount();

    /**
     * indicates the current, high & low of the number of work objects waiting
     * in the work queue before executing, since the last reset
     * @return RangeStatistic
     */
    RangeStatistic getWaitQueueLength();

    /**
     * indicates the longest and shorted wait of a work object in the work queue
     * before it gets executed, since the last reset
     * @return RangeStatistic
     */
    RangeStatistic getWorkRequestWaitTime();

    /**
     * indicates the number of work objects submitted by a connector module
     *  for execution, since the last reset
     * @return CountStatistic
     */
    CountStatistic getSubmittedWorkCount();

    /**
     * indicates the number of work objects rejected by the Application Server
     * per connector module, since the last reset.
     * @return CountStatistic
     */
    CountStatistic getRejectedWorkCount();

    /**
     * indicates the number of work objects that were completed by the
     * Application Server per connector module, since the last reset.
     * @return CountStatistic
     */
    CountStatistic getCompletedWorkCount();

}
