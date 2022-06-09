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

package org.apache.catalina;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;


/**
 * A <b>Wrapper</b> is a Container that represents an individual servlet
 * definition from the deployment descriptor of the web application.  It
 * provides a convenient mechanism to use Interceptors that see every single
 * request to the servlet represented by this definition.
 * <p>
 * Implementations of Wrapper are responsible for managing the servlet life
 * cycle for their underlying servlet class, including calling init() and
 * destroy() at appropriate times, as well as respecting the existence of
 * the SingleThreadModel declaration on the servlet class itself.
 * <p>
 * The parent Container attached to a Wrapper will generally be an
 * implementation of Context, representing the servlet context (and
 * therefore the web application) within which this servlet executes.
 * <p>
 * Child Containers are not allowed on Wrapper implementations, so the
 * <code>addChild()</code> method should throw an
 * <code>IllegalArgumentException</code>.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3.6.1 $ $Date: 2008/04/17 18:37:01 $
 */

public interface Wrapper extends Container {


    // ------------------------------------------------------------- Properties


    /**
     * Return the available date/time for this servlet, in milliseconds since
     * the epoch.  If this date/time is in the future, any request for this
     * servlet will return an SC_SERVICE_UNAVAILABLE error.  If it is zero,
     * the servlet is currently available.  A value equal to Long.MAX_VALUE
     * is considered to mean that unavailability is permanent.
     */
    long getAvailable();


    /**
     * Set the available date/time for this servlet, in milliseconds since the
     * epoch.  If this date/time is in the future, any request for this servlet
     * will return an SC_SERVICE_UNAVAILABLE error.  A value equal to
     * Long.MAX_VALUE is considered to mean that unavailability is permanent.
     *
     * @param available The new available date/time
     */
    void setAvailable(long available);


    /**
     * Return the context-relative URI of the JSP file for this servlet.
     */
    String getJspFile();


    /**
     * Set the context-relative URI of the JSP file for this servlet.
     *
     * @param jspFile JSP file URI
     */
    void setJspFile(String jspFile);


    /**
     * Return the load-on-startup order value (negative value means
     * load on first call).
     */
    int getLoadOnStartup();


    /**
     * Set the load-on-startup order value (negative value means
     * load on first call).
     *
     * @param value New load-on-startup value
     */
    void setLoadOnStartup(int value);


    /**
     * Return the run-as identity for this servlet.
     */
    String getRunAs();


    /**
     * Set the run-as identity for this servlet.
     *
     * @param runAs New run-as identity value
     */
    void setRunAs(String runAs);


    /**
     * Return the fully qualified servlet class name for this servlet.
     */
    String getServletClassName();


    /**
     * Gets the name of the wrapped servler.
     */
    String getServletName();


    /**
     * Set the fully qualified servlet class name for this servlet.
     *
     * @param className Servlet class name
     */
    void setServletClassName(String className);


    /**
     * Sets the class object from which this servlet will be instantiated.
     *
     * @param servletClass the class object from which the servlet will be
     * instantiated
     */
    void setServletClass(Class <? extends Servlet> servletClass);


    /**
     * Gets the names of the methods supported by the underlying servlet.
     *
     * This is the same set of methods included in the Allow response header
     * in response to an OPTIONS request method processed by the underlying
     * servlet.
     *
     * @return Array of names of the methods supported by the underlying
     * servlet
     */
    String[] getServletMethods() throws ServletException;


    /**
     * Is this servlet currently unavailable?
     */
    boolean isUnavailable();


    /**
     * Sets the description of this servlet.
     */
    void setDescription(String description);


    /**
     * Gets the description of this servlet.
     */
    String getDescription();


    /**
     * Sets the multipart location
     */
    void setMultipartLocation(String location);


    /**
     * Gets the multipart location
     */
    String getMultipartLocation();


    /**
     * Sets the multipart max-file-size
     */
    void setMultipartMaxFileSize(long maxFileSize);


    /**
     * Gets the multipart max-file-size
     */
    long getMultipartMaxFileSize();


    /**
     * Sets the multipart max-request-size
     */
    void setMultipartMaxRequestSize(long maxRequestSize);


    /**
     * Gets the multipart max-request-Size
     */
    long getMultipartMaxRequestSize();


    /**
     * Sets the multipart file-size-threshold
     */
    void setMultipartFileSizeThreshold(int fileSizeThreshold);


