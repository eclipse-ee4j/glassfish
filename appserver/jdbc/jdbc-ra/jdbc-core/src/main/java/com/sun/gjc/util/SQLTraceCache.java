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

package com.sun.gjc.util;

import com.sun.logging.LogDomains;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Maintains the Sql Tracing Cache used to store SQL statements used by the
 * applications. This is used by the JDBCRA monitoring to display the most
 * frequently used queries by applications.
 *
 * @author Shalini M
 */
public class SQLTraceCache {

    //List of sql trace objects
    private final List<SQLTrace> list;
    //Maximum size of the cache.
    private int numTopQueriesToReport = 10;
    private long timeToKeepQueries = 60 * 1000;
    private SQLTraceTimerTask sqlTraceTimerTask;
    private String poolName;
    private String appName;
    private String moduleName;
    private final static Logger _logger = LogDomains.getLogger(SQLTraceCache.class,
            LogDomains.RSR_LOGGER);
    private static final String LINE_BREAK = "%%%EOL%%%";

    public SQLTraceCache(String poolName, String appName, String moduleName, int maxSize, long timeToKeepQueries) {
        this.poolName = poolName;
        this.appName = appName;
        this.moduleName = moduleName;
        this.numTopQueriesToReport = maxSize;
        list = new ArrayList<SQLTrace>();
        this.timeToKeepQueries = timeToKeepQueries * 60 * 1000;
    }

    public List<SQLTrace> getSqlTraceList() {
        return list;
    }

    public String getPoolName() {
        return poolName;
    }

    /**
     * Schedule timer to perform purgeEntries on the cache after the
     * specified timeToKeepQueries delay and period.
     */
    public void scheduleTimerTask(Timer timer) {

        if(sqlTraceTimerTask != null) {
            sqlTraceTimerTask.cancel();
            sqlTraceTimerTask = null;
        }

        sqlTraceTimerTask = initializeTimerTask();

        if(timer != null) {

            timer.scheduleAtFixedRate(sqlTraceTimerTask, timeToKeepQueries, timeToKeepQueries);
        }
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("Scheduled Sql Trace Caching timer task");
        }
    }

    /**
     * Cancel the timer task used to perform a purgeEntries on the cache.
     */
    public synchronized void cancelTimerTask() {

        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("Cancelling Sql Trace Caching timer task");
        }
        if (sqlTraceTimerTask != null) {
            sqlTraceTimerTask.cancel();
        }
        sqlTraceTimerTask = null;
    }

    /**
     * Instantiate the timer task used to perform a purgeEntries on the cache
     *
     * @return SQLTraceTimerTask
     */
    private SQLTraceTimerTask initializeTimerTask() {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("Initializing Sql Trace Caching timer task");
        }
        return new SQLTraceTimerTask(this);
    }

    /**
     * Request for adding a sql query in the form of SQLTrace to this cache.
     * If the query is already found
     * in the list, the number of times it is executed is incremented by one
     * along with the timestamp.
     * If the query is a new one, it is added to the list.
     *
     * @param cacheObj
     * @return
     */
    public void checkAndUpdateCache(SQLTrace cacheObj) {
        synchronized (list) {
            if (cacheObj != null) {
                int index = list.indexOf(cacheObj);
                if (index != -1) {
                    //If already found in the cache
                    //equals is invoked here and hence comparison based on query name is done
                    //Get the object at the index to update the numExecutions
                    SQLTrace cache = (SQLTrace) list.get(index);
                    cache.setNumExecutions(cache.getNumExecutions() + 1);
                    cache.setLastUsageTime(System.currentTimeMillis());
                } else {
                    //First occurrence of the query. query to be added.
                    cacheObj.setNumExecutions(1);
                    cacheObj.setLastUsageTime(System.currentTimeMillis());
                    list.add(cacheObj);
                }
            }
        }
    }

    /**
     * Entries are removed from the list after sorting
     * them in the least frequently used order. Only numTopQueriesToReport number of
     * entries are maintained in the list after the purgeEntries.
     */
    public void purgeEntries() {
        synchronized(list) {
            Collections.sort(list, Collections.reverseOrder());
            Iterator i = list.iterator();
            while (i.hasNext()) {
                SQLTrace cacheObj = (SQLTrace) i.next();
                if (list.size() > numTopQueriesToReport) {
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("removing sql=" + cacheObj.getQueryName());
                    }
                    i.remove();
                } else {
                    break;
                }
            }
            //sort by most frequently used queries first
            Collections.sort(list);
        }
    }

    /**
     * Returns the String representation of the list of traced sql queries
     * ordered by the number most frequently used, followed by the usage
     * timestamp. Only the top 'n' queries represented by the numTopQueriesToReport are
     * chosen for display.
     *
     * @return string representation of the list of sql queries sorted
     */
    public String getTopQueries() {
        purgeEntries();
        StringBuffer sb = new StringBuffer();
        for(SQLTrace cache : list) {
            sb.append(LINE_BREAK);
            sb.append(cache.getQueryName());
        }
        return sb.toString();
    }
}
