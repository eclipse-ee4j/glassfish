/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.util.reflect;

import java.lang.reflect.Method;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Byron Nevins
 */
public class ReflectUtilsTest {
    public ReflectUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of equalSignatures method, of class ReflectUtils.
     */
    @Test
    public void testEqualSignatures() throws NoSuchMethodException {
        Method m1 = getClass().getDeclaredMethod("met1", String.class, Long.class);
        Method m2 = getClass().getDeclaredMethod("met2", String.class, Long.class);
        Method m3 = getClass().getDeclaredMethod("met3", String.class);
        Method m4 = getClass().getDeclaredMethod("met4", String.class, Integer.class);
        Method m5 = getClass().getDeclaredMethod("met5", String.class, Integer.class);
        Method m6 = getClass().getDeclaredMethod("met6", String.class, String.class);

        String s1 = ReflectUtils.equalSignatures(m1, m2);
        String s2 = ReflectUtils.equalSignatures(m1, m3);
        String s3 = ReflectUtils.equalSignatures(m1, m4);
        String s4 = ReflectUtils.equalSignatures(m2, m1);
        String s5 = ReflectUtils.equalSignatures(m2, m3);
        String s6 = ReflectUtils.equalSignatures(m2, m4);
        String s7 = ReflectUtils.equalSignatures(m3, m4);
        String s8 = ReflectUtils.equalSignatures(m4, m5);
        String s9 = ReflectUtils.equalSignatures(m5, m6);

        assertNull(s1);
        assertNull(s4);
        assertNotNull(s2);
        assertNotNull(s3);
        assertNotNull(s5);
        assertNotNull(s6);
        assertNotNull(s7);
        assertNotNull(s8);
        assertNotNull(s9);
        System.out.println("---------SUCCESSful MISMATCH STRINGS: ------------");
        System.out.printf("%s\n%s\n%s\n%s\n%s\n%s\n%s\n", s2, s3, s5, s6, s7, s8, s9);
        System.out.println("--------------------------------------------------");
    }

    public void met1(String s, Long l) {
    }

    public void met2(String s, Long l) {
    }

    public void met3(String s) {
    }

    public void met4(String s, Integer i) {
    }

    public String met5(String s, Integer i) {
        return "";
    }

    public Long met6(String s, String s2) {
        return 22L;
    }
}
