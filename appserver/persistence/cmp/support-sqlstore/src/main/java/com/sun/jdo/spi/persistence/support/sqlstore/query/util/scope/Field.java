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
 * Field.java
 *
 * Created on March 21, 2001
 */

package com.sun.jdo.spi.persistence.support.sqlstore.query.util.scope;

import org.glassfish.persistence.common.I18NHelper;

import com.sun.jdo.spi.persistence.support.sqlstore.query.util.type.FieldInfo;

/**
 * An object of class Field is used if an identifier
 * denotes a field.
 *
 * @author  Michael Bouschen
 * @version 0.1
 */
public class Field
  extends Definition
{
    /**
     * The corresponding field info.
     */
    protected FieldInfo fieldInfo;

    /**
     *
     */
    public Field(FieldInfo fieldInfo)
    {
        super(fieldInfo.getType());
        this.fieldInfo = fieldInfo;
    }

    /**
     *
     */
    public String getName()
    {
        return I18NHelper.getMessage(messages, "scope.field.getname.name"); //NOI18N
    }

    /**
     * Returns the corresponding field info.
     */
    public FieldInfo getFieldInfo()
    {
        return fieldInfo;
    }

    /**
     *
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("Field("); //NOI18N
        buf.append(scope);
        buf.append(", "); //NOI18N
        buf.append(type);
        buf.append(")"); //NOI18N
        return buf.toString();
    }
}
