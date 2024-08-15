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
import java.lang.management.OperatingSystemMXBean;

import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.StringStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.external.statistics.impl.StringStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/* jvm.operating-system */
// v2 mbean: com.sun.appserv:name=operating-system,type=operating-system,category=monitor,server=server
// v3 mbean:
@AMXMetadata(type = "operating-system-mon", group = "monitoring")
@ManagedObject
@Description("JVM Operating System Statistics")
public class JVMOSStatsProvider {

    private OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

    private StringStatisticImpl arch = new StringStatisticImpl("Architecture", "String", "Operating system architecture");
    private CountStatisticImpl availableProcessors = new CountStatisticImpl("AvailableProcessors", CountStatisticImpl.UNIT_COUNT,
            "Number of processors available to the Java virtual machine");
    private StringStatisticImpl osName = new StringStatisticImpl("Name", "String", "Operating system name");
    private StringStatisticImpl osVersion = new StringStatisticImpl("Version", "String", "operating system version");
    //private CountStatisticImpl sysLoadAverage = new CountStatisticImpl(
    //        "SystemLoadAverage", CountStatisticImpl.UNIT_COUNT,
    //            "System load average for the last minute" );

    @ManagedAttribute(id = "arch-current")
    @Description("operating system architecture")
    public StringStatistic getArch() {
        arch.setCurrent(osBean.getArch());
        return arch;
    }

    @ManagedAttribute(id = "availableprocessors-count")
    @Description("number of processors available to the Java virtual machine")
    public CountStatistic getAvailableProcessors() {
        availableProcessors.setCount(osBean.getAvailableProcessors());
        return availableProcessors;
    }

    @ManagedAttribute(id = "name-current")
    @Description("operating system name")
    public StringStatistic getOSName() {
        osName.setCurrent(osBean.getName());
        return osName;
    }

    @ManagedAttribute(id = "version-current")
    @Description("operating system version")
    public StringStatistic getOSVersion() {
        osVersion.setCurrent(osBean.getVersion());
        return osVersion;
    }

    /*@ManagedAttribute(id="systemloadaverage-current")
    @Description( "system load average for the last minute" )
    public CountStatistic getSystemLoadAverage() {
        sysLoadAverage.setCurrent(osBean.getSystemLoadAverage());
        return sysLoadAverage;
    }*/
}
