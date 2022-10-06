/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.deployment.ManagedExecutorDefinitionDescriptor;

import jakarta.inject.Inject;

import java.util.Collection;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.concurrent.runtime.ConcurrentRuntime;
import org.glassfish.concurrent.runtime.deployer.cfg.ManagedExecutorServiceCfg;
import org.glassfish.enterprise.concurrent.ContextServiceImpl;
import org.glassfish.enterprise.concurrent.ManagedExecutorServiceImpl;
import org.glassfish.resourcebase.resources.api.ResourceConflictException;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resourcebase.resources.naming.ResourceNamingService;
import org.jvnet.hk2.annotations.Service;

import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.deriveResourceName;


/**
 * @author David Matejcek
 */
@Service
@ResourceDeployerInfo(ManagedExecutorDefinitionDescriptor.class)
public class ConcurrencyManagedExecutorDeployer implements ResourceDeployer {

    @Inject
    private InvocationManager invocationManager;
    @Inject
    private ResourceNamingService namingService;
    @Inject
    private ConcurrentRuntime runtime;

    @Override
    public void deployResource(Object resource) throws Exception {
        String applicationName = invocationManager.getCurrentInvocation().getAppName();
        String moduleName = invocationManager.getCurrentInvocation().getModuleName();
        deployResource(resource, applicationName, moduleName);
    }


    @Override
    public void deployResource(Object resource, String applicationName, String moduleName) throws Exception {
        ManagedExecutorDefinitionDescriptor descriptor = (ManagedExecutorDefinitionDescriptor) resource;
        ManagedExecutorServiceImpl service = createExecutorService(applicationName, moduleName, descriptor);
        String resourceName = toResourceName(descriptor);
        ResourceInfo resourceInfo = new ResourceInfo(resourceName, applicationName, moduleName);
        namingService.publishObject(resourceInfo, resourceName, service, true);
    }


    @Override
    public void undeployResource(Object resource) throws Exception {
        ManagedExecutorDefinitionDescriptor descriptor = (ManagedExecutorDefinitionDescriptor) resource;
        String applicationName = invocationManager.getCurrentInvocation().getAppName();
        String moduleName = invocationManager.getCurrentInvocation().getModuleName();
        String resourceName = toResourceName(descriptor);
        ResourceInfo resourceInfo = new ResourceInfo(resourceName, applicationName, moduleName);
        namingService.unpublishObject(resourceInfo, resourceInfo.getName());
        runtime.shutdownContextService(resourceInfo.getName());
    }


    @Override
    public void undeployResource(Object resource, String applicationName, String moduleName) throws Exception {
        ManagedExecutorDefinitionDescriptor descriptor = (ManagedExecutorDefinitionDescriptor) resource;
        String resourceName = toResourceName(descriptor);
        ResourceInfo resourceInfo = new ResourceInfo(resourceName, applicationName, moduleName);
        namingService.unpublishObject(resourceInfo, resourceInfo.getName());
        runtime.shutdownContextService(resourceInfo.getName());
    }


    @Override
    public void redeployResource(Object resource) throws Exception {
        undeployResource(resource);
        deployResource(resource);
    }


    @Override
    public void enableResource(Object resource) throws Exception {
    }


    @Override
    public void disableResource(Object resource) throws Exception {
    }


    @Override
    public boolean handles(Object resource) {
        return resource instanceof ManagedExecutorDefinitionDescriptor;
    }


    @Override
    public boolean supportsDynamicReconfiguration() {
        return false;
    }


    @Override
    public Class<?>[] getProxyClassesForDynamicReconfiguration() {
        return new Class[0];
    }


    /**
     * Returns false - cannot be deployed before application.
     */
    @Override
    public boolean canDeploy(boolean postApplicationDeployment, Collection<Resource> allResources, Resource resource) {
        return false;
    }


    @Override
    public void validatePreservedResource(Application oldApp, Application newApp, Resource resource,
        Resources allResources) throws ResourceConflictException {
    }


    private ManagedExecutorServiceImpl createExecutorService(String applicationName, String moduleName,
        ManagedExecutorDefinitionDescriptor descriptor) {
        ConcurrencyManagedExecutorServiceConfig config = new ConcurrencyManagedExecutorServiceConfig(descriptor);
        ManagedExecutorServiceCfg mesConfig = new ManagedExecutorServiceCfg(config);
        ContextServiceImpl contextService = runtime.findOrCreateContextService(descriptor, applicationName, moduleName);
        return runtime.createManagedExecutorService(mesConfig, contextService);
    }

    // FIXME to parent.
    private String toResourceName(ManagedExecutorDefinitionDescriptor descriptor) {
        return deriveResourceName(descriptor.getResourceId(), descriptor.getJndiName(), descriptor.getResourceType());
    }
}
