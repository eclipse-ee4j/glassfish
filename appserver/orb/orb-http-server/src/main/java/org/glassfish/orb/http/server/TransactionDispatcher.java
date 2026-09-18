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


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.glassfish.orb.http.protocol.PathScanner;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.ProtocolException;
import org.glassfish.orb.http.protocol.TxRoutes;
import org.glassfish.orb.http.protocol.Xids;

/** Serves the transaction operations. */
public final class TransactionDispatcher {

    /** Carries the XA error code of a failure back to the caller. */
    public static final String H_ERROR_CODE = "x-gf-txn-error-code";

    /** Content type of the recover listing: one base64url xid per line. */
    public static final String CT_XID_LIST = "text/x-gf-txn-xid-list";

    private final TransactionBridge transactions;
    private final SecurityBridge security;

    public TransactionDispatcher(TransactionBridge transactions) {
        this(transactions, SecurityBridge.NONE);
    }

    public TransactionDispatcher(TransactionBridge transactions, SecurityBridge security) {
        this.transactions = transactions;
        this.security = security;
    }

    public void dispatch(ServerExchange exchange) throws IOException {
        TxRoutes.Request request;
        try {
            PathScanner path = PathScanner.scan(
                    exchange.pathBytes(), exchange.pathOffset(), exchange.pathLength());
            request = TxRoutes.parse(path);
        } catch (ProtocolException e) {
            fail(exchange, Protocol.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        if (!TxRoutes.methodFor(request.operation()).equals(exchange.method())) {
            fail(exchange, Protocol.SC_BAD_REQUEST,
                    request.operation() + " must be a " + TxRoutes.methodFor(request.operation()));
            return;
        }

        Object securityToken = security.establish(exchange.authenticatedUser());
        try {
            perform(exchange, request);
        } catch (TransactionBridge.TransactionException e) {
            // The XA error code is the part the caller's transaction manager
            // acts on - XAER_NOTA and XA_RBROLLBACK mean very different things
            // to a coordinator - so it travels in its own header rather than
            // being flattened into a status code.
            exchange.setStatus(Protocol.SC_EXCEPTION);
            exchange.setResponseHeader(H_ERROR_CODE, Integer.toString(e.errorCode()));
            exchange.setResponseHeader("X-GF-Reason", String.valueOf(e.getMessage()).replace('\n', ' '));
        } finally {
            security.clear(securityToken);
        }
    }

    private void perform(ServerExchange exchange, TxRoutes.Request request)
            throws IOException, TransactionBridge.TransactionException {

        if (request.isUserTransaction()) {
            switch (request.operation()) {
                case TxRoutes.OP_BEGIN -> {
                    Xid xid = transactions.begin(longParameter(exchange, TxRoutes.PARAM_TIMEOUT));
                    exchange.setResponseHeader(TxRoutes.H_XID, Xids.toPathSegment(xid));
                    exchange.setStatus(Protocol.SC_NO_CONTENT);
                }
                case TxRoutes.OP_COMMIT -> {
                    transactions.commitUserTransaction(request.xid());
                    exchange.setStatus(Protocol.SC_NO_CONTENT);
                }
                case TxRoutes.OP_ROLLBACK -> {
                    transactions.rollbackUserTransaction(request.xid());
                    exchange.setStatus(Protocol.SC_NO_CONTENT);
                }
                default -> fail(exchange, Protocol.SC_NOT_FOUND,
                        "unknown user transaction operation: " + request.operation());
            }
            return;
        }

        switch (request.operation()) {
            case TxRoutes.OP_BEFORE_COMPLETION -> {
                transactions.beforeCompletion(request.xid());
                exchange.setStatus(Protocol.SC_NO_CONTENT);
            }
            case TxRoutes.OP_PREPARE -> {
                int vote = transactions.prepare(request.xid());
                if (vote == XAResource.XA_RDONLY) {
                    // A read-only branch drops out of the protocol here and
                    // must not be committed, so say so explicitly.
                    exchange.setResponseHeader(TxRoutes.H_READ_ONLY, "true");
                }
                exchange.setStatus(Protocol.SC_NO_CONTENT);
            }
            case TxRoutes.OP_COMMIT -> {
                transactions.commit(request.xid(), booleanParameter(exchange, TxRoutes.PARAM_ONE_PHASE));
                exchange.setStatus(Protocol.SC_NO_CONTENT);
            }
            case TxRoutes.OP_ROLLBACK -> {
                transactions.rollback(request.xid());
                exchange.setStatus(Protocol.SC_NO_CONTENT);
            }
            case TxRoutes.OP_FORGET -> {
                transactions.forget(request.xid());
                exchange.setStatus(Protocol.SC_NO_CONTENT);
            }
            case TxRoutes.OP_RECOVER -> writeRecoverListing(exchange);
            default -> fail(exchange, Protocol.SC_NOT_FOUND,
                    "unknown XA operation: " + request.operation());
        }
    }

    private void writeRecoverListing(ServerExchange exchange)
            throws IOException, TransactionBridge.TransactionException {
        int flags = (int) longParameter(exchange, TxRoutes.PARAM_FLAGS);
        Xid[] branches = transactions.recover(flags == 0 ? XAResource.TMSTARTRSCAN : flags);

        StringBuilder sb = new StringBuilder();
        if (branches != null) {
            for (Xid xid : branches) {
                sb.append(Xids.toPathSegment(xid)).append('\n');
            }
        }
        // Plain text, not a marshalled array: a recovery listing is exactly
        // the thing an operator may need to read out of a proxy log during an
        // incident, and it costs nothing to keep it legible.
        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
        exchange.setStatus(Protocol.SC_OK);
        exchange.setResponseHeader("Content-Type", CT_XID_LIST);
        exchange.writeBody(new ByteBuffer[] { ByteBuffer.wrap(body) });
    }

    private static long longParameter(ServerExchange exchange, String name) {
        String value = exchange.queryParameter(name);
        if (value == null || value.isEmpty()) {
            return 0;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static boolean booleanParameter(ServerExchange exchange, String name) {
        return Boolean.parseBoolean(exchange.queryParameter(name));
    }

    private static void fail(ServerExchange exchange, int status, String reason) {
        exchange.setStatus(status);
        exchange.setResponseHeader(H_ERROR_CODE, Integer.toString(XAException.XAER_INVAL));
        if (reason != null) {
            exchange.setResponseHeader("X-GF-Reason", reason.replace('\n', ' '));
        }
    }
}
