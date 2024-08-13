/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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

import jakarta.inject.Inject;

import java.util.List;
import java.util.logging.Logger;

import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HttpListener.getEnabled() API test
 *
 * @author Jerome Dochez Date: Feb 21, 2008 Time: 2:06:44 PM
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class EnabledTest {

    @Inject
    private ServiceLocator locator;
    @Inject
    private Logger logger;

    private List<NetworkListener> listeners;


    @BeforeEach
    public void setup() {
        NetworkConfig service = locator.getService(NetworkConfig.class);
        assertNotNull(service);
        listeners = service.getNetworkListeners().getNetworkListener();
    }

    @Test
    public void enabled() {
        for (NetworkListener listener : listeners) {
            logger.fine("Listener " + listener.getName() + " enabled " + listener.getEnabled());
            if ("http-listener-2".equals(listener.getName())) {
                assertFalse(Boolean.parseBoolean(listener.getEnabled()));
            } else {
                assertTrue(Boolean.parseBoolean(listener.getEnabled()));
            }
        }
    }
}
