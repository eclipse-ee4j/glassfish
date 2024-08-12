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

package org.glassfish.security.services.impl;

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.security.AccessController;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.security.services.api.authentication.AuthenticationService;
import org.glassfish.security.services.common.PrivilegedLookup;
import org.glassfish.security.services.common.Secure;
import org.glassfish.security.services.common.SecurityScope;
import org.glassfish.security.services.common.StateManager;

/**
 * The factory of AuthenticationService instances used by the SecurityScopeContext.
 */
@Singleton
@Secure(accessPermissionName = "security/service/authentication")
public class AuthenticationServiceFactory extends ServiceFactory implements Factory<AuthenticationService> {

    @Inject
    private StateManager manager;

    @Inject
    private ServiceLocator serviceLocator;

    @SecurityScope
    public AuthenticationService provide() {
        String currentState = manager.getCurrent();

        // Get Service Instance
        AuthenticationService atnService = AccessController.doPrivileged(
                new PrivilegedLookup<AuthenticationService>(
                        serviceLocator, AuthenticationService.class));

        // Get Service Configuration
        org.glassfish.security.services.config.AuthenticationService atnConfiguration =
            serviceLocator.getService(org.glassfish.security.services.config.AuthenticationService.class,currentState);

        // Initialize Service
        atnService.initialize(atnConfiguration);

        return atnService;
    }

    @Override
    public void dispose(AuthenticationService instance) {
    }

    /**
     * Helper function to obtain the Authentication Service configuration from the Domain.
     */
    public static org.glassfish.security.services.config.AuthenticationService getAuthenticationServiceConfiguration(Domain domain) {
                org.glassfish.security.services.config.AuthenticationService atnConfiguration =
                ServiceFactory.getSecurityServiceConfiguration(
                                domain, org.glassfish.security.services.config.AuthenticationService.class);
        return atnConfiguration;
        }
}
