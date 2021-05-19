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
 * ParameterInfo
 *
 * Created on January 31, 2003
 */

package com.sun.jdo.spi.persistence.utility;

//XXX FIXME This file may need to move under support/sqlstore.
public class ParameterInfo
{
    /**
     * Parameter index.
     * The index corresponds to JDO QL parameters.
     */
    private final int index;

    /** Parameter type. See FieldTypeEnumeration for possible values. */
    private final int type;

    /**
     * Associated field to a parameter for runtime processing.
     * This is defined if and only if the corresponding subfilter is of
     * the form: field [relational op] _jdoParam or
     *           _jdoParam [relational op] field
     * Otherwise, this is null.
     */
    private final String associatedField;

    /** Constructor */
    public ParameterInfo(int index, int type)
    {
        this(index, type, null);
    }

    /**
     * Constructs a new ParameterInfo with the specified index, type and
     * associatedField.
     * @param index
     * @param type
     * @param associatedField
     */
    public ParameterInfo(int index, int type, String associatedField)
    {
        this.index = index;
        this.type = type;
        this.associatedField = associatedField;
    }

    /** Returns the parameter index. */
    public int getIndex()
    {
        return index;
    }

    /** Returns the parameter type. See FieldTypeEnumeration for possible values. */
    public int getType()
    {
        return type;
    }

    /**
     * Returns the associated field.
     */
    public String getAssociatedField()
    {
        return associatedField;
    }
}
