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

package org.glassfish.connectors.admin.cli;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.jdbc.config.JdbcConnectionPool;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;

@Service(name = "flush-connection-pool")
@PerLookup
@I18n("flush.connection.pool")
@RestEndpoints({
    @RestEndpoint(configBean=Resources.class,
        opType=RestEndpoint.OpType.POST, 
        path="flush-connection-pool", 
        description="flush-connection-pool")
})
public class FlushConnectionPool implements AdminCommand {
    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(FlushConnectionPool.class);

    @Param(name = "pool_name", primary = true)
    private String poolName;

    @Inject
    private Domain domain;

    @Param(name="appname", optional=true)
    private String applicationName;

    @Param(name="modulename", optional=true)
    private String moduleName;

    @Inject
    private Applications applications;

    @Inject
    private ConnectionPoolUtil poolUtil;

    @Inject
    private ConnectorRuntime _runtime;

    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        Resources resources = domain.getResources();
        String scope = "";
        if(moduleName != null){
            if(!poolUtil.isValidModule(applicationName, moduleName, poolName, report)){
                return ;
            }
            Application application = applications.getApplication(applicationName);
            Module module = application.getModule(moduleName);
            resources = module.getResources();
            scope = ConnectorConstants.JAVA_MODULE_SCOPE_PREFIX;
        }else if(applicationName != null){
            if(!poolUtil.isValidApplication(applicationName, poolName, report)){
                return;
            }
            Application application = applications.getApplication(applicationName);
            resources = application.getResources();
            scope = ConnectorConstants.JAVA_APP_SCOPE_PREFIX;
        }

        if(!poolUtil.isValidPool(resources, poolName, scope, report)){
            return;
        }

        boolean poolingEnabled = false;
        ResourcePool pool =
                (ResourcePool) ConnectorsUtil.getResourceByName(resources, ResourcePool.class, poolName);
        if(pool instanceof ConnectorConnectionPool){
            ConnectorConnectionPool ccp = (ConnectorConnectionPool)pool;
            poolingEnabled = Boolean.valueOf(ccp.getPooling());
        }else{
            JdbcConnectionPool ccp = (JdbcConnectionPool)pool;
            poolingEnabled = Boolean.valueOf(ccp.getPooling());
        }

        if(!poolingEnabled){
            String i18nMsg = localStrings.getLocalString("flush.connection.pool.pooling.disabled",
                    "Attempt to Flush Connection Pool failed because Pooling is disabled for pool : {0}", poolName);
            report.setMessage(i18nMsg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            PoolInfo poolInfo = new PoolInfo(poolName, applicationName, moduleName);
            _runtime.flushConnectionPool(poolInfo);
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (ConnectorRuntimeException e) {
            report.setMessage(localStrings.getLocalString("flush.connection.pool.fail",
                    "Flush connection pool for {0} failed", poolName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }
}
