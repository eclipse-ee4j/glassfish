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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.glassfish.orb.http.protocol.ContentType;
import org.glassfish.orb.http.protocol.EjbRoutes;
import org.glassfish.orb.http.protocol.InvocationEnvelope;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Marshaller;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.TxContext;
import org.glassfish.orb.http.protocol.Xids;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A failure of the transaction bridge during an invocation is reported in
 * headers, and what goes into a header must stay on one line.
 */
class EjbTransactionFailureTest {

    @Test
    @DisplayName("a line break in a failure reason cannot start a second header")
    void reasonCarriesNoLineBreak() throws IOException {
        FakeContainer container = new FakeContainer();
        container.registerStateless("GreeterBean", new TestBeans.GreeterBean());
        FakeTransactions transactions = new FakeTransactions();
        transactions.failWith = XAException.XAER_RMERR;
        transactions.failReason = "first\r\nX-Injected: yes\nsecond\rthird";
        EjbDispatcher dispatcher = new EjbDispatcher(container, SecurityBridge.NONE, transactions,
                new JavaSerializationMarshaller(), new InvocationRegistry(), SessionAffinity.forThisNode());

        String path = EjbRoutes.invokePath(Protocol.CONTEXT_PATH, "myapp", "mymodule", null,
                "GreeterBean", null, TestBeans.Greeter.class.getName(), "greet",
                new String[] { String.class.getName(), int.class.getName() });
        Xid xid = new Xids.SimpleXid(0x4F524248, new byte[] { 1, 2, 3 }, new byte[] { 1 });

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        InvocationEnvelope.writeTxContext(body, Xids.toContext(xid, TxContext.TYPE_REMOTE));
        try (Marshaller.ObjectWriter writer = new JavaSerializationMarshaller().newWriter(body)) {
            writer.writeObject("Ada");
            writer.writeObject(1);
            writer.writeObject(Map.of());
        }

        Loopback.InMemoryExchange exchange = new Loopback.InMemoryExchange("POST",
                path.getBytes(StandardCharsets.UTF_8), null,
                Map.of("Content-Type", ContentType.of(JavaSerializationMarshaller.CODEC,
                        ContentType.KIND_INVOCATION).toHeaderValue()),
                body.toByteArray());

        dispatcher.dispatch(exchange);

        assertEquals(Protocol.SC_EXCEPTION, exchange.toResponse().status());
        assertEquals(List.of("first  X-Injected: yes second third"),
                exchange.toResponse().headers().get("X-GF-Reason"));
    }
}
