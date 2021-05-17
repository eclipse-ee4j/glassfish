/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.gjc.monitoring;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Interface that contains all the constants used in the jdbc-ra module.
 *
 * @author Shalini M
 */
public interface JdbcRAConstants {

    /**
     * Represents the statement cache hit monitoring event.
     */
    public static final String STATEMENT_CACHE_HIT = "statementCacheHitEvent";

    /**
     * Represents the statement cache miss monitoring event.
     */
    public static final String STATEMENT_CACHE_MISS = "statementCacheMissEvent";

    /**
     * Represents caching of sql query event.
     */
    public static final String TRACE_SQL = "traceSQLEvent";

    public static final String POTENTIAL_STATEMENT_LEAK = "potentialStatementLeakEvent";

    /**
     * Represents module provider name.
     */
    public static final String GLASSFISH = "glassfish";

    /**
     * Represents the module name
     */
    public static final String JDBCRA = "jdbcra";

    /**
     * Represents probe provider name
     */
    public static final String STATEMENT_CACHE_PROBE = "statementcache";

    /**
     * Represents probe provider name for sql tracing.
     */
    public static final String SQL_TRACING_PROBE = "sqltracing";

    public static final String STATEMENT_LEAK_PROBE = "statementleak";

    /**
     * Dotted name used in monitoring for Statement caching.
     */
    public static final String STATEMENT_CACHE_DOTTED_NAME = GLASSFISH + ":" +
            JDBCRA + ":" + STATEMENT_CACHE_PROBE + ":";

    /**
     * Dotted name used in monitoring for Sql Tracing.
     */
    public static final String SQL_TRACING_DOTTED_NAME = GLASSFISH + ":" +
            JDBCRA + ":" + SQL_TRACING_PROBE + ":";

    public static final String STATEMENT_LEAK_DOTTED_NAME = GLASSFISH + ":" +
            JDBCRA + ":" + STATEMENT_LEAK_PROBE + ":";

    /**
     * Represents top queries to report.
     */
    public static final String REPORT_QUERIES = "reportQueriesEvent";

    /**
     * List of valid method names that can be used for sql trace monitoring.
     */
    public static final List<String> validSqlTracingMethodNames =
            Collections.unmodifiableList(
            Arrays.asList(
                "nativeSQL",
                "prepareCall",
                "prepareStatement",
                "addBatch",
                "execute",
                "executeQuery",
                "executeUpdate"
            ));
}
