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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Records what was asked of the server and answers what the test dictates.
 * <p>
 * The transaction protocol is paths, query parameters and headers rather than
 * marshalled bodies, so a transport that remembers the URIs it was given is
 * enough to say exactly what a client decided to do.
 */
final class RecordingTransport implements HttpTransport {

    /** Every request URI, in order, as "METHOD path?query". */
    final List<String> calls = new ArrayList<>();

    /** The headers of every request, in the same order as {@link #calls}. */
    final List<Map<String, String>> requestHeaders = new ArrayList<>();

    /** The body of every request, in the same order as {@link #calls}. */
    final List<byte[]> requestBodies = new ArrayList<>();

    /** Whether an asynchronous exchange completes later and on another thread, as a real transport's does. */
    boolean asyncOnAnotherThread;

    private final Map<String, Response> canned = new HashMap<>();

    /** How many of the next exchanges are refused with 406 "unsupported codec", to provoke a downgrade. */
    int refuseUnsupportedCodec;

    private int status = Protocol_SC_NO_CONTENT;

    private Map<String, List<String>> headers = Map.of();

    private static final int Protocol_SC_NO_CONTENT = 204;

    /** Answers the next request matching this path fragment with these headers. */
    void answer(String pathFragment, int withStatus, Map<String, List<String>> withHeaders) {
        canned.put(pathFragment, new Response(withStatus, null, withHeaders,
                new ByteArrayInputStream(new byte[0])));
    }

    void answerAll(int withStatus, Map<String, List<String>> withHeaders) {
        this.status = withStatus;
        this.headers = withHeaders;
    }

    @Override
    public Response exchange(Request request) throws IOException {
        String uri = record(request);
        if (refuseUnsupportedCodec > 0) {
            refuseUnsupportedCodec--;
            return new Response(406, null, Map.of("X-GF-Reason", List.of("unsupported codec exotic")),
                    new ByteArrayInputStream(new byte[0]));
        }
        for (Map.Entry<String, Response> entry : canned.entrySet()) {
            if (uri.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return new Response(status, null, headers, new ByteArrayInputStream(new byte[0]));
    }

    private String record(Request request) {
        String uri = request.uri().getPath()
                + (request.uri().getRawQuery() == null ? "" : "?" + request.uri().getRawQuery());
        calls.add(request.method() + ' ' + uri);
        requestHeaders.add(request.headers());
        requestBodies.add(bytes(request.body()));
        return uri;
    }

    private static byte[] bytes(ByteBuffer[] body) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (body != null) {
            for (ByteBuffer buffer : body) {
                ByteBuffer view = buffer.duplicate();
                byte[] bytes = new byte[view.remaining()];
                view.get(bytes);
                out.write(bytes, 0, bytes.length);
            }
        }
        return out.toByteArray();
    }

    /** @return whether any recorded call's URI contains this fragment */
    boolean called(String fragment) {
        return calls.stream().anyMatch(c -> c.contains(fragment));
    }

    @Override
    public CompletableFuture<Response> exchangeAsync(Request request) {
        if (asyncOnAnotherThread) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return exchange(request);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }, CompletableFuture.delayedExecutor(100, TimeUnit.MILLISECONDS));
        }
        record(request);
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public String negotiatedVersion() {
        return "HTTP/1.1";
    }

    @Override
    public void close() {
    }
}
