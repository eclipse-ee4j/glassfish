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

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;

import java.util.List;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

@Service(name="_get-targets")
@ExecuteOn(value={RuntimeType.DAS})
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.GET,
        path="_get-targets",
        description="_get-targets")
})
@AccessRequired(resource={DeploymentCommandUtils.CLUSTERS_RESOURCE_NAME,DeploymentCommandUtils.SERVERS_RESOURCE_NAME}, action="read")
public class GetTargetsCommand implements AdminCommand {

    @Param(optional=true, primary=true)
    String appName = null;

    @Inject
    Domain domain;

    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        ActionReport.MessagePart part = report.getTopMessagePart();


        List<String> targets = null;

        if (appName == null || appName.equals("*")) {
            targets = domain.getAllTargets();
        } else {
            targets = domain.getAllReferencedTargetsForApplication(appName);
        }

        for (String target : targets) {
            ActionReport.MessagePart childPart = part.addChild();
            childPart.setMessage(target);
        }
    }
}
