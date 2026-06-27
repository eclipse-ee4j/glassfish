/*
 * Copyright (c) 2021, 2026 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.web.security.realmadapter;

import com.sun.enterprise.util.net.NetUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.apache.catalina.Globals;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;

import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpRedirectGenerator {

    private static final Logger LOG = Logger.getLogger(HttpRedirectGenerator.class.getName());

    private NetworkListeners networkListeners;

    record HostAndPort(String host, int port) {}

    public HttpRedirectGenerator(NetworkListeners networkListeners) {
        this.networkListeners = networkListeners;
    }

    public boolean redirect(HttpRequest request, HttpResponse response) throws IOException {
        // Initialize variables we need to determine the appropriate action
        HttpServletRequest httpServletRequest = (HttpServletRequest) request.getRequest();
        HttpServletResponse httpServletResponse = (HttpServletResponse) response.getResponse();

        int redirectPort = request.getConnector().getRedirectPort();
        if (redirectPort <= 0) {
            // Is redirecting disabled?
            LOG.fine("SSL redirect is disabled");
            httpServletResponse.sendError(SC_FORBIDDEN, URLEncoder.encode(httpServletRequest.getRequestURI(), UTF_8));
            return false;
        }

        StringBuilder file = new StringBuilder(httpServletRequest.getRequestURI());

        String requestedSessionId = httpServletRequest.getRequestedSessionId();
        if (requestedSessionId != null && httpServletRequest.isRequestedSessionIdFromURL()) {
            file.append(';')
                .append(Globals.SESSION_PARAMETER_NAME)
                .append('=')
                .append(requestedSessionId);
        }

        String queryString = httpServletRequest.getQueryString();
        if (queryString != null) {
            file.append('?')
                .append(queryString);
        }

        HostAndPort hostAndPort = getHostAndPort(request);
        String serverHost = hostAndPort.host;
        redirectPort = hostAndPort.port;

        try {
            // Generate the base URL to safely handle IPv6 brackets and lock the authority
            URI base = new URI("https", null, serverHost, redirectPort, null, null, null);

            // Append the already-encoded path/query to mimic the old URL constructor behavior
            URI uri = URI.create(base.toASCIIString() + file);

            httpServletResponse.sendRedirect(uri.toASCIIString());
            return false;
        } catch (IllegalArgumentException | URISyntaxException e) {
            httpServletResponse.sendError(SC_INTERNAL_SERVER_ERROR, URLEncoder.encode(httpServletRequest.getRequestURI(), UTF_8));
            return false;
        }
    }

    private HostAndPort getHostAndPort(HttpRequest request) throws IOException {
        Enumeration<String> headerNames = ((HttpServletRequest) request.getRequest()).getHeaderNames();

        String[] hostPort = null;
        boolean isHeaderPresent = false;
        boolean isWebServerRequest = false;
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String hostVal;
            if (headerName.equalsIgnoreCase("Host")) {
                hostVal = ((HttpServletRequest) request.getRequest()).getHeader(headerName);
                isHeaderPresent = true;
                hostPort = hostVal.split(":");
            }
        }
        if (hostPort == null) {
            throw new ProtocolException("Host header not found in request");
        }

        // If the port in the Header is empty (it refers to the default port), which is
        // not one of the GlassFish listener ports -> GF is front-ended by a proxy (LB plugin)
        boolean isHostPortNullOrEmpty = ((hostPort.length <= 1) || (hostPort[1] == null || hostPort[1].isBlank()));
        if (!isHeaderPresent) {
            isWebServerRequest = false;
        } else if (isHostPortNullOrEmpty) {
            isWebServerRequest = true;
        } else {
            boolean breakFromLoop = false;

            for (NetworkListener nwListener : networkListeners.getNetworkListener()) {
                // Loop through the network listeners
                String nwAddress = nwListener.getAddress();
                InetAddress[] localHostAdresses;
                if (nwAddress == null || nwAddress.equals("0.0.0.0")) {
                    nwAddress = NetUtils.getCanonicalHostName();
                    if (!nwAddress.equals(hostPort[0])) {
                        // compare the InetAddress objects
                        // only if the hostname in the header
                        // does not match with the hostname in the
                        // listener-To avoid performance overhead
                        localHostAdresses = NetUtils.getHostAddresses();
                        if (localHostAdresses.length == 0) {
                            break;
                        }
                        InetAddress hostAddress = InetAddress.getByName(hostPort[0]);
                        for (InetAddress inetAdress : localHostAdresses) {
                            if (inetAdress.equals(hostAddress)) {
                                // Hostname of the request in the listener and the hostname in the Host header match.
                                // Check the port
                                String nwPort = nwListener.getPort();
                                // If the listener port is different from the port
                                // in the Host header, then request is received by WS frontend
                                if (nwPort.equals(hostPort[1])) {
                                    isWebServerRequest = false;
                                    breakFromLoop = true;
                                    break;
                                }
                                isWebServerRequest = true;
                            }
                        }
                    } else {
                        // Host names are the same, compare the ports
                        String nwPort = nwListener.getPort();
                        // If the listener port is different from the port
                        // in the Host header, then request is received by WS frontend
                        if (!nwPort.equals(hostPort[1])) {
                            isWebServerRequest = true;

                        } else {
                            isWebServerRequest = false;
                            breakFromLoop = true;

                        }

                    }
                }
                if (breakFromLoop && !isWebServerRequest) {
                    break;
                }
            }
        }
        String serverHost = request.getRequest().getServerName();
        int redirectPort = request.getConnector().getRedirectPort();

        // If the request is a from a webserver frontend, redirect to the url
        // with the webserver frontend host and port
        if (isWebServerRequest) {
            serverHost = hostPort[0];
            if (isHostPortNullOrEmpty) {
                // Use the default port
                redirectPort = -1;
            } else {
                redirectPort = Integer.parseInt(hostPort[1]);
            }
        }

        return new HostAndPort(serverHost, redirectPort);
    }

}
