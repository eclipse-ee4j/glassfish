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
 * Base implementation of the <b>WebListener</b> interface
 *
 * @author Amy Roh
 */

public class WebListenerBase implements WebListener  {

    private WebListenerConfig config;

    private String id;

    private int port;

    private String protocol;

    private WebContainer webContainer;

    public WebListenerBase() {
    }

    public WebListenerBase(String id, int port) {
        this.id = id;
        this.port = port;
    }

    /**
     * Sets the id for this <tt>WebListener</tt>.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the id of this <tt>WebListener</tt>.
     */
    public String getId() {
        return id;
    }

    /**
     * Reconfigures this <tt>WebListener</tt> with the given
     * configuration.
     */
    public void setConfig(WebListenerConfig config)
            throws ConfigException, GlassFishException {
        this.config = config;
        setId(config.getId());
        setPort(config.getPort());
        setProtocol(config.getProtocol());
        if (webContainer != null) {
            webContainer.removeWebListener(this);
            webContainer.addWebListener(this);
        }
    }

    /**
     * Gets the current configuration of this <tt>WebListener</tt>.
     */
    public WebListenerConfig getConfig() {
        return config;
    }

    /**
     * Sets the port number for this <tt>WebListener</tt>.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the port number of this <tt>WebListener</tt>.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the protocol which will be used by this <tt>WebListener</tt>.
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Gets the protocol used by this <tt>WebListener</tt>.
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets the <tt>WebContainer</tt> which will be used by this <tt>WebListener</tt>.
     */
    public void setWebContainer(WebContainer webContainer) {
        this.webContainer = webContainer;
    }

    /**
     * Gets the <tt>WebContainer</tt> used by this <tt>WebListener</tt>.
     */
    public WebContainer getWebContainer() {
        return webContainer;
    }


}


