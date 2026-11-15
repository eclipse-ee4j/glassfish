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
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private static final Xid CARRIED = new Xids.SimpleXid(0x4F524248, new byte[] { 1, 2, 3 }, new byte[] { 1 });

    /** Invokes greet(String,int) carrying {@link #CARRIED} and the given extra request headers. */
    private static Loopback.InMemoryExchange invokeCarryingTransaction(
            TransactionBridge transactions, Map<String, String> extraHeaders) throws IOException {
        return invoke(transactions, extraHeaders, true);
    }

    private static Loopback.InMemoryExchange invoke(TransactionBridge transactions,
            Map<String, String> extraHeaders, boolean carryTransaction) throws IOException {
        return invoke(transactions, extraHeaders, carryTransaction, "greet",
                new String[] { String.class.getName(), int.class.getName() }, "Ada", 1);
    }

    private static Loopback.InMemoryExchange invoke(TransactionBridge transactions,
            Map<String, String> extraHeaders, boolean carryTransaction, String methodName,
            String[] parameterTypes, Object... arguments) throws IOException {
        FakeContainer container = new FakeContainer();
        container.registerStateless("GreeterBean", new TestBeans.GreeterBean());
        return invoke(transactions, container, new InvocationRegistry(), extraHeaders, carryTransaction,
                methodName, parameterTypes, arguments);
    }

    private static Loopback.InMemoryExchange invoke(TransactionBridge transactions, FakeContainer container,
            InvocationRegistry registry, Map<String, String> extraHeaders, boolean carryTransaction,
            String methodName, String[] parameterTypes, Object... arguments) throws IOException {
        EjbDispatcher dispatcher = new EjbDispatcher(container, SecurityBridge.NONE, transactions,
                new JavaSerializationMarshaller(), registry, SessionAffinity.forThisNode());

        String path = EjbRoutes.invokePath(Protocol.CONTEXT_PATH, "myapp", "mymodule", null,
                "GreeterBean", null, TestBeans.Greeter.class.getName(), methodName, parameterTypes);
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        InvocationEnvelope.writeTxContext(body, carryTransaction
                ? Xids.toContext(CARRIED, TxContext.TYPE_REMOTE) : TxContext.NONE);
        try (Marshaller.ObjectWriter writer = new JavaSerializationMarshaller().newWriter(body)) {
            for (Object argument : arguments) {
                writer.writeObject(argument);
            }
            writer.writeObject(Map.of());
        }
        Map<String, String> headers = new java.util.HashMap<>(extraHeaders);
        headers.putIfAbsent("Content-Type", ContentType.of(JavaSerializationMarshaller.CODEC,
                ContentType.KIND_INVOCATION).toHeaderValue());
        Loopback.InMemoryExchange exchange = new Loopback.InMemoryExchange("POST",
                path.getBytes(StandardCharsets.UTF_8), null, headers, body.toByteArray());
        dispatcher.dispatch(exchange);
        return exchange;
    }

    private void assertTimeoutRefused(String bad) throws IOException {
        FakeTransactions transactions = new FakeTransactions();
        Loopback.InMemoryExchange exchange = invokeCarryingTransaction(transactions,
                Map.of(Protocol.H_TXN_TIMEOUT, bad));

        assertEquals(Protocol.SC_BAD_REQUEST, exchange.toResponse().status(), "timeout " + bad);
        // Nothing is imported, and the thread is left clean of the previous request.
        assertEquals(List.of("detach"), transactions.calls, "recreate must not see timeout " + bad);
    }

    @Test
    @DisplayName("a negative timeout header is a 400 and reaches nobody")
    void aNegativeTimeoutIsRefused() throws IOException {
        assertTimeoutRefused("-5");
    }

    @Test
    @DisplayName("a timeout header larger than an int is a 400 and reaches nobody")
    void anOversizedTimeoutIsRefused() throws IOException {
        assertTimeoutRefused("99999999999");
    }

    @Test
    @DisplayName("a timeout header that is Long.MAX_VALUE is a 400 and reaches nobody")
    void aMaximalTimeoutIsRefused() throws IOException {
        assertTimeoutRefused("9223372036854775807");
    }

    @Test
    @DisplayName("a timeout header that is not a number is a 400 and reaches nobody")
    void aNonNumericTimeoutIsRefused() throws IOException {
        assertTimeoutRefused("abc");
    }

    @Test
    @DisplayName("guard: a sensible timeout still reaches recreate unchanged")
    void aSensibleTimeoutIsPassedOn() throws IOException {
        FakeTransactions transactions = new FakeTransactions();
        Loopback.InMemoryExchange exchange = invokeCarryingTransaction(transactions,
                Map.of(Protocol.H_TXN_TIMEOUT, "30"));

        assertEquals(Protocol.SC_OK, exchange.toResponse().status());
        assertEquals("recreate(" + Xids.key(CARRIED) + ",30)", transactions.calls.get(0));
    }

    @Test
    @DisplayName("an import that fails leaves the thread with no transaction")
    void aFailedImportDetachesTheThread() throws IOException {
        FakeTransactions transactions = new FakeTransactions();
        transactions.failWith = XAException.XAER_RMERR;

        Loopback.InMemoryExchange exchange = invokeCarryingTransaction(transactions, Map.of());

        assertEquals(Protocol.SC_EXCEPTION, exchange.toResponse().status());
        // A manager that fails halfway may have associated the thread already,
        // and a branch that was never returned is never released.
        assertEquals(List.of("recreate(" + Xids.key(CARRIED) + ",0)", "detach"), transactions.calls);
    }

    private static Map<String, String> rollbackOnly() {
        return Map.of(Protocol.H_TXN_ROLLBACK_ONLY, "true");
    }

    @Test
    @DisplayName("a bridge that does not know rollback-only refuses with a rollback code, and the bean is not called")
    void aBridgeWithoutRollbackOnlySupportRefuses() throws IOException {
        Loopback.InMemoryExchange exchange = invoke(TransactionBridge.NONE, rollbackOnly(), true);

        assertEquals(Protocol.SC_EXCEPTION, exchange.toResponse().status());
        assertEquals(List.of(Integer.toString(XAException.XA_RBROLLBACK)),
                exchange.toResponse().headers().get(TransactionDispatcher.H_ERROR_CODE));
    }

    @Test
    @DisplayName("a rollback-only call whose method does not need the transaction runs, and its branch is released")
    void aRollbackOnlyCallThatMayRunRuns() throws IOException {
        FakeTransactions transactions = new FakeTransactions();

        Loopback.InMemoryExchange exchange = invoke(transactions, rollbackOnly(), true);

        assertEquals(Protocol.SC_OK, exchange.toResponse().status());
        assertEquals(List.of("recreateRollbackOnly(" + Xids.key(CARRIED) + ",0,greet,key=true)",
                "releaseRollbackOnly(" + Xids.key(CARRIED) + ")"), transactions.calls);
        assertEquals(Map.of(), transactions.imported);
    }

    @Test
    @DisplayName("a rollback-only call the container refuses answers a rollback code and detaches the thread")
    void aRefusedRollbackOnlyCallAnswersARollbackCode() throws IOException {
        FakeTransactions transactions = new FakeTransactions();
        transactions.refuseRollbackOnly = true;

        Loopback.InMemoryExchange exchange = invoke(transactions, rollbackOnly(), true);

        assertEquals(Protocol.SC_EXCEPTION, exchange.toResponse().status());
        assertEquals(List.of(Integer.toString(XAException.XA_RBROLLBACK)),
                exchange.toResponse().headers().get(TransactionDispatcher.H_ERROR_CODE));
        assertEquals(List.of("recreateRollbackOnly(" + Xids.key(CARRIED) + ",0,greet,key=true)", "detach"),
                transactions.calls);
    }

    @Test
    @DisplayName("a rollback-only call with no branch reaches the bridge with no xid, and is released after")
    void aRollbackOnlyCallWithoutABranchReachesTheBridge() throws IOException {
        FakeTransactions transactions = new FakeTransactions();

        Loopback.InMemoryExchange exchange = invoke(transactions, rollbackOnly(), false);

        assertEquals(Protocol.SC_OK, exchange.toResponse().status());
        assertEquals(List.of("detach", "recreateRollbackOnly(none,0,greet,key=true)",
                "releaseRollbackOnly(local)"), transactions.calls);
        assertEquals(Map.of(), transactions.imported, "the local transaction was left behind");
    }

    @Test
    @DisplayName("guard: only the value true is a rollback-only marker")
    void onlyTrueIsTheMarker() throws IOException {
        FakeTransactions transactions = new FakeTransactions();

        invoke(transactions, Map.of(Protocol.H_TXN_ROLLBACK_ONLY, "false"), true);

        assertEquals("recreate(" + Xids.key(CARRIED) + ",0)", transactions.calls.get(0));
    }

    @Test
    @DisplayName("a detach that fails while cleaning up after a failed import does not hide the first failure")
    void aFailingDetachIsSuppressed() throws IOException {
        FakeTransactions transactions = new FakeTransactions();
        transactions.recreateThrows = new IllegalStateException("manager is broken");
        transactions.detachThrows = new IllegalArgumentException("detach is broken too");

        IllegalStateException thrown = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> invoke(transactions, Map.of(), true));

        assertEquals(1, thrown.getSuppressed().length);
        assertEquals("detach is broken too", thrown.getSuppressed()[0].getMessage());
    }

    @Test
    @DisplayName("the rollback-only handle is released when the bean throws")
    void theHandleIsReleasedWhenTheBeanThrows() throws IOException {
        FakeTransactions transactions = new FakeTransactions();

        Loopback.InMemoryExchange exchange = invoke(transactions, rollbackOnly(), false, "refuse", new String[0]);

        assertEquals(Protocol.SC_EXCEPTION, exchange.toResponse().status());
        assertEquals(List.of("detach", "recreateRollbackOnly(none,0,refuse,key=true)",
                "releaseRollbackOnly(local)"), transactions.calls);
    }

    @Test
    @DisplayName("the rollback-only handle is released after an asynchronous method")
    void theHandleIsReleasedAfterAnAsynchronousMethod() throws IOException {
        FakeTransactions transactions = new FakeTransactions();

        Loopback.InMemoryExchange exchange = invoke(transactions, rollbackOnly(), true, "greetLater",
                new String[] { String.class.getName() }, "Ada");

        assertTrue(exchange.toResponse().status() < 300, "status " + exchange.toResponse().status());
        assertEquals(List.of("recreateRollbackOnly(" + Xids.key(CARRIED) + ",0,greetLater,key=true)",
                "releaseRollbackOnly(" + Xids.key(CARRIED) + ")"), transactions.calls);
    }

    @Test
    @DisplayName("guard: a request refused for its codec never reaches the bridge, and the retry is paired with its release")
    void aRequestRefusedForItsCodecNeverReachesTheBridge() throws IOException {
        FakeTransactions transactions = new FakeTransactions();
        Map<String, String> exotic = Map.of(Protocol.H_TXN_ROLLBACK_ONLY, "true",
                "Content-Type", ContentType.of("exotic", ContentType.KIND_INVOCATION).toHeaderValue());

        Loopback.InMemoryExchange refused = invoke(transactions, exotic, false);
        assertEquals(Protocol.SC_NOT_ACCEPTABLE, refused.toResponse().status());
        assertEquals(List.of(), transactions.calls, "the bridge was reached by a request that was refused");

        invoke(transactions, rollbackOnly(), false);

        assertEquals(List.of("detach", "recreateRollbackOnly(none,0,greet,key=true)", "releaseRollbackOnly(local)"),
                transactions.calls);
    }

    @Test
    @DisplayName("a bridge that returns no handle is refused with XAER_RMERR and the thread is detached")
    void aMissingHandleIsRefused() throws IOException {
        FakeTransactions transactions = new FakeTransactions();
        transactions.returnNullHandle = true;

        Loopback.InMemoryExchange exchange = invoke(transactions, rollbackOnly(), false);

        assertEquals(Protocol.SC_EXCEPTION, exchange.toResponse().status());
        assertEquals(List.of(Integer.toString(XAException.XAER_RMERR)),
                exchange.toResponse().headers().get(TransactionDispatcher.H_ERROR_CODE));
        assertEquals(List.of("detach", "recreateRollbackOnly(none,0,greet,key=true)", "detach"),
                transactions.calls);
    }

    @Test
    @DisplayName("a container that fails to release the target does not skip the transaction release")
    void aFailingTargetReleaseStillReleasesTheTransaction() throws IOException {
        FakeTransactions transactions = new FakeTransactions();
        FakeContainer container = new FakeContainer();
        container.registerStateless("GreeterBean", new TestBeans.GreeterBean());
        container.releaseFails = new IllegalStateException("container broke");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> invoke(transactions, container, new InvocationRegistry(), rollbackOnly(), true, "greet",
                        new String[] { String.class.getName(), int.class.getName() }, "Ada", 1));

        assertEquals(List.of("recreateRollbackOnly(" + Xids.key(CARRIED) + ",0,greet,key=true)",
                "releaseRollbackOnly(" + Xids.key(CARRIED) + ")"), transactions.calls);
    }

    @Test
    @DisplayName("a release that throws is logged, detaches the thread and leaves the response alone")
    void aFailingReleaseDetachesAndKeepsTheResponse() throws IOException {
        FakeTransactions transactions = new FakeTransactions();
        transactions.releaseRollbackOnlyThrows = new IllegalStateException("release broke");

        Loopback.InMemoryExchange exchange = invoke(transactions, rollbackOnly(), true);

        assertEquals(Protocol.SC_OK, exchange.toResponse().status());
        assertEquals(List.of("recreateRollbackOnly(" + Xids.key(CARRIED) + ",0,greet,key=true)",
                "releaseRollbackOnly(" + Xids.key(CARRIED) + ")", "detach"), transactions.calls);
    }

    @Test
    @DisplayName("guard: a cancelled call that holds a rollback-only handle releases it")
    void aCancelledCallReleasesItsHandle() throws IOException {
        FakeTransactions transactions = new FakeTransactions();
        InvocationRegistry registry = new InvocationRegistry();
        transactions.afterRecreateRollbackOnly = () -> registry.cancel("inv-1", false);
        FakeContainer container = new FakeContainer();
        container.registerStateless("GreeterBean", new TestBeans.GreeterBean());
        Map<String, String> headers = new java.util.HashMap<>(rollbackOnly());
        headers.put(Protocol.H_INVOCATION_ID, "inv-1");

        Loopback.InMemoryExchange exchange = invoke(transactions, container, registry, headers, true, "greet",
                new String[] { String.class.getName(), int.class.getName() }, "Ada", 1);

        assertEquals(Protocol.SC_CANCELLED, exchange.toResponse().status());
        assertEquals(List.of("recreateRollbackOnly(" + Xids.key(CARRIED) + ",0,greet,key=true)",
                "releaseRollbackOnly(" + Xids.key(CARRIED) + ")"), transactions.calls);
    }

    @Test
    @DisplayName("a refused timeout on a call with no branch detaches the thread once, not twice")
    void aBadTimeoutWithNoBranchDetachesOnce() throws IOException {
        FakeTransactions transactions = new FakeTransactions();
        Map<String, String> headers = Map.of(Protocol.H_TXN_ROLLBACK_ONLY, "true", Protocol.H_TXN_TIMEOUT, "-1");

        Loopback.InMemoryExchange exchange = invoke(transactions, headers, false);

        assertEquals(Protocol.SC_BAD_REQUEST, exchange.toResponse().status());
        assertEquals(List.of("detach"), transactions.calls);
    }
}
