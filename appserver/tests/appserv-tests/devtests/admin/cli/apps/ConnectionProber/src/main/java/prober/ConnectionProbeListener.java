/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package prober;

import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
// Super simple output -- it just writes to stdout -- which will go into the server logfile.
// The point is just to demonstrate how to listen to GlassFish probes

public class ConnectionProbeListener {
    private static final String ACCEPT = "glassfish:kernel:connection-queue:connectionAcceptedEvent";

    // this is what ProberServlet shows at runtime:
    // glassfish:kernel:connection-queue:connectionAcceptedEvent (java.lang.String listenerName,
    // int connection, java.lang.String address)
    @ProbeListener(ACCEPT)
    public void accepted(
            //java.lang.String listenerName, int connection, java.lang.String address
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("connection") int connectionNumber,
            @ProbeParam("address") String address) {

        // you can do anything you like in here.
        System.out.printf("Connection Accepted!  name=%s, connection: %d, address: %s\n",
                listenerName, connectionNumber, address);
    }

    // this is what ProberServlet shows at runtime:
    //glassfish:web:http-service:requestStartEvent (java.lang.String appName,
    // java.lang.String hostName, java.lang.String serverName,
    //int serverPort, java.lang.String contextPath, java.lang.String servletPath)
    private static final String REQUEST = "glassfish:web:http-service:requestStartEvent";
    @ProbeListener(REQUEST)
    public void startEvent(
            @ProbeParam("appName") String appName,
            @ProbeParam("hostName") String hostName,
            @ProbeParam("serverName") String serverName,
            @ProbeParam("serverPort") int serverPort,
            @ProbeParam("contextPath") String contextPath,
            @ProbeParam("servletPath") String servletPath) {

        // you can do anything you like in here.
        System.out.printf("Web Start Event with these params:%s, %s, %s, %d, %s, %s\n",
                appName, hostName, serverName, serverPort, contextPath, servletPath);
    }
}
