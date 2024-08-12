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

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/* jvm.garbage-collectors */
// v2 mbean: com.sun.appserv:name=Copy,type=garbage-collector,category=monitor,server=server
// v3 mbean:
@AMXMetadata(type = "garbage-collector-mon", group = "monitoring")
@ManagedObject
@Description("JVM Garbage Collectors Statistics")
public class JVMGCStatsProvider {

    private List<GarbageCollectorMXBean> gcBeanList = ManagementFactory.getGarbageCollectorMXBeans();
    private String gcName = null;

    private CountStatisticImpl collectionCount = new CountStatisticImpl("CollectionCount", CountStatisticImpl.UNIT_COUNT,
            "Total number of collections that have occurred");

    private CountStatisticImpl collectionTimeCount = new CountStatisticImpl("CollectionTime", CountStatisticImpl.UNIT_MILLISECOND,
            "Approximate accumulated collection elapsed time in milliseconds");

    public JVMGCStatsProvider(String gcName) {
        this.gcName = gcName;
    }

    @ManagedAttribute(id = "collectioncount-count")
    @Description("total number of collections that have occurred")
    public CountStatistic getCollectionCount() {
        long counts = -1;
        for (GarbageCollectorMXBean gcBean : gcBeanList) {
            if (gcBean.getName().equals(gcName)) {
                counts = gcBean.getCollectionCount();
            }
        }
        collectionCount.setCount(counts);
        return collectionCount;
    }

    @ManagedAttribute(id = "collectiontime-count")
    @Description("approximate accumulated collection elapsed time in milliseconds")
    public CountStatistic getCollectionTime() {
        long times = -1;
        int i = 0;
        for (GarbageCollectorMXBean gcBean : gcBeanList) {
            if (gcBean.getName().equals(gcName)) {
                times = gcBean.getCollectionTime();
            }
        }
        collectionTimeCount.setCount(times);
        return collectionTimeCount;
    }

}
