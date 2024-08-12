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
 * MappingFieldElementImpl.java
 *
 * Created on March 3, 2000, 1:11 PM
 */

package com.sun.jdo.api.persistence.model.mapping.impl;

import com.sun.jdo.api.persistence.model.ModelException;
import com.sun.jdo.api.persistence.model.ModelVetoException;
import com.sun.jdo.api.persistence.model.jdo.ConcurrencyGroupElement;
import com.sun.jdo.api.persistence.model.jdo.PersistenceClassElement;
import com.sun.jdo.api.persistence.model.jdo.PersistenceFieldElement;
import com.sun.jdo.api.persistence.model.mapping.MappingClassElement;
import com.sun.jdo.api.persistence.model.mapping.MappingFieldElement;
import com.sun.jdo.api.persistence.model.mapping.MappingTableElement;
import com.sun.jdo.spi.persistence.utility.JavaTypeHelper;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.glassfish.persistence.common.I18NHelper;
import org.netbeans.modules.dbschema.DBMemberElement;
import org.netbeans.modules.dbschema.util.NameUtil;

/**
 *
 * @author Mark Munro
 * @author Rochelle Raccah
 * @version %I%
 */
public class MappingFieldElementImpl extends MappingMemberElementImpl
    implements MappingFieldElement
{
    private ArrayList _columns;    // array of member names (columns or pairs)

    //@olsen: made transient to prevent from serializing into mapping files
    private transient ArrayList _columnObjects; // array of DBMemberElement (for runtime)

    private int _fetchGroup;
    private int _properties;

    /** Version field flag of the field element. */
    private boolean _isVersion;

    // leave for runtime
    public static final int CLONE_FIELD = 1;
    public static final int CLONE_DEEP = 2;
    public static final int CLONE_MASK = 3;
    public static final int LOG_ON_ACCESS = 4;
    public static final int LOG_ON_MASK = 48;
    public static final int LOG_ON_UPDATE = 16;
    public static final int MOD_BI_ON_UPDATE = 32;
    public static final int OBSERVE_ON_ACCESS = 8;
    public static final int RECORD_ON_UPDATE = 64;
    public static final int SEND_BEFORE_IMAGE = 128;
    public static final int READ_ONLY = 256;
    public static final int REF_INTEGRITY_UPDATES = 512;
    public static final int IN_CONCURRENCY_CHECK = 1024;
    public static final int XLATE_FIELD = 2048;

    /** Create new MappingFieldElementImpl with no corresponding name or
     * declaring class.  This constructor should only be used for cloning and
     * archiving.
     */
    public MappingFieldElementImpl ()
    {
        this(null, null);
    }

    /** Create new MappingFieldElementImpl with the corresponding name and
     * declaring class.
     * @param name the name of the element
     * @param declaringClass the class to attach to
     */
    public MappingFieldElementImpl (String name,
        MappingClassElement declaringClass)
    {
        super(name, declaringClass);
        setFetchGroupInternal(GROUP_DEFAULT);
    }

// TBD?
/*    public boolean mapped ()
    {
        ArrayList columns = getColumns();

        return ((columns != null) && (columns.size() > 0));
    }

    public void clear () { _columns = null; }*/
// end TBD

    //=================== properties exposed in interface ==================

    /** Determines whether this field element is read only or not.
     * @return <code>true</code> if the field is read only,
     * <code>false</code> otherwise
     */
    public boolean isReadOnly () { return getProperty(READ_ONLY); }

    /** Set whether this field element is read only or not.
     * @param flag - if <code>true</code>, the field element is marked as
     * read only; otherwise, it is not
     * @exception ModelException if impossible
     */
    public void setReadOnly (boolean flag) throws ModelException
    {
        Boolean old = JavaTypeHelper.valueOf(isReadOnly());
        Boolean newFlag = JavaTypeHelper.valueOf(flag);

        try
        {
            fireVetoableChange(PROP_READ_ONLY, old, newFlag);
            setProperty(flag, READ_ONLY);
            firePropertyChange(PROP_READ_ONLY, old, newFlag);
        }
        catch (PropertyVetoException e)
        {
            throw new ModelVetoException(e);
        }
    }

    /** Determines whether this field element is in a concurrency check or not.
     * @return <code>true</code> if the field is in a concurrency check,
     * <code>false</code> otherwise
     */
    public boolean isInConcurrencyCheck ()
    {
        return getProperty(IN_CONCURRENCY_CHECK);
    }

    /** Set whether this field element is in a concurrency check or not.
     * @param flag - if <code>true</code>, the field element is marked as
     * being in a concurrency check; otherwise, it is not
     * @exception ModelException if impossible
     */
    public void setInConcurrencyCheck (boolean flag) throws ModelException
    {
        Boolean old = JavaTypeHelper.valueOf(isInConcurrencyCheck());
        Boolean newFlag = JavaTypeHelper.valueOf(flag);

        try
        {
            fireVetoableChange(PROP_IN_CONCURRENCY_CHECK, old, newFlag);
            setProperty(flag, IN_CONCURRENCY_CHECK);
            firePropertyChange(PROP_IN_CONCURRENCY_CHECK, old, newFlag);
        }
        catch (PropertyVetoException e)
        {
            throw new ModelVetoException(e);
        }
    }

    /** Determines whether this field element is a version field or not.
     * @return <code>true</code> if the field is a version field,
     * <code>false</code> otherwise
     */
    public boolean isVersion () { return _isVersion; }

    /** Set whether this field element is a version field or not.
     * @param flag - if <code>true</code>, the field element is marked
     * as a version field; otherwise, it is not
     * @exception ModelException if impossible
     */
    public void setVersion (boolean flag) throws ModelException
    {
        Boolean old = JavaTypeHelper.valueOf(isVersion());
        Boolean newFlag = JavaTypeHelper.valueOf(flag);

        try
        {
            fireVetoableChange(PROP_VERSION_FIELD, old, newFlag);
            _isVersion = flag;
            firePropertyChange(PROP_VERSION_FIELD, old, newFlag);
        }
        catch (PropertyVetoException e)
        {
            throw new ModelVetoException(e);
        }
    }

    //======================= fetch group handling ======================

    /** Get the fetch group of this field element.
     * @return the fetch group, one of {@link #GROUP_DEFAULT},
     * {@link #GROUP_NONE}, or anything less than or equal to
     * {@link #GROUP_INDEPENDENT}
     */
    public int getFetchGroup () { return _fetchGroup; }

    /** Set the fetch group of this field element.
     * @param group - an integer indicating the fetch group, one of:
     * {@link #GROUP_DEFAULT}, {@link #GROUP_NONE}, or anything less than or
     * equal to {@link #GROUP_INDEPENDENT}
     * @exception ModelException if impossible
     */
    public void setFetchGroup (int group) throws ModelException
    {
        Integer old = new Integer(getFetchGroup());
        Integer newGroup = new Integer(group);

        try
        {
            fireVetoableChange(PROP_FETCH_GROUP, old, newGroup);
            setFetchGroupInternal(group);
            firePropertyChange(PROP_FETCH_GROUP, old, newGroup);
        }
        catch (PropertyVetoException e)
        {
            throw new ModelVetoException(e);
        }
    }

    /** Set the fetch group of this field element.  Meant to be used in the
     * constructor and by subclasses when there should be no exceptions and
     * no property change events fired.
     * @param group - an integer indicating the fetch group, one of:
     * {@link #GROUP_DEFAULT}, {@link #GROUP_NONE}, or anything less than or
     * equal to {@link #GROUP_INDEPENDENT}
     */
    protected void setFetchGroupInternal (int group)
    {
        _fetchGroup = group;
    }

    //========================== column handling ==========================

    /** Returns the list of column names to which this mapping field is
     * mapped.
     * @return the names of the columns mapped by this mapping field
     */
    public ArrayList getColumns ()
    {
        if (_columns == null)
            _columns = new ArrayList();

        return _columns;
    }

    /** Adds a column to the list of columns mapped by this mapping field.
     * @param column column element to be added to the mapping
     * @exception ModelException if impossible
     */
    public void addColumn (DBMemberElement column) throws ModelException
    {
        if (column != null)
        {
            ArrayList columns = getColumns();
            String columnName = NameUtil.getRelativeMemberName(
                column.getName().getFullName());

            if (!columns.contains(columnName))
            {
                try
                {
                    fireVetoableChange(PROP_COLUMNS, null, null);
                    columns.add(columnName);
                    firePropertyChange(PROP_COLUMNS, null, null);

                    // sync up runtime's object list too
                    _columnObjects = null;
                }
                catch (PropertyVetoException e)
                {
                    throw new ModelVetoException(e);
                }
            }
            else
            {
                // this part was blank -- do we want an error or skip here?
            }
        }
        else
        {
            throw new ModelException(I18NHelper.getMessage(getMessages(),
                "mapping.element.null_argument"));                // NOI18N
        }
    }

    /** Removes a column from the list of columns mapped by this mapping field.
     * This method overrides the one in MappingFieldElement to
     * remove the argument from the associated columns if necessary.
     * @param columnName the relative name of the column to be removed from
     * the mapping
     * @exception ModelException if impossible
     */
    public void removeColumn (String columnName) throws ModelException
    {
        if (columnName != null)
        {
            try
            {
                fireVetoableChange(PROP_COLUMNS, null, null);

                if (!getColumns().remove(columnName))
                {
                    throw new ModelException(
                        I18NHelper.getMessage(getMessages(),
                        "mapping.element.element_not_removed",         // NOI18N
                        columnName));
                }

                firePropertyChange(PROP_COLUMNS, null, null);

                // sync up runtime's object list too
                _columnObjects = null;
            }
            catch (PropertyVetoException e)
            {
                throw new ModelVetoException(e);
            }
        }
    }

    protected boolean isMappedToTable (MappingTableElement table)
    {
        String tableName = table.getName();
        Iterator iterator = getColumns().iterator();

        while (iterator.hasNext())
        {
            String columnName = iterator.next().toString();

            if (NameUtil.getTableName(columnName).equals(tableName))
                return true;
        }

        return false;
    }

    //============= extra object support for runtime ========================

    /** Returns the list of columns (ColumnElements) to which this mapping
     * field is mapped.  This method should only be used by the runtime.
     * @return the columns mapped by this mapping field
     */
    public ArrayList getColumnObjects ()
    {
        //@olsen: compute objects on access
        if (_columnObjects == null)
        {
            //@olsen: calculate the column objects based on
            //        the column names as stored in _columns
            //_columnObjects = new ArrayList();
            _columnObjects = MappingClassElementImpl.toColumnObjects(
                getDeclaringClass().getDatabaseRoot(), getColumns());
        }

        return _columnObjects;
    }

    //============= delegation to PersistenceFieldElement ===========

    final PersistenceFieldElement getPersistenceFieldElement ()
    {
        return ((MappingClassElementImpl)getDeclaringClass()).
            getPersistenceElement().getField(getName());
    }

    /** Computes the field number of this field element.
     * @return the field number of this field
     */
    public int getFieldNumber ()
    {
        return getPersistenceFieldElement().getFieldNumber();
    }

    /** Returns the array of concurrency groups to which this field belongs.
     * @return the concurrency groups in which this field participates
     * @see PersistenceClassElement#getConcurrencyGroups
     */
    public ConcurrencyGroupElement[] getConcurrencyGroups ()
    {
        return getPersistenceFieldElement().getConcurrencyGroups();
    }

    //================ convenience methods for properties bits ===============

    private boolean getProperty (int propertyBit)
    {
        return ((getProperties() & propertyBit) > 0);
    }

    public void setProperty (boolean flag, int propertyBit)
    {
        _properties =
            (flag) ? (_properties | propertyBit) : (_properties & ~propertyBit);
    }

    //================= properties not available in interface ================

    public int getProperties () { return _properties;}

    public boolean getLogOnAccess () { return getProperty(LOG_ON_ACCESS); }

    public void setLogOnAccess (boolean flag)
    {
        setProperty(flag, LOG_ON_ACCESS);
    }

    public boolean getLogOnUpdate () { return getProperty(LOG_ON_UPDATE); }

    public void setLogOnUpdate (boolean flag)
    {
        setProperty(flag, LOG_ON_UPDATE);
    }

    public boolean getObserveOnAccess ()
    {
        return getProperty(OBSERVE_ON_ACCESS);
    }

    public void setObserveOnAccess (boolean flag)
    {
        setProperty(flag, OBSERVE_ON_ACCESS);
    }

    public boolean getRecordOnUpdate ()
    {
        return getProperty(RECORD_ON_UPDATE);
    }

    public void setRecordOnUpdate (boolean flag)
    {
        setProperty(flag, RECORD_ON_UPDATE);
    }

    public boolean getModifyBeforeImageOnUpdate ()
    {
        return getProperty(MOD_BI_ON_UPDATE);
    }

    public void setModifyBeforeImageOnUpdate (boolean flag)
    {
        setProperty(flag, MOD_BI_ON_UPDATE);
    }

    public boolean getReferentialIntegrityUpdates ()
    {
        return getProperty(REF_INTEGRITY_UPDATES);
    }

    public void setReferentialIntegrityUpdates (boolean flag)
    {
        setProperty(flag, REF_INTEGRITY_UPDATES);
    }

    public boolean getSendBeforeImage ()
    {
        return getProperty(SEND_BEFORE_IMAGE);
    }
    public void setSendBeforeImage (boolean flag)
    {
        setProperty(flag, SEND_BEFORE_IMAGE);
    }

    public int getCloneDepth () { return (_properties & CLONE_MASK); }

    public void setCloneDepth (int cloneDepth)
    {
        if (cloneDepth < CLONE_FIELD || cloneDepth > CLONE_DEEP)
        {
        }

        _properties = _properties & ~CLONE_MASK | cloneDepth;
    }

    //============== extra methods for Boston -> Pilsen conversion ============

    /** Boston to Pilsen conversion.
     * This method converts the absolute column names to relative names.
     */
    protected void stripSchemaName ()
    {
        if (_columns != null)        // handle _columns
        {
            // Use ListIterator here, because I want to replace the value
            // stored in the ArrayList.  The ListIterator returned by
            // ArrayList.listIterator() supports the set method.
            ListIterator i = _columns.listIterator();

            while (i.hasNext())
                i.set(NameUtil.getRelativeMemberName((String)i.next()));
        }
    }
}
