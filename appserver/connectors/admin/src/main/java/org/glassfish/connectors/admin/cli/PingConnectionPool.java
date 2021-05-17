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

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.jdbc.config.JdbcConnectionPool;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.jvnet.hk2.annotations.Service;
import com.sun.enterprise.config.serverbeans.Module;

import jakarta.inject.Inject;

/**
 * Ping Connection Pool Command
 *
 */
@Service(name="ping-connection-pool")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn(value={RuntimeType.DAS})
@I18n("ping.connection.pool")
@RestEndpoints({
    @RestEndpoint(configBean=JdbcConnectionPool.class,
        opType=RestEndpoint.OpType.GET,
        path="ping",
        description="Ping",
        params={
            @RestParam(name="id", value="$parent")
        }),
    @RestEndpoint(configBean=Resources.class,
        opType=RestEndpoint.OpType.GET,
        path="ping-connection-pool",
        description="Ping")
})
public class PingConnectionPool implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(PingConnectionPool.class);

    @Param(name="pool_name", primary=true)
    private String poolName;

    @Inject
    private ConnectorRuntime connRuntime;

    @Inject
    private Domain domain;

    @Param(name="appname", optional=true)
    private String applicationName;

    @Param(name="modulename", optional=true)
    private String moduleName;

    @Inject
    private ConnectionPoolUtil poolUtil;

    @Inject
    private Applications applications;

    @Param(optional = true, alias = "targetName", obsolete = true)
    private String target = SystemPropertyConstants.DAS_SERVER_NAME;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the parameter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        boolean status = false;
        Resources resources = domain.getResources();
        String scope = "";
        if(moduleName != null){
            if(!poolUtil.isValidModule(applicationName, moduleName, poolName, report)){
                return ;
            }
            Application application = applications.getApplication(applicationName);
            Module module = application.getModule(moduleName);
            resources = module.getResources();
            scope = "java:module/";
        }else if(applicationName != null){
            if(!poolUtil.isValidApplication(applicationName, poolName, report)){
                return;
            }
            Application application = applications.getApplication(applicationName);
            resources = application.getResources();
            scope = "java:app/";
        }

        if(!poolUtil.isValidPool(resources, poolName, scope, report)){
            return;
        }

        PoolInfo poolInfo = new PoolInfo(poolName, applicationName, moduleName);
        try {
            status = connRuntime.pingConnectionPool(poolInfo);
            if (status) {
                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            } else {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(
                        localStrings.getLocalString("ping.connection.pool.fail",
                                "Ping Connection Pool for {0} Failed", poolInfo));
            }
        } catch (Exception e) {
            report.setMessage(
                    localStrings.getLocalString("ping.connection.pool.fail",
                            "Ping Connection Pool for {0} Failed", poolInfo));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }
}
