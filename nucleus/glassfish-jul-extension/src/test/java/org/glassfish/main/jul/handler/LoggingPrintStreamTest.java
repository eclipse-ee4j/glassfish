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

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.glassfish.main.jul.JULHelperFactory;
import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.logging.Level.FINE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author David Matejcek
 */
public class LoggingPrintStreamTest {

    private LogCollectorHandler handler;
    private LoggingPrintStream stream;
    private Logger logger;


    @BeforeEach
    public void init() {
        logger = JULHelperFactory.getHelper().getJULLogger(getClass());
        logger.setLevel(Level.ALL);
        handler = new LogCollectorHandler(logger);
        stream = LoggingPrintStream.create(logger, FINE, 100, UTF_8);
    }


    @AfterEach
    public void close() {
        if (handler != null) {
            handler.close();
        }
        if (stream != null) {
            stream.close();
        }
    }


    /**
     * The stream manages Throwable.printStacktrace calls in a way depending
     * on the internal implementation in exceptions. The method (if not overriden)
     * first calls throwable.toString and then prints the stacktrace line by line.
     * That would cause creation of a log record for each line.
     * <p>
     * Test verifies that the stream is capable avoiding it so there will be just
     * a single log record created.
     */
    @Test
    public void testPrintStacktrace() throws Exception {
        final RuntimeException exception = new RuntimeException();
        exception.printStackTrace(stream);
        Thread.sleep(20L);
        final GlassFishLogRecord record = handler.pop();
        assertNotNull(record);
        assertNull(handler.pop());
        assertAll(
            () -> assertNotNull(record.getInstant()),
            () -> assertEquals(FINE, record.getLevel()),
            () -> assertEquals(logger.getName(), record.getLoggerName()),
            () -> assertNull(record.getResourceBundle()),
            () -> assertNull(record.getResourceBundleName()),
            () -> assertEquals("", record.getMessage()),
            () -> assertNull(record.getMessageKey()),
            () -> assertNull(record.getParameters()),
            () -> assertEquals(1L, record.getThreadID()),
            () -> assertEquals(Thread.currentThread().getName(), record.getThreadName()),
            () -> assertSame(exception, record.getThrown()),
            () -> assertNull(record.getSourceMethodName())
        );
    }


    @Test
    public void testPrintException() throws Exception {
        Throwable throwable = new RuntimeException("Crash!");
        // this will create the record and remember the stacktrace
        stream.println(throwable);
        // this duplicates printing the stacktrace - will be ignored.
        stream.println(throwable.getStackTrace()[0].toString());
        // this is not related to the throwable -> remembered stacktrace will be forgotten
        // as it is not expected that printing would continue.
        // Locking in Throwable.printStacktrace guarantees that those calls will
        // not be interrupted, but we do that on our own here and as we own all locks,
        // this is possible.
        stream.println("This should pass.");
        // as the stacktrace was forgotten we can simply print it.
        // This will create the third log record.
        stream.println(throwable.getStackTrace()[1].toString());
        Thread.sleep(50L);
        List<GlassFishLogRecord> records = handler.getAll();
        assertThat(records.toString(), records, hasSize(3));
    }


    /**
     * This test verifies that every call of stream's writing methods mean creation of another log
     * record. That can result in quite unuseful result so you should prevent it in your
     * applications.
     * As the stream can be used by multiple threads in parallel, it probably
     * would not be better if we would wait for the newline character.
     * @throws Exception
     */
    @Test
    public void testStreamMethods() throws Exception {
        stream.append('x');
        stream.append("-y");
        stream.append("B-z12345", 1, 3);
        stream.print(false);
        stream.print('c');
        stream.print("hmm".toCharArray());
        stream.print(0.55f);
        stream.print(1.553d);
        stream.print(50);
        stream.print(6L);
        stream.print(Level.FINE);
        stream.print("why?");
        stream.println(true);
        stream.println('d');
        stream.println("okay".toCharArray());
        stream.println(8.3f);
        stream.println(99.99d);
        stream.println(67);
        stream.println(89L);
        stream.println(Level.OFF);
        stream.println("It's fine.");
        stream.write("F".getBytes(UTF_8));
        stream.write(0);

        // This should not produce any record.
        stream.println();
        assertSame(stream, stream.printf("This: %s", "XXX"));
        // locale != charset. Affects decimal number separator.
        assertSame(stream, stream.printf(Locale.GERMAN, "こんにちは %S, debt: %f", "David", 0.0d));
        assertSame(stream, stream.format("That %s", Level.ALL));
        assertSame(stream, stream.format(Locale.GERMAN, "Number: %f.", 8.7f));

        // The internal buffer is managed by a thread, so we need to be sure it processed all records.
        stream.close();
        List<String> list = handler.getAll(LogRecord::getMessage);
        assertAll(
            () -> assertThat(list,
                containsInAnyOrder("x", "-y", "-z", "false", "c", "hmm", "0.55", "1.553", "50", "6", "FINE", "why?",
                    "true", "d", "okay", "8.3", "99.99", "67", "89", "OFF", "It's fine.", "F", "This: XXX",
                    "こんにちは DAVID, debt: 0,000000", "That ALL", "Number: 8,700000.")),
            () -> assertThat(list, hasSize(26))
        );
    }

}
