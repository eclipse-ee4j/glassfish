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

package org.glassfish.security.services.api.authorization;

import java.net.URI;

import javax.security.auth.Subject;

import org.glassfish.security.services.api.SecurityService;
import org.jvnet.hk2.annotations.Contract;

/**
 * The <code>RoleMappingService</code> provides functions that determine a user's role.
 */
@Contract
public interface RoleMappingService extends SecurityService {

    /**
     * Determine whether the user (<code>Subject</code>) has the indicated role
     * for a given resource (<code>URI</code>) and application context.
     *
     * @param appContext The application context for the query (can be null).
     * @param subject The target <code>Subject</code>.
     * @param resource The <code>URI</code> resource for the query.
     * @param role The target role.
     * @return true if the user has the specified role.
     *
     * @throws IllegalArgumentException for a <code>null</code> subject or resource
     * @throws IllegalStateException if the service was not initialized.
     */
    boolean isUserInRole(String appContext, Subject subject, URI resource, String role);

    /**
     * Determine whether the user (<code>AzSubject</code>) has the indicated role
     * for a given resource (<code>AzResource</code>) and application context.
     *
     * @param appContext The application context for the query (can be null).
     * @param subject The target <code>{@link org.glassfish.security.services.api.authorization.AzSubject}</code>.
     * @param resource The <code>{@link org.glassfish.security.services.api.authorization.AzResource}</code> for the query.
     * @param role The target role.
     * @return true if the user has the specified role.
     *
     * @throws IllegalArgumentException for a <code>null</code> subject or resource
     * @throws IllegalStateException if the service was not initialized.
     */
    boolean isUserInRole(String appContext, AzSubject subject, AzResource resource, String role);

    /**
     * Find an existing <code>RoleDeploymentContext</code>, or create a new one if one does not
     * already exist for the specified application context.  The role deployment context will be
     * returned in an "open" state, and will stay that way until commit() or delete() is called.
     *
     * @param appContext The application context for which the <code>RoleDeploymentContext</code> is desired.
     * @return The resulting <code>RoleDeploymentContext</code> or <code>null</code> if the configured providers
     * do not support this feature.
     *
     * @throws IllegalStateException if the service was not initialized.
     */
    RoleDeploymentContext findOrCreateDeploymentContext(String appContext);

    /**
     * This interface represents a <code>RoleDeploymentContext</code> as returned by the Role Mapping
     * Service's findOrCreateDeploymentContext() method.  The <code>RoleDeploymentContext</code> is used
     * to configure role mapping policy for an application (or server administration) context.
     * It represents the body of policy that applies to the given context.
     *
     * A <code>RoleDeploymentContext</code> is always in one of three states: open, closed/inService,
     * or deleted.  When returned by the Role Mapping service, a context is in an open state.
     * Policies can be added or deleted while in the open state, but the context is not
     * in service.  Upon calling commit(), the context is closed and the policies are place
     * in service.  Upon calling delete(), the context is taken out of service and the policies
     * are deleted from the Role Mapping Provider.
     */
    public interface RoleDeploymentContext {

        void addMapping(String role, String[] users, String[] groups);

        void removeMapping(String role, String[] users, String[] groups);

        void removeRole(String role);

        void commit();

        void delete();
    }
}
