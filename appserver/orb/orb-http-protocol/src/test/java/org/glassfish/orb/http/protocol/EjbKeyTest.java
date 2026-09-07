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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EjbKeyTest {

    /**
     * The layout must stay byte-identical to what
     * {@code POARemoteReferenceFactory} documents, otherwise an HTTP
     * invocation and an IIOP invocation would not name the same instance.
     * This checks the bytes explicitly rather than only round-tripping.
     */
    @Test
    @DisplayName("wire layout is ejbId(8) + length(4) + key, big endian")
    void layoutMatchesTheIiopKeyFormat() {
        EjbKey key = new EjbKey(0x0102030405060708L, new byte[] { 0x11, 0x22, 0x33 });
        byte[] bytes = key.toBytes();

        assertEquals(15, bytes.length);
        assertArrayEquals(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 },
                java.util.Arrays.copyOfRange(bytes, 0, 8));
        assertArrayEquals(new byte[] { 0, 0, 0, 3 },
                java.util.Arrays.copyOfRange(bytes, 8, 12));
        assertArrayEquals(new byte[] { 0x11, 0x22, 0x33 },
                java.util.Arrays.copyOfRange(bytes, 12, 15));
    }

    @Test
    @DisplayName("the VarHandle decode agrees with byte-by-byte arithmetic")
    void decodeAgreesWithManualArithmetic() throws Exception {
        Random random = new Random(7L);
        for (int i = 0; i < 500; i++) {
            long ejbId = random.nextLong();
            byte[] instanceKey = new byte[random.nextInt(24)];
            random.nextBytes(instanceKey);

            byte[] bytes = new EjbKey(ejbId, instanceKey).toBytes();

            long manual = 0;
            for (int b = 0; b < 8; b++) {
                manual = (manual << 8) | (bytes[b] & 0xFFL);
            }
            int manualLen = 0;
            for (int b = 8; b < 12; b++) {
                manualLen = (manualLen << 8) | (bytes[b] & 0xFF);
            }

            EjbKey decoded = EjbKey.fromBytes(bytes);
            assertEquals(manual, decoded.ejbId());
            assertEquals(manualLen, decoded.instanceKey().length);
            assertEquals(ejbId, decoded.ejbId());
            assertArrayEquals(instanceKey, decoded.instanceKey());
        }
    }

    @Test
    void roundTripsThroughAPathSegment() throws Exception {
        EjbKey key = new EjbKey(-1L, new byte[] { -128, 0, 127 });
        String segment = key.toPathSegment();

        assertFalse(segment.contains("/"), "a key must be a single path segment");
        assertFalse(segment.contains("+"), "a key must be URL safe");
        assertFalse(segment.contains("="), "padding would need escaping");
        assertEquals(key, EjbKey.fromPathSegment(segment));
    }

    @Test
    void recognisesTheHomeKey() {
        assertTrue(EjbKey.home(42L).isHome());
        assertFalse(new EjbKey(42L, new byte[] { 1, 2 }).isHome());
    }

    @Test
    @DisplayName("a truncated or lying length prefix is rejected, not read past")
    void rejectsMalformedKeys() {
        assertThrows(ProtocolException.class, () -> EjbKey.fromBytes(new byte[11]));

        byte[] lying = new EjbKey(1L, new byte[] { 1, 2, 3 }).toBytes();
        lying[11] = 100; // claims 100 bytes of instance key, only 3 follow
        assertThrows(ProtocolException.class, () -> EjbKey.fromBytes(lying));

        byte[] negative = new EjbKey(1L, new byte[] { 1 }).toBytes();
        negative[8] = (byte) 0xFF;
        assertThrows(ProtocolException.class, () -> EjbKey.fromBytes(negative));

        assertThrows(ProtocolException.class, () -> EjbKey.fromPathSegment("not base64!!"));
    }

    @Test
    void isDefensivelyCopied() {
        byte[] instanceKey = { 1, 2, 3 };
        EjbKey key = new EjbKey(1L, instanceKey);
        instanceKey[0] = 99;
        assertArrayEquals(new byte[] { 1, 2, 3 }, key.instanceKey());

        key.instanceKey()[0] = 99;
        assertArrayEquals(new byte[] { 1, 2, 3 }, key.instanceKey());
    }
}
