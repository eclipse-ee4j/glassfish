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

package org.glassfish.orb.http.server;

/**
 * Establishes the caller's identity for the duration of an invocation.
 *
 * <h2>Why this is only a few lines</h2>
 * Propagating identity over IIOP takes about 5,300 lines in
 * {@code appserver/security/ejb.security/.../iiop/security}: CSIv2, GSSUP
 * tokens, {@code CSIV2TaggedComponentInfo}, {@code SecurityMechanismSelector},
 * client and server request interceptors, service-context marshalling. But
 * none of that is what the container consumes. The container consumes
 * {@code com.sun.enterprise.security.SecurityContext}, and the whole of that
 * apparatus converges on one call,
 * {@code SecurityContext.setCurrent(...)} - see
 * {@code SecurityContextUtil.java:218}.
 * <p>
 * Over HTTP the identity arrives already established by the container's own
 * authentication - Basic over TLS, a client certificate, or a bearer token -
 * so all that remains is that same call. This is why Payara's HTTP transport
 * documenting "Security Propagation is not supported" is a decision rather
 * than a constraint: the insertion point is downstream of the transport.
 */
public interface SecurityBridge {

    /**
     * Makes {@code userName} the current caller.
     *
     * @return a token to pass to {@link #clear(Object)}, or {@code null}
     */
    Object establish(String userName);

    /** Restores whatever identity was current before {@link #establish}. */
    void clear(Object token);

    /** A bridge that does nothing, for an endpoint that does not authenticate. */
    SecurityBridge NONE = new SecurityBridge() {

        @Override
        public Object establish(String userName) {
            return null;
        }

        @Override
        public void clear(Object token) {
        }
    };
}
