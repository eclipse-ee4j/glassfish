/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.xml;

import java.io.*;
import java.util.List;
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
public class ProbeProviderStaxParserTest {
    public ProbeProviderStaxParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ClassLoader cl = ProbeProviderStaxParserTest.class.getClassLoader();
        oneProbeStream = cl.getResourceAsStream("one_probe.xml");
        twoProbeStream = cl.getResourceAsStream("two_probe.xml");
        statefulProbeStream = cl.getResourceAsStream("stateful_probes.xml");
        assertNotNull(oneProbeStream);
        assertNotNull(twoProbeStream);
        assertNotNull(statefulProbeStream);
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
     * Test of read method, of class ProbeProviderStaxParser.
     */
    @Test
    public void testRead() throws Exception {
        System.out.println("read");
        ProbeProviderStaxParser ppsp = new ProbeProviderStaxParser(oneProbeStream);
        List<Provider> providers = ppsp.getProviders();

        System.out.println("oneProbeStream:");
        for(Provider p : providers) {
            System.out.println("******** PROVIDER: *******\n" + p);
        }

        System.out.println("\n\ntwoProbeStream:");
        ppsp = new ProbeProviderStaxParser(twoProbeStream);
        providers = ppsp.getProviders();

        for(Provider p : providers) {
            System.out.println("******** PROVIDER: *******\n" + p);
        }

        System.out.println("\n\nstatefulProbeStream:");
        ppsp = new ProbeProviderStaxParser(statefulProbeStream);
        providers = ppsp.getProviders();

        for(Provider p : providers) {
            System.out.println("******** PROVIDER: *******\n" + p);
        }
    }
   private static InputStream oneProbeStream;
   private static InputStream twoProbeStream;
   private static InputStream statefulProbeStream;
}
