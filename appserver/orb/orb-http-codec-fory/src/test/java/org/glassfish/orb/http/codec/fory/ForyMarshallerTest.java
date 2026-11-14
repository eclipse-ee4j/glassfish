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

package org.glassfish.orb.http.codec.fory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.glassfish.orb.http.protocol.ChunkedInput;
import org.glassfish.orb.http.protocol.ChunkedOutput;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Marshaller;
import org.glassfish.orb.http.protocol.Marshallers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForyMarshallerTest {

    private static final String STREAMING = "org.glassfish.orb.http.codec.fory.streaming";
    private static final String THRESHOLD = ForyMarshaller.STREAMING_THRESHOLD_PROPERTY;

    private final Marshaller marshaller = new ForyMarshaller();

    private static void restore(String previous) {
        if (previous == null) {
            System.clearProperty(STREAMING);
        } else {
            System.setProperty(STREAMING, previous);
        }
    }

    private interface Body {
        void run() throws Exception;
    }

    private static void setOrClear(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }

    /** Runs the body with the two streaming properties set, or cleared when null, and restores both. */
    private static void withProperties(String streaming, String threshold, Body body) throws Exception {
        String previousStreaming = System.getProperty(STREAMING);
        String previousThreshold = System.getProperty(THRESHOLD);
        try {
            setOrClear(STREAMING, streaming);
            setOrClear(THRESHOLD, threshold);
            body.run();
        } finally {
            setOrClear(STREAMING, previousStreaming);
            setOrClear(THRESHOLD, previousThreshold);
        }
    }

    @Test
    void streamsSmallObjectsIntoChunkedOutput() throws Exception {
        String previous = System.getProperty(STREAMING);
        ChunkedOutput out = new ChunkedOutput(64);
        long before = ForyMarshaller.streamedFrameCount();
        try {
            System.setProperty(STREAMING, "true");
            try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
                writer.writeObject("small");
                writer.flush();
            }
            // The bytes are the same on either path, so only the counter
            // tells that the frame went through ChunkedOutput.beginFrame.
            assertEquals(before + 1, ForyMarshaller.streamedFrameCount());

            try (Marshaller.ObjectReader reader = marshaller.newReader(
                    new ChunkedInput(out.toByteBuffers()), getClass().getClassLoader(),
                    ObjectInputFilter.Config.createFilter("java.base/*;!*"))) {
                assertEquals("small", reader.readObject());
            }
        } finally {
            restore(previous);
        }
    }

    private static final ObjectInputFilter ALLOW_ALL = info -> ObjectInputFilter.Status.ALLOWED;

    static class Node implements Serializable {
        private static final long serialVersionUID = 1L;
        String name;
        Node peer;
        transient String secret;

        Node(String name) {
            this.name = name;
        }
    }

    static class Holder implements Serializable {
        private static final long serialVersionUID = 1L;
        Node left;
        Node right;
        List<Node> all = new ArrayList<>();
    }

    private Object roundTrip(Object value) throws Exception {
        return roundTrip(value, ALLOW_ALL);
    }

    private Object roundTrip(Object value, ObjectInputFilter filter) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
            writer.writeObject(value);
            writer.flush();
        }
        try (Marshaller.ObjectReader reader = marshaller.newReader(
                new ByteArrayInputStream(out.toByteArray()), getClass().getClassLoader(), filter)) {
            return reader.readObject();
        }
    }

    @Test
    @DisplayName("the codec is chosen simply by being on the class path")
    void theCodecIsDiscovered() {
        // The application names nothing. This module is present, so this is
        // the codec that gets used.
        assertEquals(ForyMarshaller.CODEC, Marshallers.preferred().codec());
        assertTrue(Marshallers.find(ForyMarshaller.CODEC).isPresent());
    }

    @Test
    @DisplayName("a cycle closes on the same object, as it does under Java serialization")
    void aCycleIsPreserved() throws Exception {
        Node first = new Node("first");
        Node second = new Node("second");
        first.peer = second;
        second.peer = first;

        Node decoded = (Node) roundTrip(first);
        assertEquals("first", decoded.name);
        assertEquals("second", decoded.peer.name);
        // Without reference tracking this either recurses forever or arrives
        // as an ever deeper chain of copies.
        assertSame(decoded, decoded.peer.peer);
    }

    @Test
    @DisplayName("a shared reference keeps its identity rather than arriving duplicated")
    void sharedReferencesKeepIdentity() throws Exception {
        Node shared = new Node("shared");
        Holder holder = new Holder();
        holder.left = shared;
        holder.right = shared;
        holder.all.add(shared);

        Holder decoded = (Holder) roundTrip(holder);
        assertSame(decoded.left, decoded.right);
        assertSame(decoded.left, decoded.all.get(0));
    }

    @Test
    @DisplayName("transient fields are not transmitted")
    void transientFieldsAreNotTransmitted() throws Exception {
        Node node = new Node("visible");
        node.secret = "must not travel";

        Node decoded = (Node) roundTrip(node);
        assertEquals("visible", decoded.name);
        assertNull(decoded.secret);
    }

    @Test
    @DisplayName("several objects written in sequence read back in the same order")
    void framingKeepsObjectsSeparate() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
            writer.writeObject("first");
            writer.writeObject(42);
            writer.writeObject(new Node("third"));
            writer.flush();
        }

        try (Marshaller.ObjectReader reader = marshaller.newReader(
                new ByteArrayInputStream(out.toByteArray()), getClass().getClassLoader(), ALLOW_ALL)) {
            assertEquals("first", reader.readObject());
            assertEquals(42, reader.readObject());
            assertEquals("third", ((Node) reader.readObject()).name);
        }
    }

    @Test
    @DisplayName("the deserialization filter governs this codec too")
    void theFilterIsEnforced() throws Exception {
        Node node = new Node("rejected");
        ObjectInputFilter refuseNode = info -> info.serialClass() == Node.class
                ? ObjectInputFilter.Status.REJECTED
                : ObjectInputFilter.Status.ALLOWED;

        // Changing codec must not widen what an attacker can instantiate.
        assertThrows(Exception.class, () -> roundTrip(node, refuseNode));
    }

    @Test
    @DisplayName("a reader without a filter is refused rather than left unguarded")
    void aMissingFilterIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> marshaller.newReader(new ByteArrayInputStream(new byte[0]),
                        getClass().getClassLoader(), null));
    }

    @Test
    @DisplayName("a truncated frame is reported, not silently half decoded")
    void aTruncatedFrameIsReported() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
            writer.writeObject(new Node("whole"));
            writer.flush();
        }
        byte[] full = out.toByteArray();
        byte[] cut = new byte[full.length - 3];
        System.arraycopy(full, 0, cut, 0, cut.length);

        try (Marshaller.ObjectReader reader = marshaller.newReader(
                new ByteArrayInputStream(cut), getClass().getClassLoader(), ALLOW_ALL)) {
            assertThrows(IOException.class, reader::readObject);
        }
    }

    @Test
    @DisplayName("the transport's own default filter admits ordinary classes")
    void theDefaultFilterAdmitsOrdinaryClasses() throws Exception {
        // Every other test here supplies a filter written for the test. This
        // one uses the filter the transport actually ships with, which is the
        // only way to catch a FilterInfo that the real filter refuses - as it
        // did when the counters reported -1 for "not applicable".
        Node node = new Node("ordinary");
        Node decoded = (Node) roundTrip(node, JavaSerializationMarshaller.defaultFilter());
        assertEquals("ordinary", decoded.name);
    }

    @Test
    @DisplayName("an exception keeps its stack trace, cause and message")
    void aThrowableRoundTripsIntact() throws Exception {
        IllegalStateException cause = new IllegalStateException("underlying");
        RuntimeException thrown = new RuntimeException("outer", cause);

        RuntimeException decoded = (RuntimeException) roundTrip(thrown,
                JavaSerializationMarshaller.defaultFilter());

        assertEquals("outer", decoded.getMessage());
        assertEquals("underlying", decoded.getCause().getMessage());
        // An exception that arrives with no stack trace looks like it was
        // thrown from nowhere, which is worst precisely when someone is
        // trying to find out where it came from.
        assertTrue(decoded.getStackTrace().length > 0, "stack trace was lost");
        assertTrue(decoded.getCause().getStackTrace().length > 0, "the cause's trace was lost");
    }

    /** Mirrors an application exception: a subclass carrying business state. */
    static final class Refused extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final String reasonCode;

        Refused(String message, String reasonCode, Throwable cause) {
            super(message, cause);
            this.reasonCode = reasonCode;
        }

        String reasonCode() {
            return reasonCode;
        }
    }

    @Test
    @DisplayName("an application exception subclass keeps its own state and its trace")
    void anExceptionSubclassRoundTrips() throws Exception {
        Refused thrown = new Refused("not today", "E_CLOSED", new IllegalStateException("closed"));

        Refused decoded = (Refused) roundTrip(thrown, JavaSerializationMarshaller.defaultFilter());

        assertEquals("not today", decoded.getMessage());
        assertEquals("E_CLOSED", decoded.reasonCode());
        assertTrue(decoded.getStackTrace().length > 0, "stack trace was lost");
    }

    @Test
    @DisplayName("the reference a JNDI lookup returns survives the codec")
    void aRemoteEjbReferenceRoundTrips() throws Exception {
        // This is what a lookup actually puts on the wire, and it is a record:
        // final fields, no no-argument constructor. Nothing else in these
        // tests has that shape.
        org.glassfish.orb.http.protocol.RemoteEjbReference reference =
                new org.glassfish.orb.http.protocol.RemoteEjbReference(
                        "app", "module", "", "GreeterBean", "org.example.Greeter", null);

        Object decoded = roundTrip(reference, JavaSerializationMarshaller.defaultFilter());
        assertEquals(reference, decoded);
    }

    @Test
    void theCodecTokenIsStable() {
        assertEquals("fory", marshaller.codec());
        assertNotNull(Marshallers.find("fory").orElse(null));
    }

    @Test
    @DisplayName("the default filter is one shared instance, so it can key a cache")
    void defaultFilterIsASingleton() {
        assertSame(JavaSerializationMarshaller.defaultFilter(),
                JavaSerializationMarshaller.defaultFilter());
    }

    @Test
    @DisplayName("repeated requests with the default filter reuse one reader")
    void readersAreCachedAcrossRequests() throws Exception {
        String name = "cached";
        // A throwaway loader, so entries other tests made cannot be counted.
        // The writer uses the context class loader and the reader this one:
        // WRITERS and READERS are keyed separately.
        try (URLClassLoader loader = new URLClassLoader(new URL[0], getClass().getClassLoader())) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
                writer.writeObject(new Node(name));
                writer.flush();
            }
            for (int request = 0; request < 2; request++) {
                try (Marshaller.ObjectReader reader = marshaller.newReader(
                        new ByteArrayInputStream(out.toByteArray()), loader,
                        JavaSerializationMarshaller.defaultFilter())) {
                    assertEquals(name, ((Node) reader.readObject()).name);
                }
            }
            assertEquals(1, ForyMarshaller.readerCount(loader),
                    "one reader per (loader, filter), not one per request");
        }
    }

    @Test
    @DisplayName("a streamed frame after a nearly full chunk round-trips")
    void streamedFrameAfterNearlyFullChunkRoundTrips() throws Exception {
        String previous = System.getProperty(STREAMING);
        System.setProperty(STREAMING, "true");
        try {
            ChunkedOutput out = new ChunkedOutput(16);
            out.write(new byte[13]);
            try (Marshaller.ObjectWriter w = marshaller.newWriter(out)) {
                w.writeObject("x".repeat(4096));
                w.flush();
            }

            ChunkedInput in = new ChunkedInput(out.toByteBuffers());
            assertEquals(13, in.readNBytes(13).length);
            try (Marshaller.ObjectReader r = marshaller.newReader(in, getClass().getClassLoader(), ALLOW_ALL)) {
                assertEquals("x".repeat(4096), r.readObject());
            }
        } finally {
            restore(previous);
        }
    }

    @Test
    @DisplayName("a streamed byte[] frame over several chunks has a correct header")
    void streamedFrameSpanningManyChunks() throws Exception {
        String previous = System.getProperty(STREAMING);
        System.setProperty(STREAMING, "true");
        try {
            byte[] value = new byte[200];
            new Random(9L).nextBytes(value);
            ChunkedOutput out = new ChunkedOutput(64);
            out.write(new byte[62]);
            try (Marshaller.ObjectWriter w = marshaller.newWriter(out)) {
                w.writeObject(value);
                w.flush();
            }

            // The 5 byte header starts at 62 and straddles the first chunk boundary.
            byte[] bytes = out.toByteArray();
            assertEquals(0, bytes[62]);
            assertEquals(out.size() - 67, ByteBuffer.wrap(bytes, 63, 4).getInt());
            assertTrue(out.size() > 192, "the frame must span four chunks");
            int buffered = 0;
            for (ByteBuffer b : out.toByteBuffers()) {
                buffered += b.remaining();
            }
            assertEquals(out.size(), buffered, "no chunk may carry padding");

            ChunkedInput in = new ChunkedInput(out.toByteBuffers());
            assertEquals(62, in.readNBytes(62).length);
            try (Marshaller.ObjectReader r = marshaller.newReader(in, getClass().getClassLoader(), ALLOW_ALL)) {
                assertArrayEquals(value, (byte[]) r.readObject());
            }
        } finally {
            restore(previous);
        }
    }

    @Test
    @DisplayName("a header straddling a chunk boundary round-trips at every offset")
    void streamedHeaderStraddlingChunkBoundary() throws Exception {
        String previous = System.getProperty(STREAMING);
        System.setProperty(STREAMING, "true");
        try {
            int chunk = 32;
            for (int prefix = chunk - 4; prefix < chunk; prefix++) {
                ChunkedOutput out = new ChunkedOutput(chunk);
                out.write(new byte[prefix]);
                try (Marshaller.ObjectWriter w = marshaller.newWriter(out)) {
                    w.writeObject("straddle " + prefix);
                    w.flush();
                }
                ChunkedInput in = new ChunkedInput(out.toByteBuffers());
                assertEquals(prefix, in.readNBytes(prefix).length);
                try (Marshaller.ObjectReader r = marshaller.newReader(
                        in, getClass().getClassLoader(), ALLOW_ALL)) {
                    assertEquals("straddle " + prefix, r.readObject());
                }
            }
        } finally {
            restore(previous);
        }
    }

    @Test
    @DisplayName("a large String, a small Integer and a large byte[] in sequence round-trip")
    void mixedSequenceOverSmallChunks() throws Exception {
        String previous = System.getProperty(STREAMING);
        System.setProperty(STREAMING, "true");
        try {
            String text = "y".repeat(300);
            byte[] blob = new byte[500];
            new Random(4L).nextBytes(blob);
            ChunkedOutput out = new ChunkedOutput(32);
            out.write(new byte[30]);
            try (Marshaller.ObjectWriter w = marshaller.newWriter(out)) {
                w.writeObject(text);
                w.writeObject(7);
                w.writeObject(blob);
                w.flush();
            }

            ChunkedInput in = new ChunkedInput(out.toByteBuffers());
            assertEquals(30, in.readNBytes(30).length);
            try (Marshaller.ObjectReader r = marshaller.newReader(in, getClass().getClassLoader(), ALLOW_ALL)) {
                assertEquals(text, r.readObject());
                assertEquals(7, r.readObject());
                assertArrayEquals(blob, (byte[]) r.readObject());
            }
        } finally {
            restore(previous);
        }
    }

    /**
     * An object that cannot be serialized, whichever way the codec goes about
     * it: it asks to be replaced, has a custom write method and is
     * externalizable, and every one of those throws.
     */
    public static class Exploding implements java.io.Externalizable {
        private static final long serialVersionUID = 1L;

        public Exploding() {
        }

        private Object writeReplace() {
            throw new IllegalStateException("cannot serialize");
        }

        private void writeObject(java.io.ObjectOutputStream out) {
            throw new IllegalStateException("cannot serialize");
        }

        @Override
        public void writeExternal(java.io.ObjectOutput out) {
            throw new IllegalStateException("cannot serialize");
        }

        @Override
        public void readExternal(java.io.ObjectInput in) {
            throw new IllegalStateException("cannot deserialize");
        }
    }

    @Test
    @DisplayName("a streamed frame whose serialization fails leaves nothing behind")
    void failedStreamedWriteLeavesNoFrame() throws Exception {
        withProperties("true", null, () -> {
            for (int prefix : new int[] {0, 3, 29, 32, 40}) {
                ChunkedOutput out = new ChunkedOutput(16);
                out.write(new byte[prefix]);
                long streamed = ForyMarshaller.streamedFrameCount();
                try (Marshaller.ObjectWriter w = marshaller.newWriter(out)) {
                    assertThrows(Exception.class, () -> w.writeObject(new Exploding()));
                    // Without the abort the stream now ends in a header that
                    // announces an empty frame of kind 0.
                    assertEquals(prefix, out.size(), "prefix " + prefix);
                    assertEquals(streamed, ForyMarshaller.streamedFrameCount(), "a failed frame is not counted");

                    // The writer is still usable, and the next object follows directly.
                    w.writeObject("after");
                    w.flush();
                }
                ChunkedInput in = new ChunkedInput(out.toByteBuffers());
                assertEquals(prefix, in.readNBytes(prefix).length);
                try (Marshaller.ObjectReader r = marshaller.newReader(
                        in, getClass().getClassLoader(), ALLOW_ALL)) {
                    assertEquals("after", r.readObject());
                }
            }
        });
    }

    private static long streamedFramesFor(Object value) throws Exception {
        long before = ForyMarshaller.streamedFrameCount();
        ChunkedOutput out = new ChunkedOutput(64);
        Marshaller marshaller = new ForyMarshaller();
        try (Marshaller.ObjectWriter w = marshaller.newWriter(out)) {
            w.writeObject(value);
            w.flush();
        }
        return ForyMarshaller.streamedFrameCount() - before;
    }

    @Test
    @DisplayName("the streaming threshold property: negative disables, zero streams everything, junk means the default")
    void thresholdPropertyIsInterpreted() throws Exception {
        withProperties(null, "-1", () -> {
            assertEquals(0, streamedFramesFor(new byte[100_000]));
        });
        withProperties(null, "0", () -> {
            assertEquals(1, streamedFramesFor("a"));
            assertEquals(1, streamedFramesFor(new byte[1]));
        });
        for (String junk : new String[] {"abc", "", "  ", "4096"}) {
            withProperties(null, junk, () -> {
                assertEquals(0, streamedFramesFor(new byte[4095]), "threshold '" + junk + "'");
                assertEquals(1, streamedFramesFor(new byte[4096]), "threshold '" + junk + "'");
            });
        }
        withProperties(null, " 8192 ", () -> {
            assertEquals(0, streamedFramesFor(new byte[4096]));
            assertEquals(1, streamedFramesFor(new byte[8192]));
        });
        withProperties(null, null, () -> {
            assertEquals(0, streamedFramesFor(new byte[4095]));
            assertEquals(1, streamedFramesFor(new byte[4096]));
            // Two bytes per character: 2047 characters are 4094 bytes, 2048 are 4096.
            assertEquals(0, streamedFramesFor("c".repeat(2047)));
            assertEquals(1, streamedFramesFor("c".repeat(2048)));
        });
    }

    @Test
    @DisplayName("the properties are read when the writer is created, not for every object")
    void propertiesAreReadOncePerWriter() throws Exception {
        withProperties("false", "-1", () -> {
            Marshaller m = new ForyMarshaller();
            ChunkedOutput out = new ChunkedOutput(64);
            long before = ForyMarshaller.streamedFrameCount();
            try (Marshaller.ObjectWriter w = m.newWriter(out)) {
                System.setProperty(STREAMING, "true");
                w.writeObject("small");
                w.flush();
            }
            assertEquals(before, ForyMarshaller.streamedFrameCount());

            // A writer created afterwards sees the new value.
            try (Marshaller.ObjectWriter w = m.newWriter(new ChunkedOutput(64))) {
                w.writeObject("small");
                w.flush();
            }
            assertEquals(before + 1, ForyMarshaller.streamedFrameCount());
        });
    }

    @Test
    @DisplayName("small frames are decoded without the stream decoder's per-frame buffer")
    void smallFramesDecodeWithoutStreamBuffer() throws Exception {
        java.lang.management.ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        Assumptions.assumeTrue(bean instanceof com.sun.management.ThreadMXBean
                && ((com.sun.management.ThreadMXBean) bean).isThreadAllocatedMemorySupported());
        com.sun.management.ThreadMXBean allocation = (com.sun.management.ThreadMXBean) bean;

        withProperties(null, null, () -> {
            int frames = 10_000;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (Marshaller.ObjectWriter w = marshaller.newWriter(out)) {
                for (int i = 0; i < frames; i++) {
                    w.writeObject("hello");
                }
                w.flush();
            }
            try (Marshaller.ObjectReader r = marshaller.newReader(
                    new ByteArrayInputStream(out.toByteArray()), getClass().getClassLoader(), ALLOW_ALL)) {
                for (int i = 0; i < 1_000; i++) {
                    assertEquals("hello", r.readObject());
                }
                long before = allocation.getCurrentThreadAllocatedBytes();
                for (int i = 0; i < 9_000; i++) {
                    assertEquals("hello", r.readObject());
                }
                long perFrame = (allocation.getCurrentThreadAllocatedBytes() - before) / 9_000;
                // The stream decoder allocates a 4096 byte buffer per frame
                // before it reads anything.
                assertTrue(perFrame < 2048, "a 14 byte frame allocated " + perFrame + " bytes");
            }
        });
    }

    @Test
    @DisplayName("frames decode the same whether they take the array path or the stream path")
    void bothDecodePathsAgree() throws Exception {
        for (String threshold : new String[] {"-1", "0", "4096", "100000"}) {
            withProperties(null, threshold, () -> {
                byte[] blob = new byte[30_000];
                new Random(8L).nextBytes(blob);
                String big = "z".repeat(10_000);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try (Marshaller.ObjectWriter w = marshaller.newWriter(out)) {
                    w.writeObject("first");
                    w.writeObject(big);
                    w.writeObject(42);
                    w.writeObject(new Node("node"));
                    w.writeObject(blob);
                    w.writeObject("last");
                    w.flush();
                }
                try (Marshaller.ObjectReader r = marshaller.newReader(
                        new ByteArrayInputStream(out.toByteArray()), getClass().getClassLoader(), ALLOW_ALL)) {
                    // Each frame must end exactly where the next begins.
                    assertEquals("first", r.readObject(), threshold);
                    assertEquals(big, r.readObject(), threshold);
                    assertEquals(42, r.readObject(), threshold);
                    assertEquals("node", ((Node) r.readObject()).name, threshold);
                    assertArrayEquals(blob, (byte[]) r.readObject(), threshold);
                    assertEquals("last", r.readObject(), threshold);
                }
            });
        }
    }

    @Test
    @DisplayName("a large streamed frame over many small chunks decodes from the stream")
    void largeStreamedFrameRoundTrips() throws Exception {
        withProperties(null, null, () -> {
            byte[] value = new byte[100_000];
            new Random(2L).nextBytes(value);
            ChunkedOutput out = new ChunkedOutput(1_000);
            out.write(new byte[7]);
            try (Marshaller.ObjectWriter w = marshaller.newWriter(out)) {
                w.writeObject(value);
                w.writeObject("tail");
                w.flush();
            }
            ChunkedInput in = new ChunkedInput(out.toByteBuffers());
            assertEquals(7, in.readNBytes(7).length);
            try (Marshaller.ObjectReader r = marshaller.newReader(in, getClass().getClassLoader(), ALLOW_ALL)) {
                assertArrayEquals(value, (byte[]) r.readObject());
                assertEquals("tail", r.readObject());
            }
        });
    }

    @Test
    @DisplayName("a large frame that is cut short is reported as an IOException")
    void truncatedLargeFrameIsAnIOException() throws Exception {
        withProperties(null, null, () -> {
            byte[] value = new byte[10_000];
            new Random(6L).nextBytes(value);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (Marshaller.ObjectWriter w = marshaller.newWriter(out)) {
                w.writeObject(value);
                w.flush();
            }
            byte[] full = out.toByteArray();
            for (int missing : new int[] {1, 3, 5_000, full.length - 6}) {
                try (Marshaller.ObjectReader r = marshaller.newReader(
                        new ByteArrayInputStream(Arrays.copyOf(full, full.length - missing)),
                        getClass().getClassLoader(), ALLOW_ALL)) {
                    EOFException e = assertThrows(EOFException.class, r::readObject, "missing " + missing);
                    assertTrue(e.getMessage().contains("truncated"), e.getMessage());
                }
            }
        });
    }

    /** Delivers the bytes it was given up to a point, then fails the way a dropped connection does. */
    private static final class Flaky extends InputStream {
        private final byte[] data;
        private final int failAt;
        private int position;

        Flaky(byte[] data, int failAt) {
            this.data = data;
            this.failAt = failAt;
        }

        @Override
        public int read() throws IOException {
            if (position >= failAt) {
                throw new IOException("connection reset");
            }
            return data[position++] & 0xFF;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (position >= failAt) {
                throw new IOException("connection reset");
            }
            int n = Math.min(len, failAt - position);
            System.arraycopy(data, position, b, off, n);
            position += n;
            return n;
        }
    }

    @Test
    @DisplayName("a connection that fails in the middle of a large frame surfaces its own IOException")
    void deliveryFailureKeepsItsType() throws Exception {
        withProperties(null, null, () -> {
            byte[] value = new byte[10_000];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (Marshaller.ObjectWriter w = marshaller.newWriter(out)) {
                w.writeObject(value);
                w.flush();
            }
            try (Marshaller.ObjectReader r = marshaller.newReader(
                    new Flaky(out.toByteArray(), 6_000), getClass().getClassLoader(), ALLOW_ALL)) {
                IOException e = assertThrows(IOException.class, r::readObject);
                assertEquals("connection reset", e.getMessage());
            }
        });
    }
}
