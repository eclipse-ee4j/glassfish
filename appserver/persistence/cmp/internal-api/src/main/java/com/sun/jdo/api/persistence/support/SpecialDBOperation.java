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

package com.sun.jdo.api.persistence.support;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * SpecialDBOperation interface is defined for database specific operations.
 * @author Shing Wai Chan
 */
public interface SpecialDBOperation {

    /**
     * This method is called immediately after an instance implementing this
     * interface is created. The implementation can initialize itself using
     * supplied metaData.
     * @param metaData DatbaseMetaData of the database for which an instance
     * implementing this interface is ingratiated.
     * @param identifier Identifier of object used to obtain databaseMetaData.
     * This can be null in non managed environment.
     */
    public void initialize(DatabaseMetaData metaData,
        String identifier) throws SQLException;
    /**
     * Defines column type for result.
     * @param ps java.sql.PreparedStatement
     * @param columns List of ColumnElement corresponding to select clause
     */
    public void defineColumnTypeForResult(
        PreparedStatement ps, List columns) throws SQLException;

    /**
     * Binds specified value to parameter at specified index that is bound to
     * CHAR column.
     * @param ps java.sql.PreparedStatement
     * @param index Index of paramater marker in <code>ps</code>.
     * @param strVal value that needs to bound.
     * @param length length of the column to which strVal is bound.
     */
    public void bindFixedCharColumn(PreparedStatement ps,
         int index, String strVal, int length) throws SQLException;

}
