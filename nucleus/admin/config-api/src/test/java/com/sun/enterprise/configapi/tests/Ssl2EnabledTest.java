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

import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.Ssl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * User: Jerome Dochez Date: Mar 4, 2008 Time: 2:44:59 PM
 */
public class Ssl2EnabledTest extends ConfigApiTest {

    @Override
    public String getFileName() {
        return "DomainTest";
    }

    private NetworkConfig config;

    @BeforeEach
    public void setup() {
        config = getHabitat().getService(NetworkConfig.class);
        assertNotNull(config);
    }

    @Test
    public void sslEnabledTest() {
        for (NetworkListener listener : config.getNetworkListeners().getNetworkListener()) {
            Protocol httpProtocol = listener.findHttpProtocol();
            if (httpProtocol != null) {
                Ssl ssl = httpProtocol.getSsl();
                if (ssl != null) {
                    assertFalse(Boolean.parseBoolean(ssl.getSsl2Enabled()));
                    assertFalse(Boolean.parseBoolean(ssl.getSsl3Enabled()));
                    assertTrue(Boolean.parseBoolean(ssl.getTlsEnabled()));
                }
            }
        }
    }
}
