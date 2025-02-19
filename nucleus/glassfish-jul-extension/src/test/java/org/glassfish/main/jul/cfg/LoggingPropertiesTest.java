/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Enumeration;
import java.util.Properties;
import java.util.SortedSet;

import org.junit.jupiter.api.Test;

import static org.glassfish.main.jul.cfg.LoggingProperties.loadFrom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author David Matejcek
 */
public class LoggingPropertiesTest {

    public static final int PROPERTY_COUNT = 13;

    @Test
    void conversions() throws Exception {
        final LoggingProperties properties = loadFrom(getClass().getResourceAsStream("/logging.properties"));
        assertAll("properties: " + properties,
            () -> assertNotNull(properties),
            () -> assertThat(properties.getPropertyNames(), hasSize(PROPERTY_COUNT))
        );

        final File file = File.createTempFile("logging", "properties");
        file.deleteOnExit();
        properties.store(file, "This is a test: " + getClass());

        final LoggingProperties properties2 = loadFrom(file);
        assertAll("properties2: " + properties2,
            () -> assertNotNull(properties2),
            () -> assertThat(properties2.getPropertyNames(), hasSize(PROPERTY_COUNT))
        );

        final ByteArrayInputStream inputStream = properties2.toInputStream(null);
        final LoggingProperties properties3 = loadFrom(inputStream);
        assertEquals(properties.size(), properties3.size(), "size of properties1 and properties3");

        final SortedSet<String> names = properties3.getPropertyNames();
        assertThat(names.first(), lessThan(names.last()));
    }


    @Test
    void methods() {
        Properties properties = new Properties();
        properties.setProperty("property0", "value0");
        final LoggingProperties cfg = new LoggingProperties(properties);
        // first xzy, then abc
        cfg.setProperty("x.y.z", "XZY");
        cfg.setProperty("a.b.c", "ABC");
        Enumeration<Object> keys = cfg.keys();
        assertEquals("a.b.c", keys.nextElement());
        assertEquals("property0", keys.nextElement());
        assertEquals("x.y.z", keys.nextElement());
        assertFalse(keys.hasMoreElements());
    }
}
