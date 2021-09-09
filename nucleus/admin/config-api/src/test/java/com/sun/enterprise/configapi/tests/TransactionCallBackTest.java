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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.TransactionCallBack;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.WriteableView;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;

/**
 * User: Jerome Dochez
 * Date: Mar 28, 2008
 * Time: 4:23:31 PM
 */
public class TransactionCallBackTest extends ConfigPersistence {

    @Override
    public void doTest() throws TransactionFailure {
        ConfigBean serviceBean = (ConfigBean) Dom.unwrap(locator.<NetworkListeners> getService(NetworkListeners.class));
        Map<String, String> configChanges = new HashMap<>();
        configChanges.put("name", "funky-listener");

        TransactionCallBack<WriteableView> callBack = view -> {
            // if you know the type...
            NetworkListener listener = view.getProxy(NetworkListener.class);
            listener.setName("Aleksey");
            // if you don't know the type
            Method setAddressMethod;
            try {
                setAddressMethod = view.getProxyType().getMethod("setAddress", String.class);
                setAddressMethod.invoke(view.getProxy(view.getProxyType()), "localhost");
            } catch (NoSuchMethodException e1) {
                throw new TransactionFailure("Cannot find getProperty method", e1);
            } catch (IllegalAccessException | InvocationTargetException e2) {
                throw new TransactionFailure("Cannot call getProperty method", e2);
            }
        };
        ConfigSupport.createAndSet(serviceBean, NetworkListener.class, configChanges, callBack);
    }


    @Override
    public void assertResult(String xml) {
        assertThat(xml, stringContainsInOrder("localhost", "Aleksey"));
    }
}
