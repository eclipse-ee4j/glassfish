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





import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An {@link OutputStream} that accumulates into a chain of fixed-size chunks
 * and can hand them out as {@link ByteBuffer}s <em>without copying</em>.
 *
 * <h2>Why not ByteArrayOutputStream</h2>
 * The obvious way to marshal a reply is
 * {@code new ObjectOutputStream(new ByteArrayOutputStream())} followed by
 * {@code toByteArray()}. That pays for two classes of copy on every
 * invocation:
 * <ul>
 *   <li>{@code ByteArrayOutputStream} grows by allocating a larger array and
 *       {@code System.arraycopy}-ing the old one into it. Serializing a 4 MB
 *       result from the 32-byte default therefore copies roughly 8 MB in
 *       total across ~17 reallocations.</li>
 *   <li>{@code toByteArray()} then copies the whole buffer once more, so the
 *       peak footprint is twice the payload.</li>
 * </ul>
 * This class never copies: a full chunk is simply appended to a list, and the
 * chunks are exposed as buffers that the transport writes straight out. Peak
 * footprint is the payload plus at most one partly-filled chunk.
 *
 * <h2>What "zero-copy" does and does not mean here</h2>
 * It means zero <em>additional</em> copies of the marshalled bytes between the
 * serializer and the socket. It does not mean zero copies overall: Java
 * serialization inherently walks the object graph and writes its bytes out,
 * and that traversal is by far the dominant cost. Nor is this
 * {@code sendfile}-style zero-copy - there is no file and no
 * {@code FileChannel.transferTo} to exploit, since the bytes are produced in
 * user space. The honest claim is narrow: we stopped copying a buffer we had
 * already built, three times.
 */
public final class ChunkedOutput extends OutputStream {

    /**
     * 16 KB. Large enough that a typical invocation reply is one or two
     * chunks, small enough to stay well inside the G1 humongous-object
     * threshold (half a region, i.e. 512 KB at the smallest region size) so
     * chunks are allocated in TLABs and die young.
     */
    public static final int DEFAULT_CHUNK_SIZE = 16 * 1024;

    private final int chunkSize;
    private final List<byte[]> chunks = new ArrayList<>(4);
    private byte[] current;
    private int currentPos;
    private long total;

    public ChunkedOutput() {
        this(DEFAULT_CHUNK_SIZE);
    }

    public ChunkedOutput(int chunkSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be positive");
        }
        this.chunkSize = chunkSize;
        this.current = new byte[chunkSize];
    }

    @Override
    public void write(int b) {
        if (currentPos == chunkSize) {
            rollOver();
        }
        current[currentPos++] = (byte) b;
        total++;
    }

    @Override
    public void write(byte[] b, int off, int len) {
        if (off < 0 || len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException();
        }
        int remaining = len;
        int pos = off;
        while (remaining > 0) {
            if (currentPos == chunkSize) {
                rollOver();
            }
            int n = Math.min(remaining, chunkSize - currentPos);
            System.arraycopy(b, pos, current, currentPos, n);
            currentPos += n;
            pos += n;
            remaining -= n;
        }
        total += len;
    }

    private void rollOver() {
        chunks.add(current);
        current = new byte[chunkSize];
        currentPos = 0;
    }

    /** @return the number of bytes written so far. */
    public long size() {
        return total;
    }

    /**
     * Exposes the accumulated bytes as read-only buffers over the internal
     * chunks. No data is copied; the returned buffers are invalidated by any
     * further write to this stream.
     */
    public ByteBuffer[] toByteBuffers() {
        int n = chunks.size() + (currentPos > 0 ? 1 : 0);
        ByteBuffer[] out = new ByteBuffer[n];
        for (int i = 0; i < chunks.size(); i++) {
            out[i] = ByteBuffer.wrap(chunks.get(i), 0, chunkSize).asReadOnlyBuffer();
        }
        if (currentPos > 0) {
            out[n - 1] = ByteBuffer.wrap(current, 0, currentPos).asReadOnlyBuffer();
        }
        return out;
    }

    /** Immutable view of the filled chunks, for transports that want arrays. */
    public List<byte[]> chunks() {
        return Collections.unmodifiableList(chunks);
    }

    /**
     * Coalescing escape hatch, for transports that can only take one array.
     * This is the copy the rest of the class exists to avoid - call it only
     * when the transport leaves no choice.
     */
    public byte[] toByteArray() {
        if (total > Integer.MAX_VALUE) {
            throw new OutOfMemoryError("payload larger than 2 GiB: " + total);
        }
        byte[] out = new byte[(int) total];
        int pos = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, out, pos, chunkSize);
            pos += chunkSize;
        }
        System.arraycopy(current, 0, out, pos, currentPos);
        return out;
    }

    @Override
    public void close() {
        // nothing to release; chunks are plain heap arrays
    }
}
