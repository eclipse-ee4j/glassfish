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

package servlet;

import beans.MessageCheckerHome;
import beans.MessageChecker;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.io.IOException;
import java.io.PrintWriter;

public class SimpleServlet extends HttpServlet {


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * handles the HTTP POST operation *
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doTest(request, response);
    }

    public String doTest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        debug("This is to test connector 1.5 " +
                "contracts.");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String res = "NOT RUN";
        debug("doTest() ENTER...");
        boolean pass = false;
        try {
            res = "ALL TESTS PASSED";
            int testCount = 1;
            out.println("Starting the test");
            out.flush();
            while (!done()) {

                notifyAndWait();
                if (!done()) {
                    debug("Running...");
                    pass = checkResults(expectedResults());
                    debug("Got expected results = " + pass);

                    //do not continue if one test failed
                    if (!pass) {
                        res = "SOME TESTS FAILED";
                        System.out.println("ID Connector Embedded 1.5 test - " + testCount + " FAIL");
                        out.println("TEST:FAIL");
                        break;
                    } else {
                        System.out.println("ID Connector Embedded 1.5 test - " + testCount + " PASS");
                        out.println("TEST:PASS");
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
        } finally {
            try {
                out.println("END_OF_TEST");
                out.flush();
                done();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("connector1.5EmbeddedID");


        debug("EXITING... STATUS = " + res);
        return res;
    }

    private boolean checkResults(int num) throws Exception {
        Object o = (new InitialContext()).lookup("java:comp/env/ejb/messageChecker");
        MessageCheckerHome home = (MessageCheckerHome)
                PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        int result = checker.getMessageCount();
        return result == num;
    }

    private boolean done() throws Exception {
        Object o = (new InitialContext()).lookup("java:comp/env/ejb/messageChecker");
        MessageCheckerHome home = (MessageCheckerHome)
                PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        return checker.done();
    }

    private int expectedResults() throws Exception {
        Object o = (new InitialContext()).lookup("java:comp/env/ejb/messageChecker");
        MessageCheckerHome home = (MessageCheckerHome)
                PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        return checker.expectedResults();
    }

    private void notifyAndWait() throws Exception {
        Object o = (new InitialContext()).lookup("java:comp/env/ejb/messageChecker");
        MessageCheckerHome home = (MessageCheckerHome)
                PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        checker.notifyAndWait();
    }


    private void debug(String msg) {
        System.out.println("[CLIENT]:: --> " + msg);
    }
}
