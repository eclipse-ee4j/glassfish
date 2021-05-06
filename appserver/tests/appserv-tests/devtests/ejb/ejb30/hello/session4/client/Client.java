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

package com.sun.s1asdev.ejb.ejb30.hello.session4.client;

import java.io.*;
import java.util.*;
import javax.naming.InitialContext;
import jakarta.ejb.EJB;
import com.sun.s1asdev.ejb.ejb30.hello.session4.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-hello-session4");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-hello-session4ID");
    }

    public Client (String[] args) {
    }

    @EJB(name="ejb/sful")
    private static Sful sful;

    // NOTE : Do not reference Sful2 within annotations
    // so that we ensure that the first time Sful2 is
    // accessed it is as part of the return value of
    // a business method.  This tests that the dynamic
    // interface generation machinery is working properly
    // for the case where a previously unseen remote 3.0
    // interface is somewhere within the return
    // value of a business method.

    // @EJB
    private static Sless sless;

    public void doTest() {

        try {
            InitialContext ic = new InitialContext();

            sful = (Sful) ic.lookup("ejb_ejb30_hello_session4_Sful#com.sun.s1asdev.ejb.ejb30.hello.session4.Sful");
            System.out.println("invoking stateful");
            String sfulId = "1";
            sful.setId(sfulId);
            sful.hello();
            sful.sameMethod();

            Sful2 sful_2 = sful.getSful2();
            sful_2.sameMethod();
            String sful_2Id = sful_2.getId();

            System.out.println("Expected id " + sfulId);
            System.out.println("Received id " + sful_2Id);
            if( !sful_2Id.equals(sfulId) ) {
                throw new Exception("sful bean id mismatch " +
                                    sfulId + " , " + sful_2Id);
            }


            //            System.out.println("invoking stateful2");
            //            sful2.hello2();

            System.out.println("invoking stateless");

            sless = (Sless) ic.lookup("com.sun.s1asdev.ejb.ejb30.hello.session4.Sless");
            sless.hello();

            System.out.println("test complete");

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }

            return;
    }

}

