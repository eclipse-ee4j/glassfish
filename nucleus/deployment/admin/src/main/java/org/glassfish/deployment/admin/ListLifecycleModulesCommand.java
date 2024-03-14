/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.TargetType;
import org.glassfish.config.support.CommandTarget;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Inject;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.hk2.api.PerLookup;


/**
 * List lifecycle modules.
 *
 */
@Service(name="list-lifecycle-modules")
@I18n("list.lifecycle.modules")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn(value={RuntimeType.DAS})
@TargetType(value={CommandTarget.DOMAIN, CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER})
@RestEndpoints({
    @RestEndpoint(configBean=Cluster.class,
        opType=RestEndpoint.OpType.GET,
        path="list-lifecycle-modules",
        description="List Lifecycle Modules",
        params={
            @RestParam(name="target", value="$parent")
        }),
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.GET,
        path="list-lifecycle-modules",
        description="List Lifecycle Modules"),
    @RestEndpoint(configBean=Server.class,
        opType=RestEndpoint.OpType.GET,
        path="list-lifecycle-modules",
        description="List Lifecycle Modules",
        params={
            @RestParam(name="target", value="$parent")
        })
})
@AccessRequired(resource=DeploymentCommandUtils.APPLICATION_RESOURCE_NAME, action="read")
public class ListLifecycleModulesCommand implements AdminCommand {

    @Param(primary=true, optional=true)
    public String target = "server";

    @Param(optional=true, defaultValue="false", shortName="t")
    public Boolean terse = false;

    @Inject
    Domain domain;

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListLifecycleModulesCommand.class);

    public void execute(AdminCommandContext context) {

        ActionReport report = context.getActionReport();
        ActionReport.MessagePart part = report.getTopMessagePart();

        boolean found = false;
        for (Application app : domain.getApplicationsInTarget(target)) {
            if (app.isLifecycleModule()) {
                ActionReport.MessagePart childPart = part.addChild();
                childPart.setMessage(app.getName());
                found = true;
            }
        }

        if (!found && !terse) {
            part.setMessage(localStrings.getLocalString("list.components.no.elements.to.list", "Nothing to List."));
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
