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

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.connectors.config.ConnectorResource;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.admin.cli.ResourceUtil;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import jakarta.inject.Inject;
import java.beans.PropertyVetoException;

/**
 * Delete Connector Resource command
 *
 * @author Jennifer Chou, Jagadish Ramu
 *
 */
@TargetType(value={CommandTarget.DAS,CommandTarget.DOMAIN, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE })
@RestEndpoints({
        @RestEndpoint(configBean = Resources.class,
                opType = RestEndpoint.OpType.DELETE,
                path = "delete-connector-resource",
                description = "delete-connector-resource")
})

@ExecuteOn(value={RuntimeType.ALL})
@Service(name="delete-connector-resource")
@PerLookup
@I18n("delete.connector.resource")
public class DeleteConnectorResource implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteConnectorResource.class);

    @Param(optional=true)
    private String target = SystemPropertyConstants.DAS_SERVER_NAME;

    @Param(name="connector_resource_name", primary=true)
    private String jndiName;

    @Inject
    private ResourceUtil resourceUtil;

    @Inject
    private Domain domain;

    @Inject
    private ServerEnvironment environment;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the parameter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        if (jndiName == null) {
            report.setMessage(localStrings.getLocalString("delete.connector.resource.noJndiName",
                            "No JNDI name defined for connector resource."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        // ensure we already have this resource
        Resource r = ConnectorsUtil.getResourceByName(domain.getResources(), ConnectorResource.class, jndiName);
        if (r == null) {
            report.setMessage(localStrings.getLocalString("delete.connector.resource.notfound",
                    "A connector resource named {0} does not exist.", jndiName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        if ("system-all-req".equals(r.getObjectType())) {
            report.setMessage(localStrings.getLocalString("delete.connector.resource.notAllowed",
                    "The {0} resource cannot be deleted as it is required to be configured in the system.",
                    jndiName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (environment.isDas()) {

            if ("domain".equals(target)) {
                if (resourceUtil.getTargetsReferringResourceRef(jndiName).size() > 0) {
                    report.setMessage(localStrings.getLocalString("delete.connector.resource.resource-ref.exist",
                            "connector-resource [ {0} ] is referenced in an" +
                                    "instance/cluster target, Use delete-resource-ref on appropriate target",
                            jndiName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            } else {
                if (!resourceUtil.isResourceRefInTarget(jndiName, target)) {
                    report.setMessage(localStrings.getLocalString("delete.connector.resource.no.resource-ref",
                            "connector-resource [ {0} ] is not referenced in target [ {1} ]",
                            jndiName, target));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;

                }

                if (resourceUtil.getTargetsReferringResourceRef(jndiName).size() > 1) {
                    report.setMessage(localStrings.getLocalString("delete.connector.resource.multiple.resource-refs",
                            "connector resource [ {0} ] is referenced in multiple " +
                                    "instance/cluster targets, Use delete-resource-ref on appropriate target",
                            jndiName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
        }

        try {
            //delete resource-ref
            resourceUtil.deleteResourceRef(jndiName, target);

            // delete connector-resource
            if (ConfigSupport.apply(new SingleConfigCode<Resources>() {
                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                    ConnectorResource resource = (ConnectorResource)
                            ConnectorsUtil.getResourceByName(domain.getResources(), ConnectorResource.class, jndiName );
                    return param.getResources().remove(resource);
                }
            }, domain.getResources()) == null) {
                report.setMessage(localStrings.getLocalString("delete.connector.resource.fail",
                                "Connector resource {0} delete failed ", jndiName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        } catch(TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString("delete.connector.resource.fail",
                            "Connector resource {0} delete failed ", jndiName)
                            + " " + tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
        }

        report.setMessage(localStrings.getLocalString("delete.connector.resource.success",
                "Connector resource {0} deleted successfully", jndiName));
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
