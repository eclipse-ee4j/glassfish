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
import org.jvnet.hk2.config.TransactionFailure;

/**
 * User: Jerome Dochez
 * Date: Mar 27, 2008
 * Time: 3:18:57 PM
 */
public class DirectRemovalTest extends ConfigPersistence {

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

    public void doTest() throws TransactionFailure {

        NetworkListeners listeners = habitat.getService(NetworkListeners.class);

        ConfigBean serviceBean = (ConfigBean) ConfigBean.unwrap(listeners);

        for (NetworkListener listener : listeners.getNetworkListener()) {
            if (listener.getName().endsWith("http-listener-1")) {
                ConfigSupport.deleteChild(serviceBean, (ConfigBean) ConfigBean.unwrap(listener));
                break;
            }
        }
    }

    public boolean assertResult(String s) {
        // we must not find it
        return !s.contains("id=\"http-listener-1\"");
    }
}
