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

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.naming.DefaultResourceProxy;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jdbc.config.JdbcResource;
import org.glassfish.resourcebase.resources.util.BindableResourcesHelper;
import org.jvnet.hk2.annotations.Service;

/**
 * List JDBC Resources command
 *
 */
@TargetType(value={CommandTarget.DAS,CommandTarget.DOMAIN, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTERED_INSTANCE })
@ExecuteOn(value={RuntimeType.DAS})
@Service(name="list-jdbc-resources")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.jdbc.resources")
@RestEndpoints({
    @RestEndpoint(configBean=Resources.class,
        opType=RestEndpoint.OpType.GET,
        path="list-jdbc-resources",
        description="List JDBC Resources")
})
public class ListJdbcResources implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListJdbcResources.class);

    @Param(primary = true, optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME, alias = "targetName")
    private String target ;

    @Inject
    private Domain domain;

    @Inject
    private BindableResourcesHelper bindableResourcesHelper;

    @Inject
    private JDBCResourceManager jdbcMgr;

    @Inject
    private ServiceLocator habitat;


    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the parameter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        try {
            Collection<JdbcResource> jdbcResources = domain.getResources().getResources(JdbcResource.class);
            List<Map<String,String>> resourcesList = new ArrayList<Map<String, String>>();

            List<DefaultResourceProxy> drps = habitat.getAllServices(DefaultResourceProxy.class);
            for (JdbcResource jdbcResource : jdbcResources) {
                String jndiName = jdbcResource.getJndiName();
                if(bindableResourcesHelper.resourceExists(jndiName, target)){
                    ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                    part.setMessage(jndiName);
                    Map<String,String> resourceNameMap = new HashMap<String,String>();
                    String logicalName = DefaultResourceProxy.Util.getLogicalName(drps, jndiName);
                    if (logicalName != null) {
                        resourceNameMap.put("logical-jndi-name", logicalName);
                    }
                    resourceNameMap.put("name", jndiName);
                    resourcesList.add(resourceNameMap);
                }
            }

            Properties extraProperties = new Properties();
            extraProperties.put("jdbcResources", resourcesList);
            report.setExtraProperties(extraProperties);

        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString("list.jdbc.resources.failed",
                    "List JDBC resources failed"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
