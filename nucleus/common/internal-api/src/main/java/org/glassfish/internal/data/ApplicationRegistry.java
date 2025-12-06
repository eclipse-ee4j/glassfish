/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.internal.data;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.internal.deployment.Deployment;
import org.jvnet.hk2.annotations.Service;

/**
 * Registry for deployed Applications
 *
 * TODO : dochez this class needs to go, I think we should use the configured tree (applications) to store this list.
 * This could be achieve once hk2 configured support Transient objects attachment.
 * [narrator: and 15 years later this class is still there]
 */
@Service
@Singleton
public class ApplicationRegistry {

    @Inject
    Provider<Deployment> deploymentServiceProvider;

    @Inject
    Provider<InvocationManager> invocationManagerProvider;

    private Map<String, ApplicationInfo> deployedApplications = new HashMap<>();

    public synchronized void add(String name, ApplicationInfo info) {
        deployedApplications.put(name, info);
    }

    public ApplicationInfo get(String name) {
        return deployedApplications.get(name);
    }

    public synchronized void remove(String name) {
        deployedApplications.remove(name);
    }

    public Set<String> getAllApplicationNames() {
        return deployedApplications.keySet();
    }

    /**
     * Return current application or null, if we're in context of the server, outside of a context of any application.
     *
     * @return Information about the current application
     */
    public Optional<ApplicationInfo> getCurrentApplicationInfo() {
        final Deployment deploymentService = deploymentServiceProvider.get();
        DeploymentContext deploymentContext = deploymentService.getCurrentDeploymentContext();
        if (deploymentContext != null) {
            // during app deployment, we don't have current invocation, we retrieve it from the deployment
            return Optional.ofNullable(deploymentContext.getModuleMetaData(ApplicationInfo.class));
        }
        final ComponentInvocation currentInvocation = invocationManagerProvider.get().getCurrentInvocation();
        String applicationName = null;
        if (currentInvocation != null && null != (applicationName = currentInvocation.getAppName())) {
            // application started
            return Optional.ofNullable(this.get(applicationName));
        }
        // we're not in a context related to an application
        return Optional.empty();
    }

}
