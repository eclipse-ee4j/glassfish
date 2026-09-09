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




import java.util.Arrays;

/**
 * The transaction context prefixed to an invocation body.
 * <p>
 * Transaction propagation is <strong>not implemented</strong>: GlassFish
 * propagates transactions over OTS through {@code com.sun.jts}, roughly 32,000
 * lines whose coordination protocol has no counterpart here, and building an
 * XA-over-HTTP equivalent is a project in its own right. What this type does
 * is reserve the space on the wire, so that adding it later is a change of
 * behaviour rather than a change of protocol version. Every invocation
 * currently writes {@link #NONE}.
 * <p>
 * The layout matches the WildFly wire specification: one type byte, and when
 * it is non-zero an {@code int} format id followed by two length-prefixed byte
 * arrays holding the global transaction id and the branch qualifier - i.e. the
 * three components of an {@code javax.transaction.xa.Xid}.
 */
public record TxContext(int type, int formatId, byte[] globalId, byte[] branchId) {

    /** No transaction is associated with this invocation. */
    public static final int TYPE_NONE = 0;
    /** The transaction is coordinated by a remote transaction manager. */
    public static final int TYPE_REMOTE = 1;
    /** The transaction was outflowed from the caller's local manager. */
    public static final int TYPE_OUTFLOWED = 2;

    private static final byte[] EMPTY = new byte[0];

    /** The only value this implementation currently sends. */
    public static final TxContext NONE = new TxContext(TYPE_NONE, 0, EMPTY, EMPTY);

    public TxContext {
        if (type < TYPE_NONE || type > TYPE_OUTFLOWED) {
            throw new IllegalArgumentException("unknown transaction context type: " + type);
        }
        globalId = globalId == null ? EMPTY : globalId.clone();
        branchId = branchId == null ? EMPTY : branchId.clone();
    }

    public boolean isPresent() {
        return type != TYPE_NONE;
    }

    @Override
    public byte[] globalId() {
        return globalId.clone();
    }

    @Override
    public byte[] branchId() {
        return branchId.clone();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TxContext other
                && other.type == type
                && other.formatId == formatId
                && Arrays.equals(other.globalId, globalId)
                && Arrays.equals(other.branchId, branchId);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * type + formatId) + Arrays.hashCode(globalId);
    }

    @Override
    public String toString() {
        return isPresent()
                ? "TxContext[type=" + type + ", formatId=" + formatId + "]"
                : "TxContext[none]";
    }
}
