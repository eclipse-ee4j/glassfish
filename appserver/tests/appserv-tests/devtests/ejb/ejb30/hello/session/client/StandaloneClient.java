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

package com.sun.s1asdev.ejb.ejb30.hello.session.client;

import java.io.*;
import java.util.*;
import com.sun.s1asdev.ejb.ejb30.hello.session.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import javax.naming.*;

public class StandaloneClient {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-hello-sessionstandalone");
        StandaloneClient client = new StandaloneClient(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-hello-sessionstandaloneID");
    }

    public StandaloneClient (String[] args) {
    }

    public void doTest() {

        try {

            InitialContext ic = new InitialContext();

            Sful sful1 = (Sful) ic.lookup
                ("com.sun.s1asdev.ejb.ejb30.hello.session.Sful");

            Sful sful2 = (Sful) ic.lookup
                ("com.sun.s1asdev.ejb.ejb30.hello.session.Sful");

            Sful sful3 = (Sful) ic.lookup
                ("com.sun.s1asdev.ejb.ejb30.hello.session.Sful#com.sun.s1asdev.ejb.ejb30.hello.session.Sful");

            Sless sless1 = (Sless) ic.lookup
                ("com.sun.s1asdev.ejb.ejb30.hello.session.Sless");

            Sless sless2 = (Sless) ic.lookup
                ("com.sun.s1asdev.ejb.ejb30.hello.session.Sless#com.sun.s1asdev.ejb.ejb30.hello.session.Sless");


            System.out.println("invoking stateful");
            sful1.hello();
            sful2.hello();
            sful3.hello();

            if( sful1.equals(sful2) || sful1.equals(sful3) ||
                sful2.equals(sful3) ) {
                throw new Exception("invalid equality checks on different " +
                                    "sful session beans");
            }

            System.out.println("invoking stateless");
            sless1.hello();
            sless2.hello();

            if( !sless1.equals(sless2) ) {
                throw new Exception("invalid equality checks on same " +
                                    "sless session beans");
            }

            System.out.println("test complete");

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }

            return;
    }

}

