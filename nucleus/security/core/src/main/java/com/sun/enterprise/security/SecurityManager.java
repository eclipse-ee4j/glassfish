/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security;

import java.lang.reflect.Method;
import java.security.Principal;
import javax.security.auth.Subject;
import org.glassfish.api.invocation.ComponentInvocation;

/**
 * This interface is used by the Container to manage access to EJBs. The container has a reference to an implementation of this
 * interface.
 *
 * @author Harish Prabandham
 */
public interface SecurityManager {

    /**
     * @param componentInvocation The Invocation object containing the details of the invocation.
     * @return true if the client is allowed to invoke the EJB, false otherwise.
     */
    boolean authorize(ComponentInvocation componentInvocation);

    /**
     * @return The Principal of the client who made the current invocation.
     */
    Principal getCallerPrincipal();

    /**
     * @return A boolean true/false depending on whether or not the caller has the specified role.
     * @param The EJB developer specified "logical role".
     */
    boolean isCallerInRole(String role);

    /**
     * This sets up the security context - if not set and does run-as related login if required
     *
     * @param componentInvocation The Invocation object containing the details of the invocation.
     */
    void preInvoke(ComponentInvocation componentInvocation);

    Object invoke(Object bean, Method beanClassMethod, Object[] methodParameters) throws Throwable;

    /**
     * This method is used by the Invocation Manager to remove the run-as identity information that was set up using the preInvoke
     *
     * @param componentInvocation The Invocation object containing the details of the invocation.
     */
    void postInvoke(ComponentInvocation componentInvocation);

    /**
     * Call this method to clean up all the bookeeping data-structures in the SM.
     */
    void destroy();

    /**
     * This will return the subject associated with the current call. If the run as subject is in effect. It will return that
     * subject. This is done to support the JACC specification which says if the runas principal is in effect, that principal should
     * be used for making a component call.
     *
     * @return Subject the current subject. Null if this is not the runas case
     */
    Subject getCurrentSubject();

    /**
     * Purge ThreadLocals held by jakarta.security.jacc.PolicyContext
     */
    void resetPolicyContext();
}
