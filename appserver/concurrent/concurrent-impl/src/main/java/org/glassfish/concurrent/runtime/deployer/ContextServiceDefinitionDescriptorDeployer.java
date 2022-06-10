/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.glassfish.concurrent.runtime.deployer;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.RefAddr;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.logging.LogHelper;
import org.glassfish.concurrent.LogFacade;
import org.glassfish.concurrent.runtime.ConcurrentRuntime;
import org.glassfish.resourcebase.resources.api.ResourceConflictException;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resourcebase.resources.naming.ResourceNamingService;
import org.glassfish.resources.naming.SerializableObjectRefAddr;
import org.jvnet.hk2.annotations.Service;

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.deployment.ContextServiceDefinitionDescriptor;

import jakarta.inject.Inject;

/**
 * Deployer for ContextServiceDefinitionDescriptor.
 *
 * @author Petr Aubrecht <aubrecht@asoftware.cz>
 */
@Service
@ResourceDeployerInfo(ContextServiceDefinitionDescriptor.class)
public class ContextServiceDefinitionDescriptorDeployer implements ResourceDeployer {

    private static final Logger logger = Logger.getLogger(ContextServiceDefinitionDescriptorDeployer.class.getName());

    @Inject
    private ResourceNamingService namingService;

    @Inject
    private InvocationManager invocationManager;

    @Inject
    ConcurrentRuntime concurrentRuntime;

    @Override
    public void deployResource(Object resource, String applicationName, String moduleName) throws Exception {
        //not implemented
    }

    @Override
    public void deployResource(Object resource) throws Exception {
        ContextServiceDefinitionDescriptor concurrentDefinitionDescriptor = (ContextServiceDefinitionDescriptor) resource;
        ContextServiceConfig contextServiceConfig
                = new ContextServiceConfig(concurrentDefinitionDescriptor.getName(), null, "true");
        String applicationName = invocationManager.getCurrentInvocation().getAppName();
        String customNameOfResource = ConnectorsUtil.deriveResourceName(concurrentDefinitionDescriptor.getResourceId(), concurrentDefinitionDescriptor.getName(), concurrentDefinitionDescriptor.getResourceType());
        ResourceInfo resourceInfo = new ResourceInfo(customNameOfResource, applicationName, null);
        javax.naming.Reference ref = new javax.naming.Reference(
                jakarta.enterprise.concurrent.ContextServiceDefinition.class.getName(),
                "org.glassfish.concurrent.runtime.deployer.ConcurrentObjectFactory",
                null);
        RefAddr addr = new SerializableObjectRefAddr(ContextServiceConfig.class.getName(), contextServiceConfig);
        ref.add(addr);
        RefAddr resAddr = new SerializableObjectRefAddr(ResourceInfo.class.getName(), resourceInfo);
        ref.add(resAddr);

        try {
            // Publish the object ref
            namingService.publishObject(resourceInfo, ref, true);
        } catch (NamingException ex) {
            LogHelper.log(logger, Level.SEVERE, LogFacade.UNABLE_TO_BIND_OBJECT, ex,
                    "ContextService", contextServiceConfig.getJndiName());
        }
    }

    @Override
    public void undeployResource(Object resource) throws Exception {
        ContextServiceDefinitionDescriptor concurrentDefinitionDescriptor = (ContextServiceDefinitionDescriptor) resource;
        throw new UnsupportedOperationException("undeployResource not supported yet.");
//        ResourceInfo resourceInfo = ResourceUtil.getResourceInfo(concurrentDefinitionDescriptor);
//        undeployResource(resource, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
    }

    @Override
    public void undeployResource(Object resource, String applicationName, String moduleName) throws Exception {
        ContextServiceDefinitionDescriptor concurrentDefinitionDescriptor = (ContextServiceDefinitionDescriptor) resource;
        String jndiName = concurrentDefinitionDescriptor.getName();
        ResourceInfo resourceInfo = new ResourceInfo(jndiName, applicationName, moduleName);
        namingService.unpublishObject(resourceInfo, jndiName);
        // stop the runtime object
        concurrentRuntime.shutdownContextService(jndiName);
    }

    @Override
    public void redeployResource(Object resource) throws Exception {
        undeployResource(resource);
        deployResource(resource);
    }

    @Override
    public void enableResource(Object resource) throws Exception {
        deployResource(resource);
    }

    @Override
    public void disableResource(Object resource) throws Exception {
        undeployResource(resource);
    }

    @Override
    public boolean handles(Object resource) {
        return resource instanceof ContextServiceDefinitionDescriptor;
    }

    @Override
    public boolean supportsDynamicReconfiguration() {
        return false;
    }

    @Override
    public Class[] getProxyClassesForDynamicReconfiguration() {
        return new Class[0];
    }

    @Override
    public boolean canDeploy(boolean postApplicationDeployment, Collection<Resource> allResources, Resource resource) {
        if (handles(resource)) {
            if (!postApplicationDeployment) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void validatePreservedResource(Application oldApp, Application newApp, Resource resource, Resources allResources) throws ResourceConflictException {

    }
}