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

package org.glassfish.admin.monitor.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/* jvm.memory */
// v2 mbean: com.sun.appserv:name=memory,type=memory,category=monitor,server=server
// v3 mbean:
@AMXMetadata(type = "memory-mon", group = "monitoring")
@ManagedObject
@Description("JVM Memory Statistics")
public class JVMMemoryStatsProvider {
    private MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();

    private CountStatisticImpl committedHeap = new CountStatisticImpl("CommittedHeapSize", "bytes",
            "Amount of memory in bytes that is committed for the Java virtual machine to use");
    private CountStatisticImpl initHeap = new CountStatisticImpl("InitialHeapSize", "bytes",
            "Amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management");
    private CountStatisticImpl maxHeap = new CountStatisticImpl("MaxHeapSize", "bytes",
            "Maximum amount of memory in bytes that can be used for memory management");
    private CountStatisticImpl usedHeap = new CountStatisticImpl("UsedHeapSize", "bytes", "Amount of used memory in bytes");
    private CountStatisticImpl committedNonHeap = new CountStatisticImpl("CommittedNonHeapSize", "bytes",
            "Amount of memory in bytes that is committed for the Java virtual machine to use");
    private CountStatisticImpl initNonHeap = new CountStatisticImpl("InitialNonHeapSize", "bytes",
            "Amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management");
    private CountStatisticImpl maxNonHeap = new CountStatisticImpl("MaxNonHeapSize", "bytes",
            "Maximum amount of memory in bytes that can be used for memory management");
    private CountStatisticImpl usedNonHeap = new CountStatisticImpl("UsedNonHeapSize", "bytes", "Amount of used memory in bytes");
    private CountStatisticImpl objectPendingFinalizationCount = new CountStatisticImpl("ObjectsPendingFinalization",
            CountStatisticImpl.UNIT_COUNT, "Approximate number of objects for which finalization is pending");

    @ManagedAttribute(id = "committedheapsize-count")
    @Description("amount of memory in bytes that is committed for the Java virtual machine to use")
    public CountStatistic getCommittedHeap() {
        committedHeap.setCount(memBean.getHeapMemoryUsage().getCommitted());
        return committedHeap;
    }

    @ManagedAttribute(id = "initheapsize-count")
    @Description("amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management")
    public CountStatistic getInitHeap() {
        initHeap.setCount(memBean.getHeapMemoryUsage().getInit());
        return initHeap;
    }

    @ManagedAttribute(id = "maxheapsize-count")
    @Description("maximum amount of memory in bytes that can be used for memory management")
    public CountStatistic getMaxHeap() {
        maxHeap.setCount(memBean.getHeapMemoryUsage().getMax());
        return maxHeap;
    }

    @ManagedAttribute(id = "usedheapsize-count")
    @Description("amount of used memory in bytes")
    public CountStatistic getUsedHeap() {
        usedHeap.setCount(memBean.getHeapMemoryUsage().getUsed());
        return usedHeap;
    }

    @ManagedAttribute(id = "committednonheapsize-count")
    @Description("amount of memory in bytes that is committed for the Java virtual machine to use")
    public CountStatistic getCommittedNonHeap() {
        committedNonHeap.setCount(memBean.getNonHeapMemoryUsage().getCommitted());
        return committedNonHeap;
    }

    @ManagedAttribute(id = "initnonheapsize-count")
    @Description("amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management")
    public CountStatistic getInitNonHeap() {
        initNonHeap.setCount(memBean.getNonHeapMemoryUsage().getInit());
        return initNonHeap;
    }

    @ManagedAttribute(id = "maxnonheapsize-count")
    @Description("maximum amount of memory in bytes that can be used for memory management")
    public CountStatistic getMaxNonHeap() {
        maxNonHeap.setCount(memBean.getNonHeapMemoryUsage().getMax());
        return maxNonHeap;
    }

    @ManagedAttribute(id = "usednonheapsize-count")
    @Description("amount of used memory in bytes")
    public CountStatistic getUsedNonHeap() {
        usedNonHeap.setCount(memBean.getNonHeapMemoryUsage().getUsed());
        return usedNonHeap;
    }

    @ManagedAttribute(id = "objectpendingfinalizationcount-count")
    @Description("approximate number of objects for which finalization is pending")
    public CountStatistic getObjectPendingFinalizationCount() {
        objectPendingFinalizationCount.setCount(memBean.getObjectPendingFinalizationCount());
        return objectPendingFinalizationCount;
    }
}
