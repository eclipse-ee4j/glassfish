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


    public void doGet (HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
      doPost(request, response);
    }

    /** handles the HTTP POST operation **/
    public void doPost (HttpServletRequest request,HttpServletResponse response)
          throws ServletException, IOException {
        doTest(request, response);
    }

    public String doTest(HttpServletRequest request, HttpServletResponse response) throws IOException{
        System.out.println("This is to test connector 1.6 "+
                     "contracts.");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String res = "NOT RUN";
        debug("doTest() ENTER...");
        boolean pass = false;
        try {
            res  = "ALL TESTS PASSED";
            int testCount = 1;
            out.println("Starting the test");
            out.flush();

             testCount++;
             if(testAdminObject()){
                 out.println("TEST:PASS");
                        System.out.println("Admin Object Resource - ResourceAdapterAssociation- " + testCount + " PASS");
             }else{
                 out.println("TEST:FAIL");
                        System.out.println("Admin Object Resource - ResourceAdapterAssociation- " + testCount + " FAIL");
             }


            while (!done(out)) {

                notifyAndWait(out);
                if (!done(out)) {
                    debug("Running...");
                    pass = checkResults(expectedResults(out), out);
                    debug("Got expected results = " + pass);

                    //do not continue if one test failed
                    if (!pass) {
                        res = "SOME TESTS FAILED";
                        System.out.println("ID Connector 1.6 test - " + testCount + " FAIL");
                        out.println("TEST:FAIL");

                    } else {
                        System.out.println("ID Connector 1.6 test - " + testCount + " PASS");
                        out.println("TEST:PASS");
                            }
                } else {
                    out.println("END_OF_EXECUTION");
                    break;
                }
            }
            out.println("END_OF_EXECUTION");


        } catch (Exception ex) {
            System.out.println("Importing transaction test failed.");
            ex.printStackTrace();
            res = "TEST FAILED";
            out.println("TEST:FAIL");
        }finally{
            out.println("END_OF_EXECUTION");
            out.flush();
        }
        System.out.println("connector1.6ID");


        debug("EXITING... STATUS = " + res);
        return res;
    }

    private boolean testAdminObject() throws Exception {
        Object o = (new InitialContext()).lookup("java:comp/env/ejb/messageChecker");
        MessageCheckerHome home = (MessageCheckerHome)
            PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        return checker.testAdminObjectResourceAdapterAssociation();
    }

    private boolean checkResults(int num, PrintWriter out) throws Exception {
        out.println("checking results");
        out.flush();
        Object o = (new InitialContext()).lookup("java:comp/env/ejb/messageChecker");
        MessageCheckerHome home = (MessageCheckerHome)
            PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        int result = checker.getMessageCount();
        System.out.println("Expected num : " + num);
        System.out.println("Result got : " + result);

        return result == num;
    }

    private boolean done(PrintWriter out) throws Exception {
        out.println("Checking whether its completed");
        out.flush();
        Object o = (new InitialContext()).lookup("java:comp/env/ejb/messageChecker");
        MessageCheckerHome  home = (MessageCheckerHome)
            PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        return checker.done();
    }

    private int expectedResults(PrintWriter out) throws Exception {
        out.println("expectedResults");
        out.flush();
        Object o = (new InitialContext()).lookup("java:comp/env/ejb/messageChecker");
        MessageCheckerHome  home = (MessageCheckerHome)
            PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        return checker.expectedResults();
    }

    private void notifyAndWait(PrintWriter out) throws Exception {
        out.println("notifyAndWait");
        out.flush();
        Object o = (new InitialContext()).lookup("java:comp/env/ejb/messageChecker");
        MessageCheckerHome  home = (MessageCheckerHome)
            PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        checker.notifyAndWait();
    }


    private void debug(String msg) {
        System.out.println("[CLIENT]:: --> " + msg);
    }
}
