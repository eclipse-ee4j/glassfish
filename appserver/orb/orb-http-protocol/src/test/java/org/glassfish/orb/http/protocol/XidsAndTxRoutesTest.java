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


import java.util.Random;

import javax.transaction.xa.Xid;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XidsAndTxRoutesTest {

    private static Xid xid(int format, int gtridLength, int bqualLength) {
        Random random = new Random(format * 31L + gtridLength);
        byte[] gtrid = new byte[gtridLength];
        byte[] bqual = new byte[bqualLength];
        random.nextBytes(gtrid);
        random.nextBytes(bqual);
        return new Xids.SimpleXid(format, gtrid, bqual);
    }

    @Test
    void xidRoundTripsThroughBytes() throws Exception {
        for (int gtrid = 0; gtrid <= Xids.MAX_COMPONENT_LENGTH; gtrid += 16) {
            for (int bqual = 0; bqual <= Xids.MAX_COMPONENT_LENGTH; bqual += 16) {
                Xid original = xid(0x12345678, gtrid, bqual);
                Xid decoded = Xids.fromBytes(Xids.toBytes(original));

                assertEquals(original.getFormatId(), decoded.getFormatId());
                assertArrayEquals(original.getGlobalTransactionId(), decoded.getGlobalTransactionId());
                assertArrayEquals(original.getBranchQualifier(), decoded.getBranchQualifier());
                assertTrue(Xids.sameXid(original, decoded));
            }
        }
    }

    @Test
    void xidRoundTripsThroughAPathSegment() throws Exception {
        Xid original = xid(-1, 64, 64);
        String segment = Xids.toPathSegment(original);

        assertFalse(segment.contains("/"), "an xid must be a single path segment");
        assertFalse(segment.contains("+") || segment.contains("="), "and URL safe");
        assertTrue(Xids.sameXid(original, Xids.fromPathSegment(segment)));
    }

    @Test
    @DisplayName("a lying or oversized length prefix is refused, not trusted")
    void malformedXidsAreRejected() {
        assertThrows(ProtocolException.class, () -> Xids.fromBytes(new byte[11]));

        byte[] lying = Xids.toBytes(xid(1, 4, 4));
        lying[7] = (byte) 200; // claims a 200 byte gtrid
        assertThrows(ProtocolException.class, () -> Xids.fromBytes(lying));

        byte[] oversized = Xids.toBytes(xid(1, 4, 4));
        oversized[7] = (byte) 65; // just over the XA limit of 64
        assertThrows(ProtocolException.class, () -> Xids.fromBytes(oversized));

        assertThrows(ProtocolException.class, () -> Xids.fromPathSegment("not base64!!"));
    }

    @Test
    @DisplayName("Xid has no equals of its own, so comparison must be by value")
    void xidsAreComparedByValue() {
        Xid mine = new Xids.SimpleXid(1, new byte[] { 1, 2 }, new byte[] { 3 });
        Xid foreign = new Xid() {
            @Override
            public int getFormatId() {
                return 1;
            }

            @Override
            public byte[] getGlobalTransactionId() {
                return new byte[] { 1, 2 };
            }

            @Override
            public byte[] getBranchQualifier() {
                return new byte[] { 3 };
            }
        };

        assertTrue(Xids.sameXid(mine, foreign), "a foreign Xid implementation must still compare equal");
        assertEquals(Xids.key(mine), Xids.key(foreign));
        assertFalse(Xids.sameXid(mine, null));
        assertFalse(Xids.sameXid(mine, new Xids.SimpleXid(2, new byte[] { 1, 2 }, new byte[] { 3 })));
    }

    @Test
    void contextConversionRoundTrips() {
        Xid original = xid(7, 8, 4);
        TxContext context = Xids.toContext(original, TxContext.TYPE_OUTFLOWED);

        assertEquals(TxContext.TYPE_OUTFLOWED, context.type());
        assertTrue(Xids.sameXid(original, Xids.fromContext(context)));
        assertNull(Xids.fromContext(TxContext.NONE));
    }

    @Test
    void transactionPathsRoundTrip() throws Exception {
        Xid xid = xid(3, 16, 8);

        String prepare = TxRoutes.path(Protocol.CONTEXT_PATH, TxRoutes.FAMILY_XA, TxRoutes.OP_PREPARE, xid);
        assertTrue(prepare.startsWith("/glassfish-services/txn/v1/xa/prep/"));

        TxRoutes.Request parsed = TxRoutes.parse(PathScanner.scan(prepare));
        assertEquals(TxRoutes.FAMILY_XA, parsed.family());
        assertEquals(TxRoutes.OP_PREPARE, parsed.operation());
        assertTrue(Xids.sameXid(xid, parsed.xid()));
        assertFalse(parsed.isUserTransaction());
    }

    @Test
    void beginAndRecoverCarryNoXid() throws Exception {
        TxRoutes.Request begin = TxRoutes.parse(PathScanner.scan(TxRoutes.beginPath(Protocol.CONTEXT_PATH)));
        assertEquals(TxRoutes.OP_BEGIN, begin.operation());
        assertTrue(begin.isUserTransaction());
        assertNull(begin.xid());

        TxRoutes.Request recover = TxRoutes.parse(PathScanner.scan(TxRoutes.recoverPath(Protocol.CONTEXT_PATH)));
        assertEquals(TxRoutes.OP_RECOVER, recover.operation());
        assertNull(recover.xid());
    }

    @Test
    void anOperationThatNeedsAnXidIsRejectedWithout() {
        String path = Protocol.CONTEXT_PATH + "/txn/v1/xa/prep";
        assertThrows(ProtocolException.class, () -> TxRoutes.parse(PathScanner.scan(path)));
    }

    @Test
    void unknownFamiliesAndVersionsAreRejected() {
        assertThrows(ProtocolException.class,
                () -> TxRoutes.parse(PathScanner.scan(Protocol.CONTEXT_PATH + "/txn/v1/zz/prep/AAAA")));
        assertThrows(ProtocolException.class,
                () -> TxRoutes.parse(PathScanner.scan(Protocol.CONTEXT_PATH + "/txn/v9/xa/prep/AAAA")));
        assertThrows(ProtocolException.class,
                () -> TxRoutes.parse(PathScanner.scan(Protocol.CONTEXT_PATH + "/naming/v1/lookup/x")));
    }

    @Test
    void recoverIsTheOnlyGet() {
        assertEquals("GET", TxRoutes.methodFor(TxRoutes.OP_RECOVER));
        assertEquals("POST", TxRoutes.methodFor(TxRoutes.OP_PREPARE));
        assertEquals("POST", TxRoutes.methodFor(TxRoutes.OP_BEGIN));
        assertEquals("POST", TxRoutes.methodFor(TxRoutes.OP_COMMIT));
    }
}
