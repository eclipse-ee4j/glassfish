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
import java.util.List;
import java.util.Map;

import org.glassfish.orb.http.client.ClientConfiguration;
import org.glassfish.orb.http.client.EjbLocator;
import org.glassfish.orb.http.client.HttpEjbClient;
import org.glassfish.orb.http.codec.fory.ForyMarshaller;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Protocol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A full round trip through a codec that frames its own objects.
 * <p>
 * Every other test here uses a codec that delegates to Java serialization,
 * which frames the stream itself. That cannot catch a mistake in where one
 * encoded object ends and the next begins - and that boundary is this
 * transport's responsibility, not the library's.
 */
class ForyRoundTripTest {

    private HttpEjbClient client;

    @BeforeEach
    void setUp() {
        FakeContainer container = new FakeContainer();
        container.registerStateless("GreeterBean", new TestBeans.GreeterBean());

        Loopback loopback = new Loopback(new EjbDispatcher(container),
                new NamingDispatcher(new HttpRoundTripTest.FakeNaming()));

        ClientConfiguration config = ClientConfiguration
                .builder(URI.create("http://localhost:8080" + Protocol.CONTEXT_PATH))
                .build();
        client = new HttpEjbClient(config, loopback, new ForyMarshaller(),
                JavaSerializationMarshaller.defaultFilter());
    }

    private TestBeans.Greeter greeter() {
        return client.createProxy(TestBeans.Greeter.class,
                new EjbLocator("myapp", "mymodule", null, "GreeterBean"));
    }

    @Test
    @DisplayName("arguments and a result survive a codec that frames its own objects")
    void aCallRoundTripsThroughFory() {
        assertEquals("Ada Ada", greeter().greet("Ada", 2));
    }

    @Test
    @DisplayName("a void method reads the attachments frame and nothing more")
    void aVoidCallRoundTrips() {
        // Nothing comes back but the attachments map. If the framing is off by
        // anything, this is where it shows first.
        greeter().notifyArrival("Ada");
    }

    @Test
    @DisplayName("a nested structure survives, so the frame is not merely the right length")
    void aNestedStructureRoundTrips() {
        // One key, whose list carries the twenty values.
        Map<String, List<Integer>> payload = greeter().cyclicFriendlyPayload(20);
        assertEquals(1, payload.size());
        assertEquals(20, payload.get("numbers").size());
        assertEquals(19, payload.get("numbers").get(19));
    }

    @Test
    @DisplayName("a large payload crosses frame boundaries intact")
    void aLargePayloadRoundTrips() {
        String payload = "x".repeat(200_000);
        assertEquals(payload, greeter().echoLarge(payload));
    }
}
