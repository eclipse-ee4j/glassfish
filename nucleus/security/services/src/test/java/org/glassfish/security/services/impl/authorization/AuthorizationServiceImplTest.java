/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation.
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
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;

import org.glassfish.security.services.api.authorization.AzAction;
import org.glassfish.security.services.api.authorization.AzAttributeResolver;
import org.glassfish.security.services.api.authorization.AzAttributes;
import org.glassfish.security.services.api.authorization.AzEnvironment;
import org.glassfish.security.services.api.common.Attribute;
import org.glassfish.security.services.impl.common.AttributeImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.glassfish.security.services.impl.authorization.AuthorizationServiceImpl.InitializationState.FAILED_INIT;
import static org.glassfish.security.services.impl.authorization.AuthorizationServiceImpl.InitializationState.NOT_INITIALIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @see AuthorizationServiceImpl
 */
public class AuthorizationServiceImplTest {

    private AuthorizationServiceImpl impl;

    @BeforeEach
    public void setUp() throws Exception {
        impl = new AuthorizationServiceImpl();
    }


    @AfterEach
    public void tearDown() throws Exception {
        impl = null;
    }


    @Test
    public void testInitialize() throws Exception {
        assertSame(NOT_INITIALIZED, impl.getInitializationState(), "NOT_INITIALIZED");
        assertThrows(RuntimeException.class, () -> impl.initialize(null));
        assertSame(FAILED_INIT, impl.getInitializationState(), "FAILED_INIT");
        assertNotNull("getReasonInitializationFailed", impl.getReasonInitializationFailed());
    }

    @Test
    public void testIsAuthorized() throws Exception {
        assertSame(NOT_INITIALIZED, impl.getInitializationState(), "NOT_INITIALIZED");
        assertThrows(RuntimeException.class,
            () -> impl.isAuthorized(new Subject(), new URI("admin:///tenants/tenant/mytenant"), "update"));
        assertSame(NOT_INITIALIZED, impl.getInitializationState(), "NOT_INITIALIZED");
        assertNotNull(impl.getReasonInitializationFailed(), "getReasonInitializationFailed");
    }


    @Test
    public void testGetAuthorizationDecision() throws Exception {
        assertSame(NOT_INITIALIZED, impl.getInitializationState(), "NOT_INITIALIZED");
        final AzSubjectImpl subject = new AzSubjectImpl(new Subject());
        final AzResourceImpl resource = new AzResourceImpl(new URI("admin:///tenants/tenant/mytenant"));
        final AzActionImpl action = new AzActionImpl("update");
        assertThrows(RuntimeException.class, () -> impl.getAuthorizationDecision(subject, resource, action));
        assertSame(NOT_INITIALIZED, impl.getInitializationState(), "NOT_INITIALIZED");
        assertNotNull(impl.getReasonInitializationFailed(), "getReasonInitializationFailed");
    }


    @Test
    public void testMakeAzSubject() throws Exception {
        assertThrows(RuntimeException.class, () -> impl.makeAzSubject(null));
        Subject subject = new Subject();
        assertSame(subject, impl.makeAzSubject(subject).getSubject(), "Subject");
    }


    @Test
    public void testMakeAzResource() throws Exception {
        assertThrows(RuntimeException.class, () -> impl.makeAzResource(null));
        final URI uri = new URI("admin:///");
        assertSame(uri, impl.makeAzResource(uri).getUri(), "URI");
    }


    @Test
    public void testMakeAzAction() throws Exception {
        {
            AzAction azAction = impl.makeAzAction(null);
            assertNotNull(azAction);
            assertNull(azAction.getAction());
        }
        {
            String action = "update";
            AzAction azAction = impl.makeAzAction(action);
            assertNotNull(azAction);
            assertEquals(action, azAction.getAction(), "action");
        }
    }


    @Test
    public void testFindOrCreateDeploymentContext() throws Exception {
        assertSame(NOT_INITIALIZED, impl.getInitializationState(), "NOT_INITIALIZED");
        assertThrows(RuntimeException.class, () -> impl.findOrCreateDeploymentContext("foo"));
        assertSame(NOT_INITIALIZED, impl.getInitializationState(), "NOT_INITIALIZED");
        assertNotNull("getReasonInitializationFailed", impl.getReasonInitializationFailed());
    }


    @Test
    public void testAttributeResolvers() throws Exception {
        assertEquals(0, impl.getAttributeResolvers().size(), "initial");

        final AzAttributeResolver testAr1 = new TestAttributeResolver(new AttributeImpl("1"));
        final AzAttributeResolver testAr2 = new TestAttributeResolver(new AttributeImpl("2"));

        assertTrue(impl.appendAttributeResolver(testAr1), "append 1");
        assertFalse(impl.appendAttributeResolver(testAr1), "append 1");
        assertTrue(impl.appendAttributeResolver(testAr2), "append 2");
        assertFalse(impl.appendAttributeResolver(testAr2), "append 2");

        List<AzAttributeResolver> arList = impl.getAttributeResolvers();
        assertThat("size after append", arList, hasSize(2));
        assertEquals("1", arList.get(0).resolve(null, null, null).getName(), "append 1");
        assertEquals("2", arList.get(1).resolve(null, null, null).getName(), "append 2");

        final AzAttributeResolver testAr3 = new TestAttributeResolver(new AttributeImpl("3"));
        final AzAttributeResolver testAr4 = new TestAttributeResolver(new AttributeImpl("4"));
        List<AzAttributeResolver> tempList = new ArrayList<>();
        tempList.add(testAr3);
        tempList.add(testAr4);
        impl.setAttributeResolvers(tempList);

        List<AzAttributeResolver> arList2 = impl.getAttributeResolvers();
        assertThat("after get list 2", arList2, hasSize(2));
        assertEquals("3", arList2.get(0).resolve(null, null, null).getName(), "append 3");
        assertEquals("4", arList2.get(1).resolve(null, null, null).getName(), "append 4");

        assertTrue(impl.removeAllAttributeResolvers(), "removeAllAttributeResolvers");
        assertFalse(impl.removeAllAttributeResolvers(), "removeAllAttributeResolvers");
        assertThat("final", impl.getAttributeResolvers(), hasSize(0));
    }

    /**
     * Fake test class
     */
    private static class TestAttributeResolver implements AzAttributeResolver {

        final Attribute attr;

        private TestAttributeResolver(Attribute attr) {
            this.attr = attr;
        }

        @Override
        public Attribute resolve(
            String attributeName,
            AzAttributes collection,
            AzEnvironment environment) {
            return this.attr;
        }
    }
}
