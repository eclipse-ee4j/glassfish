/*
 * Copyright (c) 2009, 2021 Oracle and/or its affiliates. All rights reserved.
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

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.tests.utils.Utils;
import org.junit.jupiter.api.Test;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.TransactionListener;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvent;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Jerome Dochez
 */
public class UnprocessedEventsTest extends ConfigApiTest implements ConfigListener, TransactionListener {

    private final ServiceLocator habitat = Utils.instance.getHabitat(this);
    private UnprocessedChangeEvents unprocessed;

    /**
     * Returns the DomainTest file name without the .xml extension to load the test configuration
     * from.
     *
     * @return the configuration file name
     */
    @Override
    public String getFileName() {
        return "DomainTest";
    }

    @Test
     public void unprocessedEventsTest() throws TransactionFailure {

        // let's find our target
        NetworkConfig service = habitat.getService(NetworkConfig.class);
        NetworkListener networkListener = service.getNetworkListener("http-listener-1");
        assertNotNull(networkListener);

        // Let's register a listener
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(networkListener);
        bean.addListener(this);
        Transactions transactions = getHabitat().getService(Transactions.class);

        try {
            transactions.addTransactionsListener(this);

            SingleConfigCode<NetworkListener> configCode = listener -> {
                listener.setPort("8908");
                return null;
            };
            ConfigSupport.apply(configCode, networkListener);

            // check the result.
            String port = networkListener.getPort();
            assertEquals(port, "8908");

            // ensure events are delivered.
            transactions.waitForDrain();
            assertNotNull(unprocessed);

            SingleConfigCode<NetworkListener> configCodeReset = listener -> {
                listener.setPort("8080");
                return null;
            };
            ConfigSupport.apply(configCodeReset, networkListener);
            assertEquals(networkListener.getPort(), "8080");

            // ensure events are delivered.
            transactions.waitForDrain();
            assertNotNull(unprocessed);
            bean.removeListener(this);
        } finally {
            transactions.removeTransactionsListener(this);
        }

    }

    @Override
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] propertyChangeEvents) {
        assertEquals(propertyChangeEvents.length, 1, "Array size");
        final UnprocessedChangeEvent unp = new UnprocessedChangeEvent(
            propertyChangeEvents[0], "Java NIO port listener cannot reconfigure its port dynamically" );
        unprocessed = new UnprocessedChangeEvents( unp );
        return unprocessed;
    }

    @Override
    public void transactionCommited(List<PropertyChangeEvent> changes) {
        // don't care...
    }

    @Override
    public void unprocessedTransactedEvents(List<UnprocessedChangeEvents> changes) {
        assertThat(changes, hasSize(1));
    }
}
