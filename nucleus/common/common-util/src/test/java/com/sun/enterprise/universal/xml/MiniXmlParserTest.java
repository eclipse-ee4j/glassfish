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

package com.sun.enterprise.universal.xml;

import com.sun.enterprise.util.HostAndPort;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author bnevins
 */
public class MiniXmlParserTest {
    private static File hasProfiler;
    private static File wrongOrder;
    private static File rightOrder;
    private static File noconfig;
    private static File adminport;
    private static File adminport2;
    private static File noCloseRightOrder;
    private static File noCloseWrongOrder;
    private static File noDomainName;
    private static File bigDomain;
    private static File monitoringFalse;
    private static File monitoringTrue;
    private static File monitoringNone;
    private static File clusters1;
    private static File manysysprops;

    @BeforeAll
    public static void setUpClass() throws Exception {
        wrongOrder = getFile("wrongorder.xml");
        rightOrder = getFile("rightorder.xml");
        noconfig = getFile("noconfig.xml");
        hasProfiler = getFile("hasprofiler.xml");
        adminport = getFile("adminport.xml");
        adminport2 = getFile("adminport2.xml");
        noCloseRightOrder = getFile("rightordernoclosedomain.xml");
        noCloseWrongOrder = getFile("wrongordernoclosedomain.xml");
        noDomainName = getFile("nodomainname.xml");
        bigDomain = getFile("big.xml");
        monitoringFalse = getFile("monitoringFalse.xml");
        monitoringTrue = getFile("monitoringTrue.xml");
        monitoringNone = getFile("monitoringNone.xml");
        clusters1 = getFile("clusters1.xml");
        manysysprops = getFile("manysysprops.xml");
    }

    private static File getFile(final String fileName) throws URISyntaxException {
        final URL url = MiniXmlParserTest.class.getClassLoader().getResource(fileName);
        assertNotNull(url, "url");
        assertEquals("file", url.getProtocol(), "url.protocol");
        final File file = Paths.get(url.toURI()).toFile();
        assertTrue(file.exists(), "File doesn't exist: " + file);
        return file;
    }

    /**
     * Positive Test Case where servers appears after configs
     */
    @Test
    public void serversAfterConfigs() throws Exception {
        MiniXmlParser instance = new MiniXmlParser(wrongOrder, "server");
        Map<String, String> javaConfig = instance.getJavaConfig();
        List<String> jvmOptions = instance.getJvmOptions();
        assertEquals("JVMOPTION1", jvmOptions.get(0));
        assertEquals("JVMOPTION2", jvmOptions.get(1));
        assertEquals("test", javaConfig.get("test"));
    }

    /**
     * Test that the correct Exception is thrown for a null xml File
     */
    @Test
    public void nullXmlFile() throws MiniXmlParserException {
        assertThrows(MiniXmlParserException.class, () -> new MiniXmlParser(null, "server"));
    }

    /**
     * Test that the correct Exception is thrown for a non-existing xml File
     */
    @Test
    public void nonexistentFile() throws MiniXmlParserException {
        assertThrows(MiniXmlParserException.class, () -> new MiniXmlParser(new File("."), "server"));
    }

    /**
     * Positive Test Case where configs appears after servers
     */
    @Test
    public void configsAfterServers() throws Exception {
        MiniXmlParser instance = new MiniXmlParser(rightOrder, "server");
        Map<String, String> javaConfig = instance.getJavaConfig();
        List<String> jvmOptions = instance.getJvmOptions();
        assertEquals("JVMOPTION1", jvmOptions.get(0));
        assertEquals("JVMOPTION2", jvmOptions.get(1));
        assertEquals("test", javaConfig.get("test"));
    }

    /**
     * Negative Test Case where there is no "server-config"
     */
    @Test
    public void noServerConfig() throws MiniXmlParserException {
        assertThrows(MiniXmlParserException.class, () -> new MiniXmlParser(noconfig, "server"));
    }

