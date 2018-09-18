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
 * Type.java
 *
 * Created on March 8, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore.query.util.type;

import com.sun.jdo.spi.persistence.utility.FieldTypeEnumeration;

/**
 *
 * @author  Michael Bouschen
 * @version 0.1
 */
public abstract class Type
{
    /**
     * The name of the type represented by this object.
     */
    protected String name;

    /**
     * The corresponding class object.
     */
    protected Class clazz;

    /**
     * The FieldTypeEnumeration constant for this Type.
     */
    protected int enumType;

    /**
     * Creates a new Type object with the specified name.
     * @param name name of the type represented by this
     * @param clazz the class object for this type
     */
    public Type(String name, Class clazz)
    {
        this(name, clazz, FieldTypeEnumeration.NOT_ENUMERATED);
    }

    /**
     * Creates a new Type object with the specified name.
     * @param name name of the type represented by this
     * @param clazz the class object for this type
     * @param enumType the FieldTypeEnumeration value for this type
     */
    public Type(String name, Class clazz, int enumType)
    {
        this.name = name;
        this.clazz = clazz;
        this.enumType = enumType;
    }

    /**
     * Returns the name of the type.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the corresponding class object.
     */
    public Class getJavaClass() {
        return this.clazz;
    }

    /**
     * Checks type compatibility.
     * @param type the type this is checked with.
     * @return true if this is compatible with type;
     * false otherwise.
     */
    public abstract boolean isCompatibleWith(Type type);

    /**
     * Returns whether this represents a type with an
     * defined order.
     * @return true if an order is defined for this;
     * false otherwise.
     */
    public boolean isOrderable()
    {
        return false;
    }

    /**
     * Returns the FieldTypeEnumeration value for this type.
     */
    public int getEnumType()
    {
        return enumType;
    }

    /**
     * Representation of this type as a string.
     */
    public String toString()
    {
        return getName();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * Two types are equal if their names are equal.
     */
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        else if (obj instanceof Type)
            return this.name.equals(((Type)obj).name);
        else
            return false;
    }


}
