/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resources.api.ResourceAttributes;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.config.serverbeans.ServerTags.DESCRIPTION;
import static org.glassfish.api.ActionReport.ExitCode.FAILURE;
import static org.glassfish.api.ActionReport.ExitCode.SUCCESS;
import static org.glassfish.api.admin.RestEndpoint.OpType.POST;
import static org.glassfish.api.admin.RuntimeType.ALL;
import static org.glassfish.config.support.CommandTarget.CLUSTER;
import static org.glassfish.config.support.CommandTarget.DAS;
import static org.glassfish.config.support.CommandTarget.DOMAIN;
import static org.glassfish.config.support.CommandTarget.STANDALONE_INSTANCE;
import static org.glassfish.resources.admin.cli.ResourceConstants.ENABLED;
import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.POOL_NAME;

/**
 * Create JDBC Resource Command
 *
 */
@TargetType(value = { DAS, DOMAIN, CLUSTER, STANDALONE_INSTANCE })
@RestEndpoints({
    @RestEndpoint(
        configBean = Resources.class,
        opType = POST,
        path = "create-jdbc-resource",
        description = "create-jdbc-resource") })
@ExecuteOn(ALL)
@Service(name = "create-jdbc-resource")
@PerLookup
@I18n("create.jdbc.resource")
public class CreateJdbcResource implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateJdbcResource.class);

    @Param(name = "connectionpoolid", alias = "poolName")
    private String connectionPoolId;

    @Param(optional = true, defaultValue = "true")
    private Boolean enabled;

    @Param(optional = true)
    private String description;

    @Param(name = "property", optional = true, separator = ':')
    private Properties properties;

    @Param(optional = true, defaultValue = CommandTarget.TARGET_SERVER)
    private String target;

    @Param(optional = true)
    String sqlFileName;

    @Param(name = "jndi_name", primary = true)
    private String jndiName;

    @Inject
    private Domain domain;

    @Inject
    private JDBCResourceManager jdbcResourceManager;

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the paramter names and the
     * values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        ResourceAttributes attributes = new ResourceAttributes();
        attributes.set(JNDI_NAME, jndiName);
        attributes.set(POOL_NAME, connectionPoolId);
        attributes.set(DESCRIPTION, description);
        attributes.set(ENABLED, enabled.toString());
        ResourceStatus resourceStatus;

        try {
            resourceStatus = jdbcResourceManager.create(domain.getResources(), attributes, properties, target);

            if (sqlFileName != null && resourceStatus.getException() == null && resourceStatus.getStatus() != ResourceStatus.FAILURE) {
                ResourceStatus executeStatus = jdbcResourceManager.executeSql(jndiName, sqlFileName);

                resourceStatus =
                    new ResourceStatus(
                        executeStatus.getStatus(),
                        (resourceStatus.getMessage() == null? "" : resourceStatus.getMessage()) + " " + executeStatus.getMessage(),
                        resourceStatus.isAlreadyExists() || executeStatus.isAlreadyExists());
            }

        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString("create.jdbc.resource.failed", "JDBC resource {0} creation failed", jndiName));
            report.setActionExitCode(FAILURE);
            report.setFailureCause(e);
            return;
        }

        ExitCode exitCode = SUCCESS;
        if (resourceStatus.getMessage() != null) {
            report.setMessage(resourceStatus.getMessage());
        }

        if (resourceStatus.getStatus() == ResourceStatus.FAILURE) {
            exitCode = FAILURE;
            if (resourceStatus.getException() != null) {
                report.setFailureCause(resourceStatus.getException());
            }
        }

        report.setActionExitCode(exitCode);
    }
}
