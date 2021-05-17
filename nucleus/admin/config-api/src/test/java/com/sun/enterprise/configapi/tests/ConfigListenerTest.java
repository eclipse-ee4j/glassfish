/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;

import org.glassfish.config.support.ConfigConfigBeanListener;
import org.glassfish.tests.utils.Utils;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.Transactions;

import com.sun.enterprise.config.serverbeans.Config;

/**
 * Simple ConfigListener tests
 */
public class ConfigListenerTest extends ConfigApiTest {

    ServiceLocator habitat;

    @Override
    public String getFileName() {
        return "DomainTest";
    }

    @Before
    public void setup() {
        habitat = Utils.instance.getHabitat(this);

        // make sure the ConfigConfigListener exists
        ServiceHandle<ConfigConfigBeanListener> i = habitat.getServiceHandle(ConfigConfigBeanListener.class);
        ConfigConfigBeanListener ccbl = i.getService();
        assertTrue(ccbl != null);
    }

    private HttpListenerContainer registerAndCreateHttpListenerContainer(ServiceLocator locator) {
        HttpListenerContainer retVal = locator.getService(HttpListenerContainer.class);
        if (retVal != null) return retVal;

        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        Assert.assertNotNull(dcs);

        DynamicConfiguration config = dcs.createDynamicConfiguration();

        config.addActiveDescriptor(HttpListenerContainer.class);

        config.commit();

        return locator.getService(HttpListenerContainer.class);
    }


    @Test
    public void changedTest() throws TransactionFailure {

        Transactions transactions = getHabitat().getService(Transactions.class);

        HttpListenerContainer container = registerAndCreateHttpListenerContainer(habitat);

        ConfigSupport.apply(new SingleConfigCode<NetworkListener>() {

            @Override
            public Object run(NetworkListener param) {
                param.setPort("8989");
                return null;
            }
        }, container.httpListener);

        transactions.waitForDrain();
        assertTrue(container.received);
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(container.httpListener);
        bean.removeListener(container);

        // put back the right values in the domain to avoid test collisions
        ConfigSupport.apply(new SingleConfigCode<NetworkListener>() {

            @Override
            public Object run(NetworkListener param) {
                param.setPort("8080");
                return null;
            }
        }, container.httpListener);
    }

    @Test
    public void removeListenerTest() throws TransactionFailure {

        Transactions transactions = getHabitat().getService(Transactions.class);

        HttpListenerContainer container = registerAndCreateHttpListenerContainer(habitat);

        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(container.httpListener);
        bean.removeListener(container);

        ConfigSupport.apply(new SingleConfigCode<NetworkListener>() {

            @Override
            public Object run(NetworkListener param) {
                param.setPort("8989");
                return null;
            }
        }, container.httpListener);

        transactions.waitForDrain();
        assertFalse(container.received);

        // put back the right values in the domain to avoid test collisions
        ConfigSupport.apply(new SingleConfigCode<NetworkListener>() {

            @Override
            public Object run(NetworkListener param) {
                param.setPort("8080");
                return null;
            }
        }, container.httpListener);
    }
}
