/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.s1asdev.jdbc.connsharing.nonxa.client;

import com.sun.s1asdev.jdbc.connsharing.nonxa.ejb.SimpleSessionHome;
import com.sun.s1asdev.jdbc.connsharing.nonxa.ejb.SimpleSession;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import javax.naming.InitialContext;

public class Client {

    public static void main(String[] args)
            throws Exception {

        SimpleReporterAdapter stat = new SimpleReporterAdapter();
        String testSuite = "ConnSharing ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleSessionHome");
        SimpleSessionHome simpleSessionHome = (SimpleSessionHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleSessionHome.class);

        stat.addDescription("Connection Sharing tests ");
        SimpleSession simpleSession = simpleSessionHome.create();
        if (simpleSession.test1() && simpleSession.query()) {
            stat.addStatus(testSuite + " test1 : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + " test1 : ", stat.FAIL);
        }

        if (simpleSession.test2() && simpleSession.query()) {
            stat.addStatus(testSuite + " test2 : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + " test2 : ", stat.FAIL);
        }

        if (simpleSession.test3() && simpleSession.query2()) {
            stat.addStatus(testSuite + " test3 : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + " test3 : ", stat.FAIL);
        }

        if (simpleSession.test4() && simpleSession.query2()) {
            stat.addStatus(testSuite + " test4 : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + " test4 : ", stat.FAIL);
        }

        if (simpleSession.test5()) {
            stat.addStatus(testSuite + " test5 : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + " test5 : ", stat.FAIL);
        }

        if (simpleSession.test6()) {
            stat.addStatus(testSuite + " test6 : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + " test6 : ", stat.FAIL);
        }

        if (simpleSession.test7()) {
            stat.addStatus(testSuite + " test7 : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + " test7 : ", stat.FAIL);
        }

        if (simpleSession.test8()) {
            stat.addStatus(testSuite + " test8 : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + " test8 : ", stat.FAIL);
        }

        stat.printSummary();
    }
}
