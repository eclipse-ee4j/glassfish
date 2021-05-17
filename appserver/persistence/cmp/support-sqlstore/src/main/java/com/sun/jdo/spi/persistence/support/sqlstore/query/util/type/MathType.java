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
 * MathType.java
 *
 * Created on August 24, 2001
 */

package com.sun.jdo.spi.persistence.support.sqlstore.query.util.type;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * This class represents the types java.math.BigDecimal and java.math.BigInteger.
 *
 *
 * @author  Michael Bouschen
 * @version 0.1
 */
public class MathType
    extends ClassType
    implements NumberType
{
    /**
     *
     */
    public MathType(String name, Class clazz, int enumType, TypeTable typetab)
    {
        super(name, clazz, enumType, typetab);
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
     * E.g. an Integer is converted into a BigDecimal, if this represents
     * the type BigDecimal.
     * @param value value to be converted
     * @return converted value
     */
    public Number getValue(Number value)
    {
        Number ret = null;

        if (value == null)
            ret = null;
        else if ("java.math.BigDecimal".equals(getName()))
        {
            if (value instanceof BigDecimal)
                ret = value;
            else if (value instanceof BigInteger)
                ret = new BigDecimal((BigInteger)value);
            else if (value instanceof Double)
                ret = new BigDecimal(((Double)value).toString());
            else if (value instanceof Float)
                ret = new BigDecimal(((Float)value).toString());
            else if (value instanceof Number)
                ret = BigDecimal.valueOf(((Number)value).longValue());
        }
        else if ("java.math.BigInteger".equals(getName()))
        {
            if (value instanceof BigInteger)
                ret = value;
            else if (value instanceof Double)
                ret = (new BigDecimal(((Double)value).toString())).toBigInteger();
            else if (value instanceof Float)
                ret = (new BigDecimal(((Float)value).toString())).toBigInteger();
            else if (value instanceof Number)
                ret = BigInteger.valueOf(((Number)value).longValue());
        }

        return ret;
    }

    /**
     * Returns -value.
     * @param value value to be negated
     * @return -value
     */
    public Number negate(Number value)
    {
        Number ret = null;

        if (value == null)
            ret = null;
        else if ("java.math.BigDecimal".equals(getName()))
        {
            if (value instanceof BigDecimal)
                ret = ((BigDecimal)value).negate();
            else if (value instanceof BigInteger)
                ret = new BigDecimal(((BigInteger)value).negate());
            else if (value instanceof Double)
                ret = (new BigDecimal(((Double)value).toString())).negate();
            else if (value instanceof Float)
                ret = (new BigDecimal(((Float)value).toString())).negate();
            else if (value instanceof Number)
                ret = BigDecimal.valueOf(-((Number)value).longValue());
        }
        else if ("java.math.BigInteger".equals(getName()))
        {
            if (value instanceof BigInteger)
                ret = ((BigInteger)value).negate();
            else if (value instanceof Double)
                ret = (new BigDecimal(((Double)value).toString())).negate().toBigInteger();
            else if (value instanceof Float)
                ret = (new BigDecimal(((Float)value).toString())).negate().toBigInteger();
            else if (value instanceof Number)
                ret = BigInteger.valueOf(-((Number)value).longValue());
        }

        return ret;
    }

}
