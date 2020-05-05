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

package org.glassfish.tests.paas.lazysharedservice;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;


public final class BasicDBPaaSServlet extends HttpServlet {

    @Resource(mappedName = "java:app/jdbc/LazyInitSharedService")
    private DataSource ds = null;

    /**
     * Respond to a GET request for the content produced by
     * this servlet.
     *
     * @param request  The servlet request we are processing
     * @param response The servlet response we are producing
     * @throws java.io.IOException      if an input/output error occurs
     * @throws jakarta.servlet.ServletException if a servlet error occurs
     */
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        System.out.println("Servlet processing do get..");

        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();

        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Basic DB PaaS Application</title>");
        writer.println("</head>");
        writer.println("<body bgcolor=white>");

        writer.println("<table border=\"0\">");
        writer.println("<tr>");
        writer.println("<td>");
        //writer.println("<img src=\"images/tomcat.gif\">");
        writer.println("</td>");
        writer.println("<td>");
        writer.println("<h1>Basic DB PaaS Application</h1>");
        writer.println("Request headers from the request:");
        writer.println("</td>");
        writer.println("</tr>");
        writer.println("</table>");

        writer.println("<table border=\"0\" width=\"100%\">");
        Enumeration names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            writer.println("<tr>");
            writer.println("  <th align=\"right\">" + name + ":</th>");
            writer.println("  <td>" + request.getHeader(name) + "</td>");
            writer.println("</tr>");
        }
        writer.println("</table>");
        if (ds != null) {
            Statement stmt = null;
            try {
                stmt = ds.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery("SELECT TABLENAME from sys.systables");
                writer.println("<table border=\"0\" width=\"100%\">");
                writer.println("<tr>");
                writer.println("  <th align=\"left\" colspan=\"2\">" + "List of Tables in Database" + "</th>");
                writer.println("</tr>");
                while (rs.next()) {
                    writer.println("<tr>");
                    writer.println("  <td align=\"right\">" + " " + "</td>");
                    writer.println("  <td>" + rs.getObject(1) + "</td>");
                    writer.println("</tr>");
                }
                writer.println("</table>");
            } catch (Exception ex) {
                ex.printStackTrace(writer);
            } finally {
                if (stmt != null) {
                    try {
                        stmt.getConnection().close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            //writer.println("DataSource is null");
        }
        writer.println("</body>");
        writer.println("</html>");


    }

}

