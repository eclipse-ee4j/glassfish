/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul.cfg;

import java.io.File;
import java.util.stream.Collectors;

import org.glassfish.main.jul.GlassFishLogManager;
import org.glassfish.main.jul.cfg.GlassFishLogManagerConfiguration.ConfigurationEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.glassfish.main.jul.cfg.LoggingPropertiesTest.PROPERTY_COUNT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * @author David Matejcek
 */
public class GlassFishLogManagerConfigurationTest {

    private static GlassFishLogManager logManager;
    private static GlassFishLogManagerConfiguration originalCfg;


    @BeforeAll
    public static void backup() {
        logManager = GlassFishLogManager.getLogManager();
        originalCfg = logManager.getConfiguration();
        // just for fun, it is a bit nicer than map.toString
        System.out.println("Original configuration: "
            + originalCfg.toStream().map(ConfigurationEntry::toString).collect(Collectors.joining(", ")));
    }


    @Test
    void toStream() {
        final LoggingProperties properties = originalCfg.toProperties();
        assertAll(
            () -> assertEquals(PROPERTY_COUNT, originalCfg.toStream().count()),
            () -> assertEquals(PROPERTY_COUNT, properties.size())
        );
        originalCfg.toStream().forEach(entry -> {
            assertEquals(entry.getValue(), originalCfg.getProperty(entry.getKey()));
        });
    }


    @Test
    void testClone() {
        final GlassFishLogManagerConfiguration clone = originalCfg.clone();
        assertAll(
            () -> assertNotSame(originalCfg.getPropertyNames(), clone.getPropertyNames()),
            () -> assertNotSame(originalCfg, clone),
            () -> assertThat(clone.getPropertyNames(), contains(originalCfg.getPropertyNames().toArray())),
            () -> assertThat(clone.getPropertyNames(), hasSize(originalCfg.getPropertyNames().size()))
        );
        for (String name : originalCfg.getPropertyNames()) {
            assertEquals(originalCfg.getProperty(name), clone.getProperty(name));
        }
    }


    @Test
    void parseStream() throws Exception {
        final GlassFishLogManagerConfiguration configuration = GlassFishLogManagerConfiguration
            .parse(GlassFishLogManagerConfigurationTest.class.getResourceAsStream("/logging.properties"));
        assertAll(
            () -> assertNotSame(originalCfg.getPropertyNames(), configuration.getPropertyNames()),
            () -> assertThat(configuration.getPropertyNames(), contains(originalCfg.getPropertyNames().toArray())),
            () -> assertThat(configuration.getPropertyNames(), hasSize(originalCfg.getPropertyNames().size()))
        );
    }



    @Test
    void parseEmptyFile() throws Exception {
        final File file = File.createTempFile("logging", "properties");
        final GlassFishLogManagerConfiguration configuration = GlassFishLogManagerConfiguration.parse(file);
        assertThat(configuration.getPropertyNames(), hasSize(0));
    }
}
