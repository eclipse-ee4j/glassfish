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

package com.sun.s1asdev.ejb.allowedmethods.remove.client;

import java.io.*;
import java.util.*;
import jakarta.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import com.sun.s1asdev.ejb.allowedmethods.remove.DriverHome;
import com.sun.s1asdev.ejb.allowedmethods.remove.Driver;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-allowedmethods-remove");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-allowedmethods-remove");
    }

    public Client (String[] args) {
    }

    public void doTest() {
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/Driver");
            DriverHome  home = (DriverHome)PortableRemoteObject.narrow
                (objref, DriverHome.class);
            Driver driver = home.create();
            boolean ok = driver.test();
            System.out.println("Test returned: " + ok);
            stat.addStatus("ejbclient remote test", ((ok)? stat.PASS : stat.FAIL));
        } catch(Exception e) {
            System.out.println("Got exception: " + e.getMessage());
            stat.addStatus("ejbclient remote test(-)" , stat.FAIL);
        }
    }

}

