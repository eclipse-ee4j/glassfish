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
 * ColumnRef.java
 *
 * Create on March 3, 2000
 *
 */

package com.sun.jdo.spi.persistence.support.sqlstore.sql.generator;

import com.sun.jdo.api.persistence.support.FieldMapping;

import org.netbeans.modules.dbschema.ColumnElement;

/**
 */
public class ColumnRef extends Object implements FieldMapping {
    //
    // actual ColumnElement from the dbmodel
    //
    private ColumnElement columnElement;

    //
    // the table this column belongs to
    //
    private QueryTable table;

    //
    // input value for this column for update statements.
        // This field contains LocalFieldDesc for the corresponding field when
        // an UpdateStatement using batching uses this field.
    //
    private Object value;

    //
    // the position of this column in the SQL statement
    //
    private int index;

    //
    // the name of this column
    //
    private String name;

    public ColumnRef(ColumnElement columnElement,
                     QueryTable table) {
        this.columnElement = columnElement;
        name = columnElement.getName().getName();
        this.table = table;
    }

    public ColumnRef(ColumnElement columnElement,
                     Object value) {
        this.columnElement = columnElement;
        name = columnElement.getName().getName();
        this.value = value;
    }

    /** Return the actual ColumnElement associated with this column.
     *  @return the ColumnElement associated with this
     */
    public ColumnElement getColumnElement() {
        return columnElement;
    }

    /** Return the position of this column in the SQL statement.
     *  @return the position of this column in the SQL statement
     */
    public int getIndex() {
        return index;
    }

    /** Set the position of this column in the SQL statement.
     *  @param value - the new position
     */
    public void setIndex(int value) {
        this.index = value;
    }

    /** Return the input value for this column.
     *  @return the input value for this column
     */
    public Object getValue() {
        return value;
    }

    /** Return the QueryTable associated with this column.
     *  @return the QueryTable associated with this column.
     */
    public QueryTable getQueryTable() {
        return table;
    }

    /** Return the name of this column.
     *  @return the name of this column.
     */
    public String getName() {
        return name;
    }

     //---- implementing FieldMapping ------------------------------//
     /**
      * This method return int corresponding to java.sql.Types.
      */
     public int getColumnType() {
         return columnElement.getType();
     }

     /**
      * This method return the name of the column.
      */
     public String getColumnName() {
         return name;
     }

     /**
      * This method return the length of the column and -1 if unknown.
      */
     public int getColumnLength() {
         Integer len = columnElement.getLength();
         return (len != null) ? len.intValue(): -1;
     }

     //---- end of implementing FieldMapping -----------------------//
}
