/*
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

package org.glassfish.embeddable.web;

import java.io.File;
import java.util.Collection;
import java.util.logging.Level;

import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.web.config.WebContainerConfig;

/**
 * Class representing an embedded web container, which supports the
 * programmatic creation of different types of web protocol listeners
 * and virtual servers, and the registration of static and dynamic
 * web resources into the URI namespace.
 *
 * WebContainer service can be accessed using GlassFish instance.
 *
 * <p> Usage example:
 *
 * <pre>
 *      // Create and start GlassFish
 *      GlassFish glassfish = GlassFishRuntime.bootstrap().newGlassFish();
 *      glassfish.start();
 *
 *      // Access WebContainer
 *      WebContainer container = glassfish.getService(WebContainer.class);
 *
 *      // Create and add {@link WebListener}
 *      // By default, when GlassFish Embedded Server starts, no web listener is enabled
 *      WebListener listener = container.createWebListener("listener-1", HttpListener.class);
 *      listener.setPort(8080);
 *      container.addWebListener(listener);
 *
 *      // Create and register web resources {@link Context}.
 *      File docroot = new File(path_to_web_resources);
 *      Context context = container.createContext(docroot);
 *      container.addContext(context, "contextroot_to_register");
 *
 *      // Create and add {@link VirtualServer}
 *      // By default, when GlassFish Embedded Server starts,
 *      // a virtual server named server starts automatically.
 *      VirtualServer virtualServer = (VirtualServer)
 *          container.createVirtualServer("embedded-server", new File(docroot_of_VirtualServer));
 *      VirtualServerConfig config = new VirtualServerConfig();
 *      config.setHostNames("localhost");
 *      virtualServer.setConfig(config);
 *      container.addVirtualServer(virtualServer);
 * </pre>
 */

public interface WebContainer {

    /**
     * Sets the embedded configuration for this embedded instance.
     * Such configuration will always override any xml based
     * configuration.
     *
     * @param config the embedded instance configuration
     */
    void setConfiguration(WebContainerConfig config);

    /**
     * Creates a <code>Context</code> and configures it with the given
     * docroot and classloader.
     *
     * <p>The classloader of the class on which this method is called
     * will be used.
     *
     * <p>In order to access the new <code>Context</code> or any of its
     * resources, the <code>Context</code> must be registered with a
     * <code>VirtualServer</code> that has been started using either
     * WebContainer#addContext or VirtualServer#addContext method.
     *
     * @param docRoot the docroot of the <code>Context</code>
     *
     * @return the new <code>Context</code>
     *
     * @see VirtualServer#addContext
     */
    Context createContext(File docRoot);

    /**
     * Creates a <code>Context</code> and configures it with the given
     * docroot and classloader.
     *
     * <p>The given classloader will be set as the thread's context
     * classloader whenever the new <code>Context</code> or any of its
     * resources are asked to process a request.
     * If a <code>null</code> classloader is passed, the classloader of the
     * class on which this method is called will be used.
     *
     * <p>In order to access the new <code>Context</code> or any of its
     * resources, the <code>Context</code> must be registered with a
     * <code>VirtualServer</code> that has been started using either
     * WebContainer#addContext or VirtualServer#addContext method.
     *
     * @param docRoot the docroot of the <code>Context</code>
     * @param classLoader the classloader of the <code>Context</code>
     *
     * @return the new <code>Context</code>
     *
     * @see VirtualServer#addContext
     */
    Context createContext(File docRoot, ClassLoader classLoader);

    /**
     * Creates a <code>Context</code>, configures it with the given
     * docroot and classloader, and registers it with all
     * <code>VirtualServer</code>.
     *
     * <p>The given classloader will be set as the thread's context
     * classloader whenever the new <code>Context</code> or any of its
     * resources are asked to process a request.
     * If a <code>null</code> classloader is passed, the classloader of the
     * class on which this method is called will be used.
     *
     * @param docRoot the docroot of the <code>Context</code>
     * @param contextRoot the contextroot at which to register
     * @param classLoader the classloader of the <code>Context</code>
     *
     * @return the new <code>Context</code>
     */
    Context createContext(File docRoot, String contextRoot,
                                 ClassLoader classLoader);

    /**
     * Registers the given <code>Context</code> with all <code>VirtualServer</code>
     * at the given context root.
     *
     * <p>If <code>VirtualServer</code> has already been started, the
     * given <code>context</code> will be started as well.
     *
     * @param context the <code>Context</code> to register
     * @param contextRoot the context root at which to register
     *
     * @throws ConfigException if a <code>Context</code> already exists
     * at the given context root on <code>VirtualServer</code>
     * @throws GlassFishException if the given <code>context</code> fails
     * to be started
     */
    void addContext(Context context, String contextRoot)
        throws ConfigException, GlassFishException;

    /**
     * Stops the given <code>Context</code> and removes it from all
     * <code>VirtualServer</code>.
     *
     * @param context the <code>Context</code> to be stopped and removed
     *
     * @throws GlassFishException if an error occurs during the stopping
     * or removal of the given <code>context</code>
     */
    void removeContext(Context context)
        throws ConfigException, GlassFishException;

