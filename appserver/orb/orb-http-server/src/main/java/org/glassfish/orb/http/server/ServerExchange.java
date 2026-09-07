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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * The minimum a container must supply for the dispatchers to work.
 * <p>
 * Deliberately not {@code HttpServletRequest}/{@code HttpServletResponse} and
 * not a Grizzly {@code Request}/{@code Response}: keeping the dispatch logic
 * behind this interface is what lets it be driven by a servlet, by a Grizzly
 * {@code HttpHandler}, or by a plain in-memory fake in a unit test that never
 * opens a socket. It also keeps this module free of any GlassFish dependency,
 * so it compiles and is testable on its own.
 * <p>
 * Note {@link #pathBytes()} rather than a {@code String} path. The raw bytes
 * are what arrived; handing them straight to
 * {@code org.glassfish.orb.http.protocol.PathScanner} avoids decoding and
 * allocating a String for a path whose segments are mostly compared against
 * constants.
 */
public interface ServerExchange {

    String method();

    /** The raw request path, undecoded. */
    byte[] pathBytes();

    int pathOffset();

    int pathLength();

    /** @return the first value of a request header, or {@code null} */
    String requestHeader(String name);

    /** @return the value of a query parameter, or {@code null} */
    String queryParameter(String name);

    InputStream requestBody() throws IOException;

    void setStatus(int status);

    void setResponseHeader(String name, String value);

    /**
     * Writes the response body from buffers that were marshalled elsewhere.
     * An implementation is expected to hand these to the transport without
     * copying - Grizzly can wrap a {@code byte[]} in a {@code HeapBuffer} and
     * chain the result, so the marshalled reply never needs to be flattened
     * into a single array.
     */
    void writeBody(ByteBuffer[] body) throws IOException;

    /**
     * @return the authenticated caller's name, or {@code null} if the request
     *         is unauthenticated. Supplied by the container's own HTTP
     *         authentication - Basic, client certificate, or a bearer token -
     *         rather than re-implemented here.
     */
    String authenticatedUser();
}
