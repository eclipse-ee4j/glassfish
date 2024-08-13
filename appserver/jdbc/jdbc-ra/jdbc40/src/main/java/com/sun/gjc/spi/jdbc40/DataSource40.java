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

package com.sun.gjc.spi.jdbc40;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.gjc.spi.ManagedConnectionFactoryImpl;
import com.sun.gjc.spi.base.AbstractDataSource;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Wrapper;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

/**
 * Holds the <code>java.sql.Connection</code> object, which is to be passed to
 * the application program.
 *
 * @author Binod P.G
 * @version 1.0, 02/07/31
 */
public class DataSource40 extends AbstractDataSource {

    private static final long serialVersionUID = 1L;
    protected final static StringManager localStrings = StringManager.getManager(ManagedConnectionFactoryImpl.class);

    /**
     * Constructs <code>DataSource</code> object. This is created by the
     * <code>ManagedConnectionFactory</code> object.
     *
     * @param mcf <code>ManagedConnectionFactory</code> object creating this object.
     * @param cm <code>ConnectionManager</code> object either associated with
     * Application server or Resource Adapter.
     */
    public DataSource40(ManagedConnectionFactoryImpl mcf, ConnectionManager cm) {
        super(mcf, cm);
    }

    /**
     * Returns an object that implements the given interface to allow access to
     * non-standard methods, or standard methods not exposed by the proxy.
     * <p/>
     * If the receiver implements the interface then the result is the receiver or a
     * proxy for the receiver. If the receiver is a wrapper and the wrapped object
     * implements the interface then the result is the wrapped object or a proxy for
     * the wrapped object. Otherwise return the the result of calling
     * <code>unwrap</code> recursively on the wrapped object or a proxy for that
     * result. If the receiver is not a wrapper and does not implement the
     * interface, then an <code>SQLException</code> is thrown.
     *
     * @param iface A Class defining an interface that the result must implement.
     * @return an object that implements the interface. May be a proxy for the
     * actual implementing object.
     * @throws java.sql.SQLException If no object found that implements the
     * interface
     * @since 1.6
     */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        T result;
        try {
            Object cds = managedConnectionFactoryImpl.getDataSource();

            if (iface.isInstance(cds)) {
                result = iface.cast(cds);
            } else if (cds instanceof java.sql.Wrapper) {
                result = ((java.sql.Wrapper) cds).unwrap(iface);
            } else {
                String msg = localStrings.getString("jdbc.feature_not_supported");
                throw new SQLException(msg);
            }
        } catch (ResourceException e) {
            _logger.log(WARNING, "jdbc.exc_unwrap", e);
            throw new SQLException(e);
        }

        return result;
    }

    /**
     * Returns true if this either implements the interface argument or is directly
     * or indirectly a wrapper for an object that does. Returns false otherwise. If
     * this implements the interface then return true, else if this is a wrapper
     * then return the result of recursively calling <code>isWrapperFor</code> on
     * the wrapped object. If this does not implement the interface and is not a
     * wrapper, return false. This method should be implemented as a low-cost
     * operation compared to <code>unwrap</code> so that callers can use this method
     * to avoid expensive <code>unwrap</code> calls that may fail. If this method
     * returns true then calling <code>unwrap</code> with the same argument should
     * succeed.
     *
     * @param iface a Class defining an interface.
     * @return true if this implements the interface or directly or indirectly wraps
     * an object that does.
     * @throws java.sql.SQLException if an error occurs while determining whether
     * this is a wrapper for an object with the given interface.
     * @since 1.6
     */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        boolean result = false;
        try {
            Object cds = managedConnectionFactoryImpl.getDataSource();

            if (iface.isInstance(cds)) {
                result = true;
            } else if (cds instanceof java.sql.Wrapper) {
                result = ((Wrapper) cds).isWrapperFor(iface);
            }
        } catch (ResourceException e) {
            _logger.log(WARNING, "jdbc.exc_is_wrapper", e);
            throw new SQLException(e);
        }
        return result;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        try {
            return (Logger) executor.invokeMethod(managedConnectionFactoryImpl.getDataSource().getClass(), "getParentLogger", null);
        } catch (ResourceException ex) {
            _logger.log(SEVERE, "jdbc.ex_get_parent_logger", ex);
            throw new SQLFeatureNotSupportedException(ex);
        }

    }
}
