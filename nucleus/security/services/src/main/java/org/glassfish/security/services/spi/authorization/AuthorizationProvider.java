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

import org.glassfish.security.services.api.authorization.AuthorizationService;
import org.glassfish.security.services.api.authorization.AzAction;
import org.glassfish.security.services.api.authorization.AzAttributeResolver;
import org.glassfish.security.services.api.authorization.AzEnvironment;
import org.glassfish.security.services.api.authorization.AzResource;
import org.glassfish.security.services.api.authorization.AzResult;
import org.glassfish.security.services.api.authorization.AzSubject;
import org.glassfish.security.services.spi.SecurityProvider;
import org.jvnet.hk2.annotations.Contract;

/**
 * <code>AuthorizationProvider</code> instances are used by a
 * <code>{@link org.glassfish.security.services.api.authorization.AuthorizationService}</code>
 * to make access authorization decisions. This is part of a plug-in mechanism,
 * which allows access decisions to deferred to an configured implementation.
 */
@Contract
public interface AuthorizationProvider extends SecurityProvider {


    /**
     * Evaluates the specified subject, resource, action, and environment against the body of
     * policy managed by this provider and returns an access control result.
     *
     * @param subject The attributes collection representing the Subject for which an authorization
     * decision is requested.
     * @param resource The attributes collection representing the resource for which access is
     * being requested.
     * @param action  The attributes collection representing the action, with respect to the resource,
     * for which access is being requested.  A null action is interpreted as all
     * actions, however all actions may also be represented by the AzAction instance.
     * See <code>{@link org.glassfish.security.services.api.authorization.AzAction}</code>.
     * @param environment The attributes collection representing the environment, or context,
     *                    in which the access decision is being requested, null if none.
     * @param attributeResolvers The ordered list of attribute resolvers, for
     * run time determination of missing attributes, null if none.
     * @return The AzResult indicating the result of the access decision.
     * @throws IllegalArgumentException Given null or illegal subject or resource
     * @throws IllegalStateException Provider was not initialized.
     * @see AuthorizationService#getAuthorizationDecision
     */
    AzResult getAuthorizationDecision(
        AzSubject subject,
        AzResource resource,
        AzAction action,
        AzEnvironment environment,
        List<AzAttributeResolver> attributeResolvers );


    /**
     * Finds an existing PolicyDeploymentContext, or create a new one if one does not
     * already exist for the specified appContext.  The context will be returned in
     * an "open" state, and will stay that way until commit() or delete() is called.
     *
     * @param appContext The application context for which the PolicyDeploymentContext
     * is desired.
     * @return The resulting PolicyDeploymentContext,
     * null if this provider does not support this feature.
     * @throws IllegalStateException Provider was not initialized, if this method is supported.
     * @see AuthorizationService#findOrCreateDeploymentContext(String)
     */
    AuthorizationService.PolicyDeploymentContext findOrCreateDeploymentContext(
        String appContext);
}
