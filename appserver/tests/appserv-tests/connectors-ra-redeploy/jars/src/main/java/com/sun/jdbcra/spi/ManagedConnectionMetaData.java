/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.jdbcra.spi;

import jakarta.resource.ResourceException;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * <code>ManagedConnectionMetaData</code> implementation for Generic JDBC Connector.
 *
 * @version        1.0, 02/08/03
 * @author        Evani Sai Surya Kiran
 */
public class ManagedConnectionMetaData implements jakarta.resource.spi.ManagedConnectionMetaData {

    private java.sql.DatabaseMetaData dmd = null;
    private ManagedConnection mc;

    private static Logger _logger;
    static {
        _logger = Logger.getAnonymousLogger();
    }

    /**
     * Constructor for <code>ManagedConnectionMetaData</code>
     *
     * @param        mc        <code>ManagedConnection</code>
     * @throws        <code>ResourceException</code>        if getting the DatabaseMetaData object fails
     */
    public ManagedConnectionMetaData(ManagedConnection mc) throws ResourceException {
        try {
            this.mc = mc;
            dmd = mc.getActualConnection().getMetaData();
        } catch(SQLException sqle) {
            _logger.log(Level.SEVERE, "jdbc.exc_md");
            throw new ResourceException(sqle.getMessage());
        }
    }

    /**
     * Returns product name of the underlying EIS instance connected
     * through the ManagedConnection.
     *
     * @return        Product name of the EIS instance
     * @throws        <code>ResourceException</code>
     */
    @Override
    public String getEISProductName() throws ResourceException {
        try {
            return dmd.getDatabaseProductName();
        } catch(SQLException sqle) {
            _logger.log(Level.SEVERE, "jdbc.exc_eis_prodname", sqle);
            throw new ResourceException(sqle.getMessage());
        }
    }

    /**
     * Returns product version of the underlying EIS instance connected
     * through the ManagedConnection.
     *
     * @return        Product version of the EIS instance
     * @throws        <code>ResourceException</code>
     */
    @Override
    public String getEISProductVersion() throws ResourceException {
        try {
            return dmd.getDatabaseProductVersion();
        } catch(SQLException sqle) {
            _logger.log(Level.SEVERE, "jdbc.exc_eis_prodvers", sqle);
            throw new ResourceException(sqle.getMessage(), sqle.getMessage());
        }
    }

    /**
     * Returns maximum limit on number of active concurrent connections
     * that an EIS instance can support across client processes.
     *
     * @return        Maximum limit for number of active concurrent connections
     * @throws        <code>ResourceException</code>
     */
    @Override
    public int getMaxConnections() throws ResourceException {
        try {
            return dmd.getMaxConnections();
        } catch(SQLException sqle) {
            _logger.log(Level.SEVERE, "jdbc.exc_eis_maxconn");
            throw new ResourceException(sqle.getMessage());
        }
    }

    /**
     * Returns name of the user associated with the ManagedConnection instance. The name
     * corresponds to the resource principal under whose whose security context, a connection
     * to the EIS instance has been established.
     *
     * @return        name of the user
     * @throws        <code>ResourceException</code>
     */
    @Override
    public String getUserName() throws ResourceException {
        jakarta.resource.spi.security.PasswordCredential pc = mc.getPasswordCredential();
        if(pc != null) {
            return pc.getUserName();
        }

        return mc.getManagedConnectionFactory().getUser();
    }
}
