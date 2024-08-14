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
import org.glassfish.j2ee.statistics.BoundedRangeStatistic;
import org.glassfish.j2ee.statistics.CountStatistic;
import org.glassfish.j2ee.statistics.Stats;

/**
 * Stats interface for the monitorable attributes of the
 * ORBConnectionManager
 * This combines the statistics that were exposed in 7.0
 * with the new ones.
 */
public interface OrbConnectionManagerStats extends Stats {

    /**
     * Returns the total number of connections to the ORB as an instance of BoundedRangeStatistic.
     *
     * @return an instance of {@link BoundedRangeStatistic}
     */
    BoundedRangeStatistic getTotalConnections();


    /**
     * Returns the total number of idle connections to the ORB as an instance of CountStatistic.
     *
     * @return an instance of {@link CountStatistic}
     */
    CountStatistic getConnectionsIdle();


    /**
     * Returns the total number of in-use connections to the ORB as an instance of CountStatistic.
     *
     * @return an instance of {@link CountStatistic}
     */
    CountStatistic getConnectionsInUse();
}
