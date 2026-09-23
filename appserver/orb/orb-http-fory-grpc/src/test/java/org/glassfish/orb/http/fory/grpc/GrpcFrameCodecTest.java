/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.orb.http.fory.grpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GrpcFrameCodecTest {

    @Test
    void writesAndReadsAnUncompressedFrame() throws IOException {
        byte[] payload = "fory-request".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        GrpcFrameCodec.write(output, payload);

        assertArrayEquals(payload, GrpcFrameCodec.read(
                new ByteArrayInputStream(output.toByteArray()), 1024));
    }

    @Test
    void rejectsCompressedFrames() {
        byte[] compressed = {1, 0, 0, 0, 0};

        assertThrows(IOException.class, () -> GrpcFrameCodec.read(
                new ByteArrayInputStream(compressed), 1024));
    }

    @Test
    void payloadCodecRoundTripsValueInsideGrpcFrame() throws Exception {
        ForyGrpcPayloadCodec codec = new ForyGrpcPayloadCodec();
        byte[] frame = codec.encodeFrame("hello-fory");

        Object value = codec.decodeFrame(frame, 1024 * 1024,
                getClass().getClassLoader(), ObjectInputFilter.Config.createFilter("java.lang.String;!*"));

        assertEquals("hello-fory", value);
    }

    @Test
    void enforcesThePayloadLimit() {
        byte[] oversized = {0, 0, 0, 0, 2, 1, 2};

        assertThrows(IOException.class, () -> GrpcFrameCodec.read(
                new ByteArrayInputStream(oversized), 1));
    }
}
