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

import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.glassfish.orb.http.client.ClientConfiguration;
import org.glassfish.orb.http.client.ClientTransactionContext;
import org.glassfish.orb.http.client.EjbLocator;
import org.glassfish.orb.http.client.HttpEjbClient;
import org.glassfish.orb.http.client.HttpUserTransaction;
import org.glassfish.orb.http.client.HttpXAResource;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.Xids;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives transactions end to end: a real client, the real wire format, the
 * real dispatch, with only the socket and the transaction manager replaced.
 */
class HttpTransactionRoundTripTest {

    private static final ClientConfiguration CONFIG = ClientConfiguration
            .builder(URI.create("http://localhost:8080" + Protocol.CONTEXT_PATH))
            .build();

    private FakeContainer container;
    private FakeTransactions transactions;
    private Loopback loopback;
    private HttpEjbClient client;

    @BeforeEach
    void setUp() {
        container = new FakeContainer();
        container.registerStateless("GreeterBean", new TestBeans.GreeterBean());
        transactions = new FakeTransactions();

        EjbDispatcher ejb = new EjbDispatcher(container, SecurityBridge.NONE, transactions,
                new JavaSerializationMarshaller(), new InvocationRegistry(),
                SessionAffinity.forThisNode());
        loopback = new Loopback(ejb, new NamingDispatcher(new HttpRoundTripTest.FakeNaming()),
                new TransactionDispatcher(transactions));
        client = new HttpEjbClient(CONFIG, loopback, new JavaSerializationMarshaller(),
                JavaSerializationMarshaller.defaultFilter());
    }

    @AfterEach
    void tearDown() {
        TestBeans.duringInvocation = null;
        ClientTransactionContext.disassociate();
    }

    private TestBeans.Greeter greeter() {
        return client.createProxy(TestBeans.Greeter.class,
                new EjbLocator("myapp", "mymodule", null, "GreeterBean"));
    }

    // ---- server coordinated ------------------------------------------------

    @Test
    @DisplayName("begin, invoke, commit - and the branch is imported around the call")
    void userTransactionRoundTrip() throws Exception {
        HttpUserTransaction transaction = new HttpUserTransaction(CONFIG, loopback);

        assertEquals(Status.STATUS_NO_TRANSACTION, transaction.getStatus());
        transaction.begin();
        assertEquals(Status.STATUS_ACTIVE, transaction.getStatus());

        Xid xid = ClientTransactionContext.current();
        assertNotNull(xid, "begin must associate the branch with this thread");

        assertEquals("Ada", greeter().greet("Ada", 1));
        transaction.commit();

        assertEquals(List.of("begin(0)",
                        "recreate(" + Xids.key(xid) + ",0)",
                        "release(" + Xids.key(xid) + ")",
                        "commitUserTransaction"),
                transactions.calls);
        assertEquals(Status.STATUS_NO_TRANSACTION, transaction.getStatus());
        assertTrue(transactions.live.isEmpty());
    }

    @Test
    @DisplayName("the transaction is actually associated while the bean runs, not merely before and after")
    void theBranchIsLiveDuringTheInvocation() throws Exception {
        HttpUserTransaction transaction = new HttpUserTransaction(CONFIG, loopback);
        transaction.begin();
        Xid xid = ClientTransactionContext.current();

        AtomicReference<Long> seen = new AtomicReference<>();
        TestBeans.duringInvocation = () -> seen.set(transactions.imported.get(Xids.key(xid)));

        greeter().notifyArrival("Grace");
        transaction.commit();

        assertNotNull(seen.get(), "the branch must be imported while the bean is executing");
        assertTrue(transactions.imported.isEmpty(), "and released afterwards");
    }

    @Test
    @DisplayName("an invocation outside a transaction imports none - and clears any left on the thread")
    void anInvocationOutsideATransactionCarriesNone() {
        assertEquals("Ada", greeter().greet("Ada", 1));
        // detach and nothing else: no branch is imported, and the thread is
        // made to match what the caller asked for. A request thread is pooled,
        // so a branch an earlier request left on it would become this
        // invocation's transaction - which is how a call made deliberately
        // outside a transaction came to report someone else's.
        assertEquals(List.of("detach"), transactions.calls);
    }

    @Test
    void rollbackDiscardsTheTransaction() throws Exception {
        HttpUserTransaction transaction = new HttpUserTransaction(CONFIG, loopback);
        transaction.begin();
        greeter().greet("Ada", 1);
        transaction.rollback();

        assertTrue(transactions.calls.contains("rollbackUserTransaction"));
        assertTrue(transactions.live.isEmpty());
        assertEquals(Status.STATUS_NO_TRANSACTION, transaction.getStatus());
    }

    @Test
    @DisplayName("setRollbackOnly makes commit roll back and report it")
    void rollbackOnlyIsHonoured() throws Exception {
        HttpUserTransaction transaction = new HttpUserTransaction(CONFIG, loopback);
        transaction.begin();
        transaction.setRollbackOnly();
        assertEquals(Status.STATUS_MARKED_ROLLBACK, transaction.getStatus());

        assertThrows(RollbackException.class, transaction::commit);
        assertTrue(transactions.calls.contains("rollbackUserTransaction"));
        assertTrue(transactions.live.isEmpty(), "a rolled back branch must not be left live");
    }

