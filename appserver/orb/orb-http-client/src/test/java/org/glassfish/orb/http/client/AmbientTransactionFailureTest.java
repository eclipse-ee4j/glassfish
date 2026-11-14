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

import jakarta.ejb.EJBException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;

import java.net.URI;
import java.util.concurrent.ExecutionException;

import javax.naming.Context;

import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Protocol;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A call that cannot join the transaction it is made in must not be made.
 * <p>
 * The alternative is to run the bean and commit its work outside a
 * transaction that is marked rollback-only, which is the one outcome a
 * transactional caller has no way to notice.
 */
class AmbientTransactionFailureTest {

    public interface Pinger {
        void ping();
    }

    private ClientConfiguration config;

    private RecordingTransport transport;

    private AmbientTransactionTest.FakeTransaction transaction;

    private HttpEjbClient client;

    @BeforeEach
    void setUp() {
        AmbientTransactionTest.BOUND.clear();
        AmbientTransactionTest.started = null;
        AmbientTransaction.forget();
        ClientTransactionContext.disassociate();
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, AmbientTransactionTest.Factory.class.getName());

        config = ClientConfiguration.builder(URI.create("http://localhost:8080/glassfish-services")).build();
        transport = new RecordingTransport();
        transaction = new AmbientTransactionTest.FakeTransaction();
        AmbientTransactionTest.BOUND.put("java:comp/TransactionSynchronizationRegistry",
                new AmbientTransactionTest.FakeRegistry(transaction));
        AmbientTransactionTest.BOUND.put("java:appserver/TransactionManager",
                new AmbientTransactionTest.FakeManager(transaction));
        client = new HttpEjbClient(config, transport, new JavaSerializationMarshaller(),
                JavaSerializationMarshaller.defaultFilter());
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
        ClientTransactionContext.disassociate();
        AmbientTransaction.forget();
    }

    private Pinger pinger() {
        return client.createProxy(Pinger.class, new EjbLocator("app", "module", null, "PingerBean"));
    }

    private String rollbackOnlyMarker(int call) {
        return transport.requestHeaders.get(call).get(Protocol.H_TXN_ROLLBACK_ONLY);
    }

    @Test
    @DisplayName("a transaction marked rollback-only is not refused: the call is sent with the marker")
    void aRollbackOnlyTransactionIsSentWithTheMarker() {
        transaction.status = Status.STATUS_MARKED_ROLLBACK;

        pinger().ping();

        assertEquals(1, transport.calls.size());
        assertEquals("true", rollbackOnlyMarker(0));
        // A manager will not enlist in such a transaction, so there is no branch.
        assertEquals(0, transaction.enlisted);
        assertNull(ClientTransactionContext.current());
    }

    @Test
    @DisplayName("a transaction that is already rolling back is sent with the marker too")
    void aRollingBackTransactionIsSentWithTheMarker() {
        transaction.status = Status.STATUS_ROLLING_BACK;

        pinger().ping();

        assertEquals("true", rollbackOnlyMarker(0));
    }

    @Test
    @DisplayName("a joined transaction that becomes rollback-only still carries its branch, with the marker")
    void aJoinedTransactionThatBecomesRollbackOnlyCarriesBranchAndMarker() {
        pinger().ping();
        assertNull(transport.requestHeaders.get(0).get(Protocol.H_TXN_ROLLBACK_ONLY));
        ClientTransactionContext.disassociate();

        transaction.status = Status.STATUS_MARKED_ROLLBACK;
        pinger().ping();

        assertEquals("true", rollbackOnlyMarker(1));
        assertEquals(AmbientTransactionTest.started, ClientTransactionContext.current());
        assertEquals(1, transaction.enlisted, "the endpoint is enlisted once");
    }

    @Test
    @DisplayName("a call from afterCompletion, when the transaction has completed, is sent without a transaction")
    void aCompletedTransactionIsNoTransaction() {
        for (int completed : new int[] { Status.STATUS_COMMITTED, Status.STATUS_ROLLEDBACK,
                Status.STATUS_NO_TRANSACTION }) {
            transport.calls.clear();
            transport.requestHeaders.clear();
            transaction.status = completed;

            pinger().ping();

            assertEquals(1, transport.calls.size(), "status " + completed);
            assertNull(rollbackOnlyHeader(), "status " + completed);
            assertEquals(0, transaction.enlisted, "status " + completed);
            assertNull(ClientTransactionContext.current(), "status " + completed);
        }
    }

    private String rollbackOnlyHeader() {
        return transport.requestHeaders.get(0).get(Protocol.H_TXN_ROLLBACK_ONLY);
    }

    @Test
    @DisplayName("a branch joined earlier is not put back once its transaction has completed")
    void aCompletedTransactionDoesNotRestoreItsBranch() {
        pinger().ping();
        ClientTransactionContext.disassociate();

        transaction.status = Status.STATUS_COMMITTED;
        pinger().ping();

        assertNull(ClientTransactionContext.current(), "a completed transaction's xid was re-associated");
        assertNull(transport.requestHeaders.get(1).get(Protocol.H_TXN_ROLLBACK_ONLY));
    }

    @Test
    @DisplayName("a transaction in the middle of two-phase commit refuses the call")
    void aTransactionInFlightRefusesTheCall() {
        for (int inFlight : new int[] { Status.STATUS_PREPARING, Status.STATUS_PREPARED,
                Status.STATUS_COMMITTING }) {
            transport.calls.clear();
            transaction.status = inFlight;

            assertThrows(EJBException.class, () -> pinger().ping(), "status " + inFlight);

            assertEquals(0, transport.calls.size(), "status " + inFlight);
        }
    }

    @Test
    @DisplayName("a manager that refuses the enlistment because the transaction cannot commit means rollback-only")
    void aRollbackDuringEnlistmentIsRollbackOnly() {
        transaction.enlistFailure = new RollbackException("marked rollback-only");
        // A manager that refuses late has already started the branch.
        transaction.startFirst = true;

        pinger().ping();

        assertEquals("true", rollbackOnlyMarker(0));
        assertNull(ClientTransactionContext.current(), "a refused enlistment left its branch on the thread");
    }

    @Test
    @DisplayName("any other failure to enlist refuses the call too, as an EJBException")
    void anEnlistmentFailureRefusesTheCall() {
        transaction.enlistFailure = new SystemException("manager is broken");

        EJBException thrown = assertThrows(EJBException.class, () -> pinger().ping());

        assertInstanceOf(SystemException.class, thrown.getCause());
        assertEquals(0, transport.calls.size());
    }

    @Test
    @DisplayName("an active transaction with no transaction manager to join it refuses the call")
    void aMissingManagerRefusesTheCall() {
        AmbientTransactionTest.BOUND.remove("java:appserver/TransactionManager");

        EJBException thrown = assertThrows(EJBException.class, () -> pinger().ping());

        assertEquals(0, transport.calls.size());
        assertTrue(thrown.getMessage().contains("org.glassfish.orb.http.transactionManager"), thrown.getMessage());
        assertTrue(thrown.getMessage().contains(HttpInitialContextFactory.PROP_AMBIENT_TRANSACTION),
                thrown.getMessage());
    }

    @Test
    @DisplayName("an asynchronous call that cannot join fails its future and sends nothing")
    void anAsynchronousCallFailsItsFuture() throws Exception {
        transaction.status = Status.STATUS_PREPARED;

        ExecutionException thrown = assertThrows(ExecutionException.class, () -> client.invokeAsync(
                new EjbLocator("app", "module", null, "PingerBean"), Pinger.class,
                Pinger.class.getMethod("ping"), new Object[0]).get());

        assertInstanceOf(EJBException.class, thrown.getCause());
        assertEquals(0, transport.calls.size());
    }

    @Test
    @DisplayName("guard: with joining switched off a transaction that would refuse the call does not stop it")
    void switchingJoiningOffIsTheOnlyWayToCallAnyway() {
        // Fails if only the switch is reverted: the join would then refuse.
        transaction.status = Status.STATUS_PREPARED;
        HttpEjbClient detached = new HttpEjbClient(
                ClientConfiguration.builder(config.baseUri()).joinAmbientTransaction(false).build(),
                transport, new JavaSerializationMarshaller(), JavaSerializationMarshaller.defaultFilter());

        detached.createProxy(Pinger.class, new EjbLocator("app", "module", null, "PingerBean")).ping();

        assertEquals(1, transport.calls.size());
    }
}
