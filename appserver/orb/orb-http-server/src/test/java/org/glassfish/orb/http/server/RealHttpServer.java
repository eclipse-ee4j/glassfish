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





import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.glassfish.orb.http.protocol.Protocol;

/**
 * The dispatchers behind a real socket, using the JDK's own
 * {@code com.sun.net.httpserver}.
 *
 * <p>{@link Loopback} proves the protocol; this proves everything underneath
 * it. Over a real connection the client is the actual
 * {@code java.net.http.HttpClient}, the cookies are actually stored and
 * returned by its {@code CookieManager}, the status codes and content types
 * actually cross a wire, and a request that a hand-written fake would have
 * accepted can be rejected by a real HTTP implementation.
 *
 * <p>It speaks HTTP/1.1 only - the JDK ships no HTTP/2 server - so it proves
 * the protocol is correct, not that HTTP/2 negotiation works. That the
 * protocol carries no trailers, no server push and no ordering assumptions is
 * what makes running it over either a deployment choice.
 */
final class RealHttpServer implements AutoCloseable {

    private final HttpServer server;
    private final ExecutorService executor;
    private final URI baseUri;

    RealHttpServer(EjbDispatcher ejb, NamingDispatcher naming, AffinityDispatcher affinity) throws IOException {
        this(ejb, naming, affinity, null);
    }

    RealHttpServer(EjbDispatcher ejb,
                   NamingDispatcher naming,
                   AffinityDispatcher affinity,
                   TransactionDispatcher transactions) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        // Held so it can be shut down. HttpServer.stop does not touch an
        // executor it was given, so leaving this unreferenced leaked eight
        // threads per instance for the life of the JVM - and one of these is
        // built per test method.
        this.executor = Executors.newFixedThreadPool(8);
        this.server.setExecutor(executor);
        this.server.createContext(Protocol.CONTEXT_PATH, http -> {
            Adapter exchange = new Adapter(http);
            try {
                String path = http.getRequestURI().getRawPath();
                if (path.contains("/naming/")) {
                    naming.dispatch(exchange);
                } else if (path.contains("/common/")) {
                    affinity.dispatch(exchange);
                } else if (path.contains("/txn/")) {
                    transactions.dispatch(exchange);
                } else {
                    ejb.dispatch(exchange);
                }
            } catch (RuntimeException | IOException e) {
                exchange.setStatus(500);
            } finally {
                exchange.flush();
                http.close();
            }
        });
        this.server.start();
        this.baseUri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + Protocol.CONTEXT_PATH);
    }

    URI baseUri() {
        return baseUri;
    }

    @Override
    public void close() {
        // A second of grace so an exchange still being written finishes,
        // rather than being cut off and surfacing as a failure in whichever
        // test happens to run next.
        server.stop(1);
        executor.shutdownNow();
    }

    /** Adapts one {@link HttpExchange} to {@link ServerExchange}. */
    private static final class Adapter implements ServerExchange {

        private final HttpExchange http;
        private final byte[] path;
        private final Map<String, String> query = new HashMap<>();
        private final Map<String, String> headers = new LinkedHashMap<>();
        private final ByteArrayOutputStream body = new ByteArrayOutputStream();
        private int status = Protocol.SC_OK;

        Adapter(HttpExchange http) {
            this.http = http;
            this.path = http.getRequestURI().getRawPath().getBytes(StandardCharsets.UTF_8);
            String raw = http.getRequestURI().getRawQuery();
            if (raw != null) {
                for (String pair : raw.split("&")) {
                    int eq = pair.indexOf('=');
                    if (eq > 0) {
                        query.put(pair.substring(0, eq),
                                java.net.URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8));
                    }
                }
            }
        }

        @Override
        public String method() {
            return http.getRequestMethod();
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
            return http.getRequestHeaders().getFirst(name);
        }

        @Override
        public String queryParameter(String name) {
            return query.get(name);
        }

        @Override
        public InputStream requestBody() {
            return http.getRequestBody();
        }

        @Override
        public void setStatus(int status) {
            this.status = status;
        }

        @Override
        public void setResponseHeader(String name, String value) {
            if ("Set-Cookie".equalsIgnoreCase(name)) {
                http.getResponseHeaders().add(name, value);
            } else {
                headers.put(name, value);
            }
        }

        @Override
        public void writeBody(ByteBuffer[] buffers) throws IOException {
            for (ByteBuffer buffer : buffers) {
                ByteBuffer copy = buffer.duplicate();
                byte[] chunk = new byte[copy.remaining()];
                copy.get(chunk);
                body.write(chunk);
            }
        }

        @Override
        public String authenticatedUser() {
            String authorization = http.getRequestHeaders().getFirst("Authorization");
            if (authorization == null || !authorization.startsWith("Basic ")) {
                return null;
            }
            String decoded = new String(Base64.getDecoder().decode(authorization.substring(6)),
                    StandardCharsets.UTF_8);
            int colon = decoded.indexOf(':');
            return colon < 0 ? decoded : decoded.substring(0, colon);
        }

        void flush() {
            try {
                headers.forEach((name, value) -> http.getResponseHeaders().set(name, value));
                byte[] payload = body.toByteArray();
                // 204 must carry no body, and the JDK server enforces that.
                if (payload.length == 0) {
                    http.sendResponseHeaders(status, -1);
                } else {
                    http.sendResponseHeaders(status, payload.length);
                    try (OutputStream out = http.getResponseBody()) {
                        out.write(payload);
                    }
                }
            } catch (IOException e) {
                // The client hung up; nothing useful left to do.
            }
        }
    }
}
