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

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.apache.catalina.Container;
import org.apache.catalina.ContainerServlet;
import org.apache.catalina.Context;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Loader;
import org.apache.catalina.LogFacade;
import org.apache.catalina.Wrapper;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.InstanceSupport;
import org.glassfish.web.valve.GlassFishValve;

import static com.sun.logging.LogCleanerUtil.neutralizeForLog;
import static java.text.MessageFormat.format;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.logging.Level.FINEST;
import static org.apache.catalina.InstanceEvent.EventType.AFTER_DESTROY_EVENT;
import static org.apache.catalina.InstanceEvent.EventType.AFTER_INIT_EVENT;
import static org.apache.catalina.InstanceEvent.EventType.AFTER_SERVICE_EVENT;
import static org.apache.catalina.InstanceEvent.EventType.BEFORE_DESTROY_EVENT;
import static org.apache.catalina.InstanceEvent.EventType.BEFORE_INIT_EVENT;
import static org.apache.catalina.InstanceEvent.EventType.BEFORE_SERVICE_EVENT;
import static org.apache.catalina.LogFacade.CANNOT_ALLOCATE_SERVLET_EXCEPTION;
import static org.apache.catalina.LogFacade.CANNOT_FIND_SERVLET_CLASS_EXCEPTION;
import static org.apache.catalina.LogFacade.ERROR_ALLOCATE_SERVLET_INSTANCE_EXCEPTION;
import static org.apache.catalina.LogFacade.ERROR_LOADING_INFO;
import static org.apache.catalina.LogFacade.PARENT_CONTAINER_MUST_BE_CONTEXT_EXCEPTION;
import static org.apache.catalina.LogFacade.WRAPPER_CONTAINER_NO_CHILD_EXCEPTION;
import static org.apache.catalina.core.Constants.JSP_SERVLET_CLASS;

/**
 * Standard implementation of the <b>Wrapper</b> interface that represents an individual servlet definition. No child
 * Containers are allowed, and the parent Container must be a Context.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 1.12.2.1 $ $Date: 2008/04/17 18:37:09 $
 */
public class StandardWrapper extends ContainerBase implements ServletConfig, Wrapper {

    private static final String[] DEFAULT_SERVLET_METHODS = new String[] { "GET", "HEAD", "POST" };

    // ----------------------------------------------------------- Constructors

