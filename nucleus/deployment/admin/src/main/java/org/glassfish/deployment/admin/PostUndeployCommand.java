/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.Supplemental;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.jvnet.hk2.annotations.Service;

/**
 * Runs after any replications of the undeploy command have been sent to
 * instances.
 *
 * @author Tim Quinn
 */
@Service(name="_postundeploy")
@Supplemental(value="undeploy", ifFailure=FailurePolicy.Warn, on= Supplemental.Timing.AfterReplication)
@PerLookup
@ExecuteOn(value={RuntimeType.DAS})
@AccessRequired(resource=DeploymentCommandUtils.APPLICATION_RESOURCE_NAME, action="write")

public class PostUndeployCommand extends UndeployCommandParameters implements AdminCommand {

    @Inject
    private ServiceLocator habitat;

    @Override
    public void execute(AdminCommandContext context) {
        final Logger logger = context.getLogger();
        logger.log(Level.INFO, "PostUndeployCommand starting");
      try {
        ActionReport report = context.getActionReport();

        final DeployCommandSupplementalInfo suppInfo =
                context.getActionReport().getResultType(DeployCommandSupplementalInfo.class);
        /*
         * If the user undeployed by specifying the target as "domain" then
         * the undeploy command has already explicitly sent the undeploy command
         * to the instances and has notified the after(replication) listeners.
         * In that case, the suppInfo won't have been set in the deployment
         * context.
         */
        if (suppInfo == null) {
            return;
        }
        final ExtendedDeploymentContext dc = suppInfo.deploymentContext();

        final InterceptorNotifier notifier = new InterceptorNotifier(habitat, dc);

        try {
            notifier.ensureAfterReported(ExtendedDeploymentContext.Phase.REPLICATION);
            logger.log(Level.INFO, "PostUndeployCommand done successfully");
        } catch (Exception e) {
            report.failure(logger, e.getMessage());
            logger.log(Level.SEVERE, "Error in inner PostUndeployCommand", e);
        }
      }
      catch (Exception e) {
          logger.log(Level.SEVERE, "Error in outer PostUndeployCommand", e);
      }
    }
}
