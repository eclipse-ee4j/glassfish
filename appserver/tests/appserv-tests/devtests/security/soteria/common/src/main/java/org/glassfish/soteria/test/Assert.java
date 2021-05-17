/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.soteria.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.List;
import com.gargoylesoftware.htmlunit.WebResponse;

public final class Assert {

    public static void assertDefaultAuthenticated(String response) {
        assertAuthenticated("web", "reza", response, "foo", "bar");
    }

    public static void assertDefaultAuthenticated(WebResponse response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertAuthenticated("web", "reza", response.getContentAsString(), "foo", "bar");
    }

    public static void assertDefaultNotAuthenticated(String response) {
        assertNotAuthenticated("web", "reza", response, "foo", "bar");
    }

    public static void assertDefaultNotAuthenticated(WebResponse response) {
        assertNotNull(response);
        assertEquals(401, response.getStatusCode());
        assertNotAuthenticated("web", "reza", response.getContentAsString(), "foo", "bar");
    }

    public static void assertDefaultNotAuthenticatedUnprotected(WebResponse response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotAuthenticatedUnprotected("web", "null", response.getContentAsString(), new ArrayList<String>());
    }

    public static void assertNotAuthenticatedError(WebResponse response) {
        assertNotNull(response);
        assertEquals(500, response.getStatusCode());
    }

    public static void assertApplicationPrincipalAndContainerPrincipalName(WebResponse response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertApplicationPrincipalAndContainerPrincipalSubject("reza", "foo", response
                .getContentAsString());
    }

    public static void assertBundledHAMPrecedenceOverLoginConfig(WebResponse response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertBundledHAMPrecedenceOverLoginConfig("reza", "foo", response
                .getContentAsString());
    }

    public static void assertBothContainerAndApplicationPrincipalsAreSame(WebResponse response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertBothContainerAndApplicationPrincipalsAreSame("reza", "foo", response
                .getContentAsString());
    }

    public static void assertAuthenticated(String userType, String name, String response, String... roles) {
        assertTrue(
            "Should be authenticated as user " + name + " but was not \n Response: \n" +
            response + "\n search: " + userType + " username: " + name,
            response.contains(userType + " username: " + name));

        for (String role : roles) {
            assertTrue(
                "Authenticated user should have role \"" + role + "\", but did not \n Response: \n" +
                response,
                response.contains(userType + " user has role \"" + role + "\": true"));
        }
    }

    public static void assertNotAuthenticated(String userType, String name, String response, String... roles) {
        assertFalse(
            "Should not be authenticated as user " + name + " but was \n Response: \n" +
            response + "\n search: " + userType + " username: " + name,
            response.contains(userType + " username: " + name));

        for (String role : roles) {
            assertFalse(
                "Authenticated user should not have role \"" + role + "\", but did \n Response: \n" +
                response,
                response.contains(userType + " user has role \"" + role + "\": true"));
        }
     }

    public static void assertAuthenticatedRoles(String userType, String response, String... roles) {
        for (String role : roles) {
            assertTrue(
                    "Authenticated user should have role \"" + role + "\", but did not \n Response: \n" +
                            response,
                    response.contains(userType + " has role \"" + role + "\": true"));
        }
    }

    public static void assertNotAuthenticatedRoles(String userType, String name, String response, String... roles) {

        for (String role : roles) {
            assertFalse(
                    "Authenticated user should not have role \"" + role + "\", but did \n Response: \n" +
                            response,
                    response.contains(userType + " has role \"" + role + "\": true"));
        }
    }
    public static void assertNotAuthenticatedUnprotected(String userType, String name, String response, List<String> roles) {
        assertTrue(
                "Should not be authenticated as user " + name + " but was \n Response: \n" +
                        response + "\n search: " + userType + " username: " + name,
                response.contains(userType + " username: " + name));

        for (String role : roles) {
            assertFalse(
                    "Authenticated user should not have role \"" + role + "\", but did \n Response: \n" +
                            response,
                    response.contains(userType + " user has role \"" + role + "\": true"));
        }
    }

    public static void assertHasAccessToResource(String userType, String name, String resource, String response) {
        assertTrue(
                "user " + name + " should have access to resource "+ resource +" but was not \n Response: \n" +
                        response,
                response.contains(userType + " user has access to " + resource + ": true"));
    }

    public static void assertNotHasAccessToResource(String userType, String name, String resource, String response) {
        assertFalse(
                "user " + name + " should have access to resource "+ resource +" but was not \n Response: \n" +
                        response,
                response.contains(userType + " user has access to " + resource + ": true"));
    }

    public static void assertBundledHAMPrecedenceOverLoginConfig(String name, String role, String response) {
        assertTrue(
                "For " + name + " authentication should have been performed by TestAuthenticationMechanism, but wasn't. \n" +
                        "+ Response: \n" +
                        response,
                response.contains(String.format("Authentication Mechanism:TestAuthenticationMechanism")));
    }

    public static void assertApplicationPrincipalAndContainerPrincipalSubject(String name, String role, String response) {
        assertTrue(
                "Both application principal's and container principal's name should have been same as "
                        + " but was not. \n Response: \n" +
                        response,
                response.contains(String.format("Container caller principal and application caller principal both are " +
                        "represented by same principal for user %s and is in role %s", name, role)));
    }

    public static void assertBothContainerAndApplicationPrincipalsAreSame(String name, String role, String response) {
        assertTrue(
                "For user " + name + " both container caller principal and application caller principal should have been same, " +
                        "but " +
                        "wasn't. \n" +
                        "+ Response: \n" +
                        response,
                response.contains(String.format("Both container caller principal and application caller principals are one and " +
                                "the same for user %s in role %s",
                        name, role)));
    }

}
