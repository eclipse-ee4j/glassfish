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

package org.glassfish.orb.http.client;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Builds a request URI from a path this code has <em>already</em> encoded.
 *
 * <p>The obvious call, {@code new URI(scheme, null, host, port, path, query, null)},
 * is wrong here. That constructor quotes any character of {@code path} that is
 * not legal in a URI - and {@code %} is one of them. A path segment we escaped
 * to {@code java%3Aglobal} therefore goes out as {@code java%253Aglobal}, the
 * server decodes it once to {@code java%3Aglobal}, and the lookup fails on a
 * name that looks right in every log. The single-argument constructor parses
 * rather than quotes, which is what an already-encoded path needs.
 */
final class RequestUri {

    private RequestUri() {
    }

    static URI build(URI base, String encodedPath, String encodedQuery) {
        if (base.getHost() == null) {
            // A registry-based authority such as "my_host" has no host as far
            // as java.net.URI is concerned. Appending it anyway produced
            // "https://null:8181/...", which fails far from the cause.
            String authority = base.getRawAuthority();
            int at = authority == null ? -1 : authority.lastIndexOf('@');
            throw new IllegalArgumentException("the base URI has no usable host: " + base.getScheme()
                    + "://" + (at < 0 ? authority : authority.substring(at + 1)));
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append(base.getScheme()).append("://").append(base.getHost());
        if (base.getPort() != -1) {
            sb.append(':').append(base.getPort());
        }
        sb.append(encodedPath);
        if (encodedQuery != null && !encodedQuery.isEmpty()) {
            sb.append('?').append(encodedQuery);
        }
        try {
            return new URI(sb.toString());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("cannot build a request URI for " + encodedPath, e);
        }
    }
}
