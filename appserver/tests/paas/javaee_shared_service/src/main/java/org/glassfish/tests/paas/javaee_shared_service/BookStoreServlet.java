/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.paas.javaee_shared_service;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.String;


public final class BookStoreServlet extends HttpServlet {

    @Resource(mappedName = "jdbc/__bookstore2")
    private DataSource ds = null;
    private boolean createdTables = false;

    /**
     * Respond to a GET request for the content produced by
     * this servlet.
     *
     * @param request  The servlet request we are processing
     * @param response The servlet response we are producing
     * @throws IOException      if an input/output error occurs
     * @throws ServletException if a servlet error occurs
     */
    public void service(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        System.out.println("Servlet processing do get..");

        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();

        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Simple PaaS Enabled BookStore Application</title>");
        writeCSS(writer);
        writer.println("</head>");
        writer.println("<body bgcolor=white>");

        writer.println("<table border=\"0\">");
        writer.println("<tr>");
        writer.println("<td>");
        writer.println("<img height=\"200\" width=\"200\" src=\"images/bookstore.gif\">");
        writer.println("</td>");
        writer.println("<td>");
        writer.println("<h1>Simple PaaS Enabled BookStore Application</h1>");
        writer.println("</td>");
        writer.println("</tr>");
        writer.println("</table>");

        writer.println("<table border=\"0\" width=\"100%\">");
        writer.println("<p>This application is served by <b>" +
                getServletContext().getServerInfo() + "</b> [" +
                System.getProperty("com.sun.aas.instanceName") + "]</p>");
        writer.println("Please wait while accessing the bookstore database.....");
        writer.println("</table>");
        if (ds != null) {
            DatabaseOperations operations = new DatabaseOperations();
            String userName = "World";//System.getenv("USER");

            operations.createAccessInfoTable(ds, writer);
            operations.createBookStoreTable(ds, writer);
            operations.updateAccessInfo(ds, userName, writer);

            operations.addBookToTable(ds, request.getParameter("title"),
                    request.getParameter("authors"), request.getParameter("price"));
            operations.printBooksTable(ds, writer);
            generateNewBookForm(writer);

        }

        writer.println("<p/><a href=\'BookStoreServlet\'>My Home</a>");
        writer.println("<p><font color=red>Thanks for using Oracle PaaS Solutions</font></p>");
        writer.println("</body>");
        writer.println("</html>");

    }

    private void writeCSS(PrintWriter out) {
        out.println("<style type=\"text/css\">"
                + "table {"
                + "width:90%;"
                + "border-top:1px solid #e5eff8;"
                + "border-right:1px solid #e5eff8;"
                + "margin:1em auto;"
                + "border-collapse:collapse;"
                + "}"
                + "td {"
                + "color:#678197;"
                + "border-bottom:1px solid #e5eff8;"
                + "border-left:1px solid #e5eff8;"
                + "padding:.3em 1em;"
                + "text-align:center;"
                + "}"
                + "</style>");
    }


    private void generateNewBookForm(PrintWriter out) {
        out.println("<form name=\'add_new_book\' method=\'GET\' action=\'BookStoreServlet\'>");
        out.println("<p/><b>Add a new book to the store:</b>");
        out.println("<table>");
        out.println("<tr><td>Title: </td><td><input type=text name=\'title\' size=30 " +
                "value=\'Developing PaaS Components\'></td></tr>");
        out.println("<tr><td>Author(s): </td><td><input type=text name=\'authors\' size=30 " +
                "value=\'Shalini M\'></td></tr>");
        out.println("<tr><td>Price:</td><td><input type=text name=\'price\' size=30 value=\'100$\'></td></tr>");
        out.println("<tr><td></td><td><input type=submit value=\'Add This Book\'></td></tr>");
        out.println("</table>");
        out.println("</form>");
    }
}

