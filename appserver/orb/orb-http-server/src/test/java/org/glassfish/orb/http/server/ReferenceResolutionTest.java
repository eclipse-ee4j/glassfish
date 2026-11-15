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

import java.net.URI;
import java.util.Hashtable;

import org.glassfish.orb.http.client.ClientConfiguration;
import org.glassfish.orb.http.client.HttpNamingContext;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.RemoteEjbReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ReferenceResolutionTest {

    @Test
    @DisplayName("a reference whose view is not an interface is handed back, not thrown")
    void aReferenceToANonInterfaceIsHandedBackAsIs() throws Exception {
        HttpRoundTripTest.FakeNaming naming = new HttpRoundTripTest.FakeNaming();
        // String is on every class path and is not an interface, so it cannot be proxied.
        naming.bindings.put("java:global/x", new RemoteEjbReference("myapp", "mymodule", null,
                "GreeterBean", String.class.getName(), null));
        Loopback loopback = new Loopback(new EjbDispatcher(new FakeContainer()), new NamingDispatcher(naming));
        ClientConfiguration config = ClientConfiguration
                .builder(URI.create("http://localhost:8080" + Protocol.CONTEXT_PATH))
                .build();

        HttpNamingContext context = new HttpNamingContext(config, loopback, new Hashtable<>());

        assertInstanceOf(RemoteEjbReference.class, context.lookup("java:global/x"));
    }
}
