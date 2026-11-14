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
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.transaction.xa.Xid;

/**
 * Joins a transaction this client did not start.
 * <p>
 * {@link HttpUserTransaction} covers the case where the client begins the
 * transaction itself. It does not cover the more common one: code running
 * inside a container, in a transaction the container began, calling a remote
 * bean. Nothing there ever touches this transport's own UserTransaction, and
 * without this the call would leave the transaction behind - the bean would
 * run in its own, and work the caller believes is one unit would be two.
 * <p>
 * The join is the ordinary JTA one. An {@link HttpXAResource} is enlisted with
 * the caller's transaction, the caller's transaction manager calls
 * {@code start} on it, and from then on invocations carry the branch because
 * {@link ClientTransactionContext} has been associated. At the end the
 * caller's coordinator drives prepare and commit over this transport, which is
 * what makes the remote server a genuine participant rather than a side
 * effect.
 * <p>
 * Nothing to join is not an error. In a plain client JVM there is no JNDI
 * initial context at all, the lookup fails once, and the answer is remembered
 * so later invocations cost nothing. Any other failure to look the registry up
 * is not remembered: a thread that happens to have no component environment
 * must not turn joining off for the rest of the JVM's life.
 * <p>
 * Failing to join a transaction that <em>is</em> active is an error, and
 * {@link #join} throws it. The alternative, calling anyway, runs the remote
 * work outside the caller's transaction and commits it there whatever the
 * caller decides next - the one outcome a transactional caller cannot detect.
 * <p>
 * A transaction that can only roll back is not refused here. The call is sent
 * with a marker and the server applies the method's transaction attribute, as
 * it does for an IIOP caller in that state. A transaction that has already
 * completed - the caller is in {@code afterCompletion} - is no transaction at
 * all, and the call is sent without one.
 */
final class AmbientTransaction {

    private static final Logger LOG = System.getLogger(AmbientTransaction.class.getName());

    /**
     * Where a transaction manager is usually bound. The registry has a
     * standard name; the manager does not, so the container-specific names
     * are tried in turn and the answer is cached either way.
     */
    private static final String MANAGER_PROPERTY = "org.glassfish.orb.http.transactionManager";

    private static final List<String> MANAGER_NAMES = List.of(
            System.getProperty(MANAGER_PROPERTY, "java:appserver/TransactionManager"),
            "java:comp/TransactionManager",
            "java:/TransactionManager",
            "java:jboss/TransactionManager");

    /**
     * Where the registry is bound. The server-wide name comes first: GlassFish
     * serves it through its transaction naming proxy without a component
     * invocation, so it works on a thread that has no java:comp, which is
     * exactly the thread that reaches here from an unmanaged executor.
     */
    private static final List<String> REGISTRY_NAMES = List.of(
            "java:appserver/TransactionSynchronizationRegistry",
            "java:comp/TransactionSynchronizationRegistry");

    /**
     * The branch the caller's manager started for one endpoint, kept with the
     * transaction it belongs to rather than with the thread.
     *
     * @param xid            the branch, or null if the manager has not started it
     * @param timeoutSeconds the timeout the branch was started with
     * @param deadlineNanos  when that timeout runs out, so that putting the
     *                       branch back on a thread does not restart the clock
     */
    private record Joined(Xid xid, long timeoutSeconds, long deadlineNanos) {
    }

    /** Set once we know this JVM has no transaction manager to join. */
    private static final AtomicBoolean UNAVAILABLE = new AtomicBoolean();

    /** Set once a lookup failure that was not final has been reported, so a broken one is said once. */
    private static final AtomicBoolean LOOKUP_FAILURE_REPORTED = new AtomicBoolean();

    /** How long a failed registry lookup is believed before it is tried again. */
    private static final long RETRY_AFTER_NANOS = 30_000_000_000L;

    /** The clock the retry delay is measured with; replaced in tests. */
    static volatile LongSupplier clock = System::nanoTime;

    /**
     * Class loaders whose threads failed to find the registry, and when they
     * may look again. Keyed by the thread's context class loader rather than
     * held JVM-wide: what a lookup can see depends on the caller, so one
     * thread's miss says nothing about another's.
     */
    private static final Map<ClassLoader, Long> MISSES = Collections.synchronizedMap(new WeakHashMap<>());

