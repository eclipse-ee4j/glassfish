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

import java.io.IOException;
import java.io.ObjectInputFilter;

/**
 * Dispatches one already-routed Fory gRPC call.
 *
 * <p>The skeleton and target are deployment state supplied by GlassFish. The
 * call path performs only a map lookup, filtered decode, cached method-handle
 * invocation and encode; method reflection and view enumeration happen at
 * deployment time.</p>
 */
public final class ForyGrpcDispatcher {

    private final ForyGrpcSkeleton skeleton;
    private final ForyGrpcPayloadCodec codec;
    private final int maxPayloadBytes;

    public ForyGrpcDispatcher(ForyGrpcSkeleton skeleton,
                              ForyGrpcPayloadCodec codec,
                              int maxPayloadBytes) {
        if (maxPayloadBytes < 0) {
            throw new IllegalArgumentException("maxPayloadBytes must not be negative");
        }
        this.skeleton = java.util.Objects.requireNonNull(skeleton, "skeleton");
        this.codec = java.util.Objects.requireNonNull(codec, "codec");
        this.maxPayloadBytes = maxPayloadBytes;
    }

    /** Invokes one method and returns one framed response. */
    public byte[] dispatch(String path, Object target, byte[] requestFrame,
                           ClassLoader loader, ObjectInputFilter filter)
            throws IOException, ClassNotFoundException, Throwable {
        Object request = codec.decodeFrame(requestFrame, maxPayloadBytes, loader, filter);
        Object response = skeleton.invoke(path, target, request);
        return codec.encodeFrame(response);
    }
}
