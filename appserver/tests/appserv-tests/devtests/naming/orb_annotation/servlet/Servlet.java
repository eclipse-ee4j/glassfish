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

package com.sun.s1asdev.orb.annotation;

import java.io.*;
import java.rmi.RemoteException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import jakarta.annotation.Resource;
import jakarta.annotation.Resources;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

public class Servlet extends HttpServlet {

    private @Resource(shareable=false) ORB unshareableOrb;
    private @Resource ORB shareableOrb;

    public void  init( ServletConfig config) throws ServletException {

        super.init(config);
        System.out.println("In webclient::servlet... init()");
    }

    public void service ( HttpServletRequest req , HttpServletResponse resp ) throws ServletException, IOException {

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        try {

            InitialContext ic = new InitialContext();

            //out.println("doing shareable orb test");
            //out.println("ORB = " + shareableOrb);
            POA poa = (POA) shareableOrb.resolve_initial_references("RootPOA");
            //out.println("POA = " + poa);

            //out.println("doing unshareable orb test");
            //out.println("ORB = " + unshareableOrb);
            POA poa1 = (POA) unshareableOrb.resolve_initial_references("RootPOA");
            //out.println("POA = " + poa1);

            out.println("<HTML> <HEAD> <TITLE> ORB Annotation Test  Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>ORB Annotation Test Servlet </FONT> </CENTER> <p> " );
            out.println("<FONT size=+1 color=red> doing unshareable orb test :  </FONT> " + "<br>" );
            out.println("<FONT size=+1 color=red> ORB = " + unshareableOrb + "</FONT> "  + "<br>" );
            out.println("<FONT size=+1 color=red> POA =  </FONT> " + poa1 + "<br>" );

out.println("<FONT size=+1 color=red> doing shareable orb test :  </FONT> " + "<br>" );
            out.println("<FONT size=+1 color=red> ORB = " + shareableOrb + "</FONT> "  + "<br>" );
            out.println("<FONT size=+1 color=red> POA =  </FONT> " + poa + "<br>" );
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
