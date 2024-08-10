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

import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.web.config.VirtualServerConfig;

/**
 * Representation of a virtual server.
 *
 * <p>Instances of <tt>VirtualServer</tt> may be in one of two states:
 * <i>stopped</i> or <i>started</i>. Any requests mapped to a
 * <tt>VirtualServer</tt> that was stopped will result in a response with
 * a status code equal to
 * jakarta.servlet.http.HttpServletResponse#SC_NOT_FOUND.
 *
 * <p/>See {@link WebContainer} for usage example.
 *
 * @author Rajiv Mordani
 * @author Jan Luehe
 */
public interface VirtualServer {


    /**
     * Sets the id of this <tt>VirtualServer</tt>.
     *
     * @param ID id of this <tt>VirtualServer</tt>.
     */
    public void setID(String ID);

    /**
     * Gets the id of this <tt>VirtualServer</tt>.
     *
     * @return the id of this <tt>VirtualServer</tt>
     */
    public String getID();

    /**
     * Sets the docroot of this <tt>VirtualServer</tt>.
     *
     * @param docRoot the docroot of this <tt>VirtualServer</tt>.
     */
    public void setDocRoot(File docRoot);

    /**
     * Gets the docroot of this <tt>VirtualServer</tt>.
     *
     * @return the docroot of this <tt>VirtualServer</tt>
     */
    public File getDocRoot();

    /**
     * Gets the collection of <tt>WebListener</tt> instances from which
     * this <tt>VirtualServer</tt> receives requests.
     *
     * @return the collection of <tt>WebListener</tt> instances from which
     * this <tt>VirtualServer</tt> receives requests.
     */
    public Collection<WebListener> getWebListeners();

    /**
     * Adds the given <tt>Valve</tt> to this <tt>VirtualServer</tt>
     *
     * @param valve the <tt>Valve</tt> to be added
     */
    //public void addValve(Valve valve);

    /**
     * Registers the given <tt>Context</tt> with this <tt>VirtualServer</tt>
     * at the given context root.
     *
     * <p>If this <tt>VirtualServer</tt> has already been started, the
     * given <tt>context</tt> will be started as well.
     *
     * @param context the <tt>Context</tt> to register
     * @param contextRoot the context root at which to register
     *
     * @throws ConfigException if a <tt>Context</tt> already exists
     * at the given context root on this <tt>VirtualServer</tt>
     * @throws GlassFishException if the given <tt>context</tt> fails
     * to be started
     */
    public void addContext(Context context, String contextRoot)
        throws ConfigException, GlassFishException;

    /**
     * Stops the given <tt>context</tt> and removes it from this
     * <tt>VirtualServer</tt>.
     *
     * @param context the <tt>Context</tt> to be stopped and removed
     *
     * @throws GlassFishException if an error occurs during the stopping
     * or removal of the given <tt>context</tt>
     */
    public void removeContext(Context context)
            throws GlassFishException;

    /**
     * Finds the <tt>Context</tt> registered at the given context root.
     *
     * @param contextRoot the context root whose <tt>Context</tt> to get
     *
     * @return the <tt>Context</tt> registered at the given context root,
     * or <tt>null</tt> if no <tt>Context</tt> exists at the given context
     * root
     */
    public Context getContext(String contextRoot);

    /**
     * Gets the collection of <tt>Context</tt> instances registered with
     * this <tt>VirtualServer</tt>.
     *
     * @return the (possibly empty) collection of <tt>Context</tt>
     * instances registered with this <tt>VirtualServer</tt>
     */
    public Collection<Context> getContexts();

    /**
     * Reconfigures this <tt>VirtualServer</tt> with the given
     * configuration.
     *
     * <p>In order for the given configuration to take effect, this
     * <tt>VirtualServer</tt> may be stopped and restarted.
     *
     * @param config the configuration to be applied
     *
     * @throws ConfigException if the configuration requires a restart,
     * and this <tt>VirtualServer</tt> fails to be restarted
     */
    public void setConfig(VirtualServerConfig config)
        throws ConfigException;

    /**
     * Gets the current configuration of this <tt>VirtualServer</tt>.
     *
     * @return the current configuration of this <tt>VirtualServer</tt>,
     * or <tt>null</tt> if no special configuration was ever applied to this
     * <tt>VirtualServer</tt>
     */
    public VirtualServerConfig getConfig();
}
