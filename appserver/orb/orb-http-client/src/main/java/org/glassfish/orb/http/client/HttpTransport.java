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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The client's HTTP transport, behind an interface.
 * <p>
 * This indirection is the whole reason the protocol was kept free of any
 * HTTP-version-specific machinery. {@link JdkHttpTransport} speaks HTTP/1.1
 * and HTTP/2 on Java 21; a second implementation adding HTTP/3 needs Java 26,
 * where {@code HttpClient.Version.HTTP_3} and {@code Alt-Svc} negotiation
 * arrived with JEP 517. Because nothing above this interface knows which is in
 * use, that is a deployment decision - and a client behind an HTTP/1.1-only
 * proxy keeps working unchanged.
 */
public interface HttpTransport extends AutoCloseable {

    /** A request. {@code body} may be empty; its buffers are not copied. */
    record Request(String method,
                   URI uri,
                   String contentType,
                   String accept,
                   Map<String, String> headers,
                   ByteBuffer[] body) {

        public Request {
            headers = headers == null ? Map.of() : Map.copyOf(headers);
            body = body == null ? new ByteBuffer[0] : body;
        }
    }

    /**
     * A response. {@code body} is a stream over the network buffers as they
     * arrive - it is deliberately not a {@code byte[]}, so that a large
     * marshalled result is fed straight to the deserializer instead of being
     * accumulated first.
     */
    record Response(int status,
                    String contentType,
                    Map<String, List<String>> headers,
                    InputStream body) implements AutoCloseable {

        public String firstHeader(String name) {
            List<String> values = headers().get(name);
            return values == null || values.isEmpty() ? null : values.get(0);
        }

        @Override
        public void close() throws IOException {
            if (body != null) {
                body.close();
            }
        }
    }

    Response exchange(Request request) throws IOException, InterruptedException;

    CompletableFuture<Response> exchangeAsync(Request request);

    /**
     * @return a short description of the HTTP version actually in use, for
     *         logging - "HTTP/1.1", "HTTP/2", "HTTP/3"
     */
    String negotiatedVersion();

    @Override
    void close();
}
