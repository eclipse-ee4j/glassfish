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
import com.sun.gjc.spi.base.AbstractDataSource;
import com.sun.gjc.spi.base.ConnectionHolder;
import com.sun.logging.LogDomains;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionDefinition;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ResourceAllocationException;
import jakarta.resource.spi.security.PasswordCredential;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.sql.DataSource;

import static com.sun.gjc.util.SecurityUtils.getPasswordCredential;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;

/**
 * Data Source <code>ManagedConnectionFactory</code> implementation for Generic JDBC Connector.
 *
 * @author Evani Sai Surya Kiran
 * @version 1.0, 02/07/30
 */

@ConnectionDefinition(
    connectionFactory = DataSource.class,
    connectionFactoryImpl = AbstractDataSource.class,
    connection = Connection.class,
    connectionImpl = ConnectionHolder.class
)
public class DSManagedConnectionFactory extends ManagedConnectionFactoryImpl {

    private static Logger _logger = LogDomains.getLogger(DSManagedConnectionFactory.class, LogDomains.RSR_LOGGER);

    private transient DataSource dataSourceObj;

    /**
     * Creates a new physical connection to the underlying EIS resource manager.
     *
     * @param subject <code>Subject</code> instance passed by the application server
     * @param connectionRequestInfo <code>ConnectionRequestInfo</code> which may be created
     * as a result of the invocation <code>getConnection(user, password)</code> on
     * the <code>DataSource</code> object
     *
     * @return <code>ManagedConnection</code> object created
     *
     * @throws ResourceException if there is an error in instantiating the
     * <code>DataSource</code> object used for the creation of the
     * <code>ManagedConnection</code> object
     * @throws SecurityException if there ino <code>PasswordCredential</code> object
     * satisfying this request
     * @throws ResourceAllocationException if there is an error in allocating the
     * physical connection
     */
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        logFine("In createManagedConnection");
        PasswordCredential passwordCredential = getPasswordCredential(this, subject, connectionRequestInfo);

        DataSource dataSource = getDataSource();

        Connection connection = null;
        ManagedConnectionImpl managedConnectionImpl = null;

        try {

            /*
             * For the case where the user/passwd of the connection pool is equal to the
             * PasswordCredential for the connection request get a connection from this pool
             * directly. for all other conditions go create a new connection
             */
            if (isEqual(passwordCredential, getUser(), getPassword())) {
                connection = dataSource.getConnection();
            } else {
                connection = dataSource.getConnection(passwordCredential.getUserName(), new String(passwordCredential.getPassword()));
            }
        } catch (SQLException sqle) {
            _logger.log(FINE, "jdbc.exc_create_conn", sqle.getMessage());

            throw new ResourceAllocationException(
                StringManager.getManager(DataSourceObjectBuilder.class).getString("jdbc.cannot_allocate_connection", sqle.getMessage()),
                sqle);
        }

        try {
            managedConnectionImpl = constructManagedConnection(null, connection, passwordCredential, this);

            validateAndSetIsolation(managedConnectionImpl);
        } finally {
            if (managedConnectionImpl == null) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        _logger.log(FINEST,
                            "Exception while closing connection : " + "createManagedConnection" + connection);
                    }
                }
            }
        }

        return managedConnectionImpl;
    }

    /**
     * Returns the underlying datasource
     *
     * @return DataSource of jdbc vendor
     * @throws ResourceException
     */
    public DataSource getDataSource() throws ResourceException {
        if (dataSourceObj == null) {
            try {
                dataSourceObj = (DataSource) super.getDataSource();
            } catch (ClassCastException cce) {
                _logger.log(SEVERE, "jdbc.exc_cce", cce);
                throw new ResourceException(cce.getMessage());
            }
        }

        return dataSourceObj;
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
        if (other instanceof DSManagedConnectionFactory) {
            DSManagedConnectionFactory otherMCF = (DSManagedConnectionFactory) other;
            return this.spec.equals(otherMCF.spec);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return 31 * 7 + (spec.hashCode());
    }

}
