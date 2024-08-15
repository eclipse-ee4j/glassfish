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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.admin.monitor.cli.MonitorContract;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.web.admin.LogFacade;
import org.jvnet.hk2.annotations.Service;

/**
 * A Stats interface to represent the statistical data exposed by an
 * HTTP Listener.
 *
 * For v3 Prelude, following stats will be available
 * errorCount, maxTime, processingTime, and requestCount
 *
 */
@Service
@PerLookup
public class HTTPListenerStatsImpl implements MonitorContract {

    @Inject
    private MonitoringRuntimeDataRegistry mrdr;

    private static final Logger logger = LogFacade.getLogger();

    private static final ResourceBundle rb = logger.getResourceBundle();

    private final static String name = "httplistener";
    private final static String displayFormat = "%1$-4s %2$-4s %3$-6.2f %4$-4s";

    public String getName() {
        return name;
    }

    public ActionReport process(final ActionReport report, final String filter) {

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("HTTPListenerStatsImpl: process ...");
        }

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

        long errorCount = 0;
        long maxTime = 0;
        double processingTime = 0;
        long requestCount = 0;

        List<TreeNode> tnL = serverNode.getNodes("server.web.request.*");
        for (TreeNode tn : tnL) {
            if (tn.hasChildNodes()) {
                continue;
            }

            if ("errorcount".equals(tn.getName())) {
                errorCount = getCountStatisticValue(tn.getValue());
            } else if ("maxtime".equals(tn.getName())) {
                maxTime = getCountStatisticValue(tn.getValue());
            } else if ("processingtime".equals(tn.getName())) {
                processingTime = getCountStatisticValue(tn.getValue());
            } else if ("requestcount".equals(tn.getName())) {
                requestCount = getCountStatisticValue(tn.getValue());
            }
        }

        //report.setMessage(String.format(displayFormat, "ec", "mt", "pt", "rc"));
        report.setMessage(String.format(displayFormat,
            errorCount, maxTime, processingTime, requestCount));
        report.setActionExitCode(ExitCode.SUCCESS);
        return report;
    }

    private long getCountStatisticValue(Object obj) {
        long l = 0L;
        if (obj == null) return l;
        if (obj instanceof CountStatistic) {
            return ((CountStatistic)obj).getCount();
        }
        return l;
    }
}
