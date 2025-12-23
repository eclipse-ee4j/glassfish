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

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.jvnet.hk2.annotations.Service;

/**
 * Delete JDBC Resource command
 */
// FIXME: should be removed here, am I right or wrong?
@TargetType(value={CommandTarget.DAS,CommandTarget.DOMAIN, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE })
@RestEndpoints({
        @RestEndpoint(configBean = Resources.class,
                opType = RestEndpoint.OpType.DELETE,
                path = "delete-jdbc-resource",
                description = "delete-jdbc-resource")
})

@ExecuteOn(RuntimeType.ALL)
@Service(name="delete-jdbc-resource")
@PerLookup
@I18n("delete.jdbc.resource")
public class DeleteJdbcResource implements AdminCommand {

    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteJdbcResource.class);

    @Param(name = "jdbc_resource_name", primary = true)
    private String jndiName;

    @Param(optional = true, defaultValue = CommandTarget.TARGET_SERVER)
    private String target;

    @Inject
    private Domain domain;

    @Inject
    private JDBCResourceManager jdbcResMgr;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();
        try {
            ResourceStatus rs = jdbcResMgr.delete(domain.getResources(), SimpleJndiName.of(jndiName), target);
            if(rs.getMessage() != null){
                report.setMessage(rs.getMessage());
            }
            if (rs.getStatus() == ResourceStatus.SUCCESS) {
                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            } else {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                if (rs.getException() != null) {
                    report.setFailureCause(rs.getException());
                }
            }
        } catch(Exception e) {
            report.setMessage(localStrings.getLocalString("delete.jdbc.resource.fail", "{0} delete failed ", jndiName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }
}
