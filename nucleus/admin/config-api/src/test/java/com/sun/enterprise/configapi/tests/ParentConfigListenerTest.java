/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.Transactions;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test will ensure that when a class is injected with a parent bean and a child
 * is added to the parent, anyone injected will that parent will be notified
 * correctly.
 *
 * User: Jerome Dochez
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class ParentConfigListenerTest {

    @Inject
    private ServiceLocator locator;

    @Test
    public void addHttpListenerTest() throws TransactionFailure {
        NetworkListenersContainer container = locator.getService(NetworkListenersContainer.class);
        SingleConfigCode<NetworkListeners> configCode = listeners -> {
            NetworkListener newListener = listeners.createChild(NetworkListener.class);
            newListener.setName("Funky-Listener");
            newListener.setPort("8078");
            listeners.getNetworkListener().add(newListener);
            return null;
        };
        ConfigSupport.apply(configCode, container.httpService);

        locator.<Transactions>getService(Transactions.class).waitForDrain();
        assertTrue(container.received);
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(container.httpService);

        // let's check that my newly added listener is available in the habitat.
        List<ServiceHandle<NetworkListener>> networkListeners = locator.getAllServiceHandles(NetworkListener.class);
        boolean found = false;
        for (ServiceHandle<NetworkListener> nlSH : networkListeners) {
            NetworkListener nl = nlSH.getService();
            if (nl.getName().equals("Funky-Listener")) {
                found = true;
            }
        }
        assertTrue(found, "Newly added listener not found");

        // direct access.
        NetworkListener networkListener = locator.getService(NetworkListener.class, "Funky-Listener");
        assertNotNull(networkListener, "Direct access to newly added listener failed");
        bean.removeListener(container);
    }
}