    /** The last lookup failure that was not final, for the report when no name worked. */
    private static volatile Exception lastLookupFailure;

    private static volatile TransactionManager manager;

    private static volatile TransactionSynchronizationRegistry registry;

    private AmbientTransaction() {
    }

    /**
     * Discards what was found about this JVM's transaction manager.
     *
     * <p>Visible for tests. The lookups are cached deliberately - a plain
     * client JVM must not pay for them on every invocation - and a cache that
     * cannot be cleared would make the first test to run decide the answer for
     * all the others.
     */
    static void forget() {
        manager = null;
        registry = null;
        UNAVAILABLE.set(false);
        LOOKUP_FAILURE_REPORTED.set(false);
        MISSES.clear();
        lastLookupFailure = null;
        clock = System::nanoTime;
    }

    /** Visible for tests: forgets the registry but keeps the manager, as when only a lookup went wrong. */
    static void forgetRegistry() {
        registry = null;
        MISSES.clear();
    }

    /** What the state of the caller's transaction means for an invocation. */
    private enum Standing { ACTIVE, ROLLBACK_ONLY, NONE, REFUSED }

    private static Standing standing(int status) {
        return switch (status) {
            case Status.STATUS_ACTIVE -> Standing.ACTIVE;
            case Status.STATUS_MARKED_ROLLBACK, Status.STATUS_ROLLING_BACK -> Standing.ROLLBACK_ONLY;
            // Completed: a call from afterCompletion. The transaction is over
            // and whatever the caller does now is not part of it, exactly as
            // when no transaction was ever there.
            case Status.STATUS_COMMITTED, Status.STATUS_ROLLEDBACK, Status.STATUS_NO_TRANSACTION -> Standing.NONE;
            // Preparing, prepared, committing, unknown: work done now would
            // either miss the decision or contradict it.
            default -> Standing.REFUSED;
        };
    }

