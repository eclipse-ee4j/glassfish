/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
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

import java.net.URL;
import java.util.List;

import javax.xml.stream.XMLInputFactory;

import org.glassfish.config.support.DomainXmlPreParser.DomainXmlPreParserException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author bnevins
 */
public class DomainXmlPreParserTest {

    private static URL stock, i1, i1i2, c1i1, c1i1c1i2, noconfigfori1;
    private static ClassLoader classLoader = DomainXmlPreParserTest.class.getClassLoader();
    private static XMLInputFactory xif = XMLInputFactory.newFactory();

    @BeforeAll
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

    @Test
    public void stockDomainHasNoInstance() throws DomainXmlPreParserException {
        System.out.println("stockDomainHasNoInstance");
        assertThrows(DomainXmlPreParserException.class, () -> new DomainXmlPreParser(stock, xif, "i1"));
    }

    @Test
    public void domainWithi1() throws DomainXmlPreParserException {
        System.out.println("domainWithi1");
        DomainXmlPreParser pp = new DomainXmlPreParser(i1, xif, "i1");
        List<String> servers = pp.getServerNames();
        String clusterName = pp.getClusterName();
        String configName = pp.getConfigName();

        assertThat(servers, hasSize(1));
        assertEquals("i1", servers.get(0));
        assertNull(clusterName);
        assertEquals("i1-config", configName);
    }

    @Test
    public void domainWithi1i2_i1() throws DomainXmlPreParserException {
        System.out.println("domainWithi1i2_i1");
        DomainXmlPreParser pp = new DomainXmlPreParser(i1i2, xif, "i1");
        List<String> servers = pp.getServerNames();
        String clusterName = pp.getClusterName();
        String configName = pp.getConfigName();

        assertThat(servers, hasSize(1));
        assertThat(servers, contains("i1"));
        assertNull(clusterName);
        assertEquals("i1-config", configName);
    }

    @Test
    public void domainWithi1i2_i2() throws DomainXmlPreParserException {
        System.out.println("domainWithi1i2_i2");
        DomainXmlPreParser pp = new DomainXmlPreParser(i1i2, xif, "i2");
        List<String> servers = pp.getServerNames();
        String clusterName = pp.getClusterName();
        String configName = pp.getConfigName();

        assertThat(servers, hasSize(1));
        assertThat(servers, contains("i2"));
        assertNull(clusterName);
        assertEquals("i2-config", configName);
    }

    @Test
    public void oneClusteredInstance() throws DomainXmlPreParserException {
        System.out.println("oneClusteredInstance");
        DomainXmlPreParser pp = new DomainXmlPreParser(c1i1, xif, "c1i1");
        List<String> servers = pp.getServerNames();
        String clusterName = pp.getClusterName();
        String configName = pp.getConfigName();

        assertThat(servers, hasSize(1));
        assertThat(servers, contains("c1i1"));
        assertEquals("c1", clusterName);
        assertEquals("c1-config", configName);
    }

    @Test
    public void twoClusteredInstances() throws DomainXmlPreParserException {
        System.out.println("twoClusteredInstances");
        DomainXmlPreParser pp = new DomainXmlPreParser(c1i1c1i2, xif, "c1i1");
        List<String> servers = pp.getServerNames();
        String clusterName = pp.getClusterName();
        String configName = pp.getConfigName();

        assertThat(servers, hasSize(2));
        assertThat(servers, contains("c1i1", "c1i2"));
        assertEquals("c1", clusterName);
        assertEquals("c1-config", configName);
    }

    @Test
    public void noConfigTest() {
        System.out.println("noConfigTest");
        try {
            new DomainXmlPreParser(noconfigfori1, xif, "i1");
            fail("Expected an exception!!!");
        } catch (DomainXmlPreParserException e) {
            assertThat(e.getMessage(), startsWith("The config element, "));
        }
    }
}
