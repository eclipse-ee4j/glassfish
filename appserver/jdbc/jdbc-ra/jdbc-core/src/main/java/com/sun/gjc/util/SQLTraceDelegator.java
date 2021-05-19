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

package com.sun.gjc.util;

import com.sun.gjc.monitoring.JdbcRAConstants;
import com.sun.gjc.monitoring.SQLTraceProbeProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.logging.LogDomains;
import org.glassfish.api.jdbc.SQLTraceListener;
import org.glassfish.api.jdbc.SQLTraceRecord;

/**
 * Implementation of SQLTraceListener to listen to events related to a
 * sql record tracing. The registry allows multiple listeners
 * to listen to the sql tracing events. Maintains a list of listeners.
 *
 * @author Shalini M
 */
//@Singleton
public class SQLTraceDelegator implements SQLTraceListener {

    private static Logger _logger;

    static {
        _logger = LogDomains
            .getLogger(MethodExecutor.class, LogDomains.RSR_LOGGER);
    }

    //List of listeners
    protected List<SQLTraceListener> sqlTraceListenersList;
    private String poolName;
    private String appName;
    private String moduleName;
    private SQLTraceProbeProvider probeProvider = null;

    public SQLTraceProbeProvider getProbeProvider() {
        return probeProvider;
    }

    public SQLTraceDelegator(String poolName, String appName, String moduleName) {
        this.poolName = poolName;
        this.appName = appName;
        this.moduleName = moduleName;
        probeProvider = new SQLTraceProbeProvider();
    }

    /**
     * Add a listener to the list of sql trace listeners maintained by
     * this registry.
     * @param listener
     */
    public void registerSQLTraceListener(SQLTraceListener listener) {
        if(sqlTraceListenersList == null) {
                sqlTraceListenersList = new ArrayList<SQLTraceListener>();
        }
        sqlTraceListenersList.add(listener);
    }


   public void sqlTrace(SQLTraceRecord record) {
       if (sqlTraceListenersList != null) {
           for (SQLTraceListener listener : sqlTraceListenersList) {
               try {
                   listener.sqlTrace(record);
               }catch(Exception e){
                   //it is possible that any of the implementations may fail processing a trace record.
                   //do not propagate such failures. Log them as FINEST.
                   if(_logger.isLoggable(Level.FINEST)){
                       _logger.log(Level.FINEST, "exception from one of the SQL trace listeners ["+listener.getClass().getName()+"]", e);
                   }
               }
           }
       }

        if (record != null) {
            record.setPoolName(poolName);
            String methodName = record.getMethodName();
            //Check if the method name is one in which sql query is used
            if (isMethodValidForCaching(methodName)) {
                Object[] params = record.getParams();
                if (params != null && params.length > 0) {
                    String sqlQuery = null;
                    for (Object param : params) {
                        if(param instanceof String) {
                            sqlQuery = param.toString();
                        }
                        break;
                    }
                    if (sqlQuery != null) {
                        probeProvider.traceSQLEvent(poolName, appName, moduleName, sqlQuery);
                    }
                }
            }
        }
    }

   /**
    * Check if the method name from the sql trace record can be used to
    * retrieve a sql string for caching purpose. Most of the method names do not
    * contain a sql string and hence are unusable for caching the sql strings.
    * These method names are filtered in this method.
    *
    * @param methodName
    * @return true if method name can be used to get a sql string for caching.
    */
    private boolean isMethodValidForCaching(String methodName) {
        return JdbcRAConstants.validSqlTracingMethodNames.contains(methodName);
    }
}
