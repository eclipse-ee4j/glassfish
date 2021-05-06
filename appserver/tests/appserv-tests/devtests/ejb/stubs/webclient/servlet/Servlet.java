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

package com.sun.s1asdev.ejb.stubs.webclient;

import java.io.*;
import java.rmi.RemoteException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import com.sun.s1asdev.ejb.stubs.ejbapp.Hello;
import com.sun.s1asdev.ejb.stubs.ejbapp.HelloHome;

//import javax.management.j2ee.ManagementHome;
//import javax.management.j2ee.Management;

public class Servlet extends HttpServlet {


    public void  init( ServletConfig config) throws ServletException {

        super.init(config);
        System.out.println("In webclient::servlet... init()");
    }

    public void service ( HttpServletRequest req , HttpServletResponse resp ) throws ServletException, IOException {

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        try {

            InitialContext ic = new InitialContext();

            System.out.println("Skipping mejb test for now.  NEED TO REENABLE");
            //            System.out.println("Calling DummyMEJBBean");

            /* TODO reenable once javax.management classes are available
            Object objref = ic.lookup("java:comp/env/ejb/dummymejb");
            ManagementHome mHome = (ManagementHome)
                PortableRemoteObject.narrow(objref, ManagementHome.class);
            Management dummyMejb = mHome.create();

            System.out.println("Invoking dummy mejb to force context class loader test.");
            dummyMejb.getMBeanCount();
            System.out.println("dummy mejb test successful");

            */

           System.out.println("Looking up ejb ref ");
            // create EJB using factory from container
            Object objref2 = ic.lookup("java:comp/env/ejb/hello");
            System.out.println("objref = " + objref2);
            System.err.println("Looked up home!!");

            HelloHome  home = (HelloHome)PortableRemoteObject.narrow
                (objref2, HelloHome.class);

            System.err.println("Narrowed home!!");

            Hello hr = home.create();
            System.err.println("Got the EJB!!");

            // invoke method on the EJB
            System.out.println("invoking ejb");
            hr.sayHello();

            System.out.println("successfully invoked ejb");

            out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServelt :: All information I can give </FONT> </CENTER> <p> " );
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>" );
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>" );
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>" );
            out.println("</BODY> </HTML> ");

        }catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("webclient servlet test failed");
            throw new ServletException(ex);
        }
    }



    public void  destroy() {
        System.out.println("in webclient::servlet destroy");
    }

}
