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

package com.sun.jdo.spi.persistence.support.sqlstore.database;

import com.sun.jdo.api.persistence.support.SpecialDBOperation;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * BaseSpecialDBOperation is the base class for all classes implementing
 * DBSpecificOperation.
 * @author Shing Wai Chan
 */
public class BaseSpecialDBOperation implements SpecialDBOperation {
    /**
     * @inheritDoc
     */
    public void initialize(DatabaseMetaData metaData,
        String identifier) throws SQLException {
    }

    /**
     * @inheritDoc
     */
    public void defineColumnTypeForResult(
        PreparedStatement ps, List columns) throws SQLException {
    }

    /**
     * @inheritDoc
     */
    public void bindFixedCharColumn(PreparedStatement ps,
         int index, String strVal, int length) throws SQLException {
         ps.setString(index, strVal);
    }

}
