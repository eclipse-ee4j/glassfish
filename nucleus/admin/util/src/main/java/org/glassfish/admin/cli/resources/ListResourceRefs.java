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

package org.glassfish.admin.cli.resources;

import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.RefContainer;
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AccessRequired.AccessCheck;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * List Resource Refs Command
 *
 */
@TargetType(value = { CommandTarget.CONFIG, CommandTarget.DAS, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE,
        CommandTarget.CLUSTERED_INSTANCE })
@ExecuteOn(value = { RuntimeType.DAS })
@Service(name = "list-resource-refs")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.resource.refs")
@RestEndpoints({
        @RestEndpoint(configBean = Resources.class, opType = RestEndpoint.OpType.GET, path = "list-resource-refs", description = "list-resource-refs") })
public class ListResourceRefs implements AdminCommand, AdminCommandSecurity.Preauthorization, AdminCommandSecurity.AccessCheckProvider {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListResourceRefs.class);

    @Param(optional = true, primary = true)
    private String target = SystemPropertyConstants.DAS_SERVER_NAME;

    @Inject
    private ConfigBeansUtilities configBeansUtilities;

    @Inject
    private Domain domain;

    @AccessRequired.To("read")
    private RefContainer refContainer;

    private List<ResourceRef> resourceRefs = null;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        refContainer = CLIUtil.chooseRefContainer(domain, target, configBeansUtilities);
        if (refContainer != null) {
            resourceRefs = refContainer.getResourceRef();
        }
        return true;
    }

    @Override
    public Collection<? extends AccessCheck> getAccessChecks() {
        final Collection<AccessCheck> accessChecks = new ArrayList<AccessCheck>();
        for (ResourceRef rr : resourceRefs) {
            accessChecks.add(new AccessCheck(rr, "read", true /* isFailureFatal */));
        }
        return accessChecks;
    }

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the parameter names and the
     * values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        try {
            if (resourceRefs != null) {
                processResourceRefs(report, resourceRefs);
                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            }
        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString("list.resource.refs.failed", "list-resource-refs failed"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }

    }

    private void processResourceRefs(ActionReport report, List<ResourceRef> resourceRefs) {
        if (resourceRefs.isEmpty()) {
            final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
            part.setMessage(localStrings.getLocalString("NothingToList", "Nothing to List."));
        } else {
            for (ResourceRef ref : resourceRefs) {
                final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                part.setMessage(ref.getRef());
            }
        }
    }
}
