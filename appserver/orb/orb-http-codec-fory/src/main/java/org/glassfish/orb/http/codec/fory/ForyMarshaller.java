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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.LongAdder;

import org.apache.fory.Fory;
import org.apache.fory.ThreadSafeFory;
import org.apache.fory.config.CompatibleMode;
import org.apache.fory.config.Language;
import org.apache.fory.io.ForyInputStream;
import org.glassfish.orb.http.protocol.ChunkedOutput;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Marshaller;

/**
 * An object codec built on Apache Fory.
 * <p>
 * This class is never referenced by the transport, by an application, or by
 * any configuration file. It is reached only through
 * {@code META-INF/services}, which is what makes the claim behind this module
 * true: an application changes codec by having this jar present, and by
 * nothing else.
 * <p>
 * The encoding changes; the semantics deliberately do not. Java serialization
 * tracks object identity, closes cycles and skips {@code transient} fields,
 * and remote calls already rely on all three. Fory is configured to match
 * rather than to be fast in ways that would silently alter what an
 * application observes - see the settings in {@link #newFory}.
 */
public final class ForyMarshaller implements Marshaller {

    private static final int DEFAULT_STREAMING_THRESHOLD = 4 * 1024;

    /** The largest buffer a streamed decode starts with; bigger frames grow it. */
    private static final int MAX_STREAM_BUFFER = 64 * 1024;

    /** Frames written through {@link ChunkedOutput#beginFrame}; read by tests. */
    private static final LongAdder STREAMED_FRAMES = new LongAdder();

    /**
     * @return how many frames this codec has written through the streaming
     *         path since the class was loaded
     */
    static long streamedFrameCount() {
        return STREAMED_FRAMES.sum();
    }

    /** Enables direct framed streaming after it has been validated for a deployment. */
    private static final String STREAMING_PROPERTY =
            "org.glassfish.orb.http.codec.fory.streaming";
    /** Overrides the automatic large-payload threshold; bytes are estimated. */
    public static final String STREAMING_THRESHOLD_PROPERTY =
            "org.glassfish.orb.http.codec.fory.streaming.threshold";

    /** The codec token that travels in the content type. */
    public static final String CODEC = "fory";

    /**
     * Whether Fory may compile a serializer per type.
     * <p>
     * Off by default, and the default is about this server rather than about
     * Fory. Generated serializers are defined through a class loader Fory
     * chooses, which works on a class path and did not survive here: the type
     * being encoded lives in one bundle and Fory in another, and generation
     * fails with "Create sequential serializer failed", naming the type rather
     * than the visibility behind it. Bridging the two loaders was tried and
     * did not fix it, so the cause is not fully understood and the honest
     * default is the path that works.
     * <p>
     * The reflective path is what runs instead. It is slower than generated
     * code and still well ahead of Java serialization, which is the comparison
     * that matters for a codec adopted to be faster than it.
     * <p>
     * Set {@code org.glassfish.orb.http.codec.fory.codegen=true} to turn it on
     * where it does work - a plain client JVM, for one, where there is an
     * ordinary class path and nothing to bridge.
     */
    private static final boolean CODEGEN =
            Boolean.getBoolean("org.glassfish.orb.http.codec.fory.codegen");

    /**
     * @return whether generated serializers are in use
     */
    static boolean codeGenerationEnabled() {
        return CODEGEN;
    }

    /** A frame this codec encoded. */
    private static final byte KIND_FORY = 0;

    /**
     * A frame handed to Java serialization instead.
     * <p>
     * Throwables go this way. Fory reconstructs a plain exception faithfully
     * but loses the stack trace of an application subclass, and an exception
     * that arrives claiming to come from nowhere is worst exactly when someone
     * is trying to find out where it came from. Java serialization has a
     * special path for throwables and gets this right, and the exceptional path
     * is not where encoding speed matters.
     */
    private static final byte KIND_JAVA = 1;

    /** The codec throwables are handed to; see {@link #KIND_JAVA}. */
    private static final JavaSerializationMarshaller BUILT_IN = new JavaSerializationMarshaller();

