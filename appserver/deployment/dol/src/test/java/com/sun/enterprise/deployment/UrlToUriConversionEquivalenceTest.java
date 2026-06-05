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

package com.sun.enterprise.deployment;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Documents the behavioural equivalence <em>and the known divergences</em> between the deprecated
 * string-based {@code java.net.URL} constructors (deprecated since Java 20) and the two
 * {@code java.net.URI}-based replacements this change uses:
 *
 * <ul>
 *   <li><b>multi-arg form</b> &mdash; {@code new URI(scheme, null, host, port, path, null, null).toURL()},
 *       used by {@link WebServiceEndpoint#composeEndpointAddress(URL, String)}. It correctly omits a
 *       default ({@code -1}) port, but <em>percent-encodes the path</em> and parses the host as a
 *       registry-based authority.</li>
 *   <li><b>spec form</b> &mdash; {@code new URI(scheme + "://" + host + ":" + port + file).toURL()},
 *       used by the {@code RealmAdapter} SSL redirect. It copies the already-encoded request URI
 *       verbatim (no re-encoding, query string preserved), but is strict about raw illegal
 *       characters.</li>
 * </ul>
 *
 * <p>The behaviour pinned here was confirmed against the deprecated {@code URL} constructor on
 * JDK 21. The point of the test is the one dmatej raised on the PR: the replacement is not a blind
 * 1:1 swap, so the traps (re-encoding, unicode host, raw spaces) are nailed down explicitly and any
 * future JDK/behaviour drift fails here rather than in production.
 */
class UrlToUriConversionEquivalenceTest {

    @SuppressWarnings("deprecation") // the deprecated URL ctor is the reference behaviour under test
    private static String legacy(String scheme, String host, int port, String file) throws Exception {
        return new URL(scheme, host, port, file).toExternalForm();
    }

    /** The form used in {@link WebServiceEndpoint}. */
    private static String multiArg(String scheme, String host, int port, String path) throws Exception {
        return new URI(scheme, null, host, port, path, null, null).toURL().toExternalForm();
    }

    /** The form used in the {@code RealmAdapter} SSL redirect. */
    private static String spec(String scheme, String host, int port, String file) throws Exception {
        return new URI(scheme + "://" + host + ":" + port + file).toURL().toExternalForm();
    }

    // ---- cases where every form agrees with the deprecated constructor -------------------------

    @Test
    void plainAsciiPathsAreEquivalent() throws Exception {
        for (String path : new String[] {"/app/MyService", "/a/b/c/MyServiceService", "/"}) {
            assertEquals(legacy("https", "host.example", 8080, path), multiArg("https", "host.example", 8080, path),
                "multi-arg must match deprecated URL for " + path);
            assertEquals(legacy("https", "host.example", 8080, path), spec("https", "host.example", 8080, path),
                "spec form must match deprecated URL for " + path);
        }
    }

    /**
     * Default port ({@code -1}): both the deprecated ctor and the multi-arg URI ctor omit the
     * {@code :port} segment. This is why {@code WebServiceEndpoint} keeps the multi-arg ctor rather
     * than assembling a {@code host:port} string (the spec form would otherwise emit {@code :-1}).
     */
    @Test
    void multiArgHandlesDefaultPort() throws Exception {
        assertEquals(legacy("http", "host.example", -1, "/app/MyService"),
            multiArg("http", "host.example", -1, "/app/MyService"));
    }

    /** A raw (non-ASCII) character in the path is preserved identically by all three forms. */
    @Test
    void rawUnicodeInPathIsPreserved() throws Exception {
        String path = "/app/café";
        assertEquals(legacy("https", "host.example", 8080, path), multiArg("https", "host.example", 8080, path));
        assertEquals(legacy("https", "host.example", 8080, path), spec("https", "host.example", 8080, path));
    }

    // ---- divergences: multi-arg form re-encodes / validates --------------------------------------

    /**
     * Multi-arg ctor percent-encodes the path. A path that is <em>already</em> encoded is therefore
     * double-encoded ({@code %2F} -&gt; {@code %252F}). Endpoint addresses are built from un-encoded
     * descriptor values so this does not arise in practice, but the boundary is recorded here.
     */
    @Test
    void multiArgReEncodesPreEncodedPath() throws Exception {
        assertEquals("https://host.example:8080/a%2Fb", legacy("https", "host.example", 8080, "/a%2Fb"));
        assertEquals("https://host.example:8080/a%252Fb", multiArg("https", "host.example", 8080, "/a%2Fb"));
        // the spec form, by contrast, copies it verbatim (matches the deprecated ctor)
        assertEquals(legacy("https", "host.example", 8080, "/a%2Fb"), spec("https", "host.example", 8080, "/a%2Fb"));
    }

    /** Multi-arg ctor percent-encodes a raw space; the deprecated ctor copied it verbatim. */
    @Test
    void multiArgEncodesRawSpace() throws Exception {
        assertEquals("https://host.example:8080/a b", legacy("https", "host.example", 8080, "/a b"));
        assertEquals("https://host.example:8080/a%20b", multiArg("https", "host.example", 8080, "/a b"));
    }

    /**
     * Multi-arg ctor parses the host as a registry-based authority and <em>rejects</em> a non-ASCII
     * (IDN) host that the deprecated ctor accepted verbatim. This is the unicode-host case dmatej
     * flagged: a web-service endpoint deployed against an IDN host would now fail here. The spec
     * form (RealmAdapter) keeps the host verbatim, matching the old behaviour.
     */
    @Test
    void multiArgRejectsUnicodeHostWhileSpecFormPreservesIt() throws Exception {
        assertThrows(URISyntaxException.class,
            () -> new URI("https", null, "höst.example", 8080, "/app/x", null, null));
        assertEquals(legacy("https", "höst.example", 8080, "/app/x"),
            spec("https", "höst.example", 8080, "/app/x"));
    }

    // ---- divergences: spec form is strict about raw illegal characters ---------------------------

    /**
     * The spec form is RFC-3986 strict, so a raw space (or other illegal char) throws instead of
     * being copied verbatim. In the {@code RealmAdapter} redirect the input is
     * {@code HttpServletRequest.getRequestURI()} + {@code getQueryString()}, which the container has
     * already percent-encoded, and the surrounding catch turns a failure into an HTTP 500 rather
     * than a crash &mdash; so the exposure is contained, but it is a real change.
     */
    @Test
    void specFormRejectsRawSpace() {
        assertThrows(URISyntaxException.class, () -> new URI("https://host.example:8080/a b"));
    }
}
