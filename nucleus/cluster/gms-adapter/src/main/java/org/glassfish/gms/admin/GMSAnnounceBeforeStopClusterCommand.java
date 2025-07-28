/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.gms.admin;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.ee.cms.core.GMSConstants;
import com.sun.enterprise.ee.cms.core.GroupManagementService;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.Supplemental;
import org.glassfish.api.logging.LogLevel;
import org.glassfish.gms.bootstrap.GMSAdapter;
import org.glassfish.gms.bootstrap.GMSAdapterService;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;
import org.jvnet.hk2.annotations.Service;


/**
 * Informs the dynamic group-management-service of Shoal GMS that all members of the cluster are stopping.
 * The GMS Notification PlannedShutdownNotification will have a subeventype of GROUP_SHUTDOWN rather than
 * INSTANCE_SHUTDOWN.  GMS clients may take advantage of this information that the entire cluster is stopping
 * rather than a single instance.
 */
@Service(name = "_gms-announce-before-stop-cluster-command")
@Supplemental(value = "stop-cluster", on = Supplemental.Timing.Before, ifFailure = FailurePolicy.Warn)
@PerLookup
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="_gms-announce-before-stop-cluster-command",
        description="_gms-announce-before-stop-cluster-command")
})
public class GMSAnnounceBeforeStopClusterCommand implements AdminCommand {

    //private static final Logger logger = LogDomains.getLogger(
    //    GMSAnnounceBeforeStopClusterCommand.class, LogDomains.GMS_LOGGER);

    @LoggerInfo(subsystem = "CLSTR", description="Group Management Service Admin Logger", publish=true)
    private static final String GMS_LOGGER_NAME = "jakarta.enterprise.cluster.gms.admin";


    @LogMessagesResourceBundle
    private static final String LOG_MESSAGES_RB = "org.glassfish.cluster.gms.LogMessages";

    static final Logger GMS_LOGGER = Logger.getLogger(GMS_LOGGER_NAME, LOG_MESSAGES_RB);

    //cluster.stop.exception=GMSAD3005: An exception occurred while announcing GMS group shutdown: {0}
    //GMSAD3005.diag.cause.1=An unexpected exception occurred in the GMS implementation.
    //GMSAD3005.diag.check.1=Check the server log file for more information from Shoal-GMS.
    @LogMessageInfo(message = "An exception occurred while announcing GMS group shutdown: {0}",
        level="WARNING",
        cause="An unexpected exception occurred in the GMS implementation.",
        action="Check the server log file for more information from Shoal-GMS.")
    private static final String GMS_CLUSTER_STOP_EXCEPTION="NCLS-CLSTR-30005";


    @Inject
    private ServerEnvironment env;
    @Param(optional = false, primary = true)
    private String clusterName;
    @Inject
    private Domain domain;
    @Param(optional = true, defaultValue = "false")
    private boolean verbose;
    @Inject
    private GMSAdapterService gmsAdapterService;

    static final private List<String> EMPTY_LIST = new LinkedList<String>();


    @Override
    public void execute(AdminCommandContext context) {
        GroupManagementService gms = null;
        GMSAdapter gmsadapter = null;
        boolean gmsStopCluster = false;
        List<String> clusterMembers = EMPTY_LIST;
        ActionReport report = context.getActionReport();
        try {
            if (gmsAdapterService.isGmsEnabled()) {
                gmsadapter = gmsAdapterService.getGMSAdapterByName(clusterName);

                //  gmsadapter can be null if GMSEnabled=false for clusterName.
                if (gmsadapter != null) {
                    gms = gmsadapter.getModule();
                    if (gms != null) {
                        clusterMembers = getClusterMembers();

                        // no need to announce a zero instance cluster.
                        if (clusterMembers != null && clusterMembers.size() > 0) {

                            // one or more clustered instances for this cluster in domain.xml.
                            // now check if any clustered instance are already running.
                            // DAS is a SPECTATOR so it will not be in list of current core members.
                            // If one or more clustered instances is already running,  do not consider this a GROUP_STARTUP,
                            // but treat as a series of individual INSTANCE_STARTUP for instances that do get started.
                            // no gms calls needed if not a GROUP_STARTUP.
                            List<String> startedGMSMembers = gms.getGroupHandle().getCurrentCoreMembers();
                            if (startedGMSMembers.size() > 0) {
                                try {
                                    // must be called on DAS only.
                                    gms.announceGroupShutdown(clusterName, GMSConstants.shutdownState.INITIATED);
                                    gmsStopCluster = true;
                                } catch (Throwable t) {

                                    // ensure gms group startup announcement does not interfere with starting cluster.
                                    // any exception here should not interfer with starting cluster.
                                    GMS_LOGGER.log(LogLevel.WARNING,
                                        GMS_CLUSTER_STOP_EXCEPTION,
                                        t.getLocalizedMessage());
                                }
                            } // else from GMS perspective treat remaining instances getting started as INSTANCE_START, not GROUP_START.
                            // nothing gms specific to do for this case.
                        }
                    }
                }
            }
        } finally {
            if (gms != null) {
                GMSAnnounceSupplementalInfo result = new GMSAnnounceSupplementalInfo(clusterMembers, gmsStopCluster, gmsadapter);
                report.setResultType(GMSAnnounceSupplementalInfo.class,  result);
            }
        }
    }

    private List<String> getClusterMembers() {
        List<String> clusterMembers = EMPTY_LIST;
        com.sun.enterprise.config.serverbeans.Cluster cluster = domain.getClusterNamed(clusterName);
        List<Server> targetServers = null;
        if (cluster != null) {
            // Get the list of servers in the cluster.
            targetServers = domain.getServersInTarget(clusterName);
            if (targetServers != null) {
                clusterMembers = new ArrayList<String>(targetServers.size());
                for (Server server : targetServers) {
                    clusterMembers.add(server.getName());
                }
            }
        }
        return clusterMembers;
    }
}
