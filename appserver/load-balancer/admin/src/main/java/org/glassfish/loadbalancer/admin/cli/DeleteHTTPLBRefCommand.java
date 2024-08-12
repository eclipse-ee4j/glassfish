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

import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ClusterRef;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.api.Target;
import org.glassfish.loadbalancer.config.LbConfig;
import org.glassfish.loadbalancer.config.LbConfigs;
import org.glassfish.loadbalancer.config.LoadBalancer;
import org.glassfish.loadbalancer.config.LoadBalancers;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 *
 * This is a remote command that supports the delete-http-lb-ref CLI command.
 *
 * @author Yamini K B
 */
@Service(name = "delete-http-lb-ref")
@PerLookup
@I18n("delete.http.lb.ref")
@TargetType(value={CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER})
@org.glassfish.api.admin.ExecuteOn(RuntimeType.DAS)
@RestEndpoints({
    @RestEndpoint(configBean=LbConfig.class,
        opType=RestEndpoint.OpType.POST, // TODO: Should be DELETE
        path="delete-http-lb-ref",
        description="delete-http-lb-ref")
})
public final class DeleteHTTPLBRefCommand extends LBCommandsBase
        implements AdminCommand {

    @Param(optional=true)
    String config;

    @Param(optional=true)
    String lbname;

    @Param(optional=true, defaultValue = "false")
    String force;

    @Param(primary=true)
    String target;

    @Inject
    Target tgt;

    @Inject
    Logger logger;

    private ActionReport report;

    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(CreateHTTPLBRefCommand.class);

    @Override
    public void execute(AdminCommandContext context) {

        report = context.getActionReport();

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        boolean isCluster = tgt.isCluster(target);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("[LB-ADMIN] deleteLBRef called for target " + target);
        }

        if (config!=null && lbname!=null) {
            String msg = localStrings.getLocalString("EitherConfigOrLBName",
                    "Either LB name or LB config name, not both");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        if (config==null && lbname==null) {
            String msg = localStrings.getLocalString("SpecifyConfigOrLBName",
                    "Please specify either LB name or LB config name.");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        LbConfigs lbconfigs = domain.getExtensionByType(LbConfigs.class);
        if (lbconfigs == null) {
            String msg = localStrings.getLocalString("NoLbConfigsElement",
                    "Empty lb-configs");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        if (config != null) {
            if (lbconfigs.getLbConfig(config) == null) {
                String msg = localStrings.getLocalString("LbConfigDoesNotExist",
                        "Specified LB config {0} does not exist", config);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }
        } else if (lbname != null) {
            LoadBalancers lbs = domain.getExtensionByType(LoadBalancers.class);
            if (lbs == null) {
                String msg = localStrings.getLocalString("NoLoadBalancersElement",
                        "No Load balancers defined.");
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }
            LoadBalancer lb = lbs.getLoadBalancer(lbname);
            if (lb == null) {
                String msg = localStrings.getLocalString("LoadBalancerNotDefined",
                        "Load balancer [{0}] not found.", lbname);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }
            config = lb.getLbConfigName();
        }

        // target is a cluster
        if (isCluster) {
            deleteClusterFromLBConfig(lbconfigs, config, target);
            String msg = localStrings.getLocalString("http_lb_admin.DeleteClusterFromConfig",
                    "Deleted cluster {0} from Lb", target, config);
            logger.info(msg);

        // target is a server
        } else if (domain.isServer(target)) {
            deleteServerFromLBConfig(lbconfigs, config, target);
            String msg = localStrings.getLocalString("http_lb_admin.DeleteServerFromConfig",
                    "Deleted server {0} from Lb", target, config);
            logger.info(msg);
        } else {
            String msg = localStrings.getLocalString("InvalidTarget", "Invalid target", target);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }
    }

    private void deleteServerFromLBConfig(LbConfigs lbconfigs, String configName,String serverName) {
        LbConfig lbConfig = lbconfigs.getLbConfig(configName);

        ServerRef  sRef = lbConfig.getRefByRef(ServerRef.class, serverName);
        if (sRef == null) {
            // does not exist, just return from here
            String msg = localStrings.getLocalString("ServerNotDefined",
                        "Server {0} cannot be used as target", target);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Server " + serverName +
                        " does not exist in any cluster in the domain");
            }
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }
        if (!Boolean.parseBoolean(force)) {
            if (Boolean.parseBoolean(sRef.getLbEnabled())) {
                String msg = localStrings.getLocalString("ServerNeedsToBeDisabled",
                        "Server [{0}] needs to be disabled before it can be removed from the load balancer.",
                        serverName);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }
            // check if its applications are LB disabled.
            Server s = domain.getServerNamed(serverName);

            if (s == null ) {
                String msg = localStrings.getLocalString("ServerNotDefined",
                            "Server {0} cannot be used as target", target);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }
            List<ApplicationRef> appRefs = domain.getApplicationRefsInTarget(target);

            if (appRefs == null) {
                String msg = localStrings.getLocalString("AppRefsNotDefined",
                        "Application refs does not exist in server {0}", target);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }
            boolean appLbEnabled = false;
            for(ApplicationRef aRef:appRefs) {
                if(Boolean.parseBoolean(aRef.getLbEnabled())) {
                    appLbEnabled = true;
                    break;
                }
            }

            if (appLbEnabled) {
                String msg = localStrings.getLocalString("AppsNotDisabled",
                        "All referenced applications must be disabled in LB");
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }
        }

        removeServerRef(lbConfig, sRef);
    }

    private void deleteClusterFromLBConfig(LbConfigs lbconfigs, String configName, String clusterName) {
        LbConfig lbConfig = lbconfigs.getLbConfig(configName);

        ClusterRef cRef = lbConfig.getRefByRef(ClusterRef.class, clusterName);
        if (cRef == null) {
            // does not exist, just return from here
            String msg = localStrings.getLocalString("ClusterNotDefined",
                        "Cluster {0} cannot be used as target", target);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Cluster " + clusterName + " does not exist.");
            }
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        if (!Boolean.parseBoolean(force)) {
            Cluster c = domain.getClusterNamed(clusterName);
            if ( c == null ) {
                String msg = localStrings.getLocalString("ClusterNotDefined",
                            "Cluster {0} cannot be used as target", target);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }

            List<ServerRef> sRefs = c.getServerRef();
            boolean refLbEnabled = false;
            for(ServerRef ref:sRefs) {
                if(Boolean.parseBoolean(ref.getLbEnabled())) {
                    refLbEnabled = true;
                }
            }

            if (refLbEnabled) {
                String msg = localStrings.getLocalString("ServerNeedsToBeDisabled",
                        "Server [{0}] needs to be disabled before it can be removed from the load balancer.", target);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }
        }
        removeClusterRef(lbConfig, cRef);
    }

    public void removeServerRef(final LbConfig lc, final ServerRef sRef) {
        try {
            ConfigSupport.apply(new SingleConfigCode<LbConfig>() {
                    @Override
                    public Object run(LbConfig param) throws PropertyVetoException, TransactionFailure {
                        param.getClusterRefOrServerRef().remove(sRef);
                        return Boolean.TRUE;
                    }
            }, lc);
        } catch (TransactionFailure ex) {
            String msg = localStrings.getLocalString("FailedToRemoveServerRef", "Failed to remove server-ref");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            report.setFailureCause(ex);
            return;
        }
    }

    public void removeClusterRef(final LbConfig lc, final ClusterRef cRef) {
        try {
            ConfigSupport.apply(new SingleConfigCode<LbConfig>() {
                    @Override
                    public Object run(LbConfig param) throws PropertyVetoException, TransactionFailure {
                        param.getClusterRefOrServerRef().remove(cRef);
                        return Boolean.TRUE;
                    }
            }, lc);
        } catch (TransactionFailure ex) {
            String msg = localStrings.getLocalString("FailedToRemoveClusterRef", "Failed to remove cluster-ref");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            report.setFailureCause(ex);
            return;
        }
    }
}
