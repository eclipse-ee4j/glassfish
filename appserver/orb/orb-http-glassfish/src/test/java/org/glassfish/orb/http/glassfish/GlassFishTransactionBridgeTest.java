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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.glassfish.orb.http.protocol.Xids;
import org.glassfish.orb.http.server.TransactionBridge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What the bridge does when there is no transaction manager behind it.
 * <p>
 * Injected at runtime and absent here, which is the same shape as a server
 * whose transaction service failed to start. Every operation has to fail as a
 * transaction failure the caller can act on - an XA error code and a sentence -
 * rather than as a null dereference that reaches the client as a generic
 * server error.
 */
class GlassFishTransactionBridgeTest {

    private static final Xid XID = new Xids.SimpleXid(1, new byte[] { 1, 2 }, new byte[] { 3 });

    private static final Xid OWN = new Xids.SimpleXid(GlassFishTransactionBridge.formatId(),
            new byte[] { 1, 2 }, new byte[] { 3 });

    /** A branch minted by a client's own transaction manager - GlassFish's JavaEEXid format, say. */
    private static final Xid CLIENT = new Xids.SimpleXid(987654321, new byte[] { 7, 7 }, new byte[] { 1 });

    private final List<String> terminatorCalls = new ArrayList<>();
    private final List<String> managerCalls = new ArrayList<>();
    private GlassFishTransactionBridge bridge = new GlassFishTransactionBridge();

    @Test
    @DisplayName("with no manager, driving a branch fails as a transaction failure")
    void theXaFamilyFailsCleanly() {
        assertTransactionFailure(() -> bridge.prepare(XID));
        assertTransactionFailure(() -> bridge.commit(XID, true));
        assertTransactionFailure(() -> bridge.rollback(XID));
        assertTransactionFailure(() -> bridge.forget(XID));
        assertTransactionFailure(() -> bridge.recover(0));
    }

    @Test
    @DisplayName("with no manager, importing and beginning fail rather than pretending")
    void theImportAndBeginFailCleanly() {
        assertTransactionFailure(() -> bridge.recreate(XID, 0));
        // begin returning an xid here would hand the caller a transaction
        // nothing can ever commit.
        assertTransactionFailure(() -> bridge.begin(30));
    }

    @Test
    @DisplayName("the failure names an XA error code, which is what a coordinator reads")
    void theErrorCodeIsUsable() {
        TransactionBridge.TransactionException thrown = assertThrows(
                TransactionBridge.TransactionException.class, () -> bridge.prepare(XID));

        assertEquals(XAException.XAER_RMFAIL, thrown.errorCode());
        assertTrue(thrown.getMessage().contains("transaction manager"), thrown.getMessage());
    }

    @Test
    @DisplayName("before-completion does nothing and says nothing, by design")
    void beforeCompletionIsSilent() throws Exception {
        // The manager runs synchronizations as part of preparing the branch.
        // Doing it here as well would run them twice, so this is deliberately
        // empty - and must stay safe to call with no manager at all.
        bridge.beforeCompletion(XID);
    }

    /** Wires a manager whose terminator records its calls and lists {@code listed} on recover. */
    private void wire(Xid... listed) {
        bridge = new GlassFishTransactionBridge(
                TerminatorRecorder.manager(terminatorCalls, managerCalls, listed));
    }

    @Test
    @DisplayName("a branch the client's manager minted and the server imported is driven through every XA call")
    void clientMintedXidImportedByRecreateIsDriven() throws Exception {
        wire();
        // What EjbDispatcher does around an invocation that carries the xid.
        bridge.recreate(CLIENT, 30);
        bridge.release(CLIENT);
        terminatorCalls.clear();

        bridge.prepare(CLIENT);
        bridge.commit(CLIENT, false);

        assertEquals(List.of("prepare", "commit"), terminatorCalls);
    }

    @Test
    @DisplayName("a client-minted branch is driven by one-phase commit, rollback and forget too")
    void clientMintedXidIsDrivenByTheOtherXaCalls() throws Exception {
        wire();

        bridge.commit(CLIENT, true);
        assertEquals(List.of("prepare", "commit"), terminatorCalls);

        terminatorCalls.clear();
        bridge.rollback(CLIENT);
        assertEquals(List.of("rollback"), terminatorCalls);

        terminatorCalls.clear();
        bridge.forget(CLIENT);
        assertEquals(List.of("forget"), terminatorCalls);
    }

    @Test
    @DisplayName("recover lists what the terminator holds, whatever the format id")
    void recoverListsAClientMintedXid() throws Exception {
        Xid other = new Xids.SimpleXid(GlassFishTransactionBridge.formatId() + 1,
                new byte[] { 9 }, new byte[] { 9 });
        wire(CLIENT, OWN, other);

        Xid[] listed = bridge.recover(0);

        assertEquals(3, listed.length, "listed: " + Arrays.toString(listed));
        assertTrue(Xids.sameXid(CLIENT, listed[0]), "listed: " + Arrays.toString(listed));
    }

    @Test
    @DisplayName("the user transaction ends refuse a branch begin() never minted, before touching the manager")
    void userTransactionEndsRefuseAForeignXid() throws Exception {
        wire();

        assertNotDriven(() -> bridge.commitUserTransaction(CLIENT));
        assertNotDriven(() -> bridge.rollbackUserTransaction(CLIENT));
        assertNotDriven(() -> bridge.commitUserTransaction(XID));
        assertNotDriven(() -> bridge.rollbackUserTransaction(XID));
    }

    private void assertNotDriven(Executable operation) {
        TransactionBridge.TransactionException thrown = assertThrows(
                TransactionBridge.TransactionException.class, operation::run);
        assertEquals(XAException.XAER_NOTA, thrown.errorCode());
        assertTrue(terminatorCalls.isEmpty(), "terminator was called: " + terminatorCalls);
        // Not even a detach: a refused caller must not disturb the thread.
        assertTrue(managerCalls.isEmpty(), "manager was called: " + managerCalls);
    }

    @Test
    @DisplayName("the user transaction ends drive a branch begin() minted")
    void userTransactionEndsDriveAnOwnXid() throws Exception {
        wire();

        bridge.commitUserTransaction(OWN);
        assertEquals(List.of("prepare", "commit"), terminatorCalls);

        terminatorCalls.clear();
        bridge.rollbackUserTransaction(OWN);
        assertEquals(List.of("rollback"), terminatorCalls);
    }

    @Test
    @DisplayName("a branch of this transport's own format is driven by the XA calls")
    void ownXidIsDriven() throws Exception {
        wire();

        bridge.rollback(OWN);
        bridge.forget(OWN);
        bridge.commit(OWN, false);
        bridge.prepare(OWN);

        assertEquals(List.of("rollback", "forget", "commit", "prepare"), terminatorCalls);
    }

    private static void assertTransactionFailure(Executable operation) {
        assertThrows(TransactionBridge.TransactionException.class, operation::run);
    }

    private interface Executable {
        void run() throws Exception;
    }
}
