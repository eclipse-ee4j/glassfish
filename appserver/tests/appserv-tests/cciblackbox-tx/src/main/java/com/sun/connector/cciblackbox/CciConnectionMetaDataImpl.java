/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.connector.cciblackbox;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.ConnectionMetaData;
import jakarta.resource.spi.EISSystemException;

import java.sql.SQLException;

/**
 * @author Sheetal Vartak
 */
public class CciConnectionMetaDataImpl implements ConnectionMetaData {

    private final CciManagedConnection mc;

    public CciConnectionMetaDataImpl(CciManagedConnection mc) {
        this.mc = mc;
    }


    @Override
    public String getEISProductName() throws ResourceException {
        try {
            java.sql.Connection con = mc.getJdbcConnection();
            return con.getMetaData().getDatabaseProductName();
        } catch (SQLException ex) {
            throw new EISSystemException(ex.getMessage(), ex);
        }
    }


    @Override
    public String getEISProductVersion() throws ResourceException {
        try {
            java.sql.Connection con = mc.getJdbcConnection();
            return con.getMetaData().getDatabaseProductVersion();
        } catch (SQLException ex) {
            throw new EISSystemException(ex.getMessage(), ex);
        }
    }


    @Override
    public String getUserName() throws ResourceException {
        if (mc.isDestroyed()) {
            throw new ResourceException("ManagedConnection has been destroyed");
        }
        return mc.getPasswordCredential().getUserName();
    }

}
