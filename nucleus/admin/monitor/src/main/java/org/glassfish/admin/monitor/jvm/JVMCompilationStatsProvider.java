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

import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;

import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.StringStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.external.statistics.impl.StringStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/* jvm.compilation-system */
// v2 mbean: com.sun.appserv:name=compilation-system,type=compilation-system,category=monitor,server=server
// v3 mbean:
@AMXMetadata(type = "compilation-system-mon", group = "monitoring")
@ManagedObject
@Description("JVM Compilation Statistics")
public class JVMCompilationStatsProvider {

    private CompilationMXBean compBean = ManagementFactory.getCompilationMXBean();

    private StringStatisticImpl compilerName = new StringStatisticImpl("Name", "String", "Name of the Just-in-time (JIT) compiler");
    private CountStatisticImpl totalCompilationTime = new CountStatisticImpl("TotalCompilationTime", CountStatisticImpl.UNIT_MILLISECOND,
            "Approximate accumlated elapsed time (in milliseconds) spent in compilation");

    @ManagedAttribute(id = "name-current")
    @Description("name of the Just-in-time (JIT) compiler")
    public StringStatistic getCompilerName() {
        compilerName.setCurrent(compBean.getName());
        return compilerName;
    }

    @ManagedAttribute(id = "totalcompilationtime-current")
    @Description("approximate accumlated elapsed time (in milliseconds) spent in compilation")
    public CountStatistic getTotalCompilationTime() {
        totalCompilationTime.setCount(compBean.getTotalCompilationTime());
        return totalCompilationTime;
    }
}
