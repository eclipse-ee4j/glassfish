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

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AccessRequired.AccessCheck;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author Tim Quinn
 */
@Service
@PerLookup
public class PostStateCommand implements AdminCommand,
        AdminCommandSecurity.Preauthorization, AdminCommandSecurity.AccessCheckProvider {

    @Inject
    protected ServiceLocator habitat;

    private DeployCommandSupplementalInfo suppInfo;
    private Collection<? extends AccessCheck> accessChecks;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        suppInfo = context.getActionReport().getResultType(DeployCommandSupplementalInfo.class);
        accessChecks = suppInfo.getAccessChecks();
        return true;
    }

    @Override
    public Collection<? extends AccessCheck> getAccessChecks() {
        return accessChecks;
    }

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();
      try {
        logger.log(Level.INFO, "PostState starting: " + this.getClass().getName());

        final ExtendedDeploymentContext dc;
        if (suppInfo == null) {
            throw new IllegalStateException("Internal Error: suppInfo was not set. Insure that it is set properly.");
        } else {
            dc = suppInfo.deploymentContext();
        }

        if (dc == null) {
            return;
        }

        final InterceptorNotifier notifier = new InterceptorNotifier(habitat, dc);

        try {
            notifier.ensureAfterReported(ExtendedDeploymentContext.Phase.REPLICATION);
            logger.log(Level.INFO, "PostStateCommand: " + this.getClass().getName() + " finished successfully");
        } catch (Exception e) {
            report.failure(logger, e.getMessage());
            logger.log(Level.SEVERE, "Error during inner PostState: " + this.getClass().getName(), e);
        }
      } catch (Exception e) {
          logger.log(Level.SEVERE, "Error duirng outer PostState: " + this.getClass().getName(), e);
      }
    }
}
