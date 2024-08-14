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
 * MappingFieldElement.java
 *
 * Created on March 3, 2000, 1:11 PM
 */

package com.sun.jdo.api.persistence.model.mapping;

import com.sun.jdo.api.persistence.model.ModelException;

import java.util.ArrayList;

import org.netbeans.modules.dbschema.DBMemberElement;

/**
 *
 * @author raccah
 * @version %I%
 */
public interface MappingFieldElement extends MappingMemberElement
{
    /** Constant representing the jdo default fetch group.
     * This is what used to be mandatory for SynerJ.
     */
    public static final int GROUP_DEFAULT = 1;

    /** Constant representing no fetch group. */
    public static final int GROUP_NONE = 0;

    /** Constant representing an independent fetch group.  All independent
     * fetch groups must have a value less than or equal to this constant.
     */
    public static final int GROUP_INDEPENDENT = -1;

    // TBD:unmap all components, if remove from class remove here too
    //public void clear ();

    /** Determines whether this field element is read only or not.
     * @return <code>true</code> if the field is read only,
     * <code>false</code> otherwise
     */
    public boolean isReadOnly ();

    /** Set whether this field element is read only or not.
     * @param flag - if <code>true</code>, the field element is marked as
     * read only; otherwise, it is not
     * @exception ModelException if impossible
     */
    public void setReadOnly (boolean flag) throws ModelException;

    /** Determines whether this field element is in a concurrency check or not.
     * @return <code>true</code> if the field is in a concurrency check,
     * <code>false</code> otherwise
     */
    public boolean isInConcurrencyCheck ();

    /** Set whether this field element is in a concurrency check or not.
     * @param flag - if <code>true</code>, the field element is marked as
     * being in a concurrency check; otherwise, it is not
     * @exception ModelException if impossible
     */
    public void setInConcurrencyCheck (boolean flag) throws ModelException;

    /** Determines whether this field element is a version field or not.
     * @return <code>true</code> if the field is a version field,
     * <code>false</code> otherwise
     */
    public boolean isVersion ();

    /** Set whether this field element is a version field or not.
     * @param flag - if <code>true</code>, the field element is marked
     * as a version field; otherwise, it is not
     * @exception ModelException if impossible
     */
    public void setVersion (boolean flag) throws ModelException;

    //====================== fetch group handling ==========================

    /** Get the fetch group of this field element.
     * @return the fetch group, one of {@link #GROUP_DEFAULT},
     * {@link #GROUP_NONE}, or anything less than or equal to
     * {@link #GROUP_INDEPENDENT}
     */
    public int getFetchGroup ();

    /** Set the fetch group of this field element.
     * @param group - an integer indicating the fetch group, one of:
     * {@link #GROUP_DEFAULT}, {@link #GROUP_NONE}, or anything less than or
     * equal to {@link #GROUP_INDEPENDENT}
     * @exception ModelException if impossible
     */
    public void setFetchGroup (int group) throws ModelException;

    //======================= column handling ===========================

    /** Returns the list of column names to which this mapping field is
     * mapped.
     * @return the names of the columns mapped by this mapping field
     */
    public ArrayList getColumns ();

    /** Adds a column to the list of columns mapped by this mapping field.
     * @param column column element to be added to the mapping
     * @exception ModelException if impossible
     */
    public void addColumn (DBMemberElement column) throws ModelException;

    /** Removes a column from the list of columns mapped by this mapping field.
     * @param columnName the relative name of the column to be removed from
     * the mapping
     * @exception ModelException if impossible
     */
    public void removeColumn (String columnName) throws ModelException;
}
