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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * An {@link InputStream} over a chain of {@link ByteBuffer}s, so a request
 * body that arrived as several network buffers can be deserialized without
 * first being coalesced into one array.
 * <p>
 * This is the read-side counterpart to {@link ChunkedOutput}. Grizzly delivers
 * a body as a composite buffer and the JDK HTTP client delivers it as a
 * sequence of {@code ByteBuffer}s; both are naturally consumed here, whereas
 * {@code readAllBytes()} would allocate and copy the whole payload before
 * {@code ObjectInputStream} even starts.
 */
public final class ChunkedInput extends InputStream {

    private final ByteBuffer[] buffers;
    private int index;

    public ChunkedInput(ByteBuffer... buffers) {
        this.buffers = buffers.clone();
    }

    public ChunkedInput(List<ByteBuffer> buffers) {
        this.buffers = buffers.toArray(new ByteBuffer[0]);
    }

    private ByteBuffer currentBuffer() {
        while (index < buffers.length && !buffers[index].hasRemaining()) {
            index++;
        }
        return index < buffers.length ? buffers[index] : null;
    }

    @Override
    public int read() {
        ByteBuffer b = currentBuffer();
        return b == null ? -1 : b.get() & 0xFF;
    }

    @Override
    public int read(byte[] dst, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > dst.length) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }
        int read = 0;
        while (read < len) {
            ByteBuffer b = currentBuffer();
            if (b == null) {
                break;
            }
            int n = Math.min(len - read, b.remaining());
            b.get(dst, off + read, n);
            read += n;
        }
        return read == 0 ? -1 : read;
    }

    @Override
    public int available() {
        int n = 0;
        for (int i = index; i < buffers.length; i++) {
            n += buffers[i].remaining();
        }
        return n;
    }

    @Override
    public long skip(long n) {
        long skipped = 0;
        while (skipped < n) {
            ByteBuffer b = currentBuffer();
            if (b == null) {
                break;
            }
            int step = (int) Math.min(n - skipped, b.remaining());
            b.position(b.position() + step);
            skipped += step;
        }
        return skipped;
    }
}
