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

package org.glassfish.deployment.admin;

import com.sun.enterprise.admin.util.ClusterOperationUtil;
import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.Supplemental;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext.Phase;
import org.jvnet.hk2.annotations.Service;

/**
 * Causes InstanceDeployCommand executions on the correct remote instances.
 *
 * @author tjquinn
 */
@Service(name="_postdeploy")
@Supplemental(value="deploy", ifFailure=FailurePolicy.Warn)
@PerLookup
@ExecuteOn(value={RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="_postdeploy",
        description="_postdeploy")
})
@AccessRequired(resource=DeploymentCommandUtils.APPLICATION_RESOURCE_NAME, action="write")
public class PostDeployCommand extends DeployCommandParameters implements AdminCommand {

    @Inject
    private ServiceLocator habitat;

    @Inject
    private Deployment deployment;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();

        final DeployCommandSupplementalInfo suppInfo =
                context.getActionReport().getResultType(DeployCommandSupplementalInfo.class);
        final DeploymentContext dc = suppInfo.deploymentContext();
        final DeployCommandParameters params = dc.getCommandParameters(DeployCommandParameters.class);
        final InterceptorNotifier notifier = new InterceptorNotifier(habitat, dc);


        // if the target is DAS, we do not need to do anything more
        if (DeploymentUtils.isDASTarget(params.target)) {
            return;
        }

        try {
            final ParameterMap paramMap = deployment.prepareInstanceDeployParamMap(dc);

            List<String> targets = new ArrayList<String>();
            if (!DeploymentUtils.isDomainTarget(params.target)) {
                targets.add(params.target);
            } else {
                targets = suppInfo.previousTargets();
            }
            ClusterOperationUtil.replicateCommand(
                "_deploy",
                FailurePolicy.Warn,
                FailurePolicy.Warn,
                FailurePolicy.Ignore,
                targets,
                context,
                paramMap,
                habitat);
            notifier.ensureAfterReported(Phase.REPLICATION);
        } catch (Exception e) {
            report.failure(logger, e.getMessage());
        }
    }
}
