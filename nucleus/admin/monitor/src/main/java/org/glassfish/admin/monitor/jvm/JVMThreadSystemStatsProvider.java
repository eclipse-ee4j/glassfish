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

package org.glassfish.admin.monitor.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.StringStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.external.statistics.impl.StatisticImpl;
import org.glassfish.external.statistics.impl.StringStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/* server.jvm.thread-system */
// v2 mbean: com.sun.appserv:name=thread-system,type=thread-system,category=monitor,server=server
// v3 mbean:
@AMXMetadata(type = "thread-system-mon", group = "monitoring")
@ManagedObject
@Description("JVM Thread System Statistics")
public class JVMThreadSystemStatsProvider {

    private ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    private StringStatisticImpl allThreadIds = new StringStatisticImpl("LiveThreads", "String", "Returns all live thread IDs");
    private CountStatisticImpl currentThreadCpuTime = new CountStatisticImpl("CurrentThreadCpuTime", StatisticImpl.UNIT_NANOSECOND,
            "Returns the total CPU time for the current thread in nanoseconds");
    private CountStatisticImpl currentThreadUserTime = new CountStatisticImpl("CurrentThreadUserTime", StatisticImpl.UNIT_NANOSECOND,
            "Returns the CPU time that the current thread has executed in user mode in nanoseconds");
    private CountStatisticImpl daemonThreadCount = new CountStatisticImpl("DaemonThreadCount", StatisticImpl.UNIT_COUNT,
            "Returns the current number of live daemon threads");
    private StringStatisticImpl deadlockedThreads = new StringStatisticImpl("DeadlockedThreads", "String",
            "Finds cycles of threads that are in deadlock waiting to acquire object monitors or ownable synchronizers");
    private StringStatisticImpl monitorDeadlockedThreads = new StringStatisticImpl("MonitorDeadlockedThreads", "String",
            "Finds cycles of threads that are in deadlock waiting to acquire object monitors");
    private CountStatisticImpl peakThreadCount = new CountStatisticImpl("PeakThreadCount", StatisticImpl.UNIT_COUNT,
            "Returns the peak live thread count since the Java virtual machine started or peak was reset");
    private CountStatisticImpl threadCount = new CountStatisticImpl("ThreadCount", StatisticImpl.UNIT_COUNT,
            "Returns the current number of live threads including both daemon and non-daemon threads");
    private CountStatisticImpl totalStartedThreadCount = new CountStatisticImpl("TotalStartedThreadCount", StatisticImpl.UNIT_COUNT,
            "Returns the total number of threads created and also started since the Java virtual machine started");

    @ManagedAttribute(id = "allthreadids")
    @Description("Returns all live thread IDs")
    public StringStatistic getAllThreadIds() {
        long[] ids = this.threadBean.getAllThreadIds();
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (long id : ids) {
            if (first)
                first = false;
            else
                sb.append(',');

            sb.append(id);
        }
        this.allThreadIds.setCurrent(sb.toString());
        return allThreadIds;
    }

    @ManagedAttribute(id = "currentthreadcputime")
    @Description("Returns the total CPU time for the current thread in nanoseconds")
    public CountStatistic getCurrentThreadCpuTime() {
        this.currentThreadCpuTime.setCount(threadBean.getCurrentThreadCpuTime());
        return this.currentThreadCpuTime;
    }

    @ManagedAttribute(id = "currentthreadusertime")
    @Description("Returns the CPU time that the current thread has executed in user mode in nanoseconds")
    public CountStatistic getCurrentThreadUserTime() {
        this.currentThreadUserTime.setCount(threadBean.getCurrentThreadUserTime());
        return this.currentThreadUserTime;
    }

    @ManagedAttribute(id = "daemonthreadcount")
    @Description("Returns the current number of live daemon threads")
    public CountStatistic getDaemonThreadCount() {
        this.daemonThreadCount.setCount(threadBean.getDaemonThreadCount());
        return this.daemonThreadCount;
    }

    @ManagedAttribute(id = "deadlockedthreads")
    @Description("Finds cycles of threads that are in deadlock waiting to acquire object monitors or ownable synchronizers")
    public StringStatistic getDeadlockedThreads() {
        long[] threads = threadBean.findDeadlockedThreads();
        if (threads == null) {
            this.deadlockedThreads.setCurrent("None of the threads are deadlocked.");
        } else {
            StringBuffer sb = new StringBuffer();
            for (long thread : threads) {
                sb.append(thread);
                sb.append(',');
            }
            this.deadlockedThreads.setCurrent(sb.toString());
        }
        return deadlockedThreads;
    }

    @ManagedAttribute(id = "monitordeadlockedthreads")
    @Description("Finds cycles of threads that are in deadlock waiting to acquire object monitors")
    public StringStatistic getMonitorDeadlockedThreads() {
        long[] threads = threadBean.findMonitorDeadlockedThreads();
        if (threads == null) {
            this.monitorDeadlockedThreads.setCurrent("None of the threads are monitor deadlocked.");
        } else {
            StringBuffer sb = new StringBuffer();
            for (long thread : threads) {
                sb.append(thread);
                sb.append(',');
            }
            this.monitorDeadlockedThreads.setCurrent(sb.toString());
        }
        return this.monitorDeadlockedThreads;
    }

    @ManagedAttribute(id = "peakthreadcount")
    @Description("Returns the peak live thread count since the Java virtual machine started or peak was reset")
    public CountStatistic getPeakThreadCount() {
        this.peakThreadCount.setCount(threadBean.getPeakThreadCount());
        return this.peakThreadCount;
    }

    @ManagedAttribute(id = "threadcount")
    @Description("Returns the current number of live threads including both daemon and non-daemon threads")
    public CountStatistic getThreadCount() {
        threadCount.setCount(threadBean.getThreadCount());
        return threadCount;
    }

    @ManagedAttribute(id = "totalstartedthreadcount")
    @Description("Returns the total number of threads created and also started since the Java virtual machine started")
    public CountStatistic getTotalStartedThreadCount() {
        totalStartedThreadCount.setCount(threadBean.getTotalStartedThreadCount());
        return totalStartedThreadCount;
    }
}
