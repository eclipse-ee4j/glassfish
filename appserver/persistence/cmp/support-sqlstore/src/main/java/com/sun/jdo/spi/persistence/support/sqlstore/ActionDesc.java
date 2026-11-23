/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.jdo.spi.persistence.support.sqlstore;


/**
 * <P>This interface defines the constraint operation constants that are
 * recognized by the <code>addConstraint</code> method of the interface <code>RetrieveDesc</code>.
 */
public interface ActionDesc {
    // LAST VALUE USED: 63

    // Constants for life cycle events

    int LOG_CREATE = 1;

    int LOG_DESTROY = 2;

    int LOG_UPDATE = 3;

    int LOG_NOOP = 4;

    // Select operators

    /**
     * Unary operator for taking the absolute value.
     */
    int OP_ABS = 1;

    /**
     * Binary operator for adding two values.
     */
    int OP_ADD = 2;

    /**
     * Logical operator AND.
     */
    int OP_AND = 3;

    /**
     */
    int OP_APPROX = 4;

    /**
     */
    int OP_FIELD = 5;

    /**
     */
    int OP_BETWEEN = 6;

    /**
     * Qualifies the query with DISTINCT
     */
    int OP_DISTINCT = 7;

    /**
     * Binary operator for dividing two values
     */
    int OP_DIV = 8;

    /**
     * Equality operator.
     */
    int OP_EQ = 9;

    /**
     */
    int OP_EQ_CLASS = 41;

    /**
     */
    int OP_EQUIJOIN = 10;

    /**
     */
    int OP_FOR_UPDATE = 11;

    /**
     */
    int OP_GE = 12;

    /**
     */
    int OP_GT = 13;

    /**
     */
    int OP_IN = 14;

    /**
     * Relational operator for less-than-and-equal.
     */
    int OP_LE = 15;

    /**
     */
    int OP_LEFTJOIN = 16;

    /**
     */
    int OP_LENGTH = 17;

    /**
     */
    int OP_LENGTHB = 18;

    /**
     * Pattern matching operator.
     */
    int OP_LIKE = 19;

    /**
     */
    int OP_LOWER = 20;

    /**
     * Relational operator for less-than.
     */
    int OP_LT = 21;

    /**
     */
    int OP_LTRIM = 22;

    /**
     */
    int OP_MAX_ROWS = 23;

    /**
     * Binary operator for multiplying two values.
     */
    int OP_MUL = 24;

    /**
     * Inequality operator.
     */
    int OP_NE = 25;

    /**
     * Unary negation operator.
     */
    int OP_NOT = 26;

    /**
     * Unary operator for checking non-null value.
     */
    int OP_NOTNULL = 27;

    /**
     * Unary operator for checking null value.
     */
    int OP_NULL = 28;

    /**
     * Logical operator OR.
     */
    int OP_OR = 29;

    /**
     * Order the result by ascending order.
     */
    int OP_ORDERBY = 30;

    /**
     * Order the result by descending order.
     */
    int OP_ORDERBY_DESC = 31;

    /**
     */
    int OP_PARAMETER_COUNT = 32;

    /**
     */
    int OP_RIGHTJOIN = 33;

    /**
     * Unary operator for trimming trailing blanks in a string.
     */
    int OP_RTRIM = 34;

    /**
     */
    int OP_RTRIMFIXED = 43;

    /**
     */
    int OP_SOUNDEX = 35;

    /**
     * square root of a number
     */
    int OP_SQRT = 47;

    /**
     * Binary operator for subtracting one value from another.
     */
    int OP_SUB = 36;

    /**
     * Operator for Substring with two arguments: string, start.
     */
    int OP_SUBSTR = 37;

    /**
     */
    int OP_SUBSTRB = 38;

    /**
     */
    int OP_UPPER = 39;

    /**
     */
    int OP_VALUE = 40;

    /**
     */
    int OP_NONKEY = 42;

    /**
     * String concatenation operator
     */
    int OP_CONCAT = 44;

    /**
     * Operator for not exists subquery
     */
    int OP_NOTEXISTS = 45;

    /**
     * Operator for exists subquery
     */
    int OP_EXISTS = 46;

    /**
     * Operator for like with escape
     */
    int OP_LIKE_ESCAPE = 48;

    /**
     * Operator for Substring with three arguments: string, start, length.
     */
    int OP_SUBSTRING = 49;

    /**
     * Operator for position
     */
    int OP_POSITION = 50;

    /**
     * Operator for position with start parameter
     */
    int OP_POSITION_START = 51;

    /**
     * Operator for a non relationship join.
     */
    int OP_NONREL_JOIN = 52;

    /**
     * Operator for query parameters
     */
    int OP_PARAMETER = 53;

    /**
     * Operator for queries on nullable columns mapped to primitive fields
     */
    int OP_MAYBE_NULL = 54;

    /**
     * Operator for NOT IN
     */
    int OP_NOTIN = 55;

    /**
     * Operator for null comparision by function.
     */
    int OP_NULL_COMPARISION_FUNCTION = 56;

    /**
     * Binary operator for MOD.
     */
    int OP_MOD = 57;

    /**
     * Operator for AVG aggregate function.
     */
    int OP_AVG = 58;

    /**
     * Operator for MIN aggregate function.
     */
    int OP_MIN = 59;

    /**
     * Operator for SUM aggregate function.
     */
    int OP_SUM = 60;

    /**
     * Operator for MAX aggregate function.
     */
    int OP_MAX = 61;

    /**
     * Operator for COUNT aggregate function.
     */
    int OP_COUNT = 62;

    /**
     * Operator for COUNT aggregate function on pc objects.
     */
    int OP_COUNT_PC = 63;

    /**
     */
    Class<?> getPersistenceCapableClass();

}
