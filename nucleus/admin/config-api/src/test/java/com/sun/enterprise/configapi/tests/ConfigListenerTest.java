/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.configapi.tests.example.HttpListenerContainer;

import jakarta.inject.Inject;

import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.config.support.ConfigConfigBeanListener;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.Transactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simple ConfigListener tests
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class ConfigListenerTest {

    private static final SingleConfigCode<NetworkListener> configCodeRestore = param -> {
        param.setPort("8080");
        return null;
    };

    @Inject
    private ServiceLocator locator;
    private Transactions transactions;
    private HttpListenerContainer container;

    @BeforeEach
    public void setup() {
        // make sure the ConfigConfigListener exists
        ServiceHandle<ConfigConfigBeanListener> i = locator.getServiceHandle(ConfigConfigBeanListener.class);
        ConfigConfigBeanListener listener = i.getService();
        assertNotNull(listener);
        transactions = locator.getService(Transactions.class);
        container = registerAndCreateHttpListenerContainer(locator);
    }


    @AfterEach
    public void reset() throws TransactionFailure {
        ConfigSupport.apply(configCodeRestore, container.httpListener);
        assertEquals("8080", container.httpListener.getPort());
    }

    @Test
    public void changedTest() throws TransactionFailure {
        SingleConfigCode<NetworkListener> configCode = listener -> {
            listener.setPort("8989");
            return null;
        };
        ConfigSupport.apply(configCode, container.httpListener);
        assertEquals("8989", container.httpListener.getPort());

        transactions.waitForDrain();
        assertTrue(container.received);
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(container.httpListener);
        bean.removeListener(container);
    }

    @Test
    public void removeListenerTest() throws TransactionFailure {
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(container.httpListener);
        bean.removeListener(container);

        SingleConfigCode<NetworkListener> configCode = listener -> {
            listener.setPort("8989");
            return null;
        };
        ConfigSupport.apply(configCode, container.httpListener);

        transactions.waitForDrain();
        assertFalse(container.received);
    }


    private HttpListenerContainer registerAndCreateHttpListenerContainer(ServiceLocator locator) {
        HttpListenerContainer retVal = locator.getService(HttpListenerContainer.class);
        if (retVal != null) {
            return retVal;
        }

        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        assertNotNull(dcs);

        DynamicConfiguration config = dcs.createDynamicConfiguration();
        config.addActiveDescriptor(HttpListenerContainer.class);
        config.commit();

        return locator.getService(HttpListenerContainer.class);
    }
}
