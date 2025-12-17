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

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.deployment.ContextServiceDefinitionDescriptor;
import com.sun.enterprise.deployment.ResourceDescriptor;
import com.sun.enterprise.deployment.annotation.handlers.ContextServiceDefinitionData;
import com.sun.enterprise.deployment.types.StandardContextType;

import jakarta.enterprise.concurrent.ContextService;
import jakarta.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.concurrent.runtime.ConcurrentRuntime;
import org.glassfish.concurrent.runtime.LogFacade;
import org.glassfish.concurrent.runtime.deployer.cfg.ContextServiceCfg;
import org.glassfish.concurro.ContextServiceImpl;
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
@ResourceDeployerInfo(ContextServiceDefinitionDescriptor.class)
public class ContextServiceDefinitionDeployer implements ResourceDeployer<ContextServiceDefinitionDescriptor> {

    private static final Logger LOG = LogFacade.getLogger();

    @Inject
    private ResourceNamingService namingService;
    @Inject
    private ConcurrentRuntime runtime;
    @Inject
    private InvocationManager invocationManager;

    @Override
    public void deployResource(ContextServiceDefinitionDescriptor resource) throws Exception {
        ResourceInfo resourceInfo = toResourceInfo(resource);
        ContextService contextService = createContextService(resource.getData());
        namingService.publishObject(resourceInfo, contextService, true);
    }


    @Override
    public void deployResource(ContextServiceDefinitionDescriptor resource, String applicationName, String moduleName) throws Exception {
        ResourceInfo resourceInfo = toResourceInfo(resource);
        ContextService contextService = createContextService(resource.getData());
        namingService.publishObject(resourceInfo, contextService, true);
    }


    @Override
    public void undeployResource(ContextServiceDefinitionDescriptor resource) throws Exception {
        ResourceInfo resourceInfo = toResourceInfo(resource);
        namingService.unpublishObject(resourceInfo);
        runtime.shutdownContextService(resourceInfo.getName());
    }


    @Override
    public void undeployResource(ContextServiceDefinitionDescriptor resource, String applicationName, String moduleName) throws Exception {
        ResourceInfo resourceInfo = toResourceInfo(resource);
        namingService.unpublishObject(resourceInfo);
        runtime.shutdownContextService(resourceInfo.getName());
    }


    @Override
    public void redeployResource(ContextServiceDefinitionDescriptor resource) throws Exception {
        undeployResource(resource);
        deployResource(resource);
    }


    @Override
    public void enableResource(ContextServiceDefinitionDescriptor resource) throws Exception {
        deployResource(resource);
    }


    @Override
    public void disableResource(ContextServiceDefinitionDescriptor resource) throws Exception {
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
    public Class<?>[] getProxyClassesForDynamicReconfiguration() {
        return new Class[0];
    }


    @Override
    public void validatePreservedResource(Application oldApp, Application newApp, Resource resource,
        Resources allResources) throws ResourceConflictException {
    }


    private ResourceInfo toResourceInfo(final ResourceDescriptor descriptor) {
        SimpleJndiName jndiName = deriveResourceName(descriptor.getResourceId(), descriptor.getJndiName(),
            descriptor.getResourceType());
        ComponentInvocation invocation = invocationManager.getCurrentInvocation();
        return new ResourceInfo(jndiName, invocation.getAppName(), invocation.getModuleName());
    }


    private ContextServiceImpl createContextService(ContextServiceDefinitionData data) throws Exception {
        LOG.log(Level.FINEST, "createContextService(data={0})", data);
        if (data.getCleared() == null || data.getCleared().isEmpty()) {
            data.addCleared(StandardContextType.WorkArea.name());
        }
        if (data.getPropagated() == null || data.getPropagated().isEmpty()) {
            data.addPropagated(StandardContextType.Remaining.name());
        }
        return runtime.createContextService(new ContextServiceCfg(data));
    }
}
