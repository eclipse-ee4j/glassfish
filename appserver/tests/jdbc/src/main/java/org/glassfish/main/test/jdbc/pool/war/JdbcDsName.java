/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.jdbc.pool.war;

import jakarta.annotation.sql.DataSourceDefinition;

/**
 * Data sources using the same database.
 */
public final class JdbcDsName {
    /** DataSource created by {@link DataSourceDefinition}, no pool */
    public static final String JDBC_DS_1 = "jdbc/dsa1";

    /** DataSource created by {@link DataSourceDefinition}, no pool, same definition as {@link #JDBC_DS_1} */
    public static final String JDBC_DS_2 = "jdbc/dsa2";

    /** DataSource using the database pool domain-pool-A */
    public static final String JDBC_DS_POOL_A = "jdbc/dsPoolA";
    /** DataSource using the database pool domain-pool-B */
    public static final String JDBC_DS_POOL_B = "jdbc/dsPoolB";
}
