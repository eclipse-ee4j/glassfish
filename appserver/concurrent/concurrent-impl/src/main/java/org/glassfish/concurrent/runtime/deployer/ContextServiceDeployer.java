/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;

import org.glassfish.api.logging.LogHelper;
import org.glassfish.concurrent.config.ContextService;
import org.glassfish.concurrent.runtime.ConcurrentRuntime;
import org.glassfish.concurrent.runtime.LogFacade;
import org.glassfish.concurrent.runtime.deployer.cfg.ConcurrentServiceCfg;
import org.glassfish.concurrent.runtime.deployer.cfg.ContextServiceCfg;
import org.glassfish.resourcebase.resources.api.ResourceConflictException;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resourcebase.resources.naming.ResourceNamingService;
import org.glassfish.resourcebase.resources.util.ResourceUtil;
import org.glassfish.resources.naming.SerializableObjectRefAddr;
import org.jvnet.hk2.annotations.Service;


@Service
@Singleton
@ResourceDeployerInfo(ContextService.class)
public class ContextServiceDeployer implements ResourceDeployer {

    private static final Logger LOG = LogFacade.getLogger();

    @Inject
    private ResourceNamingService namingService;
    @Inject
    private ConcurrentRuntime concurrentRuntime;


    @Override
    public void deployResource(Object resource, String applicationName, String moduleName) throws Exception {
        ContextService contextServiceRes = (ContextService) resource;
        if (contextServiceRes == null) {
            LOG.log(Level.WARNING, LogFacade.DEPLOY_ERROR_NULL_CONFIG, "ContextService");
            return;
        }
        String jndiName = contextServiceRes.getJndiName();
        ResourceInfo resourceInfo = new ResourceInfo(jndiName, applicationName, moduleName);
        String contextInfo = contextServiceRes.getContextInfo();
        String contextInfoEnabled = contextServiceRes.getContextInfoEnabled();
        ContextServiceCfg config = new ContextServiceCfg(
            new ConcurrentServiceCfg(jndiName, contextInfo, contextInfoEnabled, null));

        Reference ref = new Reference(jakarta.enterprise.concurrent.ContextService.class.getName(),
            "org.glassfish.concurrent.runtime.deployer.ConcurrentObjectFactory", null);
        RefAddr addr = new SerializableObjectRefAddr(ContextServiceCfg.class.getName(), config);
        ref.add(addr);
        RefAddr resAddr = new SerializableObjectRefAddr(ResourceInfo.class.getName(), resourceInfo);
        ref.add(resAddr);

        try {
            namingService.publishObject(resourceInfo, ref, true);
        } catch (NamingException ex) {
            LogHelper.log(LOG, Level.SEVERE, LogFacade.UNABLE_TO_BIND_OBJECT, ex, "ContextService", jndiName);
        }
    }


    @Override
    public void deployResource(Object resource) throws Exception {
        ContextService contextServiceResource = (ContextService) resource;
        ResourceInfo resourceInfo = ResourceUtil.getResourceInfo(contextServiceResource);
        deployResource(resource, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
    }


    @Override
    public void undeployResource(Object resource) throws Exception {
        ContextService contextServiceResource = (ContextService) resource;
        ResourceInfo resourceInfo = ResourceUtil.getResourceInfo(contextServiceResource);
        undeployResource(resource, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
    }


    @Override
    public void undeployResource(Object resource, String applicationName, String moduleName) throws Exception {
        ContextService contextServiceRes = (ContextService) resource;
        ResourceInfo resourceInfo = new ResourceInfo(contextServiceRes.getJndiName(), applicationName, moduleName);
        namingService.unpublishObject(resourceInfo, contextServiceRes.getJndiName());
        concurrentRuntime.shutdownContextService(contextServiceRes.getJndiName());
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
        return resource instanceof ContextService;
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
}
