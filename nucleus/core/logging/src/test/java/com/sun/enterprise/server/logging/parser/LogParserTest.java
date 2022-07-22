/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.server.logging.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LogParserTest {

    private static final String UNIFORM_SERVER_LOG_FILE = "uniform-server.log";
    private static final String ODL_SERVER_LOG_FILE = "odl-server.log";
    private static final String ONELINE_SERVER_LOG_FILE = "oneline-server.log";


    @Test
    public void testUniformLogFormatParser() throws Exception {
        LogParserListenerImpl listener = new LogParserListenerImpl();
        try (InputStream in = LogParserTest.class.getResourceAsStream(UNIFORM_SERVER_LOG_FILE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            UniformLogParser parser = new UniformLogParser();
            parser.parseLog(reader, listener);
        }
        assertThat("records", listener.records, hasSize(16));
        ParsedLogRecord record = listener.records.get(0);
        assertAll(
            () -> assertEquals("NCLS-LOGGING-00009", record.getMessageKey()),
            () -> assertEquals("Running GlassFish Version: Eclipse GlassFish  7.0.0"
                + "  (build master-b736-g2cdfe0f 2022-06-29T19:33:55+0200)", record.getMessage()),
            () -> assertEquals("glassfish 7.0", record.getProductId()),
            () -> assertEquals(LocalDate.of(2022, 06, 30), record.getDate()),
            () -> assertEquals(LocalTime.of(22, 57, 35, 349_000_000), record.getTime()),
            () -> assertEquals(OffsetDateTime.of(record.getDate(), record.getTime(), ZoneOffset.of("+02:00")), record.getTimestamp()),
            () -> assertEquals("INFO", record.getLevel()),
            () -> assertEquals(800, record.getLevelValue()),
            () -> assertThat(record.getSupplementalAttributes(), aMapWithSize(1)),
            () -> assertEquals(44L, record.getThreadId()),
            () -> assertEquals("RunLevelControllerThread-1656622655238", record.getThreadName()),
            () -> assertEquals("jakarta.enterprise.logging", record.getLogger())
        );
    }


    @Test
    public void testODLLogFormatParser() throws Exception {
        LogParserListenerImpl listener = new LogParserListenerImpl();
        try (InputStream in = LogParserTest.class.getResourceAsStream(ODL_SERVER_LOG_FILE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            ODLLogParser parser = new ODLLogParser();
            parser.parseLog(reader, listener);
        }
        assertThat("records", listener.records, hasSize(17));
        ParsedLogRecord record = listener.records.get(0);
        assertAll(
            () -> assertEquals("NCLS-LOGGING-00009", record.getMessageKey()),
            () -> assertEquals("Running GlassFish Version: Eclipse GlassFish  7.0.0"
                + "  (build master-b736-g2cdfe0f 2022-06-29T19:33:55+0200)", record.getMessage()),
            () -> assertEquals("glassfish 7.0", record.getProductId()),
            () -> assertEquals(LocalDate.of(2022, 06, 29), record.getDate()),
            () -> assertEquals(LocalTime.of(19, 55, 57, 907_000_000), record.getTime()),
            () -> assertEquals(OffsetDateTime.of(record.getDate(), record.getTime(), ZoneOffset.of("+02:00")),
                record.getTimestamp()),
            () -> assertEquals("INFO", record.getLevel()),
            () -> assertEquals(800, record.getLevelValue()),
            () -> assertThat(record.getSupplementalAttributes(), aMapWithSize(1)),
            () -> assertEquals(44L, record.getThreadId()),
            () -> assertEquals("RunLevelControllerThread-1656525357794", record.getThreadName()),
            () -> assertEquals("jakarta.enterprise.logging", record.getLogger())
        );
    }


    @Test
    public void testOneLineLogFormatParser() throws Exception {
        LogParserListenerImpl listener = new LogParserListenerImpl();
        try (InputStream in = LogParserTest.class.getResourceAsStream(ONELINE_SERVER_LOG_FILE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            OneLineLogParser parser = new OneLineLogParser();
            parser.parseLog(reader, listener);
        }
        assertThat("records", listener.records, hasSize(3));
        ParsedLogRecord record = listener.records.get(0);
        assertAll(
            () -> assertNull(record.getMessageKey()),
            () -> assertEquals("Some info message", record.getMessage()),
            () -> assertNull(record.getProductId()),
            () -> assertNull(record.getDate()),
            () -> assertEquals(LocalTime.of(14, 43, 27, 859_095_000), record.getTime()),
            () -> assertNull(record.getTimestamp()),
            () -> assertEquals("INFO", record.getLevel()),
            () -> assertNull(record.getLevelValue()),
            () -> assertThat(record.getSupplementalAttributes(), aMapWithSize(0)),
            () -> assertNull(record.getThreadId()),
            () -> assertEquals("main", record.getThreadName()),
            () -> assertEquals("org.glassfish.main.jul.handler.GlassFishLogHandlerTest.enableStandardStreamsLoggers",
                record.getLogger())
        );
    }

    private static final class LogParserListenerImpl implements LogParserListener {

        List<ParsedLogRecord> records = new ArrayList<>();

        @Override
        public void foundLogRecord(long pos, ParsedLogRecord record) {
            records.add(record);
        }
    }
}
