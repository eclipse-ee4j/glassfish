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

/*
 * Client.java
 *
 * Created on February 21, 2003, 3:20 PM
 */

package  com.sun.s1asdev.ejb.ejbflush.client;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.sun.s1asdev.ejb.ejbflush.Test;
import com.sun.s1asdev.ejb.ejbflush.TestHome;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 *
 * @author  mvatkina
 * @version
 */
public class Client {
    private Test t;

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");


    public Client(String[] args) {
        try {
            Context initial = new InitialContext();

            Object objref = initial.lookup("java:comp/env/ejb/T1");
            TestHome thome = (TestHome)PortableRemoteObject.narrow(objref, TestHome.class);

            t = thome.create();
        } catch( Exception ex ) {
            System.err.println("Client(): Caught an exception:");
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {

        try {
            System.out.println("START");

            stat.addDescription("ejbFlush");

            Client client = new Client(args);
            client.checkCmp11Bean();
            client.checkCmp20Bean();

            System.out.println("FINISH");

        } catch (Exception ex) {
            System.err.println("Client.main():Caught an exception:");
            ex.printStackTrace();
        }

        stat.printSummary("ejbFlush");
    }

    private void checkCmp11Bean() {
        try {
            t.testA1();
            System.out.println("A1 OK");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("A1 FAILED");
            stat.addStatus("ejbclient checkCmp11Bean", stat.FAIL);
            return;
        }

        try {
            t.testA1WithFlush();
            System.out.println("A1WithFlush OK");
        } catch (Exception e) {
            System.out.println("A1 FAILED " + e.getMessage());
            e.printStackTrace();
            stat.addStatus("ejbclient checkCmp11Bean", stat.FAIL);
            return;
        }

        stat.addStatus("ejbclient checkCmp11Bean", stat.PASS);

    }//checkCmp11Bean()

    private void checkCmp20Bean() {
        try {
            t.testA2();
            System.out.println("A2 OK");
        } catch (Exception e) {
            System.out.println("A2 FAILED");
            stat.addStatus("ejbclient checkCmp20Bean", stat.FAIL);
            return;
        }

        try {
            t.testA2();
            System.out.println("A2 FAILED");
            stat.addStatus("ejbclient checkCmp20Bean", stat.FAIL);
            return;
        } catch (Exception e) {
            Throwable t = e;
            boolean ok = false;
            while (t != null) {
                System.out.println("Nested: " + t);
                if (t instanceof java.sql.SQLException) {
                    java.sql.SQLException se = (java.sql.SQLException) t;
                    System.out.println("ErrorCode: " + se.getErrorCode());
                    System.out.println("SQLState: " + se.getSQLState());
                    java.sql.SQLException nse = se.getNextException();
                    if (nse != null) {
                        System.out.println("Nested SQLException: " + nse);
                        System.out.println("ErrorCode: " + nse.getErrorCode());
                        System.out.println("SQLState: " + nse.getSQLState());
                    }
                    ok = true;
                } else {
                    System.out.println("Not a java.sql.SQLException");
                }
                t = t.getCause();
            }

            if (ok)  {
                System.out.println("A2 SQLException OK");
            } else {
                System.out.println("A2 FAILED - no SQLException detected");
                stat.addStatus("ejbclient checkCmp20Bean", stat.FAIL);
            }
        }

        try {
            t.testA2WithFlush();
            System.out.println("A2WithFlush OK");
        } catch (Exception e) {
            System.out.println("A2 FAILED " + e.getMessage());
            e.printStackTrace();
            stat.addStatus("ejbclient checkCmp20Bean", stat.FAIL);
            return;
        }

        stat.addStatus("ejbclient checkCmp20Bean", stat.PASS);

    }//checkCmp20Bean()

}//Client{}
