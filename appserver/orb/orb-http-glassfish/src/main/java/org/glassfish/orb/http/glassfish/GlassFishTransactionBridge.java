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

import com.sun.enterprise.transaction.api.JavaEETransactionManager;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.resource.spi.XATerminator;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.glassfish.orb.http.protocol.Xids;
import org.glassfish.orb.http.server.TransactionBridge;
import org.jvnet.hk2.annotations.Service;

/**
 * Runs the caller's transaction on this server.
 * <p>
 * There is almost nothing new here, and that is the point. GlassFish already
 * accepts transactions that begin somewhere else: a resource adapter inflows
 * work under a foreign {@code Xid}, the transaction manager recreates that
 * branch on the thread, and an {@link XATerminator} drives it to an outcome.
 * That contract was written for JCA, but nothing in it is about JCA - it is
 * about a transaction whose coordinator is not this JVM, which is exactly what
 * a remote client is.
 * <p>
 * So the HTTP transport does not gain a transaction implementation. It borrows
 * the one the server already trusts, and the work is translation.
 */
@Service
@Singleton
public class GlassFishTransactionBridge implements TransactionBridge {

    /**
     * Distinguishes branches this transport began from anything else the
     * server is coordinating. Chosen, not standardised: a format id is opaque
     * to everyone but its own manager.
     */
    private static final int FORMAT_ID = 0x4F524248;

    private static final Logger LOG = System.getLogger(GlassFishTransactionBridge.class.getName());

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AtomicLong sequence = new AtomicLong();

    @Inject
    private JavaEETransactionManager transactions;

    @Override
    public void recreate(Xid xid, long timeoutSeconds) throws TransactionException {
        try {
            transactions.recreate(xid, timeoutSeconds);
        } catch (Exception e) {
            throw failure("cannot recreate " + Xids.key(xid), XAException.XAER_RMERR, e);
        }
    }

    @Override
    public void release(Xid xid) throws TransactionException {
        try {
            transactions.release(xid);
        } catch (Exception e) {
            throw failure("cannot release " + Xids.key(xid), XAException.XAER_RMERR, e);
        } finally {
            detach();
        }
    }

    /**
     * Leaves the thread with no transaction on it, whatever happened above.
     *
     * <p>A release can fail - a branch the bean marked for rollback is the
     * ordinary case - and the caller logs that and carries on, because the
     * invocation's own outcome has already been decided. What must not carry
     * on is the association: these are pooled request threads, and one still
     * holding an aborted transaction fails the next request to land on it,
     * with an error about a transaction that request never started. That is a
     * failure in one call reappearing as a failure in an unrelated one, which
     * is the hardest kind to trace back.
     */
    @Override
    public void detach() {
        try {
            Object stray = current();
            if (stray != null) {
                // Not an error - a released branch is the ordinary case - but
                // worth a line when someone goes looking for a request that
                // ran in a transaction nobody sent it.
                LOG.log(Level.DEBUG, "took a transaction off this thread: " + stray);
                transactions.suspend();
            }
        } catch (Exception e) {
            // Nothing further to try, and throwing here would replace the real
            // failure with this one.
            LOG.log(Level.WARNING, "could not detach the transaction from this thread", e);
        }
    }

