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
 * MappingRelationshipElement.java
 *
 * Created on March 3, 2000, 1:11 PM
 */

package com.sun.jdo.api.persistence.model.mapping;

import com.sun.jdo.api.persistence.model.ModelException;

import java.util.ArrayList;

import org.netbeans.modules.dbschema.ColumnPairElement;

/**
 * This is a specialized field element which represents a relationship
 * between two classes.  The mapping portion should be set up as follows:
 * When mapping a non-join table relationship, call the {@link #addColumn}
 * method once with each pair of columns between the local table and the
 * foreign table.  When mapping a join table relationship, call the
 * {@link #addLocalColumn} once for each pair of columns between the
 * local table and the join table and {@link #addAssociatedColumn} once for
 * each pair of columns between the join table and the foreign table.
 * Note that the number of pairs (local and associated) may differ and that
 * the order of adding them (local first or associated first) is not
 * important.
 *
 * @author Mark Munro
 * @author Rochelle Raccah
 * @version %I%
 */
public interface MappingRelationshipElement extends MappingFieldElement
{
    //=================== column handling for join tables ====================

    /** Returns the list of associated column names to which this
     * mapping field is mapped.  This is used for join tables.
     * @return the names of the columns mapped by this mapping field
     * @see MappingFieldElement#getColumns
     */
    public ArrayList getAssociatedColumns ();

    /** Adds a column to the list of columns mapped by this mapping field.
     * Call this method instead of <code>addColumn</code> when mapping join
     * tables.  This method is used to map between the local column and the
     * join table, while <code>addAssociatedColumn</code> is used to
     * map between the join table and the foreign table.
     * @param column foreign column element to be added to the mapping
     * @exception ModelException if impossible
     * @see MappingFieldElement#addColumn
     * @see #addAssociatedColumn
     */
    public void addLocalColumn (ColumnPairElement column) throws ModelException;

    /** Adds a column to the list of associated columns mapped by this mapping
     * field.  Call this method instead of <code>addColumn</code> when mapping
     * join tables.  This method is used to map between the join table column
     * and the foreign table column, while <code>addLocalColumn</code> is used
     * to map between the local table and the join table.
     * @param column foreign column element to be added to the mapping
     * @exception ModelException if impossible
     * @see MappingFieldElement#addColumn
     * @see #addLocalColumn
     */
    public void addAssociatedColumn (ColumnPairElement column)
        throws ModelException;
}
