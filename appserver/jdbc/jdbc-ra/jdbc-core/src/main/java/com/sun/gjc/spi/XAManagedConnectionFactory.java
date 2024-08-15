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

package com.sun.gjc.spi;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.gjc.common.DataSourceObjectBuilder;
import com.sun.gjc.common.DataSourceSpec;
import com.sun.gjc.spi.base.AbstractDataSource;
import com.sun.gjc.spi.base.ConnectionHolder;
import com.sun.logging.LogDomains;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConfigProperty;
import jakarta.resource.spi.ConnectionDefinition;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ResourceAllocationException;
import jakarta.resource.spi.security.PasswordCredential;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import static com.sun.gjc.spi.ManagedConnectionImpl.ISXACONNECTION;
import static com.sun.gjc.util.SecurityUtils.getPasswordCredential;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;

/**
 * XA <code>ManagedConnectionFactory</code> implementation for Generic JDBC
 * Connector.
 *
 * @author Evani Sai Surya Kiran
 * @version 1.0, 02/07/27
 */
@ConnectionDefinition(
    connectionFactory = DataSource.class,
    connectionFactoryImpl = AbstractDataSource.class,
    connection = Connection.class,
    connectionImpl = ConnectionHolder.class
)
public class XAManagedConnectionFactory extends ManagedConnectionFactoryImpl {

    private transient javax.sql.XADataSource xaDataSourceObj;

    private static Logger _logger = LogDomains.getLogger(XAManagedConnectionFactory.class, LogDomains.RSR_LOGGER);

    /**
     * Creates a new physical connection to the underlying EIS resource manager.
     *
     * @param subject <code>Subject</code> instance passed by the application server
     * @param connectionRequestInfo <code>ConnectionRequestInfo</code> which may be created
     * as a result of the invocation <code>getConnection(user, password)</code> on
     * the <code>DataSource</code> object
     *
     * @return <code>ManagedConnection</code> object created
     * @throws ResourceException if there is an error in instantiating the
     * <code>DataSource</code> object used for the creation of the
     * <code>ManagedConnection</code> object
     * @throws SecurityException if there ino <code>PasswordCredential</code> object
     * satisfying this request
     * @throws ResourceAllocationException if there is an error in allocating the
     * physical connection
     */
    public jakarta.resource.spi.ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        logFine("In createManagedConnection");
        PasswordCredential passwordCredential = getPasswordCredential(this, subject, connectionRequestInfo);
        XADataSource dataSource = getDataSource();

        XAConnection xaConnection = null;
        ManagedConnectionImpl managedConnection = null;

        try {
            /*
             * For the case where the user/passwd of the connection pool is equal to the
             * PasswordCredential for the connection request get a connection from this pool
             * directly. for all other conditions go create a new connection
             */
            if (isEqual(passwordCredential, getUser(), getPassword())) {
                xaConnection = dataSource.getXAConnection();
            } else {
                xaConnection = dataSource.getXAConnection(
                                passwordCredential.getUserName(),
                                new String(passwordCredential.getPassword()));
            }

        } catch (SQLException sqle) {
            _logger.log(FINE, "jdbc.exc_create_xa_conn", sqle);
            StringManager sm = StringManager.getManager(DataSourceObjectBuilder.class);
            String msg = sm.getString("jdbc.cannot_allocate_connection", sqle.getMessage());
            throw new ResourceAllocationException(msg, sqle);
        }

        try {
            managedConnection = constructManagedConnection(xaConnection, null, passwordCredential, this);
            managedConnection.initializeConnectionType(ISXACONNECTION);
            // GJCINT
            validateAndSetIsolation(managedConnection);
        } finally {
            if (managedConnection == null) {
                if (xaConnection != null) {
                    try {
                        xaConnection.close();
                    } catch (SQLException e) {
                        _logger.log(FINEST,
                            "Exception while closing connection : createManagedConnection" + xaConnection);
                    }
                }
            }
        }

