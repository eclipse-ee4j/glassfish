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

import com.sun.enterprise.deployment.ManagedThreadFactoryDefinitionDescriptor;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.concurrent.runtime.ConcurrentRuntime;
import org.glassfish.concurrent.runtime.deployer.cfg.ManagedThreadFactoryCfg;
import org.glassfish.concurro.ContextServiceImpl;
import org.glassfish.concurro.ManagedThreadFactoryImpl;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resourcebase.resources.naming.ResourceNamingService;
import org.jvnet.hk2.annotations.Service;


/**
 * @author David Matejcek
 */
@Service
@Singleton
@ResourceDeployerInfo(ManagedThreadFactoryDefinitionDescriptor.class)
public class ConcurrencyManagedThreadFactoryDeployer
    extends ConcurrencyDeployer<ManagedThreadFactoryDefinitionDescriptor> {

    @Inject
    private InvocationManager invocationManager;
    @Inject
    private ResourceNamingService resourceNamingService;
    @Inject
    private ConcurrentRuntime runtime;

    @Override
    public boolean handles(Object resource) {
        return resource instanceof ManagedThreadFactoryDefinitionDescriptor;
    }


    @Override
    public void deployResource(ManagedThreadFactoryDefinitionDescriptor resource) throws Exception {
        String applicationName = invocationManager.getCurrentInvocation().getAppName();
        String moduleName = invocationManager.getCurrentInvocation().getModuleName();
        deployResource(resource, applicationName, moduleName);
    }


    @Override
    public void deployResource(ManagedThreadFactoryDefinitionDescriptor resource, String applicationName, String moduleName) throws Exception {
        ManagedThreadFactoryDefinitionDescriptor descriptor = resource;
        ManagedThreadFactoryImpl factory = createThreadFactory(applicationName, moduleName, descriptor);
        SimpleJndiName resourceName = toResourceName(descriptor);
        ResourceInfo resourceInfo = new ResourceInfo(resourceName, applicationName, moduleName);
        resourceNamingService.publishObject(resourceInfo, factory, true);
    }


    @Override
    public void undeployResource(ManagedThreadFactoryDefinitionDescriptor resource) throws Exception {
    }


    @Override
    public void undeployResource(ManagedThreadFactoryDefinitionDescriptor resource, String applicationName, String moduleName) throws Exception {
    }


    private ManagedThreadFactoryImpl createThreadFactory(String applicationName, String moduleName,
        ManagedThreadFactoryDefinitionDescriptor descriptor) {
        ConcurrencyManagedThreadFactoryConfig config = new ConcurrencyManagedThreadFactoryConfig(descriptor);
        ManagedThreadFactoryCfg mtfConfig = new ManagedThreadFactoryCfg(config);
        ContextServiceImpl contextService = runtime.findOrCreateContextService(descriptor, applicationName, moduleName);
        return runtime.createManagedThreadFactory(mtfConfig, contextService);
    }
}
