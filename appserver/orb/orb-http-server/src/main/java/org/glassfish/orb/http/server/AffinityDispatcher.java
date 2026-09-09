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

import org.glassfish.orb.http.protocol.PathScanner;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.ProtocolException;

/**
 * Serves {@code GET {ctx}/common/v1/affinity}.
 *
 * <p>A client calls it once, before it has anything to be sticky about, so
 * that the cookie exists before the first stateful conversation starts. That
 * ordering matters in a cluster: without it the {@code open} that creates a
 * session and the invocations that follow it are separate routing decisions,
 * and a load balancer is free to send them to different nodes - at which point
 * the session id names an instance that only one of them has.
 */
public final class AffinityDispatcher {

    private final SessionAffinity affinity;

    public AffinityDispatcher(SessionAffinity affinity) {
        this.affinity = affinity;
    }

    public void dispatch(ServerExchange exchange) throws IOException {
        PathScanner path;
        try {
            path = PathScanner.scan(exchange.pathBytes(), exchange.pathOffset(), exchange.pathLength());
        } catch (ProtocolException e) {
            exchange.setStatus(Protocol.SC_BAD_REQUEST);
            return;
        }

        if (!path.segmentEquals(1, Protocol.SVC_COMMON)
                || !path.segmentEquals(2, Protocol.VERSION_SEGMENT)
                || !path.segmentEquals(3, Protocol.OP_AFFINITY)) {
            exchange.setStatus(Protocol.SC_NOT_FOUND);
            return;
        }
        if (!"GET".equals(exchange.method())) {
            exchange.setStatus(Protocol.SC_BAD_REQUEST);
            exchange.setResponseHeader("X-GF-Reason", "affinity must be a GET");
            return;
        }

        affinity.applyTo(exchange, contextPathOf(path));
        exchange.setStatus(Protocol.SC_NO_CONTENT);
    }

    /** The context path is the single segment the routes are mounted under. */
    static String contextPathOf(PathScanner path) throws ProtocolException {
        return path.count() > 0 ? "/" + path.segment(0) : Protocol.CONTEXT_PATH;
    }
}
