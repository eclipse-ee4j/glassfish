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

package org.glassfish.admin.amxtest.ext.wsmgmt;

import com.sun.appserv.management.ext.wsmgmt.MessageTrace;
import com.sun.appserv.management.j2ee.J2EETypes;
import com.sun.appserv.management.j2ee.WebServiceEndpoint;
import org.glassfish.admin.amxtest.AMXTestBase;
import org.glassfish.admin.amxtest.Capabilities;

import java.io.IOException;
import java.util.Set;

/**
 */
public final class WebServiceRuntimeTest
        extends AMXTestBase {

    public WebServiceRuntimeTest()
            throws IOException {
    }

    public static Capabilities
    getCapabilities() {
        return getOfflineCapableCapabilities(false);
    }

    public void testRuntimeMBeans() {
        assert (getDomainRoot().getWebServiceMgr() != null);

        final Set<WebServiceEndpoint> s =
                getDomainRoot().getQueryMgr().queryJ2EETypeSet(J2EETypes.WEB_SERVICE_ENDPOINT);

        for (final WebServiceEndpoint wsp : s) {
            wsp.resetStats();

            long ts = wsp.getLastResetTime();
            System.out.println("Web Service endpoint name is " + wsp.getName());
            System.out.println("Last reset time is " + ts);

            try {
                Thread.currentThread().sleep(1);
            } catch (Exception e) {
            }

            wsp.resetStats();
            long ts2 = wsp.getLastResetTime();
            System.out.println("Last reset time is " + ts2);
            if ((ts == 0) && (ts2 == 0)) {
                assert (true);
            } else {
                assert (ts != ts2);
            }
        }
    }

    public void testMessageTrace() {
        assert (getDomainRoot().getWebServiceMgr() != null);

        final Set<WebServiceEndpoint> s =
                getDomainRoot().getQueryMgr().queryJ2EETypeSet(J2EETypes.WEB_SERVICE_ENDPOINT);

        for (final WebServiceEndpoint wsp : s) {
            final MessageTrace[] msgs = wsp.getMessagesInHistory();
            if (msgs == null) {
                System.out.println(" No messages found");
                continue;
            }

            System.out.println(" Collected messages  " + msgs.length);
            for (int idx = 0; idx < msgs.length; idx++) {
                final MessageTrace msg = msgs[idx];

                System.out.println(" message id  " + msg.getMessageID());
                System.out.println(" application id " + msg.getApplicationID());
                System.out.println(" endpoint name " + msg.getEndpointName());
                System.out.println(" response size " + msg.getResponseSize());
                System.out.println(" request size " + msg.getRequestSize());
                System.out.println(" transport type is " +
                        msg.getTransportType());
                System.out.println(" request headers are " +
                        msg.getHTTPRequestHeaders());
                System.out.println(" response headers are " +
                        msg.getHTTPResponseHeaders());
                System.out.println(" fault code is  " + msg.getFaultCode());
                System.out.println(" fault string is  " + msg.getFaultString());
                System.out.println(" fault actor is " + msg.getFaultActor());
                System.out.println(" client host is  " + msg.getClientHost());
                System.out.println(" principal name is " +
                        msg.getPrincipalName());
                System.out.println(" request content is " +
                        msg.getRequestContent());
                System.out.println(" response content is " +
                        msg.getResponseContent());
                System.out.println(" call flow enabled " +
                        msg.isCallFlowEnabled());
            }
        }
    }
}


