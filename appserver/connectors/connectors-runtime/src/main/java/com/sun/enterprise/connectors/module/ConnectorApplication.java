/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.connectors.module;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.resourcebase.resources.api.ResourceConstants;
import org.glassfish.resourcebase.resources.listener.ResourceManager;
import org.glassfish.resources.listener.ApplicationScopedResourcesManager;

import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

/**
 * Represents a connector application, one per resource-adapter.
 * GlassFish kernel will call start/stop of connector application during start/stop of server and
 * deploy/undeploy of the resource-adapter.
 *
 * @author Jagadish Ramu
 */
public class ConnectorApplication implements ApplicationContainer<ConnectorDescriptor>, EventListener {
    private static final Logger LOG = LogDomains.getLogger(ConnectorApplication.class, LogDomains.RSR_LOGGER);
    private String moduleName = "";
    //indicates the "application" (ear) name if its embedded rar
    private final String applicationName;
    private final ResourceManager resourceManager;
    private final ClassLoader loader;
    private final ConnectorRuntime runtime;
    private final Events event;
    private final ConnectorDescriptor descriptor;
    private static StringManager localStrings = StringManager.getManager(ConnectorRuntime.class);
    private final ResourcesUtil resourcesUtil;

    public ConnectorApplication(String moduleName, String appName, ResourceManager resourceManager,
        ApplicationScopedResourcesManager asrManager, ClassLoader loader, ConnectorRuntime runtime, Events event,
        ConnectorDescriptor descriptor) {
        this.setModuleName(moduleName);
        this.resourceManager = resourceManager;
        this.loader = loader;
        this.runtime = runtime;
        this.applicationName = appName;
        this.event = event;
        this.descriptor = descriptor;
        this.resourcesUtil = ResourcesUtil.createInstance();
    }

    /**
     * Returns the deployment descriptor associated with this application
     *
     * @return deployment descriptor if they exist or null if not
     */
    @Override
    public ConnectorDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Starts an application container.
     * ContractProvider starting should not throw an exception but rather should
     * use their prefered Logger instance to log any issue they encounter while
     * starting. Returning false from a start mean that the container failed
     * to start
     *
     * @param startupContext the start up context
     * @return true if the container startup was successful.
     */
    @Override
    public boolean start(ApplicationContext startupContext) {
        boolean started = false;

        deployResources();
        runtime.registerConnectorApplication(this);

        started = true;

        event.register(this);

        LOG.log(Level.INFO, "Resource Adapter [ {0} ] started", getModuleName());
        return started;
    }

    /**
     * deploy all resources/pools pertaining to this resource adapter
     */
    public void deployResources() {
        deployGlobalResources();
        //deployApplicationScopedResources();
    }

    private void deployGlobalResources() {
        Resources allResources = resourceManager.getAllResources();
        Collection<Resource> resources = resourcesUtil.filterConnectorResources(allResources, moduleName, false);
        resourceManager.deployResources(resources);
    }

/*
    private void deployApplicationScopedResources() {
        Resources resources = asrManager.getResources(applicationName);
        if(resources != null){
            Collection<Resource> connectorResources = filterConnectorResources(resources);
            asrManager.deployResources(connectorResources);
        }
    }
*/
    /**
     * undeploy all resources/pools pertaining to this resource adapter
     */
    public void undeployResources() {
        undeployGlobalResources(false);
        //undeployApplicationScopedResources();
    }

/*
    public void undeployApplicationScopedResources() {
        Collection<Resource> resources = filterConnectorResources(asrManager.getResources(applicationName));
        asrManager.undeployResources(resources);
    }
*/

    /**
     * undeploy all resources/pools pertaining to this resource adapter
     */
    public boolean undeployGlobalResources(boolean failIfResourcesExist) {
        //TODO ASR : should we undeploy app-scoped connector resources also ?
        //TODO ASR : should we stop deployment by checking app-scoped connector resources also ?
        Collection<Resource> resources =
                resourcesUtil.filterConnectorResources(resourceManager.getAllResources(), moduleName, true);
        if (failIfResourcesExist && !resources.isEmpty()) {
            String message = "one or more resources of resource-adapter [ " + moduleName + " ] exist, " +
                    "use '--cascade=true' to delete them during undeploy";
            LOG.log(Level.WARNING, "resources.of.rar.exist", moduleName);
            throw new RuntimeException(message);
        }
        resourceManager.undeployResources(resources);
        return true;
    }

    /**
     * Stop the application container
     *
     * @param stopContext
     * @return true if stopping was successful.
     */
    @Override
    public boolean stop(ApplicationContext stopContext) {
        boolean stopped = false;

        DeploymentContext dc = (DeploymentContext) stopContext;
        UndeployCommandParameters dcp = dc.getCommandParameters(UndeployCommandParameters.class);
        boolean failIfResourcesExist = false;

        //"stop" may be called even during deployment/load failure.
        //Look for the undeploy flags only when it is undeploy-command
        if(dcp != null){
            if (dcp.origin == OpsParams.Origin.undeploy) {
                if(!(dcp._ignoreCascade || dcp.cascade)){
                    failIfResourcesExist = true;
                }
            }
        }

        if (!undeployGlobalResources(failIfResourcesExist)) {
            stopped = false;
        } else {
            runtime.unregisterConnectorApplication(getModuleName());
            stopped = true;
            LOG.log(Level.INFO, "Resource Adapter [ {0} ] stopped", getModuleName());
            event.unregister(this);
        }
        return stopped;
    }

    /**
     * Suspends this application container.
     *
     * @return true if suspending was successful, false otherwise.
     */
    @Override
    public boolean suspend() {
        // Not (yet) supported
        return false;
    }

    /**
     * Resumes this application container.
     *
     * @return true if resumption was successful, false otherwise.
     */
    @Override
    public boolean resume() {
        // Not (yet) supported
        return false;
    }

    /**
     * Returns the class loader associated with this application
     *
     * @return ClassLoader for this app
     */
    @Override
    public ClassLoader getClassLoader() {
        return loader;
    }


    /**
     * returns the module name
     *
     * @return module-name
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * set the module name of the application
     *
     * @param moduleName module-name
     */
    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    /**
     * event listener to listen to </code>resource-adapter undeploy validation</code> and
     * to validate the undeployment. Undeployment will fail, if resources are found
     * and --cascade is not set.
     * @param event Event
     */
    @Override
    public void event(Event<?> event) {
        if (Deployment.UNDEPLOYMENT_VALIDATION.equals(event.type())) {
            // this is an application undeploy event
            DeploymentContext dc = (DeploymentContext) event.hook();
            UndeployCommandParameters dcp = dc.getCommandParameters(UndeployCommandParameters.class);
            // Consider the application with embedded RAR being undeployed
            if (dcp.name.equals(moduleName) || (dcp.name.equals(applicationName)
                && moduleName.contains(ResourceConstants.EMBEDDEDRAR_NAME_DELIMITER)
                && moduleName.startsWith(dcp.name))) {

                if (dcp.origin != OpsParams.Origin.deploy && dcp.origin == OpsParams.Origin.undeploy) {
                    if (!dcp._ignoreCascade && !dcp.cascade) {
                        if (!resourcesUtil.filterConnectorResources(resourceManager.getAllResources(), moduleName, true).isEmpty()) {
                            String message = localStrings.getString("con.deployer.resources.exist", moduleName);
                            LOG.log(Level.WARNING, "resources.of.rar.exist", moduleName);

                            ActionReport report = dc.getActionReport();
                            report.setMessage(message);
                            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                        }
                    }
                }
            }
        }
    }
}
