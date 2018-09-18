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

package com.sun.enterprise.util.net;

import com.sun.enterprise.util.net.NetUtils.PortAvailability;
import java.net.InetAddress;
import java.net.Socket;

import org.junit.*;

import static org.junit.Assert.*;

/**
 *
 * @author bnevins
 */

/*
 *
Tim Quinn reported the following problem with these tests, June 18, 2010
nslookup unlikely_name_1000
Server: 192.168.2.1
Address: 192.168.2.1#53

Non-authoritative answer:
Name: unlikely_name_1000
Address: 208.68.139.38


I think this is because my ISP, Comcast, tries to be helpful to browser users
who look for non-existent stuff.  It causes this problem, though, for any
look-up that depends on an error response.

Might need to disable that test because other ISPs do this too.

MY (Byron Nevins) Response -- move the test into the "special" tests...

MY (Sahoo) Repsonse -- Since this is not yet moved, I am disabling this, 
because it is currently failing for me in a clean workspace.
 */
@Ignore
public class NetUtilsTest {

    public NetUtilsTest() {
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
     * Test of hostIsLocal method, of class NetUtils.
     */
    @Test
    public void testHostIsLocal() {

        String[] hostnames = new String[]{null, "", unlikelyName, "oracle.com", "localhost", NetUtils.getHostName()};
        boolean[] expected = new boolean[]{true, true, false, false, true, true};

        for (int i = 0; i < hostnames.length; i++) {
            String hostname = hostnames[i];
            boolean expResult = expected[i];
            boolean result = NetUtils.isThisHostLocal(hostname);
            assertEquals("hostname: " + hostname + ", result: " + result, expResult, result);
        }
    }

    /**
     * Test equals
     */
    @Test
    public void testEquals() {
        assertTrue(NetUtils.isEqual(null, null));
        assertTrue(NetUtils.isEqual(null, ""));
        assertTrue(NetUtils.isEqual("", ""));
        assertTrue(NetUtils.isEqual("localhost", "localhost"));
        assertFalse(NetUtils.isEqual(null, "localhost"));
        assertTrue(NetUtils.isEqual("localhost", "localhost"));
        // since neither one can be resolved -- they are NOT the same, by definition


    }

    @Test
    public void testEqualsSpecial() {
        // in case I forget to set SPECIAL to false!
        if (SPECIAL && "bnevins".equals(System.getProperty("user.name"))) {
            String x1 = "unix"; // in my hosts file
            String x2 = "improvident.sfbay";
            String x3 = "improvident.sfbay.sun.com";
            String x4 = "improvident.sfbay.sun"; // this is garbage
            assertTrue(NetUtils.isEqual(x1, x2));
            assertTrue(NetUtils.isEqual(x1, x3));
            assertTrue(NetUtils.isEqual(x2, x3));
            assertTrue(NetUtils.isEqual(x1, x1));
            assertTrue(NetUtils.isEqual(x2, x2));
            assertTrue(NetUtils.isEqual(x3, x3));
            assertFalse(NetUtils.isEqual(x4, x4));
            assertFalse(NetUtils.isEqual(x1, x4));
            assertFalse(NetUtils.isEqual(x2, x4));
            assertFalse(NetUtils.isEqual(x3, x4));
            assertFalse(NetUtils.isEqual(unlikelyName, unlikelyName));
        }
    }
    
    private static final String unlikelyName;
    private static final boolean SPECIAL = false;

    static {
        String s = "" + System.nanoTime();
        s = s.substring(s.length() - 4);
        unlikelyName = "unlikely_name_" + s;
    }


}
