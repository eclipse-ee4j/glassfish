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
 * PersistenceFieldElement.java
 *
 * Created on February 29, 2000, 1:11 PM
 */

package com.sun.jdo.api.persistence.model.jdo;

import com.sun.jdo.api.persistence.model.ModelException;

import java.util.ArrayList;

/* TODO:
    1. throw (Model or IllegalArgument)Exception on set illegal constant values?
        also applies to PersistenceClass, Relationship classes
    2. document default values for all constants (should that go in impl docs?)
        also applies to PersistenceClass, Relationship classes
 */

/**
 *
 * @author raccah
 * @version %I%
 */
public class PersistenceFieldElement extends PersistenceMemberElement
{
    /** Constant representing a persistent field modifier. */
    public static final int PERSISTENT = 0;

    /** Constant representing a derived field modifier. */
    public static final int DERIVED = 1;

    /** Constant representing a transient field modifier.  This constant is
     * only here for comparison purposes, it will not be returned by
     * <code>getPersistenceType</code> since there will be no instance of
     * this class for transient fields.
     */
    public static final int TRANSIENT = 2;

    /** Create new PersistenceFieldElement with no implementation.
     * This constructor should only be used for cloning and archiving.
     */
    public PersistenceFieldElement ()
    {
        this(null, null);
    }

    /** Create new PersistenceFieldElement with the provided implementation. The
     * implementation is responsible for storing all properties of the object.
     * @param impl the implementation to use
     * @param declaringClass the class to attach to
     */
    public PersistenceFieldElement (PersistenceFieldElement.Impl impl,
        PersistenceClassElement declaringClass)
    {
        super(impl, declaringClass);
    }

    /** @return implemetation factory for this field
     */
    final Impl getFieldImpl () { return (Impl)getImpl(); }

    /** Get the persistence type of this field element.
     * @return the persistence type, one of {@link #PERSISTENT} or
     * {@link #DERIVED}
     */
    public int getPersistenceType ()
    {
        return getFieldImpl().getPersistenceType();
    }

    /** Set the persistence type of this field element.
     * @param type - an integer indicating the persistence type, one of:
     * {@link #PERSISTENT} or {@link #DERIVED}
     * @exception ModelException if impossible
     */
    public void setPersistenceType (int type) throws ModelException
    {
        getFieldImpl().setPersistenceType(type);
    }

    /** Determines whether this field element is read sensitive or not.
     * This value is only used if <code>getPersistenceType</code> returns
     * <code>DERIVED</code>
     * @return <code>true</code> if the field is read sensitive,
     * <code>false</code> if it is not or if the persistence type is not derived
     * @see #isWriteSensitive
     * @see #setPersistenceType
     * @see #DERIVED
     */
    public boolean isReadSensitive ()
    {
        return ((getPersistenceType() == DERIVED) &&
            getFieldImpl().isReadSensitive());
    }

    /** Set whether this field element is read sensitive or not.
     * @param flag - if <code>true</code> and this is a derived field, the field
     * element is marked as read sensitive; otherwise, it is not
     * This value is only used if <code>getPersistenceType</code> returns
     * <code>DERIVED</code>
     * @exception ModelException if impossible
     * @see #setWriteSensitive
     * @see #setPersistenceType
     * @see #DERIVED
     */
    public void setReadSensitive (boolean flag) throws ModelException
    {
        getFieldImpl().setReadSensitive(flag);
    }

    /** Determines whether this field element is write sensitive or not.
     * This value is only used if <code>getPersistenceType</code> returns
     * <code>DERIVED</code>
     * @return <code>true</code> if the field is write sensitive,
     * <code>false</code> if it is not or if the persistence type is not derived
     * @see #isReadSensitive
     * @see #setPersistenceType
     * @see #DERIVED
     */
    public boolean isWriteSensitive ()
    {
        return ((getPersistenceType() == DERIVED) &&
            getFieldImpl().isWriteSensitive());
    }

    /** Set whether this field element is write sensitive or not.
     * @param flag - if <code>true</code> and this is a derived field, the field
     * element is marked as write sensitive; otherwise, it is not
     * This value is only used if <code>getPersistenceType</code> returns
     * <code>DERIVED</code>
     * @exception ModelException if impossible
     * @see #setReadSensitive
     * @see #setPersistenceType
     * @see #DERIVED
     */
    public void setWriteSensitive (boolean flag) throws ModelException
    {
        getFieldImpl().setWriteSensitive(flag);
    }

    /** Determines whether this field element is a key field or not.
     * @return <code>true</code> if the field is a key field,
     * <code>false</code> otherwise
     * @see PersistenceClassElement#getKeyClass
     */
    public boolean isKey () { return getFieldImpl().isKey(); }

