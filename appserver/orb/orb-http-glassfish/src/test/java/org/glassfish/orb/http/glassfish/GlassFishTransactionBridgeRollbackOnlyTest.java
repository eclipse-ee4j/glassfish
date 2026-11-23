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
package org.glassfish.orb.http.glassfish;

import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;

import java.lang.reflect.Method;
import java.util.List;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.glassfish.orb.http.protocol.EjbKey;
import org.glassfish.orb.http.protocol.Xids;
import org.glassfish.orb.http.server.TransactionBridge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The rollback-only inflow of the bridge, over a manager that keeps one
 * transaction per thread and records what it is asked.
 *
 * <p>What this does not cover: the real {@code JavaEETransactionManager}
 * (that {@code begin} followed by {@code setRollbackOnly} really leaves the
 * thread in a state the container's {@code useClientTx} refuses), that
 * {@code isNullTransaction()} is false on a Grizzly request thread during the
 * call, and the container applying the bean's attribute. Those need a running
 * server.
 */
class GlassFishTransactionBridgeRollbackOnlyTest {

    private static final Xid CLIENT = new Xids.SimpleXid(987654321, new byte[] { 7, 7 }, new byte[] { 1 });
    private static final EjbKey KEY = new EjbKey(11, new byte[] { 0, 0, 0, 1 });

    private final FakeTransactionManager fake = new FakeTransactionManager();
    private final GlassFishTransactionBridge bridge = new GlassFishTransactionBridge(fake.manager);

    private static Method method() throws NoSuchMethodException {
        return Object.class.getMethod("toString");
    }

    @Test
    @DisplayName("without an xid it begins a transaction with the caller's timeout and marks it rollback-only")
    void aCallerWithNoBranchGetsALocalRollbackOnlyTransaction() throws Exception {
        Object handle = bridge.recreateRollbackOnly(null, 30, KEY, method());

        assertNotNull(handle);
        assertTrue(fake.calls.contains("begin(30)"), "calls: " + fake.calls);
        assertTrue(fake.calls.indexOf("begin(30)") < fake.calls.indexOf("setRollbackOnly"), "calls: " + fake.calls);
        assertNotNull(fake.current, "the transaction is on the thread for the container to see");
        // Per thread, so it would outlive the request on a pooled thread.
        assertTrue(fake.calls.stream().noneMatch(c -> c.startsWith("setTransactionTimeout")), "calls: " + fake.calls);
        assertTrue(fake.rolledBack.isEmpty(), "rolled back before the call ran: " + fake.rolledBack);
    }

    @Test
    @DisplayName("with an xid it still begins a local transaction and does not recreate the caller's branch")
    void aCallerWithABranchIsNotResurrected() throws Exception {
        Object handle = bridge.recreateRollbackOnly(CLIENT, 0, KEY, method());

        assertNotNull(handle);
        assertTrue(fake.calls.contains("begin(0)"), "calls: " + fake.calls);
        assertTrue(fake.calls.stream().noneMatch(c -> c.startsWith("recreate")), "calls: " + fake.calls);
        assertNotNull(fake.current);
    }

    @Test
    @DisplayName("a failure to mark rolls the new transaction back, leaves the thread empty and throws")
    void aFailureToMarkLeavesNothingBehind() throws Exception {
        fake.failures.put("setRollbackOnly", new SystemException("cannot mark"));

        TransactionBridge.TransactionException thrown = assertThrows(TransactionBridge.TransactionException.class,
                () -> bridge.recreateRollbackOnly(null, 30, KEY, method()));

        assertEquals(XAException.XAER_RMERR, thrown.errorCode());
        assertNull(fake.current, "left on the thread");
        assertEquals(1, fake.rolledBack.size(), "rolled back: " + fake.rolledBack);
        assertSame(fake.begun.iterator().next(), fake.rolledBack.get(0));
    }

    @Test
    @DisplayName("a failure to begin throws and leaves the thread empty")
    void aFailureToBeginLeavesNothingBehind() throws Exception {
        fake.failures.put("begin", new SystemException("cannot begin"));

        TransactionBridge.TransactionException thrown = assertThrows(TransactionBridge.TransactionException.class,
                () -> bridge.recreateRollbackOnly(CLIENT, 30, KEY, method()));

        assertEquals(XAException.XAER_RMERR, thrown.errorCode());
        assertNull(fake.current);
        assertTrue(fake.rolledBack.isEmpty());
    }

    @Test
    @DisplayName("with no manager it refuses with a manager failure rather than a null dereference")
    void noManagerIsRefused() throws Exception {
        TransactionBridge.TransactionException thrown = assertThrows(TransactionBridge.TransactionException.class,
                () -> new GlassFishTransactionBridge().recreateRollbackOnly(null, 0, KEY, method()));

        assertEquals(XAException.XAER_RMFAIL, thrown.errorCode());
    }

    @Test
    @DisplayName("releasing rolls back the transaction that is on the thread, and never commits")
    void releaseRollsTheLocalTransactionBack() throws Exception {
        Object handle = bridge.recreateRollbackOnly(null, 30, KEY, method());
        Transaction tx = fake.current;

        bridge.releaseRollbackOnly(handle);

        assertEquals(List.of(tx), fake.rolledBack);
        assertNull(fake.current, "left on the thread");
        assertTrue(fake.calls.stream().noneMatch(c -> c.startsWith("commit")), "calls: " + fake.calls);
    }

    @Test
    @DisplayName("releasing when something else is on the thread suspends it and rolls back only the handle's transaction")
    void releaseNeverRollsBackAnotherTransaction() throws Exception {
        Object handle = bridge.recreateRollbackOnly(null, 30, KEY, method());
        Transaction mine = fake.current;
        // What a bean that began its own transaction and did not end it leaves.
        Transaction other = fake.newTransaction();
        fake.current = other;
        fake.calls.clear();

        bridge.releaseRollbackOnly(handle);

        assertEquals(List.of(mine), fake.rolledBack, "rolled back: " + fake.rolledBack);
        assertTrue(fake.calls.indexOf("suspend") < fake.calls.indexOf("resume(" + mine + ")"),
                "calls: " + fake.calls);
        assertNull(fake.current, "left on the thread");
    }

    @Test
    @DisplayName("releasing a transaction that is already over is not a failure")
    void releaseAfterTheTransactionIsOver() throws Exception {
        Object handle = bridge.recreateRollbackOnly(null, 30, KEY, method());
        fake.current = null;
        fake.failures.put("resume", new InvalidTransactionException("completed"));

        bridge.releaseRollbackOnly(handle);

        assertTrue(fake.rolledBack.isEmpty(), "rolled back a transaction that was not on the thread");
        assertNull(fake.current);
    }

    @Test
    @DisplayName("a rollback that finds no transaction is swallowed and the thread is still detached")
    void releaseSwallowsIllegalState() throws Exception {
        Object handle = bridge.recreateRollbackOnly(null, 30, KEY, method());
        fake.failures.put("rollback", new IllegalStateException("no transaction"));

        bridge.releaseRollbackOnly(handle);

        assertNull(fake.current, "left on the thread");
    }

    @Test
    @DisplayName("a rollback that fails for another reason is reported, and the thread is still detached")
    void releaseReportsOtherFailuresAndDetaches() throws Exception {
        Object handle = bridge.recreateRollbackOnly(null, 30, KEY, method());
        fake.failures.put("rollback", new SystemException("tm is down"));

        TransactionBridge.TransactionException thrown = assertThrows(TransactionBridge.TransactionException.class,
                () -> bridge.releaseRollbackOnly(handle));

        assertEquals(XAException.XAER_RMERR, thrown.errorCode());
        assertNull(fake.current, "left on the thread");
    }

    @Test
    @DisplayName("an xid handle is released through the manager, whatever it throws, and the thread is detached")
    void anXidHandleIsReleased() throws Exception {
        Transaction stray = fake.newTransaction();
        fake.current = stray;
        fake.failures.put("release", new IllegalStateException("branch is marked rollback-only"));

        bridge.releaseRollbackOnly(CLIENT);

        assertTrue(fake.calls.stream().anyMatch(c -> c.startsWith("release(")), "calls: " + fake.calls);
        assertNull(fake.current, "left on the thread");
    }

    @Test
    @DisplayName("the handle is opaque and non-null, so the dispatcher can hand it back")
    void theHandleIsALocalRecord() throws Exception {
        Object handle = bridge.recreateRollbackOnly(null, 1, KEY, method());

        assertInstanceOf(GlassFishTransactionBridge.Local.class, handle);
        assertSame(fake.current, ((GlassFishTransactionBridge.Local) handle).tx());
    }
}
