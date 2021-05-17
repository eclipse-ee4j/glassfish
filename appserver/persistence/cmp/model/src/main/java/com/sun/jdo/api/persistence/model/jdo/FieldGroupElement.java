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
 * FieldGroupElement.java
 *
 * Created on February 29, 2000, 4:57 PM
 */

package com.sun.jdo.api.persistence.model.jdo;

import com.sun.jdo.api.persistence.model.ModelException;

/**
 *
 * @author raccah
 * @version %I%
 */
public abstract class FieldGroupElement extends PersistenceMemberElement
    implements FieldElementHolder
{
    /** Create new FieldGroupElement with no implementation.
     * This constructor should only be used for cloning and archiving.
     */
    public FieldGroupElement ()
    {
        this(null, null);
    }

    /** Create new FieldGroupElement with the provided implementation. The
     * implementation is responsible for storing all properties of the object.
     * @param impl the implementation to use
     * @param declaringClass the class to attach to
     */
    public FieldGroupElement (FieldGroupElement.Impl impl,
        PersistenceClassElement declaringClass)
    {
        super(impl, declaringClass);
    }

    /** @return implemetation factory for this field group
     */
    final Impl getFieldGroupImpl () { return (Impl)getImpl(); }

    //================== Fields ===============================
    // PersistenceFieldElement handling, implementation of FieldElementHolder

    /** Add the supplied field to the collection of fields maintained by this
     * holder.
     * @param field the field to be added
     * @exception ModelException if impossible
     */
    public void addField (PersistenceFieldElement field)
        throws ModelException
    {
        addFields(new PersistenceFieldElement[]{field});
    }

    /** Add the supplied fields to the collection of fields maintained by this
     * holder.
     * @param fields the array of fields to be added
     * @exception ModelException if impossible
     */
    public void addFields(PersistenceFieldElement[] fields)
        throws ModelException
    {
        getFieldGroupImpl().changeFields(fields, Impl.ADD);
    }

    /** Remove the supplied field from the collection of fields maintained by
     * this holder.
     * @param field the field to be removed
     * @exception ModelException if impossible
     */
    public void removeField (PersistenceFieldElement field)
        throws ModelException
    {
        removeFields(new PersistenceFieldElement[]{field});
    }

    /** Removed the supplied fields from the collection of fields maintained
     * by this holder.
     * @param fields the array of fields to be removed
     * @exception ModelException if impossible
     */
    public void removeFields (PersistenceFieldElement[] fields)
        throws ModelException
    {
        getFieldGroupImpl().changeFields(fields, Impl.REMOVE);
    }

    /** Returns the collection of fields maintained by this holder in the form
     * of an array.
     * @return the fields maintained by this holder
     */
    public PersistenceFieldElement[] getFields ()
    {
        return getFieldGroupImpl().getFields();
    }

    /** Sets the collection of fields maintained by this holder to the contents
     * of the supplied array.
     * @param fields the fields maintained by this holder
     * @exception ModelException if impossible
     */
    public void setFields (PersistenceFieldElement[] fields)
        throws ModelException
    {
        getFieldGroupImpl().changeFields(fields, Impl.SET);
    }

    /** Returns the field with the supplied name from the collection of fields
     * maintained by this holder.
     * @param name the name of the field to be found
     * @return the field with the supplied name, <code>null</code> if none exists
     */
    public PersistenceFieldElement getField (String name)
    {
        return getFieldGroupImpl().getField(name);
    }

    /** Tests whether the supplied field is in the collection of fields
     * maintained by this holder.
     * @param field the field to be tested
     */
    public boolean containsField (PersistenceFieldElement field)
    {
        return (getFieldGroupImpl().getField(field.getName()) != null);
    }

    /** Pluggable implementation of the storage of field element properties.
     * @see PersistenceFieldElement#PersistenceFieldElement
     */
    public interface Impl extends PersistenceMemberElement.Impl
    {
        /** Change the set of fields.
         * @param fields the new fields
         * @param action {@link #ADD}, {@link #REMOVE}, or {@link #SET}
         * @exception ModelException if impossible
         */
        public void changeFields (PersistenceFieldElement[] fields, int action)
            throws ModelException;

        /** Get all fields.
         * @return the fields
         */
        public PersistenceFieldElement[] getFields ();

        /** Find a field by name.
         * @param name the name to match
         * @return the field, or <code>null</code> if it does not exist
         */
        public PersistenceFieldElement getField (String name);
    }
}

