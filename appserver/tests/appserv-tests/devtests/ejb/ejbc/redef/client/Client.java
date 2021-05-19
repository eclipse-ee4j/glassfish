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

package com.sun.s1asdev.ejb.ejbc.redef.client;

import java.io.*;
import java.util.*;
import jakarta.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import com.sun.s1asdev.ejb.ejbc.redef.FooHome;
import com.sun.s1asdev.ejb.ejbc.redef.Foo;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejbc-redef");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejbc-redefID");
    }

    public Client (String[] args) {
    }

    public void doTest() {

        try {
            Context ic = new InitialContext();

            System.out.println("Looking up ejb ref ");
            // create EJB using factory from container
            Object objref = ic.lookup("java:comp/env/ejb/foo");
            System.out.println("objref = " + objref);
            System.err.println("Looked up home!!");

            FooHome  home = (FooHome)PortableRemoteObject.narrow
                (objref, FooHome.class);

            System.err.println("Narrowed home!!");


            Foo f = home.create();
            System.err.println("Got the EJB!!");

            // invoke method on the EJB
            System.out.println("invoking ejb");
            f.callHello();

            f.remove();

            System.out.println("successfully invoked ejb");
            stat.addStatus("ejbclient main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("ejbclient main" , stat.FAIL);
        }

            return;
    }

}

