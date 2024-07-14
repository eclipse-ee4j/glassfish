/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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

import jakarta.inject.Inject;

import java.net.URI;

import javax.security.auth.Subject;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.security.common.Group;
import org.glassfish.security.common.UserNameAndPassword;
import org.glassfish.security.services.impl.authorization.AuthorizationServiceImpl;
import org.glassfish.security.services.spi.authorization.RoleMappingProvider;
import org.glassfish.tests.utils.junit.hk2.HK2JUnit5Extension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(HK2JUnit5Extension.class)
public class SimpleRoleProviderTest {

    @Inject
    private ServiceLocator locator;
    private RoleMappingProvider simpleRoleProvider;
    private final AuthorizationService authorizationService = new AuthorizationServiceImpl();

    @BeforeEach
    public void before() {
        simpleRoleProvider = locator.getService(RoleMappingProvider.class, "simpleRoleMapping");
    }

    @Test
    public void testProviderAdmin() throws Exception {
        assertNotNull(simpleRoleProvider);
        final AzSubject azSubject = authorizationService.makeAzSubject(adminSubject());
        final AzResource azResource = authorizationService.makeAzResource(URI.create("admin://my/respath"));
        boolean result = simpleRoleProvider.isUserInRole(null, azSubject, azResource, "Admin", null, null);
        assertTrue(result);
    }

    @Test
    public void testProviderNonAdmin() throws Exception {
        assertNotNull(simpleRoleProvider);
        AzSubject azSubject = authorizationService.makeAzSubject(nonAdminSubject());
        AzResource azResource = authorizationService.makeAzResource(URI.create("admin://negative"));
        boolean result = simpleRoleProvider.isUserInRole(null, azSubject, azResource, "Admin", null, null);
        assertFalse(result);
    }
    @Test
    public void testProviderNonAdminRole() throws Exception {
        assertNotNull(simpleRoleProvider);
        AzSubject azSubject = authorizationService.makeAzSubject(adminSubject());
        AzResource azResource = authorizationService.makeAzResource(URI.create("foo://other"));
        // Warning Message
        boolean result = simpleRoleProvider.isUserInRole(null, azSubject, azResource, "otherRole", null, null);
        assertFalse(result);
    }

    private Subject adminSubject() {
        Subject result = new Subject();
        result.getPrincipals().add(new UserNameAndPassword("admin"));
        result.getPrincipals().add(new Group("asadmin"));
        return result;
    }

    private Subject nonAdminSubject() {
        Subject result = new Subject();
        result.getPrincipals().add(new UserNameAndPassword("joe"));
        result.getPrincipals().add(new Group("myGroup"));
        return result;
    }
}
