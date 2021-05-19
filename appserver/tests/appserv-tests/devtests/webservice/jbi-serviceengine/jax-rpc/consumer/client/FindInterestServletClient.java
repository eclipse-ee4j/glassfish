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

package myclient;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.Serializable;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import javax.naming.*;
import javax.xml.namespace.QName;

public class FindInterestServletClient extends HttpServlet
                        {
    HttpServletResponse resp;
    public FindInterestServletClient() {
        System.out.println("FindInterestServletImpl() instantiated");
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws jakarta.servlet.ServletException {
           this.resp = resp;
           doPost(req, resp);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp)
              throws jakarta.servlet.ServletException {
       try{
           this.resp = resp;
           calculateInterest();
       }catch(Exception e) {
          throw new jakarta.servlet.ServletException(e);
       }

    }

    public void calculateInterest() throws Exception {
        System.out.println("calculateInterest invoked from servlet ");
        FindInterestClient client = new FindInterestClient();
        double interest= client.doTest();
        PrintWriter out = resp.getWriter();
                resp.setContentType("text/html");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>FindInterestServletClient</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<p>");
                out.println("So the RESULT OF FindInterest SERVICE IS :");
                out.println("</p>");
                out.println("[" + interest + "]");
                out.println("</body>");
                out.println("</html>");
                out.flush();
                out.close();
    }
}
