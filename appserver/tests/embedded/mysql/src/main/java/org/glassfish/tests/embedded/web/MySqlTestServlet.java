/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.embedded.web;

import jakarta.annotation.Resource;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.annotation.sql.DataSourceDefinitions;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import jakarta.transaction.UserTransaction;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

/**
 * @author bhavanishankar@java.net
 */
@DataSourceDefinitions({
@DataSourceDefinition(name = "java:app/mysql/MySQLDataSource",
        className = "com.mysql.jdbc.jdbc2.optional.MysqlDataSource",
        portNumber = 3306,
        serverName = "localhost",
        databaseName = "testDB",
        user = "root",
        password = "abc123",
        properties = {"createDatabaseIfNotExist=true"}),
@DataSourceDefinition(name = "java:app/mysql/MySQLEmbeddedDataSource",
        className = "com.mysql.jdbc.Driver",
        url="jdbc:mysql:mxj://localhost:3336/testDB",
        user = "root",
        password = "abc123",
        properties = {"createDatabaseIfNotExist=true",
                "server.basedir=/tmp/testDB", "server.initialize-user=true"})
})
@WebServlet(name = "mySqlTestServlet", urlPatterns = "/mysqlTestServlet")
public class MySqlTestServlet extends HttpServlet {

    @Resource(mappedName = "java:app/mysql/MySQLDataSource")
    DataSource myDB;

    @Resource(mappedName = "java:app/mysql/MySQLEmbeddedDataSource")
    DataSource myEmbeddedDB;

    @PersistenceContext
    private EntityManager em;

    @Resource
    private UserTransaction utx;


    @Override
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws ServletException, IOException {
        PrintWriter writer = httpServletResponse.getWriter();
        try {
            writer.println("DS = " + myDB);
            writer.println("EM = " + em);
            Connection connection = myEmbeddedDB.getConnection();
            writer.println("connection = " + connection);
            connection.close();

            if (!entryExists("BHAVANI-13-02")) {
                Person person = new Person("BHAVANI-13-02", "Bhavanishankar", "Engineer");
                utx.begin();
                em.persist(person);
                utx.commit();
                System.out.println("Persisted " + person);
            }
        } catch (Exception ex) {
            ex.printStackTrace(writer);
        } finally {
            writer.flush();
            writer.close();
        }
    }

    private boolean entryExists(String uuid) {
        return em.find(Person.class, uuid) != null;
    }

}
