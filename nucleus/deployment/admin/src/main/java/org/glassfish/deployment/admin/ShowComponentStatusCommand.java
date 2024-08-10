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

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
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
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.deployment.versioning.VersioningException;
import org.glassfish.deployment.versioning.VersioningService;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.deployment.Deployment;
import org.jvnet.hk2.annotations.Service;

@Service(name="show-component-status")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn(value={RuntimeType.DAS})
@TargetType(value={CommandTarget.DOMAIN, CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CLUSTERED_INSTANCE})
@RestEndpoints({
    @RestEndpoint(configBean=Application.class,
        opType=RestEndpoint.OpType.GET,
        path="show-component-status",
        description="Show Component Status",
        params={
            @RestParam(name="id", value="$parent")
        })
})
public class ShowComponentStatusCommand implements AdminCommand, AdminCommandSecurity.Preauthorization,
        AdminCommandSecurity.AccessCheckProvider {

    @Param(primary=true)
    public String name = null;

    @Param(optional=true)
    String target = "server";

    @Inject
    Deployment deployment;

    @Inject
    Domain domain;

    @Inject
    Applications applications;

    @Inject
    VersioningService versioningService;

    private ActionReport report;
    private Logger logger;
    private List<String> matchedVersions = null;

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListAppRefsCommand.class);

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        report = context.getActionReport();
        logger = context.getLogger();

        // retrieve matched version(s) if exist

        try {
            matchedVersions = versioningService.getMatchedVersions(name, target);
        } catch (VersioningException e) {
            report.failure(logger, e.getMessage());
            return false;
        }

        // if matched list is empty and no VersioningException thrown,
        // this is an unversioned behavior and the given application is not registered
        if(matchedVersions.isEmpty()){
            report.setMessage(localStrings.getLocalString("ref.not.referenced.target","Application {0} is not referenced by target {1}", name, target));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        }

        return true;
    }

    @Override
    public Collection<? extends AccessCheck> getAccessChecks() {
        final List<AccessCheck> accessChecks = new ArrayList<AccessCheck>();
        for (String mv : matchedVersions) {
            if ( ! DeploymentUtils.isDomainTarget(target)) {
                final ApplicationRef ref = domain.getApplicationRefInTarget(mv, target);
                if (ref != null) {
                    accessChecks.add(new AccessCheck(AccessRequired.Util.resourceNameFromConfigBeanProxy(ref), "read"));
                }
            }
        }
        return accessChecks;
    }

    public void execute(AdminCommandContext context) {

        ActionReport.MessagePart part;
        if (report == null) {
            // We could handle this more elegantly by requiring that report be passed as an argument.
            throw new IllegalStateException("Internal Error: The report should have been initializes by the preAuthorization method");
        } else {
            part = report.getTopMessagePart();
        }

         // for each matched version
        Iterator it = matchedVersions.iterator();
        while(it.hasNext()){
            String appName = (String)it.next();
            String status = "disabled";

            if (!DeploymentUtils.isDomainTarget(target)) {
                ApplicationRef ref = domain.getApplicationRefInTarget(appName, target);
                if (ref == null) {
                    report.setMessage(localStrings.getLocalString("ref.not.referenced.target","Application {0} is not referenced by target {1}", appName, target));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
            if (domain.isAppEnabledInTarget(appName, target)) {
                status = "enabled";
            }

            ActionReport.MessagePart childPart = part.addChild();
            String message = localStrings.getLocalString("component.status","Status of {0} is {1}.", appName, status);
            childPart.setMessage(message);
            childPart.addProperty(DeploymentProperties.STATE, status);
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
