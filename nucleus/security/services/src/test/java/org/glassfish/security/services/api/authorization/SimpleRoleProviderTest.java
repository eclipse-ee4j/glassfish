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

package org.glassfish.security.services.api.authorization;

import java.net.URI;
import javax.security.auth.Subject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.jvnet.hk2.testing.junit.HK2Runner;

import org.glassfish.security.common.Group;
import org.glassfish.security.common.PrincipalImpl;
import org.glassfish.security.services.impl.authorization.AuthorizationServiceImpl;
import org.glassfish.security.services.spi.authorization.RoleMappingProvider;


public class SimpleRoleProviderTest extends HK2Runner {

    private AuthorizationService authorizationService = new AuthorizationServiceImpl();
    private RoleMappingProvider simpleRoleProvider = null;

    @Before
    public void before() {
        super.before();

        simpleRoleProvider = testLocator.getService(RoleMappingProvider.class, "simpleRoleMapping");
    }

    @Test
    public void testProviderAdmin() throws Exception {
        Assert.assertNotNull(simpleRoleProvider);
        boolean result = simpleRoleProvider.isUserInRole(null,
                authorizationService.makeAzSubject(adminSubject()),
                authorizationService.makeAzResource(URI.create("admin://my/respath")),
                "Admin", null, null);
        Assert.assertEquals(true, result);
    }

    private Subject adminSubject() {
        Subject result = new Subject();
        result.getPrincipals().add(new PrincipalImpl("admin"));
        result.getPrincipals().add(new Group("asadmin"));
        return result;
    }

    @Test
    public void testProviderNonAdmin() throws Exception {
        Assert.assertNotNull(simpleRoleProvider);
        boolean result = simpleRoleProvider.isUserInRole(null,
                authorizationService.makeAzSubject(nonAdminSubject()),
                authorizationService.makeAzResource(URI.create("admin://negative")),
                "Admin", null, null);
        Assert.assertEquals(false, result);
    }

    private Subject nonAdminSubject() {
        Subject result = new Subject();
        result.getPrincipals().add(new PrincipalImpl("joe"));
        result.getPrincipals().add(new Group("myGroup"));
        return result;
    }

    @Test
    public void testProviderNonAdminRole() throws Exception {
        Assert.assertNotNull(simpleRoleProvider);
        boolean result = simpleRoleProvider.isUserInRole(null,
                authorizationService.makeAzSubject(adminSubject()),
                authorizationService.makeAzResource(URI.create("foo://other")), // Warning Message
                "otherRole", null, null);
        Assert.assertEquals(false, result);
    }
}
