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
import java.io.ObjectInputFilter;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.glassfish.orb.http.protocol.ChunkedInput;
import org.glassfish.orb.http.protocol.ChunkedOutput;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Marshaller;
import org.glassfish.orb.http.protocol.Marshallers;
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

    private final Marshaller marshaller = new ForyMarshaller();

    private static void restore(String previous) {
        if (previous == null) {
            System.clearProperty(STREAMING);
        } else {
            System.setProperty(STREAMING, previous);
        }
    }

    @Test
    void streamsSmallObjectsIntoChunkedOutput() throws Exception {
        String previous = System.getProperty(STREAMING);
        System.setProperty(STREAMING, "true");
        ChunkedOutput out = new ChunkedOutput(64);
        try {
            try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
                writer.writeObject("small");
                writer.flush();
            }

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
            assertThrows(Exception.class, reader::readObject);
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
}
