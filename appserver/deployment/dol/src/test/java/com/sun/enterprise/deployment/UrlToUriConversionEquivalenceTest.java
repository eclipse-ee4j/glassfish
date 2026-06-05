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
import java.net.URL;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins down the behavioural equivalence (and the known divergences) between the deprecated
 * {@code new URL(protocol, host, port, file)} constructor and its replacement
 * {@code new URI(protocol, null, host, port, file, null, null).toURL()} used in
 * {@link WebServiceEndpoint#composeEndpointAddress(URL, String)}.
 *
 * <p>The deprecated four-argument {@code URL} constructor copies the {@code file} part verbatim,
 * whereas the multi-argument {@code URI} constructor percent-encodes its path argument. For the
 * plain ASCII paths these endpoint addresses use in practice the two are identical; this test
 * makes that guarantee explicit and documents the two cases where they intentionally differ.
 */
class UrlToUriConversionEquivalenceTest {

    @SuppressWarnings("deprecation") // the deprecated URL ctor is the reference behaviour under test
    private static URL legacy(String protocol, String host, int port, String file) throws Exception {
        return new URL(protocol, host, port, file);
    }

    private static URL migrated(String protocol, String host, int port, String file) throws Exception {
        return new URI(protocol, null, host, port, file, null, null).toURL();
    }

    private static void assertEquivalent(String protocol, String host, int port, String file) throws Exception {
        assertEquals(
            legacy(protocol, host, port, file).toExternalForm(),
            migrated(protocol, host, port, file).toExternalForm(),
            "URI replacement must match the deprecated URL constructor for " + protocol + "://" + host + ":" + port + file);
    }

    @Test
    void plainPathsAreEquivalent() throws Exception {
        assertEquivalent("http", "localhost", 8080, "/app/MyService");
        assertEquivalent("https", "example.com", 443, "/a/b/c/MyServiceService");
        assertEquivalent("http", "127.0.0.1", 80, "/");
        assertEquivalent("http", "localhost", 8080, "/ctx/sub/endpoint");
    }

    /**
     * Default port (-1): both forms omit the {@code :port} segment, so they stay equivalent.
     * This is why {@code WebServiceEndpoint} keeps the multi-argument {@code URI} constructor
     * rather than assembling a {@code host:port} string (a {@code ":-1"} would otherwise leak in).
     */
    @Test
    void defaultPortIsEquivalent() throws Exception {
        assertEquivalent("http", "localhost", -1, "/app/MyService");
    }

    /**
     * Known, intentional divergence: a {@code file} that already contains a percent-encoded
     * sequence is copied verbatim by {@code new URL(...)} but re-encoded by the {@code URI}
     * constructor ({@code %2F} becomes {@code %252F}). Endpoint address paths are composed from
     * un-encoded descriptor values, so this case does not arise in practice, but the test records
     * the boundary so a future change that starts feeding pre-encoded input is caught here.
     */
    @Test
    void preEncodedPathIsReEncoded() throws Exception {
        assertEquals("http://localhost:8080/a%2Fb", legacy("http", "localhost", 8080, "/a%2Fb").toExternalForm());
        assertEquals("http://localhost:8080/a%252Fb", migrated("http", "localhost", 8080, "/a%2Fb").toExternalForm());
    }
}
