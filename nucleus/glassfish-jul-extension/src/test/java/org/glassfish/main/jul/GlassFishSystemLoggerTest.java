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

package org.glassfish.main.jul;

import java.lang.System.Logger.Level;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.main.jul.JULHelperFactory.JULHelper;
import org.glassfish.main.jul.handler.LogCollectorHandler;
import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author David Matejcek
 */
public class GlassFishSystemLoggerTest {

    private Logger julLogger;
    private LogCollectorHandler handler;
    private java.lang.System.Logger systemLogger;

    @BeforeEach
    public void initHandler() {
        JULHelper helper = JULHelperFactory.getHelper();
        assertNotNull(helper, "helper");
        julLogger = helper.getJULLogger(getClass());
        handler = new LogCollectorHandler(julLogger);
        systemLogger = helper.getSystemLogger(getClass());
    }


    @AfterEach
    public void reset() {
        if (julLogger != null) {
            julLogger.setLevel(null);
            if (handler != null) {
                julLogger.removeHandler(handler);
            }
        }
    }


    @Test
    public void testSystemLogging() {
        assertTrue(systemLogger.isLoggable(Level.OFF), "OFF is loggable");
        for (System.Logger.Level level : System.Logger.Level.values()) {
            if (systemLogger.isLoggable(level)) {
                systemLogger.log(level, "This is a message with level: {0}", level);
            }
        }
        List<GlassFishLogRecord> records = handler.getAll();
        assertThat(records.toString(), records, hasSize(6));
        assertAll(
            () -> assertEquals("This is a message with level: TRACE", records.get(0).getMessage()),
            () -> assertEquals(java.util.logging.Level.FINER, records.get(0).getLevel()),
            () -> assertEquals("This is a message with level: DEBUG", records.get(1).getMessage()),
            () -> assertEquals(java.util.logging.Level.FINE, records.get(1).getLevel()),
            () -> assertEquals("This is a message with level: INFO", records.get(2).getMessage()),
            () -> assertEquals(java.util.logging.Level.INFO, records.get(2).getLevel()),
            () -> assertEquals("This is a message with level: WARNING", records.get(3).getMessage()),
            () -> assertEquals(java.util.logging.Level.WARNING, records.get(3).getLevel()),
            () -> assertEquals("This is a message with level: ERROR", records.get(4).getMessage()),
            () -> assertEquals(java.util.logging.Level.SEVERE, records.get(4).getLevel()),
            () -> assertEquals("This is a message with level: OFF", records.get(5).getMessage()),
            () -> assertEquals(java.util.logging.Level.OFF, records.get(5).getLevel())
        );
    }


    @Test
    public void testSystemLogging_OffLevel() {
        julLogger.setLevel(java.util.logging.Level.OFF);
        assertFalse(systemLogger.isLoggable(Level.OFF), "OFF is loggable");
        for (System.Logger.Level level : System.Logger.Level.values()) {
            assertFalse(systemLogger.isLoggable(level), "level: " + level);
            systemLogger.log(level, "This is an error message.", new RuntimeException());
        }
        List<GlassFishLogRecord> records = handler.getAll();
        assertThat(records.toString(), records, hasSize(0));
    }
}
