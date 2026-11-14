/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.orb.http.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * What the default deserialization filter lets through, and how it combines
 * with the JVM-wide filter.
 */
class DeserializationLimitsTest {

    private final JavaSerializationMarshaller marshaller = new JavaSerializationMarshaller();

    /** A chain of nodes, which is what a deep object graph looks like to the filter. */
    static final class Link implements Serializable {
        private static final long serialVersionUID = 1L;
        Link next;
    }

    private byte[] write(Object value) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
            writer.writeObject(value);
            writer.flush();
        }
        return out.toByteArray();
    }

    private Object readWithDefaults(byte[] bytes) throws Exception {
        try (Marshaller.ObjectReader reader = marshaller.newReader(new ByteArrayInputStream(bytes),
                getClass().getClassLoader(), JavaSerializationMarshaller.defaultFilter())) {
            return reader.readObject();
        }
    }

    @Test
    @DisplayName("a byte array of 200,000 elements is accepted")
    void aLargeArrayIsAccepted() throws Exception {
        byte[] payload = new byte[200_000];
        payload[199_999] = 7;

        assertArrayEquals(payload, (byte[]) readWithDefaults(write(payload)));
    }

    @Test
    @DisplayName("a list of 150,000 objects is accepted")
    void manyReferencesAreAccepted() throws Exception {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 150_000; i++) {
            numbers.add(i);
        }

        assertEquals(numbers, readWithDefaults(write(numbers)));
    }

    @Test
    @DisplayName("a graph 65 levels deep is accepted")
    void aDeepGraphIsAccepted() throws Exception {
        Link head = new Link();
        Link tail = head;
        for (int i = 0; i < 64; i++) {
            tail.next = new Link();
            tail = tail.next;
        }

        Link decoded = (Link) readWithDefaults(write(head));
        int length = 1;
        for (Link l = decoded; l.next != null; l = l.next) {
            length++;
        }
        assertEquals(65, length);
    }

    @Test
    @DisplayName("what the JVM-wide filter denies stays denied under the transport's filter")
    void theJvmWideFilterStillApplies() throws Exception {
        ObjectInputFilter denyDate = info -> info.serialClass() == Date.class
                ? ObjectInputFilter.Status.REJECTED
                : ObjectInputFilter.Status.UNDECIDED;
        byte[] bytes = write(new Date(0));

        try (Marshaller.ObjectReader reader = marshaller.newReader(new ByteArrayInputStream(bytes),
                getClass().getClassLoader(), JavaSerializationMarshaller.defaultFilter(), denyDate)) {
            assertThrows(InvalidClassException.class, reader::readObject);
        }
        try (Marshaller.ObjectReader reader = marshaller.newReader(new ByteArrayInputStream(bytes),
                getClass().getClassLoader(), JavaSerializationMarshaller.defaultFilter(), null)) {
            assertEquals(new Date(0), reader.readObject());
        }
    }
}
