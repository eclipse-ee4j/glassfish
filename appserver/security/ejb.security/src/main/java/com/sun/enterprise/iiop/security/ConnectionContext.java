/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.iiop.security;

import com.sun.corba.ee.org.omg.CSIIOP.CompoundSecMech;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.transport.SocketInfo;

import java.io.Serializable;
import java.net.Socket;

public final class ConnectionContext implements Serializable {
    private CompoundSecMech mechanism;
    private boolean sslClientAuth;
    private boolean ssl;
    private IOR ior;
    private transient Socket socket;
    private transient SocketInfo endpoint;

    /**
     * Default constructor.
     */
    public ConnectionContext() {
    }

    /**
     * Create the security mechanism context. This is stored in TLS.
     */
    public ConnectionContext(CompoundSecMech mech, IOR ior) {
        this.ior = ior;
        mechanism = mech;
    }

    /**
     * Return the IOR.
     */
    public IOR getIOR() {
        return ior;
    }

    /**
     * Set the IOR
     */
    public void setIOR(IOR ior) {
        this.ior = ior;
    }

    /**
     * Return the selected compound security mechanism.
     */
    public CompoundSecMech getMechanism() {
        return mechanism;
    }

    /**
     * Set the mechanism used for this invocation.
     */
    public void setMechanism(CompoundSecMech mech) {
        mechanism = mech;
    }

    /**
     * Return true if SSL client authentication has happened, false otherwise.
     */
    public boolean getSSLClientAuthenticationOccurred() {
        return sslClientAuth;
    }

    /**
     * Set true if SSL client authentication has happened.
     */
    public void setSSLClientAuthenticationOccurred(boolean val) {
        sslClientAuth = val;
    }

    /**
     * Return true if SSL was used to invoke the EJB.
     */
    public boolean getSSLUsed() {
        return ssl;
    }

    /**
     * Set true if SSL was used to invoke the EJB.
     */
    public void setSSLUsed(boolean val) {
        ssl = val;
    }

    public void setEndPointInfo(SocketInfo info) {
        endpoint = info;
    }

    public SocketInfo getEndPointInfo() {
        return endpoint;
    }

    /**
     * Return the socket for this connection.
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Set the socket for this connection.
     */
    public void setSocket(Socket s) {
        socket = s;
    }

    @Override
    public String toString() {
        String s = "sslClientAuth=" + sslClientAuth;
        s = s + " SSL=" + ssl;
        s = s + " ENDPOINT=" + endpoint;
        s = s + " mechanism=" + mechanism;
        s = s + " IOR=" + ior;
        s = s + " Socket=" + socket;
        return s;
    }
}
