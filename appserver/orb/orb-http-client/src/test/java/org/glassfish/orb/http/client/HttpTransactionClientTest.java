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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.glassfish.orb.http.protocol.TxRoutes;
import org.glassfish.orb.http.protocol.Xids;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What the client does when the server's answer is not the expected one.
 */
class HttpTransactionClientTest {

    private static final Xid XID = new Xids.SimpleXid(131075,
            new byte[] { 1, 2, 3, 4 }, new byte[] { 9 });

    private RecordingTransport transport;

    private HttpTransactionClient client;

    @BeforeEach
    void setUp() {
        transport = new RecordingTransport();
        client = new HttpTransactionClient(
                ClientConfiguration.builder(URI.create("http://localhost:8080/glassfish-services")).build(),
                transport);
    }

    @AfterEach
    void tearDown() {
        ClientTransactionContext.disassociate();
    }

    @Test
    @DisplayName("a begin with no xid is a failure, not a transaction with no name")
    void aBeginWithoutAnXidFails() {
        transport.answerAll(204, Map.of());

        XAException thrown = assertThrows(XAException.class, () -> client.begin(0));
        // Returning null here would leave the caller believing it had started
        // a transaction that nothing can ever commit.
        assertEquals(XAException.XAER_RMERR, thrown.errorCode);
    }

    @Test
    @DisplayName("an xid the server sent but we cannot parse is refused")
    void aMalformedXidIsRefused() {
        transport.answerAll(204, Map.of(TxRoutes.H_XID, List.of("not-an-xid")));

        XAException thrown = assertThrows(XAException.class, () -> client.begin(0));
        assertEquals(XAException.XAER_PROTO, thrown.errorCode);
    }

    @Test
    @DisplayName("the server's XA error code is carried, not flattened to a generic one")
    void theServersErrorCodeIsUsed() {
        // A resource manager distinguishes "rolled back" from "protocol error"
        // and a coordinator acts on the difference; collapsing them would make
        // a recoverable outcome look like a broken one.
        transport.answerAll(409, Map.of(HttpTransactionClient.H_ERROR_CODE,
                List.of(String.valueOf(XAException.XA_RBROLLBACK)),
                "X-GF-Reason", List.of("the branch was rolled back")));

        XAException thrown = assertThrows(XAException.class, () -> client.commit(XID, false));
        assertEquals(XAException.XA_RBROLLBACK, thrown.errorCode);
        assertTrue(thrown.getMessage().contains("rolled back"), thrown.getMessage());
    }

    @Test
    @DisplayName("a read-only branch votes read-only, so the coordinator can drop it")
    void aReadOnlyPrepareIsReported() throws Exception {
        transport.answerAll(204, Map.of(TxRoutes.H_READ_ONLY, List.of("true")));
        assertEquals(XAResource.XA_RDONLY, client.prepare(XID));

        transport.answerAll(204, Map.of());
        assertEquals(XAResource.XA_OK, client.prepare(XID));
    }

    @Test
    @DisplayName("an association belongs to its own thread")
    void theAssociationIsThreadScoped() throws Exception {
        ClientTransactionContext.associate(XID, 30);
        assertTrue(Xids.sameXid(XID, ClientTransactionContext.current()));

        AtomicReference<Xid> seenElsewhere = new AtomicReference<>(XID);
        CountDownLatch done = new CountDownLatch(1);
        Thread other = new Thread(() -> {
            // A transaction leaking across threads would enlist work nobody
            // asked to enlist.
            seenElsewhere.set(ClientTransactionContext.current());
            done.countDown();
        });
        other.start();
        assertTrue(done.await(5, TimeUnit.SECONDS));

        assertNull(seenElsewhere.get());
        assertEquals(30, ClientTransactionContext.currentTimeoutSeconds());
    }

    @Test
    @DisplayName("associating with no xid clears the thread rather than storing nothing")
    void associatingNullDisassociates() {
        ClientTransactionContext.associate(XID, 10);
        ClientTransactionContext.associate(null, 10);

        assertNull(ClientTransactionContext.current());
        assertEquals(0, ClientTransactionContext.currentTimeoutSeconds());
    }
}
