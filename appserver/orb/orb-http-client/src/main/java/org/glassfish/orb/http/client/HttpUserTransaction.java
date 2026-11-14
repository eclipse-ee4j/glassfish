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

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

/**
 * A {@link UserTransaction} for a client that has no transaction manager.
 *
 * <p>The server begins the transaction and coordinates it; this client holds
 * the resulting branch and says when to finish. That is the only arrangement
 * that works for a thin client: two-phase commit needs a coordinator with a
 * durable log, and a log is not something to put in a jar on someone's
 * classpath. Payara's HTTP transport declines transaction propagation
 * altogether; this is the shape that makes the common case work without
 * pretending the client can be a coordinator.
 *
 * <p>The limitation to be aware of is that the transaction spans exactly one
 * endpoint. Work sent to a second server is not part of it, because nothing
 * here is coordinating between them. A caller that needs that has a
 * transaction manager already, and should enlist {@link HttpXAResource}
 * instead.
 */
public final class HttpUserTransaction implements UserTransaction {

    private final HttpTransactionClient client;
    private volatile int timeoutSeconds;
    private final ThreadLocal<Boolean> rollbackOnly = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public HttpUserTransaction(ClientConfiguration config, HttpTransport transport) {
        this.client = new HttpTransactionClient(config, transport);
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
        // A branch a finished container transaction left on this thread is not
        // a reason to refuse.
        AmbientTransaction.dropStaleBranch();
        if (ClientTransactionContext.current() != null) {
            // Nested transactions are not part of JTA, and quietly starting a
            // second one would orphan the first.
            throw new NotSupportedException("a transaction is already associated with this thread");
        }
        try {
            Xid xid = client.begin(timeoutSeconds);
            rollbackOnly.set(Boolean.FALSE);
            ClientTransactionContext.associate(xid, timeoutSeconds);
        } catch (XAException e) {
            throw systemException("could not begin a transaction", e);
        }
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException,
            HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        Xid xid = require();
        boolean markedRollbackOnly = Boolean.TRUE.equals(rollbackOnly.get());
        try {
            if (markedRollbackOnly) {
                // Marked for rollback: honour it here rather than sending a
                // commit the server would have to refuse anyway.
                client.rollbackUserTransaction(xid);
                throw new RollbackException("the transaction was marked rollback only");
            }
            client.commitUserTransaction(xid);
        } catch (XAException e) {
            int code = e.errorCode;
            if (markedRollbackOnly) {
                // The server no longer knows the transaction, which is what a
                // rollback was asking for: the outcome is the one reported.
                if (code == XAException.XAER_NOTA) {
                    throw rolledBack("the transaction was marked rollback only", e);
                }
                throw systemException("rollback of a transaction marked rollback only failed", e);
            }
            switch (code) {
                case XAException.XA_HEURCOM -> {
                    // A heuristic decision to commit is the outcome that was
                    // asked for. Reporting it as a failure would make a caller
                    // undo work that is committed.
                    return;
                }
                case XAException.XA_HEURMIX, XAException.XA_HEURHAZ -> {
                    HeuristicMixedException mixed =
                            new HeuristicMixedException("the transaction completed heuristically: " + e.getMessage());
                    mixed.initCause(e);
                    throw mixed;
                }
                case XAException.XA_HEURRB -> {
                    HeuristicRollbackException rolledBack = new HeuristicRollbackException(
                            "the transaction was rolled back heuristically: " + e.getMessage());
                    rolledBack.initCause(e);
                    throw rolledBack;
                }
                default -> {
                    // XAER_NOTA: the server has no such transaction, which for
                    // one it began is a timeout that rolled it back.
                    if (code == XAException.XAER_NOTA
                            || (code >= XAException.XA_RBBASE && code <= XAException.XA_RBEND)) {
                        throw rolledBack("the transaction was rolled back", e);
                    }
                    throw systemException("commit failed", e);
                }
            }
        } finally {
            clear();
        }
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        Xid xid = require();
        try {
            client.rollbackUserTransaction(xid);
        } catch (XAException e) {
            throw systemException("rollback failed", e);
        } finally {
            clear();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Recorded on the client and acted on at {@link #commit()}. It is not
     * sent to the server as it happens, so a bean running inside this
     * transaction on the server marking it rollback-only is reflected when the
     * invocation returns, not before.
     */
    @Override
    public void setRollbackOnly() throws IllegalStateException {
        require();
        rollbackOnly.set(Boolean.TRUE);
    }

    @Override
    public int getStatus() {
        // Only a transaction begun through this object is its business. One a
        // container's manager started is not active here: committing it from
        // here would send the server a commit for a branch the container owns.
        AmbientTransaction.dropStaleBranch();
        if (ownTransaction() == null) {
            return Status.STATUS_NO_TRANSACTION;
        }
        return Boolean.TRUE.equals(rollbackOnly.get())
                ? Status.STATUS_MARKED_ROLLBACK
                : Status.STATUS_ACTIVE;
    }

    @Override
    public void setTransactionTimeout(int seconds) {
        this.timeoutSeconds = Math.max(seconds, 0);
    }

    private Xid require() {
        AmbientTransaction.dropStaleBranch();
        Xid xid = ownTransaction();
        if (xid == null) {
            throw new IllegalStateException("no transaction begun through this UserTransaction is associated"
                    + " with this thread");
        }
        return xid;
    }

    /** @return the branch begun through this transport on this thread, not one a container's manager enlisted */
    private static Xid ownTransaction() {
        return ClientTransactionContext.isAmbient() ? null : ClientTransactionContext.current();
    }

    private static RollbackException rolledBack(String message, XAException cause) {
        RollbackException rollback = new RollbackException(message);
        rollback.initCause(cause);
        return rollback;
    }

    private void clear() {
        rollbackOnly.remove();
        ClientTransactionContext.disassociate();
    }

    private static SystemException systemException(String message, Throwable cause) {
        SystemException e = new SystemException(message + ": " + cause);
        e.initCause(cause);
        return e;
    }
}
