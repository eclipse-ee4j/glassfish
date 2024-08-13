/*
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

package org.glassfish.security.services.api.authentication;

import jakarta.inject.Inject;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.internal.api.InternalSystemAdministrator;

/**
 * Implements most of the internal system administrator.
 * <p>
 * Different concrete subclasses implement {@link #getAdminGroupName() }
 * and {@link #getInternalUsername() } and {@link #createSubject() } differently.
 *
 * @author tjquinn
 */
public abstract class AbstractInternalSystemAdministrator implements InternalSystemAdministrator, PostConstruct {

    private Subject subject;

    @Inject
    private AuthenticationService authService;

    @Override
    public void postConstruct() {
        subject = createSubject();
    }

    @Override
    public Subject getSubject() {
        return subject;
    }

    /**
     * Creates a subject using the impersonate method on the authentication
     * service.
     *
     * @return the Subject to use for the internal system administrator.
     */
    protected Subject createSubject() {
        Subject s;
        try {
            s = authService.impersonate(
                    getInternalUsername(),
                    new String[] {getAdminGroupName()},
                    null /* no pre-existing subject */,
                    true /* isVirtual */);
            return s;
        } catch (LoginException ex) {
            throw new RuntimeException(ex);
        }
    }

    abstract protected String getInternalUsername();

    abstract protected String getAdminGroupName();
}
