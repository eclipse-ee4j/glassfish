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


import java.util.ArrayList;
import java.util.List;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.glassfish.orb.http.client.ClientConfiguration;
import org.glassfish.orb.http.client.ClientTransactionContext;
import org.glassfish.orb.http.client.EjbLocator;
import org.glassfish.orb.http.client.HttpEjbClient;
import org.glassfish.orb.http.client.HttpXAResource;
import org.glassfish.orb.http.client.JdkHttpTransport;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Xids;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Two servers, one transaction, over real connections.
 *
 * <p>{@link HttpTransactionRoundTripTest} checks that each operation does what
 * it says. This checks the thing a coordinator actually needs, which is that
 * the operations compose into two-phase commit across more than one endpoint -
 * including the outcomes that are not success, since those are the ones that
 * decide whether a distributed transaction is trustworthy.
 *
 * <p>The coordinator here is a dozen lines in the test rather than a real
 * transaction manager. That is the point: {@link HttpXAResource} is an
 * ordinary {@code XAResource}, so anything that can drive one can drive it.
 */
class XaTwoPhaseScenarioTest {

    /** One participating server. */
    private static final class Node implements AutoCloseable {

        final FakeTransactions transactions = new FakeTransactions();
        final RealHttpServer server;
        final HttpEjbClient client;
        final HttpXAResource resource;
        private final JdkHttpTransport transport;

        Node(String name) throws Exception {
            FakeContainer container = new FakeContainer();
            container.registerStateless("GreeterBean", new TestBeans.GreeterBean());

            EjbDispatcher ejb = new EjbDispatcher(container, SecurityBridge.NONE, transactions,
                    new JavaSerializationMarshaller(), new InvocationRegistry(),
                    new SessionAffinity(name));
            this.server = new RealHttpServer(ejb,
                    new NamingDispatcher(new HttpRoundTripTest.FakeNaming()),
                    new AffinityDispatcher(new SessionAffinity(name)),
                    new TransactionDispatcher(transactions));

            ClientConfiguration config = ClientConfiguration.builder(server.baseUri()).build();
            this.transport = new JdkHttpTransport(config);
            this.client = new HttpEjbClient(config, transport, new JavaSerializationMarshaller(),
                    JavaSerializationMarshaller.defaultFilter());
            this.resource = new HttpXAResource(config, transport);
        }

        TestBeans.Greeter greeter() {
            return client.createProxy(TestBeans.Greeter.class,
                    new EjbLocator("myapp", "mymodule", null, "GreeterBean"));
        }

        @Override
        public void close() {
            transport.close();
            server.close();
        }
    }

    private static final byte[] GTRID = { 9, 8, 7, 6, 5, 4, 3, 2 };

    private Node first;
    private Node second;

    @BeforeEach
    void setUp() throws Exception {
        first = new Node("node-1");
        second = new Node("node-2");
    }

    @AfterEach
    void tearDown() {
        ClientTransactionContext.disassociate();
        if (first != null) {
            first.close();
        }
        if (second != null) {
            second.close();
        }
    }

    private static Xid branch(int index) {
        return new Xids.SimpleXid(0x47465348, GTRID, new byte[] { (byte) index });
    }

    /** Does work on a node inside its branch, the way a container would. */
    private static void doWorkIn(Node node, Xid xid) throws XAException {
        node.resource.start(xid, XAResource.TMNOFLAGS);
        try {
            assertEquals("Ada", node.greeter().greet("Ada", 1));
        } finally {
            node.resource.end(xid, XAResource.TMSUCCESS);
        }
    }

    /**
     * The coordinator: collect votes, then commit the branches that voted to,
     * or roll everything back if any of them refused.
     *
     * @return the branches that were committed
     */
    private static List<Xid> twoPhaseCommit(List<Node> nodes, List<Xid> branches) throws XAException {
        List<Xid> committed = new ArrayList<>();
        List<Xid> toCommit = new ArrayList<>();
        List<Node> owners = new ArrayList<>();

        try {
            for (int i = 0; i < nodes.size(); i++) {
                int vote = nodes.get(i).resource.prepare(branches.get(i));
                if (vote == XAResource.XA_OK) {
                    toCommit.add(branches.get(i));
                    owners.add(nodes.get(i));
                }
                // XA_RDONLY drops out here: the branch has nothing to commit
                // and must not be contacted again.
            }
        } catch (XAException prepareFailed) {
            for (int i = 0; i < nodes.size(); i++) {
                try {
                    nodes.get(i).resource.rollback(branches.get(i));
                } catch (XAException ignored) {
                    // A branch that is already gone is not a reason to stop
                    // rolling back the others.
                }
            }
            throw prepareFailed;
        }

        for (int i = 0; i < toCommit.size(); i++) {
            owners.get(i).resource.commit(toCommit.get(i), false);
            committed.add(toCommit.get(i));
        }
        return committed;
    }

    // ---- the scenarios ------------------------------------------------------

