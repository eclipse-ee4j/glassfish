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

import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.grizzly.config.dom.Transport;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * HttpListener related tests
 *
 * @author Jerome Dochez
 */
public class HttpListenerTest extends ConfigApiTest {


    public String getFileName() {
        return "DomainTest";
    }

    NetworkListener listener = null;

    @Before
    public void setup() {
        NetworkListeners service = getHabitat().getService(NetworkListeners.class);
        assertTrue(service!=null);
        for (NetworkListener item : service.getNetworkListener()) {
            if ("http-listener-1".equals(item.getName())) {
                listener= item;
                break;
            }
        }

        logger.fine("listener = " + listener);
        assertTrue(listener!=null);                 

    }
    
    @Test
    public void portTest() {
        logger.fine("port = " + listener.getPort());
        assertTrue("8080".equals(listener.getPort()));
    }

    @Test
    public void validTransaction() throws TransactionFailure {
        
        ConfigSupport.apply(new SingleConfigCode<Transport>() {
            public Object run(Transport okToChange) {
                okToChange.setAcceptorThreads("2");
                logger.fine("ID inside the transaction is " + okToChange.getName());
                return null;
            }
        }, listener.findTransport());
        logger.fine("ID outside the transaction is " + listener.getName());
        assertTrue("2".equals(listener.findTransport().getAcceptorThreads()));
    }    
}
