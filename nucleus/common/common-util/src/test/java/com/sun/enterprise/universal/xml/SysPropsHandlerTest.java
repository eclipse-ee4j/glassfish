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

package com.sun.enterprise.universal.xml;

import com.sun.enterprise.universal.xml.SysPropsHandler.Type;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Byron Nevins
 */
public class SysPropsHandlerTest {

    public SysPropsHandlerTest() {
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
     * Test of getCombinedSysProps method, of class SysPropsHandler.
     */
    @Test
    public void exercise() {
        System.out.println("exercise SysPropsHndler");
        SysPropsHandler instance = new SysPropsHandler();
        instance.add(Type.SERVER, "test", "from-server");
        instance.add(Type.CLUSTER, "test", "from-cluster");
        instance.add(Type.CONFIG, "test", "from-config");
        instance.add(Type.DOMAIN, "test", "from-domain");
        Map<String, String> map = instance.getCombinedSysProps();
        assertTrue(map.size() == 1);
        assertTrue(map.get("test").equals("from-server"));

        instance.add(Type.CLUSTER, "test2", "from-cluster");
        instance.add(Type.CONFIG, "test2", "from-config");
        instance.add(Type.DOMAIN, "test2", "from-domain");

        instance.add(Type.CONFIG, "test3", "from-config");
        instance.add(Type.DOMAIN, "test3", "from-domain");

        instance.add(Type.DOMAIN, "test4", "from-domain");

        map = instance.getCombinedSysProps();

        assertTrue(map.size() == 4);
        assertTrue(map.get("test").equals("from-server"));
        assertTrue(map.get("test2").equals("from-cluster"));
        assertTrue(map.get("test3").equals("from-config"));
        assertTrue(map.get("test4").equals("from-domain"));
    }
}
