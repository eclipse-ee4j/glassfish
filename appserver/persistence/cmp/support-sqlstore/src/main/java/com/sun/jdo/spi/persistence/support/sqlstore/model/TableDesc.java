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
 * TableDesc.java
 *
 * Created on March 3, 2000
 *
 */

package com.sun.jdo.spi.persistence.support.sqlstore.model;

import com.sun.jdo.api.persistence.model.mapping.MappingClassElement;

import java.util.ArrayList;

import org.netbeans.modules.dbschema.TableElement;

/**
 * This class is used to represent a database table.
 */
public class TableDesc {

    /** primary key for the table */
    private KeyDesc key;

    /** array of ReferenceKeyDescs referencing secondary tables */
    private ArrayList secondaryTableKeys;

    /** ReferenceKeyDesc referencing the primary table */
    private ReferenceKeyDesc primaryTableKey;

    /** actual TableElement from the dbmodel */
    private TableElement tableElement;

    /** Consistency level for this table defined in the model */
    private int consistencyLevel;

    /** indicates this table is a join table */
    private boolean isJoinTable;

    /** Name of the table */
    private String name;

    /** Version field used for version consistency */
    private LocalFieldDesc versionField;

    public TableDesc(TableElement tableElement) {
        this.tableElement = tableElement;

        name = tableElement.getName().getName();
        consistencyLevel = MappingClassElement.NONE_CONSISTENCY;
    }

    /** Return all secondary table keys.
     *  @return an ArrayList of ReferenceKeyDescs for secondary tables
     */
    public ArrayList getSecondaryTableKeys() {
        return secondaryTableKeys;
    }

    /** Add a new reference key to the list of secondary table keys.
     *  @param key - ReferenceKeyDesc to be added
     */
    void addSecondaryTableKey(ReferenceKeyDesc key) {
        if (secondaryTableKeys == null)
            secondaryTableKeys = new ArrayList();

        secondaryTableKeys.add(key);
    }

    /** Return the reference key referencing the primary table.
     *  @return the ReferenceKeyDesc referencing the primary table
     */
    public ReferenceKeyDesc getPrimaryTableKey() {
        return primaryTableKey;
    }

    /** Set the reference key referencing the primary table.
     *  @param key - ReferenceKeyDesc to be added
     */
    void setPrimaryTableKey(ReferenceKeyDesc key) {
        this.primaryTableKey = key;
    }

    /** Return the primary key for the table.
     *  @return the KeyDesc representing the primary key for the table
     */
    public KeyDesc getKey() {
        return key;
    }

    /** Set the primary key for the table.
     *  @param key - KeyDesc to be set as the primary key
     */
    void setKey(KeyDesc key) {
        this.key = key;
    }

    /** Return the actual dbmodel TableElement for this table.
     *  @return TableElement associated with this table
     */
    public TableElement getTableElement() {
        return tableElement;
    }

    /** Return the name of the table.
     *  @return the name of the table.
     */
    public String getName() {
        return name;
    }

    /** Return true if this table is a join table. */
    public boolean isJoinTable() {
        return isJoinTable;
    }

    /** Set consistencyLevel to value. */
    void setConsistencyLevel(int value) {
        consistencyLevel = value;
        //TODO :
        //if(isUpdateLockRequired() )
            //Check for DBVendorType.isUpdateLockSupported()
            //Log to trace if !DBVendorType.isUpdateLockSupported()
            //If this table is ever used, user would get an exception

    }

    /** Determins if an update lock is required on this table. */
    public boolean isUpdateLockRequired() {
        return consistencyLevel == MappingClassElement.LOCK_WHEN_LOADED_CONSISTENCY;
    }

    /** Set isJoinTable to value */
    void setJoinTable(boolean value) {
        isJoinTable = value;
    }

    void setVersionField(LocalFieldDesc field) {
        versionField = field;
    }

    /**
     * Returns the field representing the version column for this
     * table. The version column is used for verification with version
     * consistency. Each table can have only one version column.
     *
     * @return Version field.
     */
    public LocalFieldDesc getVersionField() {
        return versionField;
    }

}





