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
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.concurrent.config.ManagedThreadFactory;
import org.glassfish.concurrent.runtime.ConcurrentRuntime;
import org.glassfish.concurrent.runtime.LogFacade;
import org.glassfish.concurrent.runtime.deployer.cfg.ManagedThreadFactoryCfg;
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
@ResourceDeployerInfo(ManagedThreadFactory.class)
public class ManagedThreadFactoryDeployer implements ResourceDeployer<ManagedThreadFactory> {

    private static final Logger LOG = LogFacade.getLogger();

    @Inject
    private ResourceNamingService namingService;
    @Inject
    private ConcurrentRuntime concurrentRuntime;


    @Override
    public boolean handles(Object resource) {
        return resource instanceof ManagedThreadFactory;
    }


    @Override
    public void validatePreservedResource(Application oldApp, Application newApp, Resource resource, Resources allResources) throws ResourceConflictException {
        // do nothing
    }


    @Override
    public void deployResource(ManagedThreadFactory resource) throws Exception {
        ResourceInfo resourceInfo = ResourceUtil.getResourceInfo(resource);
        deployResource(resource, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
    }


    @Override
    public void deployResource(ManagedThreadFactory resource, String applicationName, String moduleName) throws Exception {
        if (resource == null) {
            LOG.log(Level.WARNING, LogFacade.DEPLOY_ERROR_NULL_CONFIG, "ManagedThreadFactory");
            return;
        }
        SimpleJndiName jndiName = new SimpleJndiName(resource.getJndiName());
        ResourceInfo resourceInfo = new ResourceInfo(jndiName, applicationName, moduleName);
        ManagedThreadFactoryCfg config = new ManagedThreadFactoryCfg(resource);
        Reference ref = new Reference(
                jakarta.enterprise.concurrent.ManagedThreadFactory.class.getName(),
                "org.glassfish.concurrent.runtime.deployer.ConcurrentObjectFactory",
                null);
        RefAddr addr = new SerializableObjectRefAddr(ManagedThreadFactoryCfg.class.getName(), config);
        ref.add(addr);
        RefAddr resAddr = new SerializableObjectRefAddr(ResourceInfo.class.getName(), resourceInfo);
        ref.add(resAddr);

        try {
            namingService.publishObject(resourceInfo, ref, true);
        } catch (NamingException ex) {
            LogHelper.log(LOG, Level.SEVERE, LogFacade.UNABLE_TO_BIND_OBJECT, ex, "ManagedThreadFactory", jndiName);
        }
    }


    @Override
    public void undeployResource(ManagedThreadFactory resource) throws Exception {
        ManagedThreadFactory factory = resource;
        ResourceInfo resourceInfo = ResourceUtil.getResourceInfo(factory);
        undeployResource(resource, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
    }


    @Override
    public void undeployResource(ManagedThreadFactory resource, String applicationName, String moduleName) throws Exception {
        SimpleJndiName jndiName = new SimpleJndiName(resource.getJndiName());
        ResourceInfo resourceInfo = new ResourceInfo(jndiName, applicationName, moduleName);
        namingService.unpublishObject(resourceInfo, jndiName);
        concurrentRuntime.shutdownManagedThreadFactory(jndiName);
    }
}
