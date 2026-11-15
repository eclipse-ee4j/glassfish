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
import jakarta.transaction.Synchronization;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.InitialContextFactory;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.glassfish.orb.http.protocol.Xids;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A transaction this client did not start must still travel.
 * <p>
 * This is the case an application server makes when it calls another one, and
 * the one an application actually writes: nothing touches this transport's own
 * UserTransaction, the container began the transaction, and the remote call
 * has to become part of it rather than run beside it.
 */
class AmbientTransactionTest {

    static final Map<String, Object> BOUND = new HashMap<>();

    static Xid started;

    static int branches;

    /** Initial contexts the fake JNDI handed out and was given back. */
    static final AtomicInteger OPENED = new AtomicInteger();

    static final AtomicInteger CLOSED = new AtomicInteger();

    private ClientConfiguration config;

    private RecordingTransport transport;

    @BeforeEach
    void setUp() {
        BOUND.clear();
        started = null;
        OPENED.set(0);
        CLOSED.set(0);
        AmbientTransaction.forget();
        ClientTransactionContext.disassociate();
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, Factory.class.getName());

        config = ClientConfiguration.builder(URI.create("http://localhost:8080/glassfish-services")).build();
        transport = new RecordingTransport();
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
        ClientTransactionContext.disassociate();
        AmbientTransaction.forget();
    }

    @Test
    @DisplayName("a container's transaction is joined, so the call carries it")
    void anAmbientTransactionIsJoined() {
        FakeTransaction transaction = new FakeTransaction();
        BOUND.put("java:comp/TransactionSynchronizationRegistry", new FakeRegistry(transaction));
        BOUND.put("java:appserver/TransactionManager", new FakeManager(transaction));

        AmbientTransaction.join(config, transport);

        // Enlisted, the caller's manager started the branch, and from here on
        // every invocation carries it.
        assertEquals(1, transaction.enlisted, "the resource was not enlisted");
        assertNotNull(started, "the manager never started the branch");
        assertNotNull(ClientTransactionContext.current());
    }

    @Test
    @DisplayName("the same endpoint is joined once per transaction, not once per call")
    void joiningIsIdempotent() {
        FakeTransaction transaction = new FakeTransaction();
        FakeRegistry registry = new FakeRegistry(transaction);
        BOUND.put("java:comp/TransactionSynchronizationRegistry", registry);
        BOUND.put("java:appserver/TransactionManager", new FakeManager(transaction));

        AmbientTransaction.join(config, transport);
        ClientTransactionContext.disassociate();
        AmbientTransaction.join(config, transport);

        // Enlisting twice would make one server two branches of the same
        // transaction, and the coordinator would prepare it twice.
        assertEquals(1, transaction.enlisted);
    }

    @Test
    @DisplayName("a plain client with no transaction manager is left alone")
    void noManagerMeansNoTransaction() {
        // Nothing bound: the ordinary standalone client.
        AmbientTransaction.join(config, transport);

        assertNull(ClientTransactionContext.current());
        assertEquals(0, transport.calls.size(), "joining must not talk to the server");
    }

    @Test
    @DisplayName("a transaction this client already began is not joined again")
    void anOwnTransactionIsLeftAsItIs() {
        Xid ours = new Xids.SimpleXid(1, new byte[] { 7 }, new byte[] { 7 });
        ClientTransactionContext.associate(ours, 0);

        FakeTransaction transaction = new FakeTransaction();
        BOUND.put("java:comp/TransactionSynchronizationRegistry", new FakeRegistry(transaction));
        BOUND.put("java:appserver/TransactionManager", new FakeManager(transaction));

        AmbientTransaction.join(config, transport);

        assertEquals(0, transaction.enlisted);
        assertEquals(ours, ClientTransactionContext.current());
    }

    @Test
    @DisplayName("a branch left behind by a transaction that has ended is not carried into the next call")
    void aStaleBranchIsDropped() {
        FakeTransaction transaction = new FakeTransaction();
        BOUND.put("java:comp/TransactionSynchronizationRegistry", new FakeRegistry(transaction));
        BOUND.put("java:appserver/TransactionManager", new FakeManager(transaction));
        AmbientTransaction.join(config, transport);
        assertNotNull(ClientTransactionContext.current(), "the branch was never associated");

        // The transaction ends, and the end that should have dropped the
        // association never reaches the resource. That is not hypothetical:
        // measured against two real servers, a call made deliberately outside
        // a transaction arrived at the far server inside one, because the
        // pooled request thread was still carrying this.
        BOUND.put("java:comp/TransactionSynchronizationRegistry", new FakeRegistry(null));
        BOUND.put("java:appserver/TransactionManager", new FakeManager(null));
        AmbientTransaction.forget();

        AmbientTransaction.join(config, transport);

        assertNull(ClientTransactionContext.current(),
                "a call outside a transaction must not carry the last one");
    }

    @Test
    @DisplayName("a transaction begun through this transport is not dropped with it")
    void ourOwnBranchSurvives() {
        // Same shape - an association, and no container transaction - but this
        // one is the application's own, begun through this transport's
        // UserTransaction. Nothing here is entitled to end it.
        Xid ours = new Xids.SimpleXid(1, new byte[] { 7 }, new byte[] { 7 });
        ClientTransactionContext.associate(ours, 0);
        BOUND.put("java:comp/TransactionSynchronizationRegistry", new FakeRegistry(null));
        BOUND.put("java:appserver/TransactionManager", new FakeManager(null));

        AmbientTransaction.join(config, transport);

        assertEquals(ours, ClientTransactionContext.current());
    }

    @Test
    @DisplayName("a thread going straight into a new transaction does not carry the last one's branch")
    void aBranchFromTheLastTransactionIsNotReused() {
        FakeTransaction first = new FakeTransaction();
        BOUND.put("java:comp/TransactionSynchronizationRegistry", new FakeRegistry(first));
        BOUND.put("java:appserver/TransactionManager", new FakeManager(first));
        AmbientTransaction.join(config, transport);
        Xid old = ClientTransactionContext.current();

        // The first transaction ends without end() reaching the resource, and
        // the same pooled thread runs a bean in a second one. Measured against
        // two real servers: the call carried the old branch, the second
        // transaction never enlisted the far server, and a rollback decided
        // there was committed here.
        FakeTransaction second = new FakeTransaction();
        BOUND.put("java:comp/TransactionSynchronizationRegistry", new FakeRegistry(second));
        BOUND.put("java:appserver/TransactionManager", new FakeManager(second));
        AmbientTransaction.forget();

        AmbientTransaction.join(config, transport);

        assertEquals(1, second.enlisted, "the new transaction must enlist the endpoint itself");
        assertEquals(started, ClientTransactionContext.current(), "the call must carry the new branch");
        assertNotEquals(Xids.key(old), Xids.key(ClientTransactionContext.current()));
    }

    @Test
    @DisplayName("two endpoints in one transaction each carry their own branch")
    void eachEndpointCarriesItsOwnBranch() {
        FakeTransaction transaction = new FakeTransaction();
        BOUND.put("java:comp/TransactionSynchronizationRegistry", new FakeRegistry(transaction));
        BOUND.put("java:appserver/TransactionManager", new FakeManager(transaction));
        ClientConfiguration other = ClientConfiguration
                .builder(URI.create("http://elsewhere:8080/glassfish-services")).build();

        AmbientTransaction.join(config, transport);
        Xid here = ClientTransactionContext.current();
        AmbientTransaction.join(other, transport);
        Xid there = ClientTransactionContext.current();
        AmbientTransaction.join(config, transport);

        assertEquals(2, transaction.enlisted);
        assertNotEquals(Xids.key(here), Xids.key(there));
        assertEquals(Xids.key(here), Xids.key(ClientTransactionContext.current()),
                "back on the first endpoint, the call must carry the first endpoint's branch");
    }

    @Test
    @DisplayName("a failed lookup is believed for a while, so a burst of calls makes one lookup")
    void aFailedLookupIsNotRepeatedOnEveryCall() {
        AtomicLong now = new AtomicLong();
        AmbientTransaction.clock = now::get;

        // Nothing bound, as on a thread that cannot see the component environment.
        for (int i = 0; i < 20; i++) {
            AmbientTransaction.join(config, transport);
            now.addAndGet(1_000_000_000L);
        }

        // Two registry names and the four manager names, once.
        assertEquals(6, OPENED.get(), "every call looked the registry up again");
    }

    @Test
    @DisplayName("a failed lookup is not tried again before the delay is over")
    void aFailedLookupIsNotRetriedBeforeTheDelay() {
        AtomicLong now = new AtomicLong();
        AmbientTransaction.clock = now::get;
        AmbientTransaction.join(config, transport);
        assertEquals(6, OPENED.get(), "both registry names and the manager names were tried");

        FakeTransaction transaction = new FakeTransaction();
        BOUND.put("java:comp/TransactionSynchronizationRegistry", new FakeRegistry(transaction));
        BOUND.put("java:appserver/TransactionManager", new FakeManager(transaction));

        // Bound now, but the earlier miss is still believed.
        now.addAndGet(29_000_000_000L);
        AmbientTransaction.join(config, transport);
        assertEquals(0, transaction.enlisted, "retried before the delay was over");
    }

    @Test
    @DisplayName("a failed lookup is tried again once the delay is over, and joining then works")
    void aFailedLookupIsRetriedOnceTheDelayIsOver() {
        AtomicLong now = new AtomicLong();
        AmbientTransaction.clock = now::get;
        AmbientTransaction.join(config, transport);

        FakeTransaction transaction = new FakeTransaction();
        BOUND.put("java:comp/TransactionSynchronizationRegistry", new FakeRegistry(transaction));
        BOUND.put("java:appserver/TransactionManager", new FakeManager(transaction));

        now.addAndGet(31_000_000_000L);
        AmbientTransaction.join(config, transport);
        assertEquals(1, transaction.enlisted, "never retried: one failed lookup disabled joining");
    }

    private static void runWithLoader(ClassLoader loader, Runnable work) throws Exception {
        Throwable[] failure = new Throwable[1];
        Thread thread = new Thread(() -> {
            try {
                work.run();
            } catch (Throwable t) {
                failure[0] = t;
            }
        });
        thread.setContextClassLoader(loader);
        thread.start();
        thread.join();
        if (failure[0] != null) {
            throw new AssertionError(failure[0]);
        }
    }

    @Test
    @DisplayName("one thread's failed lookup does not stop a thread with another class loader from joining")
    void aMissIsPerContextClassLoader() throws Exception {
        AtomicLong now = new AtomicLong();
        AmbientTransaction.clock = now::get;
        try (URLClassLoader unmanaged = new URLClassLoader(new URL[0]);
                URLClassLoader managed = new URLClassLoader(new URL[0])) {
            // An unmanaged thread: it cannot see the registry.
            runWithLoader(unmanaged, () -> AmbientTransaction.join(config, transport));

            FakeTransaction transaction = new FakeTransaction();
            BOUND.put("java:comp/TransactionSynchronizationRegistry", new FakeRegistry(transaction));
            BOUND.put("java:appserver/TransactionManager", new FakeManager(transaction));

            // A managed thread, inside the first one's backoff window.
            runWithLoader(managed, () -> AmbientTransaction.join(config, transport));

            assertEquals(1, transaction.enlisted, "another thread's miss stopped this one from joining");
        }
    }

    @Test
    @DisplayName("the server-wide registry name is tried first, so a thread without java:comp can join")
    void theServerWideNameIsLookedUpFirst() {
        FakeTransaction transaction = new FakeTransaction();
        BOUND.put("java:appserver/TransactionSynchronizationRegistry", new FakeRegistry(transaction));
        BOUND.put("java:appserver/TransactionManager", new FakeManager(transaction));

        AmbientTransaction.join(config, transport);

        assertEquals(1, transaction.enlisted);
        assertEquals(2, OPENED.get(), "the first name should have been enough for the registry");
    }

    @Test
    @DisplayName("a thread in a transaction is refused when only the registry is missing, even from the same loader")
    void aThreadInATransactionIsRefusedWhenOnlyTheRegistryIsMissing() {
        AtomicLong now = new AtomicLong();
        AmbientTransaction.clock = now::get;
        // The server-wide manager name works without a component environment;
        // the registry is not bound for this thread's loader.
        FakeManager manager = new FakeManager(null);
        BOUND.put("java:appserver/TransactionManager", manager);

        // First a thread with no transaction: a miss, and the call proceeds.
        AmbientTransaction.join(config, transport);
        assertNull(ClientTransactionContext.current());

        // Then a thread of the same application, inside a transaction, within
        // the delay: it must not be sent without it.
        FakeTransaction active = new FakeTransaction();
        manager.transaction = active;
        assertThrows(EJBException.class, () -> AmbientTransaction.join(config, transport),
                "the call would have been sent outside the caller's transaction");
        assertEquals(0, active.enlisted);
    }

    @Test
    @DisplayName("guard: a thread with no transaction is not affected when only the registry is missing")
    void aThreadWithoutATransactionProceedsWhenOnlyTheRegistryIsMissing() {
        BOUND.put("java:appserver/TransactionManager", new FakeManager(null));

        assertEquals(false, AmbientTransaction.join(config, transport));
        assertEquals(false, AmbientTransaction.join(config, transport));
    }

    private static final class Captured extends java.util.logging.Handler {
        final java.util.List<java.util.logging.LogRecord> warnings = new java.util.ArrayList<>();

        @Override
        public void publish(java.util.logging.LogRecord record) {
            if (record.getLevel().intValue() >= java.util.logging.Level.WARNING.intValue()) {
                warnings.add(record);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    }

    @Test
    @DisplayName("guard: a miss is reported once at WARNING when no name worked, and again never")
    void aMissIsReportedOnceAtWarning() {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AmbientTransaction.class.getName());
        Captured captured = new Captured();
        logger.addHandler(captured);
        try {
            AtomicLong now = new AtomicLong();
            AmbientTransaction.clock = now::get;

            AmbientTransaction.join(config, transport);
            now.addAndGet(31_000_000_000L);
            AmbientTransaction.join(config, transport);
            now.addAndGet(31_000_000_000L);
            AmbientTransaction.join(config, transport);

            assertEquals(1, captured.warnings.size(), "the miss was reported " + captured.warnings.size() + " times");
        } finally {
            logger.removeHandler(captured);
        }
    }

    @Test
    @DisplayName("guard: nothing is reported at WARNING when a later registry name worked")
    void aHitOnTheSecondNameIsNotAMiss() {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AmbientTransaction.class.getName());
        Captured captured = new Captured();
        logger.addHandler(captured);
        try {
            FakeTransaction transaction = new FakeTransaction();
            // java:appserver misses, java:comp answers.
            BOUND.put("java:comp/TransactionSynchronizationRegistry", new FakeRegistry(transaction));
            BOUND.put("java:appserver/TransactionManager", new FakeManager(transaction));

            AmbientTransaction.join(config, transport);

            assertEquals(1, transaction.enlisted);
            assertEquals(0, captured.warnings.size(), "a miss on the first name was reported");
        } finally {
            logger.removeHandler(captured);
        }
    }

    @Test
    @DisplayName("every initial context the lookup opens is closed")
    void lookupsCloseTheirContext() {
        FakeTransaction transaction = new FakeTransaction();
        BOUND.put("java:comp/TransactionSynchronizationRegistry", new FakeRegistry(transaction));
        BOUND.put("java:appserver/TransactionManager", new FakeManager(transaction));

        AmbientTransaction.join(config, transport);

        assertEquals(1, transaction.enlisted);
        assertEquals(3, OPENED.get(), "java:appserver missed, java:comp found, and one for the manager");
        assertEquals(OPENED.get(), CLOSED.get(), "an initial context was left open");
    }

    @Test
    @DisplayName("a client whose JNDI is this transport does not ask the server for a transaction registry")
    void aStandaloneClientDoesNotProbeTheServer() throws Exception {
        AtomicInteger connections = new AtomicInteger();
        try (ServerSocket server = new ServerSocket(0)) {
            Thread acceptor = new Thread(() -> {
                try {
                    while (true) {
                        Socket socket = server.accept();
                        connections.incrementAndGet();
                        socket.close();
                    }
                } catch (IOException closed) {
                    // The test is over.
                }
            }, "ambient-test-acceptor");
            acceptor.setDaemon(true);
            acceptor.start();

            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, HttpInitialContextFactory.class.getName());
            System.setProperty(Context.PROVIDER_URL,
                    "http://localhost:" + server.getLocalPort() + "/glassfish-services");
            try {
                AmbientTransaction.join(config, transport);
                AmbientTransaction.join(config, transport);
            } finally {
                System.clearProperty(Context.PROVIDER_URL);
            }
        }

        // Joining would have asked the remote server, with the user's
        // credentials, for a registry that only exists inside a container.
        assertEquals(0, connections.get(), "the join probed the server");
    }

    // ---- doubles ---------------------------------------------------------

    /** Binds the names this class looks up; everything else is absent. */
    public static final class Factory implements InitialContextFactory {
        @Override
        public Context getInitialContext(Hashtable<?, ?> environment) {
            OPENED.incrementAndGet();
            return (Context) java.lang.reflect.Proxy.newProxyInstance(
                    Factory.class.getClassLoader(), new Class<?>[] { Context.class },
                    (proxy, method, args) -> {
                        if ("lookup".equals(method.getName()) && args != null && args.length == 1) {
                            Object name = args[0] instanceof Name n ? n.toString() : args[0];
                            Object bound = BOUND.get(String.valueOf(name));
                            if (bound == null) {
                                throw new javax.naming.NameNotFoundException(String.valueOf(name));
                            }
                            return bound;
                        }
                        if ("close".equals(method.getName())) {
                            CLOSED.incrementAndGet();
                            return null;
                        }
                        return null;
                    });
        }
    }

    static class FakeTransaction implements Transaction {
        int enlisted;

        /** What {@link #getStatus()} answers. */
        int status = Status.STATUS_ACTIVE;

        /** When positive, the timeout the manager gives the resource before starting its branch. */
        int timeout;

        /** When set, enlisting throws this. */
        Exception enlistFailure;

        /** Whether the branch starts before the failure is thrown, as with a manager that fails late. */
        boolean startFirst;

        private void failIfTold() throws RollbackException, jakarta.transaction.SystemException {
            if (enlistFailure instanceof RollbackException rollback) {
                throw rollback;
            }
            if (enlistFailure instanceof jakarta.transaction.SystemException system) {
                throw system;
            }
        }

        @Override
        public boolean enlistResource(XAResource resource)
                throws RollbackException, jakarta.transaction.SystemException {
            if (!startFirst) {
                failIfTold();
            }
            enlisted++;
            try {
                // What a real manager does next, and what associates the
                // branch on this side.
                // A new branch per enlistment, as a real manager mints them;
                // a fixed one would make a stale branch look current.
                Xid xid = new Xids.SimpleXid(0x4A5441, new byte[] { 1, (byte) ++branches }, new byte[] { 3 });
                if (timeout > 0) {
                    resource.setTransactionTimeout(timeout);
                }
                resource.start(xid, XAResource.TMNOFLAGS);
                started = xid;
            } catch (Exception e) {
                throw new jakarta.transaction.SystemException(e.toString());
            }
            failIfTold();
            return true;
        }

        @Override
        public void commit() {
        }

        @Override
        public boolean delistResource(XAResource resource, int flag) {
            return true;
        }

        @Override
        public int getStatus() {
            return status;
        }

        @Override
        public void registerSynchronization(Synchronization sync) {
        }

        @Override
        public void rollback() {
        }

        @Override
        public void setRollbackOnly() {
        }
    }

    static final class FakeManager implements TransactionManager {
        /** What the manager reports for the calling thread; tests change it between calls. */
        Transaction transaction;

        FakeManager(Transaction transaction) {
            this.transaction = transaction;
        }

        @Override
        public Transaction getTransaction() {
            return transaction;
        }

        @Override
        public void begin() {
        }

        @Override
        public void commit() {
        }

        @Override
        public int getStatus() {
            return Status.STATUS_ACTIVE;
        }

        @Override
        public void resume(Transaction tx) {
        }

        @Override
        public void rollback() {
        }

        @Override
        public void setRollbackOnly() {
        }

        @Override
        public void setTransactionTimeout(int seconds) {
        }

        @Override
        public Transaction suspend() {
            return transaction;
        }
    }

    static final class FakeRegistry implements TransactionSynchronizationRegistry {
        private final Map<Object, Object> resources = new HashMap<>();
        private final Transaction transaction;

        FakeRegistry(Transaction transaction) {
            this.transaction = transaction;
        }

        @Override
        public Object getTransactionKey() {
            return transaction;
        }

        @Override
        public Object getResource(Object key) {
            return resources.get(key);
        }

        @Override
        public void putResource(Object key, Object value) {
            resources.put(key, value);
        }

        @Override
        public boolean getRollbackOnly() {
            return false;
        }

        @Override
        public int getTransactionStatus() {
            try {
                return transaction == null ? Status.STATUS_NO_TRANSACTION : transaction.getStatus();
            } catch (jakarta.transaction.SystemException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void registerInterposedSynchronization(Synchronization sync) {
        }

        @Override
        public void setRollbackOnly() {
        }
    }
}
