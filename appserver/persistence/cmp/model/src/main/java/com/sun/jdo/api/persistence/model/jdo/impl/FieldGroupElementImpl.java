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
 * FieldGroupElementImpl.java
 *
 * Created on March 2, 2000, 6:28 PM
 */

package com.sun.jdo.api.persistence.model.jdo.impl;

import com.sun.jdo.api.persistence.model.ModelException;
import com.sun.jdo.api.persistence.model.jdo.FieldGroupElement;
import com.sun.jdo.api.persistence.model.jdo.PersistenceFieldElement;

/**
 *
 * @author  raccah
 * @version %I%
 */
public class FieldGroupElementImpl extends PersistenceMemberElementImpl
    implements FieldGroupElement.Impl
{
    /** Fields of the field group element. */
    private PersistenceElementCollection _fields;

    /** Create new FieldGroupElementImpl with no corresponding name.  This
     * constructor should only be used for cloning and archiving.
     */
    public FieldGroupElementImpl ()
    {
        this(null);
    }

    /** Creates new FieldGroupElementImpl with the corresponding name
     * @param name the name of the element
     */
    public FieldGroupElementImpl (String name)
    {
        super(name);
        _fields = new PersistenceElementCollection(this, PROP_FIELDS,
            new PersistenceFieldElement[0]);
    }

    /** Find a field by name.
     * @param name the name to match
     * @return the field, or <code>null</code> if it does not exist
     */
    public PersistenceFieldElement getField (String name)
    {
        return (PersistenceFieldElement)_fields.getElement(name);
    }

    /** Get all fields.
     * @return the fields
     */
    public PersistenceFieldElement[] getFields ()
    {
        return (PersistenceFieldElement[])_fields.getElements();
    }


    /** Change the set of fields.
     * @param fields the new fields
     * @param action {@link #ADD}, {@link #REMOVE}, or {@link #SET}
     * @exception ModelException if impossible
     */
    public void changeFields (PersistenceFieldElement[] fields, int action)
        throws ModelException
    {
        _fields.changeElements(fields, action);
    }

    //=============== extra methods needed for xml archiver ==============

    /** Returns the field collection of this field group element.  This
     * method should only be used internally and for cloning and archiving.
     * @return the field collection of this field group element
     */
    public PersistenceElementCollection getCollection () { return _fields; }

    /** Set the field collection of this field group element to the supplied
     * collection.  This method should only be used internally and for
     * cloning and archiving.
     * @param collection the field collection of this field group element
     */
    public void setCollection (PersistenceElementCollection collection)
    {
        _fields = collection;
    }
}
