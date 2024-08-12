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
 * FieldInfo.java
 *
 * Created on May 2, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore.query.util.type;

import com.sun.jdo.api.persistence.model.jdo.PersistenceFieldElement;
import com.sun.jdo.api.persistence.model.jdo.RelationshipElement;
import com.sun.jdo.api.persistence.support.JDOFatalInternalException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ResourceBundle;

import org.glassfish.persistence.common.I18NHelper;

/**
 *
 */
public class FieldInfo
{
    /**
     * The name of the field.
     */
    protected String name;

    /**
     * The corresponding classType object.
     */
    protected ClassType classType;

    /**
     * The reflection representation of the field.
     */
    protected Field field;

    /**
     * JDO model representation
     */
    protected PersistenceFieldElement pfe;

    /**
     * I18N support
     */
    protected final static ResourceBundle messages = I18NHelper.loadBundle(
        "com.sun.jdo.spi.persistence.support.sqlstore.query.Bundle", // NOI18N
        FieldInfo.class.getClassLoader());

    /**
     *
     */
    public FieldInfo (Field field, ClassType classType)
    {
        this.name = field.getName();
        this.classType = classType;
        this.field = field;
        this.pfe = (classType.pce != null) ? classType.pce.getField(this.name) : null;
    }

    /**
     * Checks whether this field is defined as persistent field.
     * @return true if the field is defined as persistent;
     * false otherwise.
     */
    public boolean isPersistent()
    {
        if (pfe != null)
        {
            return pfe.getPersistenceType() == PersistenceFieldElement.PERSISTENT;
        }
        return false;
    }

    /**
     * Checks whether this field is defined with the public modifier.
     * @return true if the field is defined as public;
     * false otherwise.
     */
    public boolean isPublic()
    {
        return (field != null) && Modifier.isPublic(field.getModifiers());
    }
    /**
     * Checks whether this field is defined with the static modifier.
     * @return true if the field is defined as static;
     * false otherwise.
     */
    public boolean isStatic()
    {
        return (field != null) && Modifier.isStatic(field.getModifiers());
    }

    /**
     *
     */
    public Field getField ()
    {
        return field;
    }

    /**
     *
     */
    public String getName ()
    {
        return name;
    }

    /**
     * Returns the Type representation of the type of the field.
     * @return field type
     */
    public Type getType()
    {
        if (field == null)
            return classType.typetab.errorType;

        Type ret = classType.typetab.checkType(field.getType());
        if (ret == null)
            ret = classType.typetab.errorType;
        return ret;

    }

    /**
     * Return the field number in the case of a field of a persistence capable class.
     */
    public int getFieldNumber()
    {
        if (pfe != null)
        {
            int index = pfe.getFieldNumber();
            if (index < 0)
                throw new JDOFatalInternalException(I18NHelper.getMessage(
                    messages, "query.util.type.fieldinfo.getfieldnumber.invalidfieldno", //NO18N
                    String.valueOf(index), name));
            return index;
        }
        else
        {
            throw new JDOFatalInternalException(I18NHelper.getMessage(
                messages, "query.util.type.fieldinfo.getfieldnumber.missingfieldelement", //NO18N
                name));
        }
    }

    /**
     * @return true if the field is a relationship field
     */
    public boolean isRelationship()
    {
        return ((pfe != null) && (pfe instanceof RelationshipElement));
    }

    /**
     * @return the associated class (meaning the "other side") of the relationship;
     * or null if this does not denote a relationship field.
     */
    public Type getAssociatedClass()
    {
        Type associatedClass = null;
        if ((pfe != null) && (pfe instanceof RelationshipElement))
        {
            String className = ((RelationshipElement)pfe).getElementClass();
            associatedClass = classType.typetab.checkType(className);
        }
        return associatedClass;
    }

}