    /**
     * Creates a <code>WebListener</code> from the given class type and
     * assigns the given id to it.
     *
     * @param id the id of the new <code>WebListener</code>
     * @param c the class from which to instantiate the
     * <code>WebListener</code>
     *
     * @return the new <code>WebListener</code> instance
     *
     * @throws  IllegalAccessException if the given <code>Class</code> or
     * its nullary constructor is not accessible.
     * @throws  InstantiationException if the given <code>Class</code>
     * represents an abstract class, an interface, an array class,
     * a primitive type, or void; or if the class has no nullary
     * constructor; or if the instantiation fails for some other reason.
     * @throws ExceptionInInitializerError if the initialization
     * fails
     * @throws SecurityException if a security manager, <i>s</i>, is
     * present and any of the following conditions is met:
     *
     * <ul>
     * <li> invocation of <code>s.checkMemberAccess(this, Member.PUBLIC)</code> denies
     * creation of new instances of the given <code>Class</code>
     * <li> the caller's class loader is not the same as or an
     * ancestor of the class loader for the current class and
     * invocation of <code>{@link SecurityManager#checkPackageAccess
     * s.checkPackageAccess()}</code> denies access to the package
     * of this class
     * </ul>
     */
    <T extends WebListener> T createWebListener(String id, Class<T> c)
        throws InstantiationException, IllegalAccessException;

    /**
     * Adds the given <code>WebListener</code> to this
     * <code>WebContainer</code>.
     *
     * <p>If this <code>WebContainer</code> has already been started,
     * the given <code>webListener</code> will be started as well.
     *
     * @param webListener the <code>WebListener</code> to add
     *
     * @throws ConfigException if a <code>WebListener</code> with the
     * same id has already been registered with this
     * <code>WebContainer</code>
     * @throws GlassFishException if the given <code>webListener</code> fails
     * to be started
     */
    void addWebListener(WebListener webListener)
        throws ConfigException, GlassFishException;

    /**
     * Finds the <code>WebListener</code> with the given id.
     *
     * @param id the id of the <code>WebListener</code> to find
     *
     * @return the <code>WebListener</code> with the given id, or
     * <code>null</code> if no <code>WebListener</code> with that id has been
     * registered with this <code>WebContainer</code>
     */
    WebListener getWebListener(String id);

    /**
     * Gets the collection of <code>WebListener</code> instances registered
     * with this <code>WebContainer</code>.
     *
     * @return the (possibly empty) collection of <code>WebListener</code>
     * instances registered with this <code>WebContainer</code>
     */
    Collection<WebListener> getWebListeners();

    /**
     * Stops the given <code>webListener</code> and removes it from this
     * <code>WebContainer</code>.
     *
     * @param webListener the <code>WebListener</code> to be stopped
     * and removed
     *
     * @throws GlassFishException if an error occurs during the stopping
     * or removal of the given <code>webListener</code>
     */
    void removeWebListener(WebListener webListener)
        throws GlassFishException;

    /**
     * Creates a <code>VirtualServer</code> with the given id and docroot, and
     * maps it to the given <code>WebListener</code> instances.
     *
     * @param id the id of the <code>VirtualServer</code>
     * @param docRoot the docroot of the <code>VirtualServer</code>
     * @param webListeners the list of <code>WebListener</code> instances from
     * which the <code>VirtualServer</code> will receive requests
     *
     * @return the new <code>VirtualServer</code> instance
     */
    VirtualServer createVirtualServer(String id,
        File docRoot, WebListener...  webListeners);

    /**
     * Creates a <code>VirtualServer</code> with the given id and docroot, and
     * maps it to all <code>WebListener</code> instances.
     *
     * @param id the id of the <code>VirtualServer</code>
     * @param docRoot the docroot of the <code>VirtualServer</code>
     *
     * @return the new <code>VirtualServer</code> instance
     */
    VirtualServer createVirtualServer(String id, File docRoot);

    /**
     * Adds the given <code>VirtualServer</code> to this
     * <code>WebContainer</code>.
     *
     * <p>If this <code>WebContainer</code> has already been started,
     * the given <code>virtualServer</code> will be started as well.
     *
     * @param virtualServer the <code>VirtualServer</code> to add
     *
     * @throws ConfigException if a <code>VirtualServer</code> with the
     * same id has already been registered with this
     * <code>WebContainer</code>
     * @throws GlassFishException if the given <code>virtualServer</code> fails
     * to be started
     */
    void addVirtualServer(VirtualServer virtualServer)
        throws ConfigException, GlassFishException;

    /**
     * Finds the <code>VirtualServer</code> with the given id.
     *
     * @param id the id of the <code>VirtualServer</code> to find
     *
     * @return the <code>VirtualServer</code> with the given id, or
     * <code>null</code> if no <code>VirtualServer</code> with that id has been
     * registered with this <code>WebContainer</code>
     */
    VirtualServer getVirtualServer(String id);

    /**
     * Gets the collection of <code>VirtualServer</code> instances registered
     * with this <code>WebContainer</code>.
     *
     * @return the (possibly empty) collection of <code>VirtualServer</code>
     * instances registered with this <code>WebContainer</code>
     */
    Collection<VirtualServer> getVirtualServers();

    /**
     * Stops the given <code>virtualServer</code> and removes it from this
     * <code>WebContainer</code>.
     *
     * @param virtualServer the <code>VirtualServer</code> to be stopped
     * and removed
     *
     * @throws GlassFishException if an error occurs during the stopping
     * or removal of the given <code>virtualServer</code>
     */
    void removeVirtualServer(VirtualServer virtualServer)
        throws GlassFishException;

    /**
     * Sets log level
     *
     * @param level log level
     */
    void setLogLevel(Level level);

}
