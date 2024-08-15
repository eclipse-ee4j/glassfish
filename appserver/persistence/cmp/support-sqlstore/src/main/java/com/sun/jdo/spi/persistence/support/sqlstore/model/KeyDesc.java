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
 * KeyDesc.java
 *
 * Created on March 3, 2000
 *
 */

package com.sun.jdo.spi.persistence.support.sqlstore.model;

import java.util.ArrayList;

import org.netbeans.modules.dbschema.ColumnElement;

/**
 * This class is used to encapsulate an association between
 * key fields and key columns.
 */
public class KeyDesc {

    /** Array of LocalFieldDesc */
    private ArrayList fields;

    /** Array of ColumnElements */
    private ArrayList columns;

    /** Initialize the columns ArrayList. */
    void addColumns(ArrayList columns) {
        this.columns = columns;
    }

    /** Add a field to the KeyDesc.
     *  @param f - FieldDesc to be added
     */
    void addField(FieldDesc f) {
        if (fields == null)
            fields = new ArrayList();

        fields.add(f);
    }

    /** Add a column to this KeyDesc.
     *  @param c - ColumnElement to be added
     */
    void addColumn(ColumnElement c) {
        if (columns == null)
            columns = new ArrayList();

        columns.add(c);
    }

    /** Return all key columns.
     *  @return an ArrayList of ColumnElements
     */
    public ArrayList getColumns() {
        return columns;
    }

    /** Return all key fields.
     *  @return an ArrayList of FieldDescs
     */
    public ArrayList getFields() {
        return fields;
    }
}




