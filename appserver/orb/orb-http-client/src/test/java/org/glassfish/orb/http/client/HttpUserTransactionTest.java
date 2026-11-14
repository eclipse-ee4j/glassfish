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

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.transaction.xa.XAException;
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
        System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
        AmbientTransaction.forget();
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

    private static Map<String, List<String>> xaError(int code) {
        return Map.of(HttpTransactionClient.H_ERROR_CODE, List.of(Integer.toString(code)));
    }

    @Test
    @DisplayName("a heuristic mix or hazard on commit is reported as HeuristicMixedException")
    void heuristicMixedIsReported() throws Exception {
        for (int code : new int[] { XAException.XA_HEURMIX, XAException.XA_HEURHAZ }) {
            transport.answer("/ut/commit/", 500, xaError(code));
            transaction.begin();

            assertThrows(HeuristicMixedException.class, transaction::commit, "XA code " + code);
            assertNull(ClientTransactionContext.current());
        }
    }

    @Test
    @DisplayName("a heuristic rollback on commit is reported as HeuristicRollbackException")
    void heuristicRollbackIsReported() throws Exception {
        transport.answer("/ut/commit/", 500, xaError(XAException.XA_HEURRB));
        transaction.begin();

        assertThrows(HeuristicRollbackException.class, transaction::commit);
        assertNull(ClientTransactionContext.current());
    }

    @Test
    @DisplayName("a heuristic commit is the outcome asked for, so commit returns normally")
    void heuristicCommitIsNotAFailure() throws Exception {
        transport.answer("/ut/commit/", 500, xaError(XAException.XA_HEURCOM));
        transaction.begin();

        transaction.commit();

        assertNull(ClientTransactionContext.current());
        assertEquals(Status.STATUS_NO_TRANSACTION, transaction.getStatus());
    }

    @Test
    @DisplayName("an unknown transaction on commit is a RollbackException")
    void anUnknownTransactionOnCommitIsARollback() throws Exception {
        transport.answer("/ut/commit/", 500, xaError(XAException.XAER_NOTA));
        transaction.begin();

        assertThrows(RollbackException.class, transaction::commit);
        assertNull(ClientTransactionContext.current());
    }

    @Test
    @DisplayName("an unknown transaction on the rollback of a marked one is a RollbackException")
    void anUnknownTransactionOnMarkedRollbackIsARollback() throws Exception {
        transport.answer("/ut/rollback/", 500, xaError(XAException.XAER_NOTA));
        transaction.begin();
        transaction.setRollbackOnly();

        assertThrows(RollbackException.class, transaction::commit);
        assertNull(ClientTransactionContext.current());
    }

    @Test
    @DisplayName("a rollback code or heuristic rollback on the rollback of a marked transaction is a RollbackException")
    void aRollbackCodeOnMarkedRollbackIsARollback() throws Exception {
        for (int code : new int[] { XAException.XA_RBROLLBACK, XAException.XA_RBTIMEOUT, XAException.XA_HEURRB }) {
            transport.answer("/ut/rollback/", 500, xaError(code));
            transaction.begin();
            transaction.setRollbackOnly();

            assertThrows(RollbackException.class, transaction::commit, "XA code " + code);
        }
    }

    @Test
    @DisplayName("a marked transaction that completed heuristically in whole or part is a HeuristicMixedException")
    void aHeuristicCommitOnMarkedRollbackIsMixed() throws Exception {
        for (int code : new int[] { XAException.XA_HEURCOM, XAException.XA_HEURMIX, XAException.XA_HEURHAZ }) {
            transport.answer("/ut/rollback/", 500, xaError(code));
            transaction.begin();
            transaction.setRollbackOnly();

            assertThrows(HeuristicMixedException.class, transaction::commit, "XA code " + code);
        }
    }

    @Test
    @DisplayName("rollback of a transaction the server no longer knows succeeds, and clears the thread")
    void rollbackOfAnUnknownTransactionSucceeds() throws Exception {
        transport.answer("/ut/rollback/", 500, xaError(XAException.XAER_NOTA));
        transaction.begin();

        transaction.rollback();

        assertNull(ClientTransactionContext.current());
    }

    @Test
    @DisplayName("rollback that fails for any other reason is a SystemException")
    void anOrdinaryRollbackFailureIsASystemException() throws Exception {
        transport.answer("/ut/rollback/", 500, xaError(XAException.XAER_RMFAIL));
        transaction.begin();

        assertThrows(SystemException.class, transaction::rollback);
    }

    @Test
    @DisplayName("guard: a failure that says nothing about the outcome stays a SystemException")
    void anOrdinaryFailureIsASystemException() throws Exception {
        transport.answer("/ut/commit/", 500, xaError(XAException.XAER_RMFAIL));
        transaction.begin();

        assertThrows(SystemException.class, transaction::commit);
    }

    @Test
    @DisplayName("a container's transaction on this thread is not this UserTransaction's to commit")
    void anAmbientBranchIsNotOurTransaction() throws Exception {
        ClientTransactionContext.associate(XID, 0, true);

        assertEquals(Status.STATUS_NO_TRANSACTION, transaction.getStatus());
        assertThrows(IllegalStateException.class, transaction::commit);
        assertThrows(IllegalStateException.class, transaction::rollback);
        assertThrows(IllegalStateException.class, transaction::setRollbackOnly);
        assertFalse(transport.called(TxRoutes.OP_COMMIT), "committed the container's branch");
        assertFalse(transport.called(TxRoutes.OP_ROLLBACK), "rolled back the container's branch");
        // And it is still the container's: nothing here may drop it.
        assertEquals(XID, ClientTransactionContext.current());
        assertThrows(NotSupportedException.class, transaction::begin);
    }

    @Test
    @DisplayName("a branch left behind by a container transaction that has ended does not block begin")
    void aStaleAmbientBranchDoesNotBlockBegin() throws Exception {
        AmbientTransactionTest.BOUND.clear();
        AmbientTransactionTest.BOUND.put("java:comp/TransactionSynchronizationRegistry",
                new AmbientTransactionTest.FakeRegistry(null));
        AmbientTransaction.forget();
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, AmbientTransactionTest.Factory.class.getName());
        ClientTransactionContext.associate(XID, 0, true);

        assertEquals(Status.STATUS_NO_TRANSACTION, transaction.getStatus());
        transaction.begin();

        assertEquals(Status.STATUS_ACTIVE, transaction.getStatus());
        assertTrue(transport.called(TxRoutes.OP_BEGIN));
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
