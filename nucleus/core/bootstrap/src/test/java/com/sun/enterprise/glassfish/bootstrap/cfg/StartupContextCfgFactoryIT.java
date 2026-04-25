/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.glassfish.bootstrap.cfg;

import com.sun.enterprise.glassfish.bootstrap.StartupContextUtil;

import java.util.Properties;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link StartupContextCfgFactory#createStartupContextCfg},
 * covering the addMissingPropertiesBasedOnJavaVersion behaviour.
 */
class StartupContextCfgFactoryIT {

    private static final String JAVA_VM_SPEC_VERSION = "java.vm.specification.version";
    private static final String CAPABILITIES_KEY = "org.osgi.framework.system.capabilities";
    private static final String EXISTING_CAPABILITIES_VALUE = "some-existing-capabilities";
    private static final String EXISTING_EECAP_VALUE = "some-existing-eecap";

    private String originalJavaSpecVersion;

    @BeforeEach
    void saveJavaSpecVersion() {
        assertNotNull(System.getProperty(JAVA_VM_SPEC_VERSION),
            JAVA_VM_SPEC_VERSION + " must be set by the JDK");
        originalJavaSpecVersion = System.getProperty(JAVA_VM_SPEC_VERSION);
    }

    @AfterEach
    void restoreJavaSpecVersion() {
        System.setProperty(JAVA_VM_SPEC_VERSION, originalJavaSpecVersion);
    }

    static Stream<Arguments> javaVersions() {
        return Stream.of(
                Arguments.of(11, "1.0,1.1,1.2,1.3,1.4,1.5,1.6,1.7,1.8,9,10,11"),
                Arguments.of(17, "1.0,1.1,1.2,1.3,1.4,1.5,1.6,1.7,1.8,9,10,11,12,13,14,15,16,17"),
                Arguments.of(21, "1.0,1.1,1.2,1.3,1.4,1.5,1.6,1.7,1.8,9,10,11,12,13,14,15,16,17,18,19,20,21"),
                Arguments.of(25, "1.0,1.1,1.2,1.3,1.4,1.5,1.6,1.7,1.8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25")
        );
    }

    // Neither capabilitiesKey nor eecap-<version> exist in config
    // -> capabilitiesKey should be written with the generated value

    @ParameterizedTest
    @MethodSource("javaVersions")
    void capabilitiesKeyIsWritten_whenNeitherCapabilitiesNorEecapDefined(int version, String expectedCapability) throws Exception {
        System.setProperty(JAVA_VM_SPEC_VERSION, String.valueOf(version));

        StartupContextCfg cfg = StartupContextUtil.createStartupContextCfg(new Properties());

        String capabilities = cfg.getProperty(CAPABILITIES_KEY);
        assertNotNull(capabilities,
            CAPABILITIES_KEY + " must be set for Java " + version);
        assertTrue(capabilities.contains(String.valueOf(expectedCapability)),
            CAPABILITIES_KEY + " must contain version definitions" + (expectedCapability) + " but was: " + capabilities);
    }

    // capabilitiesKey exists in config but eecap-<version> does not
    // -> eecap-<version> should be written, capabilitiesKey should remain unchanged

    @ParameterizedTest
    @MethodSource("javaVersions")
    void eecapKeyIsWritten_whenCapabilitiesValueExistsButEecapValueDoesNot(int version, String expectedCapability) throws Exception {
        System.setProperty(JAVA_VM_SPEC_VERSION, String.valueOf(version));

        Properties osgiProps = new Properties();
        osgiProps.setProperty(CAPABILITIES_KEY, EXISTING_CAPABILITIES_VALUE);

        StartupContextCfg cfg = StartupContextUtil.createStartupContextCfg(osgiProps);

        String eecapKey = "eecap-" + version;
        String capabilities = cfg.getProperty(eecapKey);
        assertEquals(EXISTING_CAPABILITIES_VALUE, cfg.getProperty(CAPABILITIES_KEY),
            CAPABILITIES_KEY + " must remain unchanged for Java " + version);
        assertNotNull(capabilities,
            eecapKey + " must be set for Java " + version);
        assertTrue(capabilities.contains(String.valueOf(expectedCapability)),
            eecapKey + " must contain version definitions" + expectedCapability  + " but was: " + capabilities);
    }

    // Both capabilitiesKey and eecap-<version> exist in config
    // -> nothing should be written, both values remain unchanged

    @ParameterizedTest
    @MethodSource("javaVersions")
    void nothingIsWritten_whenBothCapabilitiesAndEecapExist(int version) throws Exception {
        System.setProperty(JAVA_VM_SPEC_VERSION, String.valueOf(version));

        String eecapKey = "eecap-" + version;
        Properties osgiProps = new Properties();
        osgiProps.setProperty(CAPABILITIES_KEY, EXISTING_CAPABILITIES_VALUE);
        osgiProps.setProperty(eecapKey, EXISTING_EECAP_VALUE);

        StartupContextCfg cfg = StartupContextUtil.createStartupContextCfg(osgiProps);

        assertEquals(EXISTING_CAPABILITIES_VALUE, cfg.getProperty(CAPABILITIES_KEY),
            CAPABILITIES_KEY + " must remain unchanged for Java " + version);
        assertEquals(EXISTING_EECAP_VALUE, cfg.getProperty(eecapKey),
            eecapKey + " must remain unchanged for Java " + version);
    }

}
