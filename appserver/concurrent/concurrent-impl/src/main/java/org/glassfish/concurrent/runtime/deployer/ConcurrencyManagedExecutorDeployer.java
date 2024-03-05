/*
 * Copyright (c) 2022, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.deployment.ManagedExecutorDefinitionDescriptor;

import jakarta.inject.Inject;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.concurrent.runtime.ConcurrentRuntime;
import org.glassfish.concurrent.runtime.deployer.cfg.ManagedExecutorServiceCfg;
import org.glassfish.concurro.ContextServiceImpl;
import org.glassfish.concurro.ManagedExecutorServiceImpl;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resourcebase.resources.naming.ResourceNamingService;
import org.jvnet.hk2.annotations.Service;


/**
 * @author David Matejcek
 */
@Service
@ResourceDeployerInfo(ManagedExecutorDefinitionDescriptor.class)
public class ConcurrencyManagedExecutorDeployer extends ConcurrencyDeployer<ManagedExecutorDefinitionDescriptor> {

    @Inject
    private InvocationManager invocationManager;
    @Inject
    private ResourceNamingService namingService;
    @Inject
    private ConcurrentRuntime runtime;

    @Override
    public boolean handles(Object resource) {
        return resource instanceof ManagedExecutorDefinitionDescriptor;
    }


    @Override
    public void deployResource(ManagedExecutorDefinitionDescriptor resource) throws Exception {
        String applicationName = invocationManager.getCurrentInvocation().getAppName();
        String moduleName = invocationManager.getCurrentInvocation().getModuleName();
        deployResource(resource, applicationName, moduleName);
    }


    @Override
    public void deployResource(ManagedExecutorDefinitionDescriptor resource, String applicationName, String moduleName) throws Exception {
        ManagedExecutorDefinitionDescriptor descriptor = resource;
        ManagedExecutorServiceImpl service = createExecutorService(applicationName, moduleName, descriptor);
        ResourceInfo resourceInfo = new ResourceInfo(toResourceName(descriptor), applicationName, moduleName);
        namingService.publishObject(resourceInfo, service, true);
    }


    @Override
    public void undeployResource(ManagedExecutorDefinitionDescriptor resource) throws Exception {
        ManagedExecutorDefinitionDescriptor descriptor = resource;
        String applicationName = invocationManager.getCurrentInvocation().getAppName();
        String moduleName = invocationManager.getCurrentInvocation().getModuleName();
        SimpleJndiName resourceName = toResourceName(descriptor);
        ResourceInfo resourceInfo = new ResourceInfo(resourceName, applicationName, moduleName);
        namingService.unpublishObject(resourceInfo);
        runtime.shutdownContextService(resourceName);
    }


    @Override
    public void undeployResource(ManagedExecutorDefinitionDescriptor resource, String applicationName, String moduleName) throws Exception {
        ManagedExecutorDefinitionDescriptor descriptor = resource;
        SimpleJndiName resourceName = toResourceName(descriptor);
        ResourceInfo resourceInfo = new ResourceInfo(resourceName, applicationName, moduleName);
        namingService.unpublishObject(resourceInfo);
        runtime.shutdownContextService(resourceName);
    }


    @Override
    public void redeployResource(ManagedExecutorDefinitionDescriptor resource) throws Exception {
        undeployResource(resource);
        deployResource(resource);
    }


    private ManagedExecutorServiceImpl createExecutorService(String applicationName, String moduleName,
        ManagedExecutorDefinitionDescriptor descriptor) {
        ConcurrencyManagedExecutorServiceConfig config = new ConcurrencyManagedExecutorServiceConfig(descriptor);
        ManagedExecutorServiceCfg mesConfig = new ManagedExecutorServiceCfg(config);
        ContextServiceImpl contextService = runtime.findOrCreateContextService(descriptor, applicationName, moduleName);
        return runtime.createManagedExecutorService(mesConfig, contextService);
    }
}
