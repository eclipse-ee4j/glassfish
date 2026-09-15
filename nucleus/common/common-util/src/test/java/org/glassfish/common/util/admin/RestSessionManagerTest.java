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

package org.glassfish.common.util.admin;

import javax.security.auth.Subject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the lifetime of REST sessions created by {@link RestSessionManager}.
 */
public class RestSessionManagerTest {

    private static final String CLIENT_ADDRESS = "127.0.0.1";

    private final RestSessionManager manager = new RestSessionManager();

    /**
     * The admin session timeout can be set to 0, meaning that the session never expires.
     * Such a session used to be treated as expired right away.
     */
    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    public void sessionWithNonPositiveTimeoutNeverExpires(int timeoutInMinutes) {
        Subject subject = new Subject();
        String sessionId = manager.createSession(CLIENT_ADDRESS, subject, timeoutInMinutes);
        assertNotNull(sessionId, "session id");
        assertSame(subject, manager.authenticate(sessionId, CLIENT_ADDRESS), "authenticated subject");
    }

    @Test
    public void sessionWithPositiveTimeoutIsActiveUntilItExpires() {
        Subject subject = new Subject();
        String sessionId = manager.createSession(CLIENT_ADDRESS, subject, 30);
        assertSame(subject, manager.authenticate(sessionId, CLIENT_ADDRESS), "authenticated subject");
    }

    @Test
    public void sessionIsBoundToTheClientAddress() {
        String sessionId = manager.createSession(CLIENT_ADDRESS, new Subject(), 0);
        assertNull(manager.authenticate(sessionId, "203.0.113.42"), "subject of another client");
        assertNull(manager.authenticate(sessionId, CLIENT_ADDRESS), "subject after a rejected request");
    }

    @Test
    public void unknownSessionIsNotAuthenticated() {
        assertNull(manager.authenticate("no-such-session", CLIENT_ADDRESS), "subject of an unknown session");
        assertNull(manager.authenticate(null, CLIENT_ADDRESS), "subject without a session id");
    }

    @Test
    public void deletedSessionIsNotAuthenticated() {
        String sessionId = manager.createSession(CLIENT_ADDRESS, new Subject(), 0);
        assertTrue(manager.deleteSession(sessionId), "session deleted");
        assertFalse(manager.deleteSession(sessionId), "session deleted twice");
        assertNull(manager.authenticate(sessionId, CLIENT_ADDRESS), "subject of a deleted session");
    }
}
