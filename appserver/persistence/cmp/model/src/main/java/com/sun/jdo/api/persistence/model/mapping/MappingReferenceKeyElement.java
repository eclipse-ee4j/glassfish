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
 * MappingReferenceKeyElement.java
 *
 * Created on March 3, 2000, 1:11 PM
 */

package com.sun.jdo.api.persistence.model.mapping;

import com.sun.jdo.api.persistence.model.ModelException;

import java.util.ArrayList;

import org.netbeans.modules.dbschema.ColumnPairElement;
import org.netbeans.modules.dbschema.ReferenceKey;

/**
 * This is an element which represents a relationship between two tables
 * (primary and secondary).  It should not be used for relationship fields
 * (MappingRelationshipElement has its own set of pairs).  It can be thought
 * of as a "fake foreign key" meaning it designates the column pairs used to
 * join the primary table with a secondary table.  It is analagous to a
 * foreign key and may in fact contain identical pairs as the foreign key,
 * but this is not a requirement.  The foreign key may define a different
 * set of pairs or may not exist at all.  Although any set of pairs is legal,
 * the user should be careful to define pairs which represent a logical
 * relationship between the two tables.  The relationship should be set up as
 * follows:
 * First, set a primary table for the mapping class.  Doing this sets up a
 * "fake primary key" for the associated mapping table element.  Next, add
 * a secondary table and set up the pairs which establish the connection
 * on the returned reference key object.  This sets up whatever "fake primary
 * key" information is necessary on the secondary table's mapping table,
 * establishes the primary to secondary relationship via the reference keys,
 * and puts the pair information into the "fake foreign key".
 *
 * @author Mark Munro
 * @author Rochelle Raccah
 * @version %I%
 */
public interface MappingReferenceKeyElement
    extends MappingMemberElement, ReferenceKey
{
    //======================= table handling ===========================

    /** Returns the mapping table element for this referencing key.
     * @return the meta data table for this referencing key
     */
    public MappingTableElement getTable ();

    /** Set the mapping table for this referencing key to the supplied table.
     * @param table mapping table element to be used with this key.
     * @exception ModelException if impossible
     */
    public void setTable (MappingTableElement table) throws ModelException;

    //======================= column handling ===========================

    /** Returns the list of relative column pair names in this referencing key.
     * @return the names of the column pairs in this referencing key
     */
    public ArrayList getColumnPairNames ();

    /** Remove a column pair from the holder.  This method can be used to
     * remove a pair by name when it cannot be resolved to an actual pair.
     * @param pairName the relative name of the column pair to remove
     * @throws ModelException if impossible
     */
    public void removeColumnPair (String pairName) throws ModelException;

    /** Remove some column pairs from the holder.  This method can be used to
     * remove pairs by name when they cannot be resolved to actual pairs.
     * @param pairNames the relative names of the column pairs to remove
     * @throws ModelException if impossible
     */
    public void removeColumnPairs (ArrayList pairNames) throws ModelException;

    //==== redefined from ReferenceKey to narrow Exception->ModelException ===

    /** Add a new column pair to the holder.
     * @param pair the pair to add
     * @throws ModelException if impossible
     */
    public void addColumnPair (ColumnPairElement pair) throws ModelException;

    /** Add some new column pairs to the holder.
     * @param pairs the column pairs to add
     * @throws ModelException if impossible
     */
    public void addColumnPairs (ColumnPairElement[] pairs)
        throws ModelException;

    /** Remove a column pair from the holder.
     * @param pair the column pair to remove
     * @throws ModelException if impossible
     */
    public void removeColumnPair (ColumnPairElement pair)
        throws ModelException;

    /** Remove some column pairs from the holder.
     * @param pairs the column pairs to remove
     * @throws ModelException if impossible
     */
    public void removeColumnPairs (ColumnPairElement[] pairs)
        throws ModelException;

    /** Set the column pairs for this holder.
     * Previous column pairs are removed.
     * @param pairs the new column pairs
     * @throws ModelException if impossible
     */
    public void setColumnPairs (ColumnPairElement[] pairs)
        throws ModelException;
}
