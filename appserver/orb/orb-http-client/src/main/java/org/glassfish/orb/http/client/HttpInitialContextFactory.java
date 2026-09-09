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
import java.time.Duration;
import java.util.Hashtable;

import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * The JNDI entry point for a standalone client.
 *
 * <pre>
 * Properties env = new Properties();
 * env.put(Context.INITIAL_CONTEXT_FACTORY,
 *         "org.glassfish.orb.http.client.HttpInitialContextFactory");
 * env.put(Context.PROVIDER_URL, "https://host:8181/glassfish-services");
 * env.put(Context.SECURITY_PRINCIPAL, "alice");
 * env.put(Context.SECURITY_CREDENTIALS, "secret");
 * InitialContext ctx = new InitialContext(env);
 * Greeter greeter = (Greeter) ctx.lookup("java:global/app/GreeterBean!com.acme.Greeter");
 * </pre>
 *
 * Compare with what the IIOP path needs on the client's classpath -
 * {@code gf-client.jar} and the {@code glassfish-corba} / {@code pfl} chain
 * behind it, because JEP 320 removed CORBA from the JDK in 11 so all of it has
 * to be shipped. This factory needs the JDK, the protocol module and
 * {@code jakarta.ejb-api}.
 */
public class HttpInitialContextFactory implements InitialContextFactory {

    /** Connect timeout in milliseconds. */
    public static final String PROP_CONNECT_TIMEOUT = "org.glassfish.orb.http.connectTimeout";

    /** Per-request timeout in milliseconds. */
    public static final String PROP_REQUEST_TIMEOUT = "org.glassfish.orb.http.requestTimeout";

    /** A bearer token, as an alternative to principal/credentials. */
    public static final String PROP_BEARER_TOKEN = "org.glassfish.orb.http.bearerToken";

    /**
     * Set to {@code false} to stay on HTTP/1.1. Only useful to work around a
     * broken intermediary - the JDK client already falls back on its own.
     */
    public static final String PROP_HTTP2 = "org.glassfish.orb.http.http2";

    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        Object url = environment == null ? null : environment.get(Context.PROVIDER_URL);
        if (url == null) {
            throw new ConfigurationException(Context.PROVIDER_URL + " is required, "
                    + "for example https://host:8181/glassfish-services");
        }
        URI baseUri;
        try {
            baseUri = new URI(url.toString());
        } catch (URISyntaxException e) {
            ConfigurationException ce = new ConfigurationException("malformed " + Context.PROVIDER_URL + ": " + url);
            ce.initCause(e);
            throw ce;
        }
        String scheme = baseUri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new ConfigurationException("unsupported scheme in " + Context.PROVIDER_URL + ": " + scheme);
        }

        ClientConfiguration.Builder builder = ClientConfiguration.builder(baseUri);

        Object principal = environment.get(Context.SECURITY_PRINCIPAL);
        Object credentials = environment.get(Context.SECURITY_CREDENTIALS);
        if (principal != null) {
            char[] password = credentials == null ? new char[0]
                    : credentials instanceof char[] c ? c : credentials.toString().toCharArray();
            if (!"https".equalsIgnoreCase(scheme)) {
                // Basic encodes the password reversibly; say so rather than
                // let it go out in the clear unremarked.
                System.getLogger(HttpInitialContextFactory.class.getName())
                        .log(System.Logger.Level.WARNING,
                             "credentials will be sent over plain HTTP to {0}; use https", baseUri);
            }
            builder.credentials(principal.toString(), password);
        }

        Object token = environment.get(PROP_BEARER_TOKEN);
        if (token != null) {
            builder.bearerToken(token.toString());
        }

        Duration connect = durationProperty(environment, PROP_CONNECT_TIMEOUT);
        if (connect != null) {
            builder.connectTimeout(connect);
        }
        Duration request = durationProperty(environment, PROP_REQUEST_TIMEOUT);
        if (request != null) {
            builder.requestTimeout(request);
        }
        Object http2 = environment.get(PROP_HTTP2);
        if (http2 != null) {
            builder.preferHttp2(Boolean.parseBoolean(http2.toString()));
        }

        return new HttpNamingContext(builder.build(), environment);
    }

    private static Duration durationProperty(Hashtable<?, ?> environment, String key) throws NamingException {
        Object value = environment.get(key);
        if (value == null) {
            return null;
        }
        try {
            return Duration.ofMillis(Long.parseLong(value.toString()));
        } catch (NumberFormatException e) {
            ConfigurationException ce = new ConfigurationException(key + " must be a number of milliseconds: " + value);
            ce.initCause(e);
            throw ce;
        }
    }
}
