/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

package com.sun.common.util.logging;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * The legacy {@code domain.xml} names are still accepted by {@code set-log-levels} and
 * {@code set-log-attributes}, so the keys they translate to must be usable in
 * {@code logging.properties}.
 */
public class LoggingXMLNamesTest {

    /**
     * {@code LogDomains.DOMAIN_ROOT} already ends with a dot, so concatenating it with
     * {@code ".level"} used to produce {@code jakarta..level} - a property for a logger
     * literally named {@code jakarta.}, which configures nothing.
     */
    @Test
    public void rootMapsToTheDomainRootLoggerLevel() {
        assertEquals("jakarta.level", LoggingXMLNames.xmltoPropsMap.get(LoggingXMLNames.root));
    }

    @Test
    public void noMappedKeyContainsAnEmptyNameSegment() {
        for (Map.Entry<String, String> entry : LoggingXMLNames.xmltoPropsMap.entrySet()) {
            String key = entry.getValue();
            assertFalse(key.contains(".."),
                () -> entry.getKey() + " maps to '" + key + "', which has an empty name segment");
            assertFalse(key.startsWith(".") || key.endsWith("."),
                () -> entry.getKey() + " maps to '" + key + "', which starts or ends with a dot");
        }
    }
}