    /**
     * Gets the multipart file-size-threshol
     */
    int getMultipartFileSizeThreshold();


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new servlet initialization parameter for this servlet.
     *
     * @param name Name of this initialization parameter to add
     * @param value Value of this initialization parameter to add
     */
    void addInitParameter(String name, String value);


    /**
     * Add a new listener interested in InstanceEvents.
     *
     * @param listener The new listener
     */
    void addInstanceListener(InstanceListener listener);


    /**
     * Add a mapping associated with the Wrapper.
     *
     * @param mapping The new wrapper mapping
     */
    void addMapping(String mapping);


    /**
     * Add a new security role reference record to the set of records for
     * this servlet.
     *
     * @param name Role name used within this servlet
     * @param link Role name used within the web application
     */
    void addSecurityReference(String name, String link);


    /**
     * Allocate an initialized instance of this Servlet that is ready to have
     * its <code>service()</code> method called.  If the servlet class does
     * not implement <code>SingleThreadModel</code>, the (only) initialized
     * instance may be returned immediately.  If the servlet class implements
     * <code>SingleThreadModel</code>, the Wrapper implementation must ensure
     * that this instance is not allocated again until it is deallocated by a
     * call to <code>deallocate()</code>.
     *
     * @throws ServletException if the servlet init() method threw
     *  an exception
     * @throws ServletException if a loading error occurs
     */
    Servlet allocate() throws ServletException;


    /**
     * Return this previously allocated servlet to the pool of available
     * instances.  If this servlet class does not implement SingleThreadModel,
     * no action is actually required.
     *
     * @param servlet The servlet to be returned
     *
     * @throws ServletException if a deallocation error occurs
     */
    void deallocate(Servlet servlet) throws ServletException;


    /**
     * Return the value for the specified initialization parameter name,
     * if any; otherwise return <code>null</code>.
     *
     * @param name Name of the requested initialization parameter
     */
    String findInitParameter(String name);


    /**
     * Return the names of all defined initialization parameters for this
     * servlet.
     */
    String[] findInitParameters();


    /**
     * Return the mappings associated with this wrapper.
     */
    String[] findMappings();


    /**
     * Return the security role link for the specified security role
     * reference name, if any; otherwise return <code>null</code>.
     *
     * @param name Security role reference used within this servlet
     */
    String findSecurityReference(String name);


    /**
     * Return the set of security role reference names associated with
     * this servlet, if any; otherwise return a zero-length array.
     */
    String[] findSecurityReferences();


    /**
     * Load and initialize an instance of this servlet, if there is not already
     * at least one initialized instance. This can be used, for example, to
     * load servlets that are marked in the deployment descriptor to be loaded
     * at server startup time.
     *
     * @throws ServletException if the servlet init() method threw an exception
     * @throws ServletException if some other loading problem occurs
     */
    void load() throws ServletException;


    /**
     * Remove the specified initialization parameter from this servlet.
     *
     * @param name Name of the initialization parameter to remove
     */
    void removeInitParameter(String name);


    /**
     * Remove a listener no longer interested in InstanceEvents.
     *
     * @param listener The listener to remove
     */
    void removeInstanceListener(InstanceListener listener);


    /**
     * Remove a mapping associated with the wrapper.
     *
     * @param mapping The pattern to remove
     */
    void removeMapping(String mapping);


    /**
     * Remove any security role reference for the specified role name.
     *
     * @param name Security role used within this servlet to be removed
     */
    void removeSecurityReference(String name);


    /**
     * Process an UnavailableException, marking this servlet as unavailable
     * for the specified amount of time.
     *
     * @param unavailable The exception that occurred, or <code>null</code>
     *  to mark this servlet as permanently unavailable
     */
    void unavailable(UnavailableException unavailable);


    /**
     * Unload all initialized instances of this servlet, after calling the
     * <code>destroy()</code> method for each instance.  This can be used,
     * for example, prior to shutting down the entire servlet engine, or
     * prior to reloading all of the classes from the Loader associated with
     * our Loader's repository.
     *
     * @throws ServletException if an unload error occurs
     */
    void unload() throws ServletException;


    /**
     * Configures the wrapped servlet as either supporting or not supporting
     * asynchronous operations.
     *
     * @param isAsyncSupported true if the wrapped servlet supports
     * asynchronous operations, false otherwise
     */
    void setIsAsyncSupported(boolean isAsyncSupported);


    /**
     * Checks if the wrapped servlet has been annotated or flagged in the
     * deployment descriptor as being able to support asynchronous operations.
     *
     * @return true if the wrapped servlet supports async operations, and
     * false otherwise
     */
    boolean isAsyncSupported();
}