    /**
     * Writing and reading get separate instances, and this is a security
     * property rather than tidiness.
     * <p>
     * Fory caches what it has resolved. With one shared instance, a class
     * written on the way out is already resolved by the time an attacker
     * names it on the way in, so the inbound check never runs - the guard
     * is present and bypassed. Keeping the two directions apart means the
     * reader has never been warmed by anything this process chose to send.
     * <p>
     * Keyed weakly, but the value is a serializer that references its key's
     * loader, so an entry keeps its loader reachable: the loader stays pinned
     * for the life of this class.
     */
    private static final Map<ClassLoader, ThreadSafeFory> WRITERS =
            Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Readers are additionally keyed by the filter in force. A filter is
     * normally one long lived object per configuration, so this stays small -
     * and it closes the second half of the same hole: a class admitted under
     * a permissive filter must not stay admitted for a stricter one.
     */
    private static final Map<ClassLoader, Map<ObjectInputFilter, ThreadSafeFory>> READERS =
            Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public String codec() {
        return CODEC;
    }

    @Override
    public int priority() {
        // Above the built-in floor, so simply being on the class path is
        // enough to be chosen.
        return 100;
    }

    @Override
    public ObjectWriter newWriter(OutputStream out) throws IOException {
        ThreadSafeFory fory = writerFor(Thread.currentThread().getContextClassLoader());
        DataOutputStream data = new DataOutputStream(out);
        ChunkedOutput framed = out instanceof ChunkedOutput ? (ChunkedOutput) out : null;
        // Read once per writer, that is once per request, rather than once per
        // object. Changing a property therefore applies to the next writer.
        boolean forceStreaming = framed != null && Boolean.getBoolean(STREAMING_PROPERTY);
        int threshold = streamingThreshold();
        return new ObjectWriter() {

            @Override
            public void writeObject(Object o) throws IOException {
                boolean java = o instanceof Throwable;
                if (!java && framed != null && (forceStreaming || isLargePayload(o, threshold))) {
                    // ChunkedOutput reserves the five-byte header in place and
                    // close() patches the length once the payload is in.
                    // Fory 1.7.1 still builds the whole object in its own
                    // buffer before it writes to the stream, so nothing is
                    // streamed while the object is being encoded. What this
                    // saves is the byte[] that serialize(Object) would return
                    // and the copy of it into the transport.
                    ChunkedOutput.Frame frame = framed.beginFrame(KIND_FORY);
                    try {
                        fory.serialize(out, o);
                        frame.close();
                    } catch (Throwable failure) {
                        // Take the header back out. Left behind it would be a
                        // well-formed frame of length zero in front of
                        // whatever is written next, and the reader would
                        // treat it as a frame like any other.
                        frame.abort();
                        throw failure;
                    }
                    STREAMED_FRAMES.increment();
                    return;
                }
                byte[] encoded = java ? javaEncode(o) : fory.serialize(o);
                // Each object is framed by this codec rather than by the
                // library underneath: the transport writes several objects in
                // sequence, and the boundary between them has to be a property
                // of this format. The kind byte says which encoding follows.
                data.writeByte(java ? KIND_JAVA : KIND_FORY);
                data.writeInt(encoded.length);
                data.write(encoded);
            }

            @Override
            public void flush() throws IOException {
                data.flush();
            }

            @Override
            public void close() throws IOException {
                data.flush();
            }
        };
    }

    /**
     * Whether the value is large enough to be worth writing into the chunked
     * output directly. Small messages are better served by Fory's byte-array
     * serializer, which measured faster for them (see
     * {@code ForyCodecBenchmarkTest}). The sizes are
     * conservative estimates: unknown object graphs take the byte-array path
     * (which copies the encoded bytes once more) unless the deployment
     * enables streaming explicitly.
     *
     * @param threshold the estimated size in bytes from which a value counts
     *                  as large; negative never does
     */
    private static boolean isLargePayload(Object value, int threshold) {
        if (threshold < 0) {
            return false;
        }
        if (value instanceof CharSequence) {
            return ((CharSequence) value).length() * 2L >= threshold;
        }
        if (value instanceof byte[]) {
            return ((byte[]) value).length >= threshold;
        }
        if (value != null && value.getClass().isArray()) {
            int length = Array.getLength(value);
            return (long) length * Math.max(1, elementSize(value.getClass().getComponentType()))
                    >= threshold;
        }
        if (value instanceof Map) {
            return (long) ((Map<?, ?>) value).size() * 16 >= threshold;
        }
        if (value instanceof Collection) {
            return (long) ((Collection<?>) value).size() * 16 >= threshold;
        }
        return false;
    }

    private static int streamingThreshold() {
        String configured = System.getProperty(STREAMING_THRESHOLD_PROPERTY);
        if (configured == null || configured.isBlank()) {
            return DEFAULT_STREAMING_THRESHOLD;
        }
        try {
            return Integer.parseInt(configured.trim());
        } catch (NumberFormatException ignored) {
            return DEFAULT_STREAMING_THRESHOLD;
        }
    }

