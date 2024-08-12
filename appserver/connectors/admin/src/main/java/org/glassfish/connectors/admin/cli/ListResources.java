/*
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

package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.ResourcePool;
import com.sun.enterprise.config.serverbeans.Resources;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.connectors.config.ResourceAdapterConfig;
import org.glassfish.connectors.config.WorkSecurityMap;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.jdbc.config.JdbcConnectionPool;
import org.jvnet.hk2.annotations.Service;

@Service(name="_list-resources")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.GET,
        path="_list-resources",
        description="_list-resources")
})
public class ListResources implements AdminCommand {

    @Inject
    private Applications applications;

    @Param(optional = false, name="appname")
    private String appName;

    @Param(optional = true, name="modulename")
    private String moduleName;

    @Inject
    org.glassfish.resourcebase.resources.util.BindableResourcesHelper bindableResourcesHelper;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the parameter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        if(appName != null){
            if(!isValidApplication(appName)){
                ActionReport report = context.getActionReport();
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                ActionReport.MessagePart messagePart = report.getTopMessagePart();
                messagePart.setMessage("Invalid application ["+appName+"]");
                return;
            }
        }
        if(moduleName != null){
            if(!isValidModule(appName, moduleName)){
                ActionReport report = context.getActionReport();
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                ActionReport.MessagePart messagePart = report.getTopMessagePart();
                messagePart.setMessage("Invalid module ["+moduleName+"] in application ["+appName+"]");
                return;
            }
        }
        if(appName != null && moduleName != null){
            Application application = applications.getApplication(appName);
            Module module = application.getModule(moduleName);
            Resources moduleScopedResources = module.getResources();
            if(moduleScopedResources != null){
                ActionReport report = context.getActionReport();
                ActionReport.MessagePart messagePart = report.getTopMessagePart();
                generateResourcesList(messagePart, moduleScopedResources.getResources());
            }
        }else if(appName != null){
            Application application = applications.getApplication(appName);
            Resources appScopedResources = application.getResources();
            if(appScopedResources != null){
                ActionReport report = context.getActionReport();
                ActionReport.MessagePart messagePart = report.getTopMessagePart();
                generateResourcesList(messagePart, appScopedResources.getResources());
            }
        }
    }

    private void generateResourcesList(ActionReport.MessagePart part, List<Resource> resources) {
        Map<String, List<String>> list = new HashMap<String, List<String>>();
        for (Resource r : resources) {
            if (r instanceof BindableResource) {
                String name = ((BindableResource) r).getJndiName();
                String type = "";
                String resourceName = bindableResourcesHelper.getResourceTypeName((BindableResource)(r));
                type = "<" + resourceName + ">";

                List<String> typedResources = getResourcesByType(list, type);
                typedResources.add(name);
            } else if (r instanceof ResourcePool) {
                String name = ((ResourcePool) r).getName();
                String type = "";
                if (r instanceof JdbcConnectionPool) {
                    type = "<JdbcConnectionPool>";
                } else if (r instanceof ConnectorConnectionPool) {
                    type = "<ConnectorConnectionPool>";
                }
                List<String> typedResources = getResourcesByType(list, type);
                typedResources.add(name);

            } else if (r instanceof ResourceAdapterConfig) {
                String name = (((ResourceAdapterConfig) r).getResourceAdapterName());
                String type = "<ResourceAdapterConfig>";
                List<String> typedResources = getResourcesByType(list, type);
                typedResources.add(name);

            } else if (r instanceof WorkSecurityMap) {
                String name = (((WorkSecurityMap) r).getName());
                String type = "<WorkSecurityMap>";
                List<String> typedResources = getResourcesByType(list, type);
                typedResources.add(name);
            }
        }
        for (Map.Entry e : list.entrySet()) {
            String type = (String) e.getKey();
            List<String> values = (List<String>) e.getValue();
            for (String value : values) {
                ActionReport.MessagePart child = part.addChild();
                child.setMessage("  " + value + "\t" + type);
                part.addProperty(value, type);
            }
        }
    }

    private List<String> getResourcesByType(Map<String, List<String>> list, String type) {
        List<String> typedResources = list.get(type);
        if (typedResources == null) {
            typedResources = new ArrayList<String>();
            list.put(type, typedResources);
        }
        return typedResources;
    }

    private boolean isValidApplication(String appName){
        Application app = applications.getApplication(appName);
        return app != null;
    }

    private boolean isValidModule(String appName, String moduleName){
        Application app = applications.getApplication(appName);
        Module module = app.getModule(moduleName);
        return module != null;
    }
}
