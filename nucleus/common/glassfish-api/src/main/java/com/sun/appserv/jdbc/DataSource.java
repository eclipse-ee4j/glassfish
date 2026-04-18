/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.appserv.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The <code>javax.sql.DataSource</code> implementation of SunONE application server will implement this interface. An
 * application program would be able to use this interface to do the extended functionality exposed by SunONE
 * application server.
 * <p>
 * A sample code for getting driver's connection implementation would like the following.
 *
 * <pre>
 * InitialContext ic = new InitialContext();
 * com.sun.appserv.DataSource ds = (com.sun.appserv.DataSOurce) ic.lookup("jdbc/PointBase");
 * Connection con = ds.getConnection();
 * Connection drivercon = ds.getConnection(con);
 *
 * // Do db operations.
 *
 * con.close();
 * </pre>
 *
 * @author Binod P.G
 */
public interface DataSource extends javax.sql.DataSource {

    /**
     * Retrieves the actual SQLConnection from the Connection wrapper implementation of SunONE application server. If an
     * actual connection is supplied as argument, then it will be just returned.
     *
     * @param con {@link Connection} obtained from <code>Datasource.getConnection()</code>
     * @return {@link Connection} implementation of the driver.
     * @throws SQLException If connection cannot be obtained.
     */
    Connection getConnection(Connection con) throws SQLException;

    /**
     * Gets a connection that is not in the scope of any transaction. This can be used to save performance overhead incurred
     * on enlisting/delisting each connection got, irrespective of whether its required or not. Note here that this meethod
     * does not fit in the connector contract per se.
     *
     * @return {@link Connection}
     * @throws SQLException If connection cannot be obtained
     */
    Connection getNonTxConnection() throws SQLException;

    /**
     * Gets a connection that is not in the scope of any transaction. This can be used to save performance overhead incurred
     * on enlisting/delisting each connection got, irrespective of whether its required or not. Note here that this meethod
     * does not fit in the connector contract per se.
     *
     * @param userName User name for authenticating the connection
     * @param password Password for authenticating the connection
     * @return {@link Connection}
     * @throws SQLException If connection cannot be obtained
     */
    Connection getNonTxConnection(String userName, String password) throws SQLException;

    /**
     * API to mark a connection as bad. If the application can determine that the connection is bad,
     * using this api, it can notify the resource-adapter which inturn will notify the
     * connection-pool. Connection-pool will drop and create a new connection. eg:
     *
     * <pre>{@code
     * com.sun.appserv.jdbc.DataSource ds = (com.sun.appserv.jdbc.DataSource) context.lookup("dataSource");
     * Connection con = ds.getConnection();
     * Statement stmt = null;
     * try ({
     *     stmt = con.createStatement();
     *     stmt.executeUpdate("Update");
     * } catch (BadConnectionException e) {
     *     //marking it as bad for removal
     *     dataSource.markConnectionAsBad(con)
     * } finally {
     *     stmt.close();
     *     //Connection will be destroyed while close or Tx completion
     *     con.close();
     * }
     * }</pre>
     *
     * @param conn {@link Connection}
     */
    void markConnectionAsBad(Connection conn);

}
