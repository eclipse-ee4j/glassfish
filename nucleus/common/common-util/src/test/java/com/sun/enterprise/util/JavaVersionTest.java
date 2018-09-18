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

package com.sun.enterprise.util;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * Tests for JavaVersion class's methods.
 * @author Yamini K B
 */
public class JavaVersionTest {    
    private static final String[] JAVA_VERSIONS = new String[] {
        "1.5.1-beta",
        "1.6.0",
        "1.7.0_10-ea",
        "1.7.0_17",
        "1.7.0_17-rc1"
    };

    private static final String[] INVALID_JAVA_VERSIONS = new String[] {
        "1a.7.0",
        "a.b.c",
        "1.7beta",
    };
    
    
    @Test
    public void testMatchRegex() {
        for (String st: JAVA_VERSIONS) {
            System.out.println("Test Java Version String " + st);
            JavaVersion jv = JavaVersion.getVersion(st);
            assertTrue(jv != null);
            System.out.println("Java Version = " + jv.toJdkStyle());
        }
        
        for (String st: INVALID_JAVA_VERSIONS) {
            System.out.println("Test Invalid Java Version String " + st);
            JavaVersion jv = JavaVersion.getVersion(st);
            assertTrue(jv == null);
        }
    }
    
    @Test
    public void testNewerThan() {
        JavaVersion jv1 = JavaVersion.getVersion("1.7.0_10-ea");
        JavaVersion jv2 = JavaVersion.getVersion("1.7.0_11");
        assertTrue(jv2.newerThan(jv1));
    }
    
    @Test
    public void testNewerOrEQuals() {
        JavaVersion jv1 = JavaVersion.getVersion("1.7.0_11");
        assertTrue(jv1.newerOrEquals(jv1));
        JavaVersion jv2 = JavaVersion.getVersion("1.7.0_12");
        JavaVersion jv3 = JavaVersion.getVersion("1.7.0_11-ea");
        assertTrue(jv2.newerOrEquals(jv3));
    }
    
    @Test
    public void testOlderThan() {
        JavaVersion jv1 = JavaVersion.getVersion("1.3.0");
        JavaVersion jv2 = JavaVersion.getVersion("1.3.0_11");
        assertTrue(jv1.olderThan(jv2));
        JavaVersion jv3 = JavaVersion.getVersion("1.3.1");
        assertTrue(jv1.olderThan(jv3));
    }
    
    @Test
    public void testOlderOrEquals() {
        JavaVersion jv1 = JavaVersion.getVersion("1.6.0_31");
        JavaVersion jv2 = JavaVersion.getVersion("1.7.0");
        assertTrue(jv1.olderOrEquals(jv2));
    }
}

