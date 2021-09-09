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

import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.TransactionFailure;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;

/**
 * User: Jerome Dochez
 * Date: Mar 27, 2008
 * Time: 3:18:57 PM
 */
public class DirectRemovalTest extends ConfigPersistence {

    @Override
    public void doTest() throws TransactionFailure {
        NetworkListeners listeners = locator.getService(NetworkListeners.class);
        ConfigBean serviceBean = (ConfigBean) Dom.unwrap(listeners);
        for (NetworkListener listener : listeners.getNetworkListener()) {
            if (listener.getName().endsWith("http-listener-1")) {
                ConfigSupport.deleteChild(serviceBean, (ConfigBean) Dom.unwrap(listener));
                break;
            }
        }
    }

    @Override
    public void assertResult(String xml) {
        assertThat(xml, not(stringContainsInOrder("id=\"http-listener-1\"")));
    }
}
