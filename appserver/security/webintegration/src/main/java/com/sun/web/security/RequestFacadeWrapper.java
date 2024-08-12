/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.web.security;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
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
import jakarta.servlet.http.Part;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;

class RequestFacadeWrapper extends RequestFacade implements HttpServletRequest {

    private final HttpServletRequest servletRequest;

    RequestFacadeWrapper(Request request, HttpServletRequest servletRequest, boolean mask) {
        super(request, mask);
        this.servletRequest = servletRequest;
    }

    /**
     * methods defined by HttpServletRequest
     */
    @Override
    public String getAuthType() {
        return servletRequest.getAuthType();
    }

    @Override
    public Cookie[] getCookies() {
        return servletRequest.getCookies();
    }

    @Override
    public long getDateHeader(String name) {
        return servletRequest.getDateHeader(name);
    }

    @Override
    public String getHeader(String name) {
        return servletRequest.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return servletRequest.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return servletRequest.getHeaderNames();
    }

    @Override
    public int getIntHeader(String name) {
        return servletRequest.getIntHeader(name);
    }

    @Override
    public Map<String, String> getTrailerFields() {
        return servletRequest.getTrailerFields();
    }

    @Override
    public boolean isTrailerFieldsReady() {
        return servletRequest.isTrailerFieldsReady();
    }

    @Override
    public String getMethod() {
        return servletRequest.getMethod();
    }

    @Override
    public HttpServletMapping getHttpServletMapping() {
        return servletRequest.getHttpServletMapping();
    }

    @Override
    public String getPathInfo() {
        return servletRequest.getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        return servletRequest.getPathTranslated();
    }

    @Override
    public String getContextPath() {
        return servletRequest.getContextPath();
    }

    @Override
    public String getQueryString() {
        return servletRequest.getQueryString();
    }

    @Override
    public String getRemoteUser() {
        return servletRequest.getRemoteUser();
    }

    @Override
    public boolean isUserInRole(String role) {
        return servletRequest.isUserInRole(role);
    }

    @Override
    public Principal getUserPrincipal() {
        return servletRequest.getUserPrincipal();
    }

    @Override
    public String getRequestedSessionId() {
        return servletRequest.getRequestedSessionId();
    }

    @Override
    public String getRequestURI() {
        return servletRequest.getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL() {
        return servletRequest.getRequestURL();
    }

    @Override
    public String getServletPath() {
        return servletRequest.getServletPath();
    }

    @Override
    public HttpSession getSession(boolean create) {
        return servletRequest.getSession(create);
    }

    @Override
    public HttpSession getSession() {
        return servletRequest.getSession();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return servletRequest.isRequestedSessionIdValid();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return servletRequest.isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return servletRequest.isRequestedSessionIdFromURL();
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return servletRequest.authenticate(response);
    }

    @Override
    public void login(String username, String password) throws ServletException {
        servletRequest.login(username, password);
    }

    @Override
    public void logout() throws ServletException {
        servletRequest.logout();
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return servletRequest.getParts();
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return servletRequest.getPart(name);
    }

    /**
     * Methods inherited from ServletRequest
     */

    @Override
    public Object getAttribute(String name) {
        return servletRequest.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return servletRequest.getAttributeNames();
    }

    @Override
    public String getCharacterEncoding() {
        return servletRequest.getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        servletRequest.setCharacterEncoding(env);
    }

    @Override
    public int getContentLength() {
        return servletRequest.getContentLength();
    }

    @Override
    public String getContentType() {
        return servletRequest.getContentType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return servletRequest.getInputStream();
    }

    @Override
    public String getParameter(String name) {
        return servletRequest.getParameter(name);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return servletRequest.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name) {
        return servletRequest.getParameterValues(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return servletRequest.getParameterMap();
    }

    @Override
    public String getProtocol() {
        return servletRequest.getProtocol();
    }

    @Override
    public String getScheme() {
        return servletRequest.getScheme();
    }

    @Override
    public String getServerName() {
        return servletRequest.getServerName();
    }

    @Override
    public int getServerPort() {
        return servletRequest.getServerPort();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return servletRequest.getReader();
    }

    @Override
    public String getRemoteAddr() {
        return servletRequest.getRemoteAddr();
    }

    @Override
    public String getRemoteHost() {
        return servletRequest.getRemoteHost();
    }

    @Override
    public void setAttribute(String name, Object o) {
        servletRequest.setAttribute(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        servletRequest.removeAttribute(name);
    }

    @Override
    public Locale getLocale() {
        return servletRequest.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return servletRequest.getLocales();
    }

    @Override
    public boolean isSecure() {
        return servletRequest.isSecure();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return servletRequest.getRequestDispatcher(path);
    }

    @Override
    public int getRemotePort() {
        return servletRequest.getRemotePort();
    }

    @Override
    public String getLocalName() {
        return servletRequest.getLocalName();
    }

    @Override
    public String getLocalAddr() {
        return servletRequest.getLocalAddr();
    }

    @Override
    public int getLocalPort() {
        return servletRequest.getLocalPort();
    }

    @Override
    public ServletContext getServletContext() {
        return servletRequest.getServletContext();
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return servletRequest.startAsync();
    }

    @Override
    public AsyncContext startAsync(ServletRequest sRequest, ServletResponse sResponse) throws IllegalStateException {
        return servletRequest.startAsync(sRequest, sResponse);
    }

    @Override
    public boolean isAsyncStarted() {
        return servletRequest.isAsyncStarted();

    }

    @Override
    public boolean isAsyncSupported() {
        return servletRequest.isAsyncSupported();
    }

    @Override
    public AsyncContext getAsyncContext() {
        return servletRequest.getAsyncContext();
    }

    @Override
    public DispatcherType getDispatcherType() {
        return servletRequest.getDispatcherType();
    }

}
