/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.monitoring.ContainerMonitoring;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.*;

/**
 *
 * @author PRASHANTH ABBAGANI
 */
@Service
@RunLevel(value = PostStartupRunLevel.VAL, mode = RunLevel.RUNLEVEL_MODE_NON_VALIDATING)
public class JVMStatsProviderBootstrap implements PostConstruct {

    private ServerRuntimeStatsProvider sRuntimeStatsProvider = new ServerRuntimeStatsProvider();
    private JVMClassLoadingStatsProvider clStatsProvider = new JVMClassLoadingStatsProvider();
    private JVMCompilationStatsProvider compileStatsProvider = new JVMCompilationStatsProvider();
    private JVMMemoryStatsProvider memoryStatsProvider = new JVMMemoryStatsProvider();
    private JVMOSStatsProvider osStatsProvider = new JVMOSStatsProvider();
    private JVMRuntimeStatsProvider runtimeStatsProvider = new JVMRuntimeStatsProvider();
    private JVMThreadSystemStatsProvider threadSysStatsProvider = new JVMThreadSystemStatsProvider();
    private ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    public static final String JVM = "jvm";

    public void postConstruct() {

        /* register with monitoring */
        StatsProviderManager.register("jvm", PluginPoint.SERVER, "", sRuntimeStatsProvider, ContainerMonitoring.LEVEL_LOW);
        StatsProviderManager.register("jvm", PluginPoint.SERVER, "jvm/class-loading-system", clStatsProvider,
                ContainerMonitoring.LEVEL_LOW);
        StatsProviderManager.register("jvm", PluginPoint.SERVER, "jvm/compilation-system", compileStatsProvider,
                ContainerMonitoring.LEVEL_LOW);
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            JVMGCStatsProvider jvmStatsProvider = new JVMGCStatsProvider(gc.getName());
            StatsProviderManager.register("jvm", PluginPoint.SERVER, "jvm/garbage-collectors/" + gc.getName(), jvmStatsProvider,
                    ContainerMonitoring.LEVEL_LOW);
        }
        StatsProviderManager.register("jvm", PluginPoint.SERVER, "jvm/memory", memoryStatsProvider, ContainerMonitoring.LEVEL_LOW);
        StatsProviderManager.register("jvm", PluginPoint.SERVER, "jvm/operating-system", osStatsProvider, ContainerMonitoring.LEVEL_LOW);
        StatsProviderManager.register("jvm", PluginPoint.SERVER, "jvm/runtime", runtimeStatsProvider, ContainerMonitoring.LEVEL_LOW);
        StatsProviderManager.register("jvm", PluginPoint.SERVER, "jvm/thread-system", threadSysStatsProvider,
                ContainerMonitoring.LEVEL_LOW);
        for (ThreadInfo t : threadBean.getThreadInfo(threadBean.getAllThreadIds(), 5)) {
            if (t == null)
                continue; // See issue #12636
            JVMThreadInfoStatsProvider threadInfoStatsProvider = new JVMThreadInfoStatsProvider(t);
            StatsProviderManager.register("jvm", PluginPoint.SERVER, "jvm/thread-system/thread-" + t.getThreadId(), threadInfoStatsProvider,
                    ContainerMonitoring.LEVEL_HIGH);
        }
    }
}
