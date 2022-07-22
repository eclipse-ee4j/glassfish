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

package org.glassfish.main.jul.handler;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.glassfish.main.jul.tracing.GlassFishLoggingTracer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author David Matejcek
 */
public class LogCollectorHandlerTest {

    private static Logger logger;
    private static Logger subLogger;
    private static LogCollectorHandler handler;


    @BeforeAll
    public static void initEnv() throws Exception {
        GlassFishLoggingTracer.setTracingEnabled(true);
        LogManager.getLogManager().reset();
        logger = Logger.getLogger(LogCollectorHandlerTest.class.getName());
        subLogger = Logger.getLogger(LogCollectorHandlerTest.class.getName() + ".sublog");
        handler = new LogCollectorHandler(logger);
    }


    @BeforeEach
    public void reinit() {
        logger.setLevel(Level.FINEST);
        handler.setLevel(Level.ALL);
        handler.reset();
    }


    @AfterAll
    public static void resetEverything() {
        if (handler != null) {
            handler.close();
        }
        assertThat("Nothing should remain after close", handler.getAll(), hasSize(0));
        LogManager.getLogManager().reset();
        GlassFishLoggingTracer.setTracingEnabled(false);
    }


    @Test
    public void mainLogger() {
        logger.entering(LogCollectorHandlerTest.class.getCanonicalName(), "mainLogger");
        final GlassFishLogRecord record = handler.pop();
        assertAll(
            () -> assertNotNull(record, "record"),
            () -> assertNull(handler.pop(), "more records")
        );
        assertAll(
            () -> assertEquals(Level.FINER, record.getLevel())
        );
    }


    @Test
    public void subLogger() {
        subLogger.exiting(LogCollectorHandlerTest.class.getCanonicalName(), "subLogger");
        final List<GlassFishLogRecord> records = handler.getAll();
        assertAll(
            () -> assertThat(records, hasSize(1)),
            () -> assertNull(handler.pop(), "more records")
        );
        final GlassFishLogRecord record = records.get(0);
        assertAll(
            () -> assertEquals(Level.FINER, record.getLevel())
        );
    }


    @Test
    public void handlerLevel() {
        handler.setLevel(Level.INFO);
        logger.log(Level.CONFIG, "Nothing important");
        assertNull(handler.pop(), "more records");
        logger.log(Level.SEVERE, "Important message: {0}", 42);
        assertNotNull(handler.pop(), "more records");
        logger.log(Level.SEVERE, "Some garbage for close() method");
    }
}
