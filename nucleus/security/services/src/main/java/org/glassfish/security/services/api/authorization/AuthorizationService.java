/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.util.List;
import javax.security.auth.Subject;
import org.glassfish.security.services.api.SecurityService;
import org.jvnet.hk2.annotations.Contract;

/**
 * The <code>AuthorizationService</code> interface provides methods that allow server and container
 * to determine whether access should be allowed to a particular resource.
 * It is intended for internal use, not for use by applications.
 */
@Contract
public interface AuthorizationService extends SecurityService {

    /**
     * Determines whether the given Subject is authorized to access the given resource,
     * specified by a URI.
     *
     * @param subject The Subject being tested.
     * @param resource URI of the resource being tested.
     * @return True or false, depending on whether the access is authorized.
     * @throws IllegalArgumentException Given null or illegal subject or resource
     * @throws IllegalStateException Service was not initialized.
     */
    boolean isAuthorized(Subject subject, URI resource);

    /**
     * Determines whether the given Subject is authorized to access the given resource,
     * specified by a URI.
     *
     * @param subject The Subject being tested.
     * @param resource URI of the resource being tested.
     * @param action The action, with respect to the resource parameter,
     * for which authorization is desired. To check authorization for all actions,
     * action is represented by null or "*".
     * @return True or false, depending on whether the access is authorized.
     * @throws IllegalArgumentException Given null or illegal subject or resource
     * @throws IllegalStateException Service was not initialized.
     */
    boolean isAuthorized(Subject subject, URI resource, String action);

    /**
     * The primary authorization method.  The isAuthorized() methods call this method
     * after converting their arguments into the appropriate attribute collection type.
     * It returns a full AzResult, including authorization status, decision, and
     * obligations.
     *
     * This method performs two steps prior to invoking the configured AuthorizationProvider
     * to evaluate the request:  First, it acquires the current AzEnvironment attributes by
     * calling the Security Context service.  Second, it calls the Role Mapping service to
     * determine which roles the subject has, and adds the resulting role attributes into
     * the AzSubject.
     *
     * @param subject The attributes collection representing the Subject for which an authorization
     * decision is requested.
     * @param resource The attributes collection representing the resource for which access is
     * being requested.
     * @param action  The attributes collection representing the action, with respect to the resource,
     * for which access is being requested.  A null action is interpreted as all
     * actions, however all actions may also be represented by the AzAction instance.
     * See <code>{@link org.glassfish.security.services.api.authorization.AzAction}</code>.
     * @return The AzResult indicating the result of the access decision.
     * @throws IllegalArgumentException Given null or illegal subject or resource
     * @throws IllegalStateException Service was not initialized.
     */
    AzResult getAuthorizationDecision(AzSubject subject, AzResource resource, AzAction action);

    /**
     * Converts a Java Subject into a typed attributes collection.
     *
     * @param subject The Subject to convert.
     * @return The resulting AzSubject.
     * @throws IllegalArgumentException Given null or illegal subject
     */
    AzSubject makeAzSubject(Subject subject);

    /**
     * Converts a resource, expressed as a URI, into a typed attributes collection.
     * <p>
     * Query parameters in the given URI are appended to this
     * <code>AzResource</code> instance attributes collection.
     *
     * @param resource The URI to convert.
     * @return The resulting AzResource.
     * @throws IllegalArgumentException Given null or illegal resource
     */
    AzResource makeAzResource(URI resource);

    /**
     * Converts an action, expressed as a String, into a typed attributes collection.
     *
     * @param action The action to convert. null or "*" represents all actions.
     * @return The resulting AzAction.
     */
    AzAction makeAzAction(String action);

    // TODO: What if multiple providers? Rollback/closeWithoutChange? Would delete remove an existing PolicyDeploymentContext?
    /**
     * Finds an existing PolicyDeploymentContext, or create a new one if one does not
     * already exist for the specified appContext.  The context will be returned in
     * an "open" state, and will stay that way until commit() or delete() is called.
     *
     * @param appContext The application context for which the PolicyDeploymentContext
     * is desired.
     * @return The resulting PolicyDeploymentContext,
     * null if the configured providers do not support this feature.
     * @throws IllegalStateException Service was not initialized.
     */
    PolicyDeploymentContext findOrCreateDeploymentContext(String appContext);

    /**
     * This interface represents a PolicyDeploymentContext as returned by the Authorization
     * Service's findOrCreateDeploymentContext() method.  The PolicyDeploymentContext is used
     * to configure authorization policy for an application (or server administration) context.
     * It represents the body of policy that applies to the given context.
     *
     * A PolicyDeploymentContext is always in one of three states: open, closed/inService,
     * or deleted.  When returned by the Authorization service, a context is in an open state.
     * Policies can be added or deleted while in the open state, but the context is not
     * in service.  Upon calling commit(), the context is closed and the policies are place
     * in service.  Upon calling delete(), the context is taken out of service and the policies
     * are deleted from the Authorization Provider.
     */
    public interface PolicyDeploymentContext {

        void addRolePolicy(String role, String resource, String action);

        void addUncheckedPolicy(String resource, String action);

        void addExcludedPolicy(String resource, String action);

        void removeRolePolicy(String role);

        void removeRolePolicies();

        void removeUncheckedPolicies();

        void removeExcludedPolicies();

        void commit();

        void delete();
    }


    /**
     * Appends the given <code>{@link org.glassfish.security.services.api.authorization.AzAttributeResolver}</code>
     * instance to the internal ordered list of <code>AzAttributeResolver</code> instances,
     * if not currently in the list based on
     * <code>{@link org.glassfish.security.services.api.authorization.AzAttributeResolver#equals}</code>.
     *
     * @param resolver The <code>AzAttributeResolver</code> instance to append.
     * @return true if the <code>AzAttributeResolver</code> was added,
     * false if the <code>AzAttributeResolver</code> was already in the list.
     * @throws IllegalArgumentException Given AzAttributeResolver was null.
     */
    boolean appendAttributeResolver(AzAttributeResolver resolver);


    /**
     * Replaces the internal list of <code>AttributeResolver</code> instances
     * with the given list. If multiple equivalent instances exist in the given list,
     * only the first such instance will be inserted.
     *
     * @param resolverList Replacement list of <code>AzAttributeResolver</code> instances
     * @throws IllegalArgumentException Given AzAttributeResolver list was null.
     */
    void setAttributeResolvers(List<AzAttributeResolver> resolverList);


    /**
     * Determines the current list of <code>AttributeResolver</code> instances,
     * in execution order.
     *
     * @return  The current list of AttributeResolver instances,
     * in execution order.
     */
    List<AzAttributeResolver> getAttributeResolvers();


    /**
     * Removes all <code>AttributeResolver</code> instances from the current
     * internal list of <code>AttributeResolver</code> instances.
     *
     * @return true if any <code>AttributeResolver</code> instances were removed,
     * false if the list was empty.
     */
    boolean removeAllAttributeResolvers();

}
