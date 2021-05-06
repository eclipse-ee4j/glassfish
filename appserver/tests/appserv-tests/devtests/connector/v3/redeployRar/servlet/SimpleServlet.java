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

import beans.VersionCheckerHome;
import beans.VersionChecker;

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

    public String doTest( HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        int versionToTest = Integer.valueOf((String)request.getParameter("versionToTest"));
        out.println("This is to test redeployment of connector modules. Testing version " + versionToTest);


        String res = "NOT RUN";
            debug("doTest() ENTER...");
        boolean pass = false;
        try {
                pass = checkResults(versionToTest);
                debug("Got expected results = " + pass);

                //do not continue if one test failed
                if (!pass) {
                        res = "SOME TESTS FAILED";
                        System.out.println("Redeploy Connector 1.5 test - Version : "+ versionToTest + " FAIL");
            out.println("TEST:FAIL");
                } else {
                        res  = "ALL TESTS PASSED";
                        System.out.println("Redeploy Connector 1.5 test - Version : "+ versionToTest + " PASS");
            out.println("TEST:PASS");
                }
        } catch (Exception ex) {
            System.out.println("Redeploy connector test failed.");
            ex.printStackTrace();
            res = "TEST FAILED";
        }finally{
            out.println("Redeploy Connector 1.5");
            out.println("END_OF_TEST");
        }

        debug("EXITING... STATUS = " + res);

        return res;
    }

    private boolean checkResults(int num) throws Exception {
            debug("checkResult" + num);
            debug("got initial context" + (new InitialContext()).toString());
        Object o = (new InitialContext()).lookup("java:comp/env/ejb/MyVersionChecker");
        debug("got o" + o);
        VersionCheckerHome home = (VersionCheckerHome)
            PortableRemoteObject.narrow(o, VersionCheckerHome.class);
        debug("got home" + home);
            VersionChecker checker = home.create();
            debug("got o" + checker);
        //problem here!
        int result = checker.getVersion();
        debug("checkResult" + result);
        return result == num;
    }

    private void debug(String msg) {
        System.out.println("[Redeploy Connector CLIENT]:: --> " + msg);
    }
}
