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

import com.sun.enterprise.config.serverbeans.JavaConfig;
import org.glassfish.grizzly.config.dom.Http;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.tests.utils.Utils;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.TransactionFailure;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Jerome Dochez
 * Date: Mar 12, 2008
 * Time: 8:50:42 PM
 */
public class DirectAccessTest extends ConfigPersistence {

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
        NetworkConfig networkConfig = habitat.getService(NetworkConfig.class);
        final NetworkListener listener = networkConfig.getNetworkListeners()
            .getNetworkListener().get(0);
        final Http http = listener.findHttpProtocol().getHttp();
        ConfigBean config = (ConfigBean) ConfigBean.unwrap(http.getFileCache());
        ConfigBean config2 = (ConfigBean) ConfigBean.unwrap(http);
        Map<ConfigBean, Map<String, String>> changes = new HashMap<ConfigBean, Map<String, String>>();
        Map<String, String> configChanges = new HashMap<String, String>();
        configChanges.put("max-age-seconds", "12543");
        configChanges.put("max-cache-size-bytes", "1200");
        Map<String, String> config2Changes = new HashMap<String, String>();
        config2Changes.put("version", "12351");
        changes.put(config, configChanges);
        changes.put(config2, config2Changes);

        JavaConfig javaConfig = habitat.getService(JavaConfig.class);
        ConfigBean javaConfigBean = (ConfigBean) ConfigBean.unwrap(javaConfig);
        Map<String, String> javaConfigChanges = new HashMap<String, String>();
        javaConfigChanges.put("jvm-options", "-XFooBar=false");
        changes.put(javaConfigBean, javaConfigChanges);

        getHabitat().<ConfigSupport>getService(ConfigSupport.class).apply(changes);
    }

    public boolean assertResult(String s) {
        return s.contains("max-age-seconds=\"12543\"")
            && s.contains("version=\"12351\"")
            && s.contains("-XFooBar=false");
    }
}
