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



import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Reads and writes the fixed prefix that precedes the marshalled payload of an
 * invocation body.
 * <p>
 * The prefix is written with plain {@link DataOutputStream} rather than through
 * the {@link Marshaller}, so that a peer can determine whether a transaction is
 * attached without first having to agree on a codec.
 */
public final class InvocationEnvelope {

    /** Guards against a hostile length prefix; an Xid component is 64 bytes. */
    private static final int MAX_XID_COMPONENT = 256;

    private InvocationEnvelope() {
    }

    public static void writeTxContext(OutputStream out, TxContext tx) throws IOException {
        DataOutputStream data = new DataOutputStream(out);
        data.writeByte(tx.type());
        if (tx.isPresent()) {
            byte[] global = tx.globalId();
            byte[] branch = tx.branchId();
            data.writeInt(tx.formatId());
            data.writeInt(global.length);
            data.write(global);
            data.writeInt(branch.length);
            data.write(branch);
        }
        data.flush();
    }

    public static TxContext readTxContext(InputStream in) throws IOException {
        DataInputStream data = new DataInputStream(in);
        int type;
        try {
            type = data.readByte();
        } catch (EOFException e) {
            throw new ProtocolException("empty invocation body", e);
        }
        if (type == TxContext.TYPE_NONE) {
            return TxContext.NONE;
        }
        if (type != TxContext.TYPE_REMOTE && type != TxContext.TYPE_OUTFLOWED) {
            throw new ProtocolException("unknown transaction context type: " + type);
        }
        int formatId = data.readInt();
        byte[] global = readComponent(data, "global transaction id");
        byte[] branch = readComponent(data, "branch qualifier");
        return new TxContext(type, formatId, global, branch);
    }

    private static byte[] readComponent(DataInputStream data, String what) throws IOException {
        int len = data.readInt();
        if (len < 0 || len > MAX_XID_COMPONENT) {
            throw new ProtocolException("implausible " + what + " length: " + len);
        }
        byte[] b = new byte[len];
        data.readFully(b);
        return b;
    }
}
