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

package org.glassfish.tests.paas.multipleshareddbservicetest;

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
import java.sql.PreparedStatement;
import java.sql.DatabaseMetaData;
import java.util.Enumeration;
import jakarta.annotation.Resource;


public final class EmployeeServlet extends HttpServlet {

    @Resource(mappedName = "java:app/jdbc/SalaryRes")
    private DataSource salDs = null;

    @Resource(mappedName = "java:app/jdbc/HrRes")
    private DataSource hrDs = null;

    /**
     * Respond to a GET request for the content produced by
     * this servlet.
     *
     * @param request  The servlet request we are processing
     * @param response The servlet response we are producing
     * @throws IOException      if an input/output error occurs
     * @throws ServletException if a servlet error occurs
     */
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        System.out.println("Servlet processing do get..");

        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();

        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Multiple Shared DB Service Test</title>");
        writer.println("</head>");
        writer.println("<body bgcolor=white>");

        writer.println("<table border=\"0\">");
        writer.println("<tr>");
        writer.println("<td>");
        writer.println("<img height=\"200\" width=\"200\" src=\"images/numbers.jpg\">");
        writer.println("</td>");
        writer.println("<td>");
        writer.println("<h1>Multiple Shared DB Service PaaS Application</h1>");
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
        if (hrDs != null && salDs != null) {
            Statement stmt1 = null;
        PreparedStatement stmt2 = null;
            try {
                stmt1 = hrDs.getConnection().createStatement();

                DatabaseMetaData dbMetadata1 = stmt1.getConnection().getMetaData();
                String dbUrl1 = dbMetadata1.getURL();
                writer.println("DB URL : " + dbUrl1 + "\n");
                if (dbUrl1.indexOf("hr_database") == -1) {
                    throw new Exception("Custom Database [hr_database] is not created while provisioning.");
                }


                ResultSet rs1 = stmt1.executeQuery("SELECT emp_id, emp_name from HR");
        String salQuery = "SELECT emp_sal from SALARY WHERE emp_id = ? ";
        stmt2 = salDs.getConnection().prepareStatement(salQuery);

        DatabaseMetaData dbMetadata2 = stmt2.getConnection().getMetaData();
                String dbUrl2 = dbMetadata2.getURL();
                writer.println("DB URL : " + dbUrl2 + "\n");
                if (dbUrl2.indexOf("salary_database") == -1) {
                    throw new Exception("Custom Database [salary_database] is not created while provisioning.");
                }

        writer.println("<table border=\"1\" width=\"100%\">");
                writer.println("<tr>");
                writer.println("  <th align=\"left\" colspan=\"2\">" + "Employee Information retrieved" + "</th>");
                writer.println("</tr>");
                writer.println("<tr>");
                writer.println("<td>" + "Employee ID" + "</td>");
                writer.println("<td>" + "Employee Name" + "</td>");
        writer.println("<td>" + "Employee Salary" + "</td>");
                writer.println("</tr>");
                while (rs1.next()) {
                    writer.println("<tr>");
                    writer.println("  <td>" + rs1.getObject(1) + "</td>");
                    writer.println("  <td>" + rs1.getObject(2) + "</td>");
            stmt2.setInt(1, (Integer) rs1.getObject(1));
            ResultSet rs2 = stmt2.executeQuery();
            while(rs2.next()) {
            writer.println("  <td>" + rs2.getObject(1) + "</td>");
            }
                    writer.println("</tr>");
                }
                writer.println("</table>");
            } catch (Exception ex) {
                ex.printStackTrace(writer);
            } finally {
                if (stmt1 != null) {
                    try {
                        stmt1.getConnection().close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (stmt2 != null) {
                    try {
                        stmt2.getConnection().close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
        }
        writer.println("</body>");
        writer.println("</html>");
    }

}

