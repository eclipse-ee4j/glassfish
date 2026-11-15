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
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForyMarshallerTest {

    private static final String STREAMING = "org.glassfish.orb.http.codec.fory.streaming";
    private static final String THRESHOLD = ForyMarshaller.STREAMING_THRESHOLD_PROPERTY;

    private static final String DECODE = ForyMarshaller.STREAMING_DECODE_THRESHOLD_PROPERTY;

    private final Marshaller marshaller = new ForyMarshaller();

    /** Runs the body with the decode threshold set, or cleared when null, and restores it. */
    private static void withDecodeThreshold(String decode, Body body) throws Exception {
        String previous = System.getProperty(DECODE);
        try {
            setOrClear(DECODE, decode);
            body.run();
        } finally {
            setOrClear(DECODE, previous);
        }
    }

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
            assertThrows(EOFException.class, reader::readObject);
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
    @DisplayName("[guard] the streaming threshold property: negative disables, zero streams everything, junk means the default")
    void thresholdPropertyIsInterpreted() throws Exception {
        // GUARD / characterization test of the writer threshold: it documents
        // behaviour the change under review did not alter, so it is not
        // expected to fail on a revert.
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
    @DisplayName("[guard] frames decode the same whether they take the array path or the stream path")
    void bothDecodePathsAgree() throws Exception {
        // GUARD / characterization test: both paths must give the same
        // objects; it is not expected to fail on a revert.
        for (String threshold : new String[] {null, "-1", "0", "4096", "100000"}) {
            withDecodeThreshold(threshold, () -> {
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
    @DisplayName("[guard] a large streamed frame over many small chunks decodes from the stream")
    void largeStreamedFrameRoundTrips() throws Exception {
        // GUARD / characterization test: the round trip works on both paths.
        withDecodeThreshold("0", () -> {
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
            long before = ForyMarshaller.streamedDecodeCount();
            try (Marshaller.ObjectReader r = marshaller.newReader(in, getClass().getClassLoader(), ALLOW_ALL)) {
                assertArrayEquals(value, (byte[]) r.readObject());
                assertEquals("tail", r.readObject());
            }
            assertEquals(before + 2, ForyMarshaller.streamedDecodeCount());
        });
    }

    @Test
    @DisplayName("[guard] a large frame that is cut short is reported as an IOException")
    void truncatedLargeFrameIsAnIOException() throws Exception {
        // GUARD / characterization test: the stream path already reported a
        // cut frame as an EOFException before this round.
        withDecodeThreshold("0", () -> {
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
        /** The exceptions this stream threw. */
        final List<IOException> thrown = new ArrayList<>();

        Flaky(byte[] data, int failAt) {
            this.data = data;
            this.failAt = failAt;
        }

        private IOException fail() {
            IOException e = new java.net.SocketTimeoutException("connection reset");
            thrown.add(e);
            return e;
        }

        @Override
        public int read() throws IOException {
            if (position >= failAt) {
                throw fail();
            }
            return data[position++] & 0xFF;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (position >= failAt) {
                throw fail();
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
        withDecodeThreshold("0", () -> {
            byte[] value = new byte[10_000];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (Marshaller.ObjectWriter w = marshaller.newWriter(out)) {
                w.writeObject(value);
                w.flush();
            }
            Flaky flaky = new Flaky(out.toByteArray(), 6_000);
            try (Marshaller.ObjectReader r = marshaller.newReader(
                    flaky, getClass().getClassLoader(), ALLOW_ALL)) {
                IOException e = assertThrows(IOException.class, r::readObject);
                // The connection's own exception arrives, unmodified.
                assertEquals(1, flaky.thrown.size());
                assertSame(flaky.thrown.get(0), e);
                assertTrue(e instanceof java.net.SocketTimeoutException, e.getClass().getName());
                assertEquals("connection reset", e.getMessage());
                assertEquals(0, e.getSuppressed().length);
                assertNoCircularReference(e);
            }
        });
    }

    /** Fails if walking causes and suppressed exceptions from the root ever returns to one on the current path. */
    private static void assertNoCycle(Throwable root) {
        Set<Throwable> onPath = Collections.newSetFromMap(new IdentityHashMap<>());
        walk(root, onPath);
    }

    private static void walk(Throwable t, Set<Throwable> onPath) {
        assertTrue(onPath.add(t), "exception graph has a cycle through " + t);
        if (t.getCause() != null) {
            walk(t.getCause(), onPath);
        }
        for (Throwable suppressed : t.getSuppressed()) {
            walk(suppressed, onPath);
        }
        onPath.remove(t);
    }

    @Test
    @DisplayName("a cut stream gives an EOFException with no circular reference")
    void truncatedStreamedFrameHasNoCyclicGraph() throws Exception {
        withDecodeThreshold("0", () -> {
            byte[] value = new byte[10_000];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (Marshaller.ObjectWriter w = marshaller.newWriter(out)) {
                w.writeObject(value);
                w.flush();
            }
            byte[] full = out.toByteArray();
            try (Marshaller.ObjectReader r = marshaller.newReader(
                    new ByteArrayInputStream(Arrays.copyOf(full, full.length - 5_000)),
                    getClass().getClassLoader(), ALLOW_ALL)) {
                EOFException e = assertThrows(EOFException.class, r::readObject);
                assertTrue(e.getMessage().contains("truncated"), e.getMessage());
                assertEquals(0, e.getSuppressed().length);
                assertNoCircularReference(e);
            }
        });
    }

    /** The printed trace is what an operator sees; it marks a node it reaches twice. */
    private static void assertNoCircularReference(Throwable t) {
        assertNoCycle(t);
        java.io.StringWriter text = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(text));
        assertTrue(!text.toString().contains("CIRCULAR REFERENCE"), text.toString());
    }

    private static int frameLength(byte[] encoded) {
        return ByteBuffer.wrap(encoded, 1, 4).getInt();
    }

    private static byte[] encodeOne(Marshaller m, Object value) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Marshaller.ObjectWriter w = m.newWriter(out)) {
            w.writeObject(value);
            w.flush();
        }
        return out.toByteArray();
    }

    private long streamedDecodesFor(byte[] encoded) throws Exception {
        long before = ForyMarshaller.streamedDecodeCount();
        try (Marshaller.ObjectReader r = marshaller.newReader(
                new ByteArrayInputStream(encoded), getClass().getClassLoader(), ALLOW_ALL)) {
            r.readObject();
        }
        return ForyMarshaller.streamedDecodeCount() - before;
    }

    @Test
    @DisplayName("a frame is decoded from the stream when its length is at least the decode threshold, not when it is shorter")
    void decodeThresholdBoundaryIsInclusive() throws Exception {
        byte[] encoded = encodeOne(marshaller, new byte[10_000]);
        int length = frameLength(encoded);
        withDecodeThreshold(Integer.toString(length), () -> assertEquals(1, streamedDecodesFor(encoded)));
        withDecodeThreshold(Integer.toString(length - 1), () -> assertEquals(1, streamedDecodesFor(encoded)));
        withDecodeThreshold(Integer.toString(length + 1), () -> assertEquals(0, streamedDecodesFor(encoded)));
    }

    @Test
    @DisplayName("the decode threshold property: off when unset, blank, junk or negative; zero streams every Fory frame")
    void decodeThresholdPropertyIsInterpreted() throws Exception {
        byte[] encoded = encodeOne(marshaller, new byte[10_000]);
        for (String off : new String[] {null, "", "  ", "abc", "-1", "-5"}) {
            withDecodeThreshold(off, () -> assertEquals(0, streamedDecodesFor(encoded), "value '" + off + "'"));
        }
        withDecodeThreshold("0", () -> assertEquals(1, streamedDecodesFor(encoded)));
        withDecodeThreshold(" 100 ", () -> assertEquals(1, streamedDecodesFor(encoded)));
        // The writer's threshold does not turn the reader's path on.
        withProperties(null, "0", () -> withDecodeThreshold(null, () -> assertEquals(0, streamedDecodesFor(encoded))));
    }

    @Test
    @DisplayName("by default even a large frame is decoded from an array")
    void largeFramesDecodeFromAnArrayByDefault() throws Exception {
        withProperties(null, null, () -> withDecodeThreshold(null, () -> {
            byte[] encoded = encodeOne(marshaller, new byte[1 << 20]);
            assertEquals(0, streamedDecodesFor(encoded));
        }));
    }

    private static com.sun.management.ThreadMXBean allocationBean() {
        java.lang.management.ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        Assumptions.assumeTrue(bean instanceof com.sun.management.ThreadMXBean
                && ((com.sun.management.ThreadMXBean) bean).isThreadAllocatedMemorySupported());
        return (com.sun.management.ThreadMXBean) bean;
    }

    @Test
    @DisplayName("a header that promises far more than arrives allocates only what arrived")
    void promisedLengthIsNotAllocated() throws Exception {
        com.sun.management.ThreadMXBean allocation = allocationBean();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        java.io.DataOutputStream data = new java.io.DataOutputStream(bytes);
        data.writeByte(0);
        data.writeInt(1_000_000_000);
        data.write(new byte[10]);
        byte[] cut = bytes.toByteArray();
        withDecodeThreshold(null, () -> {
            for (int attempt = 0; attempt < 6; attempt++) {
                try (Marshaller.ObjectReader r = marshaller.newReader(
                        new ByteArrayInputStream(cut), getClass().getClassLoader(), ALLOW_ALL)) {
                    long before = allocation.getCurrentThreadAllocatedBytes();
                    assertThrows(EOFException.class, r::readObject);
                    long used = allocation.getCurrentThreadAllocatedBytes() - before;
                    if (attempt >= 3) {
                        assertTrue(used < 40_000, "allocated " + used + " bytes for 10 received");
                    }
                }
            }
        });
    }

    @Test
    @DisplayName("a streamed decode of a 1 MiB byte[] frame allocates the value, one small buffer and no more")
    void streamedDecodeOfLargeFrameStaysBounded() throws Exception {
        com.sun.management.ThreadMXBean allocation = allocationBean();
        byte[] value = new byte[1 << 20];
        byte[] encoded = encodeOne(marshaller, value);
        withDecodeThreshold("0", () -> {
            long best = Long.MAX_VALUE;
            for (int i = 0; i < 12; i++) {
                try (Marshaller.ObjectReader r = marshaller.newReader(
                        new ByteArrayInputStream(encoded), getClass().getClassLoader(), ALLOW_ALL)) {
                    long before = allocation.getCurrentThreadAllocatedBytes();
                    assertEquals(value.length, ((byte[]) r.readObject()).length);
                    best = Math.min(best, allocation.getCurrentThreadAllocatedBytes() - before);
                }
            }
            // Measured in CI: 2.23 MB with the override, 5.24 MB without it,
            // because without available() the decoder grows its buffer by
            // doubling and copies it each time.
            assertTrue(best < 3 * (1 << 20), "a 1 MiB frame allocated " + best + " bytes");
        });
    }

    @Test
    @DisplayName("a streamed decode behind a header that promises 1 GB allocates at most the 64 KiB start buffer")
    void streamedDecodeDoesNotAllocateThePromisedLength() throws Exception {
        com.sun.management.ThreadMXBean allocation = allocationBean();
        List<String> valid = new ArrayList<>();
        for (int i = 0; i < 20_000; i++) {
            valid.add("item-" + i);
        }
        byte[] whole = encodeOne(marshaller, valid);
        // The first 10 KB of a valid frame of a list of 20,000 strings, behind
        // a header that promises 1 GB: the decoder reads real elements, which
        // it can only do incrementally, and then the stream ends.
        byte[] cut = Arrays.copyOf(whole, 5 + 10_000);
        ByteBuffer.wrap(cut, 1, 4).putInt(1_000_000_000);
        byte[] warm = encodeOne(marshaller, new byte[100]);
        withDecodeThreshold("0", () -> {
            for (int i = 0; i < 3; i++) {
                streamedDecodesFor(warm);
            }
            try (Marshaller.ObjectReader r = marshaller.newReader(
                    new ByteArrayInputStream(cut), getClass().getClassLoader(), ALLOW_ALL)) {
                long before = allocation.getCurrentThreadAllocatedBytes();
                EOFException e = assertThrows(EOFException.class, r::readObject);
                long used = allocation.getCurrentThreadAllocatedBytes() - before;
                assertTrue(e.getMessage().contains("truncated"), e.getMessage());
                assertTrue(used < (1 << 20), "allocated " + used + " bytes for 10 KB received");
            }
        });
    }

    @Test
    @DisplayName("the stream decoder starts with at most 64 KiB, and at least 16 bytes")
    void streamBufferIsCapped() {
        assertEquals(16, ForyMarshaller.streamBufferSize(0));
        assertEquals(100, ForyMarshaller.streamBufferSize(100));
        assertEquals(64 * 1024, ForyMarshaller.streamBufferSize(1 << 20));
        assertEquals(64 * 1024, ForyMarshaller.streamBufferSize(Integer.MAX_VALUE));
    }

    @Test
    @DisplayName("the limiting stream reports what is left, caps reads at the frame and returns 0 for a zero length read")
    void limitedInputStreamContract() throws IOException {
        ForyMarshaller.LimitedInputStream limited = new ForyMarshaller.LimitedInputStream(
                new ByteArrayInputStream(new byte[100]), 10);
        assertEquals(10, limited.available());
        byte[] b = new byte[50];
        assertEquals(0, limited.read(b, 0, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> limited.read(b, -1, 0));
        assertEquals(4, limited.read(b, 0, 4));
        assertEquals(6, limited.available());
        assertEquals(6, limited.read(b, 0, 50));
        assertEquals(0, limited.available());
        assertEquals(0, limited.read(b, 0, 0), "a zero length read returns 0 even at the end of the frame");
        assertEquals(-1, limited.read(b, 0, 1));

        ForyMarshaller.LimitedInputStream shortSource = new ForyMarshaller.LimitedInputStream(
                new ByteArrayInputStream(new byte[3]), 10);
        assertEquals(3, shortSource.available(), "bounded by what the source has");
    }

    @Test
    @DisplayName("what the JVM-wide filter denies is denied to this codec too")
    void theJvmWideFilterAppliesToForyToo() {
        ObjectInputFilter denyDate = info -> info.serialClass() == Date.class
                ? ObjectInputFilter.Status.REJECTED
                : ObjectInputFilter.Status.UNDECIDED;
        ClassLoader loader = getClass().getClassLoader();

        assertFalse(new FilterBackedTypeChecker(loader, ALLOW_ALL, denyDate).checkType(null, Date.class.getName()));
        assertTrue(new FilterBackedTypeChecker(loader, ALLOW_ALL, null).checkType(null, Date.class.getName()));
    }

    @Test
    @DisplayName("the default filter's limits do not refuse ordinary large payloads on this codec either")
    void largePayloadsPassTheDefaultFilter() throws Exception {
        ObjectInputFilter defaults = JavaSerializationMarshaller.defaultFilter();

        byte[] bytes = new byte[200_000];
        bytes[199_999] = 7;
        assertEquals(7, ((byte[]) roundTrip(bytes, defaults))[199_999]);

        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 150_000; i++) {
            numbers.add(i);
        }
        assertEquals(numbers, roundTrip(numbers, defaults));

        Node head = new Node("0");
        Node tail = head;
        for (int i = 1; i <= 64; i++) {
            tail.peer = new Node(Integer.toString(i));
            tail = tail.peer;
        }
        Node decoded = (Node) roundTrip(head, defaults);
        int length = 1;
        for (Node n = decoded; n.peer != null; n = n.peer) {
            length++;
        }
        assertEquals(65, length);
    }

    @Test
    @DisplayName("an application's class loader can be collected once the codec has been told it is gone")
    void aReleasedLoaderCanBeCollected() throws Exception {
        AtomicReference<WeakReference<ClassLoader>> reference = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        // A thread of its own, so that nothing the codec cached per thread
        // outlives the work and keeps the loader reachable from here.
        Thread worker = new Thread(() -> {
            try {
                URLClassLoader loader = new URLClassLoader(new URL[0], getClass().getClassLoader());
                Thread.currentThread().setContextClassLoader(loader);
                try {
                    Marshallers.preferred(loader);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
                        writer.writeObject(new Node("a"));
                        writer.flush();
                    }
                    try (Marshaller.ObjectReader reader = marshaller.newReader(
                            new ByteArrayInputStream(out.toByteArray()), loader, ALLOW_ALL)) {
                        assertNotNull(reader.readObject());
                    }
                    Marshallers.release(loader);
                } finally {
                    Thread.currentThread().setContextClassLoader(null);
                }
                reference.set(new WeakReference<>(loader));
            } catch (Throwable t) {
                failure.set(t);
            }
        });
        worker.start();
        worker.join();
        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }

        for (int i = 0; i < 100 && reference.get().get() != null; i++) {
            System.gc();
            Thread.sleep(50);
        }
        assertNull(reference.get().get(), "the loader is still reachable after release");
    }

    private WeakReference<ClassLoader> useAndReleaseOnThisThread() throws Exception {
        URLClassLoader loader = new URLClassLoader(new URL[0], getClass().getClassLoader());
        Thread.currentThread().setContextClassLoader(loader);
        try {
            Marshallers.preferred(loader);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
                writer.writeObject(new Node("a"));
                writer.flush();
            }
            try (Marshaller.ObjectReader reader = marshaller.newReader(
                    new ByteArrayInputStream(out.toByteArray()), loader, ALLOW_ALL)) {
                assertNotNull(reader.readObject());
            }
            Marshallers.release(loader);
        } finally {
            Thread.currentThread().setContextClassLoader(null);
        }
        return new WeakReference<>(loader);
    }

    @Test
    @DisplayName("the loader is collectable when the work and the release ran on a long-lived pooled thread")
    void aLoaderUsedOnAPooledThreadIsCollectable() throws Exception {
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        try {
            // The thread stays alive for the whole check, as a container's worker
            // thread does: per-thread state it keeps would pin the loader.
            WeakReference<ClassLoader> reference = executor.submit(this::useAndReleaseOnThisThread).get();
            for (int i = 0; i < 100 && reference.get() != null; i++) {
                System.gc();
                Thread.sleep(50);
            }
            assertNull(reference.get(), "the loader is still reachable after release");
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("a graph deeper than the shared depth limit is refused on this codec too")
    void aTooDeepGraphIsRefused() {
        Node head = new Node("0");
        Node tail = head;
        for (int i = 1; i <= 150; i++) {
            tail.peer = new Node(Integer.toString(i));
            tail = tail.peer;
        }
        Node deep = head;

        assertThrows(Exception.class, () -> roundTrip(deep, JavaSerializationMarshaller.defaultFilter()));
    }

    @Test
    @DisplayName("the pool is bounded (guard) and its cost is reported")
    void thePoolIsBounded() throws Exception {
        assertTrue(ForyMarshaller.POOL_SIZE >= 1 && ForyMarshaller.POOL_SIZE <= 64,
                "pool size " + ForyMarshaller.POOL_SIZE);
        try (URLClassLoader loader = new URLClassLoader(new URL[0], getClass().getClassLoader())) {
            com.sun.management.ThreadMXBean threads = (com.sun.management.ThreadMXBean)
                    java.lang.management.ManagementFactory.getThreadMXBean();
            long id = Thread.currentThread().getId();
            long before = threads.getThreadAllocatedBytes(id);
            try (Marshaller.ObjectReader reader = marshaller.newReader(
                    new ByteArrayInputStream(new byte[0]), loader, JavaSerializationMarshaller.defaultFilter())) {
                assertNotNull(reader);
            }
            long allocated = threads.getThreadAllocatedBytes(id) - before;
            System.out.println("FORY-POOL poolSize=" + ForyMarshaller.POOL_SIZE
                    + " allocatedBytesForFirstReaderPool=" + allocated
                    + " processors=" + Runtime.getRuntime().availableProcessors());
        }
    }

    private static byte[] javaFrame(Object value) throws Exception {
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        try (Marshaller.ObjectWriter writer = new JavaSerializationMarshaller().newWriter(payload)) {
            writer.writeObject(value);
            writer.flush();
        }
        ByteArrayOutputStream frame = new ByteArrayOutputStream();
        java.io.DataOutputStream data = new java.io.DataOutputStream(frame);
        data.writeByte(1);                      // a frame handed to Java serialization
        data.writeInt(payload.size());
        data.write(payload.toByteArray());
        data.flush();
        return frame.toByteArray();
    }

    @Test
    @DisplayName("the array budget is one per reader: a second frame cannot spend it again")
    void theBudgetIsSharedByTheFramesOfOneReader() throws Exception {
        ByteArrayOutputStream frames = new ByteArrayOutputStream();
        frames.write(javaFrame(new Object[2_500_000]));      // 20 MB declared
        frames.write(javaFrame(new Object[2_500_000]));      // another 20 MB: over 32 MB in all

        try (Marshaller.ObjectReader reader = marshaller.newReader(
                new ByteArrayInputStream(frames.toByteArray()), getClass().getClassLoader(),
                JavaSerializationMarshaller.defaultFilter())) {
            assertEquals(2_500_000, ((Object[]) reader.readObject()).length);
            assertThrows(java.io.InvalidClassException.class, reader::readObject);
        }
    }

    @Test
    @DisplayName("limits taken from properties are brought into what Fory accepts")
    void limitsAreClamped() {
        assertEquals(2, ForyMarshaller.clampDepth(1));
        assertEquals(Integer.MAX_VALUE, ForyMarshaller.clampDepth(Long.MAX_VALUE));
        assertEquals(100, ForyMarshaller.clampDepth(100));
        assertEquals(Long.MAX_VALUE, ForyMarshaller.graphMemory(Long.MAX_VALUE));
        assertEquals(128L * 1024 * 1024, ForyMarshaller.graphMemory(32L * 1024 * 1024));
        assertEquals(8, ForyMarshaller.poolSize("abc", 8));
        assertEquals(8, ForyMarshaller.poolSize("-3", 8));
        assertEquals(8, ForyMarshaller.poolSize(null, 8));
        assertEquals(12, ForyMarshaller.poolSize("12", 8));
    }
}
