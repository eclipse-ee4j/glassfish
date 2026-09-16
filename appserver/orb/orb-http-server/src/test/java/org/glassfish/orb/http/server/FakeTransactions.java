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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.glassfish.orb.http.protocol.Xids;

/**
 * A {@link TransactionBridge} that records what it was asked to do.
 *
 * <p>It stands in for {@code JavaEETransactionManagerDelegate} plus the
 * {@code XATerminator} it hands out. That it can stand in for them at all is
 * the point being made: the server half of transaction propagation is a
 * mapping onto primitives the container already has, so it can be exercised
 * without one.
 */
final class FakeTransactions implements TransactionBridge {

    /** Branches begun and not yet resolved, in order. */
    final Map<String, Xid> live = new LinkedHashMap<>();
    /** Every call, in order, for assertions about sequencing. */
    final List<String> calls = new ArrayList<>();
    /** Branches currently associated with a thread by recreate. */
    final Map<String, Long> imported = new LinkedHashMap<>();
    /** Branches that reached prepare. */
    final List<Xid> prepared = new ArrayList<>();

    private final AtomicLong nextId = new AtomicLong(1);

    /** When set, prepare answers read-only. */
    boolean voteReadOnly;
    /** When set, the next operation fails with this XA code. */
    int failWith;

    private void record(String call) {
        calls.add(call);
    }

    private void maybeFail(String what) throws TransactionException {
        if (failWith != 0) {
            int code = failWith;
            failWith = 0;
            throw new TransactionException(what + " was told to fail", code);
        }
    }

    @Override
    public Xid begin(long timeoutSeconds) throws TransactionException {
        record("begin(" + timeoutSeconds + ")");
        maybeFail("begin");
        byte[] global = new byte[8];
        java.nio.ByteBuffer.wrap(global).putLong(nextId.getAndIncrement());
        Xid xid = new Xids.SimpleXid(0x47465348, global, new byte[] { 1 });
        live.put(Xids.key(xid), xid);
        return xid;
    }

    @Override
    public void recreate(Xid xid, long timeoutSeconds) throws TransactionException {
        record("recreate(" + Xids.key(xid) + "," + timeoutSeconds + ")");
        maybeFail("recreate");
        imported.put(Xids.key(xid), timeoutSeconds);
        // Importing is how a server learns about a branch it did not begin,
        // which is the whole of the XA case: the coordinator mints the xid and
        // this side hears about it when work arrives for it.
        live.putIfAbsent(Xids.key(xid), xid);
    }

    @Override
    public void release(Xid xid) throws TransactionException {
        record("release(" + Xids.key(xid) + ")");
        imported.remove(Xids.key(xid));
    }

    @Override
    public void detach() {
        record("detach");
    }

    @Override
    public void beforeCompletion(Xid xid) throws TransactionException {
        record("beforeCompletion");
        maybeFail("beforeCompletion");
    }

    @Override
    public int prepare(Xid xid) throws TransactionException {
        record("prepare");
        maybeFail("prepare");
        requireLive(xid);
        prepared.add(xid);
        return voteReadOnly ? XAResource.XA_RDONLY : XAResource.XA_OK;
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws TransactionException {
        record("commit(onePhase=" + onePhase + ")");
        maybeFail("commit");
        requireLive(xid);
        live.remove(Xids.key(xid));
    }

    @Override
    public void rollback(Xid xid) throws TransactionException {
        record("rollback");
        maybeFail("rollback");
        live.remove(Xids.key(xid));
    }

    @Override
    public void forget(Xid xid) throws TransactionException {
        record("forget");
        maybeFail("forget");
        live.remove(Xids.key(xid));
    }

    @Override
    public Xid[] recover(int flags) throws TransactionException {
        record("recover(" + flags + ")");
        maybeFail("recover");
        return prepared.toArray(new Xid[0]);
    }

    @Override
    public void commitUserTransaction(Xid xid) throws TransactionException {
        record("commitUserTransaction");
        maybeFail("commitUserTransaction");
        requireLive(xid);
        live.remove(Xids.key(xid));
    }

    @Override
    public void rollbackUserTransaction(Xid xid) throws TransactionException {
        record("rollbackUserTransaction");
        maybeFail("rollbackUserTransaction");
        requireLive(xid);
        live.remove(Xids.key(xid));
    }

    private void requireLive(Xid xid) throws TransactionException {
        if (!live.containsKey(Xids.key(xid))) {
            throw new TransactionException("no such branch", XAException.XAER_NOTA);
        }
    }
}
