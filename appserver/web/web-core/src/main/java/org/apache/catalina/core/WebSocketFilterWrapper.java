/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.catalina.core;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.IOException;

import org.apache.catalina.connector.RequestFacade;

/**
 * This class is a wrapper for the WebSocket Filter from Tyrus.
 *
 * <p>
 * It corrects the URI to include the context path without "maskDefaultContextMapping" set.
 * This is the URI on which Tyrus expects to find a WebSocket. Without setting this, the
 * WebSocket will not be found when accessing the application via the default web module
 * URL (the context root).
 *
 * <p>
 * E.g. without this correction, for application "foo", "http://localhost:8080/foo" would work, but
 * "http://localhost:8080" would not when set to a default web module via a command like:
 *
 * {@code asadmin set server-config.http-service.virtual-server.server.default-web-module=foo}
 *
 * @author Arjan Tijms
 * @author Ondro Mihalyi
 *
 */
public class WebSocketFilterWrapper implements Filter  {

    private static final String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";

    private final Filter webSocketFilter;

    public WebSocketFilterWrapper(Filter webSocketFilter) {
        this.webSocketFilter = webSocketFilter;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        webSocketFilter.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        if (httpServletRequest.getHeader(SEC_WEBSOCKET_KEY) != null) {
            httpServletRequest = new HttpServletRequestWrapper(httpServletRequest) {

                @Override
                public String getRequestURI() {
                    RequestFacade wrappedRequest = (RequestFacade) super.getRequest();
                    String requestURI = wrappedRequest.getRequestURI();

                    // Get the contextPath without masking the default context mapping.
                    String contextPath = wrappedRequest.getContextPath(false);

                    if (requestURI.equals(contextPath) || requestURI.startsWith(contextPath + "/")) {
                        return requestURI;
                    }

                    return contextPath + requestURI;
                }
            };
        }

        webSocketFilter.doFilter(httpServletRequest, response, chain);
    }

    @Override
    public void destroy() {
        webSocketFilter.destroy();
    }

}
