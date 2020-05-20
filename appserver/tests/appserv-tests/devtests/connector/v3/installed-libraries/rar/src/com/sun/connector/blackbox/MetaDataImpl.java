/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.connector.blackbox;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.EISSystemException;
import jakarta.resource.spi.IllegalStateException;
import jakarta.resource.spi.ManagedConnectionMetaData;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Tony Ng
 */
public class MetaDataImpl implements ManagedConnectionMetaData {

    private JdbcManagedConnection mc;

    public MetaDataImpl(JdbcManagedConnection mc) {
        this.mc = mc;
    }

    public String getEISProductName() throws ResourceException {
        try {
            Connection con = mc.getJdbcConnection();
            return con.getMetaData().getDatabaseProductName();
        } catch (SQLException ex) {
            ResourceException re = new EISSystemException(ex.getMessage());
            re.setLinkedException(ex);
            throw re;
        }
    }

    public String getEISProductVersion() throws ResourceException {
        try {
            Connection con = mc.getJdbcConnection();
            return con.getMetaData().getDatabaseProductVersion();
        } catch (SQLException ex) {
            ResourceException re = new EISSystemException(ex.getMessage());
            re.setLinkedException(ex);
            throw re;
        }
    }


    public int getMaxConnections() throws ResourceException {
        try {
            Connection con = mc.getJdbcConnection();
            return con.getMetaData().getMaxConnections();
        } catch (SQLException ex) {
            ResourceException re = new EISSystemException(ex.getMessage());
            re.setLinkedException(ex);
            throw re;
        }
    }

    public String getUserName() throws ResourceException {
        if (mc.isDestroyed()) {
            throw new IllegalStateException
                    ("ManagedConnection has been destroyed");
        }

        return mc.getPasswordCredential() == null
                ? null
                : mc.getPasswordCredential().getUserName();
    }

}

