/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.server.logging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.glassfish.api.logging.LogHelper;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author sanshriv
 *
 */
public class LoggingAnnotationsTest {

    private static final String CANNOT_READ_TEST_CONFIGURATION_FILE_MSG = "Cannot read test configuration file ";

    private static final String TEST_EXCEPTION_MESSAGE = "Test exception message";

    private static final String TEST_CONF_FILE = "test.conf";

    private static final String FILE_SEP = System.getProperty("file.separator");

    private static final String USER_DIR = System.getProperty("user.dir");

    private static final String BASE_PATH = USER_DIR + FILE_SEP + "target";

    private static final String ODL_LOG =  BASE_PATH + FILE_SEP + "odl.log";

    private static final String ULF_LOG = BASE_PATH + FILE_SEP + "ulf.log";

    @LoggerInfo(subsystem = "Logging", description="Main logger for testing logging annotations.")
    public static final String LOGGER_NAME = "jakarta.enterprise.test.logging.annotations";

    @LogMessagesResourceBundle()
    public static final String RB_NAME = "com.sun.enterprise.server.logging.test.LogMessages";

    @LogMessageInfo(message = "Cannot read test configuration file {0}", level="SEVERE",
            cause="An exception has occurred while reading the logging configuration file.",
            action="Take appropriate action based on the exception message.")
    public static final String ERROR_READING_TEST_CONF_FILE_ID = "TEST-LOGGING-00001";

    private static final Logger LOGGER = Logger.getLogger(LOGGER_NAME, RB_NAME);

    private static final String LINE_SEP = System.getProperty("line.separator");

    @LogMessageInfo(message = "FINE Level test message", level="FINE")
    private static final String FINE_TEST_MESSAGE_ID = "TEST-LOGGING-00002";

    private static FileHandler uniformFormatHandler;

    private static FileHandler odlFormatHandler;

    private static ConsoleHandler consoleHandler;

    @BeforeAll
    public static void initializeLoggingAnnotationsTest() throws Exception {
        File basePath = new File(BASE_PATH);
        basePath.mkdirs();

        // Add a file handler with UniformLogFormatter
        uniformFormatHandler = new FileHandler(ULF_LOG);
        uniformFormatHandler.setLevel(Level.FINE);
        uniformFormatHandler.setFormatter(new UniformLogFormatter());

        // Add a file handler with ODLLogFormatter
        odlFormatHandler = new FileHandler(ODL_LOG);
        odlFormatHandler.setLevel(Level.FINE);
        odlFormatHandler.setFormatter(new ODLLogFormatter());

        consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new UniformLogFormatter());

        LOGGER.addHandler(uniformFormatHandler);
        LOGGER.addHandler(odlFormatHandler);
        Boolean enableConsoleHandler = Boolean.getBoolean(LoggingAnnotationsTest.class.getName() + ".enableConsoleHandler");
        if (enableConsoleHandler) {
            LOGGER.addHandler(consoleHandler);
        }
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.FINE);
    }

    @Test
    public void testLogMessageWithExceptionArgument() throws IOException {
        LogHelper.log(LOGGER, Level.SEVERE, ERROR_READING_TEST_CONF_FILE_ID,
                new Exception(TEST_EXCEPTION_MESSAGE), TEST_CONF_FILE);
        String[] expectedContents = new String[] {
                CANNOT_READ_TEST_CONFIGURATION_FILE_MSG + TEST_CONF_FILE,
                TEST_EXCEPTION_MESSAGE
        };
        validateLogContents(ULF_LOG, expectedContents);
        validateLogContents(ODL_LOG, expectedContents);
        System.out.println("Test passed successfully.");
    }

    @Test
    public void testFineLevelMessageWithSourceInfo() throws IOException {
        LOGGER.fine(FINE_TEST_MESSAGE_ID);
        String testMessage = "FINE Level test message";
        String[] ulfContents = new String[] {
                "ClassName=com.sun.enterprise.server.logging.LoggingAnnotationsTest;",
                "MethodName=testFineLevelMessageWithSourceInfo;",
                testMessage,
            };
        validateLogContents(ULF_LOG, ulfContents);
        String[] odlContents = new String[] {
                "[CLASSNAME: com.sun.enterprise.server.logging.LoggingAnnotationsTest]",
                "[METHODNAME: testFineLevelMessageWithSourceInfo]",
                testMessage,
            };
        validateLogContents(ODL_LOG, odlContents);
        System.out.println("Test passed successfully.");
    }

    @Test
    public void testFineLevelMessageWithoutSourceInfo() throws IOException {
        String msg = "Hello FINE World";
        LogRecord rec = new LogRecord(Level.FINE, msg);
        rec.setLoggerName(LOGGER_NAME);
        rec.setSourceClassName(null);
        rec.setSourceMethodName(null);
        LOGGER.log(rec);
        String[] ulfContents = new String[] {msg};
        String contents = validateLogContents(ULF_LOG, ulfContents);
        assertEquals(false,contents.contains("MethodName=testFineLevelMessageWithoutSourceInfo;"));
        String[] odlContents = new String[] {msg};
        contents = validateLogContents(ODL_LOG, odlContents);
        assertEquals(false,contents.contains("[METHODNAME: testFineLevelMessageWithoutSourceInfo]"));
        System.out.println("Test passed successfully.");
    }

    @Test
    public void testGetFormattedMessage() throws IOException {
        String formattedMsg = LogHelper.getFormattedMessage(
                LOGGER, ERROR_READING_TEST_CONF_FILE_ID, TEST_CONF_FILE);
        assertEquals(CANNOT_READ_TEST_CONFIGURATION_FILE_MSG + TEST_CONF_FILE,
                formattedMsg);
        System.out.println("Test passed successfully.");
    }

    private String validateLogContents(String file, String[] messages) throws IOException {
        StringBuffer buf = new StringBuffer();
        BufferedReader reader=null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line=reader.readLine()) != null) {
                buf.append(line);
                buf.append(LINE_SEP);
            }
            String contents = buf.toString();
            assertThat("File " + file + " does not contain expected log messages", contents,
                stringContainsInOrder(messages));
            return contents;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    @AfterAll
    public static void cleanupLoggingAnnotationsTest() throws Exception {
        LOGGER.removeHandler(consoleHandler);
        LOGGER.removeHandler(uniformFormatHandler);
        LOGGER.removeHandler(odlFormatHandler);

        // Flush and Close the handlers
        consoleHandler.flush();
        uniformFormatHandler.flush();
        uniformFormatHandler.close();
        odlFormatHandler.flush();
        odlFormatHandler.close();
    }

}
