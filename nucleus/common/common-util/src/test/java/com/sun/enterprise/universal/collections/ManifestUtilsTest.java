/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.universal.collections;

import java.util.Map;
import java.util.jar.*;
import java.util.jar.Manifest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bnevins
 */
public class ManifestUtilsTest {

    public ManifestUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of normalize method, of class ManifestUtils.
     */
    @Test
    public void normalize() {
        Manifest m = new Manifest();
        String hasToken = "abc" + ManifestUtils.EOL_TOKEN + "def";
        String convertedHasToken = "abc" + ManifestUtils.EOL + "def";
        Attributes mainAtt = m.getMainAttributes();
        Map<String,Attributes> entries =  m.getEntries();
        Attributes fooAtt = new Attributes();
        entries.put("foo", fooAtt);
        fooAtt.putValue("fooKey", "fooValue");
        fooAtt.putValue("fooKey2", hasToken);
        mainAtt.putValue("mainKey", "mainValue");

        Map<String,Map<String,String>> norm = ManifestUtils.normalize(m);
        Map<String,String> normMainAtt = norm.get(ManifestUtils.MAIN_ATTS);
        Map<String,String> normFooAtt = norm.get("foo");

        assertTrue(norm.size() == 2);
        assertNotNull(normMainAtt);
        assertNotNull(normFooAtt);
        assertTrue(normMainAtt.size() == 1);
        assertTrue(normFooAtt.size() == 2);
        assertFalse(normFooAtt.get("fooKey2").equals(hasToken));
        assertTrue(normFooAtt.get("fooKey2").equals(convertedHasToken));
        assertFalse(hasToken.equals(convertedHasToken));
        assertEquals(normMainAtt.get("mainKey"), "mainValue");
    }

    @Test
    public void encode() {
        String noLinefeed = "abc";
        String linefeed = "abc\ndef";
        String dosfeed = "abc\r\ndef";
        String s1 = ManifestUtils.encode(noLinefeed);
        String s2 = ManifestUtils.encode(linefeed);
        String s3 = ManifestUtils.encode(dosfeed);

        String desired = "abc" + ManifestUtils.EOL_TOKEN + "def";

        assertEquals(noLinefeed, s1);
        assertFalse(linefeed.equals(s2));
        assertFalse(dosfeed.equals(s3));
        assertEquals(s2, desired);
        assertEquals(s3, desired);
    }
}
