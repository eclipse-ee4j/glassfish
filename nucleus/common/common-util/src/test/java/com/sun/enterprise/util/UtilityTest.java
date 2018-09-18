/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.*;
import java.util.Map;
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
public class UtilityTest {

    public UtilityTest() {
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
     * VERY SIMPLE Test of getEnvOrProp method, of class Utility.
     */
    @Test
    public void testGetEnvOrProp() {
        Map<String, String> env = System.getenv();
        Set<String> keys = env.keySet();
        String key = null;
        String value = null;

        // warning:  super-paranoid bullet-proof test ahead!!!
        for (String akey : keys) {
            // Make sure both key and value are kosher

            if(!StringUtils.ok(akey))
                continue;

            // make sure this name:value is NOT in System Properties!
            // empty string counts as a value!
            if(System.getProperty(akey) != null)
                continue;

            String avalue = env.get(akey);

            if(!StringUtils.ok(avalue))
                continue;

            key = akey;
            value = avalue;
            break;
        }

        // allow the case where there are no env. variables.  Probably impossible
        // but this test needs to run on many many many environments and we don't
        // want to fail in such a case.

        if(key == null)
            return;

        assertEquals(Utility.getEnvOrProp(key), value);
        String sysPropValue = "SYS_PROP" + value;
        System.setProperty(key, sysPropValue);
        assertEquals(Utility.getEnvOrProp(key), sysPropValue);
    }
}
