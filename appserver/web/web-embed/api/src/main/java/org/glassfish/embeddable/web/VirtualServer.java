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
 * <p>Instances of <code>VirtualServer</code> may be in one of two states:
 * <i>stopped</i> or <i>started</i>. Any requests mapped to a
 * <code>VirtualServer</code> that was stopped will result in a response with
 * a status code equal to
 * jakarta.servlet.http.HttpServletResponse#SC_NOT_FOUND.
 *
 * <p>See {@link WebContainer} for usage example.
 *
 * @author Rajiv Mordani
 * @author Jan Luehe
 */
public interface VirtualServer {


    /**
     * Sets the id of this <code>VirtualServer</code>.
     *
     * @param ID id of this <code>VirtualServer</code>.
     */
    void setID(String ID);

    /**
     * Gets the id of this <code>VirtualServer</code>.
     *
     * @return the id of this <code>VirtualServer</code>
     */
    String getID();

    /**
     * Sets the docroot of this <code>VirtualServer</code>.
     *
     * @param docRoot the docroot of this <code>VirtualServer</code>.
     */
    void setDocRoot(File docRoot);

    /**
     * Gets the docroot of this <code>VirtualServer</code>.
     *
     * @return the docroot of this <code>VirtualServer</code>
     */
    File getDocRoot();

    /**
     * Gets the collection of <code>WebListener</code> instances from which
     * this <code>VirtualServer</code> receives requests.
     *
     * @return the collection of <code>WebListener</code> instances from which
     * this <code>VirtualServer</code> receives requests.
     */
    Collection<WebListener> getWebListeners();

    /**
     * Adds the given <code>Valve</code> to this <code>VirtualServer</code>
     *
     * @param valve the <code>Valve</code> to be added
     */
    //public void addValve(Valve valve);

    /**
     * Registers the given <code>Context</code> with this <code>VirtualServer</code>
     * at the given context root.
     *
     * <p>If this <code>VirtualServer</code> has already been started, the
     * given <code>context</code> will be started as well.
     *
     * @param context the <code>Context</code> to register
     * @param contextRoot the context root at which to register
     *
     * @throws ConfigException if a <code>Context</code> already exists
     * at the given context root on this <code>VirtualServer</code>
     * @throws GlassFishException if the given <code>context</code> fails
     * to be started
     */
    void addContext(Context context, String contextRoot)
        throws ConfigException, GlassFishException;

    /**
     * Stops the given <code>context</code> and removes it from this
     * <code>VirtualServer</code>.
     *
     * @param context the <code>Context</code> to be stopped and removed
     *
     * @throws GlassFishException if an error occurs during the stopping
     * or removal of the given <code>context</code>
     */
    void removeContext(Context context)
            throws GlassFishException;

    /**
     * Finds the <code>Context</code> registered at the given context root.
     *
     * @param contextRoot the context root whose <code>Context</code> to get
     *
     * @return the <code>Context</code> registered at the given context root,
     * or <code>null</code> if no <code>Context</code> exists at the given context
     * root
     */
    Context getContext(String contextRoot);

    /**
     * Gets the collection of <code>Context</code> instances registered with
     * this <code>VirtualServer</code>.
     *
     * @return the (possibly empty) collection of <code>Context</code>
     * instances registered with this <code>VirtualServer</code>
     */
    Collection<Context> getContexts();

    /**
     * Reconfigures this <code>VirtualServer</code> with the given
     * configuration.
     *
     * <p>In order for the given configuration to take effect, this
     * <code>VirtualServer</code> may be stopped and restarted.
     *
     * @param config the configuration to be applied
     *
     * @throws ConfigException if the configuration requires a restart,
     * and this <code>VirtualServer</code> fails to be restarted
     */
    void setConfig(VirtualServerConfig config)
        throws ConfigException;

    /**
     * Gets the current configuration of this <code>VirtualServer</code>.
     *
     * @return the current configuration of this <code>VirtualServer</code>,
     * or <code>null</code> if no special configuration was ever applied to this
     * <code>VirtualServer</code>
     */
    VirtualServerConfig getConfig();
}