    @Test
    void beginTwiceOnOneThreadIsRefused() throws Exception {
        HttpUserTransaction transaction = new HttpUserTransaction(CONFIG, loopback);
        transaction.begin();
        assertThrows(jakarta.transaction.NotSupportedException.class, transaction::begin);
        transaction.rollback();
    }

    // ---- caller coordinated -------------------------------------------------

    @Test
    @DisplayName("an XAResource drives the full two-phase sequence")
    void xaResourceTwoPhaseCommit() throws Exception {
        HttpXAResource resource = new HttpXAResource(CONFIG, loopback);
        Xid xid = transactions.begin(0);
        transactions.calls.clear();

        resource.start(xid, XAResource.TMNOFLAGS);
        greeter().greet("Ada", 1);
        resource.end(xid, XAResource.TMSUCCESS);

        assertEquals(XAResource.XA_OK, resource.prepare(xid));
        resource.commit(xid, false);

        assertEquals(List.of("recreate(" + Xids.key(xid) + ",0)",
                        "release(" + Xids.key(xid) + ")",
                        "beforeCompletion",
                        "prepare",
                        "commit(onePhase=false)"),
                transactions.calls);
    }

    @Test
    void aReadOnlyBranchVotesReadOnly() throws Exception {
        HttpXAResource resource = new HttpXAResource(CONFIG, loopback);
        Xid xid = transactions.begin(0);
        transactions.voteReadOnly = true;

        assertEquals(XAResource.XA_RDONLY, resource.prepare(xid),
                "a read-only vote must survive the wire; committing that branch would be wrong");
    }

    @Test
    @DisplayName("the XA error code survives, because a coordinator acts on it")
    void xaErrorCodesAreNotFlattened() throws Exception {
        HttpXAResource resource = new HttpXAResource(CONFIG, loopback);
        Xid xid = transactions.begin(0);

        transactions.failWith = XAException.XAER_NOTA;
        XAException thrown = assertThrows(XAException.class, () -> resource.rollback(xid));
        assertEquals(XAException.XAER_NOTA, thrown.errorCode);

        transactions.failWith = XAException.XA_RBROLLBACK;
        XAException rolled = assertThrows(XAException.class, () -> resource.commit(xid, true));
        assertEquals(XAException.XA_RBROLLBACK, rolled.errorCode);
    }

    @Test
    void recoverListsPreparedBranches() throws Exception {
        HttpXAResource resource = new HttpXAResource(CONFIG, loopback);
        Xid first = transactions.begin(0);
        Xid second = transactions.begin(0);
        resource.prepare(first);
        resource.prepare(second);

        Xid[] recovered = resource.recover(XAResource.TMSTARTRSCAN);

        assertEquals(2, recovered.length);
        assertTrue(Xids.sameXid(first, recovered[0]));
        assertTrue(Xids.sameXid(second, recovered[1]));
    }

    @Test
    void sameEndpointMeansSameResourceManager() {
        HttpXAResource one = new HttpXAResource(CONFIG, loopback);
        HttpXAResource two = new HttpXAResource(CONFIG, loopback);
        HttpXAResource elsewhere = new HttpXAResource(
                ClientConfiguration.builder(URI.create("http://other:8080" + Protocol.CONTEXT_PATH)).build(),
                loopback);

        assertTrue(one.isSameRM(two));
        assertTrue(!one.isSameRM(elsewhere));
    }

    // ---- failure handling ---------------------------------------------------

    @Test
    @DisplayName("the branch is released even when the bean throws")
    void releaseHappensAfterAFailedInvocation() throws Exception {
        HttpUserTransaction transaction = new HttpUserTransaction(CONFIG, loopback);
        transaction.begin();
        Xid xid = ClientTransactionContext.current();

        assertThrows(TestBeans.GreetingRefused.class, () -> greeter().refuse());

        assertTrue(transactions.calls.contains("release(" + Xids.key(xid) + ")"),
                "a branch left associated would leak into the next invocation on this thread");
        assertTrue(transactions.imported.isEmpty());
        transaction.rollback();
    }

    @Test
    @DisplayName("without a transaction bridge a transactional invocation is refused, not run outside it")
    void anEndpointWithoutTransactionsRefusesRatherThanIgnores() throws Exception {
        EjbDispatcher plain = new EjbDispatcher(container);
        Loopback noTransactions = new Loopback(plain,
                new NamingDispatcher(new HttpRoundTripTest.FakeNaming()),
                new TransactionDispatcher(TransactionBridge.NONE));
        HttpEjbClient plainClient = new HttpEjbClient(CONFIG, noTransactions,
                new JavaSerializationMarshaller(), JavaSerializationMarshaller.defaultFilter());

        ClientTransactionContext.associate(transactions.begin(0), 0);
        try {
            TestBeans.Greeter bean = plainClient.createProxy(TestBeans.Greeter.class,
                    new EjbLocator("myapp", "mymodule", null, "GreeterBean"));
            // Silently running outside the caller's transaction and reporting
            // success is the one outcome a transactional client cannot detect.
            assertThrows(Exception.class, () -> bean.greet("Ada", 1));
        } finally {
            ClientTransactionContext.disassociate();
        }
    }
}
