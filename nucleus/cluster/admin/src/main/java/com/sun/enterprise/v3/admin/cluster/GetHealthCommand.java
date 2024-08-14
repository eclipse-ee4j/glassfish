/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.gms.bootstrap.GMSAdapter;
import org.glassfish.gms.bootstrap.GMSAdapterService;
import org.glassfish.gms.bootstrap.HealthHistory;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * The get-health command that lists the health status of
 * instances within a cluster.
 * Currently only works when GMS is enabled. To implement the
 * non-GMS case, look for the else block with comment "non-gms"
 */
@Service(name="get-health")
@I18n("get.health.command")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@RestEndpoints({
    @RestEndpoint(configBean=Cluster.class,
        opType=RestEndpoint.OpType.GET,
        path="get-health",
        description="Get Health",
        params={
            @RestParam(name="id", value="$parent")
        })
})
public class GetHealthCommand implements AdminCommand {

    @Inject
    private Domain domain;
    @Inject
    private ServerEnvironment env;
    @Inject
    GMSAdapterService gmsAdapterService;

    @Param(optional=false, primary=true)
    @I18n("get.health.cluster.name")
    private String clusterName;


    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        Logger logger = context.getLogger();
        logger.log(Level.INFO, Strings.get("get.health.called", clusterName));

        // output will be handled within this method
        if (!checkEnvAndParams(logger, report)) {
            return;
        }

        /*
         * Check that gms is enabled for this cluster. Could also check
         * gmsAdapterService.isGmsEnabled() and
         * domain.getClusterNamed(clusterName).getGmsEnabled(), but that
         * should be redundant. If the GMSAdapter exists for the cluster,
         * we can use GMS.
         */
        GMSAdapter gmsAdapter = gmsAdapterService.getGMSAdapterByName(clusterName);
        if (gmsAdapter != null) {
            getHealthWithGMS(logger, report, gmsAdapter);
        } else {
            // if someone wants to implement the non-gms case, here's where
            setFail(logger, report, Strings.get("get.health.noGMS", clusterName));
        }

    }

    // return false for any failures
    private boolean checkEnvAndParams(Logger logger, ActionReport report) {

        // first check that we're the DAS
        if (!env.isDas()) {
            return setFail(logger, report, Strings.get("get.health.onlyRunsOnDas"));
        }

        // check that cluster exists
        Cluster cluster = domain.getClusterNamed(clusterName);
        if (cluster == null) {
            return setFail(logger, report, Strings.get("get.health.noCluster", clusterName));
        }

        // ok to go
        return true;
    }

    /*
     * Simply get the HealthHistory object from GMSAdapter and output
     * the information.
     */
    private void getHealthWithGMS(Logger logger, ActionReport report, GMSAdapter gmsAdapter) {
        StringBuilder result = new StringBuilder();
        HealthHistory history = gmsAdapter.getHealthHistory();
        if (history == null) {
            setFail(logger, report, Strings.get("get.health.noHistoryError"));
            return;
        }

        // check for data
        if (history.getInstances().isEmpty()) {
            report.setMessage(Strings.get(
                "get.health.no.instances", clusterName));
            return;
        }

        // order by instance name for human-readable output
        SortedSet<String> names = new TreeSet<String>(history.getInstances());

        // this list will be set in the "extra properties" used by admin console
        List<Properties> statesAndTimes =
            new ArrayList<Properties>(names.size());

        for(String name : names) {
            Properties instanceStateAndTime = new Properties();
            HealthHistory.InstanceHealth ih = history.getHealthByInstance(name);

            instanceStateAndTime.put("name", name);
            instanceStateAndTime.put("status", ih.state.name());

            if (HealthHistory.NOTIME == ih.time) {
                result.append(name + " " + ih.state);
                instanceStateAndTime.put("time", "");
            } else {
                result.append(Strings.get("get.health.instance.state.since",
                    name, ih.state, new Date(ih.time).toString()));
                instanceStateAndTime.put("time", String.valueOf(ih.time));
            }

            result.append("\n");
            statesAndTimes.add(instanceStateAndTime);
        }

        Properties instanceStateTimes = new Properties();
        instanceStateTimes.put("instances", statesAndTimes);
        report.setExtraProperties(instanceStateTimes);

        String rawResult = result.toString();
        report.setMessage(rawResult.substring(0, rawResult.lastIndexOf("\n")));
    }

    // come fail away, come fail away, come fail away with me....
    private boolean setFail(Logger logger, ActionReport report, String message) {
        logger.log(Level.WARNING, message);
        report.setMessage(message);
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        return false;
    }
}
