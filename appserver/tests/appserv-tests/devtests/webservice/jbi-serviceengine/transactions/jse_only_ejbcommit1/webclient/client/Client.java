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

package client;

import jakarta.annotation.Resource;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import javax.naming.InitialContext;
import jakarta.transaction.UserTransaction;
import jakarta.transaction.Status;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.sql.DataSource;

import jakarta.xml.ws.*;
import endpoint.ejb.*;

public class Client extends HttpServlet {

       @WebServiceRef(name="sun-web.serviceref/HelloEJBService")
       HelloEJBService service;
       @Resource(mappedName="jdbc/__default") private DataSource ds;

       public void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws jakarta.servlet.ServletException {
           doPost(req, resp);
       }

       public void doPost(HttpServletRequest req, HttpServletResponse resp)
              throws jakarta.servlet.ServletException {
            UserTransaction ut = null;
            // Create Table with name CUSTOMER_cm1. This name will be used in the EJB
            String tableName = "CUSTOMER_cm1";
            String[] names = {"Vikas", "VikasAwasthi"};
            String[] emails= {"vikas@sun.com", "VikasA@sun.com"};
            try {
                Connection con = ds.getConnection();
                createTable(con, tableName);
                ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
                ut.begin();

                updateTable(con, tableName, names[1], emails[1]);
                System.out.println(" Service is :" + service);
                Hello port = service.getHelloEJBPort();

                String ret = port.sayHello("Appserver Tester !");
                System.out.println("Return value from webservice:"+ret);

                if(ut.getStatus() != Status.STATUS_ACTIVE) {
                    ret += "FAILED";
                } else {
                    System.out.println("**** committing transaction");
                    ut.commit();
                    if(!isDataUpdated(con, tableName, names, emails)) {
                        ret += "FAILED";
                    }
                }
                PrintWriter out = resp.getWriter();
                resp.setContentType("text/html");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>TestServlet</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<p>");
                out.println("So the RESULT OF EJB webservice IS :");
                out.println("</p>");
                out.println("[" + ret + "]");
                out.println("</body>");
                out.println("</html>");
                dropTable(con, tableName);
            } catch(Exception e) {
                e.printStackTrace();
            }
       }

       // use this table in the EJB webservice
       private void createTable(Connection con, String tableName) throws Exception {
            // autocommit is made true so that the table is created immediately
            boolean autoCommit = con.getAutoCommit();
            con.setAutoCommit(true);
            System.out.println("**** auto commit = " + con.getAutoCommit());
            PreparedStatement pStmt =
            con.prepareStatement("CREATE TABLE "+tableName+" (NAME VARCHAR(30) NOT NULL PRIMARY KEY, EMAIL VARCHAR(30))");
            pStmt.executeUpdate();
            con.setAutoCommit(autoCommit);
       }

       private void dropTable(Connection con, String tableName) throws Exception {
            boolean autoCommit = con.getAutoCommit();
            con.setAutoCommit(true);
            PreparedStatement pStmt = con.prepareStatement("DROP TABLE "+tableName);
            pStmt.executeUpdate();
            con.setAutoCommit(autoCommit);
       }

       // Check whether the EJB webservice has updated the data in the table.
       private boolean isDataUpdated(Connection con, String tableName, String[] names, String[] emails) throws Exception {
            PreparedStatement pStmt = con.prepareStatement("SELECT NAME, EMAIL FROM "+tableName);
            ResultSet rs = pStmt.executeQuery();
            int allDataCount = 0;
            while(rs.next()) {
                String db_Name  = rs.getString(1);
                String db_Email = rs.getString(2);
                System.out.println("NAME="+db_Name+", EMAIL="+db_Email);
                      for (int i=0; i < names.length; i++)
                    if(db_Name.equals(names[i]) && db_Email.equals(emails[i]))
                        allDataCount++;
            }
            rs.close();
            return (allDataCount == names.length);
       }

       private void updateTable(Connection con, String tableName, String name, String email)
                throws Exception {
           PreparedStatement pStmt =
                con.prepareStatement("INSERT INTO "+ tableName +" (NAME, EMAIL) VALUES(?,?)");
           pStmt.setString(1, name);
           pStmt.setString(2, email);
           pStmt.executeUpdate();
       }
}
