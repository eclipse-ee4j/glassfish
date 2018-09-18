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

/*
 * ResultFieldDesc.java
 *
 * Created on October 15, 2001
 *
 */

package com.sun.jdo.spi.persistence.support.sqlstore.sql;

import com.sun.jdo.spi.persistence.support.sqlstore.model.LocalFieldDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.sql.generator.ColumnRef;

/**
 * This class is used to associated a field to the position of a column
 * in a JDBC resultset.
 */
public class ResultFieldDesc extends Object {
    /**
     * field descriptor for the field that is the recipient of the column value
     * from the resultset
     */
    private LocalFieldDesc fieldDesc;

    /** holds the index to the column in the resultset. */
    private ColumnRef columnRef;

    public ResultFieldDesc(LocalFieldDesc fieldDesc, ColumnRef columnRef) {
        this.fieldDesc = fieldDesc;
        this.columnRef = columnRef;
        //this.projection = projection;
    }

    /** Return the field descriptor.
     *  @return the field descriptor
     */
    public LocalFieldDesc getFieldDesc() {
        return fieldDesc;
    }

    /** Return the column reference
     *  @return the column reference
     */
    public ColumnRef getColumnRef() {
        return columnRef;
    }

}
