/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.catalina.connector;


import com.sun.enterprise.security.ee.web.integration.WebPrincipal;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;
import jakarta.servlet.http.PushBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import org.apache.catalina.LogFacade;
import org.apache.catalina.core.RequestFacadeHelper;

/**
 * Facade class that wraps a Catalina connector request object. All methods are delegated to the wrapped request.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @author Jean-Francois Arcand
 * @version $Revision: 1.7 $ $Date: 2007/08/01 19:04:28 $
 */
public class RequestFacade implements HttpServletRequest {

    // ----------------------------------------------- Class/Instance Variables

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    /**
     * The wrapped request.
     */
    protected Request catalinaConnectorReqest;

    /*
     * True if the fact that a request received at the root context was mapped to a default-web-module will be masked, false
     * otherwise.
     *
     * For example, if set to true, this request facade's getContextPath() method will return "/", rather than the context
     * root of the default-web-module, for requests received at the root context that were mapped to a default-web-module.
     */
    private boolean maskDefaultContextMapping;

    private RequestFacadeHelper reqFacHelper;


    // ----------------------------------------------------------- Constructors

    /**
     * Construct a wrapper for the specified request.
     *
     * @param connectorRequest The request to be wrapped
     */
    public RequestFacade(Request connectorRequest) {
        this(connectorRequest, false);
    }

    /**
     * Construct a wrapper for the specified request.
     *
     * @param request The request to be wrapped
     * @param maskDefaultContextMapping true if the fact that a request received at the root context was mapped to a
     * default-web-module will be masked, false otherwise
     */
    public RequestFacade(Request request, boolean maskDefaultContextMapping) {
        this.catalinaConnectorReqest = request;
        this.maskDefaultContextMapping = maskDefaultContextMapping;
        this.reqFacHelper = new RequestFacadeHelper(request);
    }


    // --------------------------------------------------------- Public Methods

    /**
     * Prevent cloning the facade.
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Clear facade.
     */
    public void clear() {
        catalinaConnectorReqest = null;
        if (reqFacHelper != null) {
            reqFacHelper.clear();
        }
        reqFacHelper = null;
    }

    RequestFacadeHelper getRequestFacadeHelper() {
        return reqFacHelper;
    }

    // ------------------------------------------------- ServletRequest Methods

    @Override
    public Object getAttribute(String name) {
        checkRequestNull();

        return catalinaConnectorReqest.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        checkRequestNull();

        return catalinaConnectorReqest.getAttributeNames();
    }