    /**
     * Enlists this endpoint with the caller's transaction, if there is one and
     * it has not been enlisted already.
     *
     * @param config    names the endpoint, which is also what distinguishes
     *                  branches: two calls to the same server in one
     *                  transaction are one branch, to different servers are two
     * @param transport how the branch will be driven
     * @return true if the caller's transaction can only roll back, so the
     *         invocation must carry that marker; the branch, if there is one,
     *         is associated with the thread as usual
     * @throws EJBException if a transaction is active, or in a state the call
     *         must not be made in, and could not be joined
     */
    static boolean join(ClientConfiguration config, HttpTransport transport) {
        if (UNAVAILABLE.get()) {
            // No transaction manager here to ask, and nothing this class did
            // can be stale, because it never ran.
            return false;
        }

        TransactionSynchronizationRegistry synchronizations = registry();
        if (synchronizations == null) {
            // No container here, so there is nothing to join - and nothing to
            // judge either: an association in a JVM with no transaction
            // manager belongs to whoever made it. Unless a manager is already
            // known and says this thread is in a transaction: then the lookup
            // merely failed, and sending without it would commit alone.
            failIfAKnownManagerHasATransaction(config);
            return false;
        }
        if (ClientTransactionContext.current() != null && !ClientTransactionContext.isAmbient()) {
            // Begun through this transport's own UserTransaction: not ours to
            // judge, and not ours to replace.
            return false;
        }

        if (synchronizations.getTransactionKey() == null) {
            if (ClientTransactionContext.current() != null) {
                // A branch enlisted with the caller's manager is still on this
                // thread, and the caller has no transaction any more. The end
                // that should have dropped it never came - request threads are
                // pooled, and the next call on this one would silently join a
                // transaction that is already over.
                LOG.log(Level.DEBUG, "dropping a branch left on this thread by a transaction that has ended");
                ClientTransactionContext.disassociate();
            }
            return false;
        }

        String endpoint = String.valueOf(config.baseUri());
        int status = synchronizations.getTransactionStatus();
        Standing standing = standing(status);
        if (standing == Standing.NONE) {
            ClientTransactionContext.disassociate();
            return false;
        }
        if (standing == Standing.REFUSED) {
            // A branch left on the thread must not outlive the refusal.
            ClientTransactionContext.disassociate();
            throw new EJBException("the caller's transaction is not in a state a remote call can join (status "
                    + status + "), so the call to " + endpoint + " was not made");
        }
        boolean rollbackOnly = standing == Standing.ROLLBACK_ONLY;

        // A transaction is active, and the branch to carry is the one enlisted
        // for this endpoint in this transaction - never whatever the thread
        // happens to hold. Trusting the thread was the previous version, and
        // it failed exactly when a pooled thread went from one transaction
        // straight into the next: the new call carried the old, finished
        // branch, the new transaction never enlisted the far server, and a
        // rollback decided there was committed here.
        String marker = AmbientTransaction.class.getName() + ':' + config.baseUri();
        if (synchronizations.getResource(marker) instanceof Joined joined) {
            if (joined.xid() != null) {
                // Also what keeps two endpoints in one transaction apart: each
                // call puts back its own endpoint's branch.
                ClientTransactionContext.associate(joined.xid(), joined.timeoutSeconds(), true,
                        joined.deadlineNanos());
            }
            return rollbackOnly;
        }
        // Not joined in this transaction yet, so anything on the thread
        // belongs to another one.
        ClientTransactionContext.disassociate();
        if (rollbackOnly) {
            // A manager will not enlist a resource in a transaction that can
            // only roll back, so there is no branch to send. The marker alone
            // lets the server apply the method's transaction attribute.
            return true;
        }

        TransactionManager transactions = manager();
        if (transactions == null) {
            throw new EJBException("a transaction is active but no transaction manager was found, so the call to "
                    + endpoint + " cannot carry it; the call was not made. Name where it is bound with the "
                    + "system property " + MANAGER_PROPERTY + ", or set "
                    + HttpInitialContextFactory.PROP_AMBIENT_TRANSACTION
                    + "=false (JNDI environment or system property) to send calls without the caller's transaction");
        }
        Transaction current;
        try {
            current = transactions.getTransaction();
        } catch (SystemException e) {
            throw new EJBException("could not read the caller's transaction, so the call to " + endpoint
                    + " was not made", e);
        }
        if (current == null) {
            throw new EJBException("the transaction registry reports an active transaction but the transaction"
                    + " manager has none, so the call to " + endpoint + " was not made");
        }
        try {
            // Enlisting is what makes the server a participant: the caller's
            // coordinator will drive prepare and commit on this resource.
            current.enlistResource(new HttpXAResource(config, transport));
        } catch (RollbackException e) {
            // The transaction became rollback-only between the status read and
            // the enlistment, which is the same state. The manager may have
            // started the branch before it refused.
            ClientTransactionContext.disassociate();
            return true;
        } catch (SystemException | RuntimeException e) {
            ClientTransactionContext.disassociate();
            throw new EJBException("could not join the caller's transaction, so the call to " + endpoint
                    + " was not made", e);
        }
        // The manager starts the branch while enlisting, which associates
        // it; keep it with the transaction so later calls can put it back.
        synchronizations.putResource(marker, new Joined(ClientTransactionContext.current(),
                ClientTransactionContext.configuredTimeoutSeconds(),
                ClientTransactionContext.currentDeadlineNanos()));
        return false;
    }

    /**
     * Drops a branch a container's transaction left on this thread, if that
     * transaction has ended.
     * <p>
     * {@link #join} does this on every invocation, but a caller that never
     * invokes - {@link HttpUserTransaction} asking about its own state - would
     * otherwise see the leftover as a transaction of its own.
     * <p>
     * Does nothing unless the thread's association came from a container's
     * manager, so the common case costs one thread-local read.
     */
    static void dropStaleBranch() {
        if (!ClientTransactionContext.isAmbient() || UNAVAILABLE.get()) {
            return;
        }
        TransactionSynchronizationRegistry synchronizations = registry();
        if (synchronizations != null && synchronizations.getTransactionKey() == null) {
            LOG.log(Level.DEBUG, "dropping a branch left on this thread by a transaction that has ended");
            ClientTransactionContext.disassociate();
        }
    }

