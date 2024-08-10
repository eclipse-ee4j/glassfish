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

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;

import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/* jvm.class-loading-system */
// v2: com.sun.appserv:name=class-loading-system,type=class-loading-system,category=monitor,server=server
// v3:
@AMXMetadata(type = "class-loading-system-mon", group = "monitoring")
@ManagedObject
@Description("JVM Class Loading Statistics")
public class JVMClassLoadingStatsProvider {

    private ClassLoadingMXBean clBean = ManagementFactory.getClassLoadingMXBean();

    private CountStatisticImpl loadedClassCount = new CountStatisticImpl("LoadedClassCount", CountStatisticImpl.UNIT_COUNT,
            "Number of classes currently loaded in the Java virtual machine");
    private CountStatisticImpl totalLoadedClassCount = new CountStatisticImpl("TotalLoadedClassCount", CountStatisticImpl.UNIT_COUNT,
            "Total number of classes that have been loaded since the Java virtual machine has started execution");
    private CountStatisticImpl unloadedClassCount = new CountStatisticImpl("UnLoadedClassCount", CountStatisticImpl.UNIT_COUNT,
            "Total number of classes unloaded since the Java virtual machine has started execution");

    @ManagedAttribute(id = "loadedclass-count")
    @Description("number of classes currently loaded in the JVM")
    public CountStatistic getLoadedClassCount() {
        loadedClassCount.setCount(clBean.getLoadedClassCount());
        return loadedClassCount;
    }

    @ManagedAttribute(id = "totalloadedclass-count")
    @Description("total number of classes loaded since the JVM started")
    public CountStatistic getTotalLoadedClassCount() {
        totalLoadedClassCount.setCount(clBean.getTotalLoadedClassCount());
        return totalLoadedClassCount;
    }

    @ManagedAttribute(id = "unloadedclass-count")
    @Description("total number of classes unloaded since the JVM started")
    public CountStatistic getUnloadedClassCount() {
        unloadedClassCount.setCount(clBean.getUnloadedClassCount());
        return unloadedClassCount;
    }
}
