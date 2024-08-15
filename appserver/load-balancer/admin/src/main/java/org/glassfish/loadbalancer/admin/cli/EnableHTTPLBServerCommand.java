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

package org.glassfish.loadbalancer.admin.cli;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.util.List;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.api.Target;
import org.glassfish.loadbalancer.config.LbConfig;
import org.glassfish.loadbalancer.config.LbConfigs;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * This is a remote command that enables lb-enabled attribute of
 * server-ref
 * @author Yamini K B
 */
@Service(name = "enable-http-lb-server")
@PerLookup
@TargetType(value={CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CLUSTERED_INSTANCE})
@org.glassfish.api.admin.ExecuteOn(RuntimeType.DAS)
@RestEndpoints({
    @RestEndpoint(configBean=Cluster.class,
        opType=RestEndpoint.OpType.POST,
        path="enable-http-lb-server",
        description="enable-http-lb-server",
        params={
            @RestParam(name="id", value="$parent")
        }),
    @RestEndpoint(configBean=Server.class,
        opType=RestEndpoint.OpType.POST,
        path="enable-http-lb-server",
        description="enable-http-lb-server",
        params={
            @RestParam(name="id", value="$parent")
        })
})
public final class EnableHTTPLBServerCommand extends LBCommandsBase
                                             implements AdminCommand {

    @Param(primary=true)
    String target;

    @Inject
    Target tgt;

    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(EnableHTTPLBServerCommand.class);

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();

        Logger logger = context.getLogger();

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        LbConfigs lbconfigs = domain.getExtensionByType(LbConfigs.class);
        if (lbconfigs == null) {
            String msg = localStrings.getLocalString("NoLbConfigsElement",
                    "Empty lb-configs");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        if (tgt.isCluster(target)) {
            //enable all servers in cluster
            updateLBForCluster(report, target, "true", null);
        }  else {
            boolean foundTarget = false;
            List<LbConfig> lbConfigs = lbconfigs.getLbConfig();
            for (LbConfig lc:lbConfigs) {
                //ServerRef  sRef = lc.getServerRefByRef(target);
                ServerRef  sRef = lc.getRefByRef(ServerRef.class, target);
                if (sRef == null) {
                    //log a warning and continue search
                    logger.warning(localStrings.getLocalString("InvalidInstance",
                            "Server {0} does not exist in {1}", target, lc.getName()));
                } else {
                    boolean enabled = sRef.getLbEnabled().equals("true");
                    if (enabled == true) {
                        String msg = localStrings.getLocalString("ServerEnabled",
                                "Server [{0}] is already enabled.", sRef.getRef());
                        report.setMessage(msg);
                        return;
                    }
                    try {
                        updateLbEnabled(sRef, "true", null);
                    } catch (TransactionFailure ex) {
                        String msg = localStrings.getLocalString("FailedToUpdateAttr",
                            "Failed to update lb-enabled attribute for {0}", target);
                        report.setMessage(msg);
                        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                        report.setFailureCause(ex);
                        return;
                    }
                    foundTarget = true;
                }
            }
            // did not find server target
            if (!foundTarget) {
                ServerRef sRef = getServerRefFromCluster(report, target);
                if (sRef == null) {
                    String msg = localStrings.getLocalString("InvalidServer",
                            "Server {0} does not exist", target);
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    report.setMessage(msg);
                    return;
                } else {
                    boolean enabled = sRef.getLbEnabled().equals("true");
                    if (enabled == true) {
                        String msg = localStrings.getLocalString("ServerEnabled",
                                "Server [{0}] is already enabled.", sRef.getRef());
                        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                        report.setMessage(msg);
                        return;
                    }
                    try {
                        updateLbEnabled(sRef, "true", null);
                    } catch (TransactionFailure ex) {
                        String msg = localStrings.getLocalString("FailedToUpdateAttr",
                            "Failed to update lb-enabled attribute for {0}", target);
                        report.setMessage(msg);
                        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                        report.setFailureCause(ex);
                        return;
                    }
                }
            }
        }
    }
}
