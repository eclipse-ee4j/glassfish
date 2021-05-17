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

package org.glassfish.tests.paas.basicdbteardownsql;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DbConnectionDetailsServlet extends HttpServlet{

    @Resource(mappedName = "java:app/jdbc/CoffeeRes")
    private DataSource ds = null;

    private static Map userCredentials = new HashMap<String, String>();

    static {
        userCredentials.put("APP", "APP");
        userCredentials.put("root", "mysql");
        userCredentials.put("scott", "tiger");
    }

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

        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();
        if (ds != null) {
            Statement stmt = null;
            try {
                stmt = ds.getConnection().createStatement();

            DatabaseMetaData dbMetadata = stmt.getConnection().getMetaData();

            writer.println(dbMetadata.getURL());
                writer.println(dbMetadata.getUserName());
                writer.println(userCredentials.get(dbMetadata.getUserName()));
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
        }
    }
}
