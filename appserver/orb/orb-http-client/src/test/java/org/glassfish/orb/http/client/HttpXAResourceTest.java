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
import java.util.Map;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The resource a coordinator drives when this client is a branch.
 */
class HttpXAResourceTest {

    private static final Xid XID = new Xids.SimpleXid(131075,
            new byte[] { 1, 2, 3, 4 }, new byte[] { 9 });

    private RecordingTransport transport;

    private HttpXAResource resource;

    private static ClientConfiguration at(String uri) {
        return ClientConfiguration.builder(URI.create(uri)).build();
    }

    @BeforeEach
    void setUp() {
        transport = new RecordingTransport();
        transport.answerAll(204, Map.of());
        resource = new HttpXAResource(at("http://localhost:8080/glassfish-services"), transport);
    }

    @AfterEach
    void tearDown() {
        ClientTransactionContext.disassociate();
    }

    @Test
    @DisplayName("start associates the branch, end releases it")
    void startAndEndBracketTheAssociation() throws Exception {
        resource.start(XID, XAResource.TMNOFLAGS);
        assertTrue(Xids.sameXid(XID, ClientTransactionContext.current()));

        resource.end(XID, XAResource.TMSUCCESS);
        assertNull(ClientTransactionContext.current());
    }

    @Test
    @DisplayName("a start flag this resource cannot honour is refused, not ignored")
    void anUnsupportedStartFlagIsRefused() {
        // Accepting a flag whose meaning we do not implement would associate
        // the branch under terms the coordinator believes and we do not keep.
        XAException thrown = assertThrows(XAException.class,
                () -> resource.start(XID, XAResource.TMFAIL));
        assertEquals(XAException.XAER_INVAL, thrown.errorCode);
        assertNull(ClientTransactionContext.current());
    }

    @Test
    @DisplayName("prepare asks for before-completion first, then prepares")
    void prepareRunsBeforeCompletionFirst() throws Exception {
        resource.prepare(XID);

        // Order matters: before-completion is where a bean flushes its work,
        // and preparing before that would vote on a state that is not final.
        assertEquals(2, transport.calls.size());
        assertTrue(transport.calls.get(0).contains(TxRoutes.OP_BEFORE_COMPLETION), transport.calls.get(0));
        assertTrue(transport.calls.get(1).contains(TxRoutes.OP_PREPARE), transport.calls.get(1));
    }

    @Test
    @DisplayName("one-phase commit is distinguished from two-phase on the wire")
    void onePhaseIsCarried() throws Exception {
        resource.commit(XID, true);
        assertTrue(transport.calls.get(0).contains(TxRoutes.PARAM_ONE_PHASE + "=true"),
                transport.calls.get(0));

        transport.calls.clear();
        resource.commit(XID, false);
        assertFalse(transport.calls.get(0).contains(TxRoutes.PARAM_ONE_PHASE + "=true"),
                transport.calls.get(0));
    }

    @Test
    @DisplayName("two resources are the same manager only when they address the same endpoint")
    void isSameRmComparesTheEndpoint() {
        HttpXAResource same = new HttpXAResource(
                at("http://localhost:8080/glassfish-services"), transport);
        HttpXAResource elsewhere = new HttpXAResource(
                at("http://other:8080/glassfish-services"), transport);

        assertTrue(resource.isSameRM(same));
        // Being wrong permissively here lets a coordinator join branches that
        // are not the same resource manager, which is a correctness bug and
        // not a missed optimisation.
        assertFalse(resource.isSameRM(elsewhere));
        assertFalse(resource.isSameRM(new NotOurs()));
    }

    @Test
    @DisplayName("the timeout is remembered and reported")
    void theTimeoutRoundTrips() {
        assertTrue(resource.setTransactionTimeout(30));
        assertEquals(30, resource.getTransactionTimeout());

        resource.setTransactionTimeout(-1);
        assertEquals(0, resource.getTransactionTimeout(), "a negative timeout means none");
    }

    private static final class NotOurs implements XAResource {
        @Override
        public void commit(Xid xid, boolean onePhase) {
        }

        @Override
        public void end(Xid xid, int flags) {
        }

        @Override
        public void forget(Xid xid) {
        }

        @Override
        public int getTransactionTimeout() {
            return 0;
        }

        @Override
        public boolean isSameRM(XAResource xares) {
            return false;
        }

        @Override
        public int prepare(Xid xid) {
            return XA_OK;
        }

        @Override
        public Xid[] recover(int flag) {
            return new Xid[0];
        }

        @Override
        public void rollback(Xid xid) {
        }

        @Override
        public boolean setTransactionTimeout(int seconds) {
            return false;
        }

        @Override
        public void start(Xid xid, int flags) {
        }
    }
}
