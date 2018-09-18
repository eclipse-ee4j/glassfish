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

package com.sun.enterprise.util;

import java.util.StringTokenizer;

/**
 * Represents a host and a port in a convenient package that also
 * accepts a convenient constructor.
 */
public class HostAndPort {
    private final String host;
    private final int port;
    private final boolean secure;

    /**
     * Construct a HostAndPort object.
     *
     * @param   host    the host name
     * @param   port    the port number
     * @param   secure  does this host require a secure (SSL) connection?
     */
    public HostAndPort(String host, int port, boolean secure) {
        this.host = host;
        this.port = port;
        this.secure = secure;
    }

    public HostAndPort(HostAndPort rhs) {
        this(rhs.host, rhs.port, rhs.secure);
    }

    public HostAndPort(String host, int port) {
        this(host, port, false);
    }

    /**
     * Construct a new HostAndPort from a string of the form "host:port".
     *
     * @param  str string of the form "host:port"
     */
    public HostAndPort(String str) {
        StringTokenizer tokenizer = new StringTokenizer(str, ":", false);

        host = tokenizer.nextToken();

        final String portString = tokenizer.nextToken();
        port = Integer.parseInt(portString);
        secure = false;
    }

    public boolean isSecure() {
        return secure;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        if (port == 0) {
            return secure ? 8181 : 8080;    // default ports
        } else {
            return port;
        }
    }

    public String toString() {
        return host + ":" + port;
    }
}
