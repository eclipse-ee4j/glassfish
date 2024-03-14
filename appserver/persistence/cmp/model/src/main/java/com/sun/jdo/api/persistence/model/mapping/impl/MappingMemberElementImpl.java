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
 * MappingMemberElementImpl.java
 *
 * Created on May 23, 2000, 12:41 AM
 */

package com.sun.jdo.api.persistence.model.mapping.impl;

import java.beans.PropertyVetoException;

import com.sun.jdo.api.persistence.model.mapping.*;

/**
 *
 * @author Mark Munro
 * @author Rochelle Raccah
 * @version %I%
 */
public abstract class MappingMemberElementImpl extends MappingElementImpl
    implements MappingMemberElement
{
    /** the class to which this element belongs */
    MappingClassElement _declaringClass;

    /** Create new MappingMemberElementImpl with no corresponding name or
     * declaring class.  This constructor should only be used for cloning and
     * archiving.
     */
    public MappingMemberElementImpl ()
    {
        this(null, null);
    }

    /** Create new MappingMemberElementImpl with the corresponding name and
     * declaring class.
     * @param name the name of the element
     * @param declaringClass the class to attach to
     */
    public MappingMemberElementImpl (String name,
        MappingClassElement declaringClass)
    {
        super(name);
        _declaringClass = declaringClass;
    }

    /** Get the declaring class.
     * @return the class that owns this member element, or <code>null</code>
     * if the element is not attached to any class
     */
    public MappingClassElement getDeclaringClass () { return _declaringClass; }

    /** Overrides MappingElementImpl's <code>equals</code> method to add
     * comparison of the name of the declaring class this mapping element.
     * The method returns <code>false</code> if obj does not have a declaring
     * class with the same name as this mapping element.
     * @return <code>true</code> if this object is the same as the obj argument;
     * <code>false</code> otherwise.
     * @param obj the reference object with which to compare.
     */
    public boolean equals (Object obj)
    {
        if (super.equals(obj) && (obj instanceof MappingMemberElement))
        {
            MappingClassElement declaringClass = getDeclaringClass();
            MappingClassElement objDeclaringClass =
                ((MappingMemberElement)obj).getDeclaringClass();

            return ((declaringClass == null) ? (objDeclaringClass == null) :
                declaringClass.equals(objDeclaringClass));
        }

        return false;
    }

    /** Overrides MappingElementImpl's <code>hashCode</code> method to add
     * the hashCode of this mapping element's declaring class.
     * @return a hash code value for this object.
     */
    public int hashCode ()
    {
        MappingClassElement declaringClass = getDeclaringClass();

        return (super.hashCode() +
            ((declaringClass == null) ? 0 : declaringClass.hashCode()));
    }

    /** Fires property change event.  This method overrides that of
     * MappingElementImpl to update the MappingClassElementImpl's modified
     * status.
     * @param name property name
     * @param o old value
     * @param n new value
     */
    protected final void firePropertyChange (String name, Object o, Object n)
    {
        // even though o == null and n == null will signify a change, that
        // is consistent with PropertyChangeSupport's behavior and is
        // necessary for this to work
        boolean noChange = ((o != null) && (n != null) && o.equals(n));
        MappingClassElement classElement = getDeclaringClass();

        super.firePropertyChange(name, o, n);

        if ((classElement != null) && !noChange)
            classElement.setModified(true);
    }

    /** Fires vetoable change event.  This method overrides that of
     * MappingElementImpl to give listeners a chance to block
     * changes on the mapping class element modified status.
     * @param name property name
     * @param o old value
     * @param n new value
     * @exception PropertyVetoException when the change is vetoed by a listener
     */
    protected final void fireVetoableChange (String name, Object o, Object n)
        throws PropertyVetoException
    {
        // even though o == null and n == null will signify a change, that
        // is consistent with PropertyChangeSupport's behavior and is
        // necessary for this to work
        boolean noChange = ((o != null) && (n != null) && o.equals(n));
        MappingClassElement classElement = getDeclaringClass();

        super.fireVetoableChange(name, o, n);

        if ((classElement != null) && !noChange)
        {
            ((MappingClassElementImpl)classElement).fireVetoableChange(
                PROP_MODIFIED, Boolean.FALSE, Boolean.TRUE);
        }
    }

    //=============== extra set methods needed for xml archiver ==============

    /** Set the declaring class of this mapping member.  This method should
     * only be used internally and for cloning and archiving.
     * @param declaringClass the declaring class of this mapping member
     */
    public void setDeclaringClass (MappingClassElement declaringClass)
    {
        _declaringClass = declaringClass;
    }
}
