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

import java.util.logging.Logger;

import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.grizzly.config.dom.Transport;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * HttpListener related tests
 *
 * @author Jerome Dochez
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class HttpListenerTest {

    @Inject
    private ServiceLocator locator;
    @Inject
    private Logger logger;

    private NetworkListener listener;


    @BeforeEach
    public void setup() {
        NetworkListeners service = locator.getService(NetworkListeners.class);
        assertNotNull(service);
        for (NetworkListener item : service.getNetworkListener()) {
            if ("http-listener-1".equals(item.getName())) {
                listener= item;
                break;
            }
        }
        assertNotNull(listener);
    }

    @Test
    public void portTest() {
        assertEquals("8080", listener.getPort());
    }

    @Test
    public void validTransaction() throws TransactionFailure {
        SingleConfigCode<Transport> configCode = transport -> {
            transport.setAcceptorThreads("2");
            logger.fine("ID inside the transaction is " + transport.getName());
            return null;
        };
        ConfigSupport.apply(configCode, listener.findTransport());
        logger.fine("ID outside the transaction is " + listener.getName());
        assertEquals("2", listener.findTransport().getAcceptorThreads());
    }
}
