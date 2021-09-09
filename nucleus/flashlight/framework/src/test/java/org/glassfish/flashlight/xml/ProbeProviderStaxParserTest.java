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

package org.glassfish.flashlight.xml;

import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author bnevins
 */
public class ProbeProviderStaxParserTest {

    private static InputStream oneProbeStream;
    private static InputStream twoProbeStream;
    private static InputStream statefulProbeStream;

    @BeforeAll
    public static void setUpClass() throws Exception {
        ClassLoader cl = ProbeProviderStaxParserTest.class.getClassLoader();
        oneProbeStream = cl.getResourceAsStream("one_probe.xml");
        twoProbeStream = cl.getResourceAsStream("two_probe.xml");
        statefulProbeStream = cl.getResourceAsStream("stateful_probes.xml");
        assertNotNull(oneProbeStream);
        assertNotNull(twoProbeStream);
        assertNotNull(statefulProbeStream);
    }


    @AfterAll
    public static void closeResources() throws Exception {
        assertAll(
            () -> assertDoesNotThrow(oneProbeStream::close),
            () -> assertDoesNotThrow(twoProbeStream::close),
            () -> assertDoesNotThrow(statefulProbeStream::close)
        );
    }


    @Test
    public void readOneProbeStream() throws Exception {
        ProbeProviderStaxParser ppsp = new ProbeProviderStaxParser(oneProbeStream);
        List<Provider> providers = ppsp.getProviders();
        assertThat(providers, hasSize(1));
        Provider provider = providers.get(0);
        assertEquals("MyModuleName", provider.getModuleName());
        assertEquals("MyModuleProviderName", provider.getModuleProviderName());
        assertEquals("com.sun.enterprise.web.WebModule", provider.getProbeProviderClass());
        assertEquals("MyProbeProviderName", provider.getProbeProviderName());
        assertThat(provider.getProbes(), hasSize(1));
    }


    @Test
    public void readTwoProbeStream() throws Exception {
        ProbeProviderStaxParser ppsp = new ProbeProviderStaxParser(twoProbeStream);
        List<Provider> providers = ppsp.getProviders();
        assertThat(providers, hasSize(1));
        Provider provider = providers.get(0);
        assertAll(
            () -> assertEquals("MyModuleName", provider.getModuleName()),
            () -> assertEquals("MyModuleProviderName", provider.getModuleProviderName()),
            () -> assertEquals("com.sun.enterprise.web.WebModule", provider.getProbeProviderClass()),
            () -> assertEquals("MyProbeProviderName", provider.getProbeProviderName())
        );
        List<XmlProbe> probes = provider.getProbes();
        assertThat(probes, hasSize(2));
        {
            XmlProbe probe0 = probes.get(0);
            assertAll("probe0",
                () -> assertEquals("MyProbeMethodName", probe0.getProbeMethod()),
                () -> assertEquals("MyProbeName", probe0.getProbeName()),
                () -> assertNull(probe0.getProfileNames()),
                () -> assertFalse(probe0.getStateful()),
                () -> assertFalse(probe0.getStatefulException()),
                () -> assertFalse(probe0.getStatefulReturn()),
                () -> assertThat(probe0.getProbeParams(), hasSize(1))
            );
            XmlProbeParam param0 = probe0.getProbeParams().get(0);
            assertAll(
                () -> assertEquals("MyStringParam", param0.getName()),
                () -> assertEquals("String", param0.getType())
            );
        }
        {
            XmlProbe probe1 = probes.get(1);
            assertAll("probe1",
                () -> assertEquals("MySecondProbeMethodName", probe1.getProbeMethod()),
                () -> assertEquals("MySecondProbeName", probe1.getProbeName()),
                () -> assertNull(probe1.getProfileNames()),
                () -> assertFalse(probe1.getStateful()),
                () -> assertFalse(probe1.getStatefulException()),
                () -> assertFalse(probe1.getStatefulReturn()),
                () -> assertThat(probe1.getProbeParams(), hasSize(5))
            );
            String[] names = probe1.getProbeParams().stream().map(XmlProbeParam::getName).toArray(String[]::new);
            String[] types = probe1.getProbeParams().stream().map(XmlProbeParam::getType).toArray(String[]::new);
            assertAll(
                () -> assertThat("names", names, arrayContaining("s1", "s2", "s3", "s4", "s5")),
                () -> assertThat("types", types, arrayContaining("String", "String", "String", "String", "String"))
            );
        }
    }


    @Test
    public void readStatefulProbeStream() throws Exception {
        ProbeProviderStaxParser ppsp = new ProbeProviderStaxParser(statefulProbeStream);
        List<Provider> providers = ppsp.getProviders();
        assertThat(providers, hasSize(1));
        Provider provider = providers.get(0);
        assertEquals("MyModuleName", provider.getModuleName());
        assertEquals("MyModuleProviderName", provider.getModuleProviderName());
        assertEquals("com.sun.enterprise.web.WebModule", provider.getProbeProviderClass());
        assertEquals("MyStatefulProbeProviderName", provider.getProbeProviderName());
        List<XmlProbe> probes = provider.getProbes();
        assertThat(probes, hasSize(6));
        XmlProbe probe0 = probes.get(0);
        assertAll("probe0",
            () -> assertEquals("MyProbeMethodName", probe0.getProbeMethod()),
            () -> assertEquals("StatefulProbeDefaultsStringParam", probe0.getProbeName()),
            () -> assertNull(probe0.getProfileNames()),
            () -> assertTrue(probe0.getStateful()),
            () -> assertFalse(probe0.getStatefulException()),
            () -> assertFalse(probe0.getStatefulReturn()),
            () -> assertThat(probe0.getProbeParams(), hasSize(1))
        );
        XmlProbeParam param0 = probe0.getProbeParams().get(0);
        assertAll(
            () -> assertEquals("MyStringParam", param0.getName()),
            () -> assertEquals("String", param0.getType())
        );
    }
}
