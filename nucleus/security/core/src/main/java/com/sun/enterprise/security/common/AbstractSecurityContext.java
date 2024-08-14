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

package com.sun.enterprise.security.common;

import com.sun.enterprise.security.integration.AppServSecurityContext;

import java.io.Serializable;
import java.security.Principal;

import javax.security.auth.Subject;

/**
 * This base class defines the methods that Security Context should exhibit. There are two places where a derived class
 * are used. They are on the appclient side and ejb side. The derived classes can use thread local storage to store the
 * security contexts.
 *
 * @author Harpreet Singh
 */
public abstract class AbstractSecurityContext implements AppServSecurityContext, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The principal that this security context represents
     */
    protected Principal initiator;
    protected Subject subject;

    /**
     * This method should be implemented by the subclasses to return the caller principal. This information may be redundant
     * since the same information can be inferred by inspecting the Credentials of the caller.
     *
     * @return The caller Principal.
     */
    @Override
    abstract public Principal getCallerPrincipal();

    /**
     * This method should be implemented by the subclasses to return the Credentials of the caller principal.
     *
     * @return A credentials object associated with the current client invocation.
     */
    @Override
    abstract public Subject getSubject();
}
