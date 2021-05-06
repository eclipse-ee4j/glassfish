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

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.ejb.CreateException;
import java.io.IOException;
import java.io.PrintWriter;

import com.sun.s1asdev.jdbc.resenabledisable.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.resenabledisable.ejb.SimpleBMP;

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

    public void doTest(HttpServletRequest request, HttpServletResponse response) throws IOException{
        PrintWriter out = response.getWriter();

        try{
        System.out.println("JDBC Resource Enable/Disable test");

        InitialContext ic = new InitialContext();
        SimpleBMPHome simpleBMPHome = (SimpleBMPHome) ic.lookup("java:comp/env/ejb/SimpleBMPEJB");
        out.println("Running resource enable disable test ");

        //stat.addDescription("Running serializable connector test ");
        SimpleBMP bean = simpleBMPHome.create();

            for(int i=0; i<3; i++){
                if(!bean.test1()){
                    out.println("TEST:FAIL");
                    break;
                }
            }
            out.println("TEST:PASS");
        } catch(NamingException ne) {
            ne.printStackTrace();
        } catch(CreateException e) {
            e.printStackTrace();
        } finally {
            out.println("END_OF_TEST");
            out.flush();
        }
    }
}
