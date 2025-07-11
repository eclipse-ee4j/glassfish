/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.admin.util.ClusterOperationUtil;
import com.sun.enterprise.admin.util.InstanceStateService;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ClusterExecutor;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.CommandModelProvider;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.InstanceState;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.common.util.admin.CommandModelImpl;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.jvnet.hk2.annotations.Service;

/**
 * A ClusterExecutor is responsible for remotely executing commands.
 * The list of target servers (either clusters or remote instances) is obtained
 * from the parameter list.
 *
 * @author Vijay Ramachandran
 */
@Service(name = "GlassFishClusterExecutor")
public class GlassFishClusterExecutor implements ClusterExecutor {
    private static final LocalStringManagerImpl strings = new LocalStringManagerImpl(GlassFishClusterExecutor.class);

    @Inject
    private InstanceStateService instanceState;

    @Inject
    private Target targetService;

    @Inject
    private ServiceLocator habitat;

    /**
     * <p>Execute the passed command on targeted remote instances. The list of remote
     * instances is usually retrieved from the passed parameters (with a "target"
     * parameter for instance) or from the configuration.
     *
     * <p>Each remote execution must return a different ActionReport so the user
     * or framework can get feedback on the success or failure or such executions.
     *
     * @param commandName the command to execute
     * @param context the original command context
     * @param parameters the parameters passed to the original local command
     * @return an array of @{link org.glassfish.api.ActionReport} for each remote
     * execution status.
     */
    @Override
    public ActionReport.ExitCode execute(String commandName, AdminCommand command, AdminCommandContext context, ParameterMap parameters) {

        CommandModel model =
            command instanceof CommandModelProvider ?
                ((CommandModelProvider)command).getModel() :
                new CommandModelImpl(command.getClass());

        org.glassfish.api.admin.ExecuteOn clAnnotation = model.getClusteringAttributes();
        List<RuntimeType> runtimeTypes = new ArrayList<RuntimeType>();
        @ExecuteOn final class DefaultExecuteOn {}
        if(clAnnotation == null) {
            clAnnotation = DefaultExecuteOn.class.getAnnotation(ExecuteOn.class);
        }
        if (clAnnotation.value().length == 0) {
            runtimeTypes.add(RuntimeType.DAS);
            runtimeTypes.add(RuntimeType.INSTANCE);
        } else {
            runtimeTypes.addAll(Arrays.asList(clAnnotation.value()));
        }

        String targetName = parameters.getOne("target");
        if(targetName == null) {
            targetName = "server";
        }
        //Do replication only if the RuntimeType specified is ALL or
        //only if the target is not "server" or "domain"
        if( (runtimeTypes.contains(RuntimeType.ALL)) ||
            ((!CommandTarget.DAS.isValid(habitat, targetName)) && (!CommandTarget.DOMAIN.isValid(habitat, targetName))) ) {
            //If the target is a cluster and dynamic reconfig enabled is false and RuntimeType is not ALL, no replication
            if (targetService.isCluster(targetName) && !runtimeTypes.contains(RuntimeType.ALL)) {
                String dynRecfg = targetService.getClusterConfig(targetName).getDynamicReconfigurationEnabled();
                if(Boolean.FALSE.equals(Boolean.valueOf(dynRecfg))) {
                    ActionReport aReport = context.getActionReport().addSubActionsReport();
                    aReport.setActionExitCode(ActionReport.ExitCode.WARNING);
                    aReport.setMessage(strings.getLocalString("glassfish.clusterexecutor.dynrecfgdisabled",
                            "WARNING: The command was not replicated to all cluster instances because the" +
                                    " dynamic-reconfig-enabled flag is set to false for cluster {0}", targetName));
                    for(Server s : targetService.getInstances(targetName)) {
                        instanceState.setState(s.getName(), InstanceState.StateType.RESTART_REQUIRED, false);
                        instanceState.addFailedCommandToInstance(s.getName(), commandName, parameters);
                    }
                    return ActionReport.ExitCode.WARNING;
                }
            }

            List<Server> instancesForReplication = new ArrayList<Server>();

            if (runtimeTypes.contains(RuntimeType.ALL)) {
                List<Server> allInstances = targetService.getAllInstances();
                Set<String> clusterNoReplication = new HashSet<String>();
                for (Server s : allInstances) {
                    String dynRecfg = s.getConfig().getDynamicReconfigurationEnabled();
                    if (Boolean.TRUE.equals(Boolean.valueOf(dynRecfg))) {
                        instancesForReplication.add(s);
                    } else {
                        clusterNoReplication.add(s.getCluster().getName());
                        instanceState.setState(s.getName(), InstanceState.StateType.RESTART_REQUIRED, false);
                        instanceState.addFailedCommandToInstance(s.getName(), commandName, parameters);
                    }
                }

                if (!clusterNoReplication.isEmpty()) {
                    ActionReport aReport = context.getActionReport().addSubActionsReport();
                    aReport.setActionExitCode(ActionReport.ExitCode.WARNING);
                    aReport.setMessage(strings.getLocalString("glassfish.clusterexecutor.dynrecfgdisabled",
                            "WARNING: The command was not replicated to all cluster instances because the"
                            + " dynamic-reconfiguration-enabled flag is set to false for cluster(s) {0}", clusterNoReplication));
                }

            } else {
                instancesForReplication = targetService.getInstances(targetName);
            }

            if(instancesForReplication.isEmpty()) {
                ActionReport aReport = context.getActionReport().addSubActionsReport();
                aReport.setActionExitCode(ActionReport.ExitCode.SUCCESS);
                aReport.setMessage(strings.getLocalString("glassfish.clusterexecutor.notargets",
                        "Did not find any suitable instances for target {0}; command executed on DAS only", targetName));
                return ActionReport.ExitCode.SUCCESS;
            }

            return(ClusterOperationUtil.replicateCommand(commandName, clAnnotation.ifFailure(),
                    clAnnotation.ifOffline(), clAnnotation.ifNeverStarted(),
                    instancesForReplication, context, parameters, habitat));
        }
        return ActionReport.ExitCode.SUCCESS;
    }
}
