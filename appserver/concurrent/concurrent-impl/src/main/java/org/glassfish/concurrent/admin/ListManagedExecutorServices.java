/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.concurrent.admin;

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
import org.glassfish.concurrent.config.ManagedExecutorService;
import org.glassfish.concurrent.runtime.deployer.DefaultManagedExecutorService;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.resourcebase.resources.util.BindableResourcesHelper;
import org.jvnet.hk2.annotations.Service;

/**
 * List Managed Executor Service Resources command
 *
 */
@TargetType(value={CommandTarget.DAS, CommandTarget.DOMAIN, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTERED_INSTANCE, CommandTarget.CONFIG,  })
@ExecuteOn(value={RuntimeType.DAS})
@Service(name="list-managed-executor-services")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.managed.executor.services")
@RestEndpoints({
    @RestEndpoint(configBean=Resources.class,
        opType=RestEndpoint.OpType.GET,
        path="list-managed-executor-services",
        description="List Managed Executor Services")
})
public class ListManagedExecutorServices implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListManagedExecutorServices.class);

    @Param(primary = true, optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    private String target ;

    @Inject
    private Domain domain;

    @Inject
    private BindableResourcesHelper bindableResourcesHelper;

    @Inject
    private ServiceLocator habitat;

    @Inject
    private DefaultManagedExecutorService defaultService; // make sure default has been created

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the parameter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        try {
            Collection<ManagedExecutorService> managedExecutorServices = domain.getResources().getResources(ManagedExecutorService.class);
            List<Map<String,String>> resourcesList = new ArrayList<Map<String, String>>();
            List<DefaultResourceProxy> drps = habitat.getAllServices(DefaultResourceProxy.class);

            for (ManagedExecutorService managedExecutorService : managedExecutorServices) {
                String jndiName = managedExecutorService.getJndiName();
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
            extraProperties.put("managedExecutorServices", resourcesList);
            report.setExtraProperties(extraProperties);
        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString("list.managed.executor.service.failed", "List managed executor services failed"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
