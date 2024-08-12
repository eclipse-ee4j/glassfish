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

import com.sun.enterprise.admin.util.ClusterOperationUtil;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired.AccessCheck;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.common.util.admin.ParameterMapExtractor;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.deployment.Deployment;
import org.jvnet.hk2.annotations.Service;

/**
 * Delete lifecycle modules.
 *
 */
@Service(name="delete-lifecycle-module")
@I18n("delete.lifecycle.module")
@PerLookup
@ExecuteOn(value={RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType(value={CommandTarget.DOMAIN, CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER})
@RestEndpoints({
    @RestEndpoint(configBean=Cluster.class,
        opType=RestEndpoint.OpType.DELETE,
        path="delete-lifecycle-module",
        description="Delete Lifecycle Module",
        params={
            @RestParam(name="target", value="$parent")
        }),
    @RestEndpoint(configBean=Server.class,
        opType=RestEndpoint.OpType.DELETE,
        path="delete-lifecycle-module",
        description="Delete Lifecycle Module",
        params={
            @RestParam(name="target", value="$parent")
        })
})
public class DeleteLifecycleModuleCommand implements AdminCommand, AdminCommandSecurity.AccessCheckProvider {

    @Param(primary=true)
    public String name = null;

    @Param(optional=true)
    public String target = "server";

    @Inject
    Deployment deployment;

    @Inject
    Domain domain;

    @Inject
    ServerEnvironment env;

    @Inject
    ServiceLocator habitat;

    private List<String> targets = null;

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteLifecycleModuleCommand.class);

    @Override
    public Collection<? extends AccessCheck> getAccessChecks() {
        final List<AccessCheck> accessChecks = new ArrayList<AccessCheck>();
        if (env.isDas() && DeploymentUtils.isDomainTarget(target)) {
            targets = domain.getAllReferencedTargetsForApplication(name);
            for (String t : targets) {
                final String resourceName = DeploymentCommandUtils.getTargetResourceNameForExistingAppRef(domain, t, name);
                if (resourceName != null) {
                    accessChecks.add(new AccessCheck(resourceName, "delete"));
                }
            }
        } else {
            final String resourceName = DeploymentCommandUtils.getTargetResourceNameForExistingAppRef(domain, target, name);
            if (resourceName != null) {
                accessChecks.add(new AccessCheck(resourceName, "delete"));
            }
        }
        return accessChecks;
    }

    public void execute(AdminCommandContext context) {

        ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();

        if (!deployment.isRegistered(name)) {
            report.setMessage(localStrings.getLocalString("lifecycle.notreg","Lifecycle module {0} not registered", name));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (!DeploymentUtils.isDomainTarget(target)) {
            ApplicationRef ref = domain.getApplicationRefInTarget(name, target);
            if (ref == null) {
                report.setMessage(localStrings.getLocalString("lifecycle.not.referenced.target","Lifecycle module {0} is not referenced by target {1}", name, target));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }

        deployment.validateUndeploymentTarget(target, name);

        if (env.isDas() && DeploymentUtils.isDomainTarget(target)) {
            // replicate command to all referenced targets
            try {
                ParameterMapExtractor extractor = new ParameterMapExtractor(this);
                ParameterMap paramMap = extractor.extract(Collections.EMPTY_LIST);
                paramMap.set("DEFAULT", name);

                ClusterOperationUtil.replicateCommand("delete-lifecycle-module", FailurePolicy.Error,
                        FailurePolicy.Ignore, FailurePolicy.Warn, targets, context, paramMap, habitat);
            } catch (Exception e) {
                report.failure(logger, e.getMessage());
                return;
            }
        }

        try {
            deployment.unregisterAppFromDomainXML(name, target);
        } catch(Exception e) {
            report.setMessage("Failed to delete lifecycle module: " + e);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
