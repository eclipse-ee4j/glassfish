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

    private final GlassFishTransactionBridge bridge = new GlassFishTransactionBridge();

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

    private static void assertTransactionFailure(Executable operation) {
        assertThrows(TransactionBridge.TransactionException.class, operation::run);
    }

    private interface Executable {
        void run() throws Exception;
    }
}