    @Override
    public String getCharacterEncoding() {
        checkRequestNull();

        return catalinaConnectorReqest.getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(String env) throws java.io.UnsupportedEncodingException {
        checkRequestNull();

        catalinaConnectorReqest.setCharacterEncoding(env);
    }

    @Override
    public int getContentLength() {
        checkRequestNull();

        return catalinaConnectorReqest.getContentLength();
    }

    @Override
    public long getContentLengthLong() {
        checkRequestNull();

        return catalinaConnectorReqest.getContentLengthLong();
    }

    @Override
    public String getContentType() {
        checkRequestNull();

        return catalinaConnectorReqest.getContentType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        checkRequestNull();

        return catalinaConnectorReqest.getInputStream();
    }

    @Override
    public HttpServletMapping getHttpServletMapping() {
        checkRequestNull();

        return catalinaConnectorReqest.getHttpServletMapping();
    }

    @Override
    public String getParameter(String name) {
        checkRequestNull();

        return catalinaConnectorReqest.getParameter(name);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        checkRequestNull();

        return catalinaConnectorReqest.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name) {
        checkRequestNull();

        return catalinaConnectorReqest.getParameterValues(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        checkRequestNull();

        return catalinaConnectorReqest.getParameterMap();
    }

    @Override
    public String getProtocol() {
        checkRequestNull();

        return catalinaConnectorReqest.getProtocol();
    }

    @Override
    public String getScheme() {
        checkRequestNull();

        return catalinaConnectorReqest.getScheme();
    }

    @Override
    public String getServerName() {
        checkRequestNull();

        return catalinaConnectorReqest.getServerName();
    }

    @Override
    public int getServerPort() {
        checkRequestNull();

        return catalinaConnectorReqest.getServerPort();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        checkRequestNull();

        return catalinaConnectorReqest.getReader();
    }

    @Override
    public String getRemoteAddr() {
        checkRequestNull();

        return catalinaConnectorReqest.getRemoteAddr();
    }

    @Override
    public String getRemoteHost() {
        checkRequestNull();

        return catalinaConnectorReqest.getRemoteHost();
    }

    @Override
    public void setAttribute(String name, Object o) {
        checkRequestNull();

        catalinaConnectorReqest.setAttribute(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        checkRequestNull();

        catalinaConnectorReqest.removeAttribute(name);
    }

    @Override
    public Locale getLocale() {
        checkRequestNull();

        return catalinaConnectorReqest.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        checkRequestNull();

        return catalinaConnectorReqest.getLocales();
    }

    @Override
    public boolean isSecure() {
        checkRequestNull();

        return catalinaConnectorReqest.isSecure();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        checkRequestNull();

        return catalinaConnectorReqest.getRequestDispatcher(path);
    }

    @Override
    public String getAuthType() {
        checkRequestNull();

        return catalinaConnectorReqest.getAuthType();
    }

    @Override
    public Cookie[] getCookies() {
        checkRequestNull();

        return catalinaConnectorReqest.getCookies();
    }

    @Override
    public long getDateHeader(String name) {
        checkRequestNull();

        return catalinaConnectorReqest.getDateHeader(name);
    }

    @Override
    public String getHeader(String name) {
        checkRequestNull();

        return catalinaConnectorReqest.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        checkRequestNull();

        return catalinaConnectorReqest.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        checkRequestNull();

        return catalinaConnectorReqest.getHeaderNames();
    }

    @Override
    public int getIntHeader(String name) {
        checkRequestNull();

        return catalinaConnectorReqest.getIntHeader(name);
    }

    @Override
    public Map<String, String> getTrailerFields() {
        checkRequestNull();

        return catalinaConnectorReqest.getTrailerFields();
    }

    @Override
    public boolean isTrailerFieldsReady() {
        checkRequestNull();

        return catalinaConnectorReqest.isTrailerFieldsReady();
    }

    @Override
    public String getMethod() {
        checkRequestNull();

        return catalinaConnectorReqest.getMethod();
    }

    @Override
    public String getPathInfo() {
        checkRequestNull();

        return catalinaConnectorReqest.getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        checkRequestNull();

        return catalinaConnectorReqest.getPathTranslated();
    }

    /**
     * Gets the servlet context to which this servlet request was last dispatched.
     *
     * @return the servlet context to which this servlet request was last dispatched
     */
    @Override
    public ServletContext getServletContext() {
        return catalinaConnectorReqest.getServletContext();
    }

    @Override
    public String getContextPath() {
        checkRequestNull();

        return catalinaConnectorReqest.getContextPath(maskDefaultContextMapping);
    }

    public String getContextPath(boolean maskDefaultContextMapping) {
        checkRequestNull();

        return catalinaConnectorReqest.getContextPath(maskDefaultContextMapping);
    }

    @Override
    public String getQueryString() {
        checkRequestNull();

        return catalinaConnectorReqest.getQueryString();
    }

    @Override
    public String getRemoteUser() {
        checkRequestNull();

        return catalinaConnectorReqest.getRemoteUser();
    }

    @Override
    public boolean isUserInRole(String role) {
        checkRequestNull();

        return catalinaConnectorReqest.isUserInRole(role);
    }

    @Override
    public Principal getUserPrincipal() {
        checkRequestNull();

        Principal principal = catalinaConnectorReqest.getUserPrincipal();
        if (principal instanceof WebPrincipal) {
            WebPrincipal webPrincipal = (WebPrincipal) principal;
            if (webPrincipal.getCustomPrincipal() != null) {
                principal = webPrincipal.getCustomPrincipal();
            }
        }

        return principal;
    }

    // returns the original, unwrapped principal from the underlying request
    public Principal getRequestPrincipal() {
        checkRequestNull();

        return catalinaConnectorReqest.getUserPrincipal();
    }

    @Override
    public String getRequestedSessionId() {
        checkRequestNull();

        return catalinaConnectorReqest.getRequestedSessionId();
    }

    @Override
    public String getRequestURI() {
        checkRequestNull();

        return catalinaConnectorReqest.getRequestURI(maskDefaultContextMapping);
    }

    @Override
    public StringBuffer getRequestURL() {
        checkRequestNull();

        return catalinaConnectorReqest.getRequestURL(maskDefaultContextMapping);
    }

    @Override
    public String getServletPath() {
        checkRequestNull();

        return catalinaConnectorReqest.getServletPath();
    }

    @Override
    public HttpSession getSession(boolean create) {
        checkRequestNull();

        return catalinaConnectorReqest.getSession(create);
    }

    @Override
    public HttpSession getSession() {
        checkRequestNull();

        return getSession(true);
    }

    @Override
    public String changeSessionId() {
        checkRequestNull();

        return catalinaConnectorReqest.changeSessionId();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        checkRequestNull();

        return catalinaConnectorReqest.isRequestedSessionIdValid();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        checkRequestNull();

        return catalinaConnectorReqest.isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        checkRequestNull();

        return catalinaConnectorReqest.isRequestedSessionIdFromURL();
    }

    @Override
    public String getLocalAddr() {
        checkRequestNull();

        return catalinaConnectorReqest.getLocalAddr();
    }

    @Override
    public String getLocalName() {
        checkRequestNull();

        return catalinaConnectorReqest.getLocalName();
    }

    @Override
    public int getLocalPort() {
        checkRequestNull();

        return catalinaConnectorReqest.getLocalPort();
    }

    @Override
    public int getRemotePort() {
        checkRequestNull();

        return catalinaConnectorReqest.getRemotePort();
    }

    @Override
    public DispatcherType getDispatcherType() {
        checkRequestNull();

        return catalinaConnectorReqest.getDispatcherType();
    }

    /**
     * Starts async processing on this request.
     */
    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        checkRequestNull();

        return catalinaConnectorReqest.startAsync();
    }

    /**
     * Starts async processing on this request.
     */
    @Override
    public AsyncContext startAsync(ServletRequest sreq, ServletResponse sresp) throws IllegalStateException {
        checkRequestNull();

        return catalinaConnectorReqest.startAsync(sreq, sresp);
    }

    /**
     * Checks whether async processing has started on this request.
     */
    @Override
    public boolean isAsyncStarted() {
        checkRequestNull();

        return catalinaConnectorReqest.isAsyncStarted();
    }

    /**
     * Checks whether this request supports async.
     */
    @Override
    public boolean isAsyncSupported() {
        checkRequestNull();

        return catalinaConnectorReqest.isAsyncSupported();
    }

    /**
     * Gets the AsyncContext of this request.
     */
    @Override
    public AsyncContext getAsyncContext() {
        checkRequestNull();

        return catalinaConnectorReqest.getAsyncContext();
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        checkRequestNull();

        return catalinaConnectorReqest.getParts();
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        checkRequestNull();

        return catalinaConnectorReqest.getPart(name);
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        checkRequestNull();

        return catalinaConnectorReqest.authenticate(response);
    }

    @Override
    public void login(String username, String password) throws ServletException {
        checkRequestNull();

        catalinaConnectorReqest.login(username, password);
    }

    @Override
    public void logout() throws ServletException {
        checkRequestNull();

        catalinaConnectorReqest.logout();
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        checkRequestNull();

        return catalinaConnectorReqest.upgrade(handlerClass);
    }

    @Override
    public PushBuilder newPushBuilder() {
        return catalinaConnectorReqest.newPushBuilder();
    }

    @Override
    public String toString() {
        return catalinaConnectorReqest.toString();
    }

    /**
     * Return the original <code>CoyoteRequest</code> object.
     */
    public Request getUnwrappedCoyoteRequest() {
        return catalinaConnectorReqest;
    }

    @Override
    public String getRequestId() {
        checkRequestNull();

        return catalinaConnectorReqest.getRequestId();
    }

    @Override
    public String getProtocolRequestId() {
        checkRequestNull();

        return catalinaConnectorReqest.getProtocolRequestId();
    }

    @Override
    public ServletConnection getServletConnection() {
        checkRequestNull();

        return catalinaConnectorReqest.getServletConnection();
    }

    private void checkRequestNull() {
        if (catalinaConnectorReqest == null) {
            throw new IllegalStateException(rb.getString(LogFacade.CANNOT_USE_REQUEST_OBJECT_OUTSIDE_SCOPE_EXCEPTION));
        }
    }

}