    /**
     * Create a new StandardWrapper component with the default basic Valve.
     */
    public StandardWrapper() {
        super();
        swValve = new StandardWrapperValve();
        pipeline.setBasic(swValve);
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * The date and time at which this servlet will become available (in milliseconds since the epoch), or zero if the
     * servlet is available. If this value equals Long.MAX_VALUE, the unavailability of this servlet is considered
     * permanent.
     */
    private long available;

    /**
     * The broadcaster that sends j2ee notifications.
     */
    private NotificationBroadcasterSupport broadcaster;

    /**
     * The count of allocations that are currently active (even if they are for the same instance, as will be true on a
     * non-STM servlet).
     */
    private final AtomicInteger countAllocated = new AtomicInteger(0);

    /**
     * The debugging detail level for this component.
     */
    private int debug;

    /**
     * The facade associated with this wrapper.
     */
    private final StandardWrapperFacade facade = new StandardWrapperFacade(this);

    /**
     * The descriptive information string for this implementation.
     */
    private static final String info = "org.apache.catalina.core.StandardWrapper/1.0";

    /**
     * The (single) initialized instance of this servlet.
     */
    private volatile Servlet instance;

    /**
     * Flag that indicates if this instance has been initialized
     */
    protected volatile boolean instanceInitialized;

    /**
     * The support object for our instance listeners.
     */
    private final InstanceSupport instanceSupport = new InstanceSupport(this);

    /**
     * The context-relative URI of the JSP file for this servlet.
     */
    private String jspFile;

    /**
     * The load-on-startup order value (negative value means load on first call) for this servlet.
     */
    private int loadOnStartup = -1;

    /**
     * Mappings associated with the wrapper.
     */
    private final ArrayList<String> mappings = new ArrayList<>();

    /**
     * The initialization parameters for this servlet, keyed by parameter name.
     */
    private final Map<String, String> parameters = new HashMap<>();

    /**
     * The security role references for this servlet, keyed by role name used in the servlet. The corresponding value is the
     * role name of the web application itself.
     */
    private final HashMap<String, String> references = new HashMap<>();

    /**
     * The run-as identity for this servlet.
     */
    private String runAs;

    /**
     * The notification sequence number.
     */
    private long sequenceNumber;

    /**
     * The fully qualified servlet class name for this servlet.
     */
    private String servletClassName;

    /**
     * The class from which this servlet will be instantiated
     */
    private Class<? extends Servlet> servletClass;

    /**
     * Does this servlet implement the SingleThreadModel interface?
     */
    private volatile boolean singleThreadModel;

    /**
     * Are we unloading our servlet instance at the moment?
     */
    private boolean unloading;

    /**
     * Maximum number of STM instances.
     */
    private int maxInstances = 20;

    /**
     * Number of instances currently loaded for a STM servlet.
     */
    private int nInstances;

    /**
     * Stack containing the STM instances. TODO: remove
     */
    private Stack<Servlet> instancePool;

    /**
     * Wait time for servlet unload in ms.
     */
    protected long unloadDelay = 2000;

    /**
     * True if this StandardWrapper is for the JspServlet
     */
    private boolean isJspServlet;


    // To support jmx attributes
    private final StandardWrapperValve swValve;
    private long loadTime;
    private int classLoadTime;

    private String description;

    /**
     * Async support
     */
    private boolean isAsyncSupported;

    /**
     * Static class array used when the SecurityManager is turned on and <code>Servlet.init</code> is invoked.
     */
    private static Class<?>[] classType = new Class[] { ServletConfig.class };

    /**
     * Static class array used when the SecurityManager is turned on and <code>Servlet.service</code> is invoked.
     */
    private static Class<?>[] classTypeUsedInService = new Class[] { ServletRequest.class, ServletResponse.class };

    /**
     * File upload (multipart) support
     */
    private boolean multipartConfigured;
    private String multipartLocation;
    private long multipartMaxFileSize = -1L;
    private long multipartMaxRequestSize = -1L;
    private int multipartFileSizeThreshold = 10240; // 10K

    private boolean osgi;

    // ------------------------------------------------------------- Properties

    /**
     * Return the available date/time for this servlet, in milliseconds since the epoch. If this date/time is
     * Long.MAX_VALUE, it is considered to mean that unavailability is permanent and any request for this servlet will
     * return an SC_NOT_FOUND error. If this date/time is in the future, any request for this servlet will return an
     * SC_SERVICE_UNAVAILABLE error. If it is zero, the servlet is currently available.
     */
    @Override
    public long getAvailable() {
        return available;
    }

    /**
     * Set the available date/time for this servlet, in milliseconds since the epoch. If this date/time is Long.MAX_VALUE,
     * it is considered to mean that unavailability is permanent and any request for this servlet will return an
     * SC_NOT_FOUND error. If this date/time is in the future, any request for this servlet will return an
     * SC_SERVICE_UNAVAILABLE error.
     *
     * @param available The new available date/time
     */
    @Override
    public void setAvailable(long available) {
        long oldAvailable = this.available;
        if (available > System.currentTimeMillis()) {
            this.available = available;
        } else {
            this.available = 0L;
        }

        support.firePropertyChange("available", oldAvailable, this.available);
    }

    /**
     * Return the number of active allocations of this servlet, even if they are all for the same instance (as will be true
     * for servlets that do not implement <code>SingleThreadModel</code>.
     */
    public int getCountAllocated() {
        return countAllocated.get();
    }

    /**
     * Return the debugging detail level for this component.
     */
    @Override
    public int getDebug() {
        return debug;
    }

    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    @Override
    public void setDebug(int debug) {
        int oldDebug = this.debug;
        this.debug = debug;
        support.firePropertyChange("debug", oldDebug, (long) this.debug);
    }

    public String getEngineName() {
        return ((StandardContext) getParent()).getEngineName();
    }

    /**
     * Return descriptive information about this Container implementation and the corresponding version number, in the
     * format <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    @Override
    public String getInfo() {
        return info;
    }

    /**
     * Return the InstanceSupport object for this Wrapper instance.
     */
    public InstanceSupport getInstanceSupport() {
        return instanceSupport;
    }

    /**
     * Return the context-relative URI of the JSP file for this servlet.
     */
    @Override
    public String getJspFile() {
        return jspFile;
    }

    /**
     * Set the context-relative URI of the JSP file for this servlet.
     *
     * @param jspFile JSP file URI
     */
    @Override
    public void setJspFile(String jspFile) {
        String oldJspFile = this.jspFile;
        this.jspFile = jspFile;
        support.firePropertyChange("jspFile", oldJspFile, this.jspFile);

        // Each jsp-file needs to be represented by its own JspServlet and
        // corresponding JspMonitoring mbean, because it may be initialized
        // with its own init params
        isJspServlet = true;
    }

    /**
     * Return the load-on-startup order value (negative value means load on first call).
     */
    @Override
    public int getLoadOnStartup() {
        if (isJspServlet && loadOnStartup < 0) {
            /*
             * JspServlet must always be preloaded, because its instance is used during registerJMX (when registering the JSP
             * monitoring mbean)
             */
            return Integer.MAX_VALUE;
        }

        return loadOnStartup;
    }

    /**
     * Set the load-on-startup order value (negative value means load on first call).
     *
     * @param value New load-on-startup value
     */
    @Override
    public void setLoadOnStartup(int value) {
        int oldLoadOnStartup = this.loadOnStartup;
        this.loadOnStartup = value;
        support.firePropertyChange("loadOnStartup", oldLoadOnStartup, this.loadOnStartup);

    }

    /**
     * Set the load-on-startup order value from a (possibly null) string. Per the specification, any missing or non-numeric
     * value is converted to a zero, so that this servlet will still be loaded at startup time, but in an arbitrary order.
     *
     * @param value New load-on-startup value
     */
    public void setLoadOnStartupString(String value) {
        try {
            setLoadOnStartup(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            setLoadOnStartup(0);
        }
    }

    public String getLoadOnStartupString() {
        return Integer.toString(getLoadOnStartup());
    }

    /**
     * Sets the description of this servlet.
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the description of this servlet.
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Return maximum number of instances that will be allocated when a single thread model servlet is used.
     */
    public int getMaxInstances() {
        return maxInstances;
    }

    /**
     * Set the maximum number of instances that will be allocated when a single thread model servlet is used.
     *
     * @param maxInstances New value of maxInstances
     */
    public void setMaxInstances(int maxInstances) {
        int oldMaxInstances = this.maxInstances;
        this.maxInstances = maxInstances;
        support.firePropertyChange("maxInstances", oldMaxInstances, this.maxInstances);
    }

    /**
     * Set the parent Container of this Wrapper, but only if it is a Context.
     *
     * @param container Proposed parent Container
     */
    @Override
    public void setParent(Container container) {
        if (container != null && !(container instanceof Context)) {
            throw new IllegalArgumentException(rb.getString(PARENT_CONTAINER_MUST_BE_CONTEXT_EXCEPTION));
        }

        if (container instanceof StandardContext) {
            unloadDelay = ((StandardContext) container).getUnloadDelay();
            notifyContainerListeners = ((StandardContext) container).isNotifyContainerListeners();
        }

        super.setParent(container);
    }

    /**
     * Return the run-as identity for this servlet.
     */
    @Override
    public String getRunAs() {
        return runAs;
    }

    /**
     * Set the run-as identity for this servlet.
     *
     * @param runAs New run-as identity value
     */
    @Override
    public void setRunAs(String runAs) {
        String oldRunAs = this.runAs;
        this.runAs = runAs;
        support.firePropertyChange("runAs", oldRunAs, this.runAs);
    }

    /**
     * Marks the wrapped servlet as supporting async operations or not.
     *
     * @param isAsyncSupported true if the wrapped servlet supports async mode, false otherwise
     */
    @Override
    public void setIsAsyncSupported(boolean isAsyncSupported) {
        this.isAsyncSupported = isAsyncSupported;
    }

    /**
     * Checks if the wrapped servlet has been annotated or flagged in the deployment descriptor as being able to support
     * asynchronous operations.
     *
     * @return true if the wrapped servlet supports async operations, and false otherwise
     */
    @Override
    public boolean isAsyncSupported() {
        return isAsyncSupported;
    }

    /**
     * Return the fully qualified servlet class name for this servlet.
     */
    @Override
    public String getServletClassName() {
        return this.servletClassName;
    }

    /**
     * Set the fully qualified servlet class name for this servlet.
     *
     * @param className Servlet class name
     */
    @Override
    public void setServletClassName(String className) {
        if (className == null) {
            throw new NullPointerException("Null servlet class name");
        }

        if (servletClassName != null) {
            throw new IllegalStateException("Wrapper already initialized with servlet instance, " + "class, or name");
        }

        servletClassName = className;

        // oldServletClassName is null
        support.firePropertyChange("servletClassName", null, servletClassName);
        if (JSP_SERVLET_CLASS.equals(servletClassName)) {
            isJspServlet = true;
        }
    }

    /**
     * @return the servlet class, or null if the servlet class has not been loaded yet
     */
    public Class<? extends Servlet> getServletClass() {
        return servletClass;
    }

    /**
     * Sets the class object from which this servlet will be instantiated.
     *
     * @param clazz The class object from which this servlet will be instantiated
     */
    @Override
    public void setServletClass(Class<? extends Servlet> clazz) {
        if (clazz == null) {
            throw new NullPointerException("Null servlet class");
        }

        if (servletClass != null || servletClassName != null && !servletClassName.equals(clazz.getName())) {
            throw new IllegalStateException("Wrapper already initialized with servlet instance, " + "class, or name");
        }

        servletClass = clazz;
        servletClassName = clazz.getName();
        if (JSP_SERVLET_CLASS.equals(servletClassName)) {
            isJspServlet = true;
        }
    }

    /**
     * @return the servlet instance, or null if the servlet has not yet been instantiated
     */
    public Servlet getServlet() {
        return instance;
    }

    /**
     * Sets the servlet instance for this wrapper.
     *
     * @param instance the servlet instance
     */
    public void setServlet(Servlet instance) {
        if (instance == null) {
            throw new NullPointerException("Null servlet instance");
        }

        if (servletClassName != null) {
            throw new IllegalStateException("Wrapper already initialized with servlet instance, " + "class, or name");
        }

        this.instance = instance;
        servletClass = instance.getClass();
        servletClassName = servletClass.getName();
        if (JSP_SERVLET_CLASS.equals(servletClassName)) {
            isJspServlet = true;
        }
    }

    /**
     * Set the name of this servlet. This is an alias for the normal <code>Container.setName()</code> method, and
     * complements the <code>getServletName()</code> method required by the <code>ServletConfig</code> interface.
     *
     * @param name The new name of this servlet
     */
    public void setServletName(String name) {
        setName(name);
    }

    /**
     * Is this servlet currently unavailable?
     */
    @Override
    public boolean isUnavailable() {
        if (available == 0L) {
            return false;
        }

        if (available <= System.currentTimeMillis()) {
            available = 0L;
            return false;
        }

        return true;
    }

    /**
     * Gets the names of the methods supported by the underlying servlet.
     *
     * This is the same set of methods included in the Allow response header in response to an OPTIONS request method
     * processed by the underlying servlet.
     *
     * @return Array of names of the methods supported by the underlying servlet
     */
    @Override
    public String[] getServletMethods() throws ServletException {
        loadServletClass();

        if (!HttpServlet.class.isAssignableFrom(servletClass)) {
            return DEFAULT_SERVLET_METHODS;
        }

        HashSet<String> allow = new HashSet<>();
        allow.add("TRACE");
        allow.add("OPTIONS");

        Method[] methods = getAllDeclaredMethods(servletClass);
        for (int i = 0; methods != null && i < methods.length; i++) {
            Method m = methods[i];
            Class<?> params[] = m.getParameterTypes();

            if (!(params.length == 2 && params[0] == HttpServletRequest.class && params[1] == HttpServletResponse.class)) {
                continue;
            }

            if (m.getName().equals("doGet")) {
                allow.add("GET");
                allow.add("HEAD");
            } else if (m.getName().equals("doPost")) {
                allow.add("POST");
            } else if (m.getName().equals("doPut")) {
                allow.add("PUT");
            } else if (m.getName().equals("doDelete")) {
                allow.add("DELETE");
            }
        }

        String[] methodNames = new String[allow.size()];
        return allow.toArray(methodNames);
    }

    public boolean isMultipartConfigured() {
        return multipartConfigured;
    }

    /**
     * Sets the multipart location
     */
    @Override
    public void setMultipartLocation(String location) {
        multipartConfigured = true;
        multipartLocation = location;
    }

    /**
     * Gets the multipart location
     */
    @Override
    public String getMultipartLocation() {
        return multipartLocation;
    }

    /**
     * Sets the multipart max-file-size
     */
    @Override
    public void setMultipartMaxFileSize(long maxFileSize) {
        multipartConfigured = true;
        multipartMaxFileSize = maxFileSize;
    }

    /**
     * Gets the multipart max-file-size
     */
    @Override
    public long getMultipartMaxFileSize() {
        return multipartMaxFileSize;
    }

    /**
     * Sets the multipart max-request-size
     */
    @Override
    public void setMultipartMaxRequestSize(long maxRequestSize) {
        multipartConfigured = true;
        multipartMaxRequestSize = maxRequestSize;
    }

    /**
     * Gets the multipart max-request-Size
     */
    @Override
    public long getMultipartMaxRequestSize() {
        return multipartMaxRequestSize;
    }

    /**
     * Sets the multipart file-size-threshold
     */
    @Override
    public void setMultipartFileSizeThreshold(int fileSizeThreshold) {
        multipartConfigured = true;
        multipartFileSizeThreshold = fileSizeThreshold;
    }

    /**
     * Gets the multipart file-size-threshol
     */
    @Override
    public int getMultipartFileSizeThreshold() {
        return multipartFileSizeThreshold;
    }

    protected boolean isOSGi() {
        return osgi;
    }

    protected void setOSGi(boolean osgi) {
        this.osgi = osgi;
    }

    // --------------------------------------------------------- Public Methods

    @Override
    public synchronized void addValve(GlassFishValve valve) {
        /*
         * This exception should never be thrown in reality, because we never add any valves to a StandardWrapper. This
         * exception is added here as an alert mechanism only, should there ever be a need to add valves to a StandardWrapper in
         * the future. In that case, the optimization in StandardContextValve related to GlassFish 1343 will need to be
         * adjusted, by calling pipeline.getValves() and checking the pipeline's length to determine whether the basic valve may
         * be invoked directly. The optimization currently avoids a call to pipeline.getValves(), because it is expensive.
         */
        throw new UnsupportedOperationException("Adding valves to wrappers not supported");
    }

    /**
     * Extract the root cause from a servlet exception.
     *
     * @param e The servlet exception
     */
    public static Throwable getRootCause(ServletException e) {
        Throwable rootCause = e;
        Throwable rootCauseCheck;
        // Extra aggressive rootCause finding
        int loops = 0;
        do {
            loops++;
            rootCauseCheck = rootCause.getCause();
            if (rootCauseCheck != null) {
                rootCause = rootCauseCheck;
            }
        } while (rootCauseCheck != null && (loops < 20));
        return rootCause;
    }

    /**
     * Refuse to add a child Container, because Wrappers are the lowest level of the Container hierarchy.
     *
     * @param child Child container to be added
     */
    @Override
    public void addChild(Container child) {
        throw new IllegalStateException(rb.getString(WRAPPER_CONTAINER_NO_CHILD_EXCEPTION));
    }

    /**
     * Adds the initialization parameter with the given name and value to this servlet.
     *
     * @param name the name of the init parameter
     * @param value the value of the init parameter
     */
    @Override
    public void addInitParameter(String name, String value) {
        setInitParameter(name, value, true);
        if (notifyContainerListeners) {
            fireContainerEvent("addInitParameter", name);
        }
    }

    /**
     * Sets the init parameter with the given name and value on this servlet.
     *
     * @param name the init parameter name
     * @param value the init parameter value
     * @param override true if the given init param is supposed to override an existing init param with the same name, and
     * false otherwise
     *
     * @return true if the init parameter with the given name and value was set, false otherwise
     */
    public boolean setInitParameter(String name, String value, boolean override) {
        if (name == null || value == null) {
            throw new IllegalArgumentException("Null servlet init parameter name or value");
        }

        synchronized (parameters) {
            if (override || !parameters.containsKey(name)) {
                parameters.put(name, value);
                return true;
            }

            return false;
        }
    }

    /**
     * Sets the initialization parameters contained in the given map on this servlet.
     *
     * @param initParameters the map with the init params to set
     *
     * @return the (possibly empty) Set of initialization parameter names that are in conflict
     */
    public Set<String> setInitParameters(Map<String, String> initParameters) {
        if (initParameters == null) {
            throw new IllegalArgumentException("Null init parameters");
        }

        synchronized (parameters) {
            Set<String> conflicts = null;
            for (Map.Entry<String, String> e : initParameters.entrySet()) {
                if (e.getKey() == null || e.getValue() == null) {
                    throw new IllegalArgumentException("Null parameter name or value");
                }

                if (parameters.containsKey(e.getKey())) {
                    if (conflicts == null) {
                        conflicts = new HashSet<>();
                    }
                    conflicts.add(e.getKey());
                }
            }

            if (conflicts != null) {
                return conflicts;
            }

            for (Map.Entry<String, String> e : initParameters.entrySet()) {
                setInitParameter(e.getKey(), e.getValue(), true);
            }

            return emptySet();
        }
    }

    /**
     * Add a new listener interested in InstanceEvents.
     *
     * @param listener The new listener
     */
    @Override
    public void addInstanceListener(InstanceListener listener) {
        instanceSupport.addInstanceListener(listener);
    }

    /**
     * Add a mapping associated with the Wrapper.
     *
     * @param mapping The new wrapper mapping
     */
    @Override
    public void addMapping(String mapping) {
        synchronized (mappings) {
            mappings.add(mapping);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addMapping", mapping);
        }
    }

    public Collection<String> getMappings() {
        synchronized (mappings) {
            return unmodifiableList(mappings);
        }
    }

    /**
     * Add a new security role reference record to the set of records for this servlet.
     *
     * @param name Role name used within this servlet
     * @param link Role name used within the web application
     */
    @Override
    public void addSecurityReference(String name, String link) {
        synchronized (references) {
            references.put(name, link);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addSecurityReference", name);
        }
    }

    /**
     * Allocate an initialized instance of this Servlet that is ready to have its <code>service()</code> method called. If
     * the servlet class does not implement <code>SingleThreadModel</code>, the (only) initialized instance may be returned
     * immediately. If the servlet class implements <code>SingleThreadModel</code>, the Wrapper implementation must ensure
     * that this instance is not allocated again until it is deallocated by a call to <code>deallocate()</code>.
     *
     * @exception ServletException if the servlet init() method threw an exception
     * @exception ServletException if a loading error occurs
     */
    @Override
    public synchronized Servlet allocate() throws ServletException {
        // If we are currently unloading this servlet, throw an exception
        if (unloading) {
            throw new ServletException(format(rb.getString(CANNOT_ALLOCATE_SERVLET_EXCEPTION), getName()));
        }

        // If not SingleThreadedModel, return the same instance every time
        if (!singleThreadModel) {

            // Load and initialize our instance if necessary
            if (instance == null) {
                // No instance. Instantiate and initialize
                try {
                    log.log(FINEST, "Allocating non-STM instance");
                    instance = loadServlet();
                    initServlet(instance);
                } catch (ServletException e) {
                    throw e;
                } catch (Throwable e) {
                    throw new ServletException(rb.getString(ERROR_ALLOCATE_SERVLET_INSTANCE_EXCEPTION), e);
                }
            } else if (!instanceInitialized) {
                /*
                 * Instance not yet initialized. This is the case when the instance was registered via ServletContext#addServlet
                 */
                initServlet(instance);
            }

            if (!singleThreadModel) {
                log.log(FINEST, "Returning non-STM instance");
                countAllocated.incrementAndGet();
                return (instance);
            }
        }

        synchronized (instancePool) {
            while (countAllocated.get() >= nInstances) {
                // Allocate a new instance if possible, or else wait
                if (nInstances < maxInstances) {
                    try {
                        Servlet servlet = loadServlet();
                        initServlet(servlet);
                        instancePool.push(servlet);
                        nInstances++;
                    } catch (ServletException e) {
                        throw e;
                    } catch (Throwable e) {
                        throw new ServletException(rb.getString(ERROR_ALLOCATE_SERVLET_INSTANCE_EXCEPTION), e);
                    }
                } else {
                    try {
                        instancePool.wait();
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
            }
            if (log.isLoggable(FINEST)) {
                log.log(FINEST, "Returning allocated STM instance");
            }
            countAllocated.incrementAndGet();
            return instancePool.pop();
        }
    }

    /**
     * Return this previously allocated servlet to the pool of available instances. If this servlet class does not implement
     * SingleThreadModel, no action is actually required.
     *
     * @param servlet The servlet to be returned
     *
     * @exception ServletException if a deallocation error occurs
     */
    @Override
    public void deallocate(Servlet servlet) throws ServletException {
        // If not SingleThreadModel, no action is required
        if (!singleThreadModel) {
            countAllocated.decrementAndGet();
            return;
        }

        // Unlock and free this instance
        synchronized (instancePool) {
            countAllocated.decrementAndGet();
            instancePool.push(servlet);
            instancePool.notify();
        }
    }

    /**
     * Return the value for the specified initialization parameter name, if any; otherwise return <code>null</code>.
     *
     * @param name Name of the requested initialization parameter
     */
    @Override
    public String findInitParameter(String name) {
        synchronized (parameters) {
            return parameters.get(name);
        }
    }

    /**
     * Return the names of all defined initialization parameters for this servlet.
     */
    @Override
    public String[] findInitParameters() {
        synchronized (parameters) {
            String results[] = new String[parameters.size()];
            return parameters.keySet().toArray(results);
        }
    }

    /**
     * Return the mappings associated with this wrapper.
     */
    @Override
    public String[] findMappings() {
        synchronized (mappings) {
            return mappings.toArray(new String[mappings.size()]);
        }
    }

    /**
     * Return the security role link for the specified security role reference name, if any; otherwise return
     * <code>null</code>.
     *
     * @param name Security role reference used within this servlet
     */
    @Override
    public String findSecurityReference(String name) {
        synchronized (references) {
            return references.get(name);
        }
    }

    /**
     * Return the set of security role reference names associated with this servlet, if any; otherwise return a zero-length
     * array.
     */
    @Override
    public String[] findSecurityReferences() {
        synchronized (references) {
            String results[] = new String[references.size()];
            return references.keySet().toArray(results);
        }
    }

    /**
     * FIXME: Fooling introspection ...
     */
    public Wrapper findMappingObject() {
        return (Wrapper) getMappingObject();
    }

    /**
     * Loads and initializes an instance of the servlet, if there is not already at least one initialized instance. This can
     * be used, for example, to load servlets that are marked in the deployment descriptor to be loaded at server startup
     * time.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>: Servlets whose classnames begin with <code>org.apache.catalina.</code> (so-called
     * "container" servlets) are loaded by the same classloader that loaded this class, rather than the classloader for the
     * current web application. This gives such classes access to Catalina internals, which are prevented for classes loaded
     * for web applications.
     *
     * @throws ServletException if the servlet init() method threw an exception
     * @throws ServletException if some other loading problem occurs
     */
    @Override
    public synchronized void load() throws ServletException {
        instance = loadServlet();
        initServlet(instance);
    }


    /**
     * Creates an instance of the servlet, if there is not already at least one initialized instance.
     */
    private synchronized Servlet loadServlet() throws ServletException {
        // Nothing to do if we already have an instance or an instance pool
        if (!singleThreadModel && (instance != null)) {
            return instance;
        }

        long t1 = System.currentTimeMillis();

        loadServletClass();

        // Instantiate the servlet class
        Servlet servlet = null;
        try {
            servlet = ((StandardContext) getParent()).createServletInstance(servletClass);
        } catch (ClassCastException e) {
            unavailable(null);
            // Restore the context ClassLoader
            String msg = format(rb.getString(LogFacade.CLASS_IS_NOT_SERVLET_EXCEPTION), servletClass.getName());
            throw new ServletException(msg, e);
        } catch (Throwable e) {
            unavailable(null);
            // Restore the context ClassLoader
            String msg = format(rb.getString(LogFacade.ERROR_INSTANTIATE_SERVLET_CLASS_EXCEPTION), servletClass.getName());
            throw new ServletException(msg, e);
        }

        // Check if loading the servlet in this web application should be allowed
        if (!isServletAllowed(servlet)) {
            String msg = format(rb.getString(LogFacade.PRIVILEGED_SERVLET_CANNOT_BE_LOADED_EXCEPTION),
                    servletClass.getName());
            throw new SecurityException(msg);
        }

        // Special handling for ContainerServlet instances
        if ((servlet instanceof ContainerServlet)
                && (isContainerProvidedServlet(servletClass.getName()) || ((Context) getParent()).getPrivileged())) {
            ((ContainerServlet) servlet).setWrapper(this);
        }

        classLoadTime = (int) (System.currentTimeMillis() - t1);

        // Register our newly initialized instance
        if (notifyContainerListeners) {
            fireContainerEvent("load", this);
        }

        loadTime = System.currentTimeMillis() - t1;

        return servlet;
    }

    /*
     * Loads the servlet class
     */
    private synchronized void loadServletClass() throws ServletException {
        if (servletClass != null) {
            return;
        }

        // If this "servlet" is really a JSP file, get the right class.
        final String actualClassName = resolveServletClassNameAndParameters();
        // Complain if no servlet class has been specified
        if (actualClassName == null) {
            unavailable(null);
            String msg = format(rb.getString(LogFacade.NO_SERVLET_BE_SPECIFIED_EXCEPTION), getName());
            throw new ServletException(msg);
        }

        // Acquire an instance of the class loader to be used
        Loader currentLoader = getLoader();
        if (currentLoader == null) {
            unavailable(null);
            String msg = format(rb.getString(LogFacade.CANNOT_FIND_LOADER_EXCEPTION), getName());
            throw new ServletException(msg);
        }


        // Special case class loader for a container provided servlet
        final ClassLoader classLoader;
        if (isContainerProvidedServlet(actualClassName) && !((Context) getParent()).getPrivileged()) {
            // If it is a priviledged context - using its own
            // class loader will work, since it's a child of the container
            // loader
            classLoader = this.getClass().getClassLoader();
        } else {
            classLoader = currentLoader.getClassLoader();
        }

        // Load the specified servlet class from the appropriate class loader
        Class<?> clazz = null;
        try {
            clazz = classLoader == null ? Class.forName(actualClassName) : classLoader.loadClass(actualClassName);
        } catch (ClassNotFoundException e) {
            unavailable(null);
            String msgErrorLoadingInfo = format(rb.getString(ERROR_LOADING_INFO),
                new Object[] {classLoader, actualClassName});
            getServletContext().log(msgErrorLoadingInfo, e);
            String msg = format(rb.getString(CANNOT_FIND_SERVLET_CLASS_EXCEPTION), actualClassName);
            throw new ServletException(msg, e);
        }

        if (clazz == null) {
            unavailable(null);
            throw new ServletException(format(rb.getString(CANNOT_FIND_SERVLET_CLASS_EXCEPTION), actualClassName));
        }

        servletClass = castToServletClass(clazz);
    }

    private String resolveServletClassNameAndParameters() {
        if (servletClassName != null || jspFile == null) {
            return servletClassName;
        }

        Wrapper jspWrapper = (Wrapper) ((Context) getParent()).findChild(Constants.JSP_SERVLET_NAME);
        if (jspWrapper == null) {
            return servletClassName;
        }
        // Merge init parameters
        String[] paramNames = jspWrapper.findInitParameters();
        for (String paramName : paramNames) {
            parameters.computeIfAbsent(paramName, jspWrapper::findInitParameter);
        }
        return jspWrapper.getServletClassName();
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Servlet> castToServletClass(Class<?> clazz) {
        return (Class<? extends Servlet>) clazz;
    }

    /**
     * Initializes the given servlet instance, by calling its init method.
     */
    private void initServlet(Servlet servlet) throws ServletException {
        if (instanceInitialized && !singleThreadModel) {
            // Servlet has already been initialized
            return;
        }

        try {
            instanceSupport.fireInstanceEvent(BEFORE_INIT_EVENT, servlet);
            servlet.init(facade);

            instanceInitialized = true;

            // Invoke jspInit on JSP pages
            if ((loadOnStartup >= 0) && (jspFile != null)) {
                // Invoking jspInit
                DummyRequest req = new DummyRequest();
                req.setServletPath(jspFile);
                req.setQueryString("jsp_precompile=true");

                String allowedMethods = parameters.get("httpMethods");
                if (allowedMethods != null && allowedMethods.length() > 0) {
                    String[] s = allowedMethods.split(",");
                    if (s.length > 0) {
                        req.setMethod(s[0].trim());
                    }
                }

                DummyResponse res = new DummyResponse();

                servlet.service(req, res);
            }
            instanceSupport.fireInstanceEvent(AFTER_INIT_EVENT, servlet);

        } catch (UnavailableException f) {
            instanceSupport.fireInstanceEvent(AFTER_INIT_EVENT, servlet, f);
            unavailable(f);
            throw f;

        } catch (ServletException f) {
            instanceSupport.fireInstanceEvent(AFTER_INIT_EVENT, servlet, f);
            // If the servlet wanted to be unavailable it would have
            // said so, so do not call unavailable(null).
            throw f;

        } catch (Throwable f) {
            getServletContext().log("StandardWrapper.Throwable", f);
            instanceSupport.fireInstanceEvent(AFTER_INIT_EVENT, servlet, f);
            // If the servlet wanted to be unavailable it would have
            // said so, so do not call unavailable(null).
            String msg = format(rb.getString(LogFacade.SERVLET_INIT_EXCEPTION), getName());
            throw new ServletException(msg, f);
        }
    }

    void service(ServletRequest request, ServletResponse response, Servlet servlet) throws IOException, ServletException {
        InstanceSupport supp = getInstanceSupport();

        try {
            supp.fireInstanceEvent(BEFORE_SERVICE_EVENT, servlet, request, response);
            if (!isAsyncSupported()) {
                RequestFacadeHelper reqFacHelper = RequestFacadeHelper.getInstance(request);
                if (reqFacHelper != null) {
                    reqFacHelper.disableAsyncSupport();
                }
            }

            servlet.service(request, response);

            supp.fireInstanceEvent(AFTER_SERVICE_EVENT, servlet, request, response);
        } catch (IOException | ServletException | RuntimeException | Error e) {
            log.log(Level.FINE, "Seen throwable, firing instance event and rethrowing ...", e);
            // Set response status before firing event, see IT 10022
            if (response instanceof HttpServletResponse) {
                ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            supp.fireInstanceEvent(AFTER_SERVICE_EVENT, servlet, request, response, e);
            throw e;
        } catch (Throwable e) {
            log.log(Level.FINE, "Seen throwable, firing instance event and throwing a servlet exception ...", e);
            // Set response status before firing event, see IT 10022
            ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            supp.fireInstanceEvent(AFTER_SERVICE_EVENT, servlet, request, response, e);
            throw new ServletException(rb.getString(LogFacade.SERVLET_EXECUTION_EXCEPTION), e);
        }

    }

    /**
     * Remove the specified initialization parameter from this servlet.
     *
     * @param name Name of the initialization parameter to remove
     */
    @Override
    public void removeInitParameter(String name) {
        synchronized (parameters) {
            parameters.remove(name);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("removeInitParameter", name);
        }
    }

    /**
     * Remove a listener no longer interested in InstanceEvents.
     *
     * @param listener The listener to remove
     */
    @Override
    public void removeInstanceListener(InstanceListener listener) {
        instanceSupport.removeInstanceListener(listener);
    }

    /**
     * Remove a mapping associated with the wrapper.
     *
     * @param mapping The pattern to remove
     */
    @Override
    public void removeMapping(String mapping) {
        synchronized (mappings) {
            mappings.remove(mapping);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("removeMapping", mapping);
        }
    }

    /**
     * Remove any security role reference for the specified role name.
     *
     * @param name Security role used within this servlet to be removed
     */
    @Override
    public void removeSecurityReference(String name) {

        synchronized (references) {
            references.remove(name);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("removeSecurityReference", name);
        }
    }

    /**
     * Return a String representation of this component.
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        if (getParent() != null) {
            sb.append(getParent().toString());
            sb.append(".");
        }
        sb.append("StandardWrapper[");
        sb.append(getName());
        sb.append("]");
        return (sb.toString());

    }

    /**
     * Process an UnavailableException, marking this servlet as unavailable for the specified amount of time.
     *
     * @param unavailable The exception that occurred, or <code>null</code> to mark this servlet as permanently unavailable
     */
    @Override
    public void unavailable(UnavailableException unavailable) {
        String msg = format(rb.getString(LogFacade.MARK_SERVLET_UNAVAILABLE), neutralizeForLog(getName()));
        getServletContext().log(msg);
        if (unavailable == null) {
            setAvailable(Long.MAX_VALUE);
        } else if (unavailable.isPermanent()) {
            setAvailable(Long.MAX_VALUE);
        } else {
            int unavailableSeconds = unavailable.getUnavailableSeconds();
            if (unavailableSeconds <= 0) {
                unavailableSeconds = 60; // Arbitrary default
            }
            setAvailable(System.currentTimeMillis() + (unavailableSeconds * 1000L));
        }

    }

    /**
     * Unload all initialized instances of this servlet, after calling the <code>destroy()</code> method for each instance.
     * This can be used, for example, prior to shutting down the entire servlet engine, or prior to reloading all of the
     * classes from the Loader associated with our Loader's repository.
     *
     * @exception ServletException if an exception is thrown by the destroy() method
     */
    @Override
    public synchronized void unload() throws ServletException {

        // Nothing to do if we have never loaded the instance
        if (!singleThreadModel && instance == null) {
            return;
        }
        unloading = true;

        // Loaf a while if the current instance is allocated
        // (possibly more than once if non-STM)
        if (countAllocated.get() > 0) {
            int nRetries = 0;
            long delay = unloadDelay / 20;
            while (nRetries < 21 && countAllocated.get() > 0) {
                if ((nRetries % 10) == 0) {
                    if (log.isLoggable(Level.FINE)) {
                        log.log(Level.FINE, LogFacade.WAITING_INSTANCE_BE_DEALLOCATED,
                            new Object[] {countAllocated.toString(), instance.getClass().getName()});
                    }
                }
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    // Ignore
                }
                nRetries++;
            }
        }

        ClassLoader oldCtxClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader classLoader = instance.getClass().getClassLoader();

        // Call the servlet destroy() method
        try {
            instanceSupport.fireInstanceEvent(BEFORE_DESTROY_EVENT, instance);

            Thread.currentThread().setContextClassLoader(classLoader);
            instance.destroy();

            instanceSupport.fireInstanceEvent(AFTER_DESTROY_EVENT, instance);
        } catch (Throwable t) {
            instanceSupport.fireInstanceEvent(AFTER_DESTROY_EVENT, instance, t);
            instance = null;
            instancePool = null;
            nInstances = 0;
            if (notifyContainerListeners) {
                fireContainerEvent("unload", this);
            }
            unloading = false;
            String msg = format(rb.getString(LogFacade.DESTROY_SERVLET_EXCEPTION), getName());
            throw new ServletException(msg, t);
        } finally {
            // restore the context ClassLoader
            Thread.currentThread().setContextClassLoader(oldCtxClassLoader);
        }

        // Deregister the destroyed instance
        instance = null;

        if (singleThreadModel && (instancePool != null)) {
            try {
                Thread.currentThread().setContextClassLoader(classLoader);
                while (!instancePool.isEmpty()) {
                    instancePool.pop().destroy();
                }
            } catch (Throwable t) {
                instancePool = null;
                nInstances = 0;
                unloading = false;
                if (notifyContainerListeners) {
                    fireContainerEvent("unload", this);
                }
                String msg = format(rb.getString(LogFacade.DESTROY_SERVLET_EXCEPTION), getName());
                throw new ServletException(msg, t);
            } finally {
                // restore the context ClassLoader
                Thread.currentThread().setContextClassLoader(oldCtxClassLoader);
            }
            instancePool = null;
            nInstances = 0;
        }

        singleThreadModel = false;

        unloading = false;

        if (notifyContainerListeners) {
            fireContainerEvent("unload", this);
        }
    }

    // -------------------------------------------------- ServletConfig Methods

    /**
     * Return the initialization parameter value for the specified name, if any; otherwise return <code>null</code>.
     *
     * @param name Name of the initialization parameter to retrieve
     */
    @Override
    public String getInitParameter(String name) {
        return findInitParameter(name);
    }

    public Map<String, String> getInitParameters() {
        synchronized (parameters) {
            return Collections.unmodifiableMap(parameters);
        }
    }

    /**
     * Return the set of initialization parameter names defined for this servlet. If none are defined, an empty Enumeration
     * is returned.
     */
    @Override
    public Enumeration<String> getInitParameterNames() {
        synchronized (parameters) {
            return new Enumerator<>(parameters.keySet());
        }
    }

    /**
     * Return the servlet context with which this servlet is associated.
     */
    @Override
    public ServletContext getServletContext() {
        if (parent == null) {
            return null;
        }

        if (!(parent instanceof Context)) {
            return null;
        }

        return ((Context) parent).getServletContext();
    }

    /**
     * Return the name of this servlet.
     */
    @Override
    public String getServletName() {
        return getName();
    }

    public long getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(long loadTime) {
        this.loadTime = loadTime;
    }

    public int getClassLoadTime() {
        return classLoadTime;
    }

    // -------------------------------------------------------- Package Methods

    // -------------------------------------------------------- Private Methods

    /**
     * Add a default Mapper implementation if none have been configured explicitly.
     *
     * @param mapperClass Java class name of the default Mapper
     */
    protected void addDefaultMapper(String mapperClass) {
        // No need for a default Mapper on a Wrapper
    }

    /**
     * Return <code>true</code> if the specified class name represents a container provided servlet class that should be
     * loaded by the server class loader.
     *
     * @param classname Name of the class to be checked
     */
    private boolean isContainerProvidedServlet(String classname) {
        if (classname.startsWith("org.apache.catalina.")) {
            return true;
        }

        try {
            Class<?> clazz = this.getClass().getClassLoader().loadClass(classname);
            return ContainerServlet.class.isAssignableFrom(clazz);
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Return <code>true</code> if loading this servlet is allowed.
     */
    private boolean isServletAllowed(Object servlet) {
        if (servlet instanceof ContainerServlet) {
            return ((Context) getParent()).getPrivileged() || servlet.getClass().getName().equals("org.apache.catalina.servlets.InvokerServlet");
        }

        return true;
    }

    /**
     * Log the abbreviated name of this Container for logging messages.
     */
    @Override
    protected String logName() {
        StringBuilder sb = new StringBuilder("StandardWrapper[");
        if (getParent() != null) {
            sb.append(getParent().getName());
        } else {
            sb.append("null");
        }

        sb.append(':');
        sb.append(getName());
        sb.append(']');

        return (sb.toString());
    }

    private Method[] getAllDeclaredMethods(Class<?> c) {
        if (c.equals(jakarta.servlet.http.HttpServlet.class)) {
            return null;
        }

        Method[] parentMethods = getAllDeclaredMethods(c.getSuperclass());

        Method[] thisMethods = c.getDeclaredMethods();
        if (thisMethods.length == 0) {
            return parentMethods;
        }

        if ((parentMethods != null) && (parentMethods.length > 0)) {
            Method[] allMethods = new Method[parentMethods.length + thisMethods.length];
            System.arraycopy(parentMethods, 0, allMethods, 0, parentMethods.length);
            System.arraycopy(thisMethods, 0, allMethods, parentMethods.length, thisMethods.length);

            thisMethods = allMethods;
        }

        return thisMethods;
    }

    // ------------------------------------------------------ Lifecycle Methods

    /**
     * Start this component, pre-loading the servlet if the load-on-startup value is set appropriately.
     *
     * @exception LifecycleException if a fatal error occurs during startup
     */
    @Override
    public void start() throws LifecycleException {

        // Send j2ee.state.starting notification
        if (this.getObjectName() != null) {
            Notification notification = new Notification("j2ee.state.starting", this, sequenceNumber++);
            sendNotification(notification);
        }

        // Start up this component
        super.start();

        if (oname != null) {
            registerJMX((StandardContext) getParent());
        }

        // Load and initialize an instance of this servlet if requested
        // MOVED TO StandardContext START() METHOD

        setAvailable(0L);

        // Send j2ee.state.running notification
        if (this.getObjectName() != null) {
            Notification notification = new Notification("j2ee.state.running", this, sequenceNumber++);
            sendNotification(notification);
        }

    }

    /**
     * Stop this component, gracefully shutting down the servlet if it has been initialized.
     *
     * @exception LifecycleException if a fatal error occurs during shutdown
     */
    @Override
    public void stop() throws LifecycleException {
        setAvailable(Long.MAX_VALUE);

        // Send j2ee.state.stopping notification
        if (this.getObjectName() != null) {
            Notification notification = new Notification("j2ee.state.stopping", this, sequenceNumber++);
            sendNotification(notification);
        }

        // Shut down our servlet instance (if it has been initialized)
        try {
            unload();
        } catch (ServletException e) {
            String msg = format(rb.getString(LogFacade.SERVLET_UNLOAD_EXCEPTION), neutralizeForLog(getName()));
            getServletContext().log(msg, e);
        }

        // Shut down this component
        super.stop();

        // Send j2ee.state.stoppped notification
        if (this.getObjectName() != null) {
            Notification notification = new Notification("j2ee.state.stopped", this, sequenceNumber++);
            sendNotification(notification);
        }

        if (oname != null) {

            // Send j2ee.object.deleted notification
            Notification notification = new Notification("j2ee.object.deleted", this, sequenceNumber++);
            sendNotification(notification);
        }

    }

    protected void registerJMX(StandardContext ctx) {
        String parentName = ctx.getEncodedPath();
        parentName = ("".equals(parentName)) ? "/" : parentName;

        String hostName = ctx.getParent().getName();
        hostName = (hostName == null) ? "DEFAULT" : hostName;

        String domain = ctx.getDomain();

        String webMod = "//" + hostName + parentName;
        String onameStr = domain + ":j2eeType=Servlet,name=" + getName() + ",WebModule=" + webMod + ",J2EEApplication="
                + ctx.getEEApplication() + ",J2EEServer=" + ctx.getEEServer();
        if (isOSGi()) {
            onameStr += ",osgi=true";
        }

        try {
            oname = new ObjectName(onameStr);
            controller = oname;

            // Send j2ee.object.created notification
            if (this.getObjectName() != null) {
                Notification notification = new Notification("j2ee.object.created", this, sequenceNumber++);
                sendNotification(notification);
            }
        } catch (Exception ex) {
            if (log.isLoggable(Level.INFO)) {
                log.log(Level.INFO, "Error registering servlet with jmx " + this, ex);
            }
        }
    }

    public void sendNotification(Notification notification) {
        if (broadcaster == null) {
            broadcaster = ((StandardEngine) getParent().getParent().getParent()).getService().getBroadcaster();
        }

        if (broadcaster != null) {
            broadcaster.sendNotification(notification);
        }

        return;
    }

    // ------------------------------------------------------------- Attributes

    public boolean isEventProvider() {
        return false;
    }

    public boolean isStateManageable() {
        return false;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

}
