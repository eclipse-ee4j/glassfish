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

package org.glassfish.main.jul.record;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.util.logging.Level;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * @author David Matejcek
 */
public class GlassFishLogRecordTest {

    /**
     * Non-null parameters cannot be null.
     */
    @Test
    void nullParameters() throws Exception {
        final GlassFishLogRecord record = new GlassFishLogRecord(Level.ALL, null, false);
        record.setLevel(Level.INFO);
        record.setMessage(null);
        record.setLoggerName(null);
        record.setInstant(Instant.now());
        record.setMillis(1L);
        record.setParameters(null);
        record.setResourceBundleName(null);
        record.setSequenceNumber(1L);
        record.setSourceClassName(null);
        record.setSourceMethodName(null);
        record.setThreadID(1000);
        record.setThrown(null);
    }


    @Test
    void emptyParameters() throws Exception {
        final GlassFishLogRecord record = new GlassFishLogRecord(Level.CONFIG, "", false);
        record.setMessage("");
        record.setLoggerName("");
        record.setParameters(new Object[] {});
        record.setResourceBundleName("");
        record.setSourceClassName("");
        record.setSourceMethodName("");
    }


    @Test
    void serialization() throws Exception {
        final GlassFishLogRecord record = new GlassFishLogRecord(Level.CONFIG, "message", false);
        record.setLevel(Level.FINEST);
        record.setMessage("message2");
        record.setLoggerName("loggerNameX");
        record.setInstant(Instant.now());
        record.setParameters(new Object[] {3000, "value2", new NonSerializableClass()});
        record.setResourceBundleName("resourceBundleName");
        record.setSequenceNumber(1L);
        record.setSourceClassName("SourceClassName");
        record.setSourceMethodName("sourceMethodName");
        record.setThreadID(1000);
        record.setThrown(new RuntimeException("Exception Message"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ObjectOutputStream os = new ObjectOutputStream(output)) {
            os.writeObject(record);
        }

        GlassFishLogRecord record2;
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(output.toByteArray()))) {
            Object object = input.readObject();
            assertAll(
                () -> assertThat(object, instanceOf(GlassFishLogRecord.class)),
                () -> assertNotSame(object, record)
            );
            record2 = (GlassFishLogRecord) object;
        }

        assertAll(
            // we set the level and message in constructor and then reset it with setters.
            () -> assertEquals(Level.FINEST, record2.getLevel()),
            () -> assertEquals(record.getLoggerName(), record2.getLoggerName()),
            () -> assertEquals("message2", record2.getMessage()),
            () -> assertEquals(record.getMessageKey(), record2.getMessageKey()),
            () -> assertEquals(record.getMillis(), record2.getMillis()),
            () -> assertEquals(record.getInstant(), record2.getInstant()),
            () -> assertThat(record2.getParameters(), arrayWithSize(3)),
            () -> assertThat(record2.getParameters(),
                arrayContaining(equalTo("3000"), equalTo("value2"), equalTo("NonSerializableClass"))),
            () -> assertEquals(record.getResourceBundle(), record2.getResourceBundle()),
            () -> assertEquals(record.getResourceBundleName(), record2.getResourceBundleName()),
            () -> assertEquals(record.getSequenceNumber(), record2.getSequenceNumber()),
            () -> assertEquals(record.getSourceClassName(), record2.getSourceClassName()),
            () -> assertEquals(record.getSourceMethodName(), record2.getSourceMethodName()),
            () -> assertEquals(record.getThreadID(), record2.getThreadID()),
            () -> assertEquals(record.getThreadName(), record2.getThreadName()),
            () -> assertNotEquals(record.getThrown(), record2.getThrown()),
            () -> assertThat(record2.getThrown(), instanceOf(record.getThrown().getClass())),
            () -> assertEquals(record.getThrownStackTrace(), record2.getThrownStackTrace()),
            () -> assertEquals(record.getThrown().getMessage(), record2.getThrown().getMessage()),
            () -> assertEquals(record.getTime(), record2.getTime()),
            () -> assertEquals(record.toString(), record2.toString())
        );
    }


    private static final class NonSerializableClass {
        @Override
        public String toString() {
            return "NonSerializableClass";
        }
    }

}