    /**
     * The registry could not be had on this thread. If a transaction manager
     * is already known and reports a transaction here, refuse: the caller is
     * inside a transaction this class cannot join.
     */
    private static void failIfAKnownManagerHasATransaction(ClientConfiguration config) {
        TransactionManager known = manager;
        if (known == null || UNAVAILABLE.get()) {
            return;
        }
        try {
            Transaction current = known.getTransaction();
            if (current != null && standing(current.getStatus()) != Standing.NONE) {
                throw new EJBException("the caller is in a transaction but the transaction registry could not be"
                        + " looked up from this thread, so the call to " + config.baseUri()
                        + " cannot join it and was not made");
            }
        } catch (SystemException e) {
            // A manager that cannot say is no evidence of a transaction.
            LOG.log(Level.DEBUG, "could not ask the known transaction manager: " + e);
        }
    }

    private static TransactionSynchronizationRegistry registry() {
        TransactionSynchronizationRegistry known = registry;
        if (known != null) {
            return known;
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Long retryAt = MISSES.get(loader);
        if (retryAt != null && clock.getAsLong() - retryAt < 0) {
            // A lookup from this loader's threads failed a moment ago.
            // Believing that for a while costs a call that could have joined,
            // and asking again on every call costs a JNDI lookup on every call.
            return null;
        }
        lastLookupFailure = null;
        // Only a lookup that cannot succeed here at all latches UNAVAILABLE,
        // and it does so itself. A miss for any other reason is a miss to be
        // believed for RETRY_AFTER_NANOS and then tried again.
        for (String name : REGISTRY_NAMES) {
            Object found = lookup(name);
            if (found instanceof TransactionSynchronizationRegistry resolved) {
                registry = resolved;
                MISSES.remove(loader);
                return resolved;
            }
            if (UNAVAILABLE.get()) {
                return null;
            }
        }
        Exception failure = lastLookupFailure;
        Level level = failure != null && LOOKUP_FAILURE_REPORTED.compareAndSet(false, true)
                ? Level.WARNING : Level.DEBUG;
        LOG.log(level, "could not look up the transaction registry as " + REGISTRY_NAMES
                + "; remote calls from this thread will not join a caller's transaction until this works: "
                + failure);
        MISSES.put(loader, clock.getAsLong() + RETRY_AFTER_NANOS);
        return null;
    }

    private static TransactionManager manager() {
        TransactionManager known = manager;
        if (known != null) {
            return known;
        }
        for (String name : MANAGER_NAMES) {
            if (lookup(name) instanceof TransactionManager resolved) {
                manager = resolved;
                return resolved;
            }
        }
        return null;
    }

    /**
     * Looks a name up in whatever JNDI the JVM is configured with.
     *
     * @return what is bound, or null if nothing is; null is also returned, and
     *         joining switched off for good, when no lookup here can ever work
     */
    private static Object lookup(String name) {
        InitialContext context = null;
        try {
            context = new InitialContext();
            // Asking for the environment is what makes a JVM with no initial
            // context factory say so, rather than failing the lookup below the
            // same way and leaving it ambiguous.
            Hashtable<?, ?> environment = context.getEnvironment();
            if (environment != null && HttpInitialContextFactory.class.getName()
                    .equals(environment.get(Context.INITIAL_CONTEXT_FACTORY))) {
                // This client's own transport is the JNDI provider, so the
                // caller is a standalone client: there is no container
                // transaction, and looking the registry up would be a remote
                // call to the server, with the user's credentials, to ask it
                // for something that only exists in a container.
                latch("this JVM's JNDI is the HTTP transport itself");
                return null;
            }
            return context.lookup(name);
        } catch (NoInitialContextException e) {
            latch("this JVM has no JNDI initial context");
            return null;
        } catch (NamingException | RuntimeException e) {
            // Not final: the next call may be on a thread that can see it.
            lastLookupFailure = e;
            return null;
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException | RuntimeException ignored) {
                    // Nothing to do about a context that will not close.
                }
            }
        }
    }

    /** Remembers that this JVM has nothing to join. */
    private static void latch(String why) {
        UNAVAILABLE.set(true);
        LOG.log(Level.DEBUG, "not joining caller transactions: " + why);
    }
}
