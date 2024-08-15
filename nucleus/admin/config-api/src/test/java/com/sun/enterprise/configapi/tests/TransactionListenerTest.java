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

import com.sun.enterprise.config.serverbeans.HttpService;

import jakarta.inject.Inject;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.grizzly.config.dom.Http;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.TransactionListener;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * User: dochez
 * Date: Jan 23, 2008
 * Time: 10:48:55 PM
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class TransactionListenerTest {

    @Inject
    private ServiceLocator locator;
    @Inject
    private Logger logger;
    @Inject
    private HttpService httpService;
    private List<PropertyChangeEvent> events;


    @Test
    public void transactionEvents() throws Exception, TransactionFailure {
        final NetworkConfig networkConfig = locator.getService(NetworkConfig.class);
        final NetworkListener netListener = networkConfig.getNetworkListeners().getNetworkListener().get(0);
        final Http http = netListener.findHttpProtocol().getHttp();
        final TransactionListener listener = new TransactionListener() {

            @Override
            public void transactionCommited(List<PropertyChangeEvent> changes) {
                events = changes;
            }


            @Override
            public void unprocessedTransactedEvents(List<UnprocessedChangeEvents> changes) {
            }
        };

        Transactions transactions = locator.getService(Transactions.class);

        try {
            transactions.addTransactionsListener(listener);
            assertNotNull(httpService);
            logger.fine("Max connections = " + http.getMaxConnections());
            SingleConfigCode<Http> configCode = httpConfig -> {
                httpConfig.setMaxConnections("500");
                return null;
            };
            ConfigSupport.apply(configCode, http);
            assertTrue("500".equals(http.getMaxConnections()));

            transactions.waitForDrain();

            assertNotNull(events);
            assertThat(events, hasSize(1));
            PropertyChangeEvent event = events.iterator().next();
            assertAll(
                () -> assertEquals("max-connections", event.getPropertyName()),
                () -> assertEquals("500", event.getNewValue().toString()),
                () -> assertEquals("250", event.getOldValue().toString())
            );
        } finally {
            transactions.removeTransactionsListener(listener);
        }

        // put back the right values in the domain to avoid test collisions
        SingleConfigCode<Http> configCode = httpConfig -> {
            httpConfig.setMaxConnections("250");
            return null;
        };
        ConfigSupport.apply(configCode, http);
    }
}
