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
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link HttpTransport} on {@code java.net.http.HttpClient}: HTTP/1.1 and
 * HTTP/2, no third-party dependency.
 * <p>
 * WildFly's equivalent is built on the Undertow client and XNIO, which brings
 * a connection pool, a thread pool and a buffer pool of its own. On Java 21
 * none of that is needed: the JDK client pools connections, negotiates HTTP/2
 * over ALPN (and over cleartext by {@code h2c} upgrade), and multiplexes
 * concurrent invocations onto one connection by itself. That is a meaningful
 * simplification for a jar meant to sit on a client's classpath.
 *
 * <h2>Session affinity</h2>
 * A {@link CookieManager} is installed so that the {@code JSESSIONID} the
 * server sets - at the affinity endpoint, and again when a stateful session is
 * opened - is returned automatically on every later request. That single
 * cookie is what pins a stateful conversation to one backend through an
 * ordinary HTTP load balancer, and it is what stands in for the IIOP failover
 * machinery ({@code RoundRobinPolicy}, {@code IiopFolbGmsClient},
 * {@code NamingClusterInfoImpl}).
 * <p>
 * Whether that actually routes anywhere is a deployment question this client
 * cannot answer: it needs a load balancer configured for sticky sessions on
 * that cookie. Against a single instance it is simply inert.
 */
public final class JdkHttpTransport implements HttpTransport {

    private final HttpClient client;
    private final ClientConfiguration config;
    private final String authorizationHeader;
    private final AtomicReference<String> lastVersion = new AtomicReference<>("unknown");

    public JdkHttpTransport(ClientConfiguration config) {
        this.config = config;
        this.authorizationHeader = buildAuthorization(config);

        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(config.preferHttp2() ? HttpClient.Version.HTTP_2 : HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NEVER)
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

        if (config.connectTimeout() != null) {
            builder.connectTimeout(config.connectTimeout());
        }
        if (config.sslContext() != null) {
            builder.sslContext(config.sslContext());
        }
        this.client = builder.build();
    }

    private static String buildAuthorization(ClientConfiguration config) {
        if (config.bearerToken() != null) {
            return "Bearer " + config.bearerToken();
        }
        if (config.username() != null) {
            char[] password = config.password();
            String raw = config.username() + ':' + (password == null ? "" : new String(password));
            return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        }
        return null;
    }

    private HttpRequest toHttpRequest(Request request) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(request.uri())
                .method(request.method(), ByteBuffersBodyPublisher.of(request.body()));

        if (request.contentType() != null && request.body().length > 0) {
            builder.header("Content-Type", request.contentType());
        }
        if (request.accept() != null) {
            builder.header("Accept", request.accept());
        }
        if (authorizationHeader != null) {
            builder.header("Authorization", authorizationHeader);
        }
        for (Map.Entry<String, String> h : request.headers().entrySet()) {
            builder.header(h.getKey(), h.getValue());
        }
        if (config.requestTimeout() != null) {
            builder.timeout(config.requestTimeout());
        }
        return builder.build();
    }

    private Response toResponse(HttpResponse<java.io.InputStream> response) {
        lastVersion.set(switch (response.version()) {
            case HTTP_1_1 -> "HTTP/1.1";
            case HTTP_2 -> "HTTP/2";
        });
        return new Response(
                response.statusCode(),
                response.headers().firstValue("Content-Type").orElse(null),
                response.headers().map(),
                response.body());
    }

    @Override
    public Response exchange(Request request) throws IOException, InterruptedException {
        // ofInputStream, not ofByteArray: the marshalled result is handed to
        // the deserializer as the network buffers arrive, instead of being
        // accumulated into one array first.
        return toResponse(client.send(toHttpRequest(request), HttpResponse.BodyHandlers.ofInputStream()));
    }

    @Override
    public CompletableFuture<Response> exchangeAsync(Request request) {
        return client.sendAsync(toHttpRequest(request), HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(this::toResponse);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Reports what the last exchange actually negotiated, which is not
     * necessarily what was asked for: a proxy that speaks only HTTP/1.1 will
     * have downgraded it silently.
     */
    @Override
    public String negotiatedVersion() {
        return lastVersion.get();
    }

    @Override
    public void close() {
        client.close();
    }
}
