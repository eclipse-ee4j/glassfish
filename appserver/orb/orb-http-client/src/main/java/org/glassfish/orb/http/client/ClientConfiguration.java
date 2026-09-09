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
import java.time.Duration;
import java.util.Objects;

import javax.net.ssl.SSLContext;

/**
 * Connection settings for the thin client.
 * <p>
 * These are the JNDI environment properties a standalone client sets on its
 * {@code InitialContext}; see {@link HttpInitialContextFactory} for the
 * property names.
 */
public final class ClientConfiguration {

    private final URI baseUri;
    private final Duration connectTimeout;
    private final Duration requestTimeout;
    private final String username;
    private final char[] password;
    private final String bearerToken;
    private final SSLContext sslContext;
    private final boolean preferHttp2;

    private ClientConfiguration(Builder b) {
        this.baseUri = Objects.requireNonNull(b.baseUri, "baseUri");
        this.connectTimeout = b.connectTimeout;
        this.requestTimeout = b.requestTimeout;
        this.username = b.username;
        this.password = b.password == null ? null : b.password.clone();
        this.bearerToken = b.bearerToken;
        this.sslContext = b.sslContext;
        this.preferHttp2 = b.preferHttp2;
    }

    public URI baseUri() {
        return baseUri;
    }

    /** The context path portion of {@link #baseUri()}, e.g. {@code /glassfish-services}. */
    public String contextPath() {
        String path = baseUri.getPath();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return "";
        }
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    public Duration connectTimeout() {
        return connectTimeout;
    }

    public Duration requestTimeout() {
        return requestTimeout;
    }

    public String username() {
        return username;
    }

    public char[] password() {
        return password == null ? null : password.clone();
    }

    public String bearerToken() {
        return bearerToken;
    }

    public SSLContext sslContext() {
        return sslContext;
    }

    public boolean preferHttp2() {
        return preferHttp2;
    }

    public boolean hasCredentials() {
        return username != null || bearerToken != null;
    }

    public static Builder builder(URI baseUri) {
        return new Builder(baseUri);
    }

    public static final class Builder {

        private final URI baseUri;
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration requestTimeout = Duration.ofSeconds(60);
        private String username;
        private char[] password;
        private String bearerToken;
        private SSLContext sslContext;
        private boolean preferHttp2 = true;

        private Builder(URI baseUri) {
            this.baseUri = baseUri;
        }

        public Builder connectTimeout(Duration d) {
            this.connectTimeout = d;
            return this;
        }

        public Builder requestTimeout(Duration d) {
            this.requestTimeout = d;
            return this;
        }

        /**
         * Credentials for preemptive HTTP Basic. Sent on every request rather
         * than only in answer to a 401: a challenge-response round trip per
         * invocation would be a needless doubling of latency, and unlike a
         * browser this client always knows it is going to be challenged.
         * <p>
         * Basic sends the password reversibly encoded, so this is only safe
         * over TLS. That is the same constraint CSIv2/GSSUP has over IIOP.
         */
        public Builder credentials(String username, char[] password) {
            this.username = username;
            this.password = password == null ? null : password.clone();
            return this;
        }

        /** A bearer token (OIDC, JWT) sent in the {@code Authorization} header. */
        public Builder bearerToken(String token) {
            this.bearerToken = token;
            return this;
        }

        /** For mutual TLS, or to trust a private CA. */
        public Builder sslContext(SSLContext context) {
            this.sslContext = context;
            return this;
        }

        /**
         * Whether to negotiate HTTP/2. Left on by default: multiplexing is
         * what lets concurrent invocations share one connection, which is the
         * property GIOP has and HTTP/1.1 does not.
         */
        public Builder preferHttp2(boolean preferHttp2) {
            this.preferHttp2 = preferHttp2;
            return this;
        }

        public ClientConfiguration build() {
            return new ClientConfiguration(this);
        }
    }
}
