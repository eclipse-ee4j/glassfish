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
import org.glassfish.orb.http.protocol.Protocol;
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
            if (path.contains('/' + Protocol.SVC_NAMING + '/')) {
                naming.dispatch(exchange);
            } else if (path.contains('/' + Protocol.SVC_TXN + '/')) {
                transactions.dispatch(exchange);
            } else if (path.contains('/' + Protocol.SVC_COMMON + '/')) {
                affinity.dispatch(exchange);
            } else {
                ejb.dispatch(exchange);
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
}
