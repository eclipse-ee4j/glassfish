/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.resources.mail.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resources.mail.config.MailResource;
import org.glassfish.resourcebase.resources.util.ResourceUtil;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import jakarta.inject.Inject;
import java.beans.PropertyVetoException;

/**
 * Delete Mail Resource object
 *
 */

@TargetType(value={CommandTarget.DAS,CommandTarget.DOMAIN, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE })
@RestEndpoints({
        @RestEndpoint(configBean = Resources.class,
                opType = RestEndpoint.OpType.DELETE,
                path = "delete-mail-resource",
                description = "delete-mail-resource")
})

@org.glassfish.api.admin.ExecuteOn(value={RuntimeType.ALL})
@Service(name = "delete-mail-resource")
@PerLookup
@I18n("delete.mail.resource")
public class DeleteMailResource implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(DeleteMailResource.class);

    @Param(optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    private String target;

    @Param(name = "jndi_name", primary = true)
    private String jndiName;

    @Inject
    private Domain domain;

    @Inject
    private ServerEnvironment environment;

    @Inject
    private org.glassfish.resourcebase.resources.admin.cli.ResourceUtil resourceUtil;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        // ensure we already have this resource
        if (!isResourceExists(domain.getResources(), jndiName)) {
            report.setMessage(localStrings.getLocalString(
                    "delete.mail.resource.notfound",
                    "A Mail resource named {0} does not exist.", jndiName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (environment.isDas()) {

            if ("domain".equals(target)) {
                if (resourceUtil.getTargetsReferringResourceRef(jndiName).size() > 0) {
                    report.setMessage(localStrings.getLocalString("delete.mail.resource.resource-ref.exist",
                            "mail-resource [ {0} ] is referenced in an " +
                                    "instance/cluster target, Use delete-resource-ref on appropriate target",
                            jndiName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            } else {
                if (!resourceUtil.isResourceRefInTarget(jndiName, target)) {
                    report.setMessage(localStrings.getLocalString("delete.mail.resource.no.resource-ref",
                            "mail-resource [ {0} ] is not referenced in target [ {1} ]",
                            jndiName, target));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;

                }

                if (resourceUtil.getTargetsReferringResourceRef(jndiName).size() > 1) {
                    report.setMessage(localStrings.getLocalString("delete.mail.resource.multiple.resource-refs",
                            "mail-resource [ {0} ] is referenced in multiple " +
                                    "instance/cluster targets, Use delete-resource-ref on appropriate target",
                            jndiName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
        }

        try {
            // delete resource-ref
            resourceUtil.deleteResourceRef(jndiName, target);

            // delete java-mail-resource
            ConfigSupport.apply(new SingleConfigCode<Resources>() {
                public Object run(Resources param) throws PropertyVetoException,
                        TransactionFailure {
                    MailResource resource = (MailResource)
                            ResourceUtil.getBindableResourceByName(domain.getResources(), jndiName);
                    return param.getResources().remove(resource);
                }
            }, domain.getResources());

            report.setMessage(localStrings.getLocalString(
                    "delete.mail.resource.success",
                    "Mail resource {0} deleted", jndiName));
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString(
                    "delete.mail.resource.failed",
                    "Unable to delete mail resource {0}", jndiName) + " " +
                    tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
        }
    }

    private boolean isResourceExists(Resources resources, String jndiName) {
        return ResourceUtil.getBindableResourceByName(resources, jndiName) != null;
    }
}
