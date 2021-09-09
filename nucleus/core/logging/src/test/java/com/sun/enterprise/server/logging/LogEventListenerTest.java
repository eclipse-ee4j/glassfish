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

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class to exercise the LogEvent notification mechanism.
 *
 */
public class LogEventListenerTest {

    private static final String FILE_SEP = System.getProperty("file.separator");

    private static final String USER_DIR = System.getProperty("user.dir");

    private static final String BASE_PATH = USER_DIR + FILE_SEP + "target";

    private static final String TEST_EVENTS_LOG =  BASE_PATH + FILE_SEP + "test-events.log";

    private static GFFileHandler gfFileHandler;

    private static final String LOGGER_NAME = "jakarta.enterprise.test.logging.events";

    private static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);

    @BeforeAll
    public static void initializeLoggingAnnotationsTest() throws Exception {
        File basePath = new File(BASE_PATH);
        basePath.mkdirs();
        File testLog = new File(TEST_EVENTS_LOG);

        // Add a file handler with UniformLogFormatter
        gfFileHandler = new GFFileHandler();
        gfFileHandler.changeFileName(testLog);
        UniformLogFormatter formatter = new UniformLogFormatter();
        formatter.setLogEventBroadcaster(gfFileHandler);
        gfFileHandler.setFormatter(formatter );
        gfFileHandler.initializePump();

        logEventListener = new TestLogEventListener();
        gfFileHandler.addLogEventListener(logEventListener);

        LOGGER.addHandler(gfFileHandler);
        LOGGER.setLevel(Level.ALL);
        LOGGER.setUseParentHandlers(false);
    }

    private static TestLogEventListener logEventListener;

    @Test
    public void testLogEventListenerNotifications() throws Exception {
        String msg = "Test message for testLogEventListenerNotifications";
        LOGGER.info(msg);
        LogEvent event = logEventListener.logEvents.take();
        assertEquals(msg, event.getMessage());
        System.out.println("Test testLogEventListenerNotifications passed.");
    }

    @AfterAll
    public static void cleanupLoggingAnnotationsTest() throws Exception {
        logEventListener.logEvents.clear();
        LOGGER.removeHandler(gfFileHandler);
        // Flush and Close the handler
        gfFileHandler.flush();
        gfFileHandler.close();
        gfFileHandler.preDestroy();
    }

    private static class TestLogEventListener implements LogEventListener {

        private final BlockingQueue<LogEvent> logEvents = new ArrayBlockingQueue<>(100);

        @Override
        public void messageLogged(LogEvent event) {
            logEvents.add(event);
        }

    }

}
