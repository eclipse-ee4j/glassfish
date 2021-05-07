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

package org.glassfish.security.services.impl.authorization;

import java.net.URI;
import javax.security.auth.Subject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.*;

import org.glassfish.security.services.api.authorization.AuthorizationService;
import static org.glassfish.security.services.impl.authorization.RoleMappingServiceImpl.InitializationState.*;

/**
 * @see RoleMappingServiceImpl
 */
public class RoleMappingServiceImplTest {

    // Use Authorization for creating the Az typed arguments on Role Service
    private final AuthorizationService authorizationService = new AuthorizationServiceImpl();
    private RoleMappingServiceImpl impl;

    @Before
    public void setUp() throws Exception {
        impl = new RoleMappingServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
        impl = null;
    }

    @Test
    public void testInitialize() throws Exception {
        assertSame( "NOT_INITIALIZED", NOT_INITIALIZED, impl.getInitializationState() );
        try {
            impl.initialize(null);
        } catch ( RuntimeException e ) {
            fail( "Expected service to allow no specified configuration" );
        }

        try {
            impl.isUserInRole("test",
                authorizationService.makeAzSubject(new Subject()),
                authorizationService.makeAzResource(URI.create("test://test")),
                "aRole");
            fail( "Expected fail illegal state exception." );
        } catch ( IllegalStateException e ) {
            assertNotNull("Service fails at run-time", e);
        }

        // The service will fail internally to prevent method calls
        assertSame( "FAILED_INIT", FAILED_INIT, impl.getInitializationState() );
        assertNotNull( "getReasonInitializationFailed", impl.getReasonInitializationFailed() );
    }

    @Test
    public void testIsUserRole() throws Exception {
        assertSame( "NOT_INITIALIZED", NOT_INITIALIZED, impl.getInitializationState() );
        try {
            impl.isUserInRole("test",
                authorizationService.makeAzSubject(new Subject()),
                authorizationService.makeAzResource(URI.create("test://test")),
                "aRole");
            fail( "Expected fail not initialized." );
        } catch ( RuntimeException e ) {
        }

        assertSame("NOT_INITIALIZED", NOT_INITIALIZED, impl.getInitializationState());
        assertNotNull( "getReasonInitializationFailed", impl.getReasonInitializationFailed() );
    }

    @Test
    public void testIsUserRoleNoAzArgs() throws Exception {
        assertSame( "NOT_INITIALIZED", NOT_INITIALIZED, impl.getInitializationState() );
        try {
            impl.isUserInRole("test",
                new Subject(),
                URI.create("test://test"),
                "aRole");
            fail( "Expected fail not initialized." );
        } catch ( RuntimeException e ) {
        }

        assertSame("NOT_INITIALIZED", NOT_INITIALIZED, impl.getInitializationState());
        assertNotNull( "getReasonInitializationFailed", impl.getReasonInitializationFailed() );
    }

    @Test
    public void testIsUserRoleNullArgs() throws Exception {
        // Arguments checked before service state
        try {
            impl.isUserInRole("test",
                null,
                authorizationService.makeAzResource(URI.create("test://test")),
                "aRole");
            fail( "Expected fail illegal argument." );
        } catch ( IllegalArgumentException e ) {
            assertNotNull("Subject null test", e);
        }
        try {
            impl.isUserInRole("test",
                authorizationService.makeAzSubject(new Subject()),
                null,
                "aRole");
            fail( "Expected fail illegal argument." );
        } catch ( IllegalArgumentException e ) {
            assertNotNull("Resource null test", e);
        }
    }

    @Test
    public void testIsUserRoleNoAzArgsNullArgs() throws Exception {
        // Arguments checked before service state
        try {
            impl.isUserInRole("test",
                null,
                URI.create("test://test"),
                "aRole");
            fail( "Expected fail illegal argument." );
        } catch ( IllegalArgumentException e ) {
            assertNotNull("Subject null test", e);
        }
        try {
            impl.isUserInRole("test",
                new Subject(),
                null,
                "aRole");
            fail( "Expected fail illegal argument." );
        } catch ( IllegalArgumentException e ) {
            assertNotNull("Subject null test", e);
        }
    }

    @Test
    public void testFindOrCreateDeploymentContext() throws Exception {
        assertSame( "NOT_INITIALIZED", NOT_INITIALIZED, impl.getInitializationState() );
        try {
            impl.findOrCreateDeploymentContext("test");
            fail( "Expected fail not initialized." );
        } catch ( RuntimeException e ) {
        }

        assertSame("NOT_INITIALIZED", NOT_INITIALIZED, impl.getInitializationState());
        assertNotNull("getReasonInitializationFailed", impl.getReasonInitializationFailed());
    }
}
