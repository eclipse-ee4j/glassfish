/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.config.portunif;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.net.ssl.SSLEngine;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.config.ConfigAwareElement;
import org.glassfish.grizzly.config.dom.HttpRedirect;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.ssl.SSLUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.ConfigBeanProxy;

/**
 *
 * @author Alexey Stashok
 */
public class HttpRedirectFilter extends BaseFilter implements ConfigAwareElement {

    private Integer redirectPort;

    // default to true to retain compatibility with legacy redirect declarations.
    private Boolean secure;

    // ----------------------------------------- Methods from ConfigAwareElement
    /**
     * Configuration for &lt;http-redirect&gt;.
     *
     * @param configuration filter configuration
     */
    @Override
    public void configure(ServiceLocator locator, NetworkListener networkListener,
            ConfigBeanProxy configuration) {

        if (configuration instanceof HttpRedirect) {
            final HttpRedirect httpRedirectConfig = (HttpRedirect) configuration;
            int port = Integer.parseInt(httpRedirectConfig.getPort());
            redirectPort = port != -1 ? port : null;
            secure = Boolean.parseBoolean(httpRedirectConfig.getSecure());
        } else {
            // Retained for backwards compatibility with legacy redirect declarations.
        }
    }

    // --------------------------------------------- Methods from Filter


    @Override
    public NextAction handleRead(final FilterChainContext ctx) throws IOException {
        final Connection connection = ctx.getConnection();
        final HttpContent httpContent = ctx.getMessage();

        final HttpRequestPacket request = (HttpRequestPacket) httpContent.getHttpHeader();
        final URI requestURI;
        try {
            final String uri = request.getQueryString() == null ?
                    request.getRequestURI() :
                    request.getRequestURI() + "?" + request.getQueryString();
            requestURI = new URI(uri);
        } catch (URISyntaxException ignored) {
            return ctx.getStopAction();
        }

        final boolean redirectToSecure;
        if (secure != null) { // if secure is set - we use it
            redirectToSecure = secure;
        } else {  // if secure is not set - use secure settings opposite to the current request
            final SSLEngine sslEngine = SSLUtils.getSSLEngine(connection);
            redirectToSecure = sslEngine == null;
        }


        final StringBuilder hostPort = new StringBuilder();

        String hostHeader = request.getHeader("host");
        if (hostHeader == null) {
            String hostRequestURI = requestURI.getHost();

            if (hostRequestURI == null) {
                hostPort.append(request.getLocalHost());
            } else {
                hostPort.append(hostRequestURI);
            }

            hostPort.append(':');

            if (redirectPort == null) {
                int port = requestURI.getPort();
                if (port == -1) {
                    hostPort.append(request.getLocalPort());
                } else {
                    hostPort.append(port);
                }
            } else {
                hostPort.append(redirectPort);
            }

        } else if (redirectPort != null) { // if port is specified - cut it from host header
            final int colonIdx = hostHeader.indexOf(':');
            if (colonIdx != -1) {
                hostHeader = hostHeader.substring(0, colonIdx);
            }
            hostPort.append(hostHeader)
                    .append(':')
                    .append(redirectPort);
        } else {
            hostPort.append(hostHeader);
        }

        if (hostPort.length() > 0) {
            String path = requestURI.toString();

            assert path != null;

            final StringBuilder sb = new StringBuilder();
            sb.append((redirectToSecure ? "https://" : "http://"))
                    .append(hostPort)
                    .append(path);

            request.setSkipRemainder(true);
            final HttpResponsePacket response = HttpResponsePacket.builder(request)
                    .status(302)
                    .header("Location", sb.toString())
                    .contentLength(0)
                    .build();
            ctx.write(response);
        } else {
            connection.closeSilently();
        }

        return ctx.getStopAction();
    }
}
