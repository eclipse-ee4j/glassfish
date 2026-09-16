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


package org.glassfish.orb.http.glassfish;

import com.sun.enterprise.security.SecurityContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * The identity a request runs under, and what happens to it afterwards.
 * <p>
 * These are pooled threads. An identity installed for one request and left
 * behind is not a leak of memory but of authority: the next request to land on
 * that thread would run as somebody who never made it.
 */
class GlassFishSecurityBridgeTest {

    private final GlassFishSecurityBridge bridge = new GlassFishSecurityBridge();

    @AfterEach
    void tearDown() {
        SecurityContext.setCurrent(null);
        RealmAuthenticator.clear();
    }

    @Test
    @DisplayName("an authenticated caller runs as itself")
    void anIdentityIsInstalled() {
        Object token = bridge.establish("alice");

        SecurityContext current = SecurityContext.getCurrent();
        assertNotNull(current);
        assertEquals("alice", current.getCallerPrincipal().getName());

        bridge.clear(token);
    }

    @Test
    @DisplayName("an anonymous call does not install an empty identity over what was there")
    void anonymousLeavesTheContextAlone() {
        SecurityContext before = new SecurityContext("existing", new javax.security.auth.Subject());
        SecurityContext.setCurrent(before);

        Object token = bridge.establish(null);

        // Installing an empty identity here would silently demote whatever the
        // thread was already carrying.
        assertSame(before, SecurityContext.getCurrent());
        bridge.clear(token);
    }

    @Test
    @DisplayName("the thread is handed back as it was found")
    void theContextIsRestored() {
        SecurityContext before = new SecurityContext("existing", new javax.security.auth.Subject());
        SecurityContext.setCurrent(before);

        Object token = bridge.establish("alice");
        assertEquals("alice", SecurityContext.getCurrent().getCallerPrincipal().getName());

        bridge.clear(token);

        // Restored, not cleared: the thread came from a pool and may have been
        // carrying an identity that is not ours to discard.
        assertSame(before, SecurityContext.getCurrent());
    }

    @Test
    @DisplayName("no identity outlives the request that established it")
    void theIdentityDoesNotOutliveTheRequest() {
        Object token = bridge.establish("alice");
        bridge.clear(token);

        // Not asserted as null: this API answers with a default
        // unauthenticated context rather than nothing, which is the same
        // outcome by a different name. What matters is that the next request
        // on this thread does not find itself running as alice.
        SecurityContext after = SecurityContext.getCurrent();
        String caller = after == null || after.getCallerPrincipal() == null
                ? null
                : after.getCallerPrincipal().getName();
        assertNotEquals("alice", caller);
    }
}
