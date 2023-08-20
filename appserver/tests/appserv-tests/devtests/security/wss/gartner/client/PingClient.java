/*
 * Copyright (c) 2005, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.security.wss.gartner.client;

import jakarta.xml.ws.WebServiceRef;
import jakarta.xml.ws.BindingProvider;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class PingClient {
    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");

    @WebServiceRef
    private static PingEjbService ejbService;

    @WebServiceRef
    private static PingServletService servletService;

    public static void main(String args[]) {
        String host = args[0];
        String port = args[1];
        stat.addDescription("security-wss-ping");

        try {
            PingEjb pingEjbPort = ejbService.getPingEjbPort();

            ((BindingProvider) pingEjbPort).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                    "http://" + host + ":" + port + "/PingEjbService/PingEjb?WSDL");

            String result = pingEjbPort.ping("Hello");
            if (result == null || result.indexOf("Sun") == -1) {
                System.out.println("Unexpected ping result: " + result);
                stat.addStatus("JWSS Ejb Ping", stat.FAIL);
            }
            stat.addStatus("JWSS Ejb Ping", stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus("JWSS Ejb Ping", stat.FAIL);
        }

        try {
            PingServlet pingServletPort = servletService.getPingServletPort();

            ((BindingProvider) pingServletPort).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                    "http://" + host + ":" + port + "/security-wss-gartner-web/PingServletService?WSDL");

            String result = pingServletPort.ping("Hello");
            if (result == null || result.indexOf("Sun") == -1) {
                System.out.println("Unexpected ping result: " + result);
                stat.addStatus("JWSS Servlet Ping", stat.FAIL);
            }
            stat.addStatus("JWSS Servlet Ping", stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus("JWSS Servlet Ping", stat.FAIL);
        }
        
        stat.printSummary("security-wss-ping");
    }
}
