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





import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Splits a request path into segments <em>without allocating them</em>.
 * <p>
 * The invoke path of this protocol has eleven segments:
 * <pre>
 * /glassfish-services/ejb/v1/invoke/{app}/{module}/{distinct}/{bean}/{session}/{view}/{method}/{paramTypes}
 * </pre>
 * The obvious implementation - {@code new String(bytes, UTF_8).split("/")} -
 * allocates a String for the whole path, an array, and a String per segment,
 * on every single invocation, and then throws almost all of them away: routing
 * only needs to compare three of them against constants. This class instead
 * records segment boundaries as index pairs and materialises a String only for
 * the segments a caller actually asks for. Routing can be done entirely through
 * {@link #segmentEquals}, which allocates nothing.
 *
 * <h2>On SWAR</h2>
 * {@link #indexOf} finds a delimiter eight bytes at a time using the classic
 * SWAR zero-byte test, {@code (x - 0x01..01) & ~x & 0x80..80}. This is worth
 * writing by hand <em>here</em> and almost nowhere else in this codebase, for
 * one specific reason: we are scanning a {@code byte[]} that came off the
 * network, so we skip the UTF-8 decode and the String allocation entirely.
 * {@code String.indexOf} is itself an intrinsic and is not something a hand
 * written loop beats - but it requires a String to exist first, and that String
 * is the cost we are avoiding.
 * <p>
 * Do not read this as a general endorsement. A remote EJB invocation spends its
 * time in TLS, in the network round trip, and in serialization; path scanning
 * is far down the profile. The reason to do it this way is that it costs
 * nothing to write once and it keeps the per-invocation allocation count near
 * zero, which matters for GC pressure under load rather than for latency of a
 * single call.
 */
public final class PathScanner {

    private static final VarHandle LONG_LE =
            MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);

    private static final long LOW = 0x0101010101010101L;
    private static final long HIGH = 0x8080808080808080L;

    /** Enough for the longest path this protocol defines, with headroom. */
    private static final int MAX_SEGMENTS = 24;

    private final byte[] buf;
    private final int[] starts = new int[MAX_SEGMENTS];
    private final int[] ends = new int[MAX_SEGMENTS];
    private int count;

    private PathScanner(byte[] buf) {
        this.buf = buf;
    }

    /**
     * Scans {@code buf[off, off+len)} as a slash-separated path. Empty
     * segments (a leading slash, a doubled slash) are skipped.
     */
    public static PathScanner scan(byte[] buf, int off, int len) throws ProtocolException {
        PathScanner s = new PathScanner(buf);
        int end = off + len;
        int i = off;
        while (i < end) {
            int slash = indexOf(buf, i, end, (byte) '/');
            int segEnd = slash < 0 ? end : slash;
            if (segEnd > i) {
                if (s.count == MAX_SEGMENTS) {
                    throw new ProtocolException("path has more than " + MAX_SEGMENTS + " segments");
                }
                s.starts[s.count] = i;
                s.ends[s.count] = segEnd;
                s.count++;
            }
            if (slash < 0) {
                break;
            }
            i = slash + 1;
        }
        return s;
    }

    public static PathScanner scan(String path) throws ProtocolException {
        byte[] b = path.getBytes(StandardCharsets.UTF_8);
        return scan(b, 0, b.length);
    }

    /**
     * SWAR search for {@code c} in {@code b[from, to)}.
     *
     * @return the index of the first occurrence, or -1
     */
    static int indexOf(byte[] b, int from, int to, byte c) {
        long pattern = (c & 0xFFL) * LOW;
        int i = from;
        int limit = to - Long.BYTES;
        while (i <= limit) {
            // Little-endian load: byte at the lowest address occupies the
            // least significant octet, so trailing zeros map to the first match.
            long w = (long) LONG_LE.get(b, i);
            long x = w ^ pattern;
            long m = (x - LOW) & ~x & HIGH;
            if (m != 0) {
                return i + (Long.numberOfTrailingZeros(m) >>> 3);
            }
            i += Long.BYTES;
        }
        for (; i < to; i++) {
            if (b[i] == c) {
                return i;
            }
        }
        return -1;
    }

    public int count() {
        return count;
    }

    /**
     * Compares a segment against an ASCII literal without allocating.
     * Used for routing ({@code "ejb"}, {@code "v1"}, {@code "invoke"}).
     */
    public boolean segmentEquals(int index, String ascii) {
        if (index < 0 || index >= count) {
            return false;
        }
        int start = starts[index];
        int len = ends[index] - start;
        if (len != ascii.length()) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (buf[start + i] != (byte) ascii.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /** @return the raw (still percent-encoded) segment as a String. */
    public String rawSegment(int index) throws ProtocolException {
        checkIndex(index);
        return new String(buf, starts[index], ends[index] - starts[index], StandardCharsets.UTF_8);
    }

    /** @return the segment with percent-escapes decoded. */
    public String segment(int index) throws ProtocolException {
        checkIndex(index);
        int start = starts[index];
        int end = ends[index];
        int pct = indexOf(buf, start, end, (byte) '%');
        if (pct < 0) {
            return new String(buf, start, end - start, StandardCharsets.UTF_8);
        }
        return percentDecode(buf, start, end);
    }

    /** @return the segment, or {@code null} if it is the placeholder {@code "-"}. */
    public String optionalSegment(int index) throws ProtocolException {
        String s = segment(index);
        return "-".equals(s) ? null : s;
    }

    /**
     * Joins the segments from {@code index} to the end back into a
     * slash-separated string, decoding percent escapes.
     * <p>
     * Used by the naming service: a JNDI name such as
     * {@code java:global/myapp/MyBean} contains slashes, and encoding them as
     * {@code %2F} inside a single segment is rejected or silently normalised
     * by a good number of proxies and servlet containers. Letting the name
     * occupy the tail of the path avoids the problem entirely.
     */
    public String joinFrom(int index) throws ProtocolException {
        checkIndex(index);
        StringBuilder sb = new StringBuilder(64);
        for (int i = index; i < count; i++) {
            if (i > index) {
                sb.append('/');
            }
            sb.append(segment(i));
        }
        return sb.toString();
    }

    private void checkIndex(int index) throws ProtocolException {
        if (index < 0 || index >= count) {
            throw new ProtocolException("path has " + count + " segments, asked for index " + index);
        }
    }

    private static String percentDecode(byte[] buf, int start, int end) throws ProtocolException {
        byte[] out = new byte[end - start];
        int o = 0;
        for (int i = start; i < end; i++) {
            byte b = buf[i];
            if (b == '%') {
                if (i + 2 >= end) {
                    throw new ProtocolException("truncated percent escape in path");
                }
                int hi = hexDigit(buf[i + 1]);
                int lo = hexDigit(buf[i + 2]);
                if (hi < 0 || lo < 0) {
                    throw new ProtocolException("malformed percent escape in path");
                }
                out[o++] = (byte) ((hi << 4) | lo);
                i += 2;
            } else {
                out[o++] = b;
            }
        }
        return new String(out, 0, o, StandardCharsets.UTF_8);
    }

    private static int hexDigit(byte b) {
        if (b >= '0' && b <= '9') {
            return b - '0';
        }
        if (b >= 'a' && b <= 'f') {
            return b - 'a' + 10;
        }
        if (b >= 'A' && b <= 'F') {
            return b - 'A' + 10;
        }
        return -1;
    }
}
