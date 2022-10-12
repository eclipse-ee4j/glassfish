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
import com.sun.enterprise.deployment.ManagedScheduledExecutorDefinitionDescriptor;

import jakarta.inject.Inject;

import java.util.Collection;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.concurrent.runtime.ConcurrentRuntime;
import org.glassfish.concurrent.runtime.deployer.cfg.ManagedScheduledExecutorServiceCfg;
import org.glassfish.enterprise.concurrent.ContextServiceImpl;
import org.glassfish.enterprise.concurrent.ManagedScheduledExecutorServiceImpl;
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
@ResourceDeployerInfo(ManagedScheduledExecutorDefinitionDescriptor.class)
public class ConcurrencyManagedScheduledExecutorDeployer implements ResourceDeployer {

    @Inject
    private InvocationManager invocationManager;
    @Inject
    private ResourceNamingService resourceNamingService;
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
        ManagedScheduledExecutorDefinitionDescriptor descriptor = (ManagedScheduledExecutorDefinitionDescriptor) resource;
        ManagedScheduledExecutorServiceImpl service = createExecutorService(applicationName, moduleName, descriptor);
        String resourceName = toResourceName(descriptor);
        ResourceInfo resourceInfo = new ResourceInfo(resourceName, applicationName, moduleName);
        resourceNamingService.publishObject(resourceInfo, resourceName, service, true);
    }


    @Override
    public void undeployResource(Object resource) throws Exception {
    }


    @Override
    public void undeployResource(Object resource, String applicationName, String moduleName) throws Exception {
    }


    @Override
    public void redeployResource(Object resource) throws Exception {
    }


    @Override
    public void enableResource(Object resource) throws Exception {
    }


    @Override
    public void disableResource(Object resource) throws Exception {
    }


    @Override
    public boolean handles(Object resource) {
        return resource instanceof ManagedScheduledExecutorDefinitionDescriptor;
    }


    @Override
    public boolean supportsDynamicReconfiguration() {
        return false;
    }


    @Override
    public Class<?>[] getProxyClassesForDynamicReconfiguration() {
        return new Class[0];
    }


    @Override
    public boolean canDeploy(boolean postApplicationDeployment, Collection<Resource> allResources, Resource resource) {
        return false;
    }


    @Override
    public void validatePreservedResource(Application oldApp, Application newApp, Resource resource,
        Resources allResources) throws ResourceConflictException {
    }


    private ManagedScheduledExecutorServiceImpl createExecutorService(String applicationName, String moduleName,
        ManagedScheduledExecutorDefinitionDescriptor descriptor) {
        ConcurrencyManagedScheduledExecutorServiceConfig config = new ConcurrencyManagedScheduledExecutorServiceConfig(descriptor);
        ManagedScheduledExecutorServiceCfg mesConfig = new ManagedScheduledExecutorServiceCfg(config);
        ContextServiceImpl contextService = runtime.findOrCreateContextService(descriptor, applicationName, moduleName);
        return runtime.createManagedScheduledExecutorService(mesConfig, contextService);
    }


    private String toResourceName(ManagedScheduledExecutorDefinitionDescriptor descriptor) {
        return deriveResourceName(descriptor.getResourceId(), descriptor.getJndiName(), descriptor.getResourceType());
    }
}
