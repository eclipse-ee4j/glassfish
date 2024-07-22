/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2007-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.glassfish.grizzly.config.test;

import java.net.Socket;

import javax.net.ssl.SSLEngine;

import org.glassfish.grizzly.config.ssl.JSSEFactory;
import org.glassfish.grizzly.config.ssl.SSLImplementation;
import org.glassfish.grizzly.config.ssl.ServerSocketFactory;
import org.glassfish.grizzly.ssl.SSLSupport;
import org.jvnet.hk2.annotations.Service;

/**
 * JSSEImplementation:
 *
 * Concrete implementation class for JSSE Grizzly Config Tests
 *
 * @author EKR
 */
@Service
public class JSSEImplementation4Tests implements SSLImplementation {
    private final JSSEFactory factory = new JSSEFactory();

    @Override
    public String getImplementationName() {
        return "JSSE";
    }

    @Override
    public ServerSocketFactory getServerSocketFactory() {
        return factory.getSocketFactory();
    }

    @Override
    public SSLSupport getSSLSupport(Socket socket) {
        return factory.getSSLSupport(socket);
    }

    @Override
    public SSLSupport getSSLSupport(SSLEngine sslEngine) {
        return factory.getSSLSupport(sslEngine);
    }
}
