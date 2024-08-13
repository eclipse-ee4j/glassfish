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
import com.sun.enterprise.config.serverbeans.ClusterRef;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.HealthChecker;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.logging.Level;
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
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * This is a remote command that deletes health-checker config for cluster or
 * server.
 * @author Yamini K B
 */
@Service(name = "delete-http-health-checker")
@PerLookup
@TargetType(value={CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER})
@org.glassfish.api.admin.ExecuteOn(RuntimeType.DAS)
@RestEndpoints({
    @RestEndpoint(configBean=Cluster.class,
        opType=RestEndpoint.OpType.POST,
        path="delete-http-health-checker",
        description="delete-http-health-checker",
        params={
            @RestParam(name="target", value="$parent")
        }),
    @RestEndpoint(configBean=Server.class,
        opType=RestEndpoint.OpType.POST,
        path="delete-http-health-checker",
        description="delete-http-health-checker",
        params={
            @RestParam(name="target", value="$parent")
        })
})
public final class DeleteHTTPHealthCheckerCommand implements AdminCommand {

    @Param(optional=true)
    String config;

    @Param(primary=true)
    String target;

    @Inject
    Domain domain;

    @Inject
    Target tgt;

    @Inject
    Logger logger;

    private ActionReport report;

    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(DeleteHTTPHealthCheckerCommand.class);

    @Override
    public void execute(AdminCommandContext context) {

        report = context.getActionReport();

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        LbConfigs lbconfigs = domain.getExtensionByType(LbConfigs.class);
        if (lbconfigs == null) {
            String msg = localStrings.getLocalString("NoLbConfigsElement",
                    "Empty lb-configs");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        if (config != null) {
            LbConfig lbConfig = lbconfigs.getLbConfig(config);
            deleteHealthCheckerInternal(lbConfig, target, false);
        } else {
            List<LbConfig> lbConfigs = lbconfigs.getLbConfig();
            if (lbConfigs.isEmpty()) {
                String msg = localStrings.getLocalString("NoLbConfigsElement",
                        "Empty lb-configs");
                logger.warning(msg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }
            for (LbConfig lc:lbConfigs) {
                deleteHealthCheckerInternal(lc, target, true);
            }
        }
    }

    /**
     * Deletes a health checker from a load balancer configuration.
     *
     * @param   lbConfig        Http load balancer configuration bean
     * @param   target          Name of a cluster or stand alone server instance
     * @param   ignoreFailure   if ignoreError is true, exceptions are not
     *                          thrown in the following cases
     *                          1). The specified server instance or cluster
     *                          does not exist in the LB config.
     *                          2).The target already contains the health checker
     *
     */
    private void deleteHealthCheckerInternal(LbConfig lbConfig, String target,
        boolean ignoreFailure) {

        // invalid lb config name
        if (lbConfig == null) {
            String msg = localStrings.getLocalString("InvalidLbConfigName", "Invalid LB configuration.");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        String lbConfigName = lbConfig.getName();

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("[LB-ADMIN] deleteHealthChecker called - LB Config Name: "
                + lbConfigName + ", Target: " + target);
        }

        // null target
        if (target == null) {
            String msg = localStrings.getLocalString("Nulltarget", "Null target");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        // target is a cluster
        if (tgt.isCluster(target)) {
            ClusterRef  cRef = lbConfig.getRefByRef(ClusterRef.class, target);

            // cluster is not associated to this lb config
            if ((cRef == null) && (ignoreFailure == false)){
                String msg = localStrings.getLocalString("UnassociatedCluster",
                        "Load balancer configuration [{0}] does not have a reference to the given cluster [{1}].",
                        lbConfigName, target);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }

            if (cRef != null) {
                HealthChecker hc = cRef.getHealthChecker();
                if (hc != null) {
                    removeHealthCheckerFromClusterRef(cRef);
                    String msg = localStrings.getLocalString("http_lb_admin.HealthCheckerDeleted",
                            "Health checker deleted for target {0}", target);
                    logger.info(msg);
                } else {
                   if (ignoreFailure == false) {
                       String msg = localStrings.getLocalString("HealthCheckerDoesNotExist",
                               "Health checker does not exist for target {0} in LB {1}", target, lbConfigName);
                       report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                       report.setMessage(msg);
                       return;
                    }
                }
            }

        // target is a server
        } else if (domain.isServer(target)) {
            ServerRef  sRef   = lbConfig.getRefByRef(ServerRef.class, target);

            // server is not associated to this lb config
            if ((sRef == null) && (ignoreFailure == false)) {
                String msg = localStrings.getLocalString("UnassociatedServer",
                        "Load balancer configuration [{0}] does not have a reference to the given server [{1}].",
                        lbConfigName, target);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }

            if (sRef != null) {
                HealthChecker hc  = sRef.getHealthChecker();
                if (hc != null) {
                    removeHealthCheckerFromServerRef(sRef);
                    String msg = localStrings.getLocalString("http_lb_admin.HealthCheckerDeleted",
                            "Health checker deleted for target {0}", target);
                    logger.info(msg);
                } else {
                    if (ignoreFailure == false) {
                        String msg = localStrings.getLocalString("HealthCheckerDoesNotExist",
                               "Health checker does not exist for target {0} in LB {1}", target, lbConfigName);
                        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                        report.setMessage(msg);
                        return;
                    }
                }
            }
        } else {
            String msg = localStrings.getLocalString("InvalidTarget", "Invalid target", target);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }
    }

    private void removeHealthCheckerFromClusterRef(ClusterRef cRef) {
        try {
            ConfigSupport.apply(new SingleConfigCode<ClusterRef>() {
            @Override
                public Object run(ClusterRef param) throws PropertyVetoException, TransactionFailure {
                    param.setHealthChecker(null);
                    return Boolean.TRUE;
                }
            }, cRef);
        } catch (TransactionFailure ex) {
            String msg = localStrings.getLocalString("FailedToRemoveHC", "Failed to remove health-checker");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            report.setFailureCause(ex);
            return;
        }
    }

    private void removeHealthCheckerFromServerRef(ServerRef sRef) {
        try {

            ConfigSupport.apply(new SingleConfigCode<ServerRef>() {
            @Override
                public Object run(ServerRef param) throws PropertyVetoException, TransactionFailure {
                    param.setHealthChecker(null);
                    return Boolean.TRUE;
                }
            }, sRef);
        } catch (TransactionFailure ex) {
            String msg = localStrings.getLocalString("FailedToRemoveHC", "Failed to remove health-checker");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            report.setFailureCause(ex);
            return;
        }
    }
}
