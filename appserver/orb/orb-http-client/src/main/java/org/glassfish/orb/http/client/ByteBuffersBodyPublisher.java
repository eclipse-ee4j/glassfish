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

package org.glassfish.orb.http.client;

import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

/**
 * A {@link HttpRequest.BodyPublisher} that publishes pre-marshalled buffers
 * as they are, without copying.
 * <p>
 * The JDK's ready-made publishers all copy: {@code ofByteArray} defensively
 * copies the array, and {@code ofByteArrays} copies each element. Since
 * {@code BodyPublisher} is just a {@code Flow.Publisher<ByteBuffer>}, handing
 * the chunks produced by
 * {@code org.glassfish.orb.http.protocol.ChunkedOutput} straight to the
 * subscriber removes the last copy on the request path.
 */
final class ByteBuffersBodyPublisher implements HttpRequest.BodyPublisher {

    private final List<ByteBuffer> buffers;
    private final long contentLength;

    ByteBuffersBodyPublisher(ByteBuffer[] buffers) {
        this.buffers = List.of(buffers);
        long total = 0;
        for (ByteBuffer b : buffers) {
            total += b.remaining();
        }
        this.contentLength = total;
    }

    @Override
    public long contentLength() {
        return contentLength;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
        // Duplicate rather than copy: a duplicate shares the backing array and
        // carries its own position, so a retried request republishes correctly.
        List<ByteBuffer> toSend = buffers.stream().map(ByteBuffer::duplicate).toList();
        try (SubmissionPublisher<ByteBuffer> publisher = new SubmissionPublisher<>(Runnable::run, Flow.defaultBufferSize())) {
            publisher.subscribe(subscriber);
            for (ByteBuffer b : toSend) {
                publisher.submit(b);
            }
        }
    }

    @Override
    public String toString() {
        return "ByteBuffersBodyPublisher[" + buffers.size() + " chunks, " + contentLength + " bytes]";
    }

    static HttpRequest.BodyPublisher of(ByteBuffer[] buffers) {
        if (buffers == null || buffers.length == 0) {
            return HttpRequest.BodyPublishers.noBody();
        }
        return new ByteBuffersBodyPublisher(Arrays.copyOf(buffers, buffers.length));
    }
}
