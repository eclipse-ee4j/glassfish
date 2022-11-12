/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.jms.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.connectors.config.ConnectorResource;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * Delete JMS Resource Command
 *
 */
@Service(name = "delete-jms-resource")
@PerLookup
@I18n("delete.jms.resource")
@ExecuteOn({RuntimeType.DAS})
@TargetType({CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.DOMAIN})
@RestEndpoints({
    @RestEndpoint(configBean = Resources.class,
    opType = RestEndpoint.OpType.DELETE,
    path = "delete-jms-resource",
    description = "delete-jms-resource")
})
public class DeleteJMSResource implements AdminCommand {

    private static final Logger LOG = System.getLogger(DeleteJMSResource.class.getName());
    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(DeleteJMSResource.class);

    @Param(optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    private String target;
    @Param(name = "jndi_name", primary = true)
    private String jndiName;
    @Param(optional = true, defaultValue = "false")
    private Boolean cascade;
    @Inject
    private CommandRunner commandRunner;
    @Inject
    private Domain domain;
    @Inject
    private ServiceLocator habitat;

    private static final String JNDINAME_APPENDER = "-Connection-Pool";
    /* As per new requirement all resources should have unique name so appending 'JNDINAME_APPENDER' to jndiName
     for creating  jndiNameForConnectionPool.
     */
    private String jndiNameForConnectionPool;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the parameter names and the values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        LOG.log(Level.DEBUG, "execute(context={0}); jndiName={1}, target={2}", context, jndiName, target);
        final ActionReport report = context.getActionReport();

        if (jndiName == null) {
            report.setMessage(I18N.getLocalString("delete.jms.resource.noJndiName",
                    "No JNDI name defined for JMS Resource."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        jndiNameForConnectionPool = jndiName + JNDINAME_APPENDER;

        ActionReport subReport = report.addSubActionsReport();

        ConnectorResource cresource = null;
        Resource res = domain.getResources().getResourceByName(ConnectorResource.class, SimpleJndiName.of(jndiName));
        if (res instanceof ConnectorResource) {
            cresource = (ConnectorResource) res;
        }
        if (cresource == null) {
            ParameterMap params = new ParameterMap();
            params.set("jndi_name", jndiName);
            params.set("DEFAULT", jndiName);
            params.set("target", target);
            commandRunner.getCommandInvocation("delete-admin-object", subReport, context.getSubject()).parameters(params).execute();

            if (ActionReport.ExitCode.FAILURE.equals(subReport.getActionExitCode())) {
                report.setMessage(I18N.getLocalString("delete.jms.resource.cannotDeleteJMSAdminObject",
                        "Unable to Delete Admin Object."));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        } else {
            if (!cascade) {
                Collection<ConnectorResource> connectorResources = domain.getResources().getResources(ConnectorResource.class);
                String connPoolName = jndiName + JNDINAME_APPENDER;
                int count = 0;
                for (ConnectorResource resource : connectorResources) {
                    if (connPoolName.equals(resource.getPoolName())) {
                        count ++;
                        if (count > 1) {
                            break;
                        }
                    }
                }
                if (count > 1) {
                    report.setMessage(I18N.getLocalString("found.more.connector.resources",
                            "Some connector resources are referencing connection pool {0}. Use 'cascade' option to delete them", connPoolName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }

            ActionReport listReport = habitat.getService(ActionReport.class);
            ParameterMap listParams = new ParameterMap();
            listParams.set("target", target);
            commandRunner.getCommandInvocation("list-jms-resources", listReport, context.getSubject()).parameters(listParams).execute();
            if (ActionReport.ExitCode.FAILURE.equals(listReport.getActionExitCode())) {
                report.setMessage(I18N.getLocalString("list.jms.resources.fail",
                        "Unable to list JMS Resources"));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
            Properties extraProps = listReport.getExtraProperties();
            if (extraProps != null && extraProps.size() > 0) {
                boolean resourceExist = false;
                for (int i=0; i<extraProps.size(); i++) {
                    List<Map<String, String>> nameList = (List) extraProps.get("jmsResources");
                    for (Map<String,String> m : nameList) {
                        String jndi = m.get("name");
                        if (jndiName.equals(jndi)) {
                            resourceExist = true;
                            break;
                        }
                    }
                    if (resourceExist) {
                        break;
                    }
                }
                if (!resourceExist) {
                    report.setMessage(I18N.getLocalString("jms.resources.not.found",
                        "JMS Resource {0} not found", jndiName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }

            // Delete the connector resource and connector connection pool
            String defPoolName = jndiNameForConnectionPool;
            String poolName = cresource.getPoolName();
            if (poolName != null && poolName.equals(defPoolName)) {
                ParameterMap params = new ParameterMap();
                params.set("DEFAULT", jndiName);
                params.set("connector_resource_name", jndiName);
                params.set("target", target);
                commandRunner.getCommandInvocation("delete-connector-resource", subReport, context.getSubject()).parameters(params).execute();

                if (ActionReport.ExitCode.FAILURE.equals(subReport.getActionExitCode())) {
                    report.setMessage(I18N.getLocalString("delete.jms.resource.cannotDeleteJMSResource",
                            "Unable to Delete Connector Resource."));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }


                params = new ParameterMap();
                params.set("poolname", jndiName);
                params.set("cascade", cascade.toString());
                params.set("DEFAULT", jndiNameForConnectionPool);
                commandRunner.getCommandInvocation("delete-connector-connection-pool", subReport, context.getSubject()).parameters(params).execute();

                if (ActionReport.ExitCode.FAILURE.equals(subReport.getActionExitCode())) {
                    report.setMessage(I18N.getLocalString("delete.jms.resource.cannotDeleteJMSPool",
                            "Unable to Delete Connector Connection Pool."));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
                //clear the message set by the delete-connector-connection-pool command this is to prevent the 'connection pool deleted' message from displaying
                subReport.setMessage("");

            } else {
                // There is no connector pool with the default poolName.
                // However, no need to throw exception as the connector
                // resource might still be there. Try to delete the
                // connector-resource without touching the ref. as
                // ref. might have been deleted while deleting connector-connection-pool
                // as the ref. is the same.

                ParameterMap params = new ParameterMap();
                params.set("DEFAULT", jndiName);
                params.set("connector_resource_name", jndiName);
                params.set("target", target);
                commandRunner.getCommandInvocation("delete-connector-resource", subReport, context.getSubject()).parameters(params).execute();

                if (ActionReport.ExitCode.FAILURE.equals(subReport.getActionExitCode())) {
                    report.setMessage(I18N.getLocalString("delete.jms.resource.cannotDeleteJMSResource",
                            "Unable to Delete Connector Resource."));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }

            }
        }

        ActionReport.ExitCode ec = ActionReport.ExitCode.SUCCESS;
        report.setActionExitCode(ec);
    }
}
