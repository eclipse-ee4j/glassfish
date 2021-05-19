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

import com.sun.gjc.util.SQLTrace;
import com.sun.gjc.util.SQLTraceCache;
import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.StringStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.external.statistics.impl.StatisticImpl;
import org.glassfish.external.statistics.impl.StringStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.resourcebase.resources.api.PoolInfo;

/**
 * Provides the monitoring data for JDBC RA module
 *
 * @author Shalini M
 */
@AMXMetadata(type="jdbcra-mon", group="monitoring")
@ManagedObject
@Description("JDBC RA Statistics")
public class JdbcStatsProvider {

    private StringStatisticImpl freqUsedSqlQueries = new StringStatisticImpl(
            "FreqUsedSqlQueries", "List",
            "Most frequently used sql queries");

    private CountStatisticImpl numStatementCacheHit = new CountStatisticImpl(
            "NumStatementCacheHit", StatisticImpl.UNIT_COUNT,
            "The total number of Statement Cache hits.");

    private CountStatisticImpl numStatementCacheMiss = new CountStatisticImpl(
            "NumStatementCacheMiss", StatisticImpl.UNIT_COUNT,
            "The total number of Statement Cache misses.");

    private CountStatisticImpl numPotentialStatementLeak = new CountStatisticImpl(
            "NumPotentialStatementLeak", StatisticImpl.UNIT_COUNT,
            "The total number of potential Statement leaks");

    private PoolInfo poolInfo;
    private SQLTraceCache sqlTraceCache;

    public JdbcStatsProvider(String poolName, String appName, String moduleName, int sqlTraceCacheSize,
            long timeToKeepQueries) {
        poolInfo = new PoolInfo(poolName, appName, moduleName);
        if(sqlTraceCacheSize > 0) {
            this.sqlTraceCache = new SQLTraceCache(poolName, appName, moduleName, sqlTraceCacheSize, timeToKeepQueries);
        }
    }

    /**
     * Whenever statement cache is hit, increment numStatementCacheHit count.
     * @param poolName JdbcConnectionPool that has got a statement cache hit event.
     */
    @ProbeListener(JdbcRAConstants.STATEMENT_CACHE_DOTTED_NAME + JdbcRAConstants.STATEMENT_CACHE_HIT)
    public void statementCacheHitEvent(@ProbeParam("poolName") String poolName,
                                       @ProbeParam("appName") String appName,
                                       @ProbeParam("moduleName") String moduleName
                                       ) {

        PoolInfo poolInfo = new PoolInfo(poolName, appName, moduleName);
        if(this.poolInfo.equals(poolInfo)){
            numStatementCacheHit.increment();
        }
    }

    /**
     * Whenever statement cache miss happens, increment numStatementCacheMiss count.
     * @param poolName JdbcConnectionPool that has got a statement cache miss event.
     */
    @ProbeListener(JdbcRAConstants.STATEMENT_CACHE_DOTTED_NAME + JdbcRAConstants.STATEMENT_CACHE_MISS)
    public void statementCacheMissEvent(@ProbeParam("poolName") String poolName,
                                        @ProbeParam("appName") String appName,
                                        @ProbeParam("moduleName") String moduleName
                                        ) {

        PoolInfo poolInfo = new PoolInfo(poolName, appName, moduleName);
        if(this.poolInfo.equals(poolInfo)){
            numStatementCacheMiss.increment();
        }
    }

    /**
     * Whenever a sql statement that is traced is to be cache for monitoring
     * purpose, the SQLTrace object is created for the specified sql and
     * updated in the SQLTraceCache. This is used to update the
     * frequently used sql queries.
     *
     * @param poolName
     * @param sql
     */
    @ProbeListener(JdbcRAConstants.SQL_TRACING_DOTTED_NAME + JdbcRAConstants.TRACE_SQL)
    public void traceSQLEvent(
                                   @ProbeParam("poolName") String poolName,
                                   @ProbeParam("appName") String appName,
                                   @ProbeParam("moduleName") String moduleName,
                                   @ProbeParam("sql") String sql) {

        PoolInfo poolInfo = new PoolInfo(poolName, appName, moduleName);
        if(this.poolInfo.equals(poolInfo)){
            if(sqlTraceCache != null) {
                if (sql != null) {
                    SQLTrace cacheObj = new SQLTrace(sql, 1,
                            System.currentTimeMillis());
                    sqlTraceCache.checkAndUpdateCache(cacheObj);
                }
            }
        }
    }

    /**
     * Whenever statement leak happens, increment numPotentialStatementLeak count.
     * @param poolName JdbcConnectionPool that has got a statement leak event.
     */
    @ProbeListener(JdbcRAConstants.STATEMENT_LEAK_DOTTED_NAME + JdbcRAConstants.POTENTIAL_STATEMENT_LEAK)
    public void potentialStatementLeakEvent(
                                   @ProbeParam("poolName") String poolName,
                                   @ProbeParam("appName") String appName,
                                   @ProbeParam("moduleName") String moduleName) {

        PoolInfo poolInfo = new PoolInfo(poolName, appName, moduleName);
        if(this.poolInfo.equals(poolInfo)){
            numPotentialStatementLeak.increment();
        }
    }


    @ManagedAttribute(id="numstatementcachehit")
    public CountStatistic getNumStatementCacheHit() {
        return numStatementCacheHit;
    }

    @ManagedAttribute(id="numstatementcachemiss")
    public CountStatistic getNumStatementCacheMiss() {
        return numStatementCacheMiss;
    }

    @ManagedAttribute(id="frequsedsqlqueries")
    public StringStatistic getfreqUsedSqlQueries() {
        if(sqlTraceCache != null) {
            //This is to ensure that only the queries in the last "time-to-keep-
            //queries-in-minutes" is returned back.
            freqUsedSqlQueries.setCurrent(sqlTraceCache.getTopQueries());
        }
        return freqUsedSqlQueries;
    }

    @ManagedAttribute(id="numpotentialstatementleak")
    public CountStatistic getNumPotentialStatementLeak() {
        return numPotentialStatementLeak;
    }

    /**
     * Get the SQLTraceCache associated with this stats provider.
     * @return SQLTraceCache
     */
    public SQLTraceCache getSqlTraceCache() {
        return sqlTraceCache;
    }
}
