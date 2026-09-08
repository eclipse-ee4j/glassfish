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

import java.security.SecureRandom;
import java.util.Base64;
import org.glassfish.orb.http.protocol.Protocol;

/**
 * Pins a stateful conversation to the node that owns it.
 *
 * <p>A stateful session bean lives in one instance's memory. Over IIOP the
 * client is handed a reference that names that instance directly, and
 * GlassFish keeps a whole failover apparatus around it -
 * {@code RoundRobinPolicy}, {@code IiopFolbGmsClient},
 * {@code NamingClusterInfoImpl}, some 1,200 lines that also drag in GMS.
 *
 * <p>Over HTTP the same job is done by a cookie. The value identifies this
 * node; an ordinary load balancer routes on it without knowing anything about
 * this protocol, and the client's cookie handler returns it without knowing
 * anything either. That is the whole mechanism, and it is why none of the
 * IIOP failover machinery has an equivalent here.
 *
 * <p>The cookie is named {@code JSESSIONID} deliberately: every load balancer
 * in existence already has a sticky-session mode keyed on that name, and none
 * of them would have one for a name we invented.
 */
public final class SessionAffinity {

    private final String routeId;

    /**
     * @param routeId a value identifying this node, stable for its lifetime.
     *                In a cluster this should be the instance name, so that a
     *                load balancer configured against the topology routes to
     *                the node an operator expects.
     */
    public SessionAffinity(String routeId) {
        if (routeId == null || routeId.isEmpty()) {
            throw new IllegalArgumentException("routeId is required");
        }
        this.routeId = routeId;
    }

    /**
     * @return an affinity whose route id is random but fixed for the life of
     *         this JVM - correct for a single instance, and the right default
     *         when nothing has told us the instance name
     */
    public static SessionAffinity forThisNode() {
        byte[] random = new byte[12];
        new SecureRandom().nextBytes(random);
        return new SessionAffinity(Base64.getUrlEncoder().withoutPadding().encodeToString(random));
    }

    public String routeId() {
        return routeId;
    }

    /**
     * @param contextPath the path the endpoint is mounted at
     * @return the {@code Set-Cookie} value pinning this conversation
     */
    public String cookieHeader(String contextPath) {
        // Scoped to the endpoint's own path so it cannot collide with the
        // session cookie of an application deployed on the same host, and
        // HttpOnly because no script has any business reading it.
        return Protocol.AFFINITY_COOKIE + '=' + routeId
                + "; Path=" + (contextPath == null || contextPath.isEmpty() ? "/" : contextPath)
                + "; HttpOnly";
    }

    /** Sets the affinity cookie on a response. */
    public void applyTo(ServerExchange exchange, String contextPath) {
        exchange.setResponseHeader("Set-Cookie", cookieHeader(contextPath));
    }
}
