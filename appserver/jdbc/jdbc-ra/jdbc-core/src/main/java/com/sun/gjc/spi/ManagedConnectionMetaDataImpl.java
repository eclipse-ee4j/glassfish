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

import com.sun.logging.LogDomains;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnectionMetaData;
import jakarta.resource.spi.security.PasswordCredential;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

/**
 * <code>ManagedConnectionMetaData</code> implementation for Generic JDBC
 * Connector.
 *
 * @author Evani Sai Surya Kiran
 * @version 1.0, 02/08/03
 */
public class ManagedConnectionMetaDataImpl implements ManagedConnectionMetaData {

    private static Logger _logger = LogDomains.getLogger(ManagedConnectionMetaDataImpl.class, LogDomains.RSR_LOGGER);

    private DatabaseMetaData databaseMetaData;
    private ManagedConnectionImpl managedConnection;

    /**
     * Constructor for <code>ManagedConnectionMetaDataImpl</code>
     *
     * @param managedConnection <code>ManagedConnection</code>
     * @throws <code>ResourceException</code> if getting the DatabaseMetaData object
     * fails
     */
    public ManagedConnectionMetaDataImpl(ManagedConnectionImpl managedConnection) throws ResourceException {
        try {
            this.managedConnection = managedConnection;
            databaseMetaData = managedConnection.getActualConnection().getMetaData();
        } catch (SQLException sqle) {
            _logger.log(SEVERE, "jdbc.exc_md", sqle);
            throw new ResourceException(sqle.getMessage(), sqle);
        }
    }

    /**
     * Returns product name of the underlying EIS instance connected through the
     * ManagedConnection.
     *
     * @return Product name of the EIS instance
     * @throws <code>ResourceException</code>
     */
    public String getEISProductName() throws ResourceException {
        try {
            return databaseMetaData.getDatabaseProductName();
        } catch (SQLException sqle) {
            _logger.log(SEVERE, "jdbc.exc_eis_prodname", sqle);
            throw new ResourceException(sqle.getMessage(), sqle);
        }
    }

    /**
     * Returns product version of the underlying EIS instance connected through the
     * ManagedConnection.
     *
     * @return Product version of the EIS instance
     * @throws <code>ResourceException</code>
     */
    public String getEISProductVersion() throws ResourceException {
        try {
            return databaseMetaData.getDatabaseProductVersion();
        } catch (SQLException sqle) {
            _logger.log(SEVERE, "jdbc.exc_eis_prodvers", sqle);
            throw new ResourceException(sqle.getMessage(), sqle.getMessage());
        }
    }

    /**
     * Returns maximum limit on number of active concurrent connections that an EIS
     * instance can support across client processes.
     *
     * @return Maximum limit for number of active concurrent connections
     * @throws <code>ResourceException</code>
     */
    public int getMaxConnections() throws ResourceException {
        try {
            return databaseMetaData.getMaxConnections();
        } catch (SQLException sqle) {
            _logger.log(SEVERE, "jdbc.exc_eis_maxconn");
            throw new ResourceException(sqle.getMessage(), sqle);
        }
    }

    /**
     * Returns name of the user associated with the ManagedConnection instance. The
     * name corresponds to the resource principal under whose whose security
     * context, a connection to the EIS instance has been established.
     *
     * @return name of the user
     * @throws <code>ResourceException</code>
     */
    public String getUserName() throws ResourceException {
        PasswordCredential passwordCredential = managedConnection.getPasswordCredential();
        if (passwordCredential != null) {
            return passwordCredential.getUserName();
        }

        return managedConnection.getManagedConnectionFactory().getUser();
    }
}
