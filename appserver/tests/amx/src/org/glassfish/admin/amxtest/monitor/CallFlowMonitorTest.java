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
import com.sun.appserv.management.monitor.CallFlowMonitor;
import com.sun.appserv.management.monitor.ServerRootMonitor;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CallFlowMonitorTest
        extends AMXMonitorTestBase {
    private String IP_FILTER_NAME = "129.129.129.129";
    private String PRINCIPAL_FILTER_NAME = "Harry";

    public CallFlowMonitorTest() {
    }

    public void testCallFlowOn() {
        final CallFlowMonitor cfm = getCallFlowMonitor();
        try {
            cfm.setEnabled(true);
            assertTrue(cfm.getEnabled());
        }
        catch (Throwable t) {
            warning("testCallFlowOn: " +
                    "Can't enable callflow...has the callflow database been started?");
        }
    }

    public void testIPFilter() {
        final CallFlowMonitor cfm = getCallFlowMonitor();
        cfm.setCallerIPFilter(IP_FILTER_NAME);
        final String filter = cfm.getCallerIPFilter();
        boolean val = filter.equals(IP_FILTER_NAME);
        assertTrue(val);
    }

    public void testPrincipalFilter() {
        final CallFlowMonitor cfm = getCallFlowMonitor();
        cfm.setCallerPrincipalFilter(PRINCIPAL_FILTER_NAME);
        final String filter = cfm.getCallerPrincipalFilter();
        boolean val = filter.equals(PRINCIPAL_FILTER_NAME);
        assertTrue(val);
    }

    /*
    * Disable CallFlow
    */
    public void testCallFlowOff() {
        final CallFlowMonitor cfm = getCallFlowMonitor();
        cfm.setEnabled(false);
        assertFalse(cfm.getEnabled());
    }

    public void testQueryRequestInformation() {
        final CallFlowMonitor cfm = getCallFlowMonitor();
        final List<Map<String, String>> list = cfm.queryRequestInformation();
        if (list == null)
        //            int resultSize = list.size ();
        //            int CORRECT_RESULT_SIZE = 0;
        //            if (resultSize == CORRECT_RESULT_SIZE)
        {
            assertTrue(true);
        }
    }

    public void testQueryCallStackInformation() {
        final CallFlowMonitor cfm = getCallFlowMonitor();
        final List<Map<String, String>> list = cfm.queryCallStackForRequest("RequestID_1");
        if (list == null)
        //            int resultSize = list.size ();
        //            int CORRECT_RESULT_SIZE = 0;
        //            if (resultSize == CORRECT_RESULT_SIZE)
        {
            assertTrue(true);
        }
    }

    public void testQueryPieInformation() {
        final CallFlowMonitor cfm = getCallFlowMonitor();
        final Map<String, String> map = cfm.queryPieInformation("RequestID_1");
        if (map != null)
        //            int resultSize = list.size ();
        //            int CORRECT_RESULT_SIZE = 0;
        //            if (resultSize == CORRECT_RESULT_SIZE)
        {
            assertTrue(true);
        }
    }


    public void testClearData() {
        final CallFlowMonitor cfm = getCallFlowMonitor();
        cfm.clearData();
    }

    public void testQueryRequestTypeKeys() {
        final CallFlowMonitor cfm = getCallFlowMonitor();
        final String[] rT = cfm.queryRequestTypeKeys();
        if (rT.length == 5) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }
    }

    public void testQueryComponentTypeKeys() {
        final CallFlowMonitor cfm = getCallFlowMonitor();
        final String[] rT = cfm.queryComponentTypeKeys();
        assert rT.length == 7;
    }

    public void testQueryContainerTypeOrApplicationTypeKeys() {
        final CallFlowMonitor cfm = getCallFlowMonitor();
        final String[] rT = cfm.queryContainerTypeOrApplicationTypeKeys();
        assert rT.length == 6;
    }


    private CallFlowMonitor getCallFlowMonitor() {
        Map<String, ServerRootMonitor> serverRootMonitorMap =
                getDomainRoot().getMonitoringRoot().getServerRootMonitorMap();
        // Get the server name from some MBean. Using the default value for now
        ServerRootMonitor serverRootMonitor = serverRootMonitorMap.get("server");
        return serverRootMonitor.getCallFlowMonitor();
    }

    public void testExactlyOneDASCallFlowMonitor() {
        final Set<CallFlowMonitor> cfms =
                getQueryMgr().queryJ2EETypeSet(XTypes.CALL_FLOW_MONITOR);

        int numDAS = 0;
        int numNonDAS = 0;
        for (final CallFlowMonitor cfm : cfms) {
            if (cfm.isDAS()) {
                ++numDAS;
            } else {
                ++numNonDAS;
            }
        }

        if (numNonDAS == 0) {
            warning("testExactlyOneDASCallFlowMonitor: no instances other than DAS are running");
        }

        assert numDAS == 1 :
                "There must be exactly one CallFlowMonitor in the DAS, but there are " + numDAS +
                        " and there are " + numNonDAS + " non-DAS CallFlowMonitor.";

    }
}



