/*
 * Copyright (c) 2022, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import org.glassfish.main.jul.env.LoggingSystemEnvironment;
import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author David Matejcek
 */
public class SimpleLogHandlerTest {

    private static final String P_TIME = "\\d\\d:\\d\\d:\\d\\d.[\\d]{3,9}";

    private ByteArrayOutputStream os;
    private PrintStream logCollector;

    @BeforeEach
    public void setOutput() throws Exception {
        os = new ByteArrayOutputStream();
        logCollector = new PrintStream(os, true, StandardCharsets.UTF_8.name());
    }

    @AfterEach
    public void resetOutput() throws Exception {
        assertAll(
            () -> assertFalse(LoggingSystemEnvironment.getOriginalStdErr().checkError(), "stderr closed"),
            () -> assertFalse(LoggingSystemEnvironment.getOriginalStdOut().checkError(), "stdout closed")
        );
        logCollector.close();
    }


    @Test
    void standardMessage() throws Exception {
        final SimpleLogHandler handler = new SimpleLogHandler(logCollector);
        final GlassFishLogRecord record = new GlassFishLogRecord(Level.INFO, "This should log.", false);
        record.setSourceClassName("FakeClass");
        record.setSourceMethodName("fakeMethod");
        handler.publish(record);
        final String log = os.toString(StandardCharsets.UTF_8.name());
        assertNotNull(log, "log");
        final String[] lines = log.split("\r?\n");
        assertAll(
            () -> assertThat(lines, arrayWithSize(1)),
            () -> assertThat(lines[0],
                matchesPattern(P_TIME + "\\s{4}INFO\\s{17}main\\s{52}FakeClass\\.fakeMethod This should log\\."))
        );
    }


    @Test
    void exception() throws Exception {
        final SimpleLogHandler handler = new SimpleLogHandler(logCollector);
        final GlassFishLogRecord record = new GlassFishLogRecord(Level.SEVERE, "This should log.", false);
        record.setSourceClassName("FakeClass");
        record.setSourceMethodName("fakeMethod");
        record.setThrown(new IllegalStateException("Something broke."));
        handler.publish(record);
        final String log = os.toString(StandardCharsets.UTF_8.name());
        assertNotNull(log, "log");
        final String[] lines = log.split("\r?\n");
        assertAll(
            () -> assertThat(lines, arrayWithSize(greaterThan(20))),
            () -> assertThat(lines[0],
                matchesPattern(P_TIME + "\\s{2}SEVERE\\s{17}main\\s{52}FakeClass\\.fakeMethod This should log\\.")),
            () -> assertThat(lines[1], equalTo("java.lang.IllegalStateException: Something broke."))
        );
    }
}
