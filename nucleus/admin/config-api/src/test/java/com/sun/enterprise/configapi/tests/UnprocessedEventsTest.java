/*
 * Copyright (c) 2009, 2021 Oracle and/or its affiliates. All rights reserved.
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

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.tests.utils.Utils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.TransactionListener;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvent;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
 * @author Jerome Dochez
 */
public class UnprocessedEventsTest  extends ConfigApiTest
        implements ConfigListener, TransactionListener {

    ServiceLocator habitat = Utils.instance.getHabitat(this);
    UnprocessedChangeEvents unprocessed = null;

    /**
     * Returns the DomainTest file name without the .xml extension to load the test configuration
     * from.
     *
     * @return the configuration file name
     */
    public String getFileName() {
        return "DomainTest";
    }

    @Test
     public void unprocessedEventsTest() throws TransactionFailure {

        // let's find our target
        NetworkConfig service = habitat.getService(NetworkConfig.class);
        NetworkListener listener = service.getNetworkListener("http-listener-1");
        assertNotNull(listener);

        // Let's register a listener
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(listener);
        bean.addListener(this);
        Transactions transactions = getHabitat().getService(Transactions.class);

        try {
            transactions.addTransactionsListener(this);

            ConfigSupport.apply(new SingleConfigCode<NetworkListener>() {
                public Object run(NetworkListener param) {
                    param.setPort("8908");
                    return null;
                }
            }, listener);

            // check the result.
            String port = listener.getPort();
            assertEquals(port, "8908");

            // ensure events are delivered.
            transactions.waitForDrain();
            assertNotNull(unprocessed);
            
            ConfigSupport.apply(new SingleConfigCode<NetworkListener>() {
                public Object run(NetworkListener param) {
                    param.setPort("8080");
                    return null;
                }
            }, listener);
                        
            assertEquals(listener.getPort(), "8080");

            // ensure events are delivered.
            transactions.waitForDrain();
            assertNotNull(unprocessed);

            // finally
            bean.removeListener(this);
        } finally {

            // check we recevied the event
            transactions.removeTransactionsListener(this);
        }

    }

    public UnprocessedChangeEvents changed(PropertyChangeEvent[] propertyChangeEvents) {
        assertEquals("Array size", propertyChangeEvents.length, 1 );
        
        final UnprocessedChangeEvent unp = new UnprocessedChangeEvent(
            propertyChangeEvents[0], "Java NIO port listener cannot reconfigure its port dynamically" );
        unprocessed = new UnprocessedChangeEvents( unp );
        return unprocessed;
    }

    public void transactionCommited(List<PropertyChangeEvent> changes) {
        // don't care...
    }

    public void unprocessedTransactedEvents(List<UnprocessedChangeEvents> changes) {
        assertTrue(changes.size()==1);
    }
}
