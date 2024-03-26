/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.deployment.admin;

import java.io.IOException;
import java.util.Map;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.MessagePart;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.data.EngineRef;
import org.glassfish.internal.data.ModuleInfo;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.config.serverbeans.Application;

import jakarta.inject.Inject;

/**
 * Get deployment configurations command
 */
@Service(name="_get-deployment-configurations")
@ExecuteOn(value={RuntimeType.DAS})
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@RestEndpoints({
    @RestEndpoint(configBean=Application.class,
        opType=RestEndpoint.OpType.GET,
        path="_get-deployment-configurations",
        description="Get Deployment Configurations",
        params={@RestParam(name="appname", value="$parent")})
})
@AccessRequired(resource=DeploymentCommandUtils.APPLICATION_RESOURCE_NAME + "/$appname", action="read")
public class GetDeploymentConfigurationsCommand implements AdminCommand {

    @Param(primary = true)
    private String appname;

    @Inject
    ApplicationRegistry appRegistry;

    /**
     * Entry point from the framework into the command execution
     * @param context context for the command.
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        final MessagePart part = report.getTopMessagePart();
        final ApplicationInfo appInfo = appRegistry.get(appname);
        if (appInfo == null) {
            return;
        }
        try {
            if (appInfo.getEngineRefs().isEmpty()) {
                // standalone module
                for (Sniffer sniffer : appInfo.getSniffers()) {
                    addToResultDDList(appname, sniffer.getDeploymentConfigurations(appInfo.getSource()), part);
                }
            } else {
                // composite archive case, i.e. ear
                for (EngineRef ref : appInfo.getEngineRefs()) {
                    Sniffer appSniffer = ref.getContainerInfo().getSniffer();
                    addToResultDDList("", appSniffer.getDeploymentConfigurations(appInfo.getSource()), part);
                }

                for (ModuleInfo moduleInfo : appInfo.getModuleInfos()) {
                    for (Sniffer moduleSniffer : moduleInfo.getSniffers()) {
                        try (ReadableArchive moduleArchive = appInfo.getSource().getSubArchive(moduleInfo.getName())) {
                            addToResultDDList(moduleInfo.getName(),
                                moduleSniffer.getDeploymentConfigurations(moduleArchive), part);
                        }
                    }
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void addToResultDDList(String moduleName, Map<String, String> snifferConfigs,
        ActionReport.MessagePart part) {
        for (Map.Entry<String, String> pathEntry : snifferConfigs.entrySet()) {
            ActionReport.MessagePart childPart = part.addChild();
            childPart.addProperty(DeploymentProperties.MODULE_NAME, moduleName);
            childPart.addProperty(DeploymentProperties.DD_PATH, pathEntry.getKey());
            childPart.addProperty(DeploymentProperties.DD_CONTENT, pathEntry.getValue());
        }
    }
}
