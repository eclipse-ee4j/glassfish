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

package gf_dd.endpoint_address_uri.client;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import jakarta.xml.ws.WebServiceRef;

/**
 * @author Rama Pulavarthi
 */
public class Client {

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");
    @WebServiceRef
    static AddNumbersImplService service;

    public static void main(String[] args) {
        stat.addDescription("gf_dd.endpoint_address_uri");
        Client client = new Client();
        client.runTest();
        stat.printSummary("gf_dd.endpoint_address_uri");
    }

    public void runTest() {
        try {
            AddNumbersImpl port = service.getAddNumbersImplPort();
            int ret = port.addNumbers(4321, 1234);
            if (ret != (4321 + 1234)) {
                System.out.println("Unexpected add result " + ret);
                stat.addStatus("gf_dd.endpoint_address_uri", stat.FAIL);
                return;
            }
            System.out.println(ret);
            stat.addStatus("gf_dd.endpoint_address_uri", stat.PASS);
        } catch (Exception e) {
            e.printStackTrace();
            stat.addStatus("gf_dd.endpoint_address_uri", stat.FAIL);
        }
    }
}
