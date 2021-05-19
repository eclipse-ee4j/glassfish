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
 * NumericType.java
 *
 * Created on March 8, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore.query.util.type;

/**
 * This class is the super class for all integral and floating point types.
 *
 * @author  Michael Bouschen
 * @version 0.1
 */
public abstract class NumericType
    extends PrimitiveType
    implements NumberType
{
    /**
     *
     */
    public NumericType(String name, Class clazz, int enumType)
    {
        super(name, clazz, enumType);
    }

    /**
     * A numeric type defines an ordering.
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
    public abstract Number getValue(Number value);

    /**
     * Returns -value.
     * @param value value to be negated
     * @return -value
     */
    public abstract Number negate(Number value);

}
