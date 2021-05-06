/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package client;

import beans.*;
import connector.*;
import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client   {

    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");

    public Client (String[] args) {
        //super(args);
    }

    public static void main(String[] args) {
        Client client = new Client(args);
        client.doTest();
    }

    public String doTest() {
        stat.addDescription("This is to test connector ThreadPool "+
                     "contracts.");

        String res = "NOT RUN";
        debug("Starting the thread pool test=> Please wait...");
        boolean pass = false;
        try {
            res  = " TEST PASSED";
            test();
            stat.addStatus(" Connector ThreadPool test " , stat.PASS);
        } catch (Exception ex) {
            System.out.println("Thread pool test failed.");
            ex.printStackTrace();
            res = "TEST FAILED";
            stat.addStatus(" Connector ThreadPool test " , stat.FAIL);
        }

        stat.printSummary("Connector-ThreadPool");


        debug("EXITING... STATUS = " + res);
        return res;
    }

    private void test() throws Exception {
        Object o = (new InitialContext()).lookup("WorkTest");
        WorkTestHome  home = (WorkTestHome)
            PortableRemoteObject.narrow(o, WorkTestHome.class);
        WorkTest wt = home.create();
        wt.executeTest();
    }

    private void debug(String msg) {
        System.out.println("[CLIENT]:: --> " + msg);
    }
}

