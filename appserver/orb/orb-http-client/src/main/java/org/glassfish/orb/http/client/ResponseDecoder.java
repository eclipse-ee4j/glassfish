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





import jakarta.ejb.EJBAccessException;
import jakarta.ejb.EJBException;
import jakarta.ejb.NoSuchEJBException;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.util.concurrent.CancellationException;

import org.glassfish.orb.http.protocol.ContentType;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Marshaller;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.ProtocolException;

/**
 * Turns an HTTP response back into a result or a thrown exception.
 *
 * <h2>Exception fidelity</h2>
 * The application's own exception is marshalled into the 500 body and rethrown
 * here with its type, its message, its cause chain and its server-side stack
 * trace intact. This is a property the IIOP path also has, and one that a
 * JSON-based transport structurally cannot: an {@code @ApplicationException}
 * carrying business state has to arrive as itself, not as a string.
 */
final class ResponseDecoder {

    private final Marshaller marshaller;
    private final ObjectInputFilter filter;

    ResponseDecoder(Marshaller marshaller, ObjectInputFilter filter) {
        this.marshaller = marshaller;
        this.filter = filter;
    }

    ResponseDecoder() {
        this(new JavaSerializationMarshaller(), JavaSerializationMarshaller.defaultFilter());
    }

    /**
     * @param response the exchange result; always consumed and closed
     * @param loader   class loader used to resolve incoming classes
     * @return the invocation result, or {@code null} for a void or async call
     * @throws Throwable the application exception, if the server threw one
     */
    Object decodeInvocationResult(HttpTransport.Response response, ClassLoader loader) throws Throwable {
        try (response) {
            switch (response.status()) {
                case Protocol.SC_OK -> {
                    return readValue(response, loader, ContentType.KIND_RESPONSE);
                }
                case Protocol.SC_ACCEPTED, Protocol.SC_NO_CONTENT -> {
                    // Asynchronous invocation acknowledged; there is no result yet.
                    return null;
                }
                case Protocol.SC_EXCEPTION -> throw readThrowable(response, loader);
                case Protocol.SC_NOT_FOUND -> throw new NoSuchEJBException(reason(response, "no such bean or session"));
                case Protocol.SC_FORBIDDEN ->
                        throw new EJBAccessException(reason(response, "not authorised to invoke this method"));
                case Protocol.SC_CANCELLED ->
                        throw new CancellationException(reason(response, "invocation was cancelled"));
                case Protocol.SC_NOT_ACCEPTABLE ->
                        throw new ProtocolException(reason(response, "server rejected protocol version "
                                + Protocol.VERSION + " or the requested encoding"));
                case Protocol.SC_BAD_REQUEST ->
                        throw new ProtocolException(reason(response, "server rejected the request as malformed"));
                default -> throw new EJBException(reason(response, "unexpected HTTP status " + response.status()));
            }
        }
    }

    private Object readValue(HttpTransport.Response response, ClassLoader loader, String expectedKind)
            throws IOException, ClassNotFoundException {
        ContentType contentType = ContentType.parse(response.contentType());
        if (!contentType.isVersionSupported()) {
            throw new ProtocolException("server replied with protocol version " + contentType.version());
        }
        if (!expectedKind.equals(contentType.kind())) {
            throw new ProtocolException("expected a " + expectedKind + " body, got " + contentType.kind());
        }
        InputStream body = response.body();
        try (Marshaller.ObjectReader reader = marshaller.newReader(body, loader, filter)) {
            return reader.readObject();
        }
    }

    /**
     * Prefers what the server said to what we would have guessed.
     *
     * <p>A protocol-level refusal carries its reason in a header rather than a
     * marshalled body, because an intermediary has to be able to read it too.
     * Discarding it and reporting the generic case turned a server that had
     * explained itself into one that had not - which cost a round trip through
     * CI to diagnose something the response had already answered.
     *
     * @param response the failing exchange
     * @param fallback what to say if the server said nothing
     * @return the message to report
     */
    static String reason(HttpTransport.Response response, String fallback) {
        String reason = response.firstHeader("X-GF-Reason");
        return reason == null || reason.isBlank() ? fallback : fallback + ": " + reason;
    }

    private Throwable readThrowable(HttpTransport.Response response, ClassLoader loader) {
        try {
            Object o = readValue(response, loader, ContentType.KIND_EXCEPTION);
            if (o instanceof Throwable t) {
                return t;
            }
            return new EJBException("server signalled a failure but sent a "
                    + (o == null ? "null" : o.getClass().getName()) + " instead of a Throwable");
        } catch (IOException | ClassNotFoundException e) {
            // The server failed and the body is not a Throwable we can read.
            // It may still have said why in a header, and saying "could not be
            // decoded" while discarding an explanation is the worst of both.
            return new EJBException(reason(response,
                    "server signalled a failure whose detail could not be decoded"), e);
        }
    }
}
