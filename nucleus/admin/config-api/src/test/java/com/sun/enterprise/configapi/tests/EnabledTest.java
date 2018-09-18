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

import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * HttpListener.getEnabled() API test
 *
 * User: Jerome Dochez Date: Feb 21, 2008 Time: 2:06:44 PM
 */
public class EnabledTest extends ConfigApiTest {
    public String getFileName() {
        return "DomainTest";
    }

    List<NetworkListener> listeners = null;

    @Before
    public void setup() {
        NetworkConfig service = getHabitat().getService(NetworkConfig.class);
        assertTrue(service != null);
        listeners = service.getNetworkListeners().getNetworkListener();
    }

    @Test
    public void enabled() {
        for (NetworkListener listener : listeners) {
            logger.fine("Listener " + listener.getName() + " enabled "
                + listener.getEnabled());
            if ("http-listener-2".equals(listener.getName())) {
                assertFalse(new Boolean(listener.getEnabled()));
            } else {
                assertTrue(new Boolean(listener.getEnabled()));
            }
        }
    }
}
