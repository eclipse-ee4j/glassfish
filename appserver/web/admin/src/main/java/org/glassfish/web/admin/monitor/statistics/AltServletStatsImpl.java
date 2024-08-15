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

package org.glassfish.web.admin.monitor.statistics;

import jakarta.inject.Inject;

import java.util.List;
import java.util.ResourceBundle;

import org.glassfish.admin.monitor.cli.MonitorContract;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.RangeStatistic;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.web.admin.LogFacade;
import org.jvnet.hk2.annotations.Service;

@Service
@PerLookup
public class AltServletStatsImpl implements MonitorContract {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    @Inject
    private MonitoringRuntimeDataRegistry mrdr;

    private final static String name = "servlet";

    private final static String displayFormat = "%1$-10s %2$-10s %3$-10s";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ActionReport process(final ActionReport report, final String filter) {

        if (mrdr == null) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(rb.getString(LogFacade.MRDR_NULL));
            return report;
        }

        TreeNode serverNode = mrdr.get("server");
        if (serverNode == null) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(rb.getString(LogFacade.MRDR_NULL));
            return report;
        }

        String[] patternArr = new String [] {"server.web.servlet.*"};

        long activeServletsLoadedCount = 0;
        long maxServletsLoadedCount = 0;
        long totalServletsLoadedCount = 0;

        for (String pattern : patternArr) {
            List<TreeNode> tnL = serverNode.getNodes(pattern);
            for (TreeNode tn : tnL) {
                if (tn.hasChildNodes()) {
                    continue;
                }
                if ("activeservletsloadedcount".equals(tn.getName())) {
                    activeServletsLoadedCount = getRangeStatisticValue(tn.getValue());
                } else if ("maxservletsloadedcount".equals(tn.getName())) {
                    maxServletsLoadedCount = getCountStatisticValue(tn.getValue());
                } else if ("totalservletsloadedcount".equals(tn.getName())) {
                    totalServletsLoadedCount = getCountStatisticValue(tn.getValue());
                }
            }
        }

        report.setMessage(
            String.format(displayFormat, activeServletsLoadedCount, maxServletsLoadedCount, totalServletsLoadedCount));

        report.setActionExitCode(ExitCode.SUCCESS);
        return report;
    }

    private long getCountStatisticValue(Object obj) {
        long l = 0L;
        if (obj == null) {
            return l;
        }
        if (obj instanceof CountStatistic) {
            return ((CountStatistic)obj).getCount();
        }
        return l;
    }

    private long getRangeStatisticValue(Object obj) {
        long l = 0L;
        if (obj == null) {
            return l;
        }
        if (obj instanceof RangeStatistic) {
            return ((RangeStatistic)obj).getCurrent();
        }
        return l;
    }
}
