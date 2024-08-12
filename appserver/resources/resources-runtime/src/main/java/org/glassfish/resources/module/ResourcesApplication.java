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

package org.glassfish.resources.module;

import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.resources.listener.ApplicationScopedResourcesManager;
import org.jvnet.hk2.annotations.Service;

@Service
@PerLookup
public class ResourcesApplication implements ApplicationContainer{

    private static final Logger _logger = LogDomains.getLogger(ResourcesApplication.class, LogDomains.RSR_LOGGER);
    private String applicationName;

    @Inject
    private ApplicationRegistry appRegistry;

    @Inject
    private Applications applications;

    private Resources resources ;

    @Inject
    private ApplicationScopedResourcesManager asrManager;

    @Inject
    private ResourcesDeployer resourcesDeployer;

    public ResourcesApplication(){
    }

    public void setApplicationName(String applicationName){
        this.applicationName = applicationName;
    }

    public String getApplicationName(){
        return applicationName;
    }

    public Object getDescriptor() {
        //TODO return all resources-xml ?
        return null;
    }

    public boolean start(ApplicationContext startupContext) throws Exception {
        DeploymentContext dc = (DeploymentContext)startupContext;
        final DeployCommandParameters deployParams = dc.getCommandParameters(DeployCommandParameters.class);
        //during app. deployment, create resources config and load resources
        if(deployParams.origin == OpsParams.Origin.deploy || deployParams.origin == OpsParams.Origin.deploy_instance){
            resourcesDeployer.deployResources(applicationName, true);
        }else if (deployParams.origin == OpsParams.Origin.load ||
                deployParams.origin == OpsParams.Origin.create_application_ref) {
            //<application> and its <resources>, <modules> are already available.
            //Deploy them.

            //during app. load (eg: server start or application/application-ref enable(), load resources
            asrManager.deployResources(applicationName);
        }
        return true;
    }

    public boolean stop(ApplicationContext stopContext) {
        asrManager.undeployResources(applicationName);
        return true;
    }

    public boolean suspend() {
        return true;
    }

    public boolean resume() throws Exception {
        return true;
    }

    public ClassLoader getClassLoader() {
        //TODO return loader
        return null;
    }
    private void  debug(String message){
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("[ResourcesApplication] " + message);
        }
    }

}
