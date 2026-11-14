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
import jakarta.ejb.EJBTransactionRolledbackException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;

import java.net.URI;
import java.util.concurrent.ExecutionException;

import javax.naming.Context;

import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    @DisplayName("a transaction marked rollback-only refuses the call instead of running it outside")
    void aRollbackOnlyTransactionRefusesTheCall() {
        transaction.status = Status.STATUS_MARKED_ROLLBACK;

        assertThrows(EJBTransactionRolledbackException.class, () -> pinger().ping());

        assertEquals(0, transport.calls.size(), "the bean was called outside the caller's transaction");
        assertEquals(0, transaction.enlisted);
        assertNull(ClientTransactionContext.current());
    }

    @Test
    @DisplayName("a manager that refuses the enlistment because the transaction cannot commit refuses the call")
    void aRollbackDuringEnlistmentRefusesTheCall() {
        transaction.enlistFailure = new RollbackException("marked rollback-only");
        // A manager that refuses late has already started the branch.
        transaction.startFirst = true;

        EJBTransactionRolledbackException thrown =
                assertThrows(EJBTransactionRolledbackException.class, () -> pinger().ping());

        assertInstanceOf(RollbackException.class, thrown.getCause());
        assertEquals(0, transport.calls.size());
        assertNull(ClientTransactionContext.current(), "a refused enlistment left its branch on the thread");
    }

    @Test
    @DisplayName("any other failure to enlist refuses the call too, as an EJBException")
    void anEnlistmentFailureRefusesTheCall() {
        transaction.enlistFailure = new SystemException("manager is broken");

        EJBException thrown = assertThrows(EJBException.class, () -> pinger().ping());

        assertFalse(thrown instanceof EJBTransactionRolledbackException);
        assertInstanceOf(SystemException.class, thrown.getCause());
        assertEquals(0, transport.calls.size());
    }

    @Test
    @DisplayName("an active transaction with no transaction manager to join it refuses the call")
    void aMissingManagerRefusesTheCall() {
        AmbientTransactionTest.BOUND.remove("java:appserver/TransactionManager");

        assertThrows(EJBException.class, () -> pinger().ping());

        assertEquals(0, transport.calls.size());
    }

    @Test
    @DisplayName("an asynchronous call that cannot join fails its future and sends nothing")
    void anAsynchronousCallFailsItsFuture() throws Exception {
        transaction.status = Status.STATUS_MARKED_ROLLBACK;

        ExecutionException thrown = assertThrows(ExecutionException.class, () -> client.invokeAsync(
                new EjbLocator("app", "module", null, "PingerBean"), Pinger.class,
                Pinger.class.getMethod("ping"), new Object[0]).get());

        assertInstanceOf(EJBTransactionRolledbackException.class, thrown.getCause());
        assertEquals(0, transport.calls.size());
    }

    @Test
    @DisplayName("with joining switched off the same transaction does not stop the call")
    void switchingJoiningOffIsTheOnlyWayToCallAnyway() {
        transaction.status = Status.STATUS_MARKED_ROLLBACK;
        HttpEjbClient detached = new HttpEjbClient(
                ClientConfiguration.builder(config.baseUri()).joinAmbientTransaction(false).build(),
                transport, new JavaSerializationMarshaller(), JavaSerializationMarshaller.defaultFilter());

        detached.createProxy(Pinger.class, new EjbLocator("app", "module", null, "PingerBean")).ping();

        assertEquals(1, transport.calls.size());
    }
}
