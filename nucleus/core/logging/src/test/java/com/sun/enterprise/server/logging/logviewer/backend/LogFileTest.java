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

package com.sun.enterprise.server.logging.logviewer.backend;

import com.sun.enterprise.server.logging.logviewer.backend.LogFile.LogEntry;

import java.io.ObjectInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author sanshriv
 */
public class LogFileTest {

    @Test
    public void testLogEntryDeserialization() throws Exception {
        try (ObjectInputStream objectInput = new ObjectInputStream(
            LogFileTest.class.getResource("logentry.bin").openStream())) {
            // Create and initialize a LogEntry from binary file
            LogFile.LogEntry entry = (LogEntry) objectInput.readObject();

            assertNotNull(entry.getLoggedDateTime(), "DateTime");
            ZoneId utc = ZoneId.of("Z");
            final ZonedDateTime dateTime = ZonedDateTime.ofInstant(entry.getLoggedDateTime().toInstant(), utc);
            final ZonedDateTime expectedDateTime = ZonedDateTime
                .of(LocalDateTime.of(2012, 11, 8, 18, 42, 26, 763000000), utc);
            assertEquals(expectedDateTime, dateTime, "DateTime");
            assertEquals("INFO", entry.getLoggedLevel(), "Level");
            assertEquals("javax.enterprise.logging", entry.getLoggedLoggerName(), "Logger");
            assertEquals("Running GlassFish Version: Oracle GlassFish Server  4.0  (build sanshriv-private)",
                entry.getLoggedMessage(), "Message");
            assertThat("NameValuePairs", entry.getLoggedNameValuePairs(),
                stringContainsInOrder("ThreadID", "ThreadName", "TimeMillis", "LevelValue", "MessageID"));
            assertEquals("44.0", entry.getLoggedProduct(), "Product");
            // FIXME: MessageID is not parsed.
//            assertEquals("NCLS-LOGGING-00009", entry.getMessageId(), "MessageId");
            assertEquals(1L, entry.getRecordNumber(), "RecordNumber");
        }
    }

}
