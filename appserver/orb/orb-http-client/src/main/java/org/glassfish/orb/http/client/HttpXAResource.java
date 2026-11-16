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
import java.util.Objects;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Enlists a remote GlassFish endpoint as one branch of the caller's
 * transaction.
 *
 * <p>This is the half for a caller that has a transaction manager of its own -
 * typically another application server making a remote EJB call. The caller
 * coordinates; this resource carries prepare, commit, rollback and forget over
 * HTTP to the endpoint, which resolves them through its own transaction
 * manager.
 *
 * <p>{@link #start} and {@link #end} do not talk to the server. There is
 * nothing to tell it yet: the association only matters when an invocation is
 * actually made, and each invocation carries the branch in its own body. So
 * start records the association for this thread and end drops it, and the
 * server learns about the branch the first time work arrives for it. That also
 * means a transaction in which no invocation was made never reaches the
 * server, and the coordinator's prepare on it will be answered with
 * {@code XAER_NOTA} - which is correct, and which a coordinator already knows
 * how to treat as a read-only branch.
 */
public final class HttpXAResource implements XAResource {

    private final HttpTransactionClient client;
    private final URI endpoint;
    private volatile int timeoutSeconds;

    public HttpXAResource(ClientConfiguration config, HttpTransport transport) {
        this.client = new HttpTransactionClient(config, transport);
        this.endpoint = config.baseUri();
    }

    @Override
    public void start(Xid xid, int flags) throws XAException {
        if (flags == TMRESUME || flags == TMJOIN || flags == TMNOFLAGS) {
            ClientTransactionContext.associate(xid, timeoutSeconds, true);
            return;
        }
        XAException e = new XAException("unsupported start flags: " + flags);
        e.errorCode = XAException.XAER_INVAL;
        throw e;
    }

    @Override
    public void end(Xid xid, int flags) {
        ClientTransactionContext.disassociate();
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        client.beforeCompletion(xid);
        return client.prepare(xid);
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
        client.commit(xid, onePhase);
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        client.rollback(xid);
    }

    @Override
    public void forget(Xid xid) throws XAException {
        client.forget(xid);
    }

    @Override
    public Xid[] recover(int flag) throws XAException {
        return client.recover(flag);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Two resources are the same resource manager when they address the
     * same endpoint. A coordinator uses this to decide whether it may join
     * branches instead of running a separate two-phase exchange for each, so
     * being wrong here in the permissive direction would be a correctness bug
     * - hence an exact comparison of the endpoint rather than of the host.
     */
    @Override
    public boolean isSameRM(XAResource xares) {
        return xares instanceof HttpXAResource other && endpoint.equals(other.endpoint);
    }

    @Override
    public int getTransactionTimeout() {
        return timeoutSeconds;
    }

    @Override
    public boolean setTransactionTimeout(int seconds) {
        this.timeoutSeconds = Math.max(seconds, 0);
        return true;
    }

    @Override
    public String toString() {
        return "HttpXAResource[" + endpoint + ']';
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof HttpXAResource other && endpoint.equals(other.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoint);
    }
}
