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
 * FieldElementHolder.java
 *
 * Created on February 28, 2000, 3:55 PM
 */

package com.sun.jdo.api.persistence.model.jdo;

import com.sun.jdo.api.persistence.model.ModelException;

/**
 *
 * @author raccah
 * @version %I%
 */
public interface FieldElementHolder
{
    /** Add the supplied field to the collection of fields maintained by this
     * holder.
     * @param field the field to be added
     * @exception ModelException if impossible
     */
    public void addField (PersistenceFieldElement field)
        throws ModelException;

    /** Add the supplied fields to the collection of fields maintained by this
     * holder.
     * @param fields the array of fields to be added
     * @exception ModelException if impossible
     */
    public void addFields (PersistenceFieldElement[] fields)
        throws ModelException;

    /** Remove the supplied field from the collection of fields maintained by
     * this holder.
     * @param field the field to be removed
     * @exception ModelException if impossible
     */
    public void removeField (PersistenceFieldElement field)
        throws ModelException;

    /** Removed the supplied fields from the collection of fields maintained
     * by this holder.
     * @param fields the array of fields to be removed
     * @exception ModelException if impossible
     */
    public void removeFields (PersistenceFieldElement[] fields)
        throws ModelException;

    /** Returns the collection of fields maintained by this holder in the form
     * of an array.
     * @return the fields maintained by this holder
     */
    public PersistenceFieldElement[] getFields ();

    /** Sets the collection of fields maintained by this holder to the contents
     * of the supplied array.
     * @param fields the fields maintained by this holder
     * @exception ModelException if impossible
     */
    public void setFields (PersistenceFieldElement[] fields)
        throws ModelException;

    /** Returns the field with the supplied name from the collection of fields
     * maintained by this holder.
     * @param name the name to match
     * @return the field with the supplied name, <code>null</code> if none exists
     */
    public PersistenceFieldElement getField (String name);

    /** Tests whether the supplied field is in the collection of fields
     * maintained by this holder.
     * @param field the field to be tested
     */
    public boolean containsField (PersistenceFieldElement field);
}
