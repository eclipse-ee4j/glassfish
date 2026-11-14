/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
 */

package org.glassfish.orb.http.codec.fory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputFilter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.glassfish.orb.http.protocol.ChunkedOutput;
import org.glassfish.orb.http.protocol.Marshaller;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * In-process comparison of the byte-array path and the framed streaming path,
 * for encoding and for decoding.
 * <p>
 * It is a measurement, not a pass or fail gate, and it takes a few seconds, so
 * it does not run with the unit tests. Run it with
 * {@code -Dorb.benchmark=true -Dtest=ForyCodecBenchmarkTest}; the results are
 * printed as {@code RESULT} lines.
 * <p>
 * Both encoding modes write into a fresh {@link ChunkedOutput}, so the sink,
 * and the 16 KB chunk it allocates, cost the same in both; only the way the
 * codec fills it differs. The two modes alternate round by round so a change
 * in the machine's speed during the run affects both alike. A single JVM and
 * a handful of rounds give an indication of the direction, not a precise
 * ratio.
 */
@EnabledIfSystemProperty(named = "orb.benchmark", matches = "true")
class ForyCodecBenchmarkTest {
    private static final String STREAMING = "org.glassfish.orb.http.codec.fory.streaming";
    private static final String THRESHOLD = ForyMarshaller.STREAMING_THRESHOLD_PROPERTY;
    private static final ObjectInputFilter ALLOW_ALL = info -> ObjectInputFilter.Status.ALLOWED;

    private static final int WARMUP = 2_000;
    private static final int ITERATIONS = 5_000;
    private static final int ROUNDS = 9;

    /** Applies one mode's properties; the codec reads them when a writer or reader is created. */
    private enum Mode {
        /** Encode through the byte array and copy it in. Decode from an array. */
        ARRAY("false", "-1", "-1"),
        /** Encode through the framed stream. Decode from the stream. */
        STREAM("true", null, "0");

        private final String streaming;
        private final String encodeThreshold;
        private final String decodeThreshold;

        Mode(String streaming, String encodeThreshold, String decodeThreshold) {
            this.streaming = streaming;
            this.encodeThreshold = encodeThreshold;
            this.decodeThreshold = decodeThreshold;
        }

        void forEncode() {
            System.setProperty(STREAMING, streaming);
            restore(THRESHOLD, encodeThreshold);
        }

        void forDecode() {
            System.clearProperty(STREAMING);
            restore(THRESHOLD, decodeThreshold);
        }
    }

    @Test
    void compareArrayAndStreamingPaths() throws Exception {
        String previousStreaming = System.getProperty(STREAMING);
        String previousThreshold = System.getProperty(THRESHOLD);
        try {
            Marshaller marshaller = new ForyMarshaller();
            Map<String, Object> graph = new HashMap<>();
            graph.put("message", "small");
            graph.put("number", 42);

            Map<String, Object> payloads = new LinkedHashMap<>();
            payloads.put("small", "hello");
            payloads.put("graph", graph);
            payloads.put("large", "x".repeat(64 * 1024));
            for (Map.Entry<String, Object> payload : payloads.entrySet()) {
                measure(marshaller, "encode", payload.getKey(), payload.getValue());
                measure(marshaller, "decode", payload.getKey(), payload.getValue());
            }
        } finally {
            restore(STREAMING, previousStreaming);
            restore(THRESHOLD, previousThreshold);
        }
    }

    private static void restore(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }

    private static void measure(Marshaller marshaller, String operation, String name, Object value)
            throws Exception {
        boolean encode = operation.equals("encode");
        byte[] encoded = encodeArray(marshaller, value);
        for (Mode mode : Mode.values()) {
            for (int i = 0; i < WARMUP; i++) {
                run(marshaller, mode, encode, value, encoded);
            }
        }

        long[][] samples = new long[Mode.values().length][ROUNDS];
        long bytes = 0;
        for (int round = 0; round < ROUNDS; round++) {
            for (Mode mode : Mode.values()) {
                long start = System.nanoTime();
                for (int i = 0; i < ITERATIONS; i++) {
                    bytes += run(marshaller, mode, encode, value, encoded);
                }
                samples[mode.ordinal()][round] = System.nanoTime() - start;
            }
        }
        if (bytes == 0) {
            throw new AssertionError("nothing was processed");
        }
        double[] opsPerSecond = new double[Mode.values().length];
        for (Mode mode : Mode.values()) {
            long[] sorted = samples[mode.ordinal()].clone();
            Arrays.sort(sorted);
            opsPerSecond[mode.ordinal()] = (double) ITERATIONS * 1_000_000_000L / sorted[ROUNDS / 2];
        }
        double array = opsPerSecond[Mode.ARRAY.ordinal()];
        double stream = opsPerSecond[Mode.STREAM.ordinal()];
        System.out.printf("RESULT op=%s payload=%s mode=array ops/s=%.0f bytes=%d%n",
                operation, name, array, encoded.length);
        System.out.printf("RESULT op=%s payload=%s mode=stream ops/s=%.0f bytes=%d stream/array=%.3fx%n",
                operation, name, stream, encoded.length, stream / array);
    }

    private static long run(Marshaller marshaller, Mode mode, boolean encode, Object value, byte[] encoded)
            throws Exception {
        if (encode) {
            mode.forEncode();
            ChunkedOutput out = new ChunkedOutput();
            try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
                writer.writeObject(value);
                writer.flush();
            }
            return out.size();
        }
        mode.forDecode();
        try (Marshaller.ObjectReader reader = marshaller.newReader(
                new ByteArrayInputStream(encoded), ForyCodecBenchmarkTest.class.getClassLoader(), ALLOW_ALL)) {
            return reader.readObject() == null ? 0 : encoded.length;
        }
    }

    private static byte[] encodeArray(Marshaller marshaller, Object value) throws Exception {
        Mode.ARRAY.forEncode();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
            writer.writeObject(value);
            writer.flush();
        }
        return out.toByteArray();
    }
}
