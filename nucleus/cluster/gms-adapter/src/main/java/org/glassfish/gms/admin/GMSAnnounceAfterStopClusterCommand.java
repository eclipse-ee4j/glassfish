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

import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.Supplemental;
import org.glassfish.api.logging.LogLevel;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;
import org.jvnet.hk2.annotations.Service;


@Service(name = "_gms-announce-after-stop-cluster-command")
@Supplemental(value = "stop-cluster", on = Supplemental.Timing.After, ifFailure = FailurePolicy.Warn)
@PerLookup
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="_gms-announce-after-stop-cluster-command",
        description="_gms-announce-after-stop-cluster-command")
})
public class GMSAnnounceAfterStopClusterCommand implements AdminCommand {

    //private static final Logger logger = LogDomains.getLogger(
    //    GMSAnnounceAfterStopClusterCommand.class, LogDomains.GMS_LOGGER);

    @LoggerInfo(subsystem = "CLSTR", description="Group Management Service Admin Logger", publish=true)
    private static final String GMS_LOGGER_NAME = "jakarta.enterprise.cluster.gms.admin";


    @LogMessagesResourceBundle
    private static final String LOG_MESSAGES_RB = "org.glassfish.cluster.gms.LogMessages";

    static final Logger GMS_LOGGER = Logger.getLogger(GMS_LOGGER_NAME, LOG_MESSAGES_RB);

    //# GMSAnnounceAfterStopClusterCommand
    //group.stop.exception=GMSAD3003: An exception occurred while announcing GMS group shutdown: {0}
    //GMSAD3003.diag.cause.1=An unexpected exception occurred in the GMS implementation.
    //GMSAD3003.diag.check.1=Check the server log file for more information from Shoal-GMS.
    @LogMessageInfo(message = "An exception occurred while announcing GMS group shutdown: {0}",
        level="WARNING",
        cause="An unexpected exception occurred in the GMS implementation.",
        action="Check the server log file for more information from Shoal-GMS.")
    private static final String GMS_GROUP_STOP_EXCEPTION="NCLS-CLSTR-30003";

    @Param(optional = false, primary = true)
    private String clusterName;

    @Param(optional = true, defaultValue = "false")
    private boolean verbose;


    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        announceGMSGroupStopComplete(clusterName, report);
    }

    static public void announceGMSGroupStopComplete(String clusterName, ActionReport report) {
        if (report != null) {
            GMSAnnounceSupplementalInfo gmsInfo = report.getResultType(GMSAnnounceSupplementalInfo.class);
            if (gmsInfo != null && gmsInfo.gmsInitiated) {
                GMSConstants.shutdownState groupShutdownState = GMSConstants.shutdownState.COMPLETED;
                try {
                    if (gmsInfo.gms != null) {
                        gmsInfo.gms.announceGroupShutdown(clusterName, groupShutdownState);
                    }
                } catch (Throwable t) {
                    // ensure gms group startup announcement does not interfere with starting cluster.
                    GMS_LOGGER.log(LogLevel.WARNING, GMS_GROUP_STOP_EXCEPTION,
                        t.getLocalizedMessage());
                }
            }
        }
    }

}
