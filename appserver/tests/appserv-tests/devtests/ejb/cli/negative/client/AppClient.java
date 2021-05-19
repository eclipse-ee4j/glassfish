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

package com.acme;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.util.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 * A simple java client will:
 * <ul>
 * <li>Locates the remote interface of the enterprise bean
 * <li>Invokes business methods
 * </ul>
 */
public class AppClient {

    private SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        AppClient client = new AppClient();

        // run the tests
        client.runTestClient();
    }

    public void runTestClient() {
        try{
            stat.addDescription("Testing ejb-cli");
            test01();
            stat.printSummary("test end");
        } catch (Exception ex) {
            System.out.println("Exception in runTestClient: " + ex.toString());
            ex.printStackTrace();
        }
    }


    private void test01() {
        try {
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/MyBean");
            MyBeanRemoteIntf mybean =
                (MyBeanRemoteIntf) PortableRemoteObject.narrow(objref,
                                                          MyBeanRemoteIntf.class);
            System.out.println("invocation result: "+ mybean.getCount(1));
            stat.addStatus("ejb-cli", stat.FAIL);
        } catch (Exception ex) {
            stat.addStatus("ejb-cli", stat.PASS);
            System.err.println("caught expected exception");
            ex.printStackTrace();
        }
    }
}
