/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

@Service(name="_mt-deploy")
@org.glassfish.api.admin.ExecuteOn(value={RuntimeType.DAS})
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@AccessRequired(resource=DeploymentCommandUtils.APPLICATION_RESOURCE_NAME, action="create")
public class MTDeployCommand implements AdminCommand {

    @Param(primary=true)
    public File path;

    @Inject
    CommandRunner commandRunner;

    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        // invoke the deploy command with domain target to submit the
        // application to domain

        CommandInvocation inv = commandRunner.getCommandInvocation("deploy", report, context.getSubject());

        final ParameterMap parameters = new ParameterMap();

        parameters.set("DEFAULT", path.getAbsolutePath());

        parameters.set(DeploymentProperties.TARGET, DeploymentUtils.DOMAIN_TARGET_NAME);
        inv.parameters(parameters).execute();

        // do any necessary initialization work here
    }
}
