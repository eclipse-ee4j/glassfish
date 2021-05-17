/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.appserv.jdbcra;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The <code>javax.sql.DataSource</code> implementation of SunONE application
 * server will implement this interface. An application program would be able
 * to use this interface to do the extended functionality exposed by SunONE
 * application server.
 * <p>A sample code for getting driver's connection implementation would like
 * the following.
 * <pre>
     InitialContext ic = new InitialContext();
     com.sun.appserv.DataSource ds = (com.sun.appserv.DataSOurce) ic.lookup("jdbc/PointBase");
     Connection con = ds.getConnection();
     Connection drivercon = ds.getConnection(con);

     // Do db operations.

     con.close();
   </pre>
 *
 * @author Binod P.G
 */
public interface DataSource extends javax.sql.DataSource {

    /**
     * Retrieves the actual SQLConnection from the Connection wrapper
     * implementation of SunONE application server. If an actual connection is
     * supplied as argument, then it will be just returned.
     *
     * @param con Connection obtained from <code>Datasource.getConnection()</code>
     * @return <code>java.sql.Connection</code> implementation of the driver.
     * @throws <code>java.sql.SQLException</code> If connection cannot be obtained.
     */
    public Connection getConnection(Connection con) throws SQLException;

}
