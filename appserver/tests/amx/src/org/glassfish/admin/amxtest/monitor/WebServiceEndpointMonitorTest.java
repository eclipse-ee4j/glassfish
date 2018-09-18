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
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/monitor/WebServiceEndpointMonitorTest.java,v 1.6 2007/05/05 05:24:05 tcfujii Exp $
* $Revision: 1.6 $
* $Date: 2007/05/05 05:24:05 $
*/
package org.glassfish.admin.amxtest.monitor;

import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.j2ee.statistics.NumberStatistic;
import com.sun.appserv.management.monitor.WebServiceEndpointMonitor;
import com.sun.appserv.management.monitor.statistics.WebServiceEndpointAggregateStats;

import org.glassfish.j2ee.statistics.CountStatistic;
import java.util.Iterator;
import java.util.Set;


public final class WebServiceEndpointMonitorTest
        extends AMXMonitorTestBase {
    public WebServiceEndpointMonitorTest() {
    }


    public void
    testStats() {
        final QueryMgr q = getQueryMgr();

        final Set wsMonitors = q.queryJ2EETypeSet(
                XTypes.WEBSERVICE_ENDPOINT_MONITOR);

        if (wsMonitors.size() == 0) {
            warning("WebServiceEndpointMonitorTest: no MBeans found to test.");
        } else {
            Iterator itr = wsMonitors.iterator();
            while (itr.hasNext()) {
                WebServiceEndpointMonitor m = (WebServiceEndpointMonitor)
                        itr.next();

                final WebServiceEndpointAggregateStats s =
                        m.getWebServiceEndpointAggregateStats();

                // verify that we can get each Statistic;
                // there was a problem at one time

                final CountStatistic r1 = s.getTotalFaults();
                assert (r1 != null);

                final CountStatistic r2 = s.getTotalNumSuccess();
                assert (r2 != null);

                //final AverageRangeStatistic r3 = s.getResponseTime();
                //assert( r3 != null );

                final NumberStatistic c1 = s.getThroughput();
                assert (c1 != null);

                final CountStatistic c2 = s.getTotalAuthFailures();
                assert (c2 != null);

                final CountStatistic c3 = s.getTotalAuthSuccesses();
                assert (c3 != null);

            }
        }
    }

}






