/*
 * Copyright (c) 2010, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.ssl;

import java.net.Socket;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;

import org.glassfish.grizzly.config.ssl.SSLImplementation;
import org.glassfish.grizzly.config.ssl.ServerSocketFactory;
import org.glassfish.grizzly.ssl.SSLSupport;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author Sudarsan Sridhar
 */
@Service(name = "com.sun.enterprise.security.ssl.GlassfishSSLImpl")
@ContractsProvided({ GlassfishSSLImpl.class, SSLImplementation.class })
public class GlassfishSSLImpl extends SSLImplementation {
    public GlassfishSSLImpl() {
    }

    @Override
    public String getImplementationName() {
        return "Glassfish";
    }

    @Override
    public ServerSocketFactory getServerSocketFactory() {
        return new GlassfishServerSocketFactory();
    }

    @Override
    public SSLSupport getSSLSupport(Socket socket) {
        if (socket instanceof SSLSocket) {
            return new GlassfishSSLSupport((SSLSocket) socket);
        }
        return null;
    }

    @Override
    public SSLSupport getSSLSupport(SSLEngine ssle) {
        return new GlassfishSSLSupport(ssle);
    }

}
