/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
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

package org.apache.catalina.core;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextAttributeEvent;
import jakarta.servlet.ServletContextAttributeListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.directory.DirContext;

import org.apache.catalina.ContainerEvent;
import org.apache.catalina.Globals;
import org.apache.catalina.LogFacade;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.ServerInfo;


/**
 * Standard implementation of <code>ServletContext</code> that represents
 * a web application's execution environment.  An instance of this class is
 * associated with each instance of <code>StandardContext</code>.
 *
 * @author Craig R. McClanahan 2008
 * @author Remy Maucherat 2008
 */
public class ApplicationContext implements ServletContext {

    private static final Logger log = LogFacade.getLogger();
    private static final ResourceBundle rb = log.getResourceBundle();

    /** The context attributes for this context. */
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    /** List of read only attributes for this context. */
    private final HashMap<String, String> readOnlyAttributes = new HashMap<>();

    /** Lock for synchronizing attributes and readOnlyAttributes */
    private final Object attributesLock = new Object();

    /** The Context instance with which we are associated. */
    private final StandardContext context;

    /** The facade around this object. */
    private final ServletContext facade = new ApplicationContextFacade(this);

    /** The merged context initialization parameters for this Context. */
    private final ConcurrentMap<String, String> parameters = new ConcurrentHashMap<>();

    private boolean isRestricted;


    /**
     * Construct a new instance of this class, associated with the specified
     * Context instance.
     *
     * @param context The associated Context instance
     */
    public ApplicationContext(StandardContext context) {
        this.context = context;
        setAttribute("com.sun.faces.useMyFaces", context.isUseMyFaces());
    }


    /**
     * @return the facade associated with this ApplicationContext.
     */
    protected ServletContext getFacade() {
        return this.facade;
    }


