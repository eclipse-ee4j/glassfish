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

package org.glassfish.config.support;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.stream.XMLInputFactory;
import org.glassfish.config.support.DomainXmlPreParser.DomainXmlPreParserException;
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
public class DomainXmlPreParserTest {

    public DomainXmlPreParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        stock = loadURL("parser/stock.xml");
        i1 = loadURL("parser/i1.xml");
        i1i2 = loadURL("parser/i1i2.xml");
        c1i1 = loadURL("parser/c1i1.xml");
        c1i1c1i2 = loadURL("parser/c1i1c1i2.xml");
        noconfigfori1 = loadURL("parser/noconfigfori1.xml");
        System.setProperty("AS_DEBUG", "true");
    }

    private static URL loadURL(String name) {
        URL url = classLoader.getResource(name);
        assertNotNull(url);
        return url;
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

    @Test(expected=DomainXmlPreParser.DomainXmlPreParserException.class)
    public void stockDomainHasNoInstance() throws DomainXmlPreParserException {
        System.out.println("stockDomainHasNoInstance");
        DomainXmlPreParser pp = new DomainXmlPreParser(stock, xif, "i1");
    }

    @Test
    public void domainWithi1() throws DomainXmlPreParserException {
        System.out.println("domainWithi1");
        DomainXmlPreParser pp = new DomainXmlPreParser(i1, xif, "i1");
        List<String> servers = pp.getServerNames();
        String clusterName = pp.getClusterName();
        String configName = pp.getConfigName();

        assertTrue(servers.size() == 1);
        assertTrue(servers.get(0).equals("i1"));
        assertNull(clusterName);
        assertEquals(configName, "i1-config");
    }

    @Test
    public void domainWithi1i2_i1() throws DomainXmlPreParserException {
        System.out.println("domainWithi1i2_i1");
        DomainXmlPreParser pp = new DomainXmlPreParser(i1i2, xif, "i1");
        List<String> servers = pp.getServerNames();
        String clusterName = pp.getClusterName();
        String configName = pp.getConfigName();

        assertTrue(servers.size() == 1);
        assertTrue(servers.contains("i1"));
        assertFalse(servers.contains("i2"));
        assertNull(clusterName);
        assertEquals(configName, "i1-config");
    }

    @Test
    public void domainWithi1i2_i2() throws DomainXmlPreParserException {
        System.out.println("domainWithi1i2_i2");
        DomainXmlPreParser pp = new DomainXmlPreParser(i1i2, xif, "i2");
        List<String> servers = pp.getServerNames();
        String clusterName = pp.getClusterName();
        String configName = pp.getConfigName();

        assertTrue(servers.size() == 1);
        assertTrue(servers.contains("i2"));
        assertFalse(servers.contains("i1"));
        assertNull(clusterName);
        assertEquals(configName, "i2-config");
    }

    @Test
    public void oneClusteredInstance() throws DomainXmlPreParserException {
        System.out.println("oneClusteredInstance");
        DomainXmlPreParser pp = new DomainXmlPreParser(c1i1, xif, "c1i1");
        List<String> servers = pp.getServerNames();
        String clusterName = pp.getClusterName();
        String configName = pp.getConfigName();

        assertTrue(servers.size() == 1);
        assertTrue(servers.contains("c1i1"));
        assertEquals(clusterName, "c1");
        assertEquals(configName, "c1-config");
    }

    @Test
    public void twoClusteredInstances() throws DomainXmlPreParserException {
        System.out.println("twoClusteredInstances");
        DomainXmlPreParser pp = new DomainXmlPreParser(c1i1c1i2, xif, "c1i1");
        List<String> servers = pp.getServerNames();
        String clusterName = pp.getClusterName();
        String configName = pp.getConfigName();

        assertTrue(servers.size() == 2);
        assertTrue(servers.contains("c1i1"));
        assertTrue(servers.contains("c1i2"));
        assertEquals(clusterName, "c1");
        assertEquals(configName, "c1-config");
    }

    @Test
    public void noConfigTest() {
        System.out.println("noConfigTest");
        try {
            DomainXmlPreParser pp = new DomainXmlPreParser(noconfigfori1, xif, "i1");
            fail("Expected an exception!!!");
        }
        catch(DomainXmlPreParserException e) {
            assertTrue(e.getMessage().startsWith("The config element, "));
            System.out.println(e);
        }
    }

    private static URL stock, i1, i1i2, c1i1, c1i1c1i2, noconfigfori1;
    private static ClassLoader classLoader = DomainXmlPreParserTest.class.getClassLoader();
    private static XMLInputFactory xif = XMLInputFactory.newInstance();
}
