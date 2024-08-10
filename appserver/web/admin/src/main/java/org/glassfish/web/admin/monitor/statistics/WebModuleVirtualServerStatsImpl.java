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
import org.glassfish.api.Param;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.RangeStatistic;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.web.admin.LogFacade;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * For v3 Prelude, following stats will be available
 *
 * asc activeSessionsCount,
 * ast activatedSessionsTotal,
 * rst rejectedSessionsTotal,
 * st  sessionsTotal
 * ajlc activeJspsLoadedCount,
 * mjlc maxJspsLoadedCount,
 * tjlc totalJspsLoadedCount
 * aslc activeServletsLoadedCount,
 * mslc maxServletsLoadedCount,
 * tslc totalServletsLoadedCount
 *
 * ash activeSessionsHigh,
 * est expiredSessionsTotal,
 * pvst passivatedSessionsTotal,
 * pst persistedSessionsTotal,
 */
@Service
@PerLookup
public class WebModuleVirtualServerStatsImpl implements MonitorContract {

    // app name otherwise web
    @Param (optional=true)
    String appName;

    @Inject
    private MonitoringRuntimeDataRegistry mrdr;

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    private final static String name = "webmodule";

    private final static String displayFormat
        = "%1$-5s %2$-5s %3$-5s %4$-5s %5$-5s %6$-5s %7$-5s %8$-8s %9$-10s %10$-5s";

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

        String [] patternArr;

        if (appName != null) {
            // post prelude - need to fix this for virtual server
            patternArr = new String [] {"server.applications." + appName + ".*.*"};
        } else {
            patternArr = new String [] {"server.web.session.*",
                    "server.web.servlet.*", "server.web.jsp.*"};
        }

        long activeSessionsCount = 0;
        long sessionsTotal = 0;
        long rejectedSessionsTotal = 0;
        long activatedSessionsTotal = 0;

        long activeJspsLoadedCount = 0;
        long maxJspsLoadedCount = 0;
        long totalJspsLoadedCount = 0;

        long activeServletsLoadedCount = 0;
        long maxServletsLoadedCount = 0;
        long totalServletsLoadedCount = 0;

        long lval = 0;

        for (String pattern : patternArr) {
            List<TreeNode> tnL = serverNode.getNodes(pattern);
            for (TreeNode tn : tnL) {
                if (tn.hasChildNodes()) {
                    continue;
                }

                if ("activesessionscount".equals(tn.getName())) {
                    activeSessionsCount = getRangeStatisticValue(tn.getValue());
                } else if ("activatedsessionstotal".equals(tn.getName())) {
                    activatedSessionsTotal = getCountStatisticValue(tn.getValue());
                } else if ("rejectedsessionstotal".equals(tn.getName())) {
                    rejectedSessionsTotal = getCountStatisticValue(tn.getValue());
                } else if ("sessionstotal".equals(tn.getName())) {
                    sessionsTotal = getCountStatisticValue(tn.getValue());
                } else if ("activejspsloadedcount".equals(tn.getName())) {
                    activeJspsLoadedCount = getRangeStatisticValue(tn.getValue());
                } else if ("maxjspsloadedcount".equals(tn.getName())) {
                    maxJspsLoadedCount = getCountStatisticValue(tn.getValue());
                } else if ("totaljspsloadedcount".equals(tn.getName())) {
                    totalJspsLoadedCount = getCountStatisticValue(tn.getValue());
                } else if ("activeservletsloadedcount".equals(tn.getName())) {
                    activeServletsLoadedCount = getRangeStatisticValue(tn.getValue());
                } else if ("maxservletsloadedcount".equals(tn.getName())) {
                    maxServletsLoadedCount = getCountStatisticValue(tn.getValue());
                } else if ("totalservletsloadedcount".equals(tn.getName())) {
                    totalServletsLoadedCount = getCountStatisticValue(tn.getValue());
                }
            }

        }

        report.setMessage(String.format(displayFormat,
                activeSessionsCount, activatedSessionsTotal,
                rejectedSessionsTotal, sessionsTotal,
                activeJspsLoadedCount, maxJspsLoadedCount,
                totalJspsLoadedCount,
                activeServletsLoadedCount, maxServletsLoadedCount,
                totalServletsLoadedCount));

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
