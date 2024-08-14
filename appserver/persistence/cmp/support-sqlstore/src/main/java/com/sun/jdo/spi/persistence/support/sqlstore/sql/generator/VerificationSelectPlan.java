/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jdo.spi.persistence.support.sqlstore.sql.generator;

import com.sun.jdo.spi.persistence.support.sqlstore.PersistenceManager;
import com.sun.jdo.spi.persistence.support.sqlstore.RetrieveDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.SQLStoreManager;
import com.sun.jdo.spi.persistence.support.sqlstore.model.LocalFieldDesc;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Implements Select Plan for verifying clean VC instanses.
 * @author Mitesh Meswani
 */
public class VerificationSelectPlan extends SelectQueryPlan {

    /**
     * Creates a new VerificationSelectQueryPlan.
     *
     * @param desc The Retrieve descriptor holding constraints
     * @param store Store manager executing the query.
     */
    public VerificationSelectPlan(RetrieveDesc desc, SQLStoreManager store) {
        super(desc, store, null);
    }

    /**
     * There are no real fields to be selected for verification query.
     * This method just adds the tables for the version field.
     */
    protected void processFields() {
        LocalFieldDesc[] versionFields = config.getVersionFields();
        for (int i = 0; i < versionFields.length; i++) {
            LocalFieldDesc versionField = versionFields[i];
            addTable(versionField);
        }
    }

    protected Statement newStatement() {
        return new SelectOneStatement(store.getVendorType(), this);
    }

    /**
     * Checks whether the resultset from a verification query contains atleast
     * one row.
     * @param pm This parameter is not used.
     * @param resultData The resultset containing result from the verification query
     * @return true if the resultset contains atleast one row false otherwise.
     * @throws SQLException
     */
    public Object getResult(PersistenceManager pm, ResultSet resultData)
            throws SQLException{
        boolean verificationSuccessfull = resultData.next();
        return Boolean.valueOf(verificationSuccessfull);
    }

}
