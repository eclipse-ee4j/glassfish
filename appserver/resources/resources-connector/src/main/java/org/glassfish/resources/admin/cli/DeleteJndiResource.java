/*
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
import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resources.config.ExternalJndiResource;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.glassfish.resourcebase.resources.admin.cli.ResourceUtil;

import javax.inject.Inject;
import java.beans.PropertyVetoException;

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

    @Param(optional=true, defaultValue= SystemPropertyConstants.DAS_SERVER_NAME)
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
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        // ensure we already have this resource
        if (!doesResourceExist(domain.getResources(), jndiName)) {
            report.setMessage(localStrings.getLocalString(
                    "delete.jndi.resource.notfound",
                    "A jndi resource named {0} does not exist.", jndiName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (environment.isDas()) {
            if ("domain".equals(target)) {
                if (resourceUtil.getTargetsReferringResourceRef(jndiName).size() > 0) {
                    report.setMessage(localStrings.getLocalString("delete.jndi.resource.resource-ref.exist",
                            "external-jndi-resource [ {0} ] is referenced in an" +
                                    "instance/cluster target, Use delete-resource-ref on appropriate target",
                            jndiName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            } else {
                if (!resourceUtil.isResourceRefInTarget(jndiName, target)) {
                    report.setMessage(localStrings.getLocalString("delete.jndi.resource.no.resource-ref",
                            "external-jndi-resource [ {0} ] is not referenced in target [ {1} ]",
                            jndiName, target));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;

                }

                if (resourceUtil.getTargetsReferringResourceRef(jndiName).size() > 1) {
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

            // delete resource-ref
            resourceUtil.deleteResourceRef(jndiName, target);

            // delete external-jndi-resource
            ConfigSupport.apply(new SingleConfigCode<Resources>() {

                public Object run(Resources param) throws PropertyVetoException,
                        TransactionFailure {
                    ExternalJndiResource resource = (ExternalJndiResource)
                            domain.getResources().getResourceByName(ExternalJndiResource.class, jndiName);
                    if (resource.getJndiName().equals(jndiName)) {
                        return param.getResources().remove(resource);
                    }
                    return null;
                }
            }, domain.getResources());

            report.setMessage(localStrings.getLocalString("" +
                    "delete.jndi.resource.success",
                    "Jndi resource {0} deleted.", jndiName));
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString("" +
                    "delete.jndi.resource.fail",
                    "Unable to delete jndi resource {0}.", jndiName) + " "
                    + tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
        }

    }

    private boolean doesResourceExist(Resources resources, String jndiName) {
        return resources.getResourceByName(ExternalJndiResource.class, jndiName) != null;
    }
}
