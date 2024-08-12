/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.configapi.tests;

import jakarta.inject.Inject;

import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.grizzly.config.dom.FileCache;
import org.glassfish.grizzly.config.dom.Http;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * HttpService related tests
 *
 * @author Jerome Dochez
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class HttpServiceTest {

    @Inject
    private ServiceLocator locator;

    private NetworkListener listener;


    @BeforeEach
    public void setup() {
        listener = locator.<NetworkConfig>getService(NetworkConfig.class).getNetworkListener("admin-listener");
        assertNotNull(listener);
    }

    @Test
    public void connectionTest() {
        final String max = listener.findHttpProtocol().getHttp().getMaxConnections();
        assertEquals("250", max, "Should only allow 250 connections. The default is 256, however.");
    }

    @Test
    public void validTransaction() throws TransactionFailure {
        final String max = listener.findHttpProtocol().getHttp().getMaxConnections();

        SingleConfigCode<NetworkListener> listenerConfigCode = networkListener -> {
            final Http http = networkListener.createChild(Http.class);
            http.setMaxConnections("100");
            http.setTimeoutSeconds("65");
            http.setFileCache(http.createChild(FileCache.class));
            SingleConfigCode<Protocol> protocolConfigCode = protocol -> {
                protocol.setHttp(http);
                return null;
            };
            ConfigSupport.apply(protocolConfigCode, networkListener.findHttpProtocol());
            return http;
        };
        ConfigSupport.apply(listenerConfigCode, listener);

        SingleConfigCode<Http> httpConfigCode = (SingleConfigCode<Http>) http -> {
            http.setMaxConnections(max);
            return null;
        };
        ConfigSupport.apply(httpConfigCode, listener.findHttpProtocol().getHttp());
        SingleConfigCode<Http> configCode = http -> {
            http.setMaxConnections("7");
            throw new TransactionFailure("Sorry, changed my mind", null);
        };
        assertThrows(TransactionFailure.class,
            () -> ConfigSupport.apply(configCode, listener.findHttpProtocol().getHttp()));
        assertEquals(max, listener.findHttpProtocol().getHttp().getMaxConnections());
    }
}