    /**
     * Positive test cases -- look at <system-property>
     */
    @Test
    public void systemProperties() throws Exception {
        MiniXmlParser instance = new MiniXmlParser(rightOrder, "server");
        Map<String, String> javaConfig = instance.getJavaConfig();
        List<String> jvmOptions = instance.getJvmOptions();
        Map<String, String> sysProps = instance.getSystemProperties();
        assertEquals("JVMOPTION1", jvmOptions.get(0));
        assertEquals("JVMOPTION2", jvmOptions.get(1));
        assertEquals("test", javaConfig.get("test"));
        assertEquals("true", sysProps.get("beforeJavaConfig"));
        assertEquals("true", sysProps.get("afterJavaConfig"));
        assertNull(sysProps.get("foo"));
        assertEquals(sysProps.size(), 3);
    }

    /**
     * Positive test case -- make sure system-property in <server> overrides the one in <config>
     */
    @Test
    public void systemPropertyOverrides() throws Exception {
        MiniXmlParser instance = new MiniXmlParser(rightOrder, "server");
        Map<String, String> sysProps = instance.getSystemProperties();
        assertEquals("valueFromServer", sysProps.get("test-prop"));
    }

    /**
     * Positive test case -- make sure profiler is parsed correctly
     * here is the piece of xml it will be parsing:
     *
     * <pre>{@code
     * <profiler classpath="/profiler/class/path" enabled="true" name="MyProfiler" native-library-path="/bin">
     *  <jvm-options>-Dprofiler3=foo3</jvm-options>
     *  <jvm-options>-Dprofiler2=foo2</jvm-options>
     *  <jvm-options>-Dprofiler1=foof</jvm-options>
     * </profiler>
     *  }</pre>
     */
    @Test
    public void profilerParsing() throws Exception {
        MiniXmlParser instance = new MiniXmlParser(hasProfiler, "server");
        Map<String, String> config = instance.getProfilerConfig();
        List<String> jvm = instance.getProfilerJvmOptions();
        Map<String, String> sysProps = instance.getProfilerSystemProperties();
        assertEquals(3, jvm.size());
        assertEquals("-Dprofiler3=foo3", jvm.get(0));
        assertEquals("-Dprofiler2=foo2", jvm.get(1));
        assertEquals("-Dprofiler1=foof", jvm.get(2));
        assertNotNull(config);
        assertEquals(4, config.size());
        assertEquals("/profiler/class/path", config.get("classpath"));
        assertEquals("MyProfiler", config.get("name"));
        assertEquals("/bin", config.get("native-library-path"));
        assertEquals(2, sysProps.size());
        assertEquals("value1", sysProps.get("name1"));
        assertEquals("value2", sysProps.get("name2"));
    }

    /**
     * Exercise the parsing of asadmin virtual server, network-listener and port numbers
     * this one tests for TWO listeners
     */
    @Test
    public void findTwoAdminPorts() throws Exception {
        MiniXmlParser instance = new MiniXmlParser(adminport2, "server");
        List<HostAndPort> addrs = instance.getAdminAddresses();
        assertEquals(2, addrs.size());
        boolean saw3333 = false, saw4444 = false, sawSecure = false;
        for (HostAndPort addr : addrs) {
            if (addr.getPort() == 3333) {
                saw3333 = true;
            }
            if (addr.getPort() == 4444) {
                saw4444 = true;
                if (addr.isSecure()) {
                    sawSecure = true;
                }
            }
        }
        assertTrue(saw3333, "Saw port 3333");
        assertTrue(saw4444, "Saw port 4444");
        assertTrue(sawSecure, "Saw port 4444 security-enabled");
    }

    /**
     * Exercise the parsing of asadmin virtual server, network-listener and port numbers
     * this one tests for ONE listener
     */
    @Test
    public void findOneAdminPort() throws Exception {
        MiniXmlParser instance = new MiniXmlParser(adminport, "server");
        List<HostAndPort> addrs = instance.getAdminAddresses();
        assertEquals(1, addrs.size());
        assertEquals(3333, addrs.iterator().next().getPort());
    }

