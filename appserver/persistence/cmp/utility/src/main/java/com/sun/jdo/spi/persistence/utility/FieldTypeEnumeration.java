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
    int NOT_ENUMERATED        = 0;

    //Primitive
    int BOOLEAN_PRIMITIVE     = 1;
    int CHARACTER_PRIMITIVE   = 2;
    int BYTE_PRIMITIVE        = 3;
    int SHORT_PRIMITIVE       = 4;
    int INTEGER_PRIMITIVE     = 5;
    int LONG_PRIMITIVE        = 6;
    int FLOAT_PRIMITIVE       = 7;
    int DOUBLE_PRIMITIVE      = 8;
    //Number
    int BOOLEAN               = 11;
    int CHARACTER             = 12;
    int BYTE                  = 13;
    int SHORT                 = 14;
    int INTEGER               = 15;
    int LONG                  = 16;
    int FLOAT                 = 17;
    int DOUBLE                = 18;
    int BIGDECIMAL            = 19;
    int BIGINTEGER            = 20;
    //String
    int STRING                = 21;
    //Dates
    int UTIL_DATE             = 22;
    int SQL_DATE              = 23;
    int SQL_TIME              = 24;
    int SQL_TIMESTAMP         = 25;
    //Arrays
    int ARRAY_BYTE_PRIMITIVE  = 51;

}
