/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
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

package com.sun.gjc.spi;

import com.sun.gjc.spi.base.ConnectionHolder;
import com.sun.gjc.util.SQLTraceDelegator;
import com.sun.logging.LogDomains;

import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.glassfish.api.jdbc.SQLTraceRecord;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

/**
 * Factory to create JDBC objects
 */
public abstract class JdbcObjectsFactory implements Serializable {

    private static final long serialVersionUID = 1L;
    protected final static Logger _logger = LogDomains.getLogger(JdbcObjectsFactory.class, LogDomains.RSR_LOGGER);

    /**
     * Returns JDBC Object Factory
     *
     * @return JdbcObjectsFactory
     */
    public static JdbcObjectsFactory getInstance() {
        try {
            return(JdbcObjectsFactory)
                Class.forName("com.sun.gjc.spi.jdbc40.Jdbc40ObjectsFactory")
                     .getDeclaredConstructor()
                     .newInstance();
        } catch (Exception e) {
            _logger.log(WARNING, "jdbc.jdbc_factory_class_load_exception", e);
            return null;
        }
    }

    /**
     * Returns a DataSource instance.
     *
     * @param mcfObject Managed Connection Factory
     * @param cmObject Connection Manager
     * @return DataSource
     */
    public abstract DataSource getDataSourceInstance(ManagedConnectionFactoryImpl mcfObject, ConnectionManager cmObject);

    /**
     * To get an instance of ConnectionHolder.<br>
     * Will return a ConnectionHolder with or without wrapper<br>
     *
     * @param conObject Connection
     * @param mcObject ManagedConnection
     * @param criObject Connection Request Info
     * @param statementWrapping Whether to wrap statement objects or not.
     * @return ConnectionHolder
     */
    public abstract ConnectionHolder getConnection(Connection conObject, ManagedConnectionImpl mcObject,
            ConnectionRequestInfo criObject, boolean statementWrapping,
            SQLTraceDelegator sqlTraceDelegator);

    protected Connection getProxiedConnection(final Object conObject, Class[] connIntf, final SQLTraceDelegator sqlTraceDelegator) {
        Connection proxiedConn = null;
        try {
            proxiedConn = (Connection) getProxyObject(conObject, connIntf, sqlTraceDelegator);
        } catch (Exception ex) {
            _logger.log(SEVERE, "jdbc.jdbc_proxied_connection_get_exception", ex.getMessage());
        }

        return proxiedConn;
    }

    protected <T> T getProxyObject(final Object actualObject, Class<T>[] ifaces, final SQLTraceDelegator sqlTraceDelegator) throws Exception {
        InvocationHandler invocationHandler = new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                SQLTraceRecord record = new SQLTraceRecord();
                record.setMethodName(method.getName());
                record.setParams(args);
                record.setClassName(actualObject.getClass().getName());
                record.setThreadName(Thread.currentThread().getName());
                record.setThreadID(Thread.currentThread().getId());
                record.setTimeStamp(System.currentTimeMillis());
                sqlTraceDelegator.sqlTrace(record);

                return method.invoke(actualObject, args);
            }
        };

        return (T) Proxy.newProxyInstance(actualObject.getClass().getClassLoader(), ifaces, invocationHandler);
    }

}