    private static int elementSize(Class<?> type) {
        if (type == byte.class || type == boolean.class) return 1;
        if (type == short.class || type == char.class) return 2;
        if (type == int.class || type == float.class) return 4;
        if (type == long.class || type == double.class) return 8;
        return 8;
    }

    @Override
    public ObjectReader newReader(InputStream in, ClassLoader loader, ObjectInputFilter filter)
            throws IOException {
        if (filter == null) {
            throw new IllegalArgumentException("a deserialization filter is required");
        }
        ThreadSafeFory fory = readerFor(loader, filter);
        DataInputStream data = new DataInputStream(in);
        // Frames at least this long are decoded from the stream; shorter ones
        // are read into an array first, which is cheaper for them because the
        // stream decoder allocates a buffer of its own for every frame.
        int streamFrom = streamingThreshold();
        return new ObjectReader() {

            @Override
            public Object readObject() throws IOException, ClassNotFoundException {
                byte kind = data.readByte();
                if (kind != KIND_FORY && kind != KIND_JAVA) {
                    throw new IOException("unknown frame kind " + kind
                            + "; the stream is not this codec's, or is out of step");
                }
                int length = data.readInt();
                if (length < 0) {
                    throw new IOException("negative frame length " + length);
                }
                if (kind == KIND_FORY && streamFrom >= 0 && length >= streamFrom) {
                    return readStreamed(fory, data, length);
                }
                byte[] encoded = data.readNBytes(length);
                if (encoded.length != length) {
                    throw new IOException("truncated frame: expected " + length
                            + " bytes but read " + encoded.length);
                }

                return kind == KIND_JAVA
                        ? javaDecode(encoded, loader, filter)
                        : fory.deserialize(encoded);
            }

            @Override
            public void close() throws IOException {
                data.close();
            }
        };
    }

    /**
     * Decodes one frame straight from the stream.
     * <p>
     * Fory reports a stream that ends early, or fails, as an unchecked
     * exception from deep inside its buffer. The caller reads from a socket
     * and expects an {@link IOException} for that, so the cause is kept by the
     * limiting stream and rethrown here in its own type.
     */
    private static Object readStreamed(ThreadSafeFory fory, InputStream in, int length)
            throws IOException {
        LimitedInputStream limited = new LimitedInputStream(in, length);
        try {
            int buffer = Math.max(16, Math.min(length, MAX_STREAM_BUFFER));
            Object value = fory.deserialize(new ForyInputStream(limited, buffer));
            limited.drain();
            return value;
        } catch (RuntimeException e) {
            if (limited.failure != null) {
                limited.failure.addSuppressed(e);
                throw limited.failure;
            }
            throw e;
        }
    }

    /**
     * Prevent a streaming Fory decoder from consuming the next frame, and
     * remember why the frame could not be read to its end.
     */
    private static final class LimitedInputStream extends InputStream {
        private final InputStream delegate;
        private final int length;
        private int remaining;
        /** The reason the declared length could not be read, if that happened. */
        private IOException failure;

        private LimitedInputStream(InputStream delegate, int length) {
            this.delegate = delegate;
            this.length = length;
            this.remaining = length;
        }

        @Override
        public int read() throws IOException {
            if (remaining == 0) return -1;
            int value;
            try {
                value = delegate.read();
            } catch (IOException e) {
                failure = e;
                throw e;
            }
            if (value >= 0) {
                remaining--;
            } else {
                truncated();
            }
            return value;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (remaining == 0) return -1;
            int count;
            try {
                count = delegate.read(b, off, Math.min(len, remaining));
            } catch (IOException e) {
                failure = e;
                throw e;
            }
            if (count > 0) {
                remaining -= count;
            } else if (count < 0) {
                truncated();
            }
            return count;
        }

        @Override
        public int available() throws IOException {
            // Without this the default of 0 makes the decoder grow its buffer
            // by doubling, copying it each time, for a frame whose size is known.
            return Math.min(remaining, delegate.available());
        }

        private void truncated() {
            failure = new EOFException("truncated Fory frame: expected " + length
                    + " bytes but the stream ended after " + (length - remaining));
        }