    /** @return the transaction on this thread, or null - never throwing */
    private Object current() {
        try {
            return transactions == null ? null : transactions.getTransaction();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Nothing to do here, and the reason is worth stating rather than
     * leaving as an empty method. Synchronizations registered by beans that
     * ran in this branch are driven by the transaction manager as part of
     * preparing it - that is where the inflow contract puts them. Running them
     * here as well would run them twice.
     */
    @Override
    public void beforeCompletion(Xid xid) {
    }

    /**
     * {@inheritDoc}
     *
     * <p>A branch that votes to roll back is finished: XA says the resource
     * manager has already rolled it back and released it, so a coordinator
     * that reads that vote will never call rollback for it. Measured against a
     * real server, the branch is still here after such a vote and accepts a
     * rollback - so it is rolled back here, and what the coordinator is
     * entitled to assume becomes true.
     */
    @Override
    public int prepare(Xid xid) throws TransactionException {
        int vote;
        try {
            vote = terminator().prepare(xid);
        } catch (XAException e) {
            discard(xid);
            throw failure("prepare failed for " + Xids.key(xid), e.errorCode, e);
        }
        return vote;
    }

    /**
     * Rolls back a branch whose own prepare refused it, silently.
     *
     * <p>What the caller will be told is the refusal. A rollback that fails as
     * well must not replace it, and there is nothing further to try.
     *
     * @param xid the branch that voted to roll back
     */
    private void discard(Xid xid) {
        try {
            terminator().rollback(xid);
        } catch (Exception e) {
            LOG.log(Level.DEBUG, "could not roll back " + Xids.key(xid)
                    + " after its prepare refused it", e);
        }
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws TransactionException {
        if (onePhase) {
            commitOnePhase(xid);
            return;
        }
        try {
            terminator().commit(xid, false);
        } catch (XAException e) {
            throw failure("commit failed for " + Xids.key(xid), e.errorCode, e);
        }
    }

    /**
     * Commits a branch this server is the only participant in - by preparing
     * it first.
     *
     * <p>Not with a one-phase commit, which is the obvious way and the wrong
     * one. Measured against a real server: a branch a bean marked for rollback
     * is <em>accepted</em> by a one-phase commit through the terminator, with
     * no error raised and no work committed. The mark is honoured and the
     * caller is told the opposite. The same branch is refused by prepare, with
     * XA_RBROLLBACK.
     *
     * <p>So the vote is asked for even though there is nobody to disagree with
     * it. A resource manager may always do that with its own branch: one phase
     * is an optimisation, and giving it up costs nothing when both halves are
     * local. What it buys is the only truthful answer available.
     *
     * <p>It is also what carries a decision taken on a third server back to
     * the client. A bean here that called on to another server enlisted that
     * server as a branch of this transaction; preparing reaches it, its vote
     * comes back, and a rollback decided there refuses the commit here.
     *
     * @param xid the branch to resolve
     */
    private void commitOnePhase(Xid xid) throws TransactionException {
        if (prepare(xid) == XAResource.XA_RDONLY) {
            // Nothing durable in this branch, and prepare has already ended
            // it. Committing after a read-only vote is a protocol error, and
            // the server answers it as one.
            return;
        }
        commit(xid, false);
    }

    @Override
    public void rollback(Xid xid) throws TransactionException {
        try {
            terminator().rollback(xid);
        } catch (XAException e) {
            throw failure("rollback failed for " + Xids.key(xid), e.errorCode, e);
        }
    }

    @Override
    public void forget(Xid xid) throws TransactionException {
        try {
            terminator().forget(xid);
        } catch (XAException e) {
            throw failure("forget failed for " + Xids.key(xid), e.errorCode, e);
        }
    }

    @Override
    public Xid[] recover(int flags) throws TransactionException {
        try {
            Xid[] found = terminator().recover(flags);
            return found == null ? new Xid[0] : found;
        } catch (XAException e) {
            throw failure("recover failed", e.errorCode, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>A client-driven transaction is begun as an imported branch and then
     * suspended, rather than left running on the thread that asked for it.
     * The request that begins a transaction is not the request that uses it -
     * over HTTP they are separate exchanges, possibly on different threads -
     * so a transaction still attached to the beginning thread would leak into
     * whatever that thread did next.
     */
    @Override
    public Xid begin(long timeoutSeconds) throws TransactionException {
        Xid xid = newXid();
        recreate(xid, timeoutSeconds);
        release(xid);
        return xid;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This server is the only participant the client knows of, so there is
     * nobody to run a two-phase exchange with - but the vote is still asked
     * for, for the reason {@link #commitOnePhase} gives: it is the only place
     * the server tells the truth about a branch a bean marked for rollback.
     */
    @Override
    public void commitUserTransaction(Xid xid) throws TransactionException {
        commitOnePhase(xid);
    }

    @Override
    public void rollbackUserTransaction(Xid xid) throws TransactionException {
        rollback(xid);
    }

    private Xid newXid() {
        byte[] global = new byte[24];
        RANDOM.nextBytes(global);
        // The counter makes two branches begun in the same nanosecond on the
        // same node distinct without relying on the random bytes alone.
        ByteBuffer.wrap(global, 0, Long.BYTES).putLong(sequence.incrementAndGet());
        return new Xids.SimpleXid(FORMAT_ID, global, new byte[] { 1 });
    }

    /**
     * The terminator, on a thread fit to drive one.
     *
     * <p>The detach is not tidiness. A terminator drives a branch from
     * outside it, and a thread carrying a transaction of its own is not
     * outside anything: measured against a real server, a prepare from such a
     * thread is refused with {@code XAER_PROTO} while the branch being
     * prepared is perfectly healthy. These are pooled request threads and a
     * commit arrives on whichever one is free, so the failure lands on a
     * different scenario each run - which is how it was found.
     *
     * @return the manager's terminator
     */
    private XATerminator terminator() throws TransactionException {
        XATerminator terminator = transactions == null ? null : transactions.getXATerminator();
        if (terminator == null) {
            throw new TransactionException("this server has no transaction manager to drive",
                    XAException.XAER_RMFAIL);
        }
        detach();
        return terminator;
    }

    private static TransactionException failure(String message, int errorCode, Throwable cause) {
        return new TransactionException(message + ": " + cause, errorCode == 0
                ? XAException.XAER_RMERR : errorCode, cause);
    }

    /** Exposed so the endpoint can report what it wired without reflection. */
    static int formatId() {
        return FORMAT_ID;
    }

    static int xaOk() {
        return XAResource.XA_OK;
    }
}
