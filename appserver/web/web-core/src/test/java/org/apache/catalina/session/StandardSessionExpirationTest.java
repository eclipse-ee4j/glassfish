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

package org.apache.catalina.session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.catalina.Manager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Session inactivity is measured with {@link System#nanoTime()}, so system clock changes do not affect expiration.
 */
class StandardSessionExpirationTest {

    private static final int INTERVAL_SECONDS = 60;
    private static final long INTERVAL_MILLIS = SECONDS.toMillis(INTERVAL_SECONDS);
    private static final long INTERVAL_NANOS = SECONDS.toNanos(INTERVAL_SECONDS);

    private StandardSession session;

    @BeforeEach
    void createSession() {
        final Manager manager = createNiceMock(Manager.class);
        replay(manager);
        session = new StandardSession(manager);
        session.setCreationTime(System.currentTimeMillis());
        session.setMaxInactiveInterval(INTERVAL_SECONDS);
    }

    @Test
    void wallClockAccessTimeDoesNotAffectExpiration() {
        // Equivalent to the system clock moving forward, then back, by an hour
        session.thisAccessedTime -= HOURS.toMillis(1);
        assertFalse(session.hasExpired());

        session.thisAccessedTime += HOURS.toMillis(2);
        assertFalse(session.hasExpired());
    }

    @Test
    void expiresAfterInactivityEvenIfWallClockMovedBack() {
        session.thisAccessedTime = System.currentTimeMillis() + HOURS.toMillis(1);
        session.thisAccessedNanos = System.nanoTime() - INTERVAL_NANOS;
        assertTrue(session.hasExpired());
    }

    @Test
    void accessRestartsInactivity() {
        session.thisAccessedNanos = System.nanoTime() - 2 * INTERVAL_NANOS;
        assertTrue(session.hasExpired());

        session.access();
        assertFalse(session.hasExpired());
    }

    @Test
    void negativeIntervalNeverExpires() {
        session.setMaxInactiveInterval(-1);
        session.thisAccessedNanos = System.nanoTime() - HOURS.toNanos(24);
        assertFalse(session.hasExpired());
    }

    @Test
    void setCreationTimeStartsInactivityAtThatTime() {
        session.setCreationTime(System.currentTimeMillis() - 2 * INTERVAL_MILLIS);
        assertTrue(session.hasExpired());

        session.setCreationTime(System.currentTimeMillis());
        assertFalse(session.hasExpired());
    }

    @Test
    void deserializedSessionMeasuresInactivityFromStoredAccessTime() throws Exception {
        session.setCreationTime(System.currentTimeMillis() - 2 * INTERVAL_MILLIS);
        assertTrue(roundTrip(session).hasExpired());

        session.setCreationTime(System.currentTimeMillis());
        assertFalse(roundTrip(session).hasExpired());
    }

    @Test
    void toNanoTimeDoesNotOverflow() {
        final long now = System.nanoTime();
        assertTrue(now - StandardSession.toNanoTime(Long.MIN_VALUE) > 0);
        assertTrue(now - StandardSession.toNanoTime(-1L) > 0);
        assertTrue(StandardSession.toNanoTime(Long.MAX_VALUE) - now > 0);
    }

    private static StandardSession roundTrip(StandardSession session) throws Exception {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(session);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (StandardSession) in.readObject();
        }
    }
}
