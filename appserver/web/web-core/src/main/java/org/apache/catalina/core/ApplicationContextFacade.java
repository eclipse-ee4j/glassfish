/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import jakarta.servlet.DispatcherType;
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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

import static org.apache.catalina.Globals.IS_SECURITY_ENABLED;

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
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<String> action = context::getContextPath;
            return AccessController.doPrivileged(action);
        }
        return context.getContextPath();
    }


    @Override
    public ServletContext getContext(String uripath) {
        final ServletContext theContext;
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<ServletContext> action = () -> context.getContext(uripath);
            theContext = AccessController.doPrivileged(action);
        } else {
            theContext = context.getContext(uripath);
        }
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
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<String> action = () -> context.getMimeType(file);
            return AccessController.doPrivileged(action);
        }
        return context.getMimeType(file);
    }


    @Override
    public Set<String> getResourcePaths(String path) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Set<String>> action = () -> context.getResourcePaths(path);
            return AccessController.doPrivileged(action);
        }
        return context.getResourcePaths(path);
    }


    @Override
    public URL getResource(String path) throws MalformedURLException {
        if (IS_SECURITY_ENABLED) {
            PrivilegedExceptionAction<URL> action = () -> context.getResource(path);
            try {
                return AccessController.doPrivileged(action);
            } catch (PrivilegedActionException e) {
                throw (MalformedURLException) e.getCause();
            }
        }
        return context.getResource(path);
    }


    @Override
    public InputStream getResourceAsStream(String path) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<InputStream> action = () -> context.getResourceAsStream(path);
            return AccessController.doPrivileged(action);
        }
        return context.getResourceAsStream(path);
    }


    @Override
    public RequestDispatcher getRequestDispatcher(final String path) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<RequestDispatcher> action = () -> context.getRequestDispatcher(path);
            return AccessController.doPrivileged(action);
        }
        return context.getRequestDispatcher(path);
    }


    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<RequestDispatcher> action = () -> context.getNamedDispatcher(name);
            return AccessController.doPrivileged(action);
        }
        return context.getNamedDispatcher(name);
    }

    @Override
    public void log(String msg) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Void> action = () -> {
                context.log(msg);
                return null;
            };
            AccessController.doPrivileged(action);
        } else {
            context.log(msg);
        }
    }

    @Override
    public void log(String message, Throwable throwable) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Void> action = () -> {
                context.log(message, throwable);
                return null;
            };
            AccessController.doPrivileged(action);
        } else {
            context.log(message, throwable);
        }
    }


    @Override
    public String getRealPath(String path) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<String> action = () -> context.getRealPath(path);
            return AccessController.doPrivileged(action);
        }
        return context.getRealPath(path);
    }


    @Override
    public String getServerInfo() {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<String> action = context::getServerInfo;
            return AccessController.doPrivileged(action);
        }
        return context.getServerInfo();
    }


    @Override
    public String getInitParameter(String name) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<String> action = () -> context.getInitParameter(name);
            return AccessController.doPrivileged(action);
        }
        return context.getInitParameter(name);
    }


    @Override
    public Enumeration<String> getInitParameterNames() {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Enumeration<String>> action = context::getInitParameterNames;
            return AccessController.doPrivileged(action);
        }
        return context.getInitParameterNames();
    }


    @Override
    public boolean setInitParameter(String name, String value) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Void> action = () -> {
                context.setInitParameter(name, value);
                return null;
            };
            AccessController.doPrivileged(action);
        }
        return context.setInitParameter(name, value);
    }


    @Override
    public Object getAttribute(String name) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Object> action = () -> context.getAttribute(name);
            return AccessController.doPrivileged(action);
        }
        return context.getAttribute(name);
    }


    @Override
    public Enumeration<String> getAttributeNames() {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Enumeration<String>> action = context::getAttributeNames;
            return AccessController.doPrivileged(action);
        }
        return context.getAttributeNames();
    }


    @Override
    public void setAttribute(String name, Object object) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Void> action = () -> {
                context.setAttribute(name, object);
                return null;
            };
            AccessController.doPrivileged(action);
        } else {
            context.setAttribute(name, object);
        }
    }


    @Override
    public void removeAttribute(String name) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Void> action = () -> {
                context.removeAttribute(name);
                return null;
            };
            AccessController.doPrivileged(action);
        } else {
            context.removeAttribute(name);
        }
    }


    @Override
    public String getServletContextName() {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<String> action = context::getServletContextName;
            return AccessController.doPrivileged(action);
        }
        return context.getServletContextName();
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<ServletRegistration.Dynamic> action = () -> context.addServlet(servletName, className);
            return AccessController.doPrivileged(action);
        }
        return context.addServlet(servletName, className);
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<ServletRegistration.Dynamic> action = () -> context.addServlet(servletName, servlet);
            return AccessController.doPrivileged(action);
        }
        return context.addServlet(servletName, servlet);
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<ServletRegistration.Dynamic> action = () -> context.addServlet(servletName, servletClass);
            return AccessController.doPrivileged(action);
        }
        return context.addServlet(servletName, servletClass);
    }


    @Override
    public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<ServletRegistration.Dynamic> action = () -> context.addJspFile(servletName, jspFile);
            return AccessController.doPrivileged(action);
        }
        return context.addJspFile(servletName, jspFile);
    }


    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        if (IS_SECURITY_ENABLED) {
            PrivilegedExceptionAction<T> action = () -> context.createServlet(clazz);
            try {
                return AccessController.doPrivileged(action);
            } catch (PrivilegedActionException e) {
                throw (ServletException) e.getCause();
            }
        }
        return context.createServlet(clazz);
    }


    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<ServletRegistration> action = () -> context.getServletRegistration(servletName);
            return AccessController.doPrivileged(action);
        }
        return context.getServletRegistration(servletName);
    }


    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Map<String, ? extends ServletRegistration>> action = context::getServletRegistrations;
            return AccessController.doPrivileged(action);
        }
        return context.getServletRegistrations();
    }


    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<FilterRegistration.Dynamic> action = () -> context.addFilter(filterName, className);
            return AccessController.doPrivileged(action);
        }
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

        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<FilterRegistration.Dynamic> action = () -> context.addFilter(filterName, wrappedFilter);
            return AccessController.doPrivileged(action);
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
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<FilterRegistration.Dynamic> action = () -> context.addFilter(filterName, filterClass);
            return AccessController.doPrivileged(action);
        }
        return context.addFilter(filterName, filterClass);
    }


    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        if (IS_SECURITY_ENABLED) {
            PrivilegedExceptionAction<T> action = () -> context.createFilter(clazz);
            try {
                return AccessController.doPrivileged(action);
            } catch (PrivilegedActionException e) {
                throw (ServletException) e.getCause();
            }
        }
        return context.createFilter(clazz);
    }


    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<FilterRegistration> action = () -> context.getFilterRegistration(filterName);
            return AccessController.doPrivileged(action);
        }
        return context.getFilterRegistration(filterName);
    }


    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Map<String, ? extends FilterRegistration>> action = context::getFilterRegistrations;
            return AccessController.doPrivileged(action);
        }
        return context.getFilterRegistrations();
    }


    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<SessionCookieConfig> action = context::getSessionCookieConfig;
            return AccessController.doPrivileged(action);
        }
        return context.getSessionCookieConfig();
    }


    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Void> action = () -> {
                context.setSessionTrackingModes(sessionTrackingModes);
                return null;
            };
            AccessController.doPrivileged(action);
        } else {
            context.setSessionTrackingModes(sessionTrackingModes);
        }
    }


    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Set<SessionTrackingMode>> action = context::getDefaultSessionTrackingModes;
            return AccessController.doPrivileged(action);
        }
        return context.getDefaultSessionTrackingModes();
    }


    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Set<SessionTrackingMode>> action = context::getEffectiveSessionTrackingModes;
            return AccessController.doPrivileged(action);
        }
        return context.getEffectiveSessionTrackingModes();
    }


    @Override
    public void addListener(String className) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Void> action = () -> {
                context.addListener(className);
                return null;
            };
            AccessController.doPrivileged(action);
        } else {
            context.addListener(className);
        }
    }


    @Override
    public <T extends EventListener> void addListener(T listener) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Void> action = () -> {
                context.addListener(listener);
                return null;
            };
            AccessController.doPrivileged(action);
        } else {
            context.addListener(listener);
        }
    }


    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Void> action = () -> {
                context.addListener(listenerClass);
                return null;
            };
            AccessController.doPrivileged(action);
        } else {
            context.addListener(listenerClass);
        }
    }


    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        if (IS_SECURITY_ENABLED) {
            PrivilegedExceptionAction<T> action = () -> context.createListener(clazz);
            try {
                return AccessController.doPrivileged(action);
            } catch (PrivilegedActionException e) {
                throw (ServletException) e.getCause();
            }
        }
        return context.createListener(clazz);
    }


    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<JspConfigDescriptor> action = context::getJspConfigDescriptor;
            return AccessController.doPrivileged(action);
        }
        return context.getJspConfigDescriptor();
    }


    @Override
    public ClassLoader getClassLoader() {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<ClassLoader> action = context::getClassLoader;
            return AccessController.doPrivileged(action);
        }
        return context.getClassLoader();
    }


    @Override
    public void declareRoles(String... roleNames) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Void> action = () -> {
                context.declareRoles(roleNames);
                return null;
            };
            AccessController.doPrivileged(action);
        } else {
            context.declareRoles(roleNames);
        }
    }

    @Override
    public String getVirtualServerName() {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<String> action = context::getVirtualServerName;
            return AccessController.doPrivileged(action);
        }
        return context.getVirtualServerName();
    }

    @Override
    public int getSessionTimeout() {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Integer> action = context::getSessionTimeout;
            return AccessController.doPrivileged(action);
        }
        return context.getSessionTimeout();
    }

    @Override
    public void setSessionTimeout(int sessionTimeout) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Void> action = () -> {
                context.setSessionTimeout(sessionTimeout);
                return null;
            };
            AccessController.doPrivileged(action);
        } else {
            context.setSessionTimeout(sessionTimeout);
        }
    }

    @Override
    public String getRequestCharacterEncoding() {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<String> action = context::getRequestCharacterEncoding;
            return AccessController.doPrivileged(action);
        }
        return context.getRequestCharacterEncoding();
    }

    @Override
    public void setRequestCharacterEncoding(String encoding) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Void> action = () -> {
                context.setRequestCharacterEncoding(encoding);
                return null;
            };
            AccessController.doPrivileged(action);
        } else {
            context.setRequestCharacterEncoding(encoding);
        }
    }

    @Override
    public String getResponseCharacterEncoding() {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<String> action = context::getResponseCharacterEncoding;
            return AccessController.doPrivileged(action);
        }
        return context.getResponseCharacterEncoding();
    }

    @Override
    public void setResponseCharacterEncoding(String encoding) {
        if (IS_SECURITY_ENABLED) {
            PrivilegedAction<Void> action = () -> {
                context.setResponseCharacterEncoding(encoding);
                return null;
            };
            AccessController.doPrivileged(action);
        } else {
            context.setResponseCharacterEncoding(encoding);
        }
    }

    @Override
    public String toString() {
        return super.toString() + "[context=" + context + ']';
    }
}
