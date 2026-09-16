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

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.glassfish.orb.http.protocol.ContentType;
import org.glassfish.orb.http.protocol.EjbRoutes;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Protocol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A credential that fails is refused, not quietly downgraded to anonymous.
 */
class RejectedCredentialTest {

    @Test
    @DisplayName("a credential the realm refuses stops the call rather than anonymising it")
    void aRefusedCredentialIsForbidden() throws Exception {
        FakeContainer container = new FakeContainer();
        container.registerStateless("GreeterBean", new TestBeans.GreeterBean());
        EjbDispatcher dispatcher = new EjbDispatcher(container);

        String path = EjbRoutes.invokePath(Protocol.CONTEXT_PATH, "myapp", "mymodule", null,
                "GreeterBean", null, TestBeans.Greeter.class.getName(), "greet",
                new String[] { String.class.getName(), int.class.getName() });

        Loopback.InMemoryExchange exchange = new Loopback.InMemoryExchange("POST",
                path.getBytes(StandardCharsets.UTF_8), null,
                Map.of("Content-Type", ContentType.of(JavaSerializationMarshaller.CODEC,
                        ContentType.KIND_INVOCATION).toHeaderValue()),
                new byte[0]);
        exchange.setAuthenticatedUser(Loopback.InMemoryExchange.REJECT);

        dispatcher.dispatch(exchange);

        // Not a 200 with the bean having run as nobody: the caller asked to be
        // someone and was not, and that is an answer, not a downgrade.
        assertEquals(Protocol.SC_FORBIDDEN, exchange.toResponse().status());
        assertNotNull(exchange.toResponse().headers().get("X-GF-Reason"));
    }
}
