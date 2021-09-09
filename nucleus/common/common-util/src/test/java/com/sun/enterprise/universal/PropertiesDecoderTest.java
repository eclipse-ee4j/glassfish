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

package com.sun.enterprise.universal;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author bnevins
 */
public class PropertiesDecoderTest {

    /**
     * Test of unflatten method, of class PropertiesDecoder.
     */
    @Test
    public void testUnflatten() {
        String s = "foo=goo:xyz:hoo=ioo";
        Map<String, String> result = PropertiesDecoder.unflatten(s);
        Map<String, String> expResult = new HashMap<>();
        expResult.put("foo", "goo");
        expResult.put("xyz", null);
        expResult.put("hoo", "ioo");
        assertEquals(expResult, result);

        s = "foo=goo:xyz:hoo=ioo:qqq=::::z:";
        result = PropertiesDecoder.unflatten(s);
        expResult.put("qqq", null);
        expResult.put("z", null);
        assertEquals(expResult, result);

        s = "foo=goo:xyz:hoo=ioo:qqq=::::z:foo=qbert:a=b=c=d";
        result = PropertiesDecoder.unflatten(s);
        expResult.put("foo", "qbert");
        expResult.put("a", "b=c=d");
        assertEquals(expResult, result);
    }
}
