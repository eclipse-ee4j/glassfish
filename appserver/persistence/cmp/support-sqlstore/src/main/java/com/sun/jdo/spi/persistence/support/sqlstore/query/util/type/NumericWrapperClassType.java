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
 * NumericWrapperClassType.java
 *
 * Created on April 14, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore.query.util.type;

/**
 *
 * @author  Michael Bouschen
 * @version 0.1
 */
public class NumericWrapperClassType
    extends WrapperClassType
    implements NumberType
{
    /**
     *
     */
    public NumericWrapperClassType(String name, Class clazz, int enumType,
                                   PrimitiveType primitiveType, TypeTable typetab)
    {
        super(name, clazz, enumType, primitiveType, typetab);
    }

    /**
     * A numeric wrapper class type defines an ordering.
     */
    public boolean isOrderable()
    {
        return true;
    }

    /**
     * Converts the specified value into a value of this numeric type.
     * E.g. an Integer is converted into a Double, if this represents
     * the numeric type double.
     * @param value value to be converted
     * @return converted value
     */
    public Number getValue(Number value)
    {
        return ((NumericType)primitiveType).getValue(value);
    }

    /**
     * Returns -value.
     * @param value value to be negated
     * @return -value
     */
    public Number negate(Number value)
    {
        return ((NumericType)primitiveType).negate(value);
    }
}
