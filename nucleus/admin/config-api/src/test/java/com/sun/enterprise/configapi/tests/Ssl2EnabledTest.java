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

import java.util.List;

import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.Ssl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * User: Jerome Dochez Date: Mar 4, 2008 Time: 2:44:59 PM
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class Ssl2EnabledTest {

    @Inject
    private NetworkConfig config;

    @Test
    public void sslEnabledTest() {
        final List<NetworkListener> listeners = config.getNetworkListeners().getNetworkListener();
        assertThat(listeners, hasSize(3));
        assertAll(
            () -> {
                NetworkListener listener = listeners.get(0);
                Protocol httpProtocol = listener.findHttpProtocol();
                assertNotNull(httpProtocol);
                assertEquals("http-listener-1", httpProtocol.getName());
                Ssl ssl = httpProtocol.getSsl();
                assertNull(ssl);
            },
            () -> {
                NetworkListener listener = listeners.get(1);
                Protocol httpProtocol = listener.findHttpProtocol();
                assertNotNull(httpProtocol);
                assertEquals("http-listener-2", httpProtocol.getName());
                Ssl ssl = httpProtocol.getSsl();
                assertNotNull(ssl);
                assertFalse(Boolean.parseBoolean(ssl.getSsl2Enabled()));
                assertFalse(Boolean.parseBoolean(ssl.getSsl3Enabled()));
                assertTrue(Boolean.parseBoolean(ssl.getTlsEnabled()));
            },
            () -> {
                NetworkListener listener = listeners.get(2);
                Protocol httpProtocol = listener.findHttpProtocol();
                assertNotNull(httpProtocol);
                assertEquals("admin-listener", httpProtocol.getName());
                Ssl ssl = httpProtocol.getSsl();
                assertNull(ssl);
            }
        );
    }
}
