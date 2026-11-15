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

import org.glassfish.orb.http.glassfish.OrbHttpHandler.Route;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Which dispatcher a path belongs to. Only the service segment decides.
 */
class OrbHttpHandlerTest {

    // Characterization test: it passes without the change under test and pins
    // behaviour that is meant to stay.
    @Test
    @DisplayName("each service is routed by the segment after the context path")
    void eachServiceIsRouted() {
        assertEquals(Route.EJB, OrbHttpHandler.route("/glassfish-services/ejb/v1/invoke/app/mod/-/Bean/-/a.B/m/"));
        assertEquals(Route.EJB, OrbHttpHandler.route("/glassfish-services/ejb/v1/open/app/mod/-/Bean"));
        assertEquals(Route.NAMING, OrbHttpHandler.route("/glassfish-services/naming/v1/lookup/java:global/app/Bean"));
        assertEquals(Route.TXN, OrbHttpHandler.route("/glassfish-services/txn/v1/xa/commit/abc"));
        assertEquals(Route.COMMON, OrbHttpHandler.route("/glassfish-services/common/v1/affinity"));
    }

    @Test
    @DisplayName("an app, module, bean or method named like a service does not change the route")
    void aSegmentNamedLikeAServiceDoesNotReroute() {
        assertEquals(Route.EJB,
                OrbHttpHandler.route("/glassfish-services/ejb/v1/invoke/common/naming/-/txn/-/a.B/m/"));
        assertEquals(Route.EJB, OrbHttpHandler.route("/glassfish-services/ejb/v1/invoke/naming/m/-/Bean/-/a.B/m/"));
        assertEquals(Route.EJB, OrbHttpHandler.route("/glassfish-services/ejb/v1/open/app/txn/-/common"));
        assertEquals(Route.NAMING, OrbHttpHandler.route("/glassfish-services/naming/v1/lookup/txn/common/ejb"));
    }

    // Characterization test: it passes without the change under test and pins
    // behaviour that is meant to stay.
    @Test
    @DisplayName("a path that is not any service, or cannot be scanned, is left to the EJB dispatcher to refuse")
    void everythingElseGoesToTheEjbDispatcher() {
        assertEquals(Route.EJB, OrbHttpHandler.route("/glassfish-services"));
        assertEquals(Route.EJB, OrbHttpHandler.route("/glassfish-services/unknown/v1/x"));
        assertEquals(Route.EJB, OrbHttpHandler.route("/" + "a/".repeat(40)));
    }
}
