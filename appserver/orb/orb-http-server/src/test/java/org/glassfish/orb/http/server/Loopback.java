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

package org.glassfish.orb.http.server;





import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.glassfish.orb.http.client.HttpTransport;

/**
 * Test scaffolding: an {@link HttpTransport} that, instead of opening a
 * socket, drives the server dispatchers directly through an in-memory
 * {@link ServerExchange}.
 * <p>
 * This is the payoff of keeping the dispatchers behind {@link ServerExchange}:
 * the client, the wire format and the server dispatch can all be exercised in
 * one process, with no container, no port and no HTTP stack in the way, so a
 * failure points at the protocol rather than at the plumbing.
 */
final class Loopback implements HttpTransport {

    private final EjbDispatcher ejb;
    private final NamingDispatcher naming;
    private final AffinityDispatcher affinity;
    private final TransactionDispatcher transactions;

    Loopback(EjbDispatcher ejb, NamingDispatcher naming) {
        this(ejb, naming, new AffinityDispatcher(SessionAffinity.forThisNode()), null);
    }

    Loopback(EjbDispatcher ejb, NamingDispatcher naming, AffinityDispatcher affinity) {
        this(ejb, naming, affinity, null);
    }

    Loopback(EjbDispatcher ejb, NamingDispatcher naming, TransactionDispatcher transactions) {
        this(ejb, naming, new AffinityDispatcher(SessionAffinity.forThisNode()), transactions);
    }

    Loopback(EjbDispatcher ejb,
             NamingDispatcher naming,
             AffinityDispatcher affinity,
             TransactionDispatcher transactions) {
        this.ejb = ejb;
        this.naming = naming;
        this.affinity = affinity;
        this.transactions = transactions;
    }

    @Override
    public Response exchange(Request request) throws IOException {
        String rawPath = request.uri().getRawPath();
        byte[] pathBytes = rawPath.getBytes(StandardCharsets.UTF_8);

        Map<String, String> requestHeaders = new HashMap<>(request.headers());
        if (request.contentType() != null) {
            requestHeaders.put("Content-Type", request.contentType());
        }

        InMemoryExchange exchange = new InMemoryExchange(
                request.method(), pathBytes, request.uri().getRawQuery(),
                requestHeaders, concat(request.body()));

        if (rawPath.contains("/naming/")) {
            naming.dispatch(exchange);
        } else if (rawPath.contains("/common/")) {
            affinity.dispatch(exchange);
        } else if (rawPath.contains("/txn/")) {
            transactions.dispatch(exchange);
        } else {
            ejb.dispatch(exchange);
        }
        return exchange.toResponse();
    }

    @Override
    public CompletableFuture<Response> exchangeAsync(Request request) {
        try {
            return CompletableFuture.completedFuture(exchange(request));
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public String negotiatedVersion() {
        return "loopback";
    }

    @Override
    public void close() {
    }

    private static byte[] concat(ByteBuffer[] buffers) {
        int total = 0;
        for (ByteBuffer b : buffers) {
            total += b.remaining();
        }
        byte[] out = new byte[total];
        int pos = 0;
        for (ByteBuffer b : buffers) {
            ByteBuffer copy = b.duplicate();
            int n = copy.remaining();
            copy.get(out, pos, n);
            pos += n;
        }
        return out;
    }

    /** A {@link ServerExchange} backed by arrays. */
    static final class InMemoryExchange implements ServerExchange {

        /** Pass as the user name to make authentication fail. */
        static final String REJECT = "reject-me";

        private final String method;
        private final byte[] path;
        private final Map<String, String> query = new HashMap<>();
        private final Map<String, String> requestHeaders;
        private final byte[] requestBody;

        private int status = 200;
        private final Map<String, List<String>> responseHeaders = new HashMap<>();
        private final List<ByteBuffer> responseBody = new ArrayList<>();
        private String user;

        InMemoryExchange(String method, byte[] path, String rawQuery,
                         Map<String, String> requestHeaders, byte[] requestBody) {
            this.method = method;
            this.path = path;
            this.requestHeaders = requestHeaders;
            this.requestBody = requestBody;
            if (rawQuery != null) {
                for (String pair : rawQuery.split("&")) {
                    int eq = pair.indexOf('=');
                    if (eq > 0) {
                        query.put(pair.substring(0, eq),
                                java.net.URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8));
                    }
                }
            }
        }

        void setAuthenticatedUser(String user) {
            this.user = user;
        }

        @Override
        public String method() {
            return method;
        }

        @Override
        public byte[] pathBytes() {
            return path;
        }

        @Override
        public int pathOffset() {
            return 0;
        }

        @Override
        public int pathLength() {
            return path.length;
        }

        @Override
        public String requestHeader(String name) {
            return requestHeaders.get(name);
        }

        @Override
        public String queryParameter(String name) {
            return query.get(name);
        }

        @Override
        public InputStream requestBody() {
            return new ByteArrayInputStream(requestBody);
        }

        @Override
        public void setStatus(int status) {
            this.status = status;
        }

        @Override
        public void setResponseHeader(String name, String value) {
            responseHeaders.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
        }

        @Override
        public void writeBody(ByteBuffer[] body) {
            for (ByteBuffer b : body) {
                responseBody.add(b.duplicate());
            }
        }

        @Override
        public String authenticatedUser() {
            if (REJECT.equals(user)) {
                // Stands in for a realm refusing a credential that was offered.
                throw new SecurityException("authentication failed");
            }
            return user;
        }

        Response toResponse() {
            List<String> contentType = responseHeaders.get("Content-Type");
            byte[] body = concat(responseBody.toArray(new ByteBuffer[0]));
            return new Response(status,
                    contentType == null || contentType.isEmpty() ? null : contentType.get(0),
                    responseHeaders,
                    new ByteArrayInputStream(body));
        }
    }
}
