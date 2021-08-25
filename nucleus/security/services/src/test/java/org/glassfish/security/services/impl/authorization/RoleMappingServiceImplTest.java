/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.security.services.impl.authorization;

import java.net.URI;

import javax.security.auth.Subject;

import org.glassfish.security.services.api.authorization.AuthorizationService;
import org.glassfish.security.services.api.authorization.AzResource;
import org.glassfish.security.services.api.authorization.AzSubject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.glassfish.security.services.impl.authorization.RoleMappingServiceImpl.InitializationState.FAILED_INIT;
import static org.glassfish.security.services.impl.authorization.RoleMappingServiceImpl.InitializationState.NOT_INITIALIZED;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @see RoleMappingServiceImpl
 */
public class RoleMappingServiceImplTest {

    // Use Authorization for creating the Az typed arguments on Role Service
    private final AuthorizationService authorizationService = new AuthorizationServiceImpl();
    private RoleMappingServiceImpl impl;

    @BeforeEach
    public void setUp() throws Exception {
        impl = new RoleMappingServiceImpl();
    }


    @AfterEach
    public void tearDown() throws Exception {
        impl = null;
    }


    @Test
    public void testInitialize() throws Exception {
        assertSame(NOT_INITIALIZED, impl.getInitializationState(), "NOT_INITIALIZED");
        impl.initialize(null);

        AzSubject azSubject = authorizationService.makeAzSubject(new Subject());
        AzResource azResource = authorizationService.makeAzResource(URI.create("test://test"));
        assertThrows(IllegalStateException.class, () -> impl.isUserInRole("test", azSubject, azResource, "aRole"));

        // The service will fail internally to prevent method calls
        assertSame(FAILED_INIT, impl.getInitializationState(), "FAILED_INIT");
        assertNotNull(impl.getReasonInitializationFailed(), "getReasonInitializationFailed");
    }


    @Test
    public void testIsUserRole() throws Exception {
        assertSame(NOT_INITIALIZED, impl.getInitializationState(), "NOT_INITIALIZED");
        AzSubject azSubject = authorizationService.makeAzSubject(new Subject());
        AzResource azResource = authorizationService.makeAzResource(URI.create("test://test"));
        assertThrows(RuntimeException.class, () -> impl.isUserInRole("test", azSubject, azResource, "aRole"));

        assertSame(NOT_INITIALIZED, impl.getInitializationState(), "NOT_INITIALIZED");
        assertNotNull(impl.getReasonInitializationFailed(), "getReasonInitializationFailed");
    }


    @Test
    public void testIsUserRoleNoAzArgs() throws Exception {
        assertSame(NOT_INITIALIZED, impl.getInitializationState(), "NOT_INITIALIZED");
        assertThrows(RuntimeException.class,
            () -> impl.isUserInRole("test", new Subject(), URI.create("test://test"), "aRole"));

        assertSame(NOT_INITIALIZED, impl.getInitializationState(), "NOT_INITIALIZED");
        assertNotNull(impl.getReasonInitializationFailed(), "getReasonInitializationFailed");
    }


    @Test
    public void testIsUserRoleNullArgs() throws Exception {
        assertThrows(IllegalArgumentException.class,
            () -> impl.isUserInRole("test", null, authorizationService.makeAzResource(URI.create("test://test")), "aRole"));
        assertThrows(IllegalArgumentException.class,
            () -> impl.isUserInRole("test", authorizationService.makeAzSubject(new Subject()), null, "aRole"));
    }


    @Test
    public void testIsUserRoleNoAzArgsNullArgs() throws Exception {
        assertThrows(IllegalArgumentException.class,
            () -> impl.isUserInRole("test", null, URI.create("test://test"), "aRole"));
        assertThrows(IllegalArgumentException.class,
            () -> impl.isUserInRole("test", new Subject(), null, "aRole"));
    }


    @Test
    public void testFindOrCreateDeploymentContext() throws Exception {
        assertSame(NOT_INITIALIZED, impl.getInitializationState(), "NOT_INITIALIZED");
        assertThrows(RuntimeException.class, () -> impl.findOrCreateDeploymentContext("test"));
        assertSame(NOT_INITIALIZED, impl.getInitializationState(), "NOT_INITIALIZED");
        assertNotNull(impl.getReasonInitializationFailed(), "getReasonInitializationFailed");
    }
}
