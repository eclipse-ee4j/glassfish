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

package com.sun.enterprise.security.perms;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.AllPermission;
import java.security.Permission;
import java.io.FilePermission;

import junit.framework.Assert;

public class VoidPermissionTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    
    @Test
    public void testImpliedByAllPermission() {
        
        Permission allPerm = new AllPermission();
        
        VoidPermission vPerm = new VoidPermission();
        
        
        Assert.assertTrue(allPerm.implies(vPerm));
        
        Assert.assertTrue(!vPerm.implies(allPerm));
    }
    
    
    @Test
    public void testNotImplied() {
        
        VoidPermission vPerm = new VoidPermission();
        FilePermission fPerm = new FilePermission("/scratch/test/*", "read");
        
        Assert.assertTrue(!vPerm.implies(fPerm));
        Assert.assertTrue(!fPerm.implies(vPerm));
    }

    
    @Test
    public void testNoImplySelf() {
        VoidPermission vPerm1 = new VoidPermission();
        VoidPermission vPerm2 = new VoidPermission();
        
        Assert.assertTrue(!vPerm1.implies(vPerm2));
        Assert.assertTrue(!vPerm2.implies(vPerm1));
        
        Assert.assertTrue(!vPerm1.implies(vPerm1));
    }
}
