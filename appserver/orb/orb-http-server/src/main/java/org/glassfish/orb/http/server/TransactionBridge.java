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


import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

/**
 * The seam onto the transaction manager.
 *
 * <p>Like {@link ContainerBridge} and {@link NamingBridge}, this restates
 * something GlassFish already has rather than inventing it. The methods are
 * those of {@code JavaEETransactionManagerDelegate} - an HK2 contract with two
 * implementations already, one local and one distributed over OTS - together
 * with the {@code jakarta.resource.spi.XATerminator} it hands out:
 *
 * <pre>
 * void recreate(Xid xid, long timeout);   // import a transaction onto this thread
 * void release(Xid xid);                  // and let it go again
 * XATerminator getXATerminator();         // prepare / commit / rollback / forget / recover
 * </pre>
 *
 * <p>That is the JCA transaction inflow contract, which the server already
 * implements because a resource adapter needs it. An HTTP request carrying a
 * transaction is the same problem as a message arriving on an inbound adapter:
 * something outside the server has a transaction and wants work done inside
 * it. So the server half of distributed transactions over HTTP is a mapping
 * rather than an implementation.
 *
 * <p>It is restated here without {@code jakarta.resource} so this module keeps
 * compiling and testing on its own. Only {@code javax.transaction.xa}, which
 * is a JDK module, appears in the signatures.
 */
public interface TransactionBridge {

    /**
     * A failure carrying an XA error code, so the dispatcher can report it to
     * the caller as the same code its own transaction manager expects.
     */
    class TransactionException extends Exception {

        private static final long serialVersionUID = 1L;

        private final int errorCode;

        public TransactionException(String message, int errorCode) {
            super(message);
            this.errorCode = errorCode;
        }

        public TransactionException(String message, int errorCode, Throwable cause) {
            super(message, cause);
            this.errorCode = errorCode;
        }

        /** @return an {@link XAException} code, e.g. {@link XAException#XAER_NOTA} */
        public int errorCode() {
            return errorCode;
        }
    }

    // ---- inflow: this server does work inside someone else's transaction ----

    /**
     * Associates an imported transaction with the calling thread for the
     * duration of one invocation.
     *
     * @param xid the branch being imported
     * @param timeoutSeconds the caller's remaining timeout, or 0 for the default
     */
    void recreate(Xid xid, long timeoutSeconds) throws TransactionException;

    /** Disassociates the transaction imported by {@link #recreate}. */
    void release(Xid xid) throws TransactionException;

    /**
     * Leaves this thread with no transaction on it.
     *
     * <p>Called before an invocation that carries none. Request threads are
     * pooled, and a branch one of them was left holding would otherwise become
     * the transaction of whatever ran next: a call made deliberately outside a
     * transaction would answer with someone else's, and commit or roll back
     * with it. It costs a method call to make that impossible.
     *
     * <p>Does nothing by default, for bridges with no thread association to
     * clear.
     */
    default void detach() {
    }

    // ---- this server as a branch of the caller's transaction ---------------

    /** Runs the {@code beforeCompletion} synchronizations for the branch. */
    void beforeCompletion(Xid xid) throws TransactionException;

    /**
     * @return {@link javax.transaction.xa.XAResource#XA_OK} or
     *         {@link javax.transaction.xa.XAResource#XA_RDONLY}
     */
    int prepare(Xid xid) throws TransactionException;

    void commit(Xid xid, boolean onePhase) throws TransactionException;

    void rollback(Xid xid) throws TransactionException;

    void forget(Xid xid) throws TransactionException;

    /**
     * @param flags an {@link javax.transaction.xa.XAResource} scan flag
     * @return the branches this server is prepared on and has not resolved
     */
    Xid[] recover(int flags) throws TransactionException;

    // ---- this server coordinates, for a caller with no transaction manager --

    /**
     * Begins a transaction that this server will coordinate.
     *
     * @param timeoutSeconds the requested timeout, or 0 for the default
     * @return the Xid naming it, which the caller quotes on later invocations
     */
    Xid begin(long timeoutSeconds) throws TransactionException;

    /** Commits a transaction begun by {@link #begin}. */
    void commitUserTransaction(Xid xid) throws TransactionException;

    /** Rolls back a transaction begun by {@link #begin}. */
    void rollbackUserTransaction(Xid xid) throws TransactionException;

    /**
     * A bridge that refuses everything, for an endpoint deployed without
     * transaction support. Refusing is deliberate: silently running an
     * invocation outside the caller's transaction and reporting success is
     * the one outcome a transactional client cannot detect.
     */
    TransactionBridge NONE = new TransactionBridge() {

        private TransactionException unsupported() {
            return new TransactionException(
                    "this endpoint does not support transaction propagation",
                    XAException.XAER_RMERR);
        }

        @Override
        public void recreate(Xid xid, long timeoutSeconds) throws TransactionException {
            throw unsupported();
        }

        @Override
        public void release(Xid xid) throws TransactionException {
            throw unsupported();
        }

        @Override
        public void beforeCompletion(Xid xid) throws TransactionException {
            throw unsupported();
        }

        @Override
        public int prepare(Xid xid) throws TransactionException {
            throw unsupported();
        }

        @Override
        public void commit(Xid xid, boolean onePhase) throws TransactionException {
            throw unsupported();
        }

        @Override
        public void rollback(Xid xid) throws TransactionException {
            throw unsupported();
        }

        @Override
        public void forget(Xid xid) throws TransactionException {
            throw unsupported();
        }

        @Override
        public Xid[] recover(int flags) throws TransactionException {
            throw unsupported();
        }

        @Override
        public Xid begin(long timeoutSeconds) throws TransactionException {
            throw unsupported();
        }

        @Override
        public void commitUserTransaction(Xid xid) throws TransactionException {
            throw unsupported();
        }

        @Override
        public void rollbackUserTransaction(Xid xid) throws TransactionException {
            throw unsupported();
        }
    };
}
