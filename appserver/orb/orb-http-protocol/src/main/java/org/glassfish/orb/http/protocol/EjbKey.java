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



import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Base64;

/**
 * The GlassFish EJB object key, and its encoding for an HTTP path segment.
 * <p>
 * The layout is <em>not</em> invented here - it is the key format the IIOP
 * path already uses, documented in
 * {@code org.glassfish.enterprise.iiop.impl.POARemoteReferenceFactory}:
 *
 * <pre>
 * | EJB ID (8, big endian) | INSTANCEKEY LENGTH (4, big endian) | INSTANCEKEY (n) |
 * </pre>
 *
 * Keeping the identical layout is what lets an HTTP invocation land on the
 * existing container dispatch ({@code EjbContainerFacade.getTargetObject})
 * with no translation, and lets an HTTP client and an IIOP client name the
 * same stateful session bean instance.
 *
 * <h2>On decoding cost</h2>
 * The IIOP path reads these fields a byte at a time through
 * {@code com.sun.enterprise.util.Utility.bytesToInt}. Here the 8- and 4-byte
 * fields are read as whole machine words through a {@link VarHandle} byte-array
 * view, which HotSpot lowers to a single (bswapped) load. It is a small win in
 * absolute terms - a remote invocation is dominated by TLS and serialization,
 * not by twelve bytes of header - but it is free, allocation-free, and it is
 * on the per-invocation path.
 */
public final class EjbKey {

    /**
     * Big-endian views over a {@code byte[]}. Alignment is not required:
     * {@code byteArrayViewVarHandle} permits unaligned access on platforms
     * that support it, and the JIT emits a plain load plus a byte swap.
     */
    private static final VarHandle LONG_BE =
            MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN);
    private static final VarHandle INT_BE =
            MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.BIG_ENDIAN);

    public static final int EJBID_OFFSET = 0;
    public static final int INSTANCEKEYLEN_OFFSET = 8;
    public static final int INSTANCEKEY_OFFSET = 12;
    public static final int HEADER_LENGTH = INSTANCEKEY_OFFSET;

    /** Single-byte instance key that names the home rather than an instance. */
    public static final byte HOME_KEY = (byte) 0xFF;

    private final long ejbId;
    private final byte[] instanceKey;

    public EjbKey(long ejbId, byte[] instanceKey) {
        this.ejbId = ejbId;
        this.instanceKey = instanceKey.clone();
    }

    public long ejbId() {
        return ejbId;
    }

    /** @return a defensive copy of the instance key. */
    public byte[] instanceKey() {
        return instanceKey.clone();
    }

    public boolean isHome() {
        return instanceKey.length == 1 && instanceKey[0] == HOME_KEY;
    }

    // ---- wire form -------------------------------------------------------

    /** Encodes to the 12-byte header plus the instance key. */
    public byte[] toBytes() {
        byte[] out = new byte[HEADER_LENGTH + instanceKey.length];
        LONG_BE.set(out, EJBID_OFFSET, ejbId);
        INT_BE.set(out, INSTANCEKEYLEN_OFFSET, instanceKey.length);
        System.arraycopy(instanceKey, 0, out, INSTANCEKEY_OFFSET, instanceKey.length);
        return out;
    }

    /**
     * Decodes a key from {@code buf} starting at {@code off}.
     *
     * @throws ProtocolException if the buffer is truncated or the declared
     *                           instance-key length does not fit
     */
    public static EjbKey fromBytes(byte[] buf, int off, int len) throws ProtocolException {
        if (len < HEADER_LENGTH) {
            throw new ProtocolException("EJB key truncated: " + len + " bytes, need at least " + HEADER_LENGTH);
        }
        long id = (long) LONG_BE.get(buf, off + EJBID_OFFSET);
        int keyLen = (int) INT_BE.get(buf, off + INSTANCEKEYLEN_OFFSET);
        if (keyLen < 0 || HEADER_LENGTH + keyLen > len) {
            throw new ProtocolException("EJB key declares instance key of " + keyLen
                    + " bytes but only " + (len - HEADER_LENGTH) + " remain");
        }
        byte[] key = Arrays.copyOfRange(buf, off + INSTANCEKEY_OFFSET, off + INSTANCEKEY_OFFSET + keyLen);
        return new EjbKey(id, key);
    }

    public static EjbKey fromBytes(byte[] buf) throws ProtocolException {
        return fromBytes(buf, 0, buf.length);
    }

    // ---- path segment form -----------------------------------------------

    /**
     * Base64url, unpadded, so the key is a single safe path segment.
     * <p>
     * Note this uses {@link Base64} rather than a hand-written SWAR encoder on
     * purpose: HotSpot intrinsifies {@code Base64.Encoder.encode} and
     * {@code Decoder.decode} to vectorised stubs
     * ({@code _base64_encodeBlock} / {@code _base64_decodeBlock}), so the JDK
     * implementation is already SIMD and a hand-rolled one would be slower.
     */
    public String toPathSegment() {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(toBytes());
    }

    public static EjbKey fromPathSegment(String segment) throws ProtocolException {
        try {
            return fromBytes(Base64.getUrlDecoder().decode(segment));
        } catch (IllegalArgumentException e) {
            throw new ProtocolException("malformed EJB key segment", e);
        }
    }

    /** Key naming the home of {@code ejbId}. */
    public static EjbKey home(long ejbId) {
        return new EjbKey(ejbId, new byte[] { HOME_KEY });
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof EjbKey other
                && other.ejbId == ejbId
                && Arrays.equals(other.instanceKey, instanceKey);
    }

    @Override
    public int hashCode() {
        return 31 * Long.hashCode(ejbId) + Arrays.hashCode(instanceKey);
    }

    @Override
    public String toString() {
        return "EjbKey[ejbId=" + ejbId + ", instanceKey=" + instanceKey.length + " bytes"
                + (isHome() ? ", home" : "") + "]";
    }
}
