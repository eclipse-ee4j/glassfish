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
 * ClassType.java
 *
 * Created on March 8, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore.query.util.type;

import com.sun.jdo.api.persistence.model.jdo.PersistenceClassElement;
import com.sun.jdo.api.persistence.model.jdo.PersistenceFieldElement;
import com.sun.jdo.api.persistence.support.JDOFatalUserException;
import com.sun.jdo.spi.persistence.utility.FieldTypeEnumeration;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author  Michael Bouschen
 * @version 0.1
 */
public class ClassType
    extends Type
{
    /**
     * The associated type table.
     */
    protected TypeTable typetab;

    /**
     *
     */
    protected Map fieldInfos;

    /**
     *
     */
    protected PersistenceClassElement pce;

    /**
     *
     */
    public ClassType(String name, Class clazz, int enumType, TypeTable typetab)
    {
        super(name, clazz, enumType);
        this.typetab = typetab;
        this.fieldInfos = new HashMap();
        // get JDO model element if available
        ClassLoader classLoader = clazz.getClassLoader();
        if (classLoader != null)
        {
            try
            {
                this.pce = typetab.model.getPersistenceClass(name, classLoader);
            }
            catch (IllegalArgumentException ex)
            {
                // IllegalArgumentException indicates class loader problem
                throw new JDOFatalUserException(ex.getMessage());
            }
        }
    }

    /**
     *
     */
    public ClassType(String name, Class clazz, TypeTable typetab)
    {
        this(name, clazz, FieldTypeEnumeration.NOT_ENUMERATED, typetab);
    }

    /**
     * Checks the compatibility of this with the specified type.
     * A ClassType object is compatible to
     * errorType, to the type of null (NullType), to itself
     * and to a super class (direct or indirect).
     * @param type type for compatibility check
     * @return true if this is compatible with type;
     * false otherwise.
     * @see Type#isCompatibleWith(Type)
     */
    public boolean isCompatibleWith(Type type)
    {
        boolean result = false;
        if (type instanceof ClassType)
        {
            result = ((ClassType)type).clazz.isAssignableFrom(clazz);
        }
        return result;
    }

    /**
     * Returns whether this represents a type with an
     * defined order.
     * @return true if an order is defined for this;
     * false otherwise.
     */
        public boolean isOrderable()
        {
        Type comparable = typetab.checkType("java.lang.Comparable"); //NOI18N
        return isCompatibleWith(comparable);
        }

    /**
     * Returns true if this is defined as persistence capable class.
     * @return true if this is a persistence capable class;
     * false otherwise.
     */
        public boolean isPersistenceCapable()
        {
        return (pce != null);
        }

    // --------------------
    // Field handling
    // --------------------

    /**
     * Returns an array of fieldInfos for all declared fields.
     */
    public FieldInfo[] getFieldInfos()
    {
        // Initialize the fieldInfos map with the field declared for this class.
        // NOTE, this code does not work for inheritance!
        //Field[] fields = clazz.getDeclaredFields();

        final Class cl = clazz;

        Field[] fields =  (Field[]) AccessController.doPrivileged(new PrivilegedAction() {
                                public Object run () {
                                        return cl.getDeclaredFields();
                                }
                        });

        synchronized(fieldInfos) {
            for (int i = 0; i < fields.length; i++)
            {
                String fieldName = fields[i].getName();
                FieldInfo fieldInfo = (FieldInfo)fieldInfos.get(fieldName);
                if (fieldInfo == null)
                    fieldInfos.put(fieldName, new FieldInfo(fields[i], this));
            }
        }
        return (FieldInfo[])fieldInfos.values().toArray(new FieldInfo[0]);
    }

    /**
     * Return FieldInfo object for the field with the specified name.
     */
    public FieldInfo getFieldInfo(final String fieldName)
    {
        synchronized(fieldInfos) {
            FieldInfo fieldInfo = (FieldInfo)fieldInfos.get(fieldName);
            if (fieldInfo == null)
            {
                // NOTE, no inheritance!
                final Class cl = clazz;
                Field field = (Field) AccessController.doPrivileged(new PrivilegedAction()
                    {
                        public Object run ()
                        {
                            try
                            {
                                return cl.getDeclaredField(fieldName);
                            }
                            catch (NoSuchFieldException ex)
                            {
                                return null; // do nothing, just return null
                            }
                        }
                    });

                if (field != null)
                {
                    fieldInfo = new FieldInfo(field, this);
                    fieldInfos.put(fieldName, fieldInfo);
                }
            }
            return fieldInfo;
        }
    }

    /**
     * Return the list of key field names
     */
    public List getKeyFieldNames()
    {
        if (pce != null)
        {
            PersistenceFieldElement[] persistentFields = pce.getFields();
            List names = new ArrayList();
            for (int i = 0; i < persistentFields.length; i++)
            {
                if (persistentFields[i].isKey())
                    names.add(persistentFields[i].getName());
            }
            return names;
        }
        return null;
    }

}

