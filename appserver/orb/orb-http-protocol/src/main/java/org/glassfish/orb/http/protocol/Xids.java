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
import java.util.Base64;

import javax.transaction.xa.Xid;

/**
 * Encoding of {@link Xid} for this protocol.
 * <p>
 * An Xid is three components - a format identifier, a global transaction id
 * and a branch qualifier - and the two byte arrays are at most 64 bytes each
 * by the XA specification. They travel in two places: inside the body of an
 * invocation, where {@link InvocationEnvelope} writes them, and as a path
 * segment on the transaction operations, which is what {@link #toPathSegment}
 * produces.
 * <p>
 * {@code javax.transaction.xa} is a JDK module, so nothing here adds a
 * dependency.
 */
public final class Xids {

    /** The XA specification's limit on each of the two components. */
    public static final int MAX_COMPONENT_LENGTH = 64;

    private Xids() {
    }

    /** A plain immutable {@link Xid}. */
    public static final class SimpleXid implements Xid {

        private final int formatId;
        private final byte[] globalTransactionId;
        private final byte[] branchQualifier;

        public SimpleXid(int formatId, byte[] globalTransactionId, byte[] branchQualifier) {
            this.formatId = formatId;
            this.globalTransactionId = globalTransactionId == null ? new byte[0] : globalTransactionId.clone();
            this.branchQualifier = branchQualifier == null ? new byte[0] : branchQualifier.clone();
        }

        @Override
        public int getFormatId() {
            return formatId;
        }

        @Override
        public byte[] getGlobalTransactionId() {
            return globalTransactionId.clone();
        }

        @Override
        public byte[] getBranchQualifier() {
            return branchQualifier.clone();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Xid other && sameXid(this, other);
        }

        @Override
        public int hashCode() {
            return 31 * (31 * formatId + Arrays.hashCode(globalTransactionId))
                    + Arrays.hashCode(branchQualifier);
        }

        @Override
        public String toString() {
            return "Xid[format=" + formatId
                    + ", gtrid=" + hex(globalTransactionId)
                    + ", bqual=" + hex(branchQualifier) + ']';
        }
    }

    /**
     * Compares two Xids by value.
     * <p>
     * {@link Xid} is an interface and does not specify {@code equals}, so two
     * implementations describing the same transaction are not equal to each
     * other. Anything keying a map by Xid has to use this instead - which is
     * why {@link #key(Xid)} exists.
     *
     * @param a first xid, may be null
     * @param b second xid, may be null
     * @return whether both describe the same transaction branch
     */
    public static boolean sameXid(Xid a, Xid b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.getFormatId() == b.getFormatId()
                && Arrays.equals(a.getGlobalTransactionId(), b.getGlobalTransactionId())
                && Arrays.equals(a.getBranchQualifier(), b.getBranchQualifier());
    }

    /**
     * @param xid the transaction branch
     * @return a value-based key usable in a hash map, since Xid itself is not
     */
    public static String key(Xid xid) {
        return xid.getFormatId() + ":" + hex(xid.getGlobalTransactionId())
                + ":" + hex(xid.getBranchQualifier());
    }

    // ---- wire form -------------------------------------------------------

    /**
     * @param xid the branch to encode
     * @return {@code formatId(4) | gtridLen(4) | gtrid | bqualLen(4) | bqual},
     *         big endian, matching what {@link InvocationEnvelope} writes
     */
    public static byte[] toBytes(Xid xid) {
        byte[] global = xid.getGlobalTransactionId();
        byte[] branch = xid.getBranchQualifier();
        byte[] out = new byte[12 + global.length + branch.length];
        putInt(out, 0, xid.getFormatId());
        putInt(out, 4, global.length);
        System.arraycopy(global, 0, out, 8, global.length);
        putInt(out, 8 + global.length, branch.length);
        System.arraycopy(branch, 0, out, 12 + global.length, branch.length);
        return out;
    }

    public static Xid fromBytes(byte[] buf) throws ProtocolException {
        if (buf.length < 12) {
            throw new ProtocolException("xid truncated: " + buf.length + " bytes");
        }
        int formatId = getInt(buf, 0);
        int globalLength = getInt(buf, 4);
        checkLength(globalLength, buf.length - 12, "global transaction id");
        byte[] global = Arrays.copyOfRange(buf, 8, 8 + globalLength);

        int branchOffset = 8 + globalLength;
        if (branchOffset + 4 > buf.length) {
            throw new ProtocolException("xid truncated before the branch qualifier");
        }
        int branchLength = getInt(buf, branchOffset);
        checkLength(branchLength, buf.length - branchOffset - 4, "branch qualifier");
        byte[] branch = Arrays.copyOfRange(buf, branchOffset + 4, branchOffset + 4 + branchLength);

        return new SimpleXid(formatId, global, branch);
    }

    private static void checkLength(int declared, int available, String what) throws ProtocolException {
        if (declared < 0 || declared > MAX_COMPONENT_LENGTH) {
            throw new ProtocolException("implausible " + what + " length: " + declared);
        }
        if (declared > available) {
            throw new ProtocolException(what + " declares " + declared
                    + " bytes but only " + available + " remain");
        }
    }

    /** Base64url, unpadded, so an Xid is a single safe path segment. */
    public static String toPathSegment(Xid xid) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(toBytes(xid));
    }

    public static Xid fromPathSegment(String segment) throws ProtocolException {
        try {
            return fromBytes(Base64.getUrlDecoder().decode(segment));
        } catch (IllegalArgumentException e) {
            throw new ProtocolException("malformed xid segment", e);
        }
    }

    /** Converts an Xid into the transaction context carried by an invocation. */
    public static TxContext toContext(Xid xid, int type) {
        return new TxContext(type, xid.getFormatId(),
                xid.getGlobalTransactionId(), xid.getBranchQualifier());
    }

    /** @return the Xid a context describes, or null if it carries none */
    public static Xid fromContext(TxContext context) {
        return context.isPresent()
                ? new SimpleXid(context.formatId(), context.globalId(), context.branchId())
                : null;
    }

    private static void putInt(byte[] buf, int offset, int value) {
        buf[offset] = (byte) (value >>> 24);
        buf[offset + 1] = (byte) (value >>> 16);
        buf[offset + 2] = (byte) (value >>> 8);
        buf[offset + 3] = (byte) value;
    }

    private static int getInt(byte[] buf, int offset) {
        return ((buf[offset] & 0xFF) << 24)
                | ((buf[offset + 1] & 0xFF) << 16)
                | ((buf[offset + 2] & 0xFF) << 8)
                | (buf[offset + 3] & 0xFF);
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
