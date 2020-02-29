/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.util.LocalStringManagerImpl;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.data.EngineInfo;
import org.glassfish.internal.data.ContainerRegistry;
import com.sun.enterprise.module.HK2Module;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Engine;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.*;
import org.glassfish.api.container.Sniffer;
import javax.inject.Inject;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Singleton;

/**
 * This admin command list the containers currentely running within that
 * Glassfish instance
 */
@Service(name="list-containers")
@Singleton        // no per-execution state
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.containers.command")
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.GET, 
        path="list-containers", 
        description="list-containers")
})
@AccessRequired(resource="domain", action="read")
public class ListContainersCommand implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListContainersCommand.class);

    @Inject
    ContainerRegistry containerRegistry;

    @Inject
    ModulesRegistry modulesRegistry;

    @Inject
    ServiceLocator habitat;

    @Inject
    Applications applications;

    public void execute(AdminCommandContext context) {

        ActionReport report = context.getActionReport();
        report.setActionDescription(localStrings.getLocalString("list.containers.command", "List of Containers"));
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        ActionReport.MessagePart top = report.getTopMessagePart();
        top.setMessage(localStrings.getLocalString("list.containers.command", "List of Containers"));
        top.setChildrenType(localStrings.getLocalString("container", "Container"));

        Iterable<? extends Sniffer> sniffers = habitat.getAllServices(Sniffer.class);
        if (sniffers ==null) {
            top.setMessage(localStrings.getLocalString("list.containers.nocontainer",
                    "No container currently configured"));
        } else {
            for (Sniffer sniffer : sniffers) {
                ActionReport.MessagePart container = top.addChild();
                container.setMessage(sniffer.getModuleType());
                container.addProperty(localStrings.getLocalString("contractprovider", "ContractProvider"),
                        sniffer.getModuleType());
                EngineInfo engineInfo = containerRegistry.getContainer(sniffer.getModuleType());

                if (engineInfo != null) {
                    container.addProperty(
                            localStrings.getLocalString("status", "Status"),
                            localStrings.getLocalString("started", "Started"));
                    HK2Module connectorModule = modulesRegistry.find(engineInfo.getSniffer().getClass());
                    container.addProperty(localStrings.getLocalString("connector", "Connector"),
                            connectorModule.getModuleDefinition().getName() +
                            ":" + connectorModule.getModuleDefinition().getVersion());
                    container.addProperty(localStrings.getLocalString("implementation", "Implementation"),
                            engineInfo.getContainer().getClass().toString());
                    boolean atLeastOne = false;
                    for (Application app : applications.getApplications()) {
                        for (com.sun.enterprise.config.serverbeans.Module module : app.getModule()) {
                            Engine engine = module.getEngine(engineInfo.getSniffer().getModuleType());
                            if (engine!=null) {
                                if (!atLeastOne) {
                                    atLeastOne=true;
                                    container.setChildrenType(localStrings.getLocalString("list.containers.listapps",
                                            "Applications deployed"));

                                }
                                container.addChild().setMessage(app.getName());
                            }
                        }

                        
                    }
                    if (!atLeastOne) {
                       container.addProperty("Status", "Not Started");
                    }
                }
            }
        }
    }
}
