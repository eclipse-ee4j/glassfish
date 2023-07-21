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

import java.io.Serializable;
import java.net.Socket;

public final class ServerConnectionContext implements Serializable {
    private static final long serialVersionUID = 1L;
    private transient Socket socket;

    /**
     * Default constructor.
     */
    public ServerConnectionContext() {
    }

    /**
     * Create the security mechanism context. This is stored in TLS.
     */
    public ServerConnectionContext(Socket sock) {
        this.socket = sock;
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
        String s = "Socket=" + socket;
        return s;
    }
}
