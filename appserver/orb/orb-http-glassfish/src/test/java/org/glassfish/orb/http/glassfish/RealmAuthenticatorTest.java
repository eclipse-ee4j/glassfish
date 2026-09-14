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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * What happens before a realm is ever consulted.
 * <p>
 * The decision this class makes is not "who is the caller" but "is there a
 * claim here at all, and is it well formed". Getting that wrong in the
 * permissive direction is the whole risk: a malformed credential treated as
 * absent runs the call as anonymous, which is a caller being given fewer
 * rights than it asked for on the strength of a header nobody could read.
 */
class RealmAuthenticatorTest {

    @AfterEach
    void tearDown() {
        RealmAuthenticator.clear();
    }

    private static String basic(String raw) {
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("no header at all is an anonymous call, not a refusal")
    void noCredentialIsAnonymous() {
        assertNull(RealmAuthenticator.authenticate(null));
        assertNull(RealmAuthenticator.authenticatedSubject());
    }

    @Test
    @DisplayName("a scheme this transport does not implement is not a refusal either")
    void anotherSchemeIsIgnored() {
        // Bearer tokens and client certificates are established elsewhere.
        // There is nothing here to believe and nothing to reject.
        assertNull(RealmAuthenticator.authenticate("Bearer abc.def.ghi"));
        assertNull(RealmAuthenticator.authenticate("Negotiate YII="));
    }

    @Test
    @DisplayName("the scheme is matched without regard to case, as HTTP requires")
    void theSchemeIsCaseInsensitive() {
        // A server that only accepted "Basic " would refuse a conforming
        // client for a reason the client cannot see.
        assertThrows(SecurityException.class,
                () -> RealmAuthenticator.authenticate("basic " + Base64.getEncoder()
                        .encodeToString("alice:secret".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    @DisplayName("a credential that is not readable is refused, never treated as absent")
    void aMalformedCredentialIsRefused() {
        // Each of these is a header that claims an identity and fails to name
        // one. Returning null would run the call as nobody, which is a quieter
        // outcome than it deserves.
        assertThrows(SecurityException.class, () -> RealmAuthenticator.authenticate("Basic !!!not-base64!!!"));
        assertThrows(SecurityException.class, () -> RealmAuthenticator.authenticate(basic("no-colon-here")));
        assertThrows(SecurityException.class, () -> RealmAuthenticator.authenticate("Basic "));
    }

    @Test
    @DisplayName("an empty user name is still a claim, and still has to pass")
    void anEmptyUserIsNotAnonymous() {
        // ":secret" parses, so it reaches the realm - and outside a container
        // there is none, which must be a refusal rather than a free pass.
        assertThrows(SecurityException.class, () -> RealmAuthenticator.authenticate(basic(":secret")));
    }

    @Test
    @DisplayName("with no realm to ask, a well formed credential is refused")
    void withoutARealmNothingIsBelieved() {
        // This runs outside a container, so no realm is configured. The answer
        // must be refusal: accepting a credential nobody checked is the one
        // outcome that must never happen, and "there was nothing to check it
        // with" is not a reason to accept it.
        assertThrows(SecurityException.class, () -> RealmAuthenticator.authenticate(basic("alice:secret")));
        assertNull(RealmAuthenticator.authenticatedSubject(), "no identity may survive a refusal");
    }
}
