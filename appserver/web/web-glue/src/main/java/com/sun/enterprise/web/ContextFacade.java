/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.web;

import static java.text.MessageFormat.format;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.catalina.core.DynamicFilterRegistrationImpl;
import org.apache.catalina.core.DynamicServletRegistrationImpl;
import org.apache.catalina.core.ServletRegistrationImpl;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.glassfish.embeddable.web.config.SecurityConfig;
import org.glassfish.web.LogFacade;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextAttributeListener;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletRequestAttributeListener;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionIdListener;
import jakarta.servlet.http.HttpSessionListener;

/**
 * Facade object which masks the internal <code>Context</code> object from the web application.
 *
 * @author Amy Roh
 */
public class ContextFacade extends WebModule {

    /**
     * The name of the deployed application
     */
    private String appName;
    private SecurityConfig config;

    /**
     * Wrapped web module.
     */
    private WebModule context;
    private File docRoot;
    private String contextRoot;
    private ClassLoader classLoader;

    private Map<String, String> filters = new HashMap<String, String>();
    private Map<String, String> servletNameFilterMappings = new HashMap<>();
    private Map<String, String> urlPatternFilterMappings = new HashMap<>();
    private Map<String, String> servlets = new HashMap<>();
    private Map<String, String[]> servletMappings = new HashMap<>();

    protected List<String> listenerNames = new ArrayList<>();

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a new instance of this class, associated with the specified Context instance.
     *
     * @param docRoot
     * @param contextRoot
     * @param classLoader
     *
     */
    public ContextFacade(File docRoot, String contextRoot, ClassLoader classLoader) {
        this.docRoot = docRoot;
        this.contextRoot = contextRoot;
        this.classLoader = classLoader;
    }


    // ------------------------------------------------------------- Properties

    public String getAppName() {
        return appName;
    }

    public void setAppName(String name) {
        appName = name;
    }

    @Override
    public String getContextRoot() {
        return contextRoot;
    }

    public File getDocRoot() {
        return docRoot;
    }

    // ------------------------------------------------- ServletContext Methods

    @Override
    public String getContextPath() {
        return context.getContextPath();
    }

