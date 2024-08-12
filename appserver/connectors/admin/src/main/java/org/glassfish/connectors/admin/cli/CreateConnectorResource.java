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

package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resources.admin.cli.ResourceConstants;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.connectors.admin.cli.CLIConstants.CR.CR_JNDI_NAME;
import static org.glassfish.connectors.admin.cli.CLIConstants.CR.CR_OBJECT_TYPE;
import static org.glassfish.connectors.admin.cli.CLIConstants.CR.CR_POOL_NAME;
import static org.glassfish.connectors.admin.cli.CLIConstants.DESCRIPTION;
import static org.glassfish.connectors.admin.cli.CLIConstants.PROPERTY;
import static org.glassfish.connectors.admin.cli.CLIConstants.TARGET;
import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.POOL_NAME;


/**
 * Create Connector Resource Command
 *
 * @author Jennifer Chou, Jagadish Ramu
 *
 */
@TargetType(value={CommandTarget.DAS,CommandTarget.DOMAIN, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE })
@RestEndpoints({
        @RestEndpoint(configBean=Resources.class,
                opType=RestEndpoint.OpType.POST,
                path="create-connector-resource",
                description="create-connector-resource")
})
@ExecuteOn(RuntimeType.ALL)
@Service(name="create-connector-resource")
@PerLookup
@I18n("create.connector.resource")
public class CreateConnectorResource implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateConnectorResource.class);

    @Param(name=CR_POOL_NAME)
    private String poolName;

    @Param(name=CLIConstants.ENABLED, optional=true, defaultValue="true")
    private Boolean enabled;

    @Param(name=DESCRIPTION, optional=true)
    private String description;

    @Param(name=CR_OBJECT_TYPE, defaultValue="user", optional=true)
    private String objectType;

    @Param(name=PROPERTY, optional=true, separator=':')
    private Properties properties;

    @Param(name = TARGET, optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    private String target;

    @Param(name=CR_JNDI_NAME, primary=true)
    private String jndiName;

    @Inject
    private Domain domain;

    @Inject
    private ConnectorResourceManager connResMgr;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        HashMap<String, String> attrList = new HashMap<>();
        attrList.put(POOL_NAME, poolName);
        attrList.put(ResourceConstants.ENABLED, enabled.toString());
        attrList.put(JNDI_NAME, jndiName);
        attrList.put(ServerTags.DESCRIPTION, description);
        attrList.put(ServerTags.OBJECT_TYPE, objectType);

        ResourceStatus rs;

        try {
            rs = connResMgr.create(domain.getResources(), attrList, properties, target);
        } catch(Exception e) {
            Logger.getLogger(CreateConnectorResource.class.getName()).log(Level.SEVERE,
                    "Unable to create connector resource " + jndiName, e);
            String def = "Connector resource: {0} could not be created, reason: {1}";
            report.setMessage(localStrings.getLocalString("create.connector.resource.fail",
                    def, jndiName) + " " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        ActionReport.ExitCode ec = ActionReport.ExitCode.SUCCESS;
        if(rs.getMessage() != null){
            report.setMessage(rs.getMessage());
        }
        if (rs.getStatus() == ResourceStatus.FAILURE) {
            ec = ActionReport.ExitCode.FAILURE;
            if (rs.getMessage() == null) {
                 report.setMessage(localStrings.getLocalString("create.connector.resource.fail",
                    "Connector resource {0} creation failed", jndiName, ""));
            }
            if (rs.getException() != null) {
                report.setFailureCause(rs.getException());
            }
        }
        report.setActionExitCode(ec);
    }
}
