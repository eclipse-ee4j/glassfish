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

import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.grizzly.config.dom.Transport;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Test the persistence to a file...
 */
public class PersistenceTest extends ConfigPersistence {

    public String getFileName() {
        return "DomainTest";
    }

    public void doTest() throws TransactionFailure {
        NetworkListeners service = getHabitat().getService(NetworkListeners.class);
        // now do a transaction

        ConfigSupport.apply(new SingleConfigCode<Transport>() {
            public Object run(Transport param) {
                param.setAcceptorThreads("8989");
                return null;
            }
        }, service.getNetworkListener().get(0).findTransport());
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean assertResult(String s) {
        return s.contains("acceptor-threads=\"8989\"");
    }
}
