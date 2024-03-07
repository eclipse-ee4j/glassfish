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

package org.glassfish.admin.monitor.jvm.statistics;

import java.util.*;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

import org.glassfish.hk2.api.PerLookup;
import org.glassfish.api.ActionReport;
import org.glassfish.api.monitoring.ContainerMonitoring;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.admin.monitor.cli.MonitorContract;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import org.glassfish.flashlight.datatree.TreeNode;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.MonitoringService;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.external.statistics.Statistic;
import org.glassfish.external.statistics.CountStatistic;

import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 *
 * For v3 Prelude, following stats will be available server.jvm.committedHeapSize java.lang.management.MemoryUsage init,
 * used, committed, max
 *
 */
//public class JVMStatsImpl implements JVMStats, MonitorContract {
@Service
@PerLookup
public class JVMStatsImpl implements MonitorContract {

    @Inject
    private MonitoringRuntimeDataRegistry mrdr;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    @Optional
    MonitoringService monitoringService = null;

    private final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(JVMStatsImpl.class);

    private final String name = "jvm";

    public String getName() {
        return name;
    }

    public ActionReport process(final ActionReport report, final String filter) {

        if (monitoringService != null) {
            String level = monitoringService.getMonitoringLevel("jvm");
            if ((level != null) && (level.equals(ContainerMonitoring.LEVEL_OFF))) {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(localStrings.getLocalString("level.off", "Monitoring level for jvm is off"));
                return report;
            }
        }

        if (mrdr == null) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(localStrings.getLocalString("mrdr.null", "MonitoringRuntimeDataRegistry is null"));
            return report;
        }

        TreeNode serverNode = mrdr.get("server");
        if (serverNode == null) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(localStrings.getLocalString("mrdr.null", "MonitoringRuntimeDataRegistry server node is null"));
            return report;
        }

        if ((filter != null) && (filter.length() > 0)) {
            if ("heapmemory".equals(filter)) {
                return (heapMemory(report, serverNode));
            } else if ("nonheapmemory".equals(filter)) {
                return (nonHeapMemory(report, serverNode));
            }
        } else {
            return (v2JVM(report, serverNode));
        }

        return null;
    }

    private ActionReport heapMemory(final ActionReport report, TreeNode serverNode) {
        long init = getFirstTreeNodeAsLong(serverNode, "server.jvm.memory.initheapsize-count");
        long used = getFirstTreeNodeAsLong(serverNode, "server.jvm.memory.usedheapsize-count");
        long committed = getFirstTreeNodeAsLong(serverNode, "server.jvm.memory.committedheapsize-count");
        long max = getFirstTreeNodeAsLong(serverNode, "server.jvm.memory.maxheapsize-count");
        String displayFormat = "%1$-10s %2$-10s %3$-10s %4$-10s";
        report.setMessage(String.format(displayFormat, init, used, committed, max));
        report.setActionExitCode(ExitCode.SUCCESS);
        return report;
    }

    private ActionReport nonHeapMemory(final ActionReport report, TreeNode serverNode) {
        long init = getFirstTreeNodeAsLong(serverNode, "server.jvm.memory.initnonheapsize-count");
        long used = getFirstTreeNodeAsLong(serverNode, "server.jvm.memory.usednonheapsize-count");
        long committed = getFirstTreeNodeAsLong(serverNode, "server.jvm.memory.committednonheapsize-count");
        long max = getFirstTreeNodeAsLong(serverNode, "server.jvm.memory.maxnonheapsize-count");
        String displayFormat = "%1$-10s %2$-10s %3$-10s %4$-10s";
        report.setMessage(String.format(displayFormat, init, used, committed, max));
        report.setActionExitCode(ExitCode.SUCCESS);
        return report;
    }

    // @author bnevins
    private long getFirstTreeNodeAsLong(TreeNode parent, String name) {

        List<TreeNode> nodes = parent.getNodes(name);

        if (!nodes.isEmpty()) {
            TreeNode node = nodes.get(0);
            Object val = node.getValue();
            if (val != null) {
                try {
                    CountStatistic cs = (CountStatistic) val;
                    return cs.getCount();
                } catch (Exception e) {
                    //TODO: handle exception
                }
            }
        }

        return 0L;
    }

    // @author bnevins
    private ActionReport v2JVM(final ActionReport report, TreeNode serverNode) {
        long uptime = getFirstTreeNodeAsLong(serverNode, "server.jvm.runtime.uptime-count");
        long min = getFirstTreeNodeAsLong(serverNode, "server.jvm.memory.initnonheapsize-count");
        min += getFirstTreeNodeAsLong(serverNode, "server.jvm.memory.initheapsize-count");
        long max = getFirstTreeNodeAsLong(serverNode, "server.jvm.memory.maxheapsize-count");
        max += getFirstTreeNodeAsLong(serverNode, "server.jvm.memory.maxnonheapsize-count");
        long count = getFirstTreeNodeAsLong(serverNode, "server.jvm.memory.committedheapsize-count");
        count += getFirstTreeNodeAsLong(serverNode, "server.jvm.memory.committednonheapsize-count");

        String displayFormat = "%1$-25s %2$-10s %3$-10s %4$-10s %5$-10s %6$-10s";
        report.setMessage(String.format(displayFormat, uptime, min, max, 0, 0, count));

        report.setActionExitCode(ExitCode.SUCCESS);
        return report;
    }

    public Statistic[] getStatistics() {
        return null;
    }

    public String[] getStatisticNames() {
        return null;
    }

    public Statistic getStatistic(String statisticName) {
        return null;
    }
}
