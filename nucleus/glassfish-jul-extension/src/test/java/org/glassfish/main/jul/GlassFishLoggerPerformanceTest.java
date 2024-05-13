/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import java.lang.System.Logger;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.glassfish.main.jul.handler.LogCollectorHandler;
import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.Timeout;

import static java.lang.System.Logger.Level.INFO;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * This tests serves as a reproducer for some known issues with message formatting
 * and level handling.
 */
@Timeout(value = 2, unit = TimeUnit.SECONDS)
public class GlassFishLoggerPerformanceTest {

    private static final Logger LOG = System.getLogger(GlassFishLoggerPerformanceTest.class.getName());
    private static final int COUNT_OF_RECORDS = 100_000;
    private static final Random RND = new Random(System.currentTimeMillis());
    private static final java.util.logging.Logger JUL_LOGGER = java.util.logging.Logger.getLogger(LOG.getName());

    private static LogCollectorHandler collector;
    private long start;

    @BeforeAll
    static void collect() {
        JUL_LOGGER.setUseParentHandlers(false);
        JUL_LOGGER.setLevel(Level.INFO);
        collector = new LogCollectorHandler(JUL_LOGGER, COUNT_OF_RECORDS, 10);
    }


    @BeforeEach
    void saveTime() {
        start = System.currentTimeMillis();
    }


    @AfterEach
    void logAndReset(TestInfo info) {
        System.out
            .println(info.getTestMethod().get().getName() + " time: " + (System.currentTimeMillis() - start) + " ms");
        final List<GlassFishLogRecord> records = collector.getAll();
        final int expectedRecords = LOG.isLoggable(INFO) ? COUNT_OF_RECORDS : 0;
        assertThat(records, hasSize(expectedRecords));
        for (LogRecord record : records) {
            assertThat("message", record.getMessage(), startsWith("Ororok orebuh, random: "));
        }
        collector.reset();
        JUL_LOGGER.setLevel(Level.INFO);
    }


    @RepeatedTest(value = 5)
    void testConcat() throws Exception {
        int i = 0;
        while (i++ < COUNT_OF_RECORDS) {
            LOG.log(INFO, "Ororok orebuh, random: " + RND.nextInt());
        }
    }


    @RepeatedTest(value = 5)
    void testSupplierWithStringFormat() throws Exception {
        int i = 0;
        while (i++ < COUNT_OF_RECORDS) {
            LOG.log(INFO, () -> String.format("Ororok orebuh, random: %s", RND.nextInt()));
        }
    }


    @RepeatedTest(value = 5)
    void testSupplierWithStringBuilder() throws Exception {
        int i = 0;
        while (i++ < COUNT_OF_RECORDS) {
            LOG.log(INFO, () -> new StringBuilder(128).append("Ororok orebuh, random: ").append(RND.nextInt()).toString());
        }
    }


    @RepeatedTest(value = 5)
    void testSupplierWithConcat() throws Exception {
        int i = 0;
        while (i++ < COUNT_OF_RECORDS) {
            LOG.log(INFO, () -> "Ororok orebuh, random: " + RND.nextInt());
        }
    }


    @RepeatedTest(value = 5)
    @Timeout(value = 50, unit = TimeUnit.MILLISECONDS)
    void testSupplierWithConcatWithLevelOff() throws Exception {
        JUL_LOGGER.setLevel(Level.OFF);
        int i = 0;
        while (i++ < COUNT_OF_RECORDS) {
            LOG.log(INFO, () -> "Ororok orebuh, random: " + RND.nextInt());
        }
    }


    @RepeatedTest(value = 5)
    void testMessageFormatter() throws Exception {
        int i = 0;
        while (i++ < COUNT_OF_RECORDS) {
            LOG.log(INFO, "Ororok orebuh, random: {0}", RND.nextInt());
        }
    }


    @RepeatedTest(value = 5)
    @Timeout(value = 50, unit = TimeUnit.MILLISECONDS)
    void testMessageFormatterWithLevelOff() throws Exception {
        JUL_LOGGER.setLevel(Level.OFF);
        int i = 0;
        while (i++ < COUNT_OF_RECORDS) {
            LOG.log(INFO, "Ororok orebuh, random: {0}", RND.nextInt());
        }
    }
}