        private void drain() throws IOException {
            byte[] discard = new byte[Math.min(8192, Math.max(1, remaining))];
            while (remaining > 0 && read(discard, 0, discard.length) >= 0) {
                // consume trailing bytes in the declared frame
            }
            if (remaining != 0) {
                throw failure != null ? failure : new EOFException("truncated Fory frame");
            }
        }
    }

    private static byte[] javaEncode(Object value) throws IOException {
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        try (Marshaller.ObjectWriter writer = BUILT_IN.newWriter(bytes)) {
            // Materialised first: the trace lives in a native structure until
            // getStackTrace is called, and writeObject only copies the field.
            materialiseStackTraces(value);
            writer.writeObject(value);
            writer.flush();
        }
        return bytes.toByteArray();
    }

    private static Object javaDecode(byte[] encoded, ClassLoader loader, ObjectInputFilter filter)
            throws IOException, ClassNotFoundException {
        try (Marshaller.ObjectReader reader =
                BUILT_IN.newReader(new java.io.ByteArrayInputStream(encoded), loader, filter)) {
            return reader.readObject();
        }
    }

    /**
     * Makes a throwable's stack trace real before it is encoded.
     *
     * <p>{@code Throwable.stackTrace} stays a shared empty sentinel until
     * {@code getStackTrace} is called; the trace lives in a native structure
     * until then. Java serialization materialises it on the way out, inside
     * {@code Throwable.writeObject}. A codec that reads the field directly sees
     * the sentinel and sends an exception with no stack trace - which arrives
     * looking like it was thrown from nowhere, exactly when someone is trying
     * to find out where it came from.
     *
     * @param value the object about to be written; anything that is not a
     *              throwable is left alone
     */
    private static void materialiseStackTraces(Object value) {
        for (Throwable t = value instanceof Throwable thrown ? thrown : null;
                t != null; t = t.getCause() == t ? null : t.getCause()) {
            t.getStackTrace();
            for (Throwable suppressed : t.getSuppressed()) {
                materialiseStackTraces(suppressed);
            }
        }
    }

    private static ThreadSafeFory writerFor(ClassLoader loader) {
        return WRITERS.computeIfAbsent(resolve(loader), l -> newFory(l, null));
    }

    /**
     * @param loader the class loader the readers were requested for
     * @return how many readers are cached for {@code loader}, one per filter;
     *         0 if none
     */
    static int readerCount(ClassLoader loader) {
        Map<ObjectInputFilter, ThreadSafeFory> byFilter = READERS.get(resolve(loader));
        return byFilter == null ? 0 : byFilter.size();
    }

    private static ThreadSafeFory readerFor(ClassLoader loader, ObjectInputFilter filter) {
        Map<ObjectInputFilter, ThreadSafeFory> byFilter = READERS.computeIfAbsent(resolve(loader),
                l -> Collections.synchronizedMap(new WeakHashMap<>()));
        return byFilter.computeIfAbsent(filter, f -> newFory(resolve(loader), f));
    }

    private static ClassLoader resolve(ClassLoader loader) {
        return loader == null ? ForyMarshaller.class.getClassLoader() : loader;
    }

    /**
     * Builds a Fory configured to preserve what remote calls already observe.
     *
     * <p>Reference tracking is on because an object graph crossing a remote
     * call may share a node or contain a cycle, and Java serialization closes
     * both; without it a shared node would arrive duplicated and a cycle would
     * not arrive at all. Compatible mode lets a field appear or disappear
     * between the two ends, which is the situation a running cluster is
     * actually in during a rolling upgrade.
     *
     * <p>Class registration is not required - an application cannot be asked
     * to enumerate its own types for a codec it never opted into - so the
     * transport's deserialization filter takes that duty instead.
     *
     * @param loader the loader that resolves incoming classes
     * @param filter the policy for inbound classes, or {@code null} for a
     *               writer, where classes come from live objects rather than
     *               from the wire and there is nothing to guard against
     * @return a thread safe Fory for that loader
     */
    private static ThreadSafeFory newFory(ClassLoader loader, ObjectInputFilter filter) {
        var builder = Fory.builder()
                .withLanguage(Language.JAVA)
                .withClassLoader(loader)
                .withRefTracking(true)
                .withCompatibleMode(CompatibleMode.COMPATIBLE)
                .requireClassRegistration(false)
                .withCodegen(CODEGEN)
                .suppressClassRegistrationWarnings(true);
        if (filter != null) {
            builder = builder.withTypeChecker(new FilterBackedTypeChecker(loader, filter));
        }
        return builder.buildThreadSafeFory();
    }
}
