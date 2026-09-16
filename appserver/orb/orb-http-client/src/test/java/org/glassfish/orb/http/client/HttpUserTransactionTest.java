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

import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.transaction.xa.Xid;

import org.glassfish.orb.http.protocol.TxRoutes;
import org.glassfish.orb.http.protocol.Xids;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What the client decides on its own, before the server is asked.
 */
class HttpUserTransactionTest {

    private RecordingTransport transport;

    private HttpUserTransaction transaction;

    private static final Xid XID = new Xids.SimpleXid(131075,
            new byte[] { 1, 2, 3, 4 }, new byte[] { 9 });

    @BeforeEach
    void setUp() {
        transport = new RecordingTransport();
        // Every begin answers with a known xid, the way the server would.
        transport.answerAll(204, Map.of(TxRoutes.H_XID, List.of(Xids.toPathSegment(XID))));

        ClientConfiguration config = ClientConfiguration
                .builder(URI.create("http://localhost:8080/glassfish-services")).build();
        transaction = new HttpUserTransaction(config, transport);
    }

    @AfterEach
    void tearDown() {
        // The association is thread scoped; leaving one behind would make the
        // next test in this thread start inside a transaction.
        ClientTransactionContext.disassociate();
    }

    @Test
    @DisplayName("begin associates the thread, and a second begin is refused")
    void beginAssociatesOnceOnly() throws Exception {
        assertEquals(Status.STATUS_NO_TRANSACTION, transaction.getStatus());

        transaction.begin();

        assertEquals(Status.STATUS_ACTIVE, transaction.getStatus());
        assertTrue(transport.called(TxRoutes.OP_BEGIN));
        // Nesting is not something this transport can honour, and pretending
        // otherwise would silently flatten two transactions into one.
        assertThrows(NotSupportedException.class, transaction::begin);
    }

    @Test
    @DisplayName("commit or rollback without a transaction is refused, not ignored")
    void operationsWithoutATransactionAreRefused() {
        assertThrows(IllegalStateException.class, transaction::commit);
        assertThrows(IllegalStateException.class, transaction::rollback);
        assertThrows(IllegalStateException.class, transaction::setRollbackOnly);
        assertFalse(transport.called(TxRoutes.OP_COMMIT));
    }

    @Test
    @DisplayName("a transaction marked rollback-only is rolled back, never committed")
    void aRollbackOnlyTransactionIsRolledBack() throws Exception {
        transaction.begin();
        transaction.setRollbackOnly();

        assertEquals(Status.STATUS_MARKED_ROLLBACK, transaction.getStatus());
        assertThrows(RollbackException.class, transaction::commit);

        // The whole point: the server is told to roll back. Sending a commit
        // and relying on it to refuse would put the decision in the wrong
        // place, and a server that did not refuse would lose the mark.
        assertTrue(transport.called(TxRoutes.OP_ROLLBACK));
        assertFalse(transport.called(TxRoutes.OP_COMMIT));
    }

    @Test
    @DisplayName("the thread is released whichever way the transaction ends")
    void theAssociationIsAlwaysCleared() throws Exception {
        transaction.begin();
        transaction.commit();
        assertNull(ClientTransactionContext.current());
        assertEquals(Status.STATUS_NO_TRANSACTION, transaction.getStatus());

        transaction.begin();
        transaction.rollback();
        assertNull(ClientTransactionContext.current());

        // And after a failed commit: an association surviving a failure would
        // trap the thread in a transaction no server knows about.
        transaction.begin();
        transaction.setRollbackOnly();
        assertThrows(RollbackException.class, transaction::commit);
        assertNull(ClientTransactionContext.current());
    }

    @Test
    @DisplayName("the timeout the caller set travels with begin")
    void theTimeoutIsSent() throws Exception {
        transaction.setTransactionTimeout(45);
        transaction.begin();

        assertTrue(transport.calls.get(0).contains(TxRoutes.PARAM_TIMEOUT + "=45"),
                transport.calls.get(0));
    }

    @Test
    @DisplayName("a negative timeout means none rather than a negative one")
    void aNegativeTimeoutIsTreatedAsUnset() throws Exception {
        transaction.setTransactionTimeout(-5);
        transaction.begin();

        assertFalse(transport.calls.get(0).contains("=-5"), transport.calls.get(0));
    }
}
