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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChunkedOutputTest {

    @Test
    @DisplayName("produces exactly the bytes a ByteArrayOutputStream would")
    void matchesByteArrayOutputStream() throws IOException {
        Random random = new Random(11L);
        for (int trial = 0; trial < 50; trial++) {
            ChunkedOutput chunked = new ChunkedOutput(64);
            ByteArrayOutputStream reference = new ByteArrayOutputStream();

            for (int op = 0; op < 200; op++) {
                if (random.nextBoolean()) {
                    int b = random.nextInt(256);
                    chunked.write(b);
                    reference.write(b);
                } else {
                    byte[] block = new byte[random.nextInt(200)];
                    random.nextBytes(block);
                    int off = block.length == 0 ? 0 : random.nextInt(block.length);
                    int len = block.length == 0 ? 0 : random.nextInt(block.length - off);
                    chunked.write(block, off, len);
                    reference.write(block, off, len);
                }
            }

            byte[] expected = reference.toByteArray();
            assertEquals(expected.length, chunked.size());
            assertArrayEquals(expected, chunked.toByteArray());
            assertArrayEquals(expected, flatten(chunked.toByteBuffers()));
        }
    }

    @Test
    @DisplayName("the buffers alias the chunks and remain Grizzly-wrappable")
    void buffersAreViewsNotCopies() throws IOException {
        ChunkedOutput out = new ChunkedOutput(8);
        out.write(new byte[20]);

        ByteBuffer[] buffers = out.toByteBuffers();
        // 20 bytes at 8 per chunk: two full chunks plus a partial one.
        assertEquals(3, buffers.length);
        assertEquals(8, buffers[0].remaining());
        assertEquals(8, buffers[1].remaining());
        assertEquals(4, buffers[2].remaining());
        assertTrue(buffers[0].hasArray(), "the transport must be able to wrap the chunk without copying");

        long total = 0;
        for (ByteBuffer b : buffers) {
            total += b.remaining();
        }
        assertEquals(out.size(), total);
    }

    @Test
    void handlesAnEmptyStream() {
        ChunkedOutput out = new ChunkedOutput();
        assertEquals(0, out.size());
        assertEquals(0, out.toByteBuffers().length);
        assertEquals(0, out.toByteArray().length);
    }

    @Test
    void chunkedInputReadsBackWhatChunkedOutputWrote() throws IOException {
        byte[] payload = new byte[5000];
        new Random(3L).nextBytes(payload);

        ChunkedOutput out = new ChunkedOutput(97);
        out.write(payload);

        ChunkedInput in = new ChunkedInput(out.toByteBuffers());
        assertEquals(payload.length, in.available());
        assertArrayEquals(payload, in.readAllBytes());
        assertEquals(-1, in.read());
    }

    @Test
    @DisplayName("ChunkedInput reads across buffer boundaries and honours skip")
    void chunkedInputCrossesBoundaries() throws IOException {
        List<ByteBuffer> parts = new ArrayList<>();
        parts.add(ByteBuffer.wrap(new byte[] { 1, 2, 3 }));
        parts.add(ByteBuffer.wrap(new byte[0]));
        parts.add(ByteBuffer.wrap(new byte[] { 4, 5 }));

        ChunkedInput in = new ChunkedInput(parts);
        byte[] dst = new byte[5];
        assertEquals(5, in.read(dst, 0, 5));
        assertArrayEquals(new byte[] { 1, 2, 3, 4, 5 }, dst);

        ChunkedInput skipping = new ChunkedInput(
                ByteBuffer.wrap(new byte[] { 1, 2, 3 }), ByteBuffer.wrap(new byte[] { 4, 5 }));
        assertEquals(4, skipping.skip(4));
        assertEquals(5, skipping.read());
        assertEquals(-1, skipping.read());
    }

    private static byte[] flatten(ByteBuffer[] buffers) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (ByteBuffer b : buffers) {
            ByteBuffer copy = b.duplicate();
            while (copy.hasRemaining()) {
                out.write(copy.get());
            }
        }
        return out.toByteArray();
    }

    @Test
    @DisplayName("frame header at the end of a chunk leaves no padding")
    void frameHeaderAtChunkEndLeavesNoPadding() throws IOException {
        ChunkedOutput out = new ChunkedOutput(8);
        out.write(new byte[] {1, 2, 3, 4, 5});
        ChunkedOutput.Frame f = out.beginFrame((byte) 7);
        out.write(new byte[] {9, 9, 9});
        f.close();

        assertEquals(13L, out.size());
        long buffered = 0;
        for (ByteBuffer b : out.toByteBuffers()) {
            buffered += b.remaining();
        }
        assertEquals(13L, buffered);
        assertArrayEquals(new byte[] {1, 2, 3, 4, 5, 7, 0, 0, 0, 3, 9, 9, 9}, out.toByteArray());
    }

    @Test
    @DisplayName("a frame header fits any chunk size from 1 to 5 and leaves no padding")
    void frameWorksWithChunksSmallerThanTheHeader() throws IOException {
        for (int chunk = 1; chunk <= 5; chunk++) {
            ChunkedOutput out = new ChunkedOutput(chunk);
            ChunkedOutput.Frame f = out.beginFrame((byte) 6);
            out.write(new byte[] {1, 2, 3});
            f.close();

            assertArrayEquals(new byte[] {6, 0, 0, 0, 3, 1, 2, 3}, out.toByteArray(), "chunk " + chunk);
            assertEquals(8, out.size(), "chunk " + chunk);
            assertEquals(out.size(), remaining(out.toByteBuffers()), "chunk " + chunk);
        }
    }

    @Test
    @DisplayName("a frame spanning several chunks carries the right header")
    void frameSpanningManyChunks() throws IOException {
        ChunkedOutput out = new ChunkedOutput(64);
        out.write(new byte[62]);
        ChunkedOutput.Frame f = out.beginFrame((byte) 9);
        byte[] payload = new byte[200];
        new Random(5L).nextBytes(payload);
        out.write(payload);
        f.close();

        // 62 prefix bytes, a 5 byte header at 62..66 straddling the first chunk boundary,
        // then 200 payload bytes at 67..266.
        byte[] bytes = out.toByteArray();
        assertEquals(267, bytes.length);
        assertEquals(267, out.size());
        assertEquals(9, bytes[62]);
        assertEquals(200, ByteBuffer.wrap(bytes, 63, 4).getInt());
        assertArrayEquals(payload, Arrays.copyOfRange(bytes, 67, 267));
        assertArrayEquals(bytes, flatten(out.toByteBuffers()));
        assertEquals(out.size(), remaining(out.toByteBuffers()));
    }

    @Test
    @DisplayName("a header may straddle a chunk boundary at any offset")
    void headerSpanningChunkBoundary() throws IOException {
        int chunk = 16;
        for (int prefix = chunk - 5; prefix <= chunk; prefix++) {
            ChunkedOutput out = new ChunkedOutput(chunk);
            out.write(new byte[prefix]);
            ChunkedOutput.Frame f = out.beginFrame((byte) 7);
            out.write(new byte[] {9, 9, 9, 9, 9});
            f.close();

            byte[] bytes = out.toByteArray();
            assertEquals(prefix + 10, bytes.length, "prefix " + prefix);
            assertEquals(7, bytes[prefix], "prefix " + prefix);
            assertEquals(5, ByteBuffer.wrap(bytes, prefix + 1, 4).getInt(), "prefix " + prefix);
            assertArrayEquals(bytes, flatten(out.toByteBuffers()), "prefix " + prefix);
            assertEquals(out.size(), remaining(out.toByteBuffers()), "prefix " + prefix);
        }
    }

    @Test
    @DisplayName("an empty frame is patched wherever its header ends relative to the chunk boundary")
    void emptyFrameAtAnyOffset() throws IOException {
        // Prefix 3 ends the header exactly at the chunk end: the current chunk is
        // full but not yet listed when patch() runs. Prefixes 4..8 straddle or
        // follow the boundary. Some cases (such as prefix 3, where the header
        // ends exactly at a chunk end) are guards for the new code that also
        // pass on the parent.
        for (int prefix = 3; prefix <= 8; prefix++) {
            ChunkedOutput out = new ChunkedOutput(8);
            out.write(new byte[prefix]);
            ChunkedOutput.Frame f = out.beginFrame((byte) 7);
            f.close();

            byte[] expected = new byte[prefix + 5];
            expected[prefix] = 7;
            assertArrayEquals(expected, out.toByteArray(), "prefix " + prefix);
            assertEquals(prefix + 5, out.size(), "prefix " + prefix);
            assertEquals(out.size(), remaining(out.toByteBuffers()), "prefix " + prefix);
        }
    }

    private static long remaining(ByteBuffer[] buffers) {
        long total = 0;
        for (ByteBuffer b : buffers) {
            total += b.remaining();
        }
        return total;
    }
}