    @Override
    public ServletContext getContext(String uripath) {
        return context.getContext(uripath);
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
    public void log(Exception exception, String msg) {
        context.log(exception, msg);
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

    /**
     * Returns previously added servlets
     */
    public Map<String, String> getAddedServlets() {
        return servlets;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        if (context != null) {
            return context.addServlet(servletName, className);
        }

        return addServletFacade(servletName, className);
    }


    public ServletRegistration.Dynamic addServletFacade(String servletName, String className) {
        if (servletName == null || className == null) {
            throw new NullPointerException("Null servlet instance or name");
        }

        DynamicServletRegistrationImpl dynamicServletRegistration = (DynamicServletRegistrationImpl) servletRegistrationMap.get(servletName);
        if (dynamicServletRegistration == null) {
            StandardWrapper wrapper = new StandardWrapper();
            wrapper.setName(servletName);
            wrapper.setServletClassName(className);

            dynamicServletRegistration = (DynamicServletRegistrationImpl) createDynamicServletRegistrationImpl(wrapper);

            DynamicServletRegistrationImpl tmpRegis = (DynamicServletRegistrationImpl) servletRegistrationMap.putIfAbsent(servletName, dynamicServletRegistration);
            if (tmpRegis != null) {
                dynamicServletRegistration = tmpRegis;
            }
            servlets.put(servletName, className);
        }

        return dynamicServletRegistration;
    }

    public Map<String, String[]> getServletMappings() {
        return servletMappings;
    }

    @Override
    protected ServletRegistrationImpl createServletRegistrationImpl(StandardWrapper wrapper) {
        return new ServletRegistrationImpl(wrapper, this);
    }

    @Override
    protected ServletRegistrationImpl createDynamicServletRegistrationImpl(StandardWrapper wrapper) {
        return new DynamicServletRegistrationImpl(wrapper, this);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        if (context != null) {
            return context.addServlet(servletName, servletClass);
        }

        return addServletFacade(servletName, servletClass.getName());
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        if (context != null) {
            return context.addServlet(servletName, servlet);
        }

        return addServletFacade(servletName, servlet.getClass().getName());
    }

    @Override
    public Set<String> addServletMapping(String name, String[] urlPatterns) {
        servletMappings.put(name, urlPatterns);
        return servletMappings.keySet();
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        if (context != null) {
            return context.createServlet(clazz);
        }

        try {
            return createServletInstance(clazz);
        } catch (Throwable t) {
            throw new ServletException("Unable to create Servlet from " + "class " + clazz.getName(), t);
        }
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        return servletRegistrationMap.get(servletName);
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return context.getServletRegistrations();
    }

    public Map<String, String> getAddedFilters() {
        return filters;
    }

    public Map<String, String> getServletNameFilterMappings() {
        return servletNameFilterMappings;
    }

    public Map<String, String> getUrlPatternFilterMappings() {
        return urlPatternFilterMappings;
    }

    public FilterRegistration.Dynamic addFilterFacade(String filterName, String className) {
        DynamicFilterRegistrationImpl regis = (DynamicFilterRegistrationImpl) filterRegistrationMap.get(filterName);
        FilterDef filterDef = null;
        if (regis == null) {
            filterDef = new FilterDef();
        } else {
            filterDef = regis.getFilterDefinition();
        }
        filterDef.setFilterName(filterName);
        filterDef.setFilterClassName(className);

        regis = new DynamicFilterRegistrationImpl(filterDef, this);
        filterRegistrationMap.put(filterDef.getFilterName(), regis);
        filters.put(filterName, className);

        return regis;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        if (context != null) {
            return context.addFilter(filterName, className);
        }

        return addFilterFacade(filterName, className);

    }

    @Override
    public void addFilterMap(FilterMap filterMap, boolean isMatchAfter) {
        if (filterMap.getServletName() != null) {
            servletNameFilterMappings.put(filterMap.getFilterName(), filterMap.getServletName());
        } else if (filterMap.getURLPattern() != null) {
            urlPatternFilterMappings.put(filterMap.getFilterName(), filterMap.getURLPattern());
        }
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        if (context != null) {
            return context.addFilter(filterName, filter);
        }

        return addFilterFacade(filterName, filter.getClass().getName());
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        if (context != null) {
            return context.addFilter(filterName, filterClass);
        }

        return addFilterFacade(filterName, filterClass.getName());
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        if (context != null) {
            return context.createFilter(clazz);
        }

        try {
            return createFilterInstance(clazz);
        } catch (Throwable t) {
            throw new ServletException("Unable to create Filter from " + "class " + clazz.getName(), t);
        }

    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return filterRegistrationMap.get(filterName);
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
        if (context != null) {
            context.addListener(className);
        } else {
            listenerNames.add(className);
        }
    }

    public List<String> getListeners() {
        return listenerNames;
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        if (context != null) {
            context.addListener(t);
        } else {
            listenerNames.add(t.getClass().getName());
        }
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        if (context != null) {
            context.addListener(listenerClass);
        } else {
            listenerNames.add(listenerClass.getName());
        }
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        if (context != null) {
            return context.createListener(clazz);
        }

        if (!ServletContextListener.class.isAssignableFrom(clazz)
                && !ServletContextAttributeListener.class.isAssignableFrom(clazz)
                && !ServletRequestListener.class.isAssignableFrom(clazz)
                && !ServletRequestAttributeListener.class.isAssignableFrom(clazz) && !HttpSessionListener.class.isAssignableFrom(clazz)
                && !HttpSessionAttributeListener.class.isAssignableFrom(clazz)
                && !HttpSessionIdListener.class.isAssignableFrom(clazz)) {

            throw new IllegalArgumentException(format(rb.getString(LogFacade.INVALID_LISTENER_TYPE), clazz.getName()));
        }

        try {
            return createListenerInstance(clazz);
        } catch (Throwable t) {
            throw new ServletException(t);
        }

    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return context.getJspConfigDescriptor();
    }

    @Override
    public ClassLoader getClassLoader() {
        if (classLoader != null) {
            return classLoader;
        }
        if (context != null) {
            return context.getClassLoader();
        }

        return null;
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
    public String getPath() {
        return context.getPath();
    }

    @Override
    public void setPath(String path) {
        context.setPath(path);
    }

    @Override
    public String getDefaultWebXml() {
        return context.getDefaultWebXml();
    }

    @Override
    public void setDefaultWebXml(String defaultWebXml) {
        context.setDefaultWebXml(defaultWebXml);
    }

    /**
     * Gets the underlying StandardContext to which this ContextFacade is ultimately delegating.
     *
     * @return The underlying StandardContext
     */
    public WebModule getUnwrappedContext() {
        return context;
    }

    public void setUnwrappedContext(WebModule wm) {
        context = wm;
    }

    // --------------------------------------------------------- embedded Methods

    /**
     * Enables or disables directory listings on this <tt>Context</tt>.
     */
    @Override
    public void setDirectoryListing(boolean directoryListing) {
        context.setDirectoryListing(directoryListing);
    }

    @Override
    public boolean isDirectoryListing() {
        return context.isDirectoryListing();
    }

    /**
     * Set the security related configuration for this context
     */
    @Override
    public void setSecurityConfig(SecurityConfig config) {
        this.config = config;
        if (config == null) {
            return;
        }

        if (context != null) {
            context.setSecurityConfig(config);
        }
    }

    /**
     * Gets the security related configuration for this context
     */
    @Override
    public SecurityConfig getSecurityConfig() {
        return config;
    }

}