    /**
     * @return the resources object that is mapped to a specified path.
     *         The path must begin with a "/" and is interpreted as relative
     *         to the current context root.
     */
    public DirContext getResources() {
        return context.getResources();
    }


    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }


    @Override
    public Enumeration<String> getAttributeNames() {
        return new Enumerator<>(attributes.keySet(), true);
    }


    @Override
    public String getContextPath() {
        return context.getPath();
    }


    @Override
    public ServletContext getContext(String uri) {
        return context.getContext(uri);
    }


    @Override
    public String getInitParameter(final String name) {
        return parameters.get(name);
    }


    @Override
    public Enumeration<String> getInitParameterNames() {
        return new Enumerator<>(parameters.keySet());
    }


    @Override
    public boolean setInitParameter(String name, String value) {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        return parameters.putIfAbsent(name, value) == null;
    }


    @Override
    public int getMajorVersion() {
        return Constants.MAJOR_VERSION;
    }


    @Override
    public int getMinorVersion() {
        return Constants.MINOR_VERSION;
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
    public RequestDispatcher getNamedDispatcher(String name) {
        return context.getNamedDispatcher(name);
    }


    @Override
    public String getRealPath(String path) {
        return context.getRealPath(path);
    }


    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return context.getRequestDispatcher(path);
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
    public Set<String> getResourcePaths(String path) {
        return context.getResourcePaths(path);
    }


    @Override
    public String getServerInfo() {
        return ServerInfo.getServerInfo();
    }


    @Override
    public String getServletContextName() {
        return context.getDisplayName();
    }


    @Override
    public void log(String message) {
        context.log(message);
    }


    @Override
    public void log(String message, Throwable throwable) {
        context.log(message, throwable);
    }


    @Override
    public void removeAttribute(String name) {
        Object value = null;

        // Remove the specified attribute
        synchronized (attributesLock) {
            // Check for read only attribute
            if (readOnlyAttributes.containsKey(name)) {
                return;
            }
            value = attributes.remove(name);
            if (value == null) {
                return;
            }
        }

        // Notify interested application event listeners
        List<EventListener> listeners = context.getApplicationEventListeners();
        if (listeners.isEmpty()) {
            return;
        }

        ServletContextAttributeEvent event = new ServletContextAttributeEvent(context.getServletContext(), name, value);
        for (EventListener eventListener : listeners) {
            if (!(eventListener instanceof ServletContextAttributeListener)) {
                continue;
            }
            ServletContextAttributeListener listener = (ServletContextAttributeListener) eventListener;
            try {
                context.fireContainerEvent(ContainerEvent.BEFORE_CONTEXT_ATTRIBUTE_REMOVED, listener);
                listener.attributeRemoved(event);
                context.fireContainerEvent(ContainerEvent.AFTER_CONTEXT_ATTRIBUTE_REMOVED, listener);
            } catch (Throwable t) {
                context.fireContainerEvent(ContainerEvent.AFTER_CONTEXT_ATTRIBUTE_REMOVED, listener);
                // FIXME - should we do anything besides log these?
                log.log(Level.WARNING, LogFacade.ATTRIBUTES_EVENT_LISTENER_EXCEPTION, t);
            }
        }

    }


    @Override
    public void setAttribute(String name, Object value) {
        // Name cannot be null
        if (name == null) {
            throw new NullPointerException(rb.getString(LogFacade.NULL_NAME_EXCEPTION));
        }

        // Null value is the same as removeAttribute()
        if (value == null) {
            removeAttribute(name);
            return;
        }

        Object oldValue = null;
        boolean replaced = false;

        // Add or replace the specified attribute
        synchronized (attributesLock) {
            // Check for read only attribute
            if (readOnlyAttributes.containsKey(name)) {
                return;
            }
            oldValue = attributes.get(name);
            if (oldValue != null) {
                replaced = true;
            }
            attributes.put(name, value);
        }

        if (name.equals(Globals.CLASS_PATH_ATTR) || name.equals(Globals.JSP_TLD_URI_TO_LOCATION_MAP)) {
            setAttributeReadOnly(name);
        }

        // Notify interested application event listeners
        List<EventListener> listeners = context.getApplicationEventListeners();
        if (listeners.isEmpty()) {
            return;
        }

        ServletContextAttributeEvent event = null;
        if (replaced) {
            event = new ServletContextAttributeEvent(context.getServletContext(), name, oldValue);
        } else {
            event = new ServletContextAttributeEvent(context.getServletContext(), name, value);
        }

        for (EventListener eventListener : listeners) {
            if (!(eventListener instanceof ServletContextAttributeListener)) {
                continue;
            }
            ServletContextAttributeListener listener = (ServletContextAttributeListener) eventListener;
            try {
                if (replaced) {
                    context.fireContainerEvent(ContainerEvent.BEFORE_CONTEXT_ATTRIBUTE_REPLACED, listener);
                    listener.attributeReplaced(event);
                    context.fireContainerEvent(ContainerEvent.AFTER_CONTEXT_ATTRIBUTE_REPLACED, listener);
                } else {
                    context.fireContainerEvent(ContainerEvent.BEFORE_CONTEXT_ATTRIBUTE_ADDED, listener);
                    listener.attributeAdded(event);
                    context.fireContainerEvent(ContainerEvent.AFTER_CONTEXT_ATTRIBUTE_ADDED, listener);
                }
            } catch (Throwable t) {
                if (replaced) {
                    context.fireContainerEvent(ContainerEvent.AFTER_CONTEXT_ATTRIBUTE_REPLACED, listener);
                } else {
                    context.fireContainerEvent(ContainerEvent.AFTER_CONTEXT_ATTRIBUTE_ADDED, listener);
                }
                // FIXME - should we do anything besides log these?
                log.log(Level.WARNING, LogFacade.ATTRIBUTES_EVENT_LISTENER_EXCEPTION, t);
            }
        }
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        return context.addServlet(servletName, className);
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        return context.addServlet(servletName, servlet);
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        return context.addServlet(servletName, servletClass);
    }


    @Override
    public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        return context.addJspFile(servletName, jspFile);
    }


    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        return context.createServlet(clazz);
    }


    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        return context.getServletRegistration(servletName);
    }


    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        return context.getServletRegistrations();
    }


    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        return context.addFilter(filterName, className);
    }


    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        return context.addFilter(filterName, filter);
    }


    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        return context.addFilter(filterName, filterClass);
    }


    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        return context.createFilter(clazz);
    }


    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        return context.getFilterRegistration(filterName);
    }


    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        return context.getFilterRegistrations();
    }


    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        return context.getSessionCookieConfig();
    }


    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
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
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        context.addListener(className);
    }


    @Override
    public <T extends EventListener> void addListener(T t) {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        context.addListener(t);
    }


    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        context.addListener(listenerClass);
    }


    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
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
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
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
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        context.setSessionTimeout(sessionTimeout);
    }


    @Override
    public String getRequestCharacterEncoding() {
        return context.getRequestCharacterEncoding();
    }


    @Override
    public void setRequestCharacterEncoding(String encoding) {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        context.setRequestCharacterEncoding(encoding);
    }


    @Override
    public String getResponseCharacterEncoding() {
        return context.getResponseCharacterEncoding();
    }


    @Override
    public void setResponseCharacterEncoding(String encoding) {
        if (isRestricted) {
            throw new UnsupportedOperationException(rb.getString(LogFacade.UNSUPPORTED_OPERATION_EXCEPTION));
        }
        context.setResponseCharacterEncoding(encoding);
    }


    /**
     * Clear all application-created attributes.
     */
    void clearAttributes() {

        // Create list of attributes to be removed
        ArrayList<String> list = new ArrayList<>();
        synchronized (attributesLock) {
            for (String element : attributes.keySet()) {
                list.add(element);
            }
        }

        for (String key : list) {
            removeAttribute(key);
        }
    }


    /**
     * Set an attribute as read only.
     */
    void setAttributeReadOnly(String name) {
        synchronized (attributesLock) {
            if (attributes.containsKey(name)) {
                readOnlyAttributes.put(name, name);
            }
        }
    }


    void setRestricted(boolean isRestricted) {
        this.isRestricted = isRestricted;
    }


    @Override
    public String toString() {
        return super.toString() + "[context=" + context + ']';
    }
}
