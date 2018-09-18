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

package com.sun.enterprise.configapi.tests;

import org.glassfish.grizzly.config.dom.FileCache;
import org.glassfish.grizzly.config.dom.Http;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.Protocol;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * HttpService related tests
 *
 * @author Jerome Dochez
 */
public class HttpServiceTest extends ConfigApiTest {
    public String getFileName() {
        return "DomainTest";
    }

    NetworkListener listener = null;

    @Before
    public void setup() {
        listener = getHabitat().<NetworkConfig>getService(NetworkConfig.class).getNetworkListener("admin-listener");
        assertTrue(listener != null);
    }

    @Test
    public void connectionTest() {
        final String max = listener.findHttpProtocol().getHttp().getMaxConnections();
        logger.fine("Max connections = " + max);
        assertEquals("Should only allow 250 connections.  The default is 256, however.", "250", max);
    }

    @Test
    public void validTransaction() throws TransactionFailure {
        final String max = listener.findHttpProtocol().getHttp().getMaxConnections();

        ConfigSupport.apply(new SingleConfigCode<NetworkListener>() {
            public Object run(NetworkListener okToChange) throws TransactionFailure {
                final Http http = okToChange.createChild(Http.class);
                http.setMaxConnections("100");
                http.setTimeoutSeconds("65");
                http.setFileCache(http.createChild(FileCache.class));
                ConfigSupport.apply(new SingleConfigCode<Protocol>() {
                    @Override
                    public Object run(Protocol param) {
                        param.setHttp(http);
                        return null;
                    }
                }, okToChange.findHttpProtocol());
                return http;
            }
        }, listener);

        ConfigSupport.apply(new SingleConfigCode<Http>() {
            @Override
            public Object run(Http param) {
                param.setMaxConnections(max);
                return null;
            }
        }, listener.findHttpProtocol().getHttp());
        try {
            ConfigSupport.apply(new SingleConfigCode<Http>() {
                public Object run(Http param) throws TransactionFailure {
                    param.setMaxConnections("7");
                    throw new TransactionFailure("Sorry, changed my mind", null);
                }
            }, listener.findHttpProtocol().getHttp());
        } catch (TransactionFailure e) {
            logger.fine("good, got my exception about changing my mind");
        }
    }
}
