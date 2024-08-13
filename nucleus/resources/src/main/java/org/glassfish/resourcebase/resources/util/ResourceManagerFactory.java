/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resourcebase.resources.util;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.lang.reflect.Proxy;
import java.util.List;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Jagadish Ramu
 */
@Singleton
@Service
public class ResourceManagerFactory {
    public static final String METADATA_KEY = "ResourceImpl";

    @Inject
    private ServiceLocator locator;

    public <T> ResourceDeployer<T> getResourceDeployer(T resource) {
        ServiceHandle<ResourceDeployerInfo> deployerHandle = null;
        for (ServiceHandle<ResourceDeployerInfo> handle : locator.getAllServiceHandles(ResourceDeployerInfo.class)) {
            final ActiveDescriptor<ResourceDeployerInfo> desc = handle.getActiveDescriptor();
            if (desc == null) {
                continue;
            }

            final List<String> resourceImpls = desc.getMetadata().get(METADATA_KEY);
            if (resourceImpls == null || resourceImpls.isEmpty()) {
                continue;
            }
            final String resourceImpl = resourceImpls.get(0);
            if (Proxy.isProxyClass(resource.getClass())) {
                for (Class<?> clz : resource.getClass().getInterfaces()) {
                    if (resourceImpl.equals(clz.getName())) {
                        deployerHandle = handle;
                        break;
                    }
                }

                if (deployerHandle != null) {
                    break;
                }
            }

            if (resourceImpl.equals(resource.getClass().getName())) {
                deployerHandle = handle;
                break;
            }

            // hack : for JdbcConnectionPool impl used by DataSourceDefinition.
            // check whether the interfaces implemented by the class matches
            for (Class<?> clz : resource.getClass().getInterfaces()) {
                if (resourceImpl.equals(clz.getName())) {
                    deployerHandle = handle;
                    break;
                }
            }

            if (deployerHandle != null) {
                break;
            }
        }

        if (deployerHandle != null) {
            ResourceDeployer<T> deployer = (ResourceDeployer<T>) deployerHandle.getService();
            if (deployer != null) {
                return deployer;
            }
        }

        return null;
    }
}
