/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.universal.glassfish;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 * @author bnevins
 */
public class TokenResolverTest {

    private Map<String,String> testMap;

    @BeforeEach
    public void setUp() {
        testMap = new HashMap<>();
        testMap.put("name1", "value1");
        testMap.put("name2", "value2");
        testMap.put("name3", "value3");
        testMap.put("name4", "value4");
    }


    /**
     * Test of resolve method, of class TokenResolver.
     */
    @Test
    public void testResolve_Map() {
        Map<String,String> map2 = new HashMap<>();

        map2.put("foo", "${name1}");
        map2.put("foo2", "${name111}");
        map2.put("zzz${name3}zzz", "zzz");
        map2.put("qqq${name2}qqq", "${name4}");

        TokenResolver instance = new TokenResolver(testMap);
        instance.resolve(map2);
        assertEquals(map2.get("foo"), "value1");
        assertEquals(map2.get("foo2"), "${name111}");
        // this entry should be gone:
        assertNull(map2.get("qqq${name2}qqq"));

        // and replaced with this:
        assertEquals(map2.get("qqqvalue2qqq"), "value4");

        assertEquals(map2.get("zzzvalue3zzz"), "zzz");

        instance.resolve(map2);
    }


    /**
     * Test of resolve method, of class TokenResolver.
     */
    @Test
    public void testResolve_String() {
        TokenResolver instance = new TokenResolver(testMap);
        String expResult = "xyzvalue1xyz";
        String result = instance.resolve("xyz${name1}xyz");
        assertEquals(expResult, result);

        expResult = "xyz$value1xyz";
        result = instance.resolve("xyz$${name1}xyz");
        assertEquals(expResult, result);

        expResult = "xyzvalue1}xyz";
        result = instance.resolve("xyz${name1}}xyz");
        assertEquals(expResult, result);

        expResult = "xyzvalue4xyz";
        result = instance.resolve("xyz${name4}xyz");
        assertEquals(expResult, result);

        expResult = "xyz${name5}xyz";
        result = instance.resolve("xyz${name5}xyz");
        assertEquals(expResult, result);
    }

}
