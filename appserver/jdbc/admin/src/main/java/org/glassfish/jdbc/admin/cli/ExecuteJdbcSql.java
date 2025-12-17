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

import com.sun.enterprise.config.serverbeans.Resources;

import jakarta.inject.Inject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
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
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.api.ActionReport.ExitCode.FAILURE;
import static org.glassfish.api.ActionReport.ExitCode.SUCCESS;
import static org.glassfish.api.admin.RestEndpoint.OpType.POST;
import static org.glassfish.api.admin.RuntimeType.ALL;
import static org.glassfish.config.support.CommandTarget.CLUSTER;
import static org.glassfish.config.support.CommandTarget.DAS;
import static org.glassfish.config.support.CommandTarget.DOMAIN;
import static org.glassfish.config.support.CommandTarget.STANDALONE_INSTANCE;

/**
 * Execute SQL against JDBC Resource Command
 *
 */
@TargetType(value = { DAS, DOMAIN, CLUSTER, STANDALONE_INSTANCE })
@RestEndpoints({
    @RestEndpoint(
        configBean = Resources.class,
        opType = POST,
        path = "execute-jdbc-sql",
        description = "execute-jdbc-sql") })
@ExecuteOn(ALL)
@Service(name = "execute-jdbc-sql")
@PerLookup
public class ExecuteJdbcSql implements AdminCommand {

    @Param(optional = true, defaultValue = CommandTarget.TARGET_SERVER)
    private String target;

    @Param
    String sqlFileName;

    @Param(name = "jndi_name", primary = true)
    private String jndiName;

    @Inject
    private JDBCResourceManager jdbcResourceManager;

    /**
     * Executes the command with the command parameters
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        final ResourceStatus resourceStatus;
        try {
            resourceStatus = jdbcResourceManager.executeSql(jndiName, sqlFileName);
        } catch (Exception e) {
            report.setMessage("Failed to execute SQL against datasource " + jndiName);
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
