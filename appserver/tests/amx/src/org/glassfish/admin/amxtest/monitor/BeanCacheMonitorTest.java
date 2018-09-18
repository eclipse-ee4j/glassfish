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
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/monitor/BeanCacheMonitorTest.java,v 1.5 2007/05/05 05:24:05 tcfujii Exp $
* $Revision: 1.5 $
* $Date: 2007/05/05 05:24:05 $
*/
package org.glassfish.admin.amxtest.monitor;

import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.monitor.BeanCacheMonitor;
import com.sun.appserv.management.monitor.statistics.EJBCacheStats;

import org.glassfish.j2ee.statistics.BoundedRangeStatistic;
import org.glassfish.j2ee.statistics.CountStatistic;
import java.util.Iterator;
import java.util.Set;


public final class BeanCacheMonitorTest
        extends AMXMonitorTestBase {
    public BeanCacheMonitorTest() {
    }


    public void
    testStats() {
        final QueryMgr q = getQueryMgr();

        final Set beanCacheMonitors = q.queryJ2EETypeSet(XTypes.BEAN_CACHE_MONITOR);

        if (beanCacheMonitors.size() == 0) {
            warning("BeanCacheMonitorTest: no MBeans found to test.");
        } else {
            final Iterator iter = beanCacheMonitors.iterator();

            while (iter.hasNext()) {
                final BeanCacheMonitor m = (BeanCacheMonitor) iter.next();
                final EJBCacheStats s = m.getEJBCacheStats();

                // verify that we can get each Statistic; there was a problem at one time
                final BoundedRangeStatistic b1 = s.getCacheMisses();
                final BoundedRangeStatistic b2 = s.getCacheHits();
                final BoundedRangeStatistic b3 = s.getBeansInCache();

                // these were failing
                final CountStatistic c4 = s.getPassivationSuccesses();
                final CountStatistic c3 = s.getExpiredSessionsRemoved();
                final CountStatistic c2 = s.getPassivationErrors();
                final CountStatistic c1 = s.getPassivations();

            }
        }
    }

}






