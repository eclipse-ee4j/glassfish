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

import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.web.config.WebListenerConfig;

/**
 * Representation of a network listener for web requests.
 *
 * <p>See {@link WebContainer} for usage example.
 *
 * @author Rajiv Mordani
 * @author Amy Roh
 */
public interface WebListener  {

    /**
     * Sets the id for this <code>WebListener</code>.
     *
     * @param id for this <code>WebListener</code>
     */
    void setId(String id);

    /**
     * Gets the id of this <code>WebListener</code>.
     *
     * @return id of this <code>WebListener</code>
     */
    String getId();

    /**
     * Sets the port number for this <code>WebListener</code>.
     *
     * @param port the port number for this <code>WebListener</code>
     */
    void setPort(int port);

    /**
     * Gets the port number of this <code>WebListener</code>.
     *
     * @return the port number of this <code>WebListener</code>
     */
    int getPort();

    /**
     * Sets the protocol for this <code>WebListener</code>.
     *
     * @param protocol the protocol for this <code>WebListener</code>
     */
    void setProtocol(String protocol);

    /**
     * Gets the protocol of this <code>WebListener</code>.
     *
     * @return the protocol of this <code>WebListener</code>
     */
    String getProtocol();

    /**
     * Reconfigures this <code>WebListener</code> with the given
     * configuration.
     *
     * <p>In order for the given configuration to take effect, this
     * <code>WebListener</code> will be stopped and restarted.
     *
     * @param config the configuration to be applied
     *
     * @throws ConfigException if the configuration requires a restart,
     * and this <code>WebListener</code> fails to be restarted
     * @throws GlassFishException if an error occurs,
     * and this <code>WebListener</code> fails to be restarted
     */
    void setConfig(WebListenerConfig config) throws ConfigException, GlassFishException;

    /**
     * Gets the current configuration of this <code>WebListener</code>.
     *
     * @return the current configuration of this <code>WebListener</code>,
     * or <code>null</code> if no special configuration was ever applied to this
     * <code>WebListener</code>
     */
    WebListenerConfig getConfig();

    /**
     * Sets the <code>WebContainer</code> which will be used by this <code>WebListener</code>.
     */
    void setWebContainer(WebContainer webContainer);

    /**
     * Gets the <code>WebContainer</code> used by this <code>WebListener</code>.
     */
    WebContainer getWebContainer();

}
