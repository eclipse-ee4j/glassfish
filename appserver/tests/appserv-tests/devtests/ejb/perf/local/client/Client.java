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

package com.sun.s1asdev.ejb.perf.local.client;

import java.io.*;
import java.util.*;
import jakarta.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import com.sun.s1asdev.ejb.perf.local.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-perf-local");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-perf-localID");
    }

    public Client (String[] args) {
    }

    public void doTest() {

        try {

            Context ic = new InitialContext();

            // create EJB using factory from container
            java.lang.Object objref =
                ic.lookup("java:comp/env/ejb/PerformanceApp");

            System.out.println("Looked up home!!");

            HelloHome  home = (HelloHome)
                PortableRemoteObject.narrow(objref, HelloHome.class);
            System.out.println("Narrowed home!!");

            Hello hr = home.create();
            System.out.println("Got the EJB!!");

            // invoke method on the EJB
            doPerfTest(hr, true);
            doPerfTest(hr, false);

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }

            return;
    }

    private void doPerfTest(Hello hr, boolean local)
        throws Exception
    {
        System.out.println("\nStateful Session results (microsec): \twith tx \tno tx:");
        hr.warmup(Common.STATEFUL, local);
        runTests(Common.STATEFUL, hr, local);

        System.out.println("\nStateless Session results (microsec): \twith tx \tno tx:");
        hr.warmup(Common.STATEFUL, local);
        runTests(Common.STATELESS, hr, local);

        System.out.println("\nBMP Entity results (microsec): \t\twith tx \tno tx:");
        hr.warmup(Common.BMP, local);
        runTests(Common.BMP, hr, local);
    }

    private void runTests(int type, Hello hr, boolean local)
        throws Exception
    {
        System.out.println("Testing " + (local ? " Local " :
                                         " Collocated Remote ") +
                           "performance...");

        if( local ) {
            System.out.println("notSupported : \t\t\t\t"
                               + hr.notSupported(type, true) + "\t\t" +
                               + hr.notSupported(type, false) );
            System.out.println("supports : \t\t\t\t"
                               + hr.supports(type, true) + "\t\t" +
                               + hr.supports(type, false) );
            System.out.println("required : \t\t\t\t"
                               + hr.required(type, true) + "\t\t" +
                               + hr.required(type, false) );
            System.out.println("requiresNew : \t\t\t\t"
                               + hr.requiresNew(type, true) + "\t\t" +
                               + hr.requiresNew(type, false) );
            System.out.println("mandatory : \t\t\t\t"
                               + hr.mandatory(type, true));
            System.out.println("never : \t\t\t\t\t\t"
                               + hr.never(type, false) );
        } else {
            System.out.println("notSupported : \t\t\t\t"
                               + hr.notSupportedRemote(type, true) + "\t\t" +
                               + hr.notSupportedRemote(type, false) );
            System.out.println("supports : \t\t\t\t"
                               + hr.supportsRemote(type, true) + "\t\t" +
                               + hr.supportsRemote(type, false) );
            System.out.println("required : \t\t\t\t"
                               + hr.requiredRemote(type, true) + "\t\t" +
                               + hr.requiredRemote(type, false) );
            System.out.println("requiresNew : \t\t\t\t"
                               + hr.requiresNewRemote(type, true) + "\t\t" +
                               + hr.requiresNewRemote(type, false) );
            System.out.println("mandatory : \t\t\t\t"
                               + hr.mandatoryRemote(type, true));
            System.out.println("never : \t\t\t\t\t\t"
                               + hr.neverRemote(type, false) );
        }
    }
}

