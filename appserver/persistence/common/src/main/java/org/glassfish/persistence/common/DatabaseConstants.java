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

package org.glassfish.persistence.common;

/**
 * @author Marina Vatkina
 * This interface defines string constants used by the database generation.
 */
public interface DatabaseConstants {

    // Substrings used to construct file names.
    char NAME_SEPARATOR = '_'; // NOI18N

    String JDBC_FILE_EXTENSION = ".jdbc"; // NOI18N
    String SQL_FILE_EXTENSION = ".sql"; // NOI18N
    String CREATE             = "create"; // NOI18N
    String DROP               = "drop"; // NOI18N
    String DDL                = "DDL"; // NOI18N

    // Known file name suffixes.
    String CREATE_DDL_JDBC_FILE_SUFFIX = NAME_SEPARATOR + CREATE + DDL + JDBC_FILE_EXTENSION;
    String DROP_DDL_JDBC_FILE_SUFFIX   = NAME_SEPARATOR + DROP + DDL + JDBC_FILE_EXTENSION;
    String CREATE_SQL_FILE_SUFFIX     = NAME_SEPARATOR + CREATE + SQL_FILE_EXTENSION;
    String DROP_SQL_FILE_SUFFIX       = NAME_SEPARATOR + DROP + SQL_FILE_EXTENSION;

    /**
     * Used as key to set propety in DeploymentContext indicating to override JTA dataource
     */
    String JTA_DATASOURCE_JNDI_NAME_OVERRIDE = "org.glassfish.jta.datasource.jndi.name";

    // Flag used to indicate a database generation mode.
    public static final String JAVA_TO_DB_FLAG = "java-to-database"; // NOI18N
}
