/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.s1asdev.hk2.simple.client;

import java.io.*;
import java.util.*;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import com.oracle.hk2devtest.isolation1.Isolation1;

public class Client {
    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    @EJB(lookup="java:app/env/forappclient")
    private static Isolation1 isolation1;

    public static void main (String[] args) {

        stat.addDescription("hk2-ejb-isolation");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("hk2-ejb-isolationID");
    }

    public Client (String[] args) {
    }

    public void doTest() {

        try {
            Context ic = new InitialContext();

            System.out.println("Looking up ejb ref " + isolation1);
            // create EJB using factory from container
            // Object objref = ic.lookup("java:comp/env/ejb/foo");

            stat.addStatus("ejbclient main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("ejbclient main" , stat.FAIL);
        }

        return;
    }

}

