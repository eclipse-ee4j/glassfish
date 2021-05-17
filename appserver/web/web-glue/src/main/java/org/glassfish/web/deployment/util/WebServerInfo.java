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
 * This class holds information about a particular web server
 * installation, its running engines and so on...
 *
 * @author Jerome Dochez
 */
public class WebServerInfo {

    /**
     * Holds value of property httpVS.
     */
    private VirtualServerInfo httpVS;

    /**
     * Holds value of property httpsVS.
     */
    private VirtualServerInfo httpsVS;

    /** Creates a new instance of WebServerInfo */
    public WebServerInfo() {
    }

    /**
     * Getter for property httpVS.
     * @return Value of property httpVS.
     */
    public VirtualServerInfo getHttpVS() {
        return this.httpVS;
    }

    /**
     * Setter for property httpVS.
     * @param httpVS New value of property httpVS.
     */
    public void setHttpVS(VirtualServerInfo httpVS) {
        this.httpVS = httpVS;
    }

    /**
     * Getter for property httpsVS.
     * @return Value of property httpsVS.
     */
    public VirtualServerInfo getHttpsVS() {
        return this.httpsVS;
    }

    /**
     * Setter for property httpsVS.
     * @param httpsVS New value of property httpsVS.
     */
    public void setHttpsVS(VirtualServerInfo httpsVS) {
        this.httpsVS = httpsVS;
    }

    public URL getWebServerRootURL(boolean secure) throws MalformedURLException {
        if (secure) {
            if (httpsVS!=null)
                return httpsVS.getWebServerRootURL();

        } else {
            if (httpVS!=null)
                return httpVS.getWebServerRootURL();
        }
        return null;
    }

}
