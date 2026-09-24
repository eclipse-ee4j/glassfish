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

package org.glassfish.orb.http.glassfish;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.memory.Buffers;
import org.glassfish.orb.http.server.ServerExchange;

/**
 * One Grizzly request as a {@link ServerExchange}.
 *
 * <p>The dispatchers were written against an interface rather than against
 * Grizzly precisely so that this class could be small and so that the protocol
 * could be tested without a server. It is small.
 */
final class GrizzlyServerExchange implements ServerExchange {

    private final Request request;
    private final Response response;
    private final byte[] path;

    GrizzlyServerExchange(Request request, Response response) {
        this.request = request;
        this.response = response;
        // The undecoded request URI. PathScanner is built to work over the
        // bytes as they arrived, and Grizzly does hold them in a DataChunk;
        // taking the String and re-encoding it gives up that property for one
        // allocation per request. Worth revisiting once there is a profile
        // that says it matters - not before.
        this.path = request.getRequestURI().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String method() {
        return request.getMethod().getMethodString();
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
        return request.getHeader(name);
    }

    @Override
    public String queryParameter(String name) {
        return request.getParameter(name);
    }

    @Override
    public InputStream requestBody() {
        return request.getInputStream();
    }

    @Override
    public void setStatus(int status) {
        response.setStatus(status);
    }

    @Override
    public void setResponseHeader(String name, String value) {
        if ("Set-Cookie".equalsIgnoreCase(name)) {
            response.addHeader(name, value);
        } else {
            response.setHeader(name, value);
        }
    }

    /** Set on the first setResponseTrailer; null means the response has none. */
    private Map<String, String> trailers;

    @Override
    public boolean supportsResponseTrailers() {
        return true;
    }

    @Override
    public void setResponseTrailer(String name, String value) {
        if (response.isCommitted()) {
            throw new IllegalStateException("the response is already committed: " + name);
        }
        if (trailers == null) {
            trailers = new LinkedHashMap<>(4);
        }
        trailers.put(name, value);
    }

    @Override
    public void writeBody(ByteBuffer[] body) throws IOException {
        long total = 0;
        for (ByteBuffer buffer : body) {
            total += buffer.remaining();
        }
        if (trailers == null) {
            response.setContentLengthLong(total);
        } else {
            // Trailers are only sent after a chunked body, so this response
            // must not announce a length. Grizzly asks for them when it
            // finishes the response, which is why they are handed over as a
            // supplier before the first byte goes out.
            Map<String, String> sent = trailers;
            response.setTrailers(() -> sent);
        }

        for (ByteBuffer buffer : body) {
            ByteBuffer writable = buffer;
            if (!buffer.hasArray()) {
                // ChunkedOutput supplies array-backed views, so the normal
                // path below is zero-copy. Keep a compatibility fallback for
                // callers that provide direct or read-only buffers.
                writable = ByteBuffer.allocate(buffer.remaining());
                writable.put(buffer.duplicate()).flip();
            }
            response.getNIOOutputStream().write(
                    Buffers.wrap(response.getRequest().getContext().getMemoryManager(), writable));
        }
        response.getNIOOutputStream().flush();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The credential is checked against the server's realm before the
     * identity is believed - reading a name out of the header and passing it
     * on would let any caller assert any identity.
     *
     * <p>A request with no credential is anonymous, which is what an unsecured
     * bean expects. A request whose credential fails is refused rather than
     * downgraded to anonymous: proceeding with fewer rights than were asked
     * for is still an authorization decision taken on a credential nobody
     * accepted.
     *
     * @throws SecurityException if a credential is present and does not pass
     */
    public String authenticatedUser() {
        return RealmAuthenticator.authenticate(requestHeader("Authorization"));
    }
}
