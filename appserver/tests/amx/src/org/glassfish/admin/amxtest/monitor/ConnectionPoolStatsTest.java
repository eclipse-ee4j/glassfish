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

/*
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/monitor/ConnectionPoolStatsTest.java,v 1.5 2007/05/05 05:24:05 tcfujii Exp $
* $Revision: 1.5 $
* $Date: 2007/05/05 05:24:05 $
*/
package org.glassfish.admin.amxtest.monitor;

import com.sun.appserv.management.monitor.MonitoringStats;
import com.sun.appserv.management.monitor.statistics.ConnectionPoolStats;

import org.glassfish.j2ee.statistics.CountStatistic;
import org.glassfish.j2ee.statistics.RangeStatistic;
import java.util.Iterator;
import java.util.Set;


abstract class ConnectionPoolStatsTest
        extends AMXMonitorTestBase {
    public ConnectionPoolStatsTest() {
    }


    protected abstract void nextMonitor(final MonitoringStats ms);

    protected int
    iterateAllMonitors(final String j2eeType) {
        final Set monitors = getQueryMgr().queryJ2EETypeSet(j2eeType);
        final Iterator iter = monitors.iterator();

        int numMonitors = 0;
        while (iter.hasNext()) {
            final MonitoringStats ms = (MonitoringStats) iter.next();
            ++numMonitors;
            nextMonitor(ms);
        }

        return numMonitors;
    }

    /**
     Verify that every Statistic can be successfully accessed.
     */
    protected void
    accessAllStatistics(final ConnectionPoolStats s) {
        final RangeStatistic r1 = s.getNumConnUsed();
        assert (r1 != null);

        final RangeStatistic r2 = s.getNumConnFree();
        assert (r2 != null);

        final RangeStatistic r3 = s.getConnRequestWaitTime();
        assert (r3 != null);

        final CountStatistic c1 = s.getNumConnFailedValidation();
        assert (c1 != null);

        final CountStatistic c2 = s.getNumConnTimedOut();
        assert (c2 != null);

        final CountStatistic c3 = s.getWaitQueueLength();
        assert (c3 != null);

        final CountStatistic c4 = s.getNumConnCreated();
        assert (c4 != null);

        final CountStatistic c5 = s.getNumConnDestroyed();
        assert (c5 != null);

        //final CountStatistic c6    = s.getNumConnOpened();
        // assert( c6 != null );

        //final CountStatistic c7    = s.getNumConnClosed();
        // assert( c7 != null );

        final CountStatistic c8 = s.getAverageConnWaitTime();
        assert (c8 != null);
    }

}






