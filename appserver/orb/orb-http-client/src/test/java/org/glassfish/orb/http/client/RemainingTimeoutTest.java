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

package org.glassfish.orb.http.client;

import java.net.URI;
import java.util.Map;

import javax.transaction.xa.Xid;

import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.Xids;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What travels in {@code x-gf-txn-timeout} is the time the transaction has
 * left, not the time it was given.
 * <p>
 * A server that imports a branch under the time the transaction started with
 * lets the branch outlive a transaction that is nearly over, which turns a
 * clean rollback into a heuristic outcome someone has to resolve by hand.
 */
class RemainingTimeoutTest {

    public interface Pinger {
        void ping();
    }

    private static final Xid XID = new Xids.SimpleXid(1, new byte[] { 7 }, new byte[] { 7 });

    @AfterEach
    void tearDown() {
        ClientTransactionContext.disassociate();
    }

    @Test
    @DisplayName("the timeout sent shrinks as the transaction ages")
    void theRemainingTimeIsSent() throws Exception {
        RecordingTransport transport = new RecordingTransport();
        HttpEjbClient client = new HttpEjbClient(
                ClientConfiguration.builder(URI.create("http://localhost:8080/glassfish-services")).build(),
                transport, new JavaSerializationMarshaller(), JavaSerializationMarshaller.defaultFilter());
        Pinger pinger = client.createProxy(Pinger.class, new EjbLocator("app", "module", null, "PingerBean"));

        ClientTransactionContext.associate(XID, 100, true);
        pinger.ping();
        Thread.sleep(1500);
        pinger.ping();

        long first = Long.parseLong(timeoutHeader(transport.requestHeaders.get(0)));
        long second = Long.parseLong(timeoutHeader(transport.requestHeaders.get(1)));
        assertTrue(first <= 100 && first >= 99, "first call sent " + first);
        assertTrue(second <= 99 && second >= 97, "second call sent " + second + ", not what was left");
    }

    private static String timeoutHeader(Map<String, String> headers) {
        return headers.get(Protocol.H_TXN_TIMEOUT);
    }

    @Test
    @DisplayName("an association with no timeout reports none, and one past its deadline reports 1, never 0")
    void noTimeoutAndExpiredTimeout() throws Exception {
        ClientTransactionContext.associate(XID, 0, true);
        assertEquals(0, ClientTransactionContext.currentTimeoutSeconds());

        ClientTransactionContext.associate(XID, 2, true);
        Thread.sleep(2300);
        // Zero would tell the server there is no timeout and let it apply its
        // own, longer one to a transaction that has already run out.
        assertEquals(1, ClientTransactionContext.currentTimeoutSeconds());
    }

    @Test
    @DisplayName("putting a branch back on a thread does not restart its clock")
    void restoringABranchKeepsItsDeadline() throws Exception {
        ClientTransactionContext.associate(XID, 100, true);
        long deadline = ClientTransactionContext.currentDeadlineNanos();
        Thread.sleep(1200);

        ClientTransactionContext.disassociate();
        ClientTransactionContext.associate(XID, 100, true, deadline);

        assertEquals(deadline, ClientTransactionContext.currentDeadlineNanos());
        assertTrue(ClientTransactionContext.currentTimeoutSeconds() <= 99);
    }
}
