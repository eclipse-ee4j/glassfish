/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.connectors.config.AdminObjectResource;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.util.BindableResourcesHelper;
import org.jvnet.hk2.annotations.Service;

/**
 * List Admin Objects command
 *
 */
@TargetType(value={CommandTarget.DAS,CommandTarget.CONFIG, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTERED_INSTANCE })
@ExecuteOn(value={RuntimeType.DAS})
@Service(name="list-admin-objects")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.admin.objects")
@RestEndpoints({
    @RestEndpoint(configBean=Resources.class,
        opType=RestEndpoint.OpType.GET,
        path="list-admin-objects",
        description="list-admin-objects")
})
public class ListAdminObjects implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListAdminObjects.class);

    @Param(primary = true, optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    private String target ;

    @Inject
    private BindableResourcesHelper bindableResourcesHelper;

    @Inject
    private Domain domain;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        //TODO 3.1 - support for cluster-config ?
        try {
            ArrayList<String> list = new ArrayList<String>();
            Collection<AdminObjectResource> adminObjects =
                    domain.getResources().getResources(AdminObjectResource.class);
            for (AdminObjectResource r : adminObjects) {
                if(bindableResourcesHelper.resourceExists(r.getJndiName(), target)){
                    list.add(r.getJndiName());
                }
            }
            for (String jndiName : list) {
                final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                part.setMessage(jndiName);
            }
        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString("list.admin.object.fail",
                    "Unable to list administered objects") + " " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
