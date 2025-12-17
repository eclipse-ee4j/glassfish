/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.webservices.transport.tcp;

import com.oracle.webservices.api.message.BaseDistributedPropertySet;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;
import jakarta.xml.ws.handler.MessageContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * @author Alexey Stashok
 */
public final class ServletFakeArtifactSet extends BaseDistributedPropertySet {

    private static final PropertyMap model;

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    static {
        model = parse(ServletFakeArtifactSet.class);
    }

    @Override
    public BaseDistributedPropertySet.PropertyMap getPropertyMap() {
        return model;
    }

    public ServletFakeArtifactSet(final String requestURL, final String servletPath) {
        request = createRequest(requestURL, servletPath);
        response = createResponse();
    }

    @Property(MessageContext.SERVLET_RESPONSE)
    public HttpServletResponse getResponse() {
        return response;
    }

    @Property(MessageContext.SERVLET_REQUEST)
    public HttpServletRequest getRequest() {
        return request;
    }

    private static HttpServletRequest createRequest(final String requestURL, final String servletPath) {
        return new FakeServletHttpRequest(requestURL, servletPath);
    }

    private static HttpServletResponse createResponse() {
        return new FakeServletHttpResponse();
    }

    public static final class FakeServletHttpRequest implements HttpServletRequest {
        private final StringBuffer requestURL;
        private final String requestURI;
        private final String servletPath;

        public FakeServletHttpRequest(final String requestURL, final String servletPath) {
            this.requestURI = requestURL;
            this.requestURL = new StringBuffer(requestURL);
            this.servletPath = servletPath;
        }

        @Override
        public String getAuthType() {
            return null;
        }

        @Override
        public Cookie[] getCookies() {
            return null;
        }

        @Override
        public long getDateHeader(final String string) {
            return 0L;
        }

        @Override
        public String getHeader(final String string) {
            return null;
        }

        @Override
        public Enumeration<String> getHeaders(final String string) {
            return null;
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            return null;
        }

        @Override
        public int getIntHeader(final String string) {
            return -1;
        }

        @Override
        public String getMethod() {
            return "POST";
        }

        @Override
        public String getPathInfo() {
            return null;
        }

        @Override
        public String getPathTranslated() {
            return null;
        }

        @Override
        public String getContextPath() {
            return null;
        }

        @Override
        public String getQueryString() {
            return null;
        }

        @Override
        public String getRemoteUser() {
            return null;
        }

        @Override
        public boolean isUserInRole(final String string) {
            return true;
        }

        @Override
        public Principal getUserPrincipal() {
            return null;
        }

        @Override
        public String getRequestedSessionId() {
            return null;
        }

        @Override
        public String getRequestURI() {
            return requestURI;
        }

        @Override
        public StringBuffer getRequestURL() {
            return requestURL;
        }

        @Override
        public String getServletPath() {
            return servletPath;
        }

        @Override
        public HttpSession getSession(final boolean b) {
            return null;
        }

        @Override
        public HttpSession getSession() {
            return null;
        }

        @Override
        public String changeSessionId() {
            return null;
        }

        @Override
        public boolean isRequestedSessionIdValid() {
            return true;
        }

        @Override
        public boolean isRequestedSessionIdFromCookie() {
            return true;
        }

        @Override
        public boolean isRequestedSessionIdFromURL() {
            return true;
        }

        @Override
        public Object getAttribute(final String string) {
            return null;
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return null;
        }

        @Override
        public String getCharacterEncoding() {
            return null;
        }

        @Override
        public void setCharacterEncoding(final String string) throws UnsupportedEncodingException {
        }

        @Override
        public int getContentLength() {
            return 0;
        }

        @Override
        public long getContentLengthLong() {
            return 0L;
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return null;
        }

        @Override
        public String getParameter(final String string) {
            return null;
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return null;
        }

        @Override
        public String[] getParameterValues(final String string) {
            return null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return null;
        }

        @Override
        public String getProtocol() {
            return null;
        }

        @Override
        public String getScheme() {
            return null;
        }

        @Override
        public String getServerName() {
            return null;
        }

        @Override
        public int getServerPort() {
            return 0;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return null;
        }

