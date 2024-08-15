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

import com.sun.enterprise.config.serverbeans.ClusterRef;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.api.Target;
import org.glassfish.loadbalancer.config.LbConfig;
import org.glassfish.loadbalancer.config.LbConfigs;
import org.jvnet.hk2.annotations.Service;

/**
 * This is a remote commands to list lb configs (ported from v2)
 * Interestingly, in this command, operand has multiple meanings - it can be
 * a target (cluster or standalone instance) or a lb config name.
 * And operand is optional!
 *
 *   No operand          : list all lb configs
 *   Operand is LB config: list all cluster-refs and server-refs for the LB config
 *   Operand is cluster  : list lb configs referencing the cluster
 *   Operand is instance : list all configs referencing the server instance
 *
 * @author Yamini K B
 */
@Service(name = "list-http-lb-configs")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@org.glassfish.api.admin.ExecuteOn(RuntimeType.DAS)
@RestEndpoints({
    @RestEndpoint(configBean=LbConfigs.class,
        opType=RestEndpoint.OpType.GET,
        path="list-http-lb-configs",
        description="list-http-lb-configs")
})
public final class ListLBConfigsCommand implements AdminCommand {

    @Param(primary=true, optional=true)
    String list_target;

    @Inject
    Domain domain;

    @Inject
    Target tgt;

    @Inject
    Logger logger;

    private ActionReport report;

    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(ListLBConfigsCommand.class);

    @Override
    public void execute(AdminCommandContext context) {

        report = context.getActionReport();

        ActionReport.MessagePart part = report.getTopMessagePart();

        boolean isCluster = tgt.isCluster(list_target);

        LbConfigs lbconfigs = domain.getExtensionByType(LbConfigs.class);
        if (lbconfigs == null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(localStrings.getLocalString(
                            "http_lb_admin.NoLbConfigs", "No lb configs"));
                }
                return;
        }

        List<LbConfig> lbconfigsList = lbconfigs.getLbConfig();
        if (lbconfigsList.size() == 0) {
                logger.fine(localStrings.getLocalString(
                        "http_lb_admin.NoLbConfigs", "No lb configs"));
                return;
        }

        if (list_target == null) {
            for (LbConfig lbc: lbconfigsList) {
                ActionReport.MessagePart childPart = part.addChild();
                childPart.setMessage(lbc.getName());
            }
        } else {
            // target is a cluster
            if (isCluster) {

                for (LbConfig lbc: lbconfigsList) {
                    List<ClusterRef> refs = lbc.getRefs(ClusterRef.class);
                    for (ClusterRef cRef:refs) {
                       if (cRef.getRef().equals(list_target) ) {
                            ActionReport.MessagePart childPart = part.addChild();
                            childPart.setMessage(lbc.getName());
                       }
                    }
                }


            // target is a server
            } else if (domain.isServer(list_target)) {

                for (LbConfig lbc: lbconfigsList) {
                    List<ServerRef> refs = lbc.getRefs(ServerRef.class);
                    for (ServerRef sRef:refs) {
                       if (sRef.getRef().equals(list_target) ) {
                            ActionReport.MessagePart childPart = part.addChild();
                            childPart.setMessage(lbc.getName());
                       }
                    }
                }


            } else {

                // target is a lb config
                LbConfig lbConfig = lbconfigs.getLbConfig(list_target);

                if (lbConfig != null) {

                    List<ClusterRef> cRefs = lbConfig.getRefs(ClusterRef.class);
                    for (ClusterRef ref: cRefs) {
                        String s = localStrings.getLocalString("ClusterPrefix", "Cluster:");
                        ActionReport.MessagePart childPart = part.addChild();
                        childPart.setMessage(s + ref.getRef());
                    }

                    List<ServerRef> sRefs = lbConfig.getRefs(ServerRef.class);
                    for (ServerRef ref: sRefs) {
                        String s = localStrings.getLocalString("ServerPrefix", "Server:");
                        ActionReport.MessagePart childPart = part.addChild();
                        childPart.setMessage(s + ref.getRef());
                    }
                }
            }
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
