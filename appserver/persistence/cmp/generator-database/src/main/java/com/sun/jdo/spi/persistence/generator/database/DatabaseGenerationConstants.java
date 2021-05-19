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

package com.sun.jdo.spi.persistence.generator.database;

/**
 * This interface defines string constants used by the database generation.
 *
 * @author Jie Leng, Dave Bristor
 */
// XXX Rename this class to "Constants"
interface DatabaseGenerationConstants {

    /** Separator between property name bases and indicators. */
    static final char DOT = '.';

    /** Indicator that property is for a maximum length. */
    static final String INDICATOR_MAXIMUM_LENGTH = "maximum-length"; //NOI18N

    /**
     * (Partial) indicator that property is for attributes of SQL.  The prefix
     * is recognized by alone by itself in MappingFile, and is used by other
     * DatabaseGenerationConstants.
     */
    static final String INDICATOR_JDBC_PREFIX = "jdbc"; //NOI18N

    /** Indicator that property designates the length of mapped SQL type. */
    static final String INDICATOR_JDBC_LENGTH =
        INDICATOR_JDBC_PREFIX + "-" + INDICATOR_MAXIMUM_LENGTH; //NOI18N

    /** Indicator that property designates the nullability of mapped SQL type. */
    static final String INDICATOR_JDBC_NULLABLE =
        INDICATOR_JDBC_PREFIX + "-nullable"; //NOI18N

    /** Indicator that property designates the precision of mapped SQL type. */
    static final String INDICATOR_JDBC_PRECISION =
        INDICATOR_JDBC_PREFIX + "-precision"; //NOI18N

    /** Indicator that property designates the scale of mapped SQL type. */
    static final String INDICATOR_JDBC_SCALE =
        INDICATOR_JDBC_PREFIX + "-scale"; //NOI18N

    /** Indicator that property designates the type of a mapped SQL type. */
    static final String INDICATOR_JDBC_TYPE =
        INDICATOR_JDBC_PREFIX + "-type"; //NOI18N
}
