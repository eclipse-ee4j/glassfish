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
 * PersistenceFieldElementImpl.java
 *
 * Created on March 2, 2000, 6:16 PM
 */

package com.sun.jdo.api.persistence.model.jdo.impl;

import com.sun.jdo.api.persistence.model.ModelException;
import com.sun.jdo.api.persistence.model.ModelVetoException;
import com.sun.jdo.api.persistence.model.jdo.PersistenceFieldElement;
import com.sun.jdo.spi.persistence.utility.JavaTypeHelper;

import java.beans.PropertyVetoException;

/**
 *
 * @author raccah
 * @version %I%
 */
public class PersistenceFieldElementImpl extends PersistenceMemberElementImpl
    implements PersistenceFieldElement.Impl
{
    /** Constant representing read sensitive. */
    private static final int READ_SENSITIVE = 1;

    /** Constant representing write sensitive. */
    private static final int WRITE_SENSITIVE = 2;

    /** Persistence type of the field element. */
    private int _persistenceType;

    /** Derived modifier of the field element. */
    private int _derivedModifier;

    /** Key field flag of the field element. */
    private boolean _isKey;

    /** Create new PersistenceFieldElementImpl with no corresponding name.
     * This constructor should only be used for cloning and archiving.
     */
    public PersistenceFieldElementImpl ()
    {
        this(null);
    }

    /** Creates new PersistenceFieldElementImpl with the corresponding name
     * @param name the name of the element
     */
    public PersistenceFieldElementImpl (String name)
    {
        super(name);
        _persistenceType = PersistenceFieldElement.PERSISTENT;
    }

    /** Get the persistence type of this field element.
     * @return the persistence type, one of
     * {@link PersistenceFieldElement#PERSISTENT} or
     * {@link PersistenceFieldElement#DERIVED}.  The default is PERSISTENT.
     */
    public int getPersistenceType() { return _persistenceType; }

    /** Set the persistence type of this field element.
     * @param type - an integer indicating the persistence type, one of:
     * {@link PersistenceFieldElement#PERSISTENT} or
     * {@link PersistenceFieldElement#DERIVED}
     * @exception ModelException if impossible
     */
    public void setPersistenceType (int type) throws ModelException
    {
        Integer old = new Integer(getPersistenceType());
        Integer newType = new Integer(type);

        try
        {
            fireVetoableChange(PROP_PERSISTENCE, old, newType);
            _persistenceType = type;
            firePropertyChange(PROP_PERSISTENCE, old, newType);
        }
        catch (PropertyVetoException e)
        {
            throw new ModelVetoException(e);
        }
    }

    /** Determines whether this field element is read sensitive or not.
     * This value is only used if <code>getPersistenceType</code> returns
     * <code>DERIVED</code>
     * @return <code>true</code> if the field is read sensitive,
     * <code>false</code> if it is not or if the persistence type is not
     * derived
     * @see #isWriteSensitive
     * @see #setPersistenceType
     * @see PersistenceFieldElement#DERIVED
     *
     */
    public boolean isReadSensitive ()
    {
        return ((PersistenceFieldElement.DERIVED == getPersistenceType()) ?
            ((_derivedModifier & READ_SENSITIVE) != 0) : false);
    }

    /** Set whether this field element is read sensitive or not.
     * @param flag - if <code>true</code> and this is a derived field, the
     * field element is marked as read sensitive; otherwise, it is not
     * This value is only used if <code>getPersistenceType</code> returns
     * <code>DERIVED</code>
     * @exception ModelException if impossible
     * @see #setWriteSensitive
     * @see #setPersistenceType
     * @see PersistenceFieldElement#DERIVED
     */
    public void setReadSensitive (boolean flag) throws ModelException
    {
        Boolean old = JavaTypeHelper.valueOf(isReadSensitive());
        Boolean newFlag = JavaTypeHelper.valueOf(flag);

        try
        {
            fireVetoableChange(PROP_SENSITIVITY, old, newFlag);

            if (flag)
                _derivedModifier |= READ_SENSITIVE;
            else
                _derivedModifier &= READ_SENSITIVE;

            firePropertyChange(PROP_SENSITIVITY, old, newFlag);
        }
        catch (PropertyVetoException e)
        {
            throw new ModelVetoException(e);
        }
    }

    /** Determines whether this field element is write sensitive or not.
     * This value is only used if <code>getPersistenceType</code> returns
     * <code>DERIVED</code>
     * @return <code>true</code> if the field is write sensitive,
     * <code>false</code> if it is not or if the persistence type is not
     * derived
     * @see #isReadSensitive
     * @see #setPersistenceType
     * @see PersistenceFieldElement#DERIVED
     *
     */
    public boolean isWriteSensitive ()
    {
        return ((PersistenceFieldElement.DERIVED == getPersistenceType()) ?
            ((_derivedModifier & WRITE_SENSITIVE) != 0) : false);
    }

    /** Set whether this field element is write sensitive or not.
     * @param flag - if <code>true</code> and this is a derived field, the
     * field element is marked as write sensitive; otherwise, it is not
     * This value is only used if <code>getPersistenceType</code> returns
     * <code>DERIVED</code>
     * @exception ModelException if impossible
     * @see #setReadSensitive
     * @see #setPersistenceType
     * @see PersistenceFieldElement#DERIVED
     */
    public void setWriteSensitive (boolean flag) throws ModelException
    {
        Boolean old = JavaTypeHelper.valueOf(isWriteSensitive());
        Boolean newFlag = JavaTypeHelper.valueOf(flag);

        try
        {
            fireVetoableChange(PROP_SENSITIVITY, old, newFlag);

            if (flag)
                _derivedModifier |= WRITE_SENSITIVE;
            else
                _derivedModifier &= WRITE_SENSITIVE;

            firePropertyChange(PROP_SENSITIVITY, old, newFlag);
        }
        catch (PropertyVetoException e)
        {
            throw new ModelVetoException(e);
        }
    }

    /** Determines whether this field element is a key field or not.
     * @return <code>true</code> if the field is a key field,
     * <code>false</code> otherwise
     * @see com.sun.jdo.api.persistence.model.jdo.impl.PersistenceClassElementImpl#getKeyClass
     */
    public boolean isKey () { return _isKey; }

    /** Set whether this field element is a key field or not.
     * @param flag - if <code>true</code>, the field element is marked
     * as a key field; otherwise, it is not
     * @exception ModelException if impossible
     * @see com.sun.jdo.api.persistence.model.jdo.impl.PersistenceClassElementImpl#getKeyClass
     */
    public void setKey (boolean flag) throws ModelException
    {
        Boolean old = JavaTypeHelper.valueOf(isKey());
        Boolean newFlag = JavaTypeHelper.valueOf(flag);

        try
        {
            fireVetoableChange(PROP_KEY_FIELD, old, newFlag);
            _isKey = flag;
            firePropertyChange(PROP_KEY_FIELD, old, newFlag);
        }
        catch (PropertyVetoException e)
        {
            throw new ModelVetoException(e);
        }
    }
}
