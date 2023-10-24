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

package com.sun.enterprise.security.ee.perms;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FilePermission;
import java.security.AllPermission;
import java.security.Permission;
import org.junit.jupiter.api.Test;

public class VoidPermissionTest {

    @Test
    public void testImpliedByAllPermission() {
        Permission allPerm = new AllPermission();
        VoidPermission vPerm = new VoidPermission();

        assertTrue(allPerm.implies(vPerm));
        assertFalse(vPerm.implies(allPerm));
    }

    @Test
    public void testNotImplied() {
        VoidPermission vPerm = new VoidPermission();
        FilePermission fPerm = new FilePermission("/scratch/test/*", "read");

        assertFalse(vPerm.implies(fPerm));
        assertFalse(fPerm.implies(vPerm));
    }

    @Test
    public void testNoImplySelf() {
        VoidPermission vPerm1 = new VoidPermission();
        VoidPermission vPerm2 = new VoidPermission();

        assertFalse(vPerm1.implies(vPerm2));
        assertFalse(vPerm2.implies(vPerm1));
        assertFalse(vPerm1.implies(vPerm1));
    }
}
