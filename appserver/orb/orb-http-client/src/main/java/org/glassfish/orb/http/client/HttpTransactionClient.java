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


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.ProtocolException;
import org.glassfish.orb.http.protocol.TxRoutes;
import org.glassfish.orb.http.protocol.Xids;

/**
 * The HTTP half of the transaction operations, shared by
 * {@link HttpUserTransaction} and {@link HttpXAResource}.
 *
 * <p>Failures come back as an {@link XAException} carrying the code the server
 * reported, not a generic one. That distinction is the whole protocol: a
 * coordinator does completely different things with {@code XAER_NOTA}, which
 * means the branch is already gone and can be forgotten, and
 * {@code XA_RBROLLBACK}, which means it must roll the whole transaction back.
 * Collapsing them into "something failed" would make recovery guesswork.
 */
public final class HttpTransactionClient {

    /** Header the dispatcher uses to report an XA error code. */
    static final String H_ERROR_CODE = "x-gf-txn-error-code";

    private final HttpTransport transport;
    private final ClientConfiguration config;
    private final String contextPath;

    public HttpTransactionClient(ClientConfiguration config, HttpTransport transport) {
        this.config = config;
        this.transport = transport;
        this.contextPath = config.contextPath();
    }

    // ---- server coordinated ------------------------------------------------

    Xid begin(long timeoutSeconds) throws XAException {
        String query = timeoutSeconds > 0 ? TxRoutes.PARAM_TIMEOUT + '=' + timeoutSeconds : null;
        try (HttpTransport.Response response = send("POST", TxRoutes.beginPath(contextPath), query)) {
            require(response, Protocol.SC_NO_CONTENT);
            String header = response.firstHeader(TxRoutes.H_XID);
            if (header == null) {
                throw xaException(XAException.XAER_RMERR, "server began a transaction but sent no xid");
            }
            return Xids.fromPathSegment(header);
        } catch (ProtocolException e) {
            throw xaException(XAException.XAER_PROTO, "malformed xid from the server: " + e.getMessage());
        } catch (IOException e) {
            throw xaException(XAException.XAER_RMFAIL, "begin failed: " + e);
        }
    }

    void commitUserTransaction(Xid xid) throws XAException {
        simple(TxRoutes.FAMILY_UT, TxRoutes.OP_COMMIT, xid, null);
    }

    void rollbackUserTransaction(Xid xid) throws XAException {
        simple(TxRoutes.FAMILY_UT, TxRoutes.OP_ROLLBACK, xid, null);
    }

    // ---- caller coordinated, this endpoint is a branch ---------------------

    void beforeCompletion(Xid xid) throws XAException {
        simple(TxRoutes.FAMILY_XA, TxRoutes.OP_BEFORE_COMPLETION, xid, null);
    }

    int prepare(Xid xid) throws XAException {
        try (HttpTransport.Response response =
                     send("POST", TxRoutes.path(contextPath, TxRoutes.FAMILY_XA, TxRoutes.OP_PREPARE, xid), null)) {
            require(response, Protocol.SC_NO_CONTENT);
            return Boolean.parseBoolean(response.firstHeader(TxRoutes.H_READ_ONLY))
                    ? XAResource.XA_RDONLY
                    : XAResource.XA_OK;
        } catch (IOException e) {
            throw xaException(XAException.XAER_RMFAIL, "prepare failed: " + e);
        }
    }

    void commit(Xid xid, boolean onePhase) throws XAException {
        simple(TxRoutes.FAMILY_XA, TxRoutes.OP_COMMIT, xid,
                TxRoutes.PARAM_ONE_PHASE + '=' + onePhase);
    }

    void rollback(Xid xid) throws XAException {
        simple(TxRoutes.FAMILY_XA, TxRoutes.OP_ROLLBACK, xid, null);
    }

    void forget(Xid xid) throws XAException {
        simple(TxRoutes.FAMILY_XA, TxRoutes.OP_FORGET, xid, null);
    }

    Xid[] recover(int flags) throws XAException {
        String query = TxRoutes.PARAM_FLAGS + '=' + flags;
        try (HttpTransport.Response response = send("GET", TxRoutes.recoverPath(contextPath), query)) {
            require(response, Protocol.SC_OK);
            List<Xid> branches = new ArrayList<>();
            InputStream body = response.body();
            String listing = body == null ? "" : new String(body.readAllBytes(), StandardCharsets.UTF_8);
            for (String line : listing.split("\n")) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    branches.add(Xids.fromPathSegment(trimmed));
                }
            }
            return branches.toArray(new Xid[0]);
        } catch (ProtocolException e) {
            throw xaException(XAException.XAER_PROTO, "malformed recovery listing: " + e.getMessage());
        } catch (IOException e) {
            throw xaException(XAException.XAER_RMFAIL, "recover failed: " + e);
        }
    }

    // ---- plumbing ----------------------------------------------------------

    private void simple(String family, String operation, Xid xid, String query) throws XAException {
        try (HttpTransport.Response response =
                     send(TxRoutes.methodFor(operation), TxRoutes.path(contextPath, family, operation, xid), query)) {
            require(response, Protocol.SC_NO_CONTENT);
        } catch (IOException e) {
            throw xaException(XAException.XAER_RMFAIL, operation + " failed: " + e);
        }
    }

    private HttpTransport.Response send(String method, String path, String query) throws IOException {
        URI uri = RequestUri.build(config.baseUri(), path, query);
        try {
            return transport.exchange(new HttpTransport.Request(method, uri, null, null, Map.of(), null));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted during " + method + ' ' + path, e);
        }
    }

    /** Consumes the response and turns anything unexpected into an XAException. */
    private void require(HttpTransport.Response response, int expected) throws IOException, XAException {
        if (response.status() == expected) {
            return;
        }
        String code = response.firstHeader(H_ERROR_CODE);
        String reason = response.firstHeader("X-GF-Reason");
        int errorCode = XAException.XAER_RMERR;
        if (code != null) {
            try {
                errorCode = Integer.parseInt(code.trim());
            } catch (NumberFormatException ignored) {
                // fall through to XAER_RMERR
            }
        }
        throw xaException(errorCode, reason != null ? reason : "HTTP " + response.status());
    }

    private static XAException xaException(int errorCode, String message) {
        XAException e = new XAException(message);
        e.errorCode = errorCode;
        return e;
    }
}
