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

package com.sun.enterprise.util;

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
public class StringUtilsTest {

    public StringUtilsTest() {
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
     * Test of removeEnclosingQuotes method, of class StringUtils.
     */
    @Test
    public void removeEnclosingQuotes() {
        String a = "\"hello\"";
        String b = "'hello'";
        String c = "\"hello'";
        String d = "\"\"hello";

        assertEquals(StringUtils.removeEnclosingQuotes(a), "hello");
        assertEquals(StringUtils.removeEnclosingQuotes(b), "hello");
        assertEquals(StringUtils.removeEnclosingQuotes(c), "\"hello\'");
        assertEquals(StringUtils.removeEnclosingQuotes(d), "\"\"hello");
        assertEquals(StringUtils.removeEnclosingQuotes("\""), "\"");
        assertEquals(StringUtils.removeEnclosingQuotes("'"), "'");
        assertEquals(StringUtils.removeEnclosingQuotes("''"), "");
        assertEquals(StringUtils.removeEnclosingQuotes("\"\""), "");
        assertEquals(StringUtils.removeEnclosingQuotes(""), "");
        assertNull(StringUtils.removeEnclosingQuotes(null));

    }
}
