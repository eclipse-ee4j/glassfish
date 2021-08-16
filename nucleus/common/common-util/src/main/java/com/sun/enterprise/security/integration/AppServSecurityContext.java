/*
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

package com.sun.enterprise.security.integration;

import java.security.Principal;

import javax.security.auth.Subject;

import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Contract;

/**
 * The SecurityContext Interface, also provide factory methods
 *
 */
@Contract
@PerLookup
public interface AppServSecurityContext {

    /**
     * This method should be implemented by the subclasses to return the caller principal. This information may be redundant since
     * the same information can be inferred by inspecting the Credentials of the caller.
     *
     * @return The caller Principal.
     */
    Principal getCallerPrincipal();

    /**
     * This method should be implemented by the subclasses to return the Credentials of the caller principal.
     *
     * @return A credentials object associated with the current client invocation.
     */
    Subject getSubject();

    /**
     * @return a new instance
     */
    AppServSecurityContext newInstance(String userName, Subject subject, String realm);

    /**
     * @return a new instance
     */
    AppServSecurityContext newInstance(String userName, Subject subject);

    /**
     * set the current security context
     */
    void setCurrentSecurityContext(AppServSecurityContext context);

    /**
     * @return the current security context
     */
    AppServSecurityContext getCurrentSecurityContext();

    /**
     * set the unauthenticated context
     */
    void setUnauthenticatedSecurityContext();

    /**
     * set the SecurityContext with given Principal
     */
    void setSecurityContextWithPrincipal(Principal principal);
}