    /** Set whether this field element is a key field or not.
     * @param flag - if <code>true</code>, the field element is marked
     * as a key field; otherwise, it is not
     * @exception ModelException if impossible
     * @see PersistenceClassElement#getKeyClass
     */
    public void setKey (boolean flag) throws ModelException
    {
        getFieldImpl().setKey(flag);
    }

    //================== ConcurrencyGroups ===============================
    // convenience method to access ConcurrencyGroupElements

    /** Returns the array of concurrency groups to which this field belongs.
     * @return the concurrency groups in which this field participates
     * @see PersistenceClassElement#getConcurrencyGroups
     */
    public ConcurrencyGroupElement[] getConcurrencyGroups ()
    {
        ConcurrencyGroupElement[] groups = getDeclaringClass().
            getConcurrencyGroups();
        int i, count = ((groups != null) ? groups.length : 0);
        ArrayList myGroups = new ArrayList(count);

        for (i = 0; i < count; i++)
        {
            ConcurrencyGroupElement group = groups[i];

            if (group.containsField(this))
                myGroups.add(group);
        }

        count = myGroups.size();

        return ((ConcurrencyGroupElement[])myGroups.toArray(
            new ConcurrencyGroupElement[count]));
    }

    /** Computes the field number of this field element.
     * @return the field number of this field, -1 if it cannot be found
     */
    public int getFieldNumber ()
    {
        // for later - take into account the class
        // get/setFieldInheritanceFlag behavior (i.e. might need to climb
        // inheritance hierarchy
        PersistenceFieldElement[] fields = getDeclaringClass().getFields();
        int i, count = ((fields != null) ? fields.length : 0);

        for (i = 0; i < count; i++)
            if (equals(fields[i]))
                return i;

        return -1;
    }

    /* won't be used now -- we will compute this number whenever it is requested
    public void setFieldNumber (int fieldNumber) {} */

    /** Pluggable implementation of the storage of field element properties.
     * @see PersistenceFieldElement#PersistenceFieldElement
     */
    public interface Impl extends PersistenceMemberElement.Impl
    {
        /** Get the persistence type of this field element.
         * @return the persistence type, one of {@link #PERSISTENT} or
         * {@link #DERIVED}
         */
        public int getPersistenceType ();

        /** Set the persistence type of this field element.
         * @param type - an integer indicating the persistence type, one of:
         * {@link #PERSISTENT} or {@link #DERIVED}
         * @exception ModelException if impossible
         */
        public void setPersistenceType (int type) throws ModelException;

        /** Determines whether this field element is read sensitive or not.
         * This value is only used if <code>getPersistenceType</code> returns
         * <code>DERIVED</code>
         * @return <code>true</code> if the field is read sensitive,
         * <code>false</code> if it is not or if the persistence type is not
         * derived
         * @see #isWriteSensitive
         * @see #setPersistenceType
         * @see #DERIVED
         *
         */
        public boolean isReadSensitive ();

        /** Set whether this field element is read sensitive or not.
         * @param flag - if <code>true</code> and this is a derived field, the
         * field element is marked as read sensitive; otherwise, it is not
         * This value is only used if <code>getPersistenceType</code> returns
         * <code>DERIVED</code>
         * @exception ModelException if impossible
         * @see #setWriteSensitive
         * @see #setPersistenceType
         * @see #DERIVED
         */
        public void setReadSensitive (boolean flag) throws ModelException;

        /** Determines whether this field element is write sensitive or not.
         * This value is only used if <code>getPersistenceType</code> returns
         * <code>DERIVED</code>
         * @return <code>true</code> if the field is write sensitive,
         * <code>false</code> if it is not or if the persistence type is not
         * derived
         * @see #isReadSensitive
         * @see #setPersistenceType
         * @see #DERIVED
         *
         */
        public boolean isWriteSensitive ();

        /** Set whether this field element is write sensitive or not.
         * @param flag - if <code>true</code> and this is a derived field, the
         * field element is marked as write sensitive; otherwise, it is not
         * This value is only used if <code>getPersistenceType</code> returns
         * <code>DERIVED</code>
         * @exception ModelException if impossible
         * @see #setReadSensitive
         * @see #setPersistenceType
         * @see #DERIVED
         */
        public void setWriteSensitive (boolean flag) throws ModelException;

        /** Determines whether this field element is a key field or not.
         * @return <code>true</code> if the field is a key field,
         * <code>false</code> otherwise
         * @see PersistenceClassElement#getKeyClass
         */
        public boolean isKey ();

        /** Set whether this field element is a key field or not.
         * @param flag - if <code>true</code>, the field element is marked
         * as a key field; otherwise, it is not
         * @exception ModelException if impossible
         * @see PersistenceClassElement#getKeyClass
         */
        public void setKey (boolean flag) throws ModelException;
    }
}
