/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package samples.i18n.simple.servlet;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

/**
 * Servlet included in SimpleI18nServlet
 * @author  Chand Basha
 * @version        1.0
 */
public class IncludedServlet extends HttpServlet {

    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

    }

    /** Destroys the servlet.
     */
    public void destroy() {

    }

    /**Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * Generates response with the information obtained from the including servlet.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, java.io.IOException {
        try {
                        java.io.PrintWriter out        =        res.getWriter();
                        String name                                =        req.getParameter("name");

                        out.println("<H3> This is the name from included servlet </H3>");
                        out.println("<H4> The name entered was:" + name + "</h4>");

                } catch (Exception e) {
                        e.printStackTrace();
                }
    }

    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        processRequest(request, response);
    }

    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        processRequest(request, response);
    }

    /** Servlet to display content from the including servlet.
     */
    public String getServletInfo() {
        return "Servlet to display content from the including servlet";
    }
}
