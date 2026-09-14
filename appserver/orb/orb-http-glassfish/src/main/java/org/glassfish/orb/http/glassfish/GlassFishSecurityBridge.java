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

import jakarta.inject.Singleton;

import javax.security.auth.Subject;

import org.glassfish.orb.http.server.SecurityBridge;
import org.jvnet.hk2.annotations.Service;

/**
 * Makes the authenticated caller the current identity for one invocation.
 *
 * <p>This is the whole of security propagation over HTTP, and its size is the
 * point. Propagating identity over IIOP takes roughly 5,300 lines in
 * {@code appserver/security/ejb.security}: CSIv2, GSSUP tokens, tagged
 * components, mechanism selection, client and server request interceptors. But
 * the container does not consume any of that - it consumes
 * {@code SecurityContext}, and all of that apparatus exists to arrive here.
 * Over HTTP the identity has already been established by the container's own
 * authentication, so only the last step remains.
 */
@Service
@Singleton
public class GlassFishSecurityBridge implements SecurityBridge {

    @Override
    public Object establish(String userName) {
        SecurityContext previous = SecurityContext.getCurrent();
        if (userName == null) {
            // Unauthenticated: leave whatever was current alone rather than
            // installing an empty identity over it.
            return previous;
        }
        // The subject the realm produced, not an empty one: it carries the
        // groups the caller belongs to, and every authorization decision the
        // container makes afterwards reads them from here. An empty subject
        // would authenticate the caller and then deny it everything it is
        // entitled to.
        Subject subject = RealmAuthenticator.authenticatedSubject();
        SecurityContext.setCurrent(new SecurityContext(userName, subject == null ? new Subject() : subject));
        return previous;
    }

    @Override
    public void clear(Object token) {
        // Restore rather than null out: this thread came from a pool and may
        // have been carrying an identity that is not ours to discard.
        SecurityContext.setCurrent(token instanceof SecurityContext previous ? previous : null);
        RealmAuthenticator.clear();
    }
}
