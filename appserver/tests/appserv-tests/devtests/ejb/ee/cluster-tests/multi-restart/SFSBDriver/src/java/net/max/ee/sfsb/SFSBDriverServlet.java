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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.max.ee.sfsb;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 *
 * @author mk
 */
public class SFSBDriverServlet extends HttpServlet {


    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet SFSBDriverServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet SFSBDriverServlet at " + request.getContextPath () + "</h1>");
            out.println("<h1>SFSBDriverServlet looking up jndi using: java:global/SFSBDriver/SimpleSessionBean </h1>");

            HttpSession session = request.getSession(true);
            Integer intAttr = (Integer) session.getAttribute("intAttr");

            String KEY_1 = "SFSB_1";
            String KEY_2 = "SFSB_2";

            SimpleSessionBean sfsb1 = (SimpleSessionBean) session.getAttribute(KEY_1);
            SimpleSessionBean sfsb2 = (SimpleSessionBean) session.getAttribute(KEY_2);


            out.println("Integer attr: " + intAttr);
            intAttr = (intAttr == null) ? 0 : (intAttr.intValue() + 1);
            session.setAttribute("intAttr", intAttr);

                out.println("<h1>From session SFSB[1] NOT NULL ?: " + ("" + (sfsb1 != null)) + " </h1>");
                out.println("<h1>From session SFSB[2] NOT NULL? : " + ("" + (sfsb2 != null)) + " </h1>");

            if (sfsb1 == null) {
                InitialContext ctx = new InitialContext();

                sfsb1 = (SimpleSessionBean) ctx.lookup("java:global/SFSBDriver/SimpleSessionBean");
                sfsb2 = (SimpleSessionBean) ctx.lookup("java:global/SFSBDriver/SimpleSessionBean");

                out.println("<h1>Created SFSB[1]: " + sfsb1.asString() + " </h1>");
                out.println("<h1>Created SFSB[2]: " + sfsb2.asString() + " </h1>");

            } else {
                try {
                    out.println("<h1>Retrieved SFSB[1]: " + sfsb1.asString() + " </h1>");
                    sfsb1.incrementCounter();
                } catch (Exception ex1) {
                    out.println("<h1>Error while accessing SFSB[1] </h1>");
                    ex1.printStackTrace(out);
                }

                try {
                    out.println("<h1>Retrieved SFSB[2]: " + sfsb2.asString() + " </h1>");
                    sfsb2.incrementCounter();
                } catch (Exception ex2) {
                    out.println("<h1>Error while accessing SFSB[2] </h1>");
                    ex2.printStackTrace(out);
                }
            }

            session.setAttribute(KEY_1, sfsb1);
            session.setAttribute(KEY_2, sfsb2);
            out.println("</body>");
            out.println("</html>");
        } catch (Exception nmEx) {
            nmEx.printStackTrace(out);
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
