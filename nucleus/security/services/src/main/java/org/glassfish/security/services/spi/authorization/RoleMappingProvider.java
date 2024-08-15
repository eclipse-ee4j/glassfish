/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.services.spi.authorization;

import java.util.List;

import org.glassfish.security.services.api.authorization.AzAttributeResolver;
import org.glassfish.security.services.api.authorization.AzEnvironment;
import org.glassfish.security.services.api.authorization.AzResource;
import org.glassfish.security.services.api.authorization.AzSubject;
import org.glassfish.security.services.api.authorization.RoleMappingService;
import org.glassfish.security.services.spi.SecurityProvider;
import org.jvnet.hk2.annotations.Contract;

/**
 * <code>RoleMappingProvider</code> instances are used by the
 * <code>{@link org.glassfish.security.services.api.authorization.RoleMappingService}</code>
 * to evaluate role policy conditions.
 *
 * The security provider is part of a plug-in mechanism which allows decisions
 * to be handled by a configured implementation.
 */
@Contract
public interface RoleMappingProvider extends SecurityProvider {

    /**
     * Determine whether the user (<code>AzSubject</code>) has the indicated role
     * for a given resource (<code>AzResource</code>) and application context.
     *
     * @param appContext The application context for the query (can be null).
     * @param subject The target <code>Subject</code>.
     * @param resource The <code>URI</code> resource for the query.
     * @param role The target role.
     * @param environment The attributes collection representing the environment.
     * @param attributeResolvers The ordered list of attribute resolvers.
     *
     * @see {@link org.glassfish.security.services.api.authorization.RoleMappingService#isUserInRole(String, AzSubject, AzResource, String)}
     */
    boolean isUserInRole(String appContext,
        AzSubject subject,
        AzResource resource,
        String role,
        AzEnvironment environment,
        List<AzAttributeResolver> attributeResolvers);

    /**
     * Find an existing <code>RoleDeploymentContext</code>, or create a new one if one does not
     * already exist for the specified application context.
     *
     * @param appContext The application context for which the <code>RoleDeploymentContext</code> is desired.
     *
     * @see {@link org.glassfish.security.services.api.authorization.RoleMappingService#findOrCreateDeploymentContext(String)}
     */
    RoleMappingService.RoleDeploymentContext findOrCreateDeploymentContext(String appContext);
}
