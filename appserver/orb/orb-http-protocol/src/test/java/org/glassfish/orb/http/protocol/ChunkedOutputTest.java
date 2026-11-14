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
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    @DisplayName("an aborted frame leaves the stream as it was, whatever was written after the header")
    void abortedFrameLeavesNoBytes() throws IOException {
        for (int prefix = 0; prefix <= 20; prefix++) {
            for (int payload : new int[] {0, 3, 30}) {
                String where = "prefix " + prefix + ", payload " + payload;
                byte[] head = new byte[prefix];
                Arrays.fill(head, (byte) 1);
                ChunkedOutput out = new ChunkedOutput(8);
                out.write(head);

                ChunkedOutput.Frame f = out.beginFrame((byte) 7);
                out.write(new byte[payload]);
                f.abort();
                // Closing after an abort must not patch a length into bytes
                // that are no longer there, and aborting twice is harmless.
                f.close();
                f.abort();

                assertEquals(prefix, out.size(), where);
                assertArrayEquals(head, out.toByteArray(), where);
                assertArrayEquals(head, flatten(out.toByteBuffers()), where);
                assertEquals(prefix, remaining(out.toByteBuffers()), where);

                // The stream stays usable and the next bytes follow the prefix directly.
                out.write(new byte[] {5, 6, 7});
                byte[] expected = Arrays.copyOf(head, prefix + 3);
                expected[prefix] = 5;
                expected[prefix + 1] = 6;
                expected[prefix + 2] = 7;
                assertArrayEquals(expected, out.toByteArray(), where);
                assertArrayEquals(expected, flatten(out.toByteBuffers()), where);
                assertEquals(prefix + 3, remaining(out.toByteBuffers()), where);
            }
        }
    }

    @Test
    @DisplayName("a frame that was closed cannot be aborted")
    void closedFrameCannotBeAborted() {
        ChunkedOutput out = new ChunkedOutput(8);
        ChunkedOutput.Frame f = out.beginFrame((byte) 7);
        f.close();
        assertThrows(IllegalStateException.class, f::abort);
        assertEquals(5, out.size());
    }

    @Test
    @DisplayName("[guard] closing a frame twice patches the same length and leaves the same bytes")
    void closingTwiceLeavesTheSameBytes() throws IOException {
        // GUARD / characterization test: it passes with and without the abort
        // change, because the second close was already a no-op.
        ChunkedOutput out = new ChunkedOutput(8);
        ChunkedOutput.Frame f = out.beginFrame((byte) 7);
        out.write(new byte[] {1, 2, 3});
        f.close();
        byte[] once = out.toByteArray();
        out.write(new byte[] {9});
        f.close();
        byte[] twice = out.toByteArray();

        assertArrayEquals(new byte[] {7, 0, 0, 0, 3, 1, 2, 3}, once);
        assertArrayEquals(once, Arrays.copyOf(twice, 8));
        assertEquals(9, twice[8]);
    }

    @Test
    @DisplayName("[guard] every chunk listed by chunks() is full and together they hold all but the tail")
    void listedChunksAreAllFull() throws IOException {
        // GUARD / characterization test: documents the invariant that patch()
        // and toByteArray() rely on; it is not expected to fail on a revert.
        ChunkedOutput out = new ChunkedOutput(16);
        out.write(new byte[14]);
        ChunkedOutput.Frame f = out.beginFrame((byte) 7);
        out.write(new byte[100]);
        f.close();

        assertEquals(119, out.size());
        // 119 bytes in chunks of 16: seven full ones and a tail of seven bytes.
        assertEquals(7, out.chunks().size());
        for (byte[] chunk : out.chunks()) {
            assertEquals(16, chunk.length);
        }
        assertEquals(8, out.toByteBuffers().length);
        assertEquals(7, out.toByteBuffers()[7].remaining());
    }

    @Test
    @DisplayName("a frame cannot be begun while another one is open")
    void beginWhileOpenIsRefused() throws IOException {
        // Scenario A: f1 = begin; f2 = begin; f1.abort(); write; f2.close().
        // Without the check f2 would sit inside f1's bytes, f1.abort() would
        // cut it off and f2.close() would patch a header that is gone.
        ChunkedOutput out = new ChunkedOutput(8);
        out.write(new byte[] {1, 2});
        ChunkedOutput.Frame f1 = out.beginFrame((byte) 7);
        assertThrows(IllegalStateException.class, () -> out.beginFrame((byte) 8));
        assertEquals(7L, out.size(), "the refused begin wrote nothing");
        f1.abort();
        out.write(new byte[] {5, 6, 7, 8, 9, 10});
        assertArrayEquals(new byte[] {1, 2, 5, 6, 7, 8, 9, 10}, out.toByteArray());

        // Once the first frame is closed or aborted a new one can begin.
        ChunkedOutput.Frame f3 = out.beginFrame((byte) 3);
        f3.close();
        ChunkedOutput.Frame f4 = out.beginFrame((byte) 4);
        f4.abort();
        assertEquals(13L, out.size());
    }

    @Test
    @DisplayName("[guard] a closed frame cannot be aborted after a later frame was closed")
    void staleFrameCannotBeAborted() throws IOException {
        // GUARD / characterization test: a closed frame already refused abort()
        // before this change. The open-frame checks in abort() and close()
        // are defensive: with beginFrame() refusing a second open frame, no
        // sequence of calls reaches them, so no test can fail on removing them.
        // Scenario B: f1 closed, f2 begun and closed, then f1.abort() must not
        // cut the stream back to f1's header and swallow f2.
        ChunkedOutput out = new ChunkedOutput(8);
        ChunkedOutput.Frame f1 = out.beginFrame((byte) 1);
        out.write(new byte[] {1});
        f1.close();
        ChunkedOutput.Frame f2 = out.beginFrame((byte) 2);
        out.write(new byte[] {2, 2});
        f2.close();
        byte[] before = out.toByteArray();
        assertThrows(IllegalStateException.class, f1::abort);
        assertArrayEquals(before, out.toByteArray());
        assertEquals(13L, out.size());
    }

    @Test
    @DisplayName("a frame can be begun and closed after an abort took a chunk back from the list")
    void frameAfterAbortThatReclaimedAChunk() throws IOException {
        // Chunk size 4: the header at 3..7 lands in chunk 0 and 1, and 20 bytes
        // of payload push several chunks into the list. The abort reclaims
        // chunk 0 as the current one and forgets the others.
        ChunkedOutput out = new ChunkedOutput(4);
        out.write(new byte[] {1, 2, 3});
        ChunkedOutput.Frame f = out.beginFrame((byte) 7);
        out.write(new byte[20]);
        assertTrue(out.chunks().size() >= 5);
        f.abort();
        assertEquals(0, out.chunks().size(), "chunk 0 is the current one again");
        assertEquals(3L, out.size());

        ChunkedOutput.Frame g = out.beginFrame((byte) 9);
        out.write(new byte[] {4, 5, 6});
        g.close();
        byte[] expected = {1, 2, 3, 9, 0, 0, 0, 3, 4, 5, 6};
        assertArrayEquals(expected, out.toByteArray());
        assertArrayEquals(expected, flatten(out.toByteBuffers()));
        assertEquals(2, out.chunks().size());
        assertEquals(expected.length, remaining(out.toByteBuffers()));
    }

    private static long remaining(ByteBuffer[] buffers) {
        long total = 0;
        for (ByteBuffer b : buffers) {
            total += b.remaining();
        }
        return total;
    }
}