        @Override
        public String getRemoteAddr() {
            return null;
        }

        @Override
        public String getRemoteHost() {
            return null;
        }

        @Override
        public void setAttribute(final String string, final Object object) {
        }

        @Override
        public void removeAttribute(final String string) {
        }

        @Override
        public Locale getLocale() {
            return null;
        }

        @Override
        public Enumeration<Locale> getLocales() {
            return null;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public RequestDispatcher getRequestDispatcher(final String string) {
            return null;
        }

        @Override
        public int getRemotePort() {
            return 0;
        }

        @Override
        public String getLocalName() {
            return null;
        }

        @Override
        public String getLocalAddr() {
            return null;
        }

        @Override
        public int getLocalPort() {
            return 0;
        }

        @Override
        public Part getPart(String s) {
            return null;
        }

        @Override
        public Collection<Part> getParts() {
            return null;
        }

        @Override
        public void login(String s1, String s2) {
        }

        @Override
        public void logout() {
        }

        @Override
        public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) {
            return null;
        }

        @Override
        public boolean authenticate(HttpServletResponse response) {
            return true;
        }

        @Override
        public DispatcherType getDispatcherType() {
            return null;
        }

        @Override
        public jakarta.servlet.AsyncContext getAsyncContext() {
            return null;
        }

        @Override
        public boolean isAsyncSupported() {
            return false;
        }

        @Override
        public boolean isAsyncStarted() {
            return false;
        }

        @Override
        public AsyncContext startAsync() {
            return null;
        }

        @Override
        public AsyncContext startAsync(ServletRequest request, ServletResponse response) {
            return null;
        }

        @Override
        public ServletContext getServletContext() {
            return null;
        }

        @Override
        public String getRequestId() {
            return null;
        }

        @Override
        public String getProtocolRequestId() {
            return null;
        }

        @Override
        public ServletConnection getServletConnection() {
            return null;
        }
    }

    public static final class FakeServletHttpResponse implements HttpServletResponse {
        @Override
        public void addCookie(final Cookie cookie) {
        }

        @Override
        public boolean containsHeader(final String string) {
            return true;
        }

        @Override
        public String encodeURL(final String string) {
            return null;
        }

        @Override
        public String encodeRedirectURL(final String string) {
            return null;
        }

        @Override
        public void sendError(final int i, final String string) throws IOException {
        }

        @Override
        public void sendError(final int i) throws IOException {
        }

        @Override
        public void sendRedirect(final String string) throws IOException {
        }

        @Override
        public void setDateHeader(final String string, final long l) {
        }

        @Override
        public void addDateHeader(final String string, final long l) {
        }

        @Override
        public void setHeader(final String string, final String string0) {
        }

        @Override
        public void addHeader(final String string, final String string0) {
        }

        @Override
        public void setIntHeader(final String string, final int i) {
        }

        @Override
        public void addIntHeader(final String string, final int i) {
        }

        @Override
        public void setStatus(final int i) {
        }

        @Override
        public String getCharacterEncoding() {
            return null;
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return null;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return null;
        }

        @Override
        public void setCharacterEncoding(final String string) {
        }

        @Override
        public void setContentLength(final int i) {
        }

        @Override
        public void setContentLengthLong(final long l) {
        }

        @Override
        public void setContentType(final String string) {
        }

        @Override
        public void setBufferSize(final int i) {
        }

        @Override
        public int getBufferSize() {
            return 0;
        }

        @Override
        public void flushBuffer() throws IOException {
        }

        @Override
        public void resetBuffer() {
        }

        @Override
        public boolean isCommitted() {
            return true;
        }

        @Override
        public void reset() {
        }

        @Override
        public void setLocale(final Locale locale) {
        }

        @Override
        public Locale getLocale() {
            return null;
        }

        @Override
        public Collection<String> getHeaderNames() {
            return null;
        }

        @Override
        public Collection<String> getHeaders(String s) {
            return null;
        }

        @Override
        public String getHeader(String name) {
            return null;
        }

        @Override
        public int getStatus() {
            return 200;
        }

        @Override
        public void sendRedirect(String location, int sc, boolean clearBuffer) throws IOException {

        }
    }
}
