/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejb.monitoring.stats;

import java.util.List;
import java.util.ArrayList;
import org.glassfish.external.statistics.*;
import org.glassfish.external.statistics.impl.*;
import org.glassfish.gmbal.*;

/**
 * Event listener for the Ejb monitoring events. Used by the probe framework
 * to collect and display the data.
 *
 * @author Marina Vatkina
 */
// TODO: find the right names
// v2: com.sun.appserv:application=MEjbApp,name=getEJBHome,type=bean-method,category=monitor,ejb-module=mejb_jar,server=server,stateless-session-bean=MEJBBean
// v3: amx:pp=/mon/server-mon[server],type=bean-method-mon,name=??????????
@AMXMetadata(type="bean-method-mon", group="monitoring", isSingleton=false)
@ManagedObject
@Description("Ejb Method Statistics")
public class EjbMethodStatsProvider {

    private CountStatisticImpl executionStat = new CountStatisticImpl(
            "ExecutionTime", "Milliseconds",
            "Provides the time in milliseconds spent during the last "
                     + "successful/unsuccessful attempt to execute the operation.");

    private CountStatisticImpl invocationStat = new CountStatisticImpl(
            "TotalNumInvocations", "count",
            "Provides the total number of invocations of the method.");

    private CountStatisticImpl errorStat = new CountStatisticImpl(
            "TotalNumErrors", "count",
            "Provides the total number of errors that occured during invocation "
                    + "or execution of an operation.");

    private CountStatisticImpl successStat = new CountStatisticImpl(
                "TotalNumSuccess", "count",
                "Provides the total number of successful invocations of the method.");

    private TimeStatisticImpl methodStat = null;

    private static ThreadLocal  execThreadLocal = new ThreadLocal();
    private String mname = null;
    private boolean registered = false;

    EjbMethodStatsProvider (String mname) {
        this.mname = mname;

        long now = System.currentTimeMillis();
        methodStat = new TimeStatisticImpl(
                0, 0, 0, 0, "MethodStatistic", "",
                "Provides the number of times an operation was called, the total time "
                       + "that was spent during the invocation and so on",
                now, now);
    }

    @ManagedAttribute(id="methodstatistic")
    @Description("Number of times the operation is called; total time spent during invocation, and so on.")
    public TimeStatistic getMethodStatistic() {
        return methodStat;
    }

    @ManagedAttribute(id="totalnumerrors")
    @Description("Number of times the method execution resulted in an exception")
    public CountStatistic getTotalNumErrors() {
        return errorStat;
    }

    @ManagedAttribute(id="totalnumsuccess")
    @Description("Number of times the method successfully executed")
    public CountStatistic getTotalNumSuccess() {
        return successStat;
    }

    @ManagedAttribute(id="executiontime")
    @Description("Time (ms) spent executing method for the last successful/unsuccessful "
        + "attempt to execute the operation")
    public CountStatistic getTotalExecutionTime() {
        return executionStat;
    }

    public String getStringifiedMethodName() {
        return mname;
    }

    void registered() {
        registered = true;
    }

    void unregistered() {
        registered = false;
    }

    boolean isRegistered() {
        return registered;
    }

    void methodStart() {
        List list = (ArrayList) execThreadLocal.get();
        if (list == null) {
            list = new ArrayList(5);
            execThreadLocal.set(list);
        }
        list.add(System.currentTimeMillis());
        invocationStat.increment();
    }

    void methodEnd(boolean success) {
        List list = (ArrayList) execThreadLocal.get();
        if ( (list != null) && (list.size() > 0) ) {
            int index = list.size();
            Long startTime = (Long) list.remove(index-1);
            if (success) {
                successStat.increment();
            } else {
                errorStat.increment();
            }
            if (startTime != null) {
                long diff = System.currentTimeMillis() - startTime.longValue();
                executionStat.setCount(diff);
                methodStat.incrementCount(diff);
            }
        }
    }
}
