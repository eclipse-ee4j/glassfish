/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

import java.util.Objects;

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
     * @param host the host name, must not be null.
     * @param port the port number, must not be zero.
     * @param secure does this endpoint require a secure connection?
     */
    public HostAndPort(String host, int port, boolean secure) {
        this.host = Objects.requireNonNull(host, "Host name is mandatory.");
        this.port = port;
        this.secure = secure;
        if (port == 0) {
            throw new IllegalArgumentException("Zero port is not allowed.");
        }
    }

    /**
     * @return true for HTTPS, false for HTTP
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * @return hostname, never null.
     */
    public String getHost() {
        return host;
    }

    /**
     * @return port number.
     */
    public int getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, secure);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HostAndPort other = (HostAndPort) obj;
        return Objects.equals(host, other.host) && port == other.port && secure == other.secure;
    }

    @Override
    public String toString() {
        return host + ":" + port + (secure ? " (encrypted)" : " (unencrypted)");
    }
}
