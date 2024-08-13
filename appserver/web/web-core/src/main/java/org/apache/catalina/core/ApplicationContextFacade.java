/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.apache.catalina.core;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

/**
 * Facade object which masks the internal <code>ApplicationContext</code>
 * object from the web application.
 *
 * @author Remy Maucherat 2008
 * @author Jean-Francois Arcand 2008
 * @author David Matejcek 2023
 */
public final class ApplicationContextFacade implements ServletContext {

    /** Wrapped application context. */
    private final ApplicationContext context;

    /**
     * Construct a new instance of this class, associated with the specified
     * Context instance.
     *
     * @param context The associated Context instance
     */
    public ApplicationContextFacade(ApplicationContext context) {
        this.context = context;
    }


    @Override
    public String getContextPath() {
        return context.getContextPath();
    }


    @Override
    public ServletContext getContext(String uripath) {
        final ServletContext theContext = context.getContext(uripath);
        if (theContext instanceof ApplicationContext) {
            return ((ApplicationContext) theContext).getFacade();
        }

        return theContext;
    }


    @Override
    public int getMajorVersion() {
        return context.getMajorVersion();
    }


    @Override
    public int getMinorVersion() {
        return context.getMinorVersion();
    }


    @Override
    public int getEffectiveMajorVersion() {
        return context.getEffectiveMajorVersion();
    }


    @Override
    public int getEffectiveMinorVersion() {
        return context.getEffectiveMinorVersion();
    }


    @Override
    public String getMimeType(String file) {
        return context.getMimeType(file);
    }


    @Override
    public Set<String> getResourcePaths(String path) {
        return context.getResourcePaths(path);
    }


    @Override
    public URL getResource(String path) throws MalformedURLException {
        return context.getResource(path);
    }


    @Override
    public InputStream getResourceAsStream(String path) {
        return context.getResourceAsStream(path);
    }


    @Override
    public RequestDispatcher getRequestDispatcher(final String path) {
        return context.getRequestDispatcher(path);
    }


    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        return context.getNamedDispatcher(name);
    }

    @Override
    public void log(String msg) {
        context.log(msg);
    }

    @Override
    public void log(String message, Throwable throwable) {
        context.log(message, throwable);
    }


    @Override
    public String getRealPath(String path) {
        return context.getRealPath(path);
    }


    @Override
    public String getServerInfo() {
        return context.getServerInfo();
    }


    @Override
    public String getInitParameter(String name) {
        return context.getInitParameter(name);
    }


    @Override
    public Enumeration<String> getInitParameterNames() {
        return context.getInitParameterNames();
    }


    @Override
    public boolean setInitParameter(String name, String value) {
        return context.setInitParameter(name, value);
    }


    @Override
    public Object getAttribute(String name) {
        return context.getAttribute(name);
    }


    @Override
    public Enumeration<String> getAttributeNames() {
        return context.getAttributeNames();
    }


    @Override
    public void setAttribute(String name, Object object) {
        context.setAttribute(name, object);
    }


    @Override
    public void removeAttribute(String name) {
        context.removeAttribute(name);
    }


    @Override
    public String getServletContextName() {
        return context.getServletContextName();
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        return context.addServlet(servletName, className);
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        return context.addServlet(servletName, servlet);
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        return context.addServlet(servletName, servletClass);
    }


    @Override
    public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
        return context.addJspFile(servletName, jspFile);
    }


    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        return context.createServlet(clazz);
    }


    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        return context.getServletRegistration(servletName);
    }


    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return context.getServletRegistrations();
    }


    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        return context.addFilter(filterName, className);
    }


    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {

        // Add a wrapper to the WebSocket (Tyrus) filter that corrects the URI
        // for HTTP upgrade requests when running on the root context.
        // Tyrus expects the URI to include the full context path (the application name).
        final Filter wrappedFilter;
        if ("WebSocket filter".equals(filterName)) {
            wrappedFilter = new WebSocketFilterWrapper(filter);
        } else {
            wrappedFilter = filter;
        }

        FilterRegistration.Dynamic registration = context.addFilter(filterName, wrappedFilter);

        if (registration == null && "WebSocket filter".equals(filterName)) {
            // Dummy registration to counter ordering issue between Mojarra
            // and Tyrus.
            // Should eventually be fixed in those projects.
            registration = new DummyFilterRegistrationDynamic();
        }

        return registration;
    }


    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        return context.addFilter(filterName, filterClass);
    }


    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        return context.createFilter(clazz);
    }


    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return context.getFilterRegistration(filterName);
    }


    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return context.getFilterRegistrations();
    }


    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return context.getSessionCookieConfig();
    }


    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
        context.setSessionTrackingModes(sessionTrackingModes);
    }


    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return context.getDefaultSessionTrackingModes();
    }


    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return context.getEffectiveSessionTrackingModes();
    }


    @Override
    public void addListener(String className) {
        context.addListener(className);
    }


    @Override
    public <T extends EventListener> void addListener(T listener) {
        context.addListener(listener);
    }


    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        context.addListener(listenerClass);
    }


    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        return context.createListener(clazz);
    }


    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return context.getJspConfigDescriptor();
    }


    @Override
    public ClassLoader getClassLoader() {
        return context.getClassLoader();
    }


    @Override
    public void declareRoles(String... roleNames) {
        context.declareRoles(roleNames);
    }

    @Override
    public String getVirtualServerName() {
        return context.getVirtualServerName();
    }

    @Override
    public int getSessionTimeout() {
        return context.getSessionTimeout();
    }

    @Override
    public void setSessionTimeout(int sessionTimeout) {
        context.setSessionTimeout(sessionTimeout);
    }

    @Override
    public String getRequestCharacterEncoding() {
        return context.getRequestCharacterEncoding();
    }

    @Override
    public void setRequestCharacterEncoding(String encoding) {
        context.setRequestCharacterEncoding(encoding);
    }

    @Override
    public String getResponseCharacterEncoding() {
        return context.getResponseCharacterEncoding();
    }

    @Override
    public void setResponseCharacterEncoding(String encoding) {
        context.setResponseCharacterEncoding(encoding);
    }

    @Override
    public String toString() {
        return super.toString() + "[context=" + context + ']';
    }
}
