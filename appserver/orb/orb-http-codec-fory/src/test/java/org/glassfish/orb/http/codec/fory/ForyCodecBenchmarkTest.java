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

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.orb.http.protocol.ChunkedOutput;
import org.glassfish.orb.http.protocol.Marshaller;
import org.junit.jupiter.api.Test;

/**
 * Small in-process comparison of the old byte-array path and the framed
 * streaming path. It is deliberately a measurement test rather than a pass
 * or fail performance gate: the CI runner publishes the numbers, while the
 * round-trip tests remain the correctness gate.
 */
class ForyCodecBenchmarkTest {
    private static final int WARMUP = 1_000;
    private static final int ITERATIONS = 5_000;
    private static final int ROUNDS = 5;

    @Test
    void compareByteArrayAndStreamingForSmallMessages() throws Exception {
        System.setProperty("org.glassfish.orb.http.codec.fory.streaming", "true");
        try {
            Marshaller marshaller = new ForyMarshaller();
            Map<String, Object> graph = new HashMap<>();
            graph.put("message", "small");
            graph.put("number", 42);

            measure(marshaller, "small", "hello");
            measure(marshaller, "graph", graph);
            measure(marshaller, "large", "x".repeat(64 * 1024));
        } finally {
            System.clearProperty("org.glassfish.orb.http.codec.fory.streaming");
        }
    }

    private static void measure(Marshaller marshaller, String name, Object value) throws Exception {
        for (int i = 0; i < WARMUP; i++) {
            encodeLegacy(marshaller, value);
            encodeStreaming(marshaller, value);
        }

        long legacyNanos = median(marshaller, value, false);
        long streamingNanos = median(marshaller, value, true);
        long legacyBytes = encodeLegacy(marshaller, value);
        long streamingBytes = encodeStreaming(marshaller, value);
        double legacyOps = (double) ITERATIONS * 1_000_000_000L / legacyNanos;
        double streamingOps = (double) ITERATIONS * 1_000_000_000L / streamingNanos;
        System.out.printf("RESULT payload=%s mode=legacy ops/s=%.0f bytes=%d%n",
                name, legacyOps, legacyBytes);
        System.out.printf("RESULT payload=%s mode=streaming ops/s=%.0f bytes=%d speedup=%.3fx%n",
                name, streamingOps, streamingBytes, streamingOps / legacyOps);
    }

    private static long median(Marshaller marshaller, Object value, boolean streaming) throws Exception {
        long[] samples = new long[ROUNDS];
        for (int round = 0; round < ROUNDS; round++) {
            long start = System.nanoTime();
            long bytes = 0;
            for (int i = 0; i < ITERATIONS; i++) {
                bytes += streaming
                        ? encodeStreaming(marshaller, value)
                        : encodeLegacy(marshaller, value);
            }
            if (bytes == 0) throw new AssertionError("empty encoded payload");
            samples[round] = System.nanoTime() - start;
        }
        java.util.Arrays.sort(samples);
        return samples[samples.length / 2];
    }

    private static long encodeLegacy(Marshaller marshaller, Object value) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
            writer.writeObject(value);
            writer.flush();
        }
        return out.size();
    }

    private static long encodeStreaming(Marshaller marshaller, Object value) throws Exception {
        ChunkedOutput out = new ChunkedOutput();
        try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
            writer.writeObject(value);
            writer.flush();
        }
        return out.size();
    }
}
