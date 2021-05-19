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
 * PersistenceMemberElement.java
 *
 * Created on February 29, 2000, 12:50 PM
 */

package com.sun.jdo.api.persistence.model.jdo;

/**
 *
 * @author raccah
 * @version %I%
 */
public abstract class PersistenceMemberElement extends PersistenceElement
{
    /** the class to which this element belongs */
    private PersistenceClassElement _declaringClass;

    /** Create new PersistenceMemberElement with no implementation.
     * This constructor should only be used for cloning and archiving.
     */
    public PersistenceMemberElement ()
    {
        this(null, null);
    }

    /** Create new PersistenceMemberElement with the provided implementation. The
     * implementation is responsible for storing all properties of the object.
     * @param impl the implementation to use
     * @param declaringClass the class to attach to
     */
    protected PersistenceMemberElement (PersistenceMemberElement.Impl impl,
        PersistenceClassElement declaringClass)
    {
        super(impl);
        _declaringClass = declaringClass;
    }

    /** @return the current implementation.
     */
    final Impl getMemberImpl () { return (Impl)getImpl(); }

    /** Get the declaring class.
     * @return the class that owns this member element, or <code>null</code>
     * if the element is not attached to any class
     */
    public PersistenceClassElement getDeclaringClass ()
    {
        return _declaringClass;
    }

    //=============== extra set methods needed for xml archiver ==============

    /** Set the declaring class of this member element.  This method should
     * only be used internally and for cloning and archiving.
     * @param declaringClass the declaring class of this member element
     */
    public void setDeclaringClass (PersistenceClassElement declaringClass)
    {
        _declaringClass = declaringClass;
    }

    /** Overrides PersistenceElement's <code>equals</code> method to add
     * comparison of the name of the declaring class this persistence element.
     * The method returns <code>false</code> if obj does not have a declaring
     * class with the same name as this persistence element.
     * @return <code>true</code> if this object is the same as the obj argument;
     * <code>false</code> otherwise.
     * @param obj the reference object with which to compare.
     */
    public boolean equals (Object obj)
    {
        if (super.equals(obj) && (obj instanceof PersistenceMemberElement))
        {
            PersistenceClassElement declaringClass = getDeclaringClass();
            PersistenceClassElement objDeclaringClass =
                ((PersistenceMemberElement)obj).getDeclaringClass();

            return ((declaringClass == null) ? (objDeclaringClass == null) :
                declaringClass.equals(objDeclaringClass));
        }

        return false;
    }

    /** Overrides PersistenceElement's <code>hashCode</code> method to add
     * the hashCode of this persistence element's declaring class.
     * @return a hash code value for this object.
     */
    public int hashCode ()
    {
        PersistenceClassElement declaringClass = getDeclaringClass();

        return (super.hashCode() +
            ((declaringClass == null) ? 0 : declaringClass.hashCode()));
    }

    /** Pluggable implementation of member elements.
     * @see PersistenceMemberElement
     */
    public interface Impl extends PersistenceElement.Impl { }
}

