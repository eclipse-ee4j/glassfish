/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.tests.utils.Utils;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.Transactions;

import java.util.Collection;
import java.util.List;

/**
 * This test will ensure that when a class is injected with a parent bean and a child
 * is added to the parent, anyone injected will that parent will be notified
 * correctly.
 *
 * User: Jerome Dochez
 */
public class ParentConfigListenerTest extends ConfigApiTest {

    ServiceLocator habitat;

    public String getFileName() {
        return "DomainTest";
    }

    @Before
    public void setup() {
        habitat = Utils.instance.getHabitat(this);
    }
    
    


    @Test
    public void addHttpListenerTest() throws TransactionFailure {
        NetworkListenersContainer container = habitat.getService(NetworkListenersContainer.class);

        ConfigSupport.apply(new SingleConfigCode<NetworkListeners>() {

            public Object run(NetworkListeners param) throws TransactionFailure {
                NetworkListener newListener = param.createChild(NetworkListener.class);
                newListener.setName("Funky-Listener");
                newListener.setPort("8078");
                param.getNetworkListener().add(newListener);
                return null;
            }
        }, container.httpService);

        getHabitat().<Transactions>getService(Transactions.class).waitForDrain();
        assertTrue(container.received);
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(container.httpService);

        // let's check that my newly added listener is available in the habitat.
        List<ServiceHandle<NetworkListener>> networkListeners = habitat.getAllServiceHandles(NetworkListener.class);
        boolean found = false;
        
        for (ServiceHandle<NetworkListener> nlSH : networkListeners) {
            NetworkListener nl = (NetworkListener) nlSH.getService();
            if (nl.getName().equals("Funky-Listener")) {
                found=true;
            }
        }
        Assert.assertTrue("Newly added listener not found", found);
        
        // direct access.
        NetworkListener nl = habitat.getService(NetworkListener.class, "Funky-Listener");
        Assert.assertTrue("Direct access to newly added listener failed", nl!=null);
        bean.removeListener(container);
    }
}
