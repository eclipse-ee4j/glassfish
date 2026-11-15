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
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    /**
     * Rewrites the first int that follows a class descriptor's end marker,
     * which for a one element array is its length and for an ArrayList its
     * size.
     */
    private static byte[] declaring(byte[] stream, int declared) {
        byte[] marker = { 0x78, 0x70, 0, 0, 0, 1 };
        outer:
        for (int i = 0; i + marker.length <= stream.length; i++) {
            for (int j = 0; j < marker.length; j++) {
                if (stream[i + j] != marker[j]) {
                    continue outer;
                }
            }
            byte[] out = stream.clone();
            java.nio.ByteBuffer.wrap(out).putInt(i + 2, declared);
            return out;
        }
        throw new AssertionError("no length found in the stream");
    }

    @Test
    @DisplayName("a body of a few dozen bytes cannot declare a long[100,000,000]")
    void aDeclaredHugeArrayIsRefusedBeforeItIsAllocated() throws Exception {
        byte[] lying = declaring(write(new long[1]), 100_000_000);
        assertTrue(lying.length < 100, "the body is only " + lying.length + " bytes");

        assertThrows(InvalidClassException.class, () -> readWithDefaults(lying));
    }

    @Test
    @DisplayName("a body that declares an ArrayList of 100,000,000 elements is refused the same way")
    void aDeclaredHugeListIsRefused() throws Exception {
        ArrayList<String> one = new ArrayList<>();
        one.add("x");
        byte[] lying = declaring(write(one), 100_000_000);

        assertThrows(InvalidClassException.class, () -> readWithDefaults(lying));
    }

    private static final int NESTING = 50;

    private static byte[] patch(byte[] stream, int declared) {
        byte[] out = stream.clone();
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.wrap(out);
        for (int i = 0; i + 10 <= out.length; i++) {
            if (out[i] == 0x78 && out[i + 1] == 0x70 && buf.getInt(i + 2) == 1) {
                buf.putInt(i + 2, declared);            // first level: after the class descriptor
            } else if (out[i] == 0x71 && buf.getInt(i + 1) == 0x7E0000 && buf.getInt(i + 5) == 1) {
                buf.putInt(i + 5, declared);            // later levels: after a reference to it
            } else if (out[i] == 0x77 && out[i + 1] == 0x08 && buf.getInt(i + 6) == 1) {
                buf.putInt(i + 6, declared);            // HashMap: mappings, in block data
            }
        }
        return out;
    }

    /** Runs the read and reports how much memory this thread allocated while doing it. */
    private long allocatedWhileReading(byte[] body) throws Exception {
        com.sun.management.ThreadMXBean threads = (com.sun.management.ThreadMXBean)
                ManagementFactory.getThreadMXBean();
        long id = Thread.currentThread().getId();
        long before = threads.getThreadAllocatedBytes(id);
        Throwable thrown = null;
        try {
            readWithDefaults(body);
        } catch (Throwable t) {
            thrown = t;
        }
        long allocated = threads.getThreadAllocatedBytes(id) - before;
        // The allocation is measured first and asserted first: a reader that
        // allocates and then fails for another reason must fail on the number.
        assertTrue(allocated < SMALL, allocated + " bytes were allocated before the stream was refused");
        assertTrue(thrown instanceof InvalidClassException, String.valueOf(thrown));
        return allocated;
    }

    private static final long SMALL = 64L * 1024 * 1024;

    @Test
    @DisplayName("a 2 KB chain of nested Object[2,097,151] is refused before the heap grows")
    void aNestedArrayChainIsRefused() throws Exception {
        Object[] chain = new Object[1];
        Object[] at = chain;
        for (int i = 1; i < NESTING; i++) {
            Object[] next = new Object[1];
            at[0] = next;
            at = next;
        }
        byte[] body = patch(write(chain), 2_097_151);
        assertTrue(body.length < 2048, "the body is " + body.length + " bytes");

        long allocated = allocatedWhileReading(body);

    }

    @Test
    @DisplayName("nested ArrayLists that each declare 2 million elements are refused before the heap grows")
    void aNestedListChainIsRefused() throws Exception {
        ArrayList<Object> chain = new ArrayList<>();
        ArrayList<Object> at = chain;
        for (int i = 1; i < NESTING; i++) {
            ArrayList<Object> next = new ArrayList<>();
            at.add(next);
            at = next;
        }
        byte[] body = patch(write(chain), 2_097_151);

        long allocated = allocatedWhileReading(body);

    }

    @Test
    @DisplayName("nested HashMaps that each declare a million mappings are refused before the heap grows")
    void aNestedMapChainIsRefused() throws Exception {
        HashMap<String, Object> chain = new HashMap<>();
        HashMap<String, Object> at = chain;
        for (int i = 1; i < NESTING; i++) {
            HashMap<String, Object> next = new HashMap<>();
            at.put("k", next);
            at = next;
        }
        byte[] body = patch(write(chain), 1_000_000);

        long allocated = allocatedWhileReading(body);

    }

    @Test
    @DisplayName("the budget stays in force around a filter an application narrowed to its own classes")
    void aNarrowedFilterKeepsTheBudget() throws Exception {
        ObjectInputFilter allowAll = info -> ObjectInputFilter.Status.ALLOWED;
        Object[] chain = new Object[1];
        Object[] at = chain;
        for (int i = 1; i < NESTING; i++) {
            Object[] next = new Object[1];
            at[0] = next;
            at = next;
        }
        byte[] body = patch(write(chain), 2_097_151);

        try (Marshaller.ObjectReader reader = marshaller.newReader(new ByteArrayInputStream(body),
                getClass().getClassLoader(), allowAll)) {
            assertThrows(InvalidClassException.class, reader::readObject);
        }
    }

    @Test
    @DisplayName("a graph 101 levels deep is refused at the default depth limit")
    void aTooDeepGraphIsRefused() throws Exception {
        Link head = new Link();
        Link tail = head;
        for (int i = 0; i < 100; i++) {
            tail.next = new Link();
            tail = tail.next;
        }

        assertThrows(InvalidClassException.class, () -> readWithDefaults(write(head)));
    }

    @Test
    @DisplayName("a stream of 250,000 objects is refused at the default reference limit")
    void tooManyReferencesAreRefused() throws Exception {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 250_000; i++) {
            numbers.add(i);
        }

        assertThrows(InvalidClassException.class, () -> readWithDefaults(write(numbers)));
    }

    @Test
    @DisplayName("a missing, non-numeric, zero or negative limit falls back to the default")
    void aBadLimitPropertyFallsBack() {
        String name = "org.glassfish.orb.http.filter.testLimit";
        try {
            System.clearProperty(name);
            assertEquals(42L, JavaSerializationMarshaller.limit("testLimit", 42L));
            for (String bad : new String[] { "-5", "0", "abc", "" }) {
                System.setProperty(name, bad);
                assertEquals(42L, JavaSerializationMarshaller.limit("testLimit", 42L), bad);
            }
            System.setProperty(name, "7");
            assertEquals(7L, JavaSerializationMarshaller.limit("testLimit", 42L));
        } finally {
            System.clearProperty(name);
        }
    }

    @Test
    @DisplayName("the budget is shared by the top-level objects of one reader (guard)")
    void theBudgetIsSharedAcrossTopLevelObjects() throws Exception {
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        try (Marshaller.ObjectWriter writer = marshaller.newWriter(bytes)) {
            writer.writeObject(new Object[2_500_000]);   // 20 MB declared each
            writer.writeObject(new Object[2_500_000]);
            writer.flush();
        }

        try (Marshaller.ObjectReader reader = marshaller.newReader(new ByteArrayInputStream(bytes.toByteArray()),
                getClass().getClassLoader(), JavaSerializationMarshaller.defaultFilter())) {
            assertEquals(2_500_000, ((Object[]) reader.readObject()).length);
            assertThrows(InvalidClassException.class, reader::readObject);
        }
    }

    @Test
    @DisplayName("a body is cut off once it is longer than the limit")
    void aLimitedStreamFailsPastItsLimit() throws Exception {
        java.io.InputStream within = new LimitedInputStream(new ByteArrayInputStream(new byte[10]), 10);
        assertEquals(10, within.readAllBytes().length);

        java.io.InputStream beyond = new LimitedInputStream(new ByteArrayInputStream(new byte[11]), 10);
        assertThrows(ProtocolException.class, beyond::readAllBytes);
    }
}
