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

package com.sun.s1asdev.ejb.webservice.commit.client;

import jakarta.xml.ws.WebServiceRef;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_ID = "ejb-webservices-commit";

    @WebServiceRef(wsdlLocation="http://localhost:8080/CommitBeanService/CommitBean?WSDL")
    private static CommitBeanService service;

    public static void main(String[] args) {
        stat.addDescription(TEST_ID);
        Client client = new Client();
        client.doTest(args);
        stat.printSummary(TEST_ID);
    }

    public void doTest(String[] args) {
        try {
            CommitBean port = service.getCommitBeanPort();

            try {
                // now do another create that should fail at commit
                // time and return an error.
                port.updateCustomer();
                System.out.println("call to updateCustomer() should" +
                                   " have failed.");
                stat.addStatus(TEST_ID, stat.FAIL);
            } catch(FinderException_Exception e) {
                System.out.println("incorrectly received Application exception"
                                   + " instead of system exception");
                stat.addStatus(TEST_ID, stat.FAIL);
            } catch(Throwable e) {
                System.out.println("Successfully received " + e +
                                   "for commit failure");
                stat.addStatus(TEST_ID, stat.PASS);
            }

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus(TEST_ID, stat.FAIL);
        }
    }
}

