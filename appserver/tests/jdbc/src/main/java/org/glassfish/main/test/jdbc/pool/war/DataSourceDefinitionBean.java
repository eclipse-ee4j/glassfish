/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.jdbc.pool.war;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.lang.System.Logger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import static jakarta.ejb.TransactionAttributeType.NOT_SUPPORTED;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

/**
 * @author David Matejcek
 */
@DataSourceDefinition(name = JdbcDsName.JDBC_DS_1, //
    className = "org.postgresql.ds.PGSimpleDataSource", //
    serverName = "tc-testdb", //
    portNumber = 5432, //
    user = "test", //
    password = "test", //
    databaseName = "testdb", //
    properties = { //

    })
@DataSourceDefinition(name = JdbcDsName.JDBC_DS_2, //
    className = "org.postgresql.ds.PGSimpleDataSource", //
    serverName = "tc-testdb", //
    portNumber = 5432, //
    user = "test", //
    password = "test", //
    databaseName = "testdb", //
    properties = { //

    })
@Path("rest")
@Stateless
@TransactionAttribute(NOT_SUPPORTED)
public class DataSourceDefinitionBean {
    private static final Logger LOG = System.getLogger(DataSourceDefinitionBean.class.getName());

    @Resource(name = JdbcDsName.JDBC_DS_1)
    private DataSource dsa1;
    @Resource(name = JdbcDsName.JDBC_DS_2)
    private DataSource dsa2;

    @Resource(lookup = JdbcDsName.JDBC_DS_POOL_A)
    private DataSource dsd1;
    @Resource(lookup = JdbcDsName.JDBC_DS_POOL_B)
    private DataSource dsd2;

    @PostConstruct
    private void init() {
        LOG.log(ERROR, "****************************************** HI!");
    }

    @GET
    @Path("/versions")
    @Produces(MediaType.TEXT_PLAIN)
    public Response versions() throws Exception {
        LOG.log(ERROR, "==================================== whoa!");
        try (Connection connection = dsa1.getConnection()) {
            LOG.log(ERROR, "++++++++++++++++++++++++++++++++++++++++++++++CONNECTION DSA1: " + connection);
        }
        try (Connection connection = dsa2.getConnection()) {
            LOG.log(ERROR, "++++++++++++++++++++++++++++++++++++++++++++++CONNECTION DSA2: " + connection);
        }
        try (Connection connection = dsd1.getConnection()) {
            LOG.log(ERROR, "++++++++++++++++++++++++++++++++++++++++++++++CONNECTION DSD1: " + connection);
        }
        try (Connection connection = dsd2.getConnection()) {
            LOG.log(ERROR, "++++++++++++++++++++++++++++++++++++++++++++++CONNECTION DSD2: " + connection);
        }
        final List<String> jeeVersions = getJeeVersions();
        return Response.ok().entity(Arrays.toString(jeeVersions.toArray())).build();
    }


    private List<String> getJeeVersions() throws Exception {
        LOG.log(INFO, "getJeeVersions()");
        try (Connection conn = dsd1.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select name from \"JEEVersion\"") //
        ) {

            final List<String> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rs.getString(1));
            }
            return results;
        }
    }
}
