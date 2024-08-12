/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.ejb.containers;

import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.glassfish.ejb.config.EjbContainer;
import org.glassfish.kernel.KernelLoggerInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;

public class EjbContainerUtilImplTest {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private static final String FILE_SEP = System.getProperty("file.separator");
    private static final String USER_DIR = System.getProperty("user.dir");
    private static final String BASE_PATH = USER_DIR + FILE_SEP + "target";
    private static final String TEST_LOG = BASE_PATH + FILE_SEP + "test.log";
    private static final Logger logger = KernelLoggerInfo.getLogger();

    private static FileHandler handler;
    private static Logger tmpLogger;

    @BeforeAll
    public static void initializeLoggingAnnotationsTest() throws Exception {
        handler = new FileHandler(TEST_LOG);
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);

        Field logger_field = EjbContainerUtilImpl.class.getDeclaredField("_logger");
        logger_field.setAccessible(true);
        tmpLogger = (Logger) logger_field.get(null);
        logger_field.set(null, logger);
    }

    @Test
    public void testInitCorePoolSize() {
        String val = "10";
        int expect = 10;
        int actual = EjbContainerUtilImpl.initCorePoolSize(val);

        assertThat(actual, is(expect));
    }

    @Test
    public void testInitCorePoolSizeWithNull() {
        String val = null;
        int expect = EjbContainer.DEFAULT_THREAD_CORE_POOL_SIZE;
        int actual = EjbContainerUtilImpl.initCorePoolSize(val);

        assertThat(actual, is(expect));
    }

    @Test
    public void testInitCorePoolSizeWithLowerNum() throws IOException {
        String val = "-1";
        int expect = EjbContainer.DEFAULT_THREAD_CORE_POOL_SIZE;
        String[] expectedMessage = { "WARNING",
                RuntimeTagNames.THREAD_CORE_POOL_SIZE
                        + " < 0 using default value "
                        + EjbContainer.DEFAULT_THREAD_CORE_POOL_SIZE };
        int actual = EjbContainerUtilImpl.initCorePoolSize(val);

        assertThat(actual, is(expect));
        handler.flush();
        validateLogContents(expectedMessage);
    }

    @Test
    public void testInitCorePoolSizeWithParseFail() throws IOException {
        String val = "a";
        int expect = EjbContainer.DEFAULT_THREAD_CORE_POOL_SIZE;
        String[] expectedMessage = { "WARNING",
                RuntimeTagNames.THREAD_CORE_POOL_SIZE
                        + " is not a number, using default value "
                        + EjbContainer.DEFAULT_THREAD_CORE_POOL_SIZE };
        int actual = EjbContainerUtilImpl.initCorePoolSize(val);

        assertThat(actual, is(expect));
        handler.flush();
        validateLogContents(expectedMessage);
    }

    @Test
    public void testInitMaxPoolSize() {
        String val = "20";
        int corePoolSize = 10;
        int expect = 20;
        int actual = EjbContainerUtilImpl.initMaxPoolSize(val, corePoolSize);

        assertThat(actual, is(expect));
    }

    @Test
    public void testInitMaxPoolSizeWithNull() {
        String val = null;
        int corePoolSize = 10;
        int expect = EjbContainer.DEFAULT_THREAD_MAX_POOL_SIZE;
        int actual = EjbContainerUtilImpl.initMaxPoolSize(val, corePoolSize);

        assertThat(actual, is(expect));
    }

    @Test
    public void testInitMaxPoolSizeWithLowerNum() throws IOException {
        String val = "0";
        int corePoolSize = 10;
        int expect = EjbContainer.DEFAULT_THREAD_MAX_POOL_SIZE;
        String[] expectedMessage = { "WARNING",
                RuntimeTagNames.THREAD_MAX_POOL_SIZE
                        + " <= 0 using default value "
                        + EjbContainer.DEFAULT_THREAD_MAX_POOL_SIZE };
        int actual = EjbContainerUtilImpl.initMaxPoolSize(val, corePoolSize);

        assertThat(actual, is(expect));
        handler.flush();
        validateLogContents(expectedMessage);
    }

    @Test
    public void testInitMaxPoolSizeWithParseFail() throws IOException {
        String val = "a";
        int corePoolSize = 10;
        int expect = EjbContainer.DEFAULT_THREAD_MAX_POOL_SIZE;
        String[] expectedMessage = { "WARNING",
                RuntimeTagNames.THREAD_MAX_POOL_SIZE
                        + " is not a number, using default value "
                        + EjbContainer.DEFAULT_THREAD_MAX_POOL_SIZE };
        int actual = EjbContainerUtilImpl.initMaxPoolSize(val, corePoolSize);

        assertThat(actual, is(expect));
        handler.flush();
        validateLogContents(expectedMessage);
    }

    @Test
    public void testInitMaxPoolSizeWithMaxLowerThanCore() throws IOException {
        String val = "10";
        int corePoolSize = 20;
        int expect = corePoolSize;
        String[] expectedMessage = { "WARNING",
                RuntimeTagNames.THREAD_MAX_POOL_SIZE
                        + " < " + RuntimeTagNames.THREAD_CORE_POOL_SIZE
                        + " using " + RuntimeTagNames.THREAD_MAX_POOL_SIZE
                        + "=" + corePoolSize };
        int actual = EjbContainerUtilImpl.initMaxPoolSize(val, corePoolSize);

        assertThat(actual, is(expect));
        handler.flush();
        validateLogContents(expectedMessage);
    }

    @Test
    public void testInitKeepAliveSeconds() {
        String val = "10";
        long expect = 10;
        long actual = EjbContainerUtilImpl.initKeepAliveSeconds(val);

        assertThat(actual, is(expect));
    }

    @Test
    public void testInitKeepAliveSecondsWithNull() {
        String val = null;
        long expect = EjbContainer.DEFAULT_THREAD_KEEP_ALIVE_SECONDS;
        long actual = EjbContainerUtilImpl.initKeepAliveSeconds(val);

        assertThat(actual, is(expect));
    }

    @Test
    public void testInitKeepAliveSecondsWithLowerNum() throws IOException {
        String val = "-1";
        long expect = EjbContainer.DEFAULT_THREAD_KEEP_ALIVE_SECONDS;
        String[] expectedMessage = { "WARNING",
                RuntimeTagNames.THREAD_KEEP_ALIVE_SECONDS
                        + " < 0 using default value "
                        + EjbContainer.DEFAULT_THREAD_KEEP_ALIVE_SECONDS };
        long actual = EjbContainerUtilImpl.initKeepAliveSeconds(val);

        assertThat(actual, is(expect));
        handler.flush();
        validateLogContents(expectedMessage);
    }

    @Test
    public void testInitKeepAliveSecondsWithParseFail() throws IOException {
        String val = "a";
        long expect = EjbContainer.DEFAULT_THREAD_KEEP_ALIVE_SECONDS;
        String[] expectedMessage = { "WARNING",
                RuntimeTagNames.THREAD_KEEP_ALIVE_SECONDS
                        + " is not a number, using default value "
                        + EjbContainer.DEFAULT_THREAD_KEEP_ALIVE_SECONDS };
        long actual = EjbContainerUtilImpl.initKeepAliveSeconds(val);

        assertThat(actual, is(expect));
        handler.flush();
        validateLogContents(expectedMessage);
    }

    private static void validateLogContents(String[] messages) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(TEST_LOG))) {
            StringBuffer buf = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                buf.append(line);
                buf.append(LINE_SEP);
            }
            assertThat("File " + TEST_LOG + " does not contain expected log messages", buf.toString(), stringContainsInOrder(messages));
        }
    }

    @AfterAll
    public static void cleanupLoggingAnnotationsTest() throws Exception {
        logger.removeHandler(handler);
        handler.close();

        Field logger_field = EjbContainerUtilImpl.class.getDeclaredField("_logger");
        logger_field.setAccessible(true);
        logger_field.set(null, tmpLogger);
    }
}
