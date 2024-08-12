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

package org.glassfish.jdbc.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.Properties;

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
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.config.serverbeans.ServerTags.DESCRIPTION;
import static org.glassfish.resources.admin.cli.ResourceConstants.ENABLED;
import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.POOL_NAME;

/**
 * Create JDBC Resource Command
 *
 */
@TargetType(value={CommandTarget.DAS,CommandTarget.DOMAIN, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE })
@RestEndpoints({
        @RestEndpoint(configBean=Resources.class,
                opType=RestEndpoint.OpType.POST,
                path="create-jdbc-resource",
                description="create-jdbc-resource")
})
@ExecuteOn(RuntimeType.ALL)
@Service(name="create-jdbc-resource")
@PerLookup
@I18n("create.jdbc.resource")
public class CreateJdbcResource implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateJdbcResource.class);

    @Param(name="connectionpoolid", alias="poolName")
    private String connectionPoolId;

    @Param(optional=true, defaultValue="true")
    private Boolean enabled;

    @Param(optional=true)
    private String description;

    @Param(name="property", optional=true, separator=':')
    private Properties properties;

    @Param(optional = true, defaultValue = CommandTarget.TARGET_SERVER)
    private String target;

    @Param(name="jndi_name", primary=true)
    private String jndiName;

    @Inject
    private Domain domain;

    @Inject
    private JDBCResourceManager jdbcMgr;

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
        attrList.put(JNDI_NAME, jndiName);
        attrList.put(POOL_NAME, connectionPoolId);
        attrList.put(DESCRIPTION, description);
        attrList.put(ENABLED, enabled.toString());
        ResourceStatus rs;

        try {
            rs = jdbcMgr.create(domain.getResources(), attrList, properties, target);
        } catch(Exception e) {
            report.setMessage(localStrings.getLocalString("create.jdbc.resource.failed",
                    "JDBC resource {0} creation failed", jndiName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        ActionReport.ExitCode ec = ActionReport.ExitCode.SUCCESS;
        if (rs.getMessage() != null){
             report.setMessage(rs.getMessage());
        }
        if (rs.getStatus() == ResourceStatus.FAILURE) {
            ec = ActionReport.ExitCode.FAILURE;
            if (rs.getException() != null) {
                report.setFailureCause(rs.getException());
            }
        }
        report.setActionExitCode(ec);
    }
}
