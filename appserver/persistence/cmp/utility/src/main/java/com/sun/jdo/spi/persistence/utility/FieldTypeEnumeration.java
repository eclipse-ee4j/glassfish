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
 * FieldTypeEnumeration
 *
 * Created on January 31, 2003
 */

package com.sun.jdo.spi.persistence.utility;

/**
 *
 */
public interface FieldTypeEnumeration
{

    //Not Enumerated
    public static final int NOT_ENUMERATED        = 0;

    //Primitive
    public static final int BOOLEAN_PRIMITIVE     = 1;
    public static final int CHARACTER_PRIMITIVE   = 2;
    public static final int BYTE_PRIMITIVE        = 3;
    public static final int SHORT_PRIMITIVE       = 4;
    public static final int INTEGER_PRIMITIVE     = 5;
    public static final int LONG_PRIMITIVE        = 6;
    public static final int FLOAT_PRIMITIVE       = 7;
    public static final int DOUBLE_PRIMITIVE      = 8;
    //Number
    public static final int BOOLEAN               = 11;
    public static final int CHARACTER             = 12;
    public static final int BYTE                  = 13;
    public static final int SHORT                 = 14;
    public static final int INTEGER               = 15;
    public static final int LONG                  = 16;
    public static final int FLOAT                 = 17;
    public static final int DOUBLE                = 18;
    public static final int BIGDECIMAL            = 19;
    public static final int BIGINTEGER            = 20;
    //String
    public static final int STRING                = 21;
    //Dates
    public static final int UTIL_DATE             = 22;
    public static final int SQL_DATE              = 23;
    public static final int SQL_TIME              = 24;
    public static final int SQL_TIMESTAMP         = 25;
    //Arrays
    public static final int ARRAY_BYTE_PRIMITIVE  = 51;

}
