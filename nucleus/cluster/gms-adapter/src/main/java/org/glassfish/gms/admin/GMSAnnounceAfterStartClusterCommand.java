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

package org.glassfish.gms.admin;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.ee.cms.core.GMSConstants;

import jakarta.inject.Inject;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.Supplemental;
import org.glassfish.api.logging.LogLevel;
import org.glassfish.gms.bootstrap.GMSAdapterService;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;
import org.jvnet.hk2.annotations.Service;


@Service(name = "_gms-announce-after-start-cluster-command")
@Supplemental(value = "start-cluster", on = Supplemental.Timing.After, ifFailure = FailurePolicy.Warn)
@PerLookup
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="_gms-announce-after-start-cluster-command",
        description="_gms-announce-after-start-cluster-command")
})
public class GMSAnnounceAfterStartClusterCommand implements AdminCommand {

    //private static final Logger logger = LogDomains.getLogger(
    //    GMSAnnounceAfterStartClusterCommand.class, LogDomains.GMS_LOGGER);

    @LoggerInfo(subsystem = "CLSTR", description="Group Management Service Admin Logger", publish=true)
    private static final String GMS_ADMIN_LOGGER_NAME = "jakarta.enterprise.cluster.gms.admin";


    @LogMessagesResourceBundle
    private static final String LOG_MESSAGES_RB = "org.glassfish.cluster.gms.LogMessages";

    static final Logger GMS_ADMIN_LOGGER = Logger.getLogger(GMS_ADMIN_LOGGER_NAME, LOG_MESSAGES_RB);

    //after.start=GMSAD3001: GMSAnnounceAfterStartClusterCommand: exitCode:{0} members {1} clusterMembers:{2}
    @LogMessageInfo(message = "GMSAnnounceAfterStartClusterCommand: exitCode:{0} members {1} clusterMembers:{2}",
        level="INFO")
    private static final String GMSADMIN_AFTER_START="NCLS-CLSTR-30001";

    //group.start.exception=GMSAD3002: An exception occurred while announcing GMS group startup: {0}
    //GMSAD3002.diag.cause.1=An unexpected exception occurred in the GMS implementation.
    //GMSAD3002.diag.check.1=Check the server log file for more information from Shoal-GMS.
    @LogMessageInfo(message = "An exception occurred while announcing GMS group startup: {0}",
        level="WARNING",
        cause="An unexpected exception occurred in the GMS implementation.",
        action="Check the server log file for more information from Shoal-GMS.")
    private static final String GMS_START_EXCEPTION="NCLS-CLSTR-30002";


    @Inject
    private ServerEnvironment env;
    @Inject
    private ServiceLocator habitat;
    @Param(optional = false, primary = true)
    private String clusterName;
    @Inject
    private Domain domain;
    @Inject
    private CommandRunner runner;
    @Param(optional = true, defaultValue = "false")
    private boolean verbose;

    @Inject
    GMSAdapterService gmsAdapterService;


    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        announceGMSGroupStartupComplete(clusterName, report);
    }

    static public void announceGMSGroupStartupComplete(String clusterName, ActionReport report) {
        if (report != null) {
            GMSAnnounceSupplementalInfo gmsInfo = report.getResultType(GMSAnnounceSupplementalInfo.class);
            if (gmsInfo != null && gmsInfo.gmsInitiated) {
                List<String> members = null;
                GMSConstants.groupStartupState groupStartupState = GMSConstants.groupStartupState.COMPLETED_FAILED;

                switch (report.getActionExitCode()) {
                    case SUCCESS:
                        // all instances started
                        members = gmsInfo.clusterMembers;
                        groupStartupState = GMSConstants.groupStartupState.COMPLETED_SUCCESS;
                        break;

                    case FAILURE:      // all instances failed.  should be in members list.
                        members = gmsInfo.clusterMembers;
                        groupStartupState = GMSConstants.groupStartupState.COMPLETED_FAILED;
                        break;

                    case WARNING:
                        // at least one instance started

                        // list differs based on report action result.

                        // list of failed for non SUCCESS result.  List of succeeded for SUCCESS.
                        // this is better approach than parsing report.getMessage() for failed instances.
                        // todo:  get this list to be the members that failed during start-cluster.
                        members = (List<String>)report.getResultType(List.class);


                        groupStartupState = GMSConstants.groupStartupState.COMPLETED_FAILED;
                        break;

                    default:
                }
                GMS_ADMIN_LOGGER.log(LogLevel.INFO, GMSADMIN_AFTER_START, new Object [] {
                    report.getActionExitCode(), members, gmsInfo.clusterMembers
                });
                try {
                    if (gmsInfo.gms != null) {
                        if (members == null) {
                            members = new LinkedList<String>();
                        }

                        gmsInfo.gms.announceGroupStartup(clusterName, groupStartupState, members);
                    }
                } catch (Throwable t) {
                    // ensure gms group startup announcement does not interfere with starting cluster.
                    GMS_ADMIN_LOGGER.log(LogLevel.WARNING, GMS_START_EXCEPTION,
                        t.getLocalizedMessage());
                }
            }
        }
    }

}
