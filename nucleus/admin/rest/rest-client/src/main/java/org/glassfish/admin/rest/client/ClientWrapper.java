/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.client;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.UriBuilder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.filter.CsrfProtectionFilter;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

/**
 * This class wraps the Client returned by JerseyClientBuilder. Using this class allows us to encapsulate many of the
 * client configuration concerns, such as registering the <code>CsrfProtectionFilter</code>.
 *
 * @author jdlee
 */
public class ClientWrapper implements Client {
    protected Client realClient;

    public ClientWrapper() {
        this(new HashMap<String, String>());
    }

    /**
     * Create the client, as well as registering a <code>ClientRequestFilter</code> that adds the specified headers to each
     * request.
     *
     * @param headers
     */
    public ClientWrapper(final Map<String, String> headers) {
        this(headers, null, null);
    }

    public ClientWrapper(final Map<String, String> headers, String userName, String password) {
        realClient = JerseyClientBuilder.newClient();
        realClient.register(new MultiPartFeature());
        realClient.register(new JettisonFeature());
        realClient.register(new CsrfProtectionFilter());
        if ((userName != null) && (password != null)) {
            realClient.register(HttpAuthenticationFeature.basic(userName, password));
        }
        realClient.register(new ClientRequestFilter() {

            @Override
            public void filter(ClientRequestContext rc) throws IOException {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    rc.getHeaders().add(entry.getKey(), entry.getValue());
                }
            }

        });
    }

    @Override
    public void close() {
        realClient.close();
    }

    @Override
    public WebTarget target(String uri) throws IllegalArgumentException, NullPointerException {
        return realClient.target(uri);
    }

    @Override
    public WebTarget target(URI uri) throws NullPointerException {
        return realClient.target(uri);
    }

    @Override
    public WebTarget target(UriBuilder uriBuilder) throws NullPointerException {
        return realClient.target(uriBuilder);
    }

    @Override
    public WebTarget target(Link link) throws NullPointerException {
        return realClient.target(link);
    }

    @Override
    public Builder invocation(Link link) throws NullPointerException {
        return realClient.invocation(link);
    }

    @Override
    public Configuration getConfiguration() {
        return realClient.getConfiguration();
    }

    @Override
    public Client property(String name, Object value) {
        realClient.property(name, value);
        return this;
    }

    @Override
    public Client register(Class<?> componentClass) {
        realClient.register(componentClass);
        return this;
    }

    @Override
    public Client register(Class<?> componentClass, int bindingPriority) {
        realClient.register(componentClass, bindingPriority);
        return this;
    }

    @Override
    public Client register(Class<?> componentClass, Class<?>... contracts) {
        realClient.register(componentClass, contracts);
        return this;
    }

    @Override
    public Client register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        realClient.register(componentClass, contracts);
        return this;
    }

    @Override
    public Client register(Object component) {
        realClient.register(component);
        return this;
    }

    @Override
    public Client register(Object component, int bindingPriority) {
        realClient.register(component, bindingPriority);
        return this;
    }

    @Override
    public Client register(Object component, Class<?>... contracts) {
        realClient.register(component, contracts);
        return this;
    }

    @Override
    public Client register(Object component, Map<Class<?>, Integer> contracts) {
        realClient.register(component, contracts);
        return this;
    }

    @Override
    public SSLContext getSslContext() {
        return realClient.getSslContext();
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return realClient.getHostnameVerifier();
    }
}
