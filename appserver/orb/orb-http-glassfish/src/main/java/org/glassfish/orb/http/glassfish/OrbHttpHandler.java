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

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.orb.http.protocol.EjbRoutes;
import org.glassfish.orb.http.protocol.PathScanner;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.ProtocolException;
import org.glassfish.orb.http.server.AffinityDispatcher;
import org.glassfish.orb.http.server.EjbDispatcher;
import org.glassfish.orb.http.server.NamingDispatcher;
import org.glassfish.orb.http.server.TransactionDispatcher;

/**
 * The endpoint: one Grizzly handler in front of the three dispatchers.
 *
 * <p>Routing is by the service segment of the path, and it is deliberately the
 * only thing this class does. Everything a request means is decided by the
 * dispatchers, which know nothing about Grizzly and are tested without it.
 */
final class OrbHttpHandler extends HttpHandler {

    private static final Logger LOG = System.getLogger(OrbHttpHandler.class.getName());

    private final EjbDispatcher ejb;
    private final NamingDispatcher naming;
    private final TransactionDispatcher transactions;
    private final AffinityDispatcher affinity;

    OrbHttpHandler(EjbDispatcher ejb, NamingDispatcher naming,
                   TransactionDispatcher transactions, AffinityDispatcher affinity) {
        this.ejb = ejb;
        this.naming = naming;
        this.transactions = transactions;
        this.affinity = affinity;
    }

    @Override
    public void service(Request request, Response response) {
        GrizzlyServerExchange exchange = new GrizzlyServerExchange(request, response);
        String path = request.getRequestURI();
        try {
            switch (route(path)) {
                case NAMING -> naming.dispatch(exchange);
                case TXN -> transactions.dispatch(exchange);
                case COMMON -> affinity.dispatch(exchange);
                default -> ejb.dispatch(exchange);
            }
        } catch (Exception e) {
            // A failure that reached here is the transport's, not the
            // application's: an application failure is already a marshalled
            // 500 body by this point. Say so with a status and nothing else,
            // rather than leaking a stack trace to an unauthenticated caller.
            LOG.log(Level.WARNING, "dispatch failed for " + path, e);
            response.setStatus(Protocol.SC_EXCEPTION);
        }
    }

    /** Which dispatcher a request belongs to. */
    enum Route {
        EJB, NAMING, TXN, COMMON
    }

    /**
     * Chooses the dispatcher from the service segment of the path, and from
     * nothing else.
     *
     * <p>The segment is the one every route layout puts directly after the
     * context path. Looking for the service word anywhere in the path instead
     * sends an invocation to the wrong dispatcher whenever an application,
     * module, bean or method is called {@code naming}, {@code txn} or
     * {@code common}.
     *
     * @param uri the request URI, without the query
     * @return the route; anything that is not another service, including a path
     *         that cannot be scanned, is left to the EJB dispatcher to answer
     */
    static Route route(String uri) {
        PathScanner path;
        try {
            path = PathScanner.scan(uri);
        } catch (ProtocolException e) {
            return Route.EJB;
        }
        if (path.segmentEquals(EjbRoutes.IDX_SERVICE, Protocol.SVC_NAMING)) {
            return Route.NAMING;
        }
        if (path.segmentEquals(EjbRoutes.IDX_SERVICE, Protocol.SVC_TXN)) {
            return Route.TXN;
        }
        if (path.segmentEquals(EjbRoutes.IDX_SERVICE, Protocol.SVC_COMMON)) {
            return Route.COMMON;
        }
        return Route.EJB;
    }
}
