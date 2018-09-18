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
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.tests.utils.Utils;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.TransactionCallBack;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.WriteableView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Jerome Dochez
 * Date: Mar 28, 2008
 * Time: 4:23:31 PM
 */
public class TransactionCallBackTest extends ConfigPersistence {

    ServiceLocator habitat = Utils.instance.getHabitat(this);

    /**
     * Returns the file name without the .xml extension to load the test configuration
     * from. By default, it's the name of the TestClass.
     *
     * @return the configuration file name
     */
    public String getFileName() {
        return "DomainTest";
    }

    @Override
    public ServiceLocator getBaseServiceLocator() {
        return habitat;
    }
    
    @Override
    public ServiceLocator getHabitat() {
    	return getBaseServiceLocator();
    }
    
    public void doTest() throws TransactionFailure {
        ConfigBean serviceBean = (ConfigBean) ConfigBean.unwrap(habitat.<NetworkListeners>getService(NetworkListeners.class));
        Map<String, String> configChanges = new HashMap<String, String>();
        configChanges.put("name", "funky-listener");

        ConfigSupport.createAndSet(serviceBean, NetworkListener.class, configChanges,
                new TransactionCallBack<WriteableView>() {
                    @SuppressWarnings({"unchecked"})
                    public void performOn(WriteableView param) throws TransactionFailure {
                        // if you know the type...
                        NetworkListener listener = param.getProxy(NetworkListener.class);
                        listener.setName("Aleksey");
                        // if you don't know the type
                        Method m;
                        try {
                            m = param.getProxyType().getMethod("setAddress", String.class);
                            m.invoke(param.getProxy(param.getProxyType()), "localhost");
                        } catch (NoSuchMethodException e) {
                            throw new TransactionFailure("Cannot find getProperty method", e);
                        } catch (IllegalAccessException e) {
                            throw new TransactionFailure("Cannot call getProperty method", e);
                        } catch (InvocationTargetException e) {
                            throw new TransactionFailure("Cannot call getProperty method", e);
                        }
                    }
                });
    }

    public boolean assertResult(String s) {
        return s.contains("Aleksey") && s.contains("localhost");
    }    
}