        return managedConnection;
    }

    /**
     * Returns the underlying datasource
     *
     * @return DataSource of jdbc vendor
     * @throws ResourceException
     */
    public javax.sql.XADataSource getDataSource() throws ResourceException {
        if (xaDataSourceObj == null) {
            try {
                xaDataSourceObj = (XADataSource) super.getDataSource();
            } catch (ClassCastException cce) {
                _logger.log(SEVERE, "jdbc.exc_cce_XA", cce);
                throw new ResourceException(cce.getMessage());
            }
        }

        return xaDataSourceObj;
    }

    /**
     * Sets the class name of the data source
     *
     * @param className <code>String</code>
     */
    @ConfigProperty(type = String.class, defaultValue = "org.apache.derby.jdbc.ClientXADataSource")
    @Override
    public void setClassName(String className) {
        spec.setDetail(DataSourceSpec.CLASSNAME, className);
    }

    /**
     * Sets the max statements.
     *
     * @param maxStmts <code>String</code>
     * @see <code>getMaxStatements</code>
     */
    public void setMaxStatements(String maxStmts) {
        spec.setDetail(DataSourceSpec.MAXSTATEMENTS, maxStmts);
    }

    /**
     * Gets the max statements.
     *
     * @return maxStmts
     * @see <code>setMaxStatements</code>
     */
    public String getMaxStatements() {
        return spec.getDetail(DataSourceSpec.MAXSTATEMENTS);
    }

    /**
     * Sets the initial pool size.
     *
     * @param initPoolSz <code>String</code>
     * @see <code>getInitialPoolSize</code>
     */
    public void setInitialPoolSize(String initPoolSz) {
        spec.setDetail(DataSourceSpec.INITIALPOOLSIZE, initPoolSz);
    }

    /**
     * Gets the initial pool size.
     *
     * @return initPoolSz
     * @see <code>setInitialPoolSize</code>
     */
    public String getInitialPoolSize() {
        return spec.getDetail(DataSourceSpec.INITIALPOOLSIZE);
    }

    /**
     * Sets the minimum pool size.
     *
     * @param minPoolSz <code>String</code>
     * @see <code>getMinPoolSize</code>
     */
    public void setMinPoolSize(String minPoolSz) {
        spec.setDetail(DataSourceSpec.MINPOOLSIZE, minPoolSz);
    }

    /**
     * Gets the minimum pool size.
     *
     * @return minPoolSz
     * @see <code>setMinPoolSize</code>
     */
    public String getMinPoolSize() {
        return spec.getDetail(DataSourceSpec.MINPOOLSIZE);
    }

    /**
     * Sets the maximum pool size.
     *
     * @param maxPoolSz <code>String</code>
     * @see <code>getMaxPoolSize</code>
     */
    public void setMaxPoolSize(String maxPoolSz) {
        spec.setDetail(DataSourceSpec.MAXPOOLSIZE, maxPoolSz);
    }

    /**
     * Gets the maximum pool size.
     *
     * @return maxPoolSz
     * @see <code>setMaxPoolSize</code>
     */
    public String getMaxPoolSize() {
        return spec.getDetail(DataSourceSpec.MAXPOOLSIZE);
    }

    /**
     * Sets the maximum idle time.
     *
     * @param maxIdleTime String
     * @see <code>getMaxIdleTime</code>
     */
    public void setMaxIdleTime(String maxIdleTime) {
        spec.setDetail(DataSourceSpec.MAXIDLETIME, maxIdleTime);
    }

    /**
     * Gets the maximum idle time.
     *
     * @return maxIdleTime
     * @see <code>setMaxIdleTime</code>
     */
    public String getMaxIdleTime() {
        return spec.getDetail(DataSourceSpec.MAXIDLETIME);
    }

    /**
     * Sets the property cycle.
     *
     * @param propCycle <code>String</code>
     * @see <code>getPropertyCycle</code>
     */
    public void setPropertyCycle(String propCycle) {
        spec.setDetail(DataSourceSpec.PROPERTYCYCLE, propCycle);
    }

    /**
     * Gets the property cycle.
     *
     * @return propCycle
     * @see <code>setPropertyCycle</code>
     */
    public String getPropertyCycle() {
        return spec.getDetail(DataSourceSpec.PROPERTYCYCLE);
    }

    /**
     * Check if this <code>ManagedConnectionFactory</code> is equal to another
     * <code>ManagedConnectionFactory</code>.
     *
     * @param other <code>ManagedConnectionFactory</code> object for checking
     * equality with
     * @return true if the property sets of both the
     * <code>ManagedConnectionFactory</code> objects are the same false otherwise
     */
    public boolean equals(Object other) {
        logFine("In equals");
        /**
         * The check below means that two ManagedConnectionFactory objects are equal if
         * and only if their properties are the same.
         */
        if (other instanceof XAManagedConnectionFactory) {
            XAManagedConnectionFactory otherMCF = (XAManagedConnectionFactory) other;
            return this.spec.equals(otherMCF.spec);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return 31 * 7 + (spec.hashCode());
    }
}
