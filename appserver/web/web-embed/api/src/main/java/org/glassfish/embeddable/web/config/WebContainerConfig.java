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

package org.glassfish.embeddable.web.config;


import java.io.File;
import java.net.URL;

/**
 * Class that is used for configuring WebContainer instances.
 *
 * <p/> Usage example:
 * <pre>
 *      // Create and start Glassfish
 *      GlassFish glassfish = GlassFishRuntime.bootstrap().newGlassFish();
 *      glassfish.start();
 *
 *      // Access WebContainer
 *      WebContainer container = glassfish.getService(WebContainer.class);
 *
 *      WebContainerConfig config = new WebContainerConfig();
 *      config.setListings(true);
 *      config.setPort(9090);
 *      config.setHostNames("localhost");
 *      container.setConfiguration(config);
 * </pre>
 *
 * @see org.glassfish.embeddable.web.WebContainer
 *
 */
public class WebContainerConfig {


    private URL defaultWebXml;
    private File docRoot;
    private String hostNames = "${com.sun.aas.hostName}";
    private String  listenerName = "embedded-listener";
    private boolean listings = false;
    private int port = 8080;
    private String virtualServerId = "server";

    /**
     * Sets the default web xml
     *
     * @param url the url of the default web xml
     */
    public void setDefaultWebXml(URL url) {
        defaultWebXml = url;
    }

    /**
     * Gets the default web xml
     * (default: <i>org/glassfish/web/embed/default-web.xml</i>).
     */
    public URL getDefaultWebXml() {
        if (defaultWebXml == null) {
            defaultWebXml = getClass().getClassLoader().getResource("org/glassfish/web/embed/default-web.xml");
        }
        return defaultWebXml;
    }

    /**
     * Sets the docroot directory
     *
     * @param f the docroot directory
     */
    public void setDocRootDir(File f) {
        docRoot = f;
    }

    /**
     * Gets the docroot directory
     */
    public File getDocRootDir() {
        // TODO: Need to get the docroot from the top level API somehow
        return docRoot;
    }

    /**
     * Sets the host names of the default <tt>VirtualServer</tt>
     * (default: <i>localhost</i>).
     *
     * @param hostNames the host names of the default <tt>VirtualServer</tt> seprated by commas.
     */
    public void setHostNames(String hostNames) {
        this.hostNames = hostNames;
    }

    /**
     * Gets the host names of the default <tt>VirtualServer</tt>
     * (default: <i>localhost</i>).
     *
     * @return the host names of the default <tt>VirtualServer</tt>
     */
    public String getHostNames() {
        return hostNames;
    }

    /**
     * Sets the default listener name
     *
     * @param name the name of the default listener
     */
    public void setListenerName(String name) {
        listenerName = name;
    }

    /**
     * Gets the default listener name
     */
    public String getListenerName() {
        return listenerName;
    }

    /**
     * Enables or disables directory listings
     *
     * @param directoryListing true if directory listings are to be
     * enabled, false otherwise
     */
    public void setListings(boolean directoryListing) {
        listings = directoryListing;
    }

    /**
     * Return if directory listings is enabled
     */
    public boolean getListings() {
        return listings;
    }

    /**
     * Sets the port of the default <tt>WebListener</tt> (default: 8080).
     *
     * @param port the port of the default <tt>WebListener</tt>
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the port of the default <tt>WebListener</tt> (default: 8080).
     *
     * @return the port of the default <tt>WebListener</tt>
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the id of the default <tt>VirtualServer</tt>
     * (default: <i>server</i>).
     *
     * @param virtualServerId the id of the default <tt>VirtualServer</tt>
     */
    public void setVirtualServerId(String virtualServerId) {
        this.virtualServerId = virtualServerId;
    }

    /**
     * Gets the id of the default <tt>VirtualServer</tt>
     * (default: <i>server</i>).
     *
     * @return the id of the default <tt>VirtualServer</tt>
     */
    public String getVirtualServerId() {
        return virtualServerId;
    }

}
