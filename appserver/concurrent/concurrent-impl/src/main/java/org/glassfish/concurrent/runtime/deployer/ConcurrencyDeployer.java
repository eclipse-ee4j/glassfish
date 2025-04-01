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

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.deployment.ResourceDescriptor;

import jakarta.inject.Inject;

import java.util.Collection;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.glassfish.resourcebase.resources.api.ResourceConflictException;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceInfo;

import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.deriveResourceName;

/**
 * @param <D> {@link ResourceDescriptor} managed by this class.
 *
 * @author David Matejcek
 */
public abstract class ConcurrencyDeployer<D extends ResourceDescriptor> implements ResourceDeployer<D> {

    @Inject
    private InvocationManager invocationManager;

    /**
     * Returns false - cannot be deployed before application.
     */
    @Override
    public final boolean canDeploy(boolean postApplicationDeployment, Collection<Resource> allResources, Resource resource) {
        return false;
    }


    /**
     * Does nothing
     */
    @Override
    public void validatePreservedResource(Application oldApp, Application newApp, Resource resource,
        Resources allResources) throws ResourceConflictException {
    }


    /**
     * Same as {@link #deployResource(Object)}
     */
    @Override
    public void enableResource(D resource) throws Exception {
        deployResource(resource);
    }


    /**
     * Same as {@link #undeployResource(Object)}
     */
    @Override
    public void disableResource(D resource) throws Exception {
        undeployResource(resource);
    }


    /**
     * Create {@link ResourceInfo} instance.
     * Resource name is resolved by {@link ConnectorsUtil#deriveResourceName(String, SimpleJndiName, JavaEEResourceType)}
     *
     * @param descriptor
     * @param applicatioName
     * @param moduleName
     * @return {@link ResourceInfo}
     */
    protected ResourceInfo toResourceInfo(final D descriptor, String applicatioName, String moduleName) {
        return new ResourceInfo(toResourceName(descriptor), applicatioName, moduleName);
    }


    /**
     * Create {@link ResourceInfo} instance.
     * Resource name is resolved by {@link ConnectorsUtil#deriveResourceName(String, SimpleJndiName, JavaEEResourceType)}
     *
     * @param descriptor
     * @return {@link ResourceInfo}
     */
    protected ResourceInfo toResourceInfo(final D descriptor) {
        ComponentInvocation invocation = invocationManager.getCurrentInvocation();
        return new ResourceInfo(toResourceName(descriptor), invocation.getAppName(), invocation.getModuleName());
    }


    /**
     * Calls {@link ConnectorsUtil#deriveResourceName(String, SimpleJndiName, JavaEEResourceType)} using
     * descriptor attributes
     *
     * @param descriptor
     * @return JNDI name
     */
    protected SimpleJndiName toResourceName(D descriptor) {
        return deriveResourceName(descriptor.getResourceId(), descriptor.getJndiName(), descriptor.getResourceType());
    }
}
