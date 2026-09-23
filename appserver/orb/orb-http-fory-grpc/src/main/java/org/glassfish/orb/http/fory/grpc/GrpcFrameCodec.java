/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.orb.http.fory.grpc;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The five-byte message envelope used by HTTP/2 gRPC.
 *
 * <p>The payload is intentionally opaque here. The Fory generated client and
 * server agree on its schema and serialization; this class only makes the
 * bridge independent of a particular gRPC implementation.</p>
 */
public final class GrpcFrameCodec {

    private static final int HEADER_SIZE = 5;
    private static final int FLAG_UNCOMPRESSED = 0;

    private GrpcFrameCodec() {
    }

    public static void write(OutputStream output, byte[] payload) throws IOException {
        if (payload == null) {
            throw new NullPointerException("payload");
        }
        output.write(FLAG_UNCOMPRESSED);
        output.write((payload.length >>> 24) & 0xff);
        output.write((payload.length >>> 16) & 0xff);
        output.write((payload.length >>> 8) & 0xff);
        output.write(payload.length & 0xff);
        output.write(payload);
    }

    public static byte[] read(InputStream input, int maxPayloadBytes) throws IOException {
        if (maxPayloadBytes < 0) {
            throw new IllegalArgumentException("maxPayloadBytes must not be negative");
        }
        int flags = input.read();
        if (flags < 0) {
            throw new EOFException("missing gRPC frame header");
        }
        if (flags != FLAG_UNCOMPRESSED) {
            throw new IOException("compressed gRPC frames are not supported by the Fory bridge");
        }
        int b1 = input.read();
        int b2 = input.read();
        int b3 = input.read();
        int b4 = input.read();
        if ((b1 | b2 | b3 | b4) < 0) {
            throw new EOFException("truncated gRPC frame header");
        }
        int length = (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
        if (length < 0 || length > maxPayloadBytes) {
            throw new IOException("gRPC frame exceeds configured limit: " + length);
        }
        byte[] payload = input.readNBytes(length);
        if (payload.length != length) {
            throw new EOFException("truncated gRPC frame payload");
        }
        return payload;
    }
}
