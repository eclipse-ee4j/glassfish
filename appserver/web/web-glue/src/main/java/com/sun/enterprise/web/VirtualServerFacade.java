/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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


import java.io.*;
import java.util.*;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.web.ConfigException;
import org.glassfish.embeddable.web.Context;
import org.glassfish.embeddable.web.WebListener;
import org.glassfish.embeddable.web.config.VirtualServerConfig;

/**
 * Facade object which masks the internal <code>VirtualServer</code>
 * object from the web application.
 *
 * @author Amy Roh
 */
public class VirtualServerFacade implements org.glassfish.embeddable.web.VirtualServer {


    // ----------------------------------------------------------- Constructors


    public VirtualServerFacade(String id, File docRoot, WebListener...  webListeners) {
        this.id = id;
        this.docRoot = docRoot;
        if (webListeners != null) {
            this.webListeners = Arrays.asList(webListeners);
        }
    }


    // ----------------------------------------------------- Instance Variables


    private VirtualServerConfig config;

    private File docRoot;

    private String id;

    /**
     * Wrapped web module.
     */
    private VirtualServer vs = null;

    private List<WebListener> webListeners = null;

    // ----------------------------------------------------- embedded methods


    /**
     * Sets the docroot of this <tt>VirtualServer</tt>.
     *
     * @param docRoot the docroot of this <tt>VirtualServer</tt>.
     */
    public void setDocRoot(File docRoot) {
        this.docRoot = docRoot;
        if (vs != null) {
            vs.setDocRoot(docRoot);
        }
    }

    /**
     * Gets the docroot of this <tt>VirtualServer</tt>.
     */
    public File getDocRoot() {
        return docRoot;
    }

    /**
     * Return the virtual server identifier.
     */
    public String getID() {
        return id;
    }

    /**
     * Set the virtual server identifier string.
     *
     * @param id New identifier for this virtual server
     */
    public void setID(String id) {
        this.id = id;
        if (vs != null) {
            vs.setID(id);
        }
    }

    /**
     * Sets the collection of <tt>WebListener</tt> instances from which
     * this <tt>VirtualServer</tt> receives requests.
     *
     * @param webListeners the collection of <tt>WebListener</tt> instances from which
     * this <tt>VirtualServer</tt> receives requests.
     */
    public void setWebListeners(WebListener...  webListeners) {
        if (webListeners != null) {
            this.webListeners = Arrays.asList(webListeners);
        }
    }

    /**
     * Gets the collection of <tt>WebListener</tt> instances from which
     * this <tt>VirtualServer</tt> receives requests.
     *
     * @return the collection of <tt>WebListener</tt> instances from which
     * this <tt>VirtualServer</tt> receives requests.
     */
    public Collection<WebListener> getWebListeners() {
        return webListeners;
    }

    /**
     * Registers the given <tt>Context</tt> with this <tt>VirtualServer</tt>
     * at the given context root.
     *
     * <p>If this <tt>VirtualServer</tt> has already been started, the
     * given <tt>context</tt> will be started as well.
     */
    public void addContext(Context context, String contextRoot)
        throws ConfigException, GlassFishException {
        if (vs != null) {
            vs.addContext(context, contextRoot);
        } else {
            throw new GlassFishException("Virtual server "+id+" has not been added");
        }
    }

    /**
     * Stops the given <tt>context</tt> and removes it from this
     * <tt>VirtualServer</tt>.
     */
    public void removeContext(Context context)
            throws GlassFishException {
        if (vs != null) {
            vs.removeContext(context);
        } else {
            throw new GlassFishException("Virtual server "+id+" has not been added");
        }
    }

    /**
     * Finds the <tt>Context</tt> registered at the given context root.
     */
    public Context getContext(String contextRoot) {
        if (vs != null) {
            return vs.getContext(contextRoot);
        } else {
            return null;
        }
    }

    /**
     * Gets the collection of <tt>Context</tt> instances registered with
     * this <tt>VirtualServer</tt>.
     */
    public Collection<Context> getContexts() {
        if (vs != null) {
            return vs.getContexts();
        } else {
            return null;
        }
    }

    /**
     * Reconfigures this <tt>VirtualServer</tt> with the given
     * configuration.
     *
     * <p>In order for the given configuration to take effect, this
     * <tt>VirtualServer</tt> may be stopped and restarted.
     */
    public void setConfig(VirtualServerConfig config)
        throws ConfigException {
        this.config = config;
        if (vs != null) {
            vs.setConfig(config);
        }
    }

    /**
     * Gets the current configuration of this <tt>VirtualServer</tt>.
     */
    public VirtualServerConfig getConfig() {
        return config;
    }

    public void setVirtualServer(VirtualServer vs) {
        this.vs = vs;
    }

    public VirtualServer getVirtualServer() {
        return vs;
    }

}
