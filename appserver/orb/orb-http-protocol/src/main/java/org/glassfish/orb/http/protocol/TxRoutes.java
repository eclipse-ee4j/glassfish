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

package org.glassfish.orb.http.protocol;

import javax.transaction.xa.Xid;

/**
 * Builds and parses the transaction service paths.
 *
 * <pre>
 * POST {ctx}/txn/v1/ut/begin?timeout={seconds}
 * POST {ctx}/txn/v1/ut/commit/{xid}
 * POST {ctx}/txn/v1/ut/rollback/{xid}
 *
 * POST {ctx}/txn/v1/xa/bc/{xid}
 * POST {ctx}/txn/v1/xa/prep/{xid}
 * POST {ctx}/txn/v1/xa/commit/{xid}?opc={true|false}
 * POST {ctx}/txn/v1/xa/rollback/{xid}
 * POST {ctx}/txn/v1/xa/forget/{xid}
 * GET  {ctx}/txn/v1/xa/recover?flags={n}
 * </pre>
 *
 * <h2>Two modes, on purpose</h2>
 * The {@code ut} family is for a caller that has no transaction manager of its
 * own - a standalone client. It asks the server to begin the transaction and
 * to coordinate it; the client only holds the resulting {@link Xid} and says
 * when to commit.
 * <p>
 * The {@code xa} family is for a caller that <em>is</em> a transaction
 * manager, typically another application server. There the caller coordinates
 * and this server is one branch among several, driven through the ordinary
 * two-phase sequence. Both exist because a thin client genuinely cannot do the
 * second: two-phase commit needs a coordinator with a durable log, and that is
 * not something to put in a client jar.
 */
public final class TxRoutes {

    /** Transaction begun and coordinated by the server. */
    public static final String FAMILY_UT = "ut";
    /** Transaction coordinated by the caller; this server is a branch. */
    public static final String FAMILY_XA = "xa";

    public static final String OP_BEGIN = "begin";
    public static final String OP_COMMIT = "commit";
    public static final String OP_ROLLBACK = "rollback";
    public static final String OP_BEFORE_COMPLETION = "bc";
    public static final String OP_PREPARE = "prep";
    public static final String OP_FORGET = "forget";
    public static final String OP_RECOVER = "recover";

    /** Query parameter: one-phase commit optimisation. */
    public static final String PARAM_ONE_PHASE = "opc";
    /** Query parameter: transaction timeout, in seconds. */
    public static final String PARAM_TIMEOUT = "timeout";
    /** Query parameter: the recovery scan flags. */
    public static final String PARAM_FLAGS = "flags";

    /**
     * Response header carrying the Xid minted by {@link #OP_BEGIN}, and the
     * read-only vote from {@link #OP_PREPARE}.
     */
    public static final String H_XID = "x-gf-txn-xid";
    public static final String H_READ_ONLY = "x-gf-txn-read-only";

    private static final int CTX_SEGMENTS = 1;
    public static final int IDX_SERVICE = CTX_SEGMENTS;
    public static final int IDX_VERSION = CTX_SEGMENTS + 1;
    public static final int IDX_FAMILY = CTX_SEGMENTS + 2;
    public static final int IDX_OPERATION = CTX_SEGMENTS + 3;
    public static final int IDX_XID = CTX_SEGMENTS + 4;

    private TxRoutes() {
    }

    /** A parsed transaction request. {@code xid} is null for begin and recover. */
    public record Request(String family, String operation, Xid xid) {

        public boolean isUserTransaction() {
            return FAMILY_UT.equals(family);
        }
    }

    public static String beginPath(String contextPath) {
        return contextPath + '/' + Protocol.SVC_TXN + '/' + Protocol.VERSION_SEGMENT
                + '/' + FAMILY_UT + '/' + OP_BEGIN;
    }

    public static String recoverPath(String contextPath) {
        return contextPath + '/' + Protocol.SVC_TXN + '/' + Protocol.VERSION_SEGMENT
                + '/' + FAMILY_XA + '/' + OP_RECOVER;
    }

    public static String path(String contextPath, String family, String operation, Xid xid) {
        return contextPath + '/' + Protocol.SVC_TXN + '/' + Protocol.VERSION_SEGMENT
                + '/' + family + '/' + operation + '/' + Xids.toPathSegment(xid);
    }

    /**
     * @param path an already scanned request path
     * @return the parsed request
     * @throws ProtocolException if it is not a well formed transaction path
     */
    public static Request parse(PathScanner path) throws ProtocolException {
        if (path.count() < IDX_OPERATION + 1) {
            throw new ProtocolException("transaction path is too short: " + path.count() + " segments");
        }
        if (!path.segmentEquals(IDX_SERVICE, Protocol.SVC_TXN)) {
            throw new ProtocolException("not a transaction path");
        }
        if (!path.segmentEquals(IDX_VERSION, Protocol.VERSION_SEGMENT)) {
            throw new ProtocolException("unsupported protocol version");
        }
        String family = path.segment(IDX_FAMILY);
        if (!FAMILY_UT.equals(family) && !FAMILY_XA.equals(family)) {
            throw new ProtocolException("unknown transaction family: " + family);
        }
        String operation = path.segment(IDX_OPERATION);

        Xid xid = null;
        if (path.count() > IDX_XID) {
            xid = Xids.fromPathSegment(path.rawSegment(IDX_XID));
        } else if (!OP_BEGIN.equals(operation) && !OP_RECOVER.equals(operation)) {
            throw new ProtocolException(operation + " requires an xid in the path");
        }
        return new Request(family, operation, xid);
    }

    /** @return the HTTP method an operation is invoked with */
    public static String methodFor(String operation) {
        return OP_RECOVER.equals(operation) ? "GET" : "POST";
    }
}
