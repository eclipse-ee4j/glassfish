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
import javax.sql.DataSource;

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
        stat.addDescription("This is to test connector 1.5 "+
                     "contracts.");

        String res = "NOT RUN";
        debug("doTest() ENTER...");
            int testCount = 1;
        try{
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource)ic.lookup("java:app/jdbc/XAPointbase");
            System.out.println("DataSource in appclient : " + ds);
            stat.addStatus("ID Connector Embedded 1.5 test - " + testCount++, stat.PASS);
        }catch(Exception e){
            stat.addStatus("ID Connector Embedded 1.5 test - " + testCount++, stat.FAIL);
            System.out.println("Exception during lookup of java:app/jdbc/XAPointbase : " + e.getMessage());
            //e.printStackTrace();
        }
        try{
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource)ic.lookup("java:app/jdbc/test-resource");
            System.out.println("DataSource (java:app/jdbc/test-resource) in appclient : " + ds);
            stat.addStatus("ID Connector Embedded 1.5 test - " + testCount++, stat.PASS);
        }catch(Exception e){
            stat.addStatus("ID Connector Embedded 1.5 test - " + testCount++, stat.FAIL);
            System.out.println("Exception during lookup of java:app/jdbc/test-resource: " + e.getMessage());
            //e.printStackTrace();
        }
/*        try{
            InitialContext ic = new InitialContext();
            Object ds = (Object)ic.lookup("java:app/eis/testAdmin");
            System.out.println("Admin object (java:app/eis/testAdmin) in appclient : " + ds);
            stat.addStatus("ID Connector Embedded 1.5 test - " + testCount++, stat.PASS);
        }catch(Exception e){
            stat.addStatus("ID Connector Embedded 1.5 test - " + testCount++, stat.FAIL);
//            e.printStackTrace();
        }*/
        boolean pass = false;
        try {
            res  = "ALL TESTS PASSED";
            while (!done()) {

                notifyAndWait();
                if (!done()) {
                    debug("Running...");
                    pass = checkResults(expectedResults());
                    debug("Got expected results = " + pass);

                    //do not continue if one test failed
                    if (!pass) {
                        res = "SOME TESTS FAILED";
                        stat.addStatus("ID Connector Embedded 1.5 test - " + testCount, stat.FAIL);
                        break;
                    } else {
                        stat.addStatus("ID Connector Embedded 1.5 test - " + testCount, stat.PASS);
                    }
                } else {
                    break;
                }
                testCount++;
            }

        } catch (Exception ex) {
            System.out.println("Importing transaction test failed.");
            ex.printStackTrace();
            res = "TEST FAILED";
        }


        /*try{
            InitialContext ic = new InitialContext();
            Object o = ic.lookup("java:app/eis/testAdmin");
            System.out.println("test-admin-object : " + o);
        }catch(Exception e){
            e.printStackTrace();
        }*/


        stat.printSummary("connector1.5EmbeddedID");


        debug("EXITING... STATUS = " + res);
        return res;
    }

    private boolean checkResults(int num) throws Exception {
        Object o = (new InitialContext()).lookup("MyMessageChecker");
        MessageCheckerHome  home = (MessageCheckerHome)
            PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        int result = checker.getMessageCount();
        return result == num;
    }

    private boolean done() throws Exception {
        Object o = (new InitialContext()).lookup("MyMessageChecker");
        MessageCheckerHome  home = (MessageCheckerHome)
            PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        return checker.done();
    }

    private int expectedResults() throws Exception {
        Object o = (new InitialContext()).lookup("MyMessageChecker");
        MessageCheckerHome  home = (MessageCheckerHome)
            PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        return checker.expectedResults();
    }

    private void notifyAndWait() throws Exception {
        Object o = (new InitialContext()).lookup("MyMessageChecker");
        MessageCheckerHome  home = (MessageCheckerHome)
            PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        checker.notifyAndWait();
    }


    private void debug(String msg) {
        System.out.println("[CLIENT]:: --> " + msg);
    }
}

