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

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.glassfish.main.jul.formatter.HandlerId;
import org.glassfish.main.jul.formatter.ODLLogFormatter;
import org.glassfish.main.jul.formatter.OneLineFormatter;
import org.glassfish.main.jul.test.TestFilter;
import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.Test;

import static org.glassfish.main.jul.formatter.HandlerId.forHandlerClass;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasLength;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author David Matejcek
 */
public class HandlerConfigurationHelperTest {

    @Test
    void customFormatter() {
        final HandlerId handlerId = forHandlerClass(GlassFishLogHandler.class);
        final HandlerConfigurationHelper helper = new HandlerConfigurationHelper(handlerId);
        final Formatter formatter = helper.getFormatter(ODLLogFormatter.class);
        assertThat("formatter", formatter, IsInstanceOf.instanceOf(OneLineFormatter.class));
        final String line = formatter.format(new LogRecord(Level.INFO, "something"));
        // trim to remove LF or CRLF
        assertThat("line length", line.trim(), hasLength(57));
    }


    @Test
    void defaultFormatter() {
        final HandlerId handlerId = forHandlerClass(ConsoleHandler.class);
        final HandlerConfigurationHelper helper = new HandlerConfigurationHelper(handlerId);
        final Formatter formatter = helper.getFormatter(OneLineFormatter.class);
        assertThat("formatter", formatter, IsInstanceOf.instanceOf(OneLineFormatter.class));
        final String line = formatter.format(new LogRecord(Level.INFO, "something"));
        // trim to remove LF or CRLF
        assertThat("line length", line.trim(), hasLength(116));
    }


    @Test
    void consoleHandler() {
        assertEquals("java.util.logging.ConsoleHandler.encoding", ConsoleHandlerProperty.ENCODING.getPropertyFullName());
        final HandlerId handlerId = forHandlerClass(ConsoleHandler.class);
        final HandlerConfigurationHelper helper = new HandlerConfigurationHelper(handlerId);
        assertAll(
            () -> assertEquals(null, helper.getCharset(ConsoleHandlerProperty.ENCODING, null)),
            () -> assertInstanceOf(TestFilter.class, helper.getFilter()),
            () -> assertNull(helper.getFormatter(null)),
            () -> assertEquals(null, helper.getLevel(ConsoleHandlerProperty.LEVEL, null))
        );
    }


    @Test
    void fileHandler() {
        assertEquals("java.util.logging.FileHandler.encoding", FileHandlerProperty.ENCODING.getPropertyFullName());
        final HandlerId handlerId = forHandlerClass(FileHandler.class);
        final HandlerConfigurationHelper helper = new HandlerConfigurationHelper(handlerId);
        assertAll(
            () -> assertFalse(helper.getBoolean(FileHandlerProperty.APPEND, false)),
            () -> assertEquals(1, helper.getNonNegativeInteger(FileHandlerProperty.COUNT, 1)),
            () -> assertEquals(null, helper.getCharset(FileHandlerProperty.ENCODING, null)),
            () -> assertNull(helper.getFilter()),
            () -> assertNull(helper.getFormatter(null)),
            () -> assertEquals(null, helper.getLevel(ConsoleHandlerProperty.LEVEL, null)),
            () -> assertEquals(1, helper.getNonNegativeInteger(FileHandlerProperty.LIMIT, 1)),
            () -> assertEquals(100, helper.getNonNegativeInteger(FileHandlerProperty.MAXLOCKS, 100)),
            () -> assertEquals("%h/java%u.log", helper.getString(FileHandlerProperty.PATTERN, "%h/java%u.log"))
        );
    }
}