    @Test
    @DisplayName("one transaction, two servers, both commit")
    void twoBranchesCommitTogether() throws Exception {
        Xid one = branch(1);
        Xid two = branch(2);

        doWorkIn(first, one);
        doWorkIn(second, two);

        List<Xid> committed = twoPhaseCommit(List.of(first, second), List.of(one, two));

        assertEquals(2, committed.size());
        assertTrue(first.transactions.calls.contains("commit(onePhase=false)"));
        assertTrue(second.transactions.calls.contains("commit(onePhase=false)"));
        assertTrue(first.transactions.live.isEmpty(), "a committed branch must be resolved");
        assertTrue(second.transactions.live.isEmpty());
    }

    @Test
    @DisplayName("the work really ran inside the branch on each server")
    void eachServerImportedItsOwnBranch() throws Exception {
        Xid one = branch(1);
        Xid two = branch(2);

        doWorkIn(first, one);
        doWorkIn(second, two);

        assertTrue(first.transactions.calls.contains("recreate(" + Xids.key(one) + ",0)"));
        assertTrue(first.transactions.calls.contains("release(" + Xids.key(one) + ")"));
        assertFalse(first.transactions.calls.contains("recreate(" + Xids.key(two) + ",0)"),
                "a branch must not leak to the wrong server");
        assertTrue(second.transactions.calls.contains("recreate(" + Xids.key(two) + ",0)"));
    }

    @Test
    @DisplayName("one branch refusing to prepare rolls the whole transaction back")
    void aRefusedPrepareRollsEveryoneBack() throws Exception {
        Xid one = branch(1);
        Xid two = branch(2);
        doWorkIn(first, one);
        doWorkIn(second, two);

        second.transactions.failWith = XAException.XA_RBROLLBACK;

        XAException refused = assertThrows(XAException.class,
                () -> twoPhaseCommit(List.of(first, second), List.of(one, two)));
        assertEquals(XAException.XA_RBROLLBACK, refused.errorCode,
                "the code has to survive: it is what tells a coordinator to roll back rather than retry");

        assertTrue(first.transactions.calls.contains("rollback"),
                "the branch that voted yes must be rolled back too");
        assertFalse(first.transactions.calls.contains("commit(onePhase=false)"));
    }

    @Test
    @DisplayName("a read-only branch votes read-only and is never committed")
    void aReadOnlyBranchDropsOut() throws Exception {
        Xid one = branch(1);
        Xid two = branch(2);
        doWorkIn(first, one);
        doWorkIn(second, two);

        second.transactions.voteReadOnly = true;

        List<Xid> committed = twoPhaseCommit(List.of(first, second), List.of(one, two));

        assertEquals(1, committed.size());
        assertTrue(first.transactions.calls.contains("commit(onePhase=false)"));
        assertFalse(second.transactions.calls.contains("commit(onePhase=false)"),
                "committing a branch that voted read-only would be a protocol violation");
    }

    @Test
    @DisplayName("a single branch takes the one-phase shortcut")
    void onePhaseCommitForASingleBranch() throws Exception {
        Xid only = branch(1);
        doWorkIn(first, only);

        first.resource.commit(only, true);

        assertTrue(first.transactions.calls.contains("commit(onePhase=true)"));
        assertFalse(first.transactions.calls.contains("prepare"),
                "one-phase exists precisely to skip the prepare round trip");
    }

    @Test
    void rollbackAfterPrepareIsHonoured() throws Exception {
        Xid one = branch(1);
        doWorkIn(first, one);

        assertEquals(XAResource.XA_OK, first.resource.prepare(one));
        first.resource.rollback(one);

        assertTrue(first.transactions.calls.contains("rollback"));
        assertTrue(first.transactions.live.isEmpty());
    }

    @Test
    @DisplayName("prepared branches are listed for recovery, so an interrupted commit can be finished")
    void preparedBranchesAreRecoverable() throws Exception {
        Xid one = branch(1);
        Xid two = branch(2);
        doWorkIn(first, one);
        doWorkIn(second, two);

        first.resource.prepare(one);
        second.resource.prepare(two);

        Xid[] recovered = first.resource.recover(XAResource.TMSTARTRSCAN);
        assertEquals(1, recovered.length, "each server reports only its own branches");
        assertTrue(Xids.sameXid(one, recovered[0]));

        // And a recovered branch can still be resolved.
        first.resource.commit(recovered[0], false);
        assertTrue(first.transactions.live.isEmpty());
    }

    @Test
    @DisplayName("an unknown branch is XAER_NOTA, which a coordinator treats as already resolved")
    void anUnknownBranchIsReportedAsSuch() {
        XAException thrown = assertThrows(XAException.class,
                () -> first.resource.prepare(branch(99)));
        assertEquals(XAException.XAER_NOTA, thrown.errorCode);
    }

    @Test
    @DisplayName("two endpoints are not the same resource manager")
    void theTwoNodesAreDistinctResourceManagers() {
        assertFalse(first.resource.isSameRM(second.resource),
                "joining branches across different servers would commit one and lose the other");
        assertTrue(first.resource.isSameRM(first.resource));
    }
}
