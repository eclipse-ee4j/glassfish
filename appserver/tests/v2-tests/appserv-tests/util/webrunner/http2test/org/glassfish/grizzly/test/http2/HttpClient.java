/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.test.http2;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.Filter;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.http.HttpClientFilter;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpHeader;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http2.Http2BaseFilter;
import org.glassfish.grizzly.http2.Http2ClientFilter;
import org.glassfish.grizzly.http2.Http2Configuration;
import org.glassfish.grizzly.http2.Http2Stream;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.ssl.SSLFilter;

/**
 * A simple Http2 client based on Grizzly runtime.
 *
 * @author Shing Wai Chan
 */
public class HttpClient implements AutoCloseable {
    private String host = "localhost";
    private int port = 8080;
    private boolean secure = true;
    private String[] ciphers = new String[] { "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256" };
    private String keyStore = System.getProperty("S1AS_HOME") + "/domains/domain1/config/keystore.p12";
    private String trustStore = System.getProperty("S1AS_HOME") + "/domains/domain1/config/cacerts.p12";
    private TCPNIOTransport clientTransport;
    private Connection connection;
    private final BlockingQueue<HttpContent> resultQueue = new LinkedTransferQueue<>();

    private HttpClient() {
    }

    private void setHost(String host) {
        this.host = host;
    }

    private void setPort(int port) {
        this.port = port;
    }

    private void setSecure(boolean secure) {
        this.secure = secure;
    }

    private void setCiphers(String[] ciphers) {
        this.ciphers = ciphers;
    }

    private void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    private void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    private void connect() throws Exception {
        final FilterChainBuilder filterChainBuilder =
                createClientFilterChainAsBuilder(secure);
        filterChainBuilder.add(new ClientAggregatorFilter(resultQueue));

        final TCPNIOTransport clientTransport = TCPNIOTransportBuilder.newInstance().build();
        final FilterChain clientFilterChain = filterChainBuilder.build();
        clientTransport.setProcessor(clientFilterChain);

        clientTransport.start();
        Future<Connection> connectFuture = clientTransport.connect(host, port);
        connection = connectFuture.get(10, TimeUnit.SECONDS);
    }

    public static HttpClient.Builder builder() {
        return new HttpClient.Builder();
    }

    public HttpRequest.Builder request() {
        return new HttpRequest.Builder(host, port, connection);
    }

    public HttpResponse getHttpResponse() throws InterruptedException {
        return getHttpResponse(10, TimeUnit.SECONDS);
    }

    public HttpResponse getHttpResponse(int time, TimeUnit timeUnit) throws InterruptedException {
        HttpContent content = resultQueue.poll(time, timeUnit);
        return ((content != null)? new HttpResponse(content) : null);
    }

    public void close() throws Exception {
        if (connection != null) {
            connection.closeSilently();
        }

        if (clientTransport != null) {
            clientTransport.shutdownNow();
        }
    }

    private FilterChainBuilder createClientFilterChainAsBuilder(
            final boolean isSecure,
            final Filter... clientFilters) throws MalformedURLException {

        final FilterChainBuilder builder = FilterChainBuilder.stateless()
             .add(new TransportFilter());
        if (isSecure) {
            builder.add(new SSLFilter(null, getClientSSLEngineConfigurator()));
        }

        builder.add(new HttpClientFilter());
        builder.add(new Http2ClientFilter(Http2Configuration.builder().build()));

        if (clientFilters != null) {
            for (Filter clientFilter : clientFilters) {
                if (clientFilter != null) {
                    builder.add(clientFilter);
                }
            }
        }

        return builder;
    }

    private SSLEngineConfigurator getClientSSLEngineConfigurator() throws MalformedURLException {
        SSLEngineConfigurator clientSSLEngineConfigurator = null;

        SSLContextConfigurator sslContextConfigurator = createSSLContextConfigurator();

        if (sslContextConfigurator.validateConfiguration(true)) {
            clientSSLEngineConfigurator =
                    new SSLEngineConfigurator(sslContextConfigurator.createSSLContext(),
                    true, false, false);

            clientSSLEngineConfigurator.setEnabledCipherSuites(ciphers);
        } else {
            throw new IllegalStateException("Failed to validate SSLContextConfiguration.");
        }

        return clientSSLEngineConfigurator;
    }

    private SSLContextConfigurator createSSLContextConfigurator() throws MalformedURLException {
        SSLContextConfigurator sslContextConfigurator =
                new SSLContextConfigurator();
        URL cacertsUrl = new File(trustStore).toURI().toURL();
        if (cacertsUrl != null) {
            sslContextConfigurator.setTrustStoreFile(cacertsUrl.getFile());
            sslContextConfigurator.setTrustStorePass("changeit");
        }

        URL keystoreUrl = new File(keyStore).toURI().toURL();
        if (keystoreUrl != null) {
            sslContextConfigurator.setKeyStoreFile(keystoreUrl.getFile());
            sslContextConfigurator.setKeyStorePass("changeit");
        }

        return sslContextConfigurator;
    }


    // ----- inner class -----
    public static class Builder {
        private HttpClient httpClient = new HttpClient();

        private Builder() {
        }

        public Builder host(String host) {
            httpClient.setHost(host);
            return this;
        }

        public Builder port(int port) {
            httpClient.setPort(port);
            return this;
        }

        public Builder secure(boolean secure) {
            httpClient.setSecure(secure);
            return this;
        }

        public Builder ciphers(String[] ciphers) {
            httpClient.setCiphers(ciphers);
            return this;
        }

        public Builder keyStore(String keyStore) {
            httpClient.setKeyStore(keyStore);
            return this;
        }

        public Builder trustStore(String trustStore) {
            httpClient.setTrustStore(trustStore);
            return this;
        }

        public HttpClient build() throws Exception {
            httpClient.connect();
            return httpClient;
        }
    }


    private static class ClientAggregatorFilter extends BaseFilter {
        private final BlockingQueue<HttpContent> resultQueue;
        private final Map<Http2Stream, HttpContent> remaindersMap = new HashMap<>();

        public ClientAggregatorFilter(BlockingQueue<HttpContent> resultQueue) {
            this.resultQueue = resultQueue;
        }

        @Override
        public NextAction handleRead(FilterChainContext ctx) throws IOException {
            final HttpContent message = ctx.getMessage();
            final Http2Stream http2Stream = Http2Stream.getStreamFor(message.getHttpHeader());

            final HttpContent remainder = remaindersMap.get(http2Stream);
            final HttpContent sum = remainder != null
                    ? remainder.append(message) : message;

            if (!sum.isLast()) {
                remaindersMap.put(http2Stream, sum);
                return ctx.getStopAction();
            }

            resultQueue.add(sum);

            return ctx.getStopAction();
        }
    }
}
