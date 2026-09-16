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

import com.sun.enterprise.security.auth.login.LoginContextDriver;
import com.sun.enterprise.security.auth.realm.Realm;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.security.auth.Subject;

/**
 * Turns an {@code Authorization} header into an identity the server believes.
 * <p>
 * An {@code Authorization: Basic} header names a user; it does not establish
 * that the request came from them. Reading the name out of it and handing that
 * on would let any caller assert any identity - not a weaker authentication
 * than IIOP's, but none at all wearing the shape of one. The credential is
 * therefore checked against the server's realm, which is the same check the
 * web container makes for the same header.
 * <p>
 * A credential that is absent and a credential that is wrong are different
 * answers. Absent means the call is anonymous, which is what an unsecured bean
 * expects. Wrong is refused: a caller whose password failed must not quietly
 * proceed with fewer rights than it asked for, because the effect is an
 * authorization decision made on a credential nobody accepted.
 */
final class RealmAuthenticator {

    private static final Logger LOG = System.getLogger(RealmAuthenticator.class.getName());

    private static final String BASIC = "Basic ";

    /**
     * The subject produced by the check, for the bridge that installs it.
     * <p>
     * Thread scoped because it belongs to the request being dispatched, and
     * the dispatcher establishes the security context on the same thread
     * immediately after asking who the caller is.
     */
    private static final ThreadLocal<Subject> AUTHENTICATED = new ThreadLocal<>();

    private RealmAuthenticator() {
    }

    /**
     * @param authorization the request's {@code Authorization} header, or null
     * @return the caller's name, or null if the request carries no credential
     * @throws SecurityException if a credential is present and does not pass
     */
    static String authenticate(String authorization) {
        if (authorization == null || !authorization.regionMatches(true, 0, BASIC, 0, BASIC.length())) {
            // No credential, or a scheme this transport does not implement.
            // Either way there is nothing to believe and nothing to refuse.
            AUTHENTICATED.remove();
            return null;
        }

        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(authorization.substring(BASIC.length()).trim()),
                    StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new SecurityException("malformed Basic credential");
        }

        int colon = decoded.indexOf(':');
        if (colon < 0) {
            throw new SecurityException("malformed Basic credential");
        }
        String user = decoded.substring(0, colon);
        char[] password = decoded.substring(colon + 1).toCharArray();

        try {
            Subject subject = LoginContextDriver.jmacLogin(new Subject(), user, password, realmName());
            AUTHENTICATED.set(subject);
            return user;
        } catch (Exception e) {
            // Exception, not LoginException: a realm that fails in any other
            // way must count as a refusal too. Treating an unexpected failure
            // as a successful login is the one outcome that must not happen.
            AUTHENTICATED.remove();
            // Logged without the password, and the caller is told only that it
            // failed: which half was wrong is not a client's business.
            LOG.log(Level.DEBUG, "rejected credential for " + user, e);
            throw new SecurityException("authentication failed");
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    /**
     * @return the subject from the check on this thread, or null if the call
     *         is anonymous
     */
    static Subject authenticatedSubject() {
        return AUTHENTICATED.get();
    }

    static void clear() {
        AUTHENTICATED.remove();
    }

    private static String realmName() {
        try {
            Realm realm = Realm.getDefaultInstance();
            return realm == null ? null : realm.getName();
        } catch (Exception e) {
            // No realm configured is not a reason to accept the credential.
            throw new SecurityException("no realm is available to check this credential");
        }
    }
}
