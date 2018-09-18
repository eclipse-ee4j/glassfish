<%--

    Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Public License v. 2.0, which is available at
    http://www.eclipse.org/legal/epl-2.0.

    This Source Code may also be made available under the following Secondary
    Licenses when the conditions for such availability set forth in the
    Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
    version 2 with the GNU Classpath Exception, which is available at
    https://www.gnu.org/software/classpath/license.html.

    SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

--%>

<%@ page import="javax.naming.*,java.sql.*,javax.sql.*" %>

<%
        Connection conn = null;
        String userName = request.getUserPrincipal().getName();
        try {
            InitialContext initialContext = new InitialContext();
            DataSource ds =
                (DataSource) initialContext.lookup("java:comp/env/jdbc/DS");

            conn = (Connection) ds.getConnection();

            System.out.println(" got the connection : " + conn);

            System.out.println("** insert " + userName + " into securitymapwebdb");

            PreparedStatement prepStmt =
                conn.prepareStatement("insert into securitymapwebdb values(?)");

            prepStmt.setString(1, userName);

            prepStmt.executeUpdate();


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        out.println("done - " + userName);
%>
