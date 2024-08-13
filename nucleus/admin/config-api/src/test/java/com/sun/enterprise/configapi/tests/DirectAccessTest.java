/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.config.serverbeans.JavaConfig;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.grizzly.config.dom.Http;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.TransactionFailure;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;

/**
 * User: Jerome Dochez
 * Date: Mar 12, 2008
 * Time: 8:50:42 PM
 */
public class DirectAccessTest extends ConfigPersistence {

    @Override
    public void doTest() throws TransactionFailure {
        NetworkConfig networkConfig = locator.getService(NetworkConfig.class);
        final NetworkListener listener = networkConfig.getNetworkListeners().getNetworkListener().get(0);
        final Http http = listener.findHttpProtocol().getHttp();
        ConfigBean config = (ConfigBean) Dom.unwrap(http.getFileCache());
        ConfigBean config2 = (ConfigBean) Dom.unwrap(http);
        Map<ConfigBean, Map<String, String>> changes = new HashMap<>();
        Map<String, String> configChanges = new HashMap<>();
        configChanges.put("max-age-seconds", "12543");
        configChanges.put("max-cache-size-bytes", "1200");
        Map<String, String> config2Changes = new HashMap<>();
        config2Changes.put("version", "12351");
        changes.put(config, configChanges);
        changes.put(config2, config2Changes);

        JavaConfig javaConfig = locator.getService(JavaConfig.class);
        ConfigBean javaConfigBean = (ConfigBean) Dom.unwrap(javaConfig);
        Map<String, String> javaConfigChanges = new HashMap<>();
        javaConfigChanges.put("jvm-options", "-XFooBar=false");
        changes.put(javaConfigBean, javaConfigChanges);

        locator.<ConfigSupport>getService(ConfigSupport.class).apply(changes);
    }

    @Override
    public void assertResult(String xml) {
        assertThat(xml, stringContainsInOrder("-XFooBar=false", "version=\"12351\"", "max-age-seconds=\"12543\""));
    }
}
