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

package org.glassfish.admin.amxtest.monitor;


import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.monitor.ConnectorConnectionPoolMonitor;
import com.sun.appserv.management.monitor.MonitoringStats;
import com.sun.appserv.management.monitor.statistics.ConnectorConnectionPoolStats;


public final class ConnectorConnectionPoolStatsTest
        extends ConnectionPoolStatsTest {
    public ConnectorConnectionPoolStatsTest() {
    }

    public void
    testConnectorConnectionPoolStats() {
        final long start = now();

        final int numMonitors = iterateAllMonitors(XTypes.CONNECTOR_CONNECTION_POOL_MONITOR);

        printElapsed("testConnectorConnectionPoolStats", numMonitors, start);
    }

    public void
    nextMonitor(final MonitoringStats ms) {
        final ConnectorConnectionPoolMonitor m = (ConnectorConnectionPoolMonitor) ms;
        final ConnectorConnectionPoolStats s = m.getConnectorConnectionPoolStats();

        accessAllStatistics(s);
    }

}






