/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.Globals;
import org.apache.catalina.LogFacade;
import org.apache.catalina.security.SecurityUtil;

/**
 * Facade object which masks the internal <code>ApplicationContext</code>
 * object from the web application.
 *
 * @author Remy Maucherat
 * @author Jean-Francois Arcand
 * @version $Revision: 1.7.6.1 $ $Date: 2008/04/17 18:37:06 $
 */

public final class ApplicationContextFacade
    implements ServletContext {

    private static final Logger log = LogFacade.getLogger();

    // ---------------------------------------------------------- Attributes
    /**
     * Cache Class object used for reflection.
     */
    private static HashMap<String, Class<?>[]> classCache = new HashMap<>();

    static {
        Class<?>[] clazz = new Class[]{String.class};
        classCache.put("addFilter", new Class[]{String.class, String.class});
        classCache.put("addListener", clazz);
        classCache.put("addServlet", new Class[]{String.class, String.class});
        classCache.put("addJspFile", new Class[]{String.class, String.class});
        classCache.put("createFilter", new Class[]{Class.class});
        classCache.put("createListener", new Class[]{Class.class});
        classCache.put("createServlet", new Class[]{Class.class});
        classCache.put("declareRoles", new Class<?>[] {(new String[0]).getClass()});
        classCache.put("getAttribute", clazz);
        classCache.put("getContext", clazz);
        classCache.put("getFilterRegistration", clazz);
        classCache.put("getInitParameter", clazz);
        classCache.put("getMimeType", clazz);
        classCache.put("getNamedDispatcher", clazz);
        classCache.put("getRealPath", clazz);
        classCache.put("getResourcePaths", clazz);
        classCache.put("getResource", clazz);
        classCache.put("getResourceAsStream", clazz);
        classCache.put("getRequestDispatcher", clazz);
        classCache.put("getServlet", clazz);
        classCache.put("getServletRegistration", clazz);
        classCache.put("log", clazz);
        classCache.put("removeAttribute", clazz);
        classCache.put("setAttribute", new Class[]{String.class, Object.class});
        classCache.put("setInitParameter", new Class[]{String.class, String.class});
        classCache.put("setSessionTrackingModes", new Class[]{Set.class});
        classCache.put("getSessionTimeout", new Class[]{});
        classCache.put("setSessionTimeout", new Class[]{Integer.class});
        classCache.put("getRequestCharacterEncoding", new Class[]{});
        classCache.put("setRequestCharacterEncoding", new Class[]{String.class});
        classCache.put("getResponseCharacterEncoding", new Class[]{});
        classCache.put("setResponseCharacterEncoding", new Class[]{String.class});
    }

    /**
     * Cache method object.
     */
    private final HashMap<String, Method> objectCache;


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this class, associated with the specified
     * Context instance.
     *
     * @param context The associated Context instance
     */
    public ApplicationContextFacade(ApplicationContext context) {
        this.context = context;
        objectCache = new HashMap<>();
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Wrapped application context.
     */
    private final ApplicationContext context;



    // ------------------------------------------------- ServletContext Methods

    @Override
    public String getContextPath() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("getContextPath", null);
        }
        return context.getContextPath();
    }


    @Override
    public ServletContext getContext(String uripath) {
        ServletContext theContext;
        if (SecurityUtil.isPackageProtectionEnabled()) {
            theContext = doPrivileged("getContext", new Object[] {uripath});
        } else {
            theContext = context.getContext(uripath);
        }
        if (theContext != null && (theContext instanceof ApplicationContext)) {
            theContext = ((ApplicationContext) theContext).getFacade();
        }
        return (theContext);
    }


    @Override
    public int getMajorVersion() {
        return context.getMajorVersion();
    }


    @Override
    public int getMinorVersion() {
        return context.getMinorVersion();
    }


    /**
     * Gets the major version of the Servlet specification that the
     * application represented by this ServletContext is based on.
     */
    @Override
    public int getEffectiveMajorVersion() {
        return context.getEffectiveMajorVersion();
    }


    /**
     * Gets the minor version of the Servlet specification that the
     * application represented by this ServletContext is based on.
     */
    @Override
    public int getEffectiveMinorVersion() {
        return context.getEffectiveMinorVersion();
    }


    @Override
    public String getMimeType(String file) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("getMimeType", new Object[]{file});
        }
        return context.getMimeType(file);
    }


    @Override
    public Set<String> getResourcePaths(String path) {
        if (SecurityUtil.isPackageProtectionEnabled()){
            return doPrivileged("getResourcePaths", new Object[] {path});
        }
        return context.getResourcePaths(path);
    }


    @Override
    public URL getResource(String path) throws MalformedURLException {
        if (Globals.IS_SECURITY_ENABLED) {
            try {
                return invokeMethod(context, "getResource", new Object[] {path});
            } catch (Throwable t) {
                if (t instanceof MalformedURLException) {
                    throw (MalformedURLException) t;
                }
                return null;
            }
        }
        return context.getResource(path);
    }


    @Override
    public InputStream getResourceAsStream(String path) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("getResourceAsStream", new Object[] {path});
        }
        return context.getResourceAsStream(path);
    }


    @Override
    public RequestDispatcher getRequestDispatcher(final String path) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("getRequestDispatcher", new Object[] {path});
        }
        return context.getRequestDispatcher(path);
    }


    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("getNamedDispatcher", new Object[] {name});
        }
        return context.getNamedDispatcher(name);
    }

    @Override
    public void log(String msg) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("log", new Object[]{msg} );
        } else {
            context.log(msg);
        }
    }

    @Override
    public void log(String message, Throwable throwable) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("log", new Class[] {String.class, Throwable.class}, new Object[] {message, throwable});
        } else {
            context.log(message, throwable);
        }
    }


    @Override
    public String getRealPath(String path) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("getRealPath", new Object[]{path});
        }
        return context.getRealPath(path);
    }


    @Override
    public String getServerInfo() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("getServerInfo", null);
        }
        return context.getServerInfo();
    }


    @Override
    public String getInitParameter(String name) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("getInitParameter", new Object[] {name});
        }
        return context.getInitParameter(name);
    }


    @Override
    public Enumeration<String> getInitParameterNames() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("getInitParameterNames", null);
        }
        return context.getInitParameterNames();
    }

    /**
     * @return true if the context initialization parameter with the given
     * name and value was set successfully on this ServletContext, and false
     * if it was not set because this ServletContext already contains a
     * context initialization parameter with a matching name
     */
    @Override
    public boolean setInitParameter(String name, String value) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("setInitParameter", new Object[] {name, value});
        }
        return context.setInitParameter(name, value);
    }


    @Override
    public Object getAttribute(String name) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("getAttribute", new Object[]{name});
        }
        return context.getAttribute(name);
     }


    @Override
    public Enumeration<String> getAttributeNames() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("getAttributeNames", null);
        }
        return context.getAttributeNames();
    }


    @Override
    public void setAttribute(String name, Object object) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("setAttribute", new Object[]{name,object});
        } else {
            context.setAttribute(name, object);
        }
    }


    @Override
    public void removeAttribute(String name) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("removeAttribute", new Object[]{name});
        } else {
            context.removeAttribute(name);
        }
    }


    @Override
    public String getServletContextName() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("getServletContextName", null);
        }
        return context.getServletContextName();
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("addServlet", new Object[] {servletName, className});
        }
        return context.addServlet(servletName, className);
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("addServlet", new Class[] {String.class, Servlet.class},
                new Object[] {servletName, servlet});
        }
        return context.addServlet(servletName, servlet);
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("addServlet", new Class[] {String.class, Class.class},
                new Object[] {servletName, servletClass});
        }
        return context.addServlet(servletName, servletClass);
    }

    @Override
    public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("addJspFile", new Object[] {servletName, jspFile});
        }
        return context.addJspFile(servletName, jspFile);
    }


    /**
     * Instantiates the given Servlet class and performs any required
     * resource injection into the new Servlet instance before returning
     * it.
     */
    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("createServlet", new Object[] {clazz});
        }
        return context.createServlet(clazz);
    }


    /**
     * Gets the ServletRegistration corresponding to the servlet with the
     * given <tt>servletName</tt>.
     */
    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("getServletRegistration", new Object[] {servletName});
        }
        return context.getServletRegistration(servletName);
    }


    /**
     * Gets a Map of the ServletRegistration objects corresponding to all
     * currently registered servlets.
     */
    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("getServletRegistrations", null);
        }
        return context.getServletRegistrations();
    }


    /**
     * Adds the filter with the given name and class name to this servlet context.
     */
    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("addFilter", new Object[] {filterName, className});
        }
        return context.addFilter(filterName, className);
    }


    /**
     * Registers the given filter instance with this ServletContext
     * under the given <tt>filterName</tt>.
     */
    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("addFilter", new Class[] {String.class, Filter.class},
                new Object[] {filterName, filter});
        }
        return context.addFilter(filterName, filter);
    }


    /**
     * Adds the filter with the given name and class type to this servlet
     * context.
     */
    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("addFilter", new Class[] {String.class, Class.class},
                new Object[] {filterName, filterClass});
        }
        return context.addFilter(filterName, filterClass);
    }


    /**
     * Instantiates the given Filter class and performs any required
     * resource injection into the new Filter instance before returning
     * it.
     */
    @Override
    public <T extends Filter> T createFilter(Class<T> clazz)
            throws ServletException {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("createFilter", new Object[] {clazz});
        }
        return context.createFilter(clazz);
    }


    /**
     * Gets the FilterRegistration corresponding to the filter with the
     * given <tt>filterName</tt>.
     */
    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("getFilterRegistration", new Object[] {filterName});
        }
        return context.getFilterRegistration(filterName);
    }


    /**
     * Gets a Map of the FilterRegistration objects corresponding to all
     * currently registered filters.
     */
    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("getFilterRegistrations", null);
        }
        return context.getFilterRegistrations();
    }


    /**
     * Gets the <tt>SessionCookieConfig</tt> object through which various
     * properties of the session tracking cookies created on behalf of this
     * <tt>ServletContext</tt> may be configured.
     */
    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("getSessionCookieConfig", null);
        }
        return context.getSessionCookieConfig();
    }


    /**
     * Sets the session tracking modes that are to become effective for this
     * <tt>ServletContext</tt>.
     */
    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("setSessionTrackingModes", new Object[] {sessionTrackingModes});
        } else {
            context.setSessionTrackingModes(sessionTrackingModes);
        }
    }


    /**
     * Gets the session tracking modes that are supported by default for this
     * <tt>ServletContext</tt>.
     *
     * @return set of the session tracking modes supported by default for
     * this <tt>ServletContext</tt>
     */
    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("getDefaultSessionTrackingModes", null);
        }
        return context.getDefaultSessionTrackingModes();
    }


    /**
     * Gets the session tracking modes that are in effect for this
     * <tt>ServletContext</tt>.
     *
     * @return set of the session tracking modes in effect for this
     * <tt>ServletContext</tt>
     */
    @Override
    @SuppressWarnings("unchecked") // doPrivileged() returns the correct type
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (Set<SessionTrackingMode>)
                doPrivileged("getEffectiveSessionTrackingModes", null);
        }
        return context.getEffectiveSessionTrackingModes();
    }


    /**
     * Adds the listener with the given class name to this ServletContext.
     */
    @Override
    public void addListener(String className) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("addListener",
                    new Object[]{className});
        } else {
            context.addListener(className);
        }
    }


    /**
     * Adds the given listener to this ServletContext.
     */
    @Override
    public <T extends EventListener> void addListener(T t) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("addListener",
                    new Class[]{EventListener.class},
                    new Object[]{t.getClass().getName()});
        } else {
            context.addListener(t);
        }
    }


    /**
     * Adds a listener of the given class type to this ServletContext.
     */
    @Override
    public void addListener(Class <? extends EventListener> listenerClass) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("addListener",
                    new Class[]{Class.class},
                    new Object[]{listenerClass.getName()});
        } else {
            context.addListener(listenerClass);
        }
    }


    /**
     * Instantiates the given EventListener class and performs any
     * required resource injection into the new EventListener instance
     * before returning it.
     */
    @Override
    @SuppressWarnings("unchecked") // doPrivileged() returns the correct type
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (T) doPrivileged("createListener", new Object[] {clazz});
        }
        return context.createListener(clazz);
    }


    /**
     * Gets the <code>&lt;jsp-config&gt;</code> related configuration
     * that was aggregated from the <code>web.xml</code> and
     * <code>web-fragment.xml</code> descriptor files of the web application
     * represented by this ServletContext.
     */
    @Override
    @SuppressWarnings("unchecked") // doPrivileged() returns the correct type
    public JspConfigDescriptor getJspConfigDescriptor() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (JspConfigDescriptor) doPrivileged("getJspConfigDescriptor",
                    null);
        }
        return context.getJspConfigDescriptor();
    }


    @Override
    @SuppressWarnings("unchecked") // doPrivileged() returns the correct type
    public ClassLoader getClassLoader() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (ClassLoader) doPrivileged("getClassLoader", null);
        }
        return context.getClassLoader();
    }


    @Override
    public void declareRoles(String... roleNames) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("declareRoles", roleNames);
        } else {
            context.declareRoles(roleNames);
        }
    }

    @Override
    public String getVirtualServerName() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (String)doPrivileged("getVirtualServerName", null);
        }
        return context.getVirtualServerName();
    }

    @Override
    public int getSessionTimeout() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (Integer)doPrivileged("getSessionTimeout", null);
        }
        return context.getSessionTimeout();
    }

    @Override
    public void setSessionTimeout(int sessionTimeout) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("getSessionTimeout", null);
        } else {
            context.setSessionTimeout(sessionTimeout);
        }
    }

    @Override
    public String getRequestCharacterEncoding() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (String)doPrivileged("getRequestCharacterEncoding", null);
        }
        return context.getRequestCharacterEncoding();
    }

    @Override
    public void setRequestCharacterEncoding(String encoding) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("setRequestCharacterEncoding", null);
        } else {
            context.setRequestCharacterEncoding(encoding);
        }
    }

    @Override
    public String getResponseCharacterEncoding() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (String)doPrivileged("getResponseCharacterEncoding", null);
        }
        return context.getResponseCharacterEncoding();
    }

    @Override
    public void setResponseCharacterEncoding(String encoding) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("setResponseCharacterEncoding", null);
        } else {
            context.setResponseCharacterEncoding(encoding);
        }
    }

    @Override
    public String toString() {
        return super.toString() + "[context=" + context + ']';
    }


    /**
     * Use reflection to invoke the requested method. Cache the method object
     * to speed up the process
     *                   will be invoked
     * @param methodName The method to call.
     * @param params The arguments passed to the called method.
     */
    private <T> T doPrivileged(final String methodName, Object[] params){
        try {
            return invokeMethod(context, methodName, params);
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage());
        }
    }


    /**
     * Use reflection to invoke the requested method. Cache the method object
     * to speed up the process
     * @param appContext The AppliationContext object on which the method
     *                   will be invoked
     * @param methodName The method to call.
     * @param params The arguments passed to the called method.
     */
    private <T> T invokeMethod(ApplicationContext appContext,
                                final String methodName,
                                Object[] params)
        throws Throwable{

        try{
            Method method = objectCache.get(methodName);
            if (method == null){
                method = appContext.getClass()
                    .getMethod(methodName, classCache.get(methodName));
                objectCache.put(methodName, method);
            }

            return executeMethod(method,appContext,params);
        } catch (Exception ex){
            handleException(ex, methodName);
            return null;
        }
    }

    /**
     * Use reflection to invoke the requested method. Cache the method object
     * to speed up the process
     * @param methodName The method to call.
     * @param clazz The list of argument classes for the given method
     * @param params The arguments passed to the called method.
     */
    private <T> T doPrivileged(final String methodName,
                                final Class<?>[] clazz,
                                Object[] params){

        try{
            Method method = context.getClass().getMethod(methodName, clazz);
            return executeMethod(method,context,params);
        } catch (Exception ex){
            try{
                handleException(ex, methodName);
            }catch (Throwable t){
                throw new RuntimeException(t.getMessage());
            }
            return null;
        }
    }


    /**
     * Executes the method of the specified <code>ApplicationContext</code>
     * @param method The method object to be invoked.
     * @param context The AppliationContext object on which the method
     *                   will be invoked
     * @param params The arguments passed to the called method.
     */
    private <T> T executeMethod(final Method method,
                                 final ApplicationContext context,
                                 final Object[] params)
            throws IllegalAccessException,
                   InvocationTargetException {

        if (Globals.IS_SECURITY_ENABLED) {
            PrivilegedAction<T> action = () -> {
                try {
                    return (T) method.invoke(context, params);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new IllegalStateException(e);
                }
            };
            return AccessController.doPrivileged(action);
        }
        return (T) method.invoke(context, params);
    }


    /**
     * Throw the real exception.
     * @param ex The current exception
     */
    private void handleException(Exception ex, String methodName)
        throws Throwable {

        Throwable realException;

        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "ApplicationContextFacade." + methodName, ex);
        }

        if (ex instanceof PrivilegedActionException) {
            ex = ((PrivilegedActionException) ex).getException();
        }

        if (ex instanceof InvocationTargetException) {
            realException = ((InvocationTargetException) ex).getTargetException();
        } else {
            realException = ex;
        }

        throw realException;
    }
}
