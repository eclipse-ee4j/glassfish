/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resources.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.admin.cli.ResourceUtil;
import org.glassfish.resources.config.ExternalJndiResource;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Delete Jndi Resource object
 *
 */
@TargetType(value={CommandTarget.DAS,CommandTarget.DOMAIN, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE })
@RestEndpoints({
        @RestEndpoint(configBean = Resources.class,
                opType = RestEndpoint.OpType.DELETE,
                path = "delete-jndi-resource",
                description = "delete-jndi-resource")
})

@org.glassfish.api.admin.ExecuteOn(value={RuntimeType.ALL})
@Service(name="delete-jndi-resource")
@PerLookup
@I18n("delete.jndi.resource")
public class DeleteJndiResource implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(DeleteJndiResource.class);

    @Param(optional = true, defaultValue = CommandTarget.TARGET_SERVER)
    private String target;

    @Param(name="jndi_name", primary=true)
    private String jndiName;

    @Inject
    private Domain domain;

    @Inject
    private ServerEnvironment environment;

    @Inject
    private ResourceUtil resourceUtil;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();
        SimpleJndiName simpleJndiName = SimpleJndiName.of(jndiName);
        // ensure we already have this resource
        if (!doesResourceExist(domain.getResources(), simpleJndiName)) {
            report.setMessage(localStrings.getLocalString(
                    "delete.jndi.resource.notfound",
                    "A jndi resource named {0} does not exist.", jndiName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (environment.isDas()) {
            if (CommandTarget.TARGET_DOMAIN.equals(target)) {
                if (!resourceUtil.getTargetsReferringResourceRef(simpleJndiName).isEmpty()) {
                    report.setMessage(localStrings.getLocalString("delete.jndi.resource.resource-ref.exist",
                            "external-jndi-resource [ {0} ] is referenced in an " +
                                    "instance/cluster target, Use delete-resource-ref on appropriate target",
                            jndiName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            } else {
                if (!resourceUtil.isResourceRefInTarget(simpleJndiName, target)) {
                    report.setMessage(localStrings.getLocalString("delete.jndi.resource.no.resource-ref",
                            "external-jndi-resource [ {0} ] is not referenced in target [ {1} ]",
                            jndiName, target));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;

                }

                if (resourceUtil.getTargetsReferringResourceRef(simpleJndiName).size() > 1) {
                    report.setMessage(localStrings.getLocalString("delete.jndi.resource.multiple.resource-refs",
                            "external-jndi-resource [ {0} ] is referenced in multiple " +
                                    "instance/cluster targets, Use delete-resource-ref on appropriate target",
                            jndiName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
        }

        try {
            if (!CommandTarget.TARGET_DOMAIN.equals(target)) {
                resourceUtil.deleteResourceRef(simpleJndiName, target);
            }
            SingleConfigCode<Resources> configCode = param -> {
                ExternalJndiResource resource = domain.getResources().getResourceByName(ExternalJndiResource.class,
                    simpleJndiName);
                if (resource.getJndiName().equals(jndiName)) {
                    return param.getResources().remove(resource);
                }
                return null;
            };
            ConfigSupport.apply(configCode, domain.getResources());

            report.setMessage(
                localStrings.getLocalString("delete.jndi.resource.success", "Jndi resource {0} deleted.", jndiName));
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString("delete.jndi.resource.fail",
                "Unable to delete jndi resource {0}.", jndiName) + " " + tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
        }

    }

    private boolean doesResourceExist(Resources resources, SimpleJndiName jndiName) {
        return resources.getResourceByName(ExternalJndiResource.class, jndiName) != null;
    }
}
