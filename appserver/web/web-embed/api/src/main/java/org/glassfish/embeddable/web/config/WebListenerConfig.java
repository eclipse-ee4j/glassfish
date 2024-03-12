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

/**
 * Class that is used for configuring WebListener instances.
 *
 * @see org.glassfish.embeddable.web.WebListener
 */
public class WebListenerConfig {

    private String id;

    private int port;

    private String protocol;

    private boolean traceEnabled;

    public WebListenerConfig(String id, int port) {
        this.id = id;
        this.port = port;
    }

    public WebListenerConfig(String id, int port, String protocol) {
        this.id = id;
        this.port = port;
        this.protocol = protocol;
    }

    /**
     * Sets the id used for configuring <tt>WebListener</tt>.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the id used for configuring <tt>WebListener</tt>.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the port number used for configuring <tt>WebListener</tt>.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the port number used for configuring <tt>WebListener</tt>.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the protocol used for configuring <tt>WebListener</tt>.
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Gets the protocol used for configuring <tt>WebListener</tt>.
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Enables or disables support for TRACE requests.
     *
     * @param traceEnabled true if support for TRACE requests is to be
     * enabled, false otherwise
     */
    public void setTraceEnabled(boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
    }

    /**
     * Checks if support for TRACE requests is enabled.
     *
     * @return true if support for TRACE requests is enabled, false otherwise
     */
    public boolean isTraceEnabled() {
        return traceEnabled;
    }

}
