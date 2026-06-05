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

package org.glassfish.web.deployment.util;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class holds information about a web server virtual
 * engine configuration
 *
 * @author Jerome Dochez
 */
public class VirtualServerInfo {

    /**
     * Holds value of property host.
     */
    private String host;

    /**
     * Holds value of property port.
     */
    private int port;

    /**
     * Holds value of property protocol.
     */
    private String protocol;

    /** Creates a new instance of VirtualServerInfo */
    public VirtualServerInfo(String protocol, String host, int port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    /**
     * Getter for property serverName.
     * @return Value of property serverName.
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Getter for property port.
     * @return value of property port.
     */
    public int getPort() {
       return this.port;
    }

    /**
     * Getter for property protocol.
     * @return Value of property protocol.
     */
    public String getProtocol() {
        return this.protocol;
    }

    /**
     * @return the web server root URL
     */
    public URL getWebServerRootURL() throws MalformedURLException {
        return new URL(protocol, host, port, "");
    }
}
