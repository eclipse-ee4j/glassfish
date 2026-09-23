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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;

import org.glassfish.orb.http.codec.fory.ForyMarshaller;
import org.glassfish.orb.http.protocol.Marshaller;

/**
 * Encodes one Fory value as one gRPC message payload.
 *
 * <p>The existing Fory marshaller remains the single place that owns Fory
 * configuration and deserialization filtering. This adapter only supplies
 * the message boundary expected by generated Fory gRPC clients, so the server
 * does not grow a second serializer with different security semantics.</p>
 */
public final class ForyGrpcPayloadCodec {

    private final Marshaller marshaller;

    public ForyGrpcPayloadCodec() {
        this(new ForyMarshaller());
    }

    ForyGrpcPayloadCodec(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    /** Encodes exactly one request or response value. */
    public byte[] encode(Object value) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(256);
        try (Marshaller.ObjectWriter writer = marshaller.newWriter(bytes)) {
            writer.writeObject(value);
            writer.flush();
        }
        return bytes.toByteArray();
    }

    /** Decodes one value using the same filtered Fory reader as HTTP EJB calls. */
    public Object decode(byte[] payload, ClassLoader loader, ObjectInputFilter filter)
            throws IOException, ClassNotFoundException {
        if (payload == null) {
            throw new NullPointerException("payload");
        }
        try (Marshaller.ObjectReader reader = marshaller.newReader(
                new ByteArrayInputStream(payload), loader, filter)) {
            return reader.readObject();
        }
    }

    /** Wraps one encoded value in the standard uncompressed gRPC envelope. */
    public byte[] encodeFrame(Object value) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(261);
        GrpcFrameCodec.write(bytes, encode(value));
        return bytes.toByteArray();
    }

    /** Reads one gRPC envelope and decodes its Fory payload. */
    public Object decodeFrame(byte[] frame, int maxPayloadBytes,
                              ClassLoader loader, ObjectInputFilter filter)
            throws IOException, ClassNotFoundException {
        byte[] payload = GrpcFrameCodec.read(new ByteArrayInputStream(frame), maxPayloadBytes);
        return decode(payload, loader, filter);
    }
}