    /**
     * Test that the correct Exception is thrown for a "right-order" xml that has no /domain  element in it
     *
     * @throws MiniXmlParserException
     */
    @Test
    public void testNoClosingDomainRightOrder() throws MiniXmlParserException {
        assertThrows(MiniXmlParserException.class, () -> new MiniXmlParser(noCloseRightOrder, "server"));
    }

    /**
     * Test that the correct Exception is thrown for a "wrong-order" xml that has no /domain  element in it
     *
     * @throws MiniXmlParserException
     */
    @Test
    public void testNoClosingDomainWrongOrder() throws MiniXmlParserException {
        assertThrows(MiniXmlParserException.class, () -> new MiniXmlParser(noCloseWrongOrder, "server"));
    }

    /**
     * Test that not having a domain-name is not fatal
     *
     * @throws MiniXmlParserException
     */
    @Test
    public void testNoDomainName() throws MiniXmlParserException {
        new MiniXmlParser(noDomainName, "server");
    }

    @Test
    public void testOldSchema() throws Exception {
        final MiniXmlParser parser = new MiniXmlParser(getFile("olddomain.xml"), "server");
        List<HostAndPort> addrs = parser.getAdminAddresses();
        assertEquals(1, addrs.size());

    }

    @Test
    public void testNoNetworkConfig() throws Exception {
        final MiniXmlParser parser = new MiniXmlParser(getFile("olddomain.xml"), "server");
        assertFalse(parser.hasNetworkConfig());

    }

    @Test
    public void testNetworkConfig() throws MiniXmlParserException {
        final MiniXmlParser parser = new MiniXmlParser(rightOrder, "server");
        assertTrue(parser.hasNetworkConfig());

    }


    @Test
    public void testMonitoringTrue() throws MiniXmlParserException {
        MiniXmlParser instance = new MiniXmlParser(monitoringTrue, "server");
        assertTrue(instance.isMonitoringEnabled());
    }

    @Test
    public void testMonitoringFalse() throws MiniXmlParserException {
        MiniXmlParser instance = new MiniXmlParser(monitoringFalse, "server");
        assertTrue(!instance.isMonitoringEnabled());
    }

    @Test
    public void testMonitoringNone() throws MiniXmlParserException {
        MiniXmlParser instance = new MiniXmlParser(monitoringNone, "server");
        assertTrue(instance.isMonitoringEnabled());
    }

    @Test
    public void testClusterParsing() throws MiniXmlParserException {
        MiniXmlParser instance = new MiniXmlParser(clusters1, "i1");
        assertEquals("domain1", instance.getDomainName());
    }


    /**
     * the xml has 4 special system-properties. We check here that they override
     * each other properly...
     *
     * <pre>
     * {@code
     * <system-property name="shouldbeserver" value="domain"></system-property>
     * <system-property name="shouldbeconfig" value="domain"></system-property>
     * <system-property name="shouldbecluster" value="domain"></system-property>
     * <system-property name="shouldbedomain" value="domain"></system-property>
     * }</pre>
     */
    @Test
    public void testSysPropParsing() throws MiniXmlParserException {
        MiniXmlParser instance = new MiniXmlParser(manysysprops, "i1");
        Map<String, String> sp = instance.getSystemProperties();
        // Note: there were 10 values for these 4 system-properties -- when
        // combined in the exact correct order of priority you'll get the below
        // results.  Grep on "should" in domain.xml to see behind the smoke and mirrors.
        assertThat(sp, aMapWithSize(14));
        assertEquals("server", sp.get("shouldbeserver"));
        assertEquals("config", sp.get("shouldbeconfig"));
        assertEquals("cluster", sp.get("shouldbecluster"));
        assertEquals("domain", sp.get("shouldbedomain"));
        assertNull(sp.get("shouldbejunk"));
    }


    @Test
    public void testBigDomainParsing() throws MiniXmlParserException {
        MiniXmlParser instance = new MiniXmlParser(bigDomain, "server");
        assertEquals("domain1", instance.getDomainName());
    }
}
