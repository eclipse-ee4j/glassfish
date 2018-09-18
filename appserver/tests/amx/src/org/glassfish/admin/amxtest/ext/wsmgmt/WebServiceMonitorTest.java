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
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/ext/wsmgmt/WebServiceMonitorTest.java,v 1.7 2007/05/05 05:24:04 tcfujii Exp $
* $Revision: 1.7 $
* $Date: 2007/05/05 05:24:04 $
*/
package org.glassfish.admin.amxtest.ext.wsmgmt;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.j2ee.statistics.NumberStatistic;
import com.sun.appserv.management.monitor.WebServiceEndpointMonitor;
import com.sun.appserv.management.monitor.statistics.WebServiceEndpointAggregateStats;
import org.glassfish.admin.amxtest.AMXTestBase;
import org.glassfish.admin.amxtest.Capabilities;

import org.glassfish.j2ee.statistics.CountStatistic;
import java.io.IOException;
import java.util.Set;

/**
 */
public final class WebServiceMonitorTest
        extends AMXTestBase {

    public WebServiceMonitorTest()
            throws IOException {
    }

    public static Capabilities
    getCapabilities() {
        return getOfflineCapableCapabilities(false);
    }

    public void testMonitorMBeans() {
        assert (getDomainRoot().getWebServiceMgr() != null);

        final Set<WebServiceEndpointMonitor> ms =
                getDomainRoot().getQueryMgr().queryJ2EETypeSet(XTypes.WEBSERVICE_ENDPOINT_MONITOR);

        for (final WebServiceEndpointMonitor m : ms) {
            System.out.println("\n \n Name of web service is " + m.getName());

            final WebServiceEndpointAggregateStats s =
                    m.getWebServiceEndpointAggregateStats();

            // verify that we can get each Statistic;
            // there was a problem at one time


            final CountStatistic r1 = s.getTotalFaults();
            assert (r1 != null);
            System.out.println(" total num fault is " + r1.getCount());

            final CountStatistic r2 = s.getTotalNumSuccess();
            assert (r2 != null);
            System.out.println(" total num success is " + r2.getCount());

            final CountStatistic r3 = s.getAverageResponseTime();
            assert (r3 != null);
            System.out.println("avg resp is " + r3.getCount());

            final NumberStatistic c1 = s.getThroughput();
            assert (c1 != null);
            System.out.println(" through put is " + c1.getCurrent());

            final CountStatistic c2 = s.getTotalAuthFailures();
            assert (c2 != null);
            System.out.println(" total num auth success is " + c2.getCount());

            final CountStatistic c3 = s.getTotalAuthSuccesses();
            assert (c3 != null);
            System.out.println(" total num auth failure is " + c3.getCount());

        }
    }
}


