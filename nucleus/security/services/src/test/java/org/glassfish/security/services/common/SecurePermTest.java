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

package org.glassfish.security.services.common;

import java.security.Permission;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SecurePermTest {

    @Test
    public void testEqualsObject() {
        Permission perm1 = new SecureServiceAccessPermission("a/b/c", "read,write");

        Permission perm2 = new SecureServiceAccessPermission("a/b/c/d", "read,write");
        assertNotEquals(perm1, perm2);

        Permission p3 = null;
        assertNotEquals(perm1, p3);

        Permission p5 = new SecureServiceAccessPermission("a/b/c");
        assertNotEquals(perm1, p5);
    }

    @Test
    public void testEquals1() {
        Permission p1 = new SecureServiceAccessPermission("a/b/c", "read,write");
        Permission p2 = new SecureServiceAccessPermission("a/b/c/", "read,write");
        assertNotEquals(p1, p2);
        assertFalse(p1.implies(p2));

    }

    @Test
    public void testImpliesPermission() {
        Permission p1 = new SecureServiceAccessPermission("a", "read");
        Permission p2 = new SecureServiceAccessPermission("b", "read");
        assertFalse(p1.implies(p2));

        Permission p3 = new SecureServiceAccessPermission("a", "read,write");
        assertTrue(p3.implies(p1));
    }

    @Test
    public void testImpliesWild() {
        Permission p1 = new SecureServiceAccessPermission("a/*", "read");

        Permission p2 = new SecureServiceAccessPermission("a/b", "read");
        assertTrue(p1.implies(p2));

        Permission p3 = new SecureServiceAccessPermission("a/b/c", "read");
        assertTrue(p1.implies(p3));


        Permission p11 = new SecureServiceAccessPermission("a/b/*", "read");
        assertTrue(p11.implies(p3));

        assertFalse(p11.implies(p1));
        assertFalse(p11.implies(p2));

        assertTrue(p11.implies(p11));
    }

    @Test
    public void testImpliesWild1() {
        Permission p1 = new SecureServiceAccessPermission("a/*", null);
        Permission p2 = new SecureServiceAccessPermission("a/default", null);
        assertTrue(p1.implies(p2));
    }

    @Test
    public void testImpliesActions() {
        Permission p1 = new SecureServiceAccessPermission("a", "read,write");
        Permission p2 = new SecureServiceAccessPermission("a", "read");
        assertTrue(p1.implies(p2));

        Permission p3 = new SecureServiceAccessPermission("a", "write");
        assertTrue(p1.implies(p3));
    }
}
