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
 * ConstraintParamIndex.java
 *
 * Created on March 12, 2002
 * @author  Daniel Tonn
 */

package com.sun.jdo.spi.persistence.support.sqlstore.sql.constraint;

import com.sun.jdo.spi.persistence.support.sqlstore.model.LocalFieldDesc;

/**
 * Constraint which represent an index of a query parameter.
 * This index is put to the stack and used
 * to bind the real value of the parameter to the query.
 */
public class ConstraintParamIndex extends ConstraintValue
{

    /**
     * com.sun.jdo.spi.persistence.utility.FieldTypeEnumeration constant
     * for type of this parameter.
     *
     */
    int enumType;

    /**
     * Constructor.
     * @param index parameter index
     * @param enumType type of this parameter
     * @param localField the localField to which this parameter is bound.
     */
    public ConstraintParamIndex(int index, int enumType, LocalFieldDesc localField) {
        super(new Integer(index), localField);
        this.enumType = enumType;
    }

    /**
     * Get type of this parameter.
     */
    public int getType() {
        return enumType;
    }

    public Integer getIndex() {
        return (Integer) getValue();
    }

}
