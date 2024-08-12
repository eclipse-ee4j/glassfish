/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.gjc.spi.jdbc40;

import com.sun.gjc.spi.JdbcObjectsFactory;
import com.sun.gjc.spi.ManagedConnectionFactoryImpl;
import com.sun.gjc.spi.ManagedConnectionImpl;
import com.sun.gjc.spi.base.ConnectionHolder;
import com.sun.gjc.util.SQLTraceDelegator;

import jakarta.resource.spi.ConnectionManager;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.logging.Level;

import javax.sql.DataSource;

import static java.lang.reflect.Modifier.isAbstract;

/**
 * Factory to create jdbc40 connection & datasource
 */
public class Jdbc40ObjectsFactory extends JdbcObjectsFactory {

    private static final long serialVersionUID = 1L;

    // indicates whether JDBC 3.0 Connection (and hence JDBC 3.0 DataSource) is used
    private boolean jdbc30Connection;

    // indicates whether detection of JDBC 3.0 Datasource in JDK 1.6 is done or not
    private boolean initJDBC30Connection;

    /**
     * To get an instance of ConnectionHolder40.<br>
     * Will return a ConnectionHolder40 with or without wrapper<br>
     *
     * @param conObject Connection
     * @param mcObject ManagedConnection
     * @param criObject Connection Request Info
     * @param statementWrapping Whether to wrap statement objects or not.
     * @return ConnectionHolder
     */
    @Override
    public ConnectionHolder getConnection(Connection conObject, ManagedConnectionImpl mcObject,
            jakarta.resource.spi.ConnectionRequestInfo criObject, boolean statementWrapping, SQLTraceDelegator sqlTraceDelegator) {
        ConnectionHolder connection = null;
        if (!initJDBC30Connection) {
            detectJDBC30Connection(conObject, mcObject);
        }

        if (statementWrapping) {
            if (sqlTraceDelegator != null) {
                Class<?>[] connIntf = new Class[] { Connection.class };
                Connection proxiedConn = getProxiedConnection(conObject, connIntf, sqlTraceDelegator);
                connection = new ProfiledConnectionWrapper40(proxiedConn, mcObject, criObject, jdbc30Connection, sqlTraceDelegator);
            } else {
                connection = new ConnectionWrapper40(conObject, mcObject, criObject, jdbc30Connection);
            }
        } else {
            connection = new ConnectionHolder40(conObject, mcObject, criObject, jdbc30Connection);
        }
        return connection;
    }

    /**
     * Returns a DataSource instance for JDBC 4.0
     *
     * @param mcfObject Managed Connection Factory
     * @param cmObject Connection Manager
     * @return DataSource
     */
    @Override
    public DataSource getDataSourceInstance(ManagedConnectionFactoryImpl mcfObject, ConnectionManager cmObject) {
        return new DataSource40(mcfObject, cmObject);
    }

    public boolean isJdbc30Connection() {
        return jdbc30Connection;
    }

    public void setJdbc30Connection(boolean jdbc30Connection) {
        this.jdbc30Connection = jdbc30Connection;
    }

    public boolean isJDBC30ConnectionDetected() {
        return initJDBC30Connection;
    }

    public void detectJDBC30Connection(Connection con, ManagedConnectionImpl mcObject) {
        String dataSourceProperty = mcObject.getManagedConnectionFactory().getJdbc30DataSource();
        if (dataSourceProperty != null) {
            setJdbc30Connection(Boolean.valueOf(dataSourceProperty));
            initJDBC30Connection = true;
        } else {
            try {
                Class<?>[] paramClasses = new Class[] { Class.class };

                Method isWrapperMethod = con.getClass().getMethod("isWrapperFor", paramClasses);
                setJdbc30Connection(isAbstract(isWrapperMethod.getModifiers()));
            } catch (NoSuchMethodException | AbstractMethodError e) {
                setJdbc30Connection(true);
            } catch (Throwable t) {
                setJdbc30Connection(true);
                _logger.log(Level.WARNING, "jdbc.unexpected_exception_on_detecting_jdbc_version", t);
            } finally {
                initJDBC30Connection = true;
            }
        }
    }
}
