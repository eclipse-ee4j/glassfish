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

package org.glassfish.main.jul.formatter;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.junit.jupiter.api.Test;

import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.glassfish.main.jul.formatter.LogFormatDetector.P_TIME;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author David Matejcek
 */
public class OneLineFormatterTest {

    private static final String P_LEVEL_NAME = "[A-Z]+";
    private static final String P_CLASS_NAME = "[A-Za-z0-9.]*";
    private static final String P_METHOD_NAME = "[a-z]?[a-zA-Z0-9.]*";

    private static final Pattern PATTERN_SINGLELINE = Pattern.compile(
        P_TIME + "[ ]+" + P_LEVEL_NAME + "[ ]+" + "main" + "[ ]+" + P_CLASS_NAME + "\\." + P_METHOD_NAME + " .+"
    );

    @Test
    public void nullRecord() {
        assertThrows(NullPointerException.class, () -> new OneLineFormatter().format(null));
    }


    @Test
    public void nullMessage() {
        final LogRecord record = new LogRecord(Level.INFO, null);
        final String log = new OneLineFormatter().format(record);
        assertEquals("", log);
    }


    @Test
    public void anonymousLoggerAndNullSource() {
        final String message = "Ok, this works!";
        final LogRecord record = new LogRecord(Level.INFO, message);
        record.setSourceClassName(null);
        record.setSourceMethodName(null);
        record.setLoggerName(null);
        final OneLineFormatter formatter = new OneLineFormatter();
        final String log = formatter.format(record);
        assertNotNull(log, "log");
        final String[] lines = log.split("\r?\n");
        assertAll(
            () -> assertThat(lines, arrayWithSize(1)),
            () -> assertThat(lines[0], matchesPattern(PATTERN_SINGLELINE)),
            () -> assertThat(lines[0], endsWith(leftPad("INFO", 8) + leftPad("main", 21) + leftPad("", 61) + ". " + message))
        );
    }


    @Test
    public void fullLogRecordSingleLine() {
        final String message = "Ok, this works!";
        final LogRecord record = new LogRecord(Level.INFO, message);
        record.setLoggerName("the.test.logger");
        record.setSourceClassName("org.glassfish.acme.FakeClass");
        record.setSourceMethodName("fakeMethod");
        final OneLineFormatter formatter = new OneLineFormatter();
        final String log = formatter.format(record);
        assertNotNull(log, "log");
        final String[] lines = log.split("\r?\n");
        assertAll(
            () -> assertThat(lines, arrayWithSize(1)),
            () -> assertThat(lines[0], matchesPattern(PATTERN_SINGLELINE)),
            () -> assertThat(lines[0], endsWith(leftPad("INFO", 8) + leftPad("main", 21)
                    + leftPad("org.glassfish.acme.FakeClass", 61) + ".fakeMethod " + message))
        );
    }


    @Test
    public void exception() {
        final GlassFishLogRecord record = new GlassFishLogRecord(Level.SEVERE, "Failure!", false);
        record.setThrown(new RuntimeException("Ooops!"));
        final OneLineFormatter formatter = new OneLineFormatter();
        final String log = formatter.format(record);
        assertNotNull(log, "log");
        final String[] lines = log.split("\r?\n");
        assertAll(
            () -> assertThat(lines, arrayWithSize(greaterThan(20))),
            () -> assertThat(lines[0], endsWith(leftPad("SEVERE", 8) + leftPad("main", 21) + leftPad("", 61) + ". Failure!")),
            () -> assertThat(lines[1], equalTo("java.lang.RuntimeException: Ooops!"))
        );
    }
}
