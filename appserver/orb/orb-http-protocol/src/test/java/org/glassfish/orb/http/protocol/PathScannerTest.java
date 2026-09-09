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


import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathScannerTest {

    /**
     * The SWAR word test is the one piece of hand-written bit twiddling in the
     * protocol, so it is checked exhaustively against the obvious loop rather
     * than on a handful of examples. Every start offset and every length is
     * exercised, which covers the word-aligned body, the scalar tail, and the
     * case where the match straddles a word boundary.
     */
    @Test
    @DisplayName("SWAR indexOf agrees with a naive scan at every offset and length")
    void swarIndexOfMatchesNaiveScan() {
        Random random = new Random(20260907L);
        for (int trial = 0; trial < 200; trial++) {
            byte[] data = new byte[random.nextInt(64) + 1];
            for (int i = 0; i < data.length; i++) {
                // A small alphabet makes hits and misses both common.
                data[i] = (byte) ("ab/c".charAt(random.nextInt(4)));
            }
            for (int from = 0; from <= data.length; from++) {
                for (int to = from; to <= data.length; to++) {
                    int expected = naiveIndexOf(data, from, to, (byte) '/');
                    int actual = PathScanner.indexOf(data, from, to, (byte) '/');
                    assertEquals(expected, actual,
                            () -> "mismatch scanning " + new String(data, StandardCharsets.US_ASCII));
                }
            }
        }
    }

    private static int naiveIndexOf(byte[] b, int from, int to, byte c) {
        for (int i = from; i < to; i++) {
            if (b[i] == c) {
                return i;
            }
        }
        return -1;
    }

    @Test
    @DisplayName("a byte with the high bit set is not mistaken for the delimiter")
    void highBitBytesDoNotFalsePositive() {
        byte[] data = new byte[16];
        java.util.Arrays.fill(data, (byte) 0x80);
        assertEquals(-1, PathScanner.indexOf(data, 0, data.length, (byte) '/'));

        data[11] = '/';
        assertEquals(11, PathScanner.indexOf(data, 0, data.length, (byte) '/'));
    }

    @Test
    void splitsSegmentsAndSkipsEmptyOnes() throws Exception {
        PathScanner path = PathScanner.scan("/glassfish-services/ejb/v1/invoke");
        assertEquals(4, path.count());
        assertTrue(path.segmentEquals(0, "glassfish-services"));
        assertTrue(path.segmentEquals(1, "ejb"));
        assertTrue(path.segmentEquals(2, "v1"));
        assertTrue(path.segmentEquals(3, "invoke"));
        assertFalse(path.segmentEquals(3, "invoke2"));
        assertFalse(path.segmentEquals(9, "invoke"));
    }

    @Test
    @DisplayName("doubled and trailing slashes do not produce empty segments")
    void toleratesRedundantSlashes() throws Exception {
        PathScanner path = PathScanner.scan("//a///b/");
        assertEquals(2, path.count());
        assertEquals("a", path.segment(0));
        assertEquals("b", path.segment(1));
    }

    @Test
    void decodesPercentEscapes() throws Exception {
        PathScanner path = PathScanner.scan("/a%2Fb/caff%C3%A8");
        assertEquals("a/b", path.segment(0));
        assertEquals("caffè", path.segment(1));
        assertEquals("caff%C3%A8", path.rawSegment(1));
    }

    @Test
    void rejectsTruncatedPercentEscape() throws Exception {
        PathScanner path = PathScanner.scan("/a%2");
        assertThrows(ProtocolException.class, () -> path.segment(0));
    }

    @Test
    @DisplayName("a JNDI name spanning several segments is rejoined")
    void joinsTailSegments() throws Exception {
        PathScanner path = PathScanner.scan("/gf/naming/v1/lookup/java:global/app/Bean");
        assertEquals("java:global/app/Bean", path.joinFrom(4));
    }

    @Test
    void treatsDashAsAbsent() throws Exception {
        PathScanner path = PathScanner.scan("/a/-/b");
        assertEquals("-", path.segment(1));
        assertEquals(null, path.optionalSegment(1));
        assertEquals("a", path.optionalSegment(0));
    }

    @Test
    void rejectsAnAbsurdNumberOfSegments() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            sb.append("/x");
        }
        assertThrows(ProtocolException.class, () -> PathScanner.scan(sb.toString()));
    }

    @Test
    void scanningIsIndependentOfBufferOffset() throws Exception {
        byte[] framed = ("XXXX" + "/a/b" + "YYYY").getBytes(StandardCharsets.UTF_8);
        PathScanner path = PathScanner.scan(framed, 4, 4);
        assertEquals(2, path.count());
        assertArrayEquals(new String[] { "a", "b" },
                new String[] { path.segment(0), path.segment(1) });
    }
}
