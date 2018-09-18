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

package org.glassfish.security.services.common;

import java.security.Permission;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SecurePermTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEqualsObject() {
		

		Permission perm1 = new SecureServiceAccessPermission("a/b/c", "read,write");
		
		Permission perm2 = new SecureServiceAccessPermission("a/b/c/d", "read,write");
		Assert.assertFalse(perm1.equals(perm2));
		
		Permission p3 = null;
		Assert.assertFalse(perm1.equals(p3));
		

		Permission p5 = new SecureServiceAccessPermission("a/b/c");
		Assert.assertFalse(perm1.equals(p5));
	}

	@Test
	public void testEquals1() {
		Permission p1 = new SecureServiceAccessPermission("a/b/c", "read,write");
		Permission p2 = new SecureServiceAccessPermission("a/b/c/", "read,write");
		Assert.assertFalse(p1.equals(p2));
		Assert.assertFalse(p1.implies(p2));
		
	}
	
	@Test
	public void testImpliesPermission() {
		Permission p1 = new SecureServiceAccessPermission("a", "read");
		Permission p2 = new SecureServiceAccessPermission("b", "read");
		Assert.assertFalse(p1.implies(p2));
		
		Permission p3 = new SecureServiceAccessPermission("a", "read,write");
		Assert.assertTrue(p3.implies(p1));
	}

	@Test
	public void testImpliesWild() {
		Permission p1 = new SecureServiceAccessPermission("a/*", "read");
		
		Permission p2 = new SecureServiceAccessPermission("a/b", "read");
		Assert.assertTrue(p1.implies(p2));
		
		Permission p3 = new SecureServiceAccessPermission("a/b/c", "read");
		Assert.assertTrue(p1.implies(p3));
		
		
		Permission p11 = new SecureServiceAccessPermission("a/b/*", "read");
		Assert.assertTrue(p11.implies(p3));
		
		Assert.assertFalse(p11.implies(p1));
		Assert.assertFalse(p11.implies(p2));
		
		Assert.assertTrue(p11.implies(p11));
	}

   @Test
    public void testImpliesWild1() {
        Permission p1 = new SecureServiceAccessPermission("a/*", null);
        Permission p2 = new SecureServiceAccessPermission("a/default", null);
        Assert.assertTrue(p1.implies(p2));
   }
	
	@Test
	public void testImpliesActions() {
		Permission p1 = new SecureServiceAccessPermission("a", "read,write");
		Permission p2 = new SecureServiceAccessPermission("a", "read");
		Assert.assertTrue(p1.implies(p2));
		
		Permission p3 = new SecureServiceAccessPermission("a", "write");
		Assert.assertTrue(p1.implies(p3));
	}


}
