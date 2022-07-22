/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author sanshriv
 */
public class LogFileTest {

    private static final OffsetDateTime NOW = OffsetDateTime.now();
    private static byte[] serialized;


    @BeforeAll
    public static void prepareSerializedRecord() throws Exception {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
            BufferedOutputStream buffer = new BufferedOutputStream(bos);
            ObjectOutputStream objectOutput = new ObjectOutputStream(buffer)) {
            LogFile.LogEntry entry = new LogEntry(1L);
            entry.setLoggedDateTime(NOW);
            entry.setLoggedLevel("DEBUG");
            entry.setLoggedLoggerName("org.acme.coyote");
            entry.setLoggedNameValuePairs(null);
            entry.setLoggedProduct("Coyotus Hungrus Hungrus");
            entry.setMessageId("org.acme.messageKey");
            entry.setLoggedMessage("The serialization works!");
            objectOutput.writeObject(entry);
            objectOutput.flush();
            serialized = bos.toByteArray();
        }
    }


    @Test
    public void testLogEntryDeserialization() throws Exception {
        final LogFile.LogEntry entry;
        try (ObjectInputStream objectInput = new ObjectInputStream(new ByteArrayInputStream(serialized))) {
            entry = (LogEntry) objectInput.readObject();
        }
        assertAll(
            () -> assertNotNull(entry.getLoggedDateTime(), "DateTime"),
            () -> assertEquals(NOW, entry.getLoggedDateTime(), "DateTime"),
            () -> assertEquals("DEBUG", entry.getLoggedLevel(), "Level"),
            () -> assertEquals("org.acme.coyote", entry.getLoggedLoggerName(), "Logger"),
            () -> assertEquals("The serialization works!", entry.getLoggedMessage(), "Message"),
            () -> assertNull(entry.getLoggedNameValuePairs(), "NameValuePairs"),
            () -> assertEquals("Coyotus Hungrus Hungrus", entry.getLoggedProduct(), "Product"),
            () -> assertEquals("org.acme.messageKey", entry.getMessageId(), "MessageId"),
            () -> assertEquals(1L, entry.getRecordNumber(), "RecordNumber")
        );
    }

}
