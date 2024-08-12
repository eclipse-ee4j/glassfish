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

package org.glassfish.webservices.cli;

import com.sun.enterprise.config.serverbeans.Application;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.Map;
import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.webservices.WebServicesContainer;
import org.glassfish.webservices.deployment.DeployedEndpointData;
import org.glassfish.webservices.deployment.WebServicesDeploymentMBean;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

/**
 * CLI for listing all web services.
 * <p>
 * asadmin __list-webservices [--appname <appname> [--modulename <modulename> [--
endpointname <endpointname>]]]
 *
 * Will be executed on DAS

 * @author Jitendra Kotamraju
 */
@Service(name = "__list-webservices")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn(RuntimeType.DAS)
@RestEndpoints({
    @RestEndpoint(configBean=Application.class,
        opType=RestEndpoint.OpType.GET,
        path="list-webservices",
        description="list-webservices",
        params={
            @RestParam(name="appName", value="$parent")
        })
})
public class ListWebServicesCommand implements AdminCommand {
    @Inject @Optional
    private Provider<WebServicesContainer> containerProvider;

    @Param(optional=true, alias="applicationname")
    String appName;

    @Param(optional=true)
    String moduleName;

    @Param(optional=true)
    String endpointName;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        WebServicesContainer container = containerProvider.get();
        if (container == null) {
            return;
        }
        WebServicesDeploymentMBean bean = container.getDeploymentBean();

        if (appName != null && moduleName != null && endpointName != null) {
            Map<String, Map<String, Map<String, DeployedEndpointData>>> endpoints =
                    bean.getEndpoint(appName, moduleName, endpointName);
            fillEndpoints(report, endpoints);
        } else if (appName != null && moduleName != null) {
            Map<String, Map<String, Map<String, DeployedEndpointData>>> endpoints =
                    bean.getEndpoints(appName, moduleName);
            fillEndpoints(report, endpoints);
        } else if (appName != null) {
            Map<String, Map<String, Map<String, DeployedEndpointData>>> endpoints =
                    bean.getEndpoints(appName);
            fillEndpoints(report, endpoints);
        } else {
            Map<String, Map<String, Map<String, DeployedEndpointData>>> endpoints = bean.getEndpoints();
            fillEndpoints(report, endpoints);
        }

    }

    private void fillEndpoints(ActionReport report, Map<String, Map<String, Map<String, DeployedEndpointData>>> endpoints) {
        if (!endpoints.isEmpty()) {
            Properties extra = new Properties();
            extra.putAll(endpoints);
            report.setExtraProperties(extra);
            ActionReport.MessagePart top = report.getTopMessagePart();
            for(Map.Entry<String, Map<String, Map<String, DeployedEndpointData>>> app : endpoints.entrySet()) {
                ActionReport.MessagePart child = top.addChild();
                child.setMessage("application:"+app.getKey());
                for(Map.Entry<String, Map<String, DeployedEndpointData>> module : app.getValue().entrySet()) {
                    child = child.addChild();
                    child.setMessage("  module:"+module.getKey());
                    for(Map.Entry<String, DeployedEndpointData> endpoint : module.getValue().entrySet()) {
                        child = child.addChild();
                        child.setMessage("    endpoint:"+endpoint.getKey());
                        for(Map.Entry<String, String> endpointData : endpoint.getValue().getStaticAsMap().entrySet()) {
                            child = child.addChild();
                            child.setMessage("      "+endpointData.getKey()+":"+endpointData.getValue());
                        }
                    }
                }
            }
        }
    }

}
