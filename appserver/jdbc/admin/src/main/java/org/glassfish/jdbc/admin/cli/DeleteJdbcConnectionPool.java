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

package org.glassfish.jdbc.admin.cli;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Delete JDBC Connection Pool Command
 *
 */
@ExecuteOn(RuntimeType.ALL)
@Service(name="delete-jdbc-connection-pool")
@PerLookup
@I18n("delete.jdbc.connection.pool")
public class DeleteJdbcConnectionPool implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteJdbcConnectionPool.class);

    @Param(optional=true, defaultValue="false")
    private Boolean cascade;

    @Param(name="jdbc_connection_pool_id", primary=true)
    private String poolName;

    @Param(optional=true, obsolete = true)
    private String target = SystemPropertyConstants.DAS_SERVER_NAME;

    @Inject
    private Domain domain;

    @Inject
    private IterableProvider<Server> servers;

    @Inject
    private IterableProvider<Cluster> clusters;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        try {
            JDBCConnectionPoolManager jdbcConnMgr = new JDBCConnectionPoolManager();
            ResourceStatus rs = jdbcConnMgr.delete(servers, clusters, domain.getResources(), cascade.toString(), poolName);
            if (rs.getMessage() != null) report.setMessage(rs.getMessage());
            if (rs.getStatus() == ResourceStatus.SUCCESS) {
                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            } else {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                if (rs.getException()!= null) {
                    report.setFailureCause(rs.getException());
                    Logger.getLogger(DeleteJdbcConnectionPool.class.getName()).log(Level.SEVERE,
                            "Something went wrong in delete-jdbc-connection-pool", rs.getException());
                }
            }
        } catch(Exception e) {
            Logger.getLogger(DeleteJdbcConnectionPool.class.getName()).log(Level.SEVERE,
                    "Something went wrong in delete-jdbc-connection-pool", e);
            String msg = e.getMessage() != null ? e.getMessage() :
                localStrings.getLocalString("delete.jdbc.connection.pool.fail",
                    "{0} delete failed ", poolName);
            report.setMessage(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }
}
