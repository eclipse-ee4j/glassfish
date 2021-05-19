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

import com.sun.enterprise.config.serverbeans.HttpService;
import org.glassfish.grizzly.config.dom.Http;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.TransactionListener;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

import java.beans.PropertyChangeEvent;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Jan 23, 2008
 * Time: 10:48:55 PM
 */
public class TransactionListenerTest extends ConfigApiTest {
    public String getFileName() {
        return "DomainTest";
    }

    HttpService httpService = null;
    List<PropertyChangeEvent> events = null;

    @Test
    public void transactionEvents() throws Exception, TransactionFailure {
        httpService = getHabitat().getService(HttpService.class);
        NetworkConfig networkConfig = getHabitat().getService(NetworkConfig.class);
        final NetworkListener netListener = networkConfig.getNetworkListeners()
            .getNetworkListener().get(0);
        final Http http = netListener.findHttpProtocol().getHttp();
        final TransactionListener listener = new TransactionListener() {
                public void transactionCommited(List<PropertyChangeEvent> changes) {
                    events = changes;
                }

            public void unprocessedTransactedEvents(List<UnprocessedChangeEvents> changes) {
            }
        };

        Transactions transactions = getHabitat().getService(Transactions.class);

        try {
            transactions.addTransactionsListener(listener);
            assertTrue(httpService!=null);
            logger.fine("Max connections = " + http.getMaxConnections());
            ConfigSupport.apply(new SingleConfigCode<Http>() {

                public Object run(Http param) {
                    param.setMaxConnections("500");
                    return null;
                }
            }, http);
            assertTrue("500".equals(http.getMaxConnections()));

            transactions.waitForDrain();

            assertTrue(events!=null);
            logger.fine("Number of events " + events.size());
            assertTrue(events.size()==1);
            PropertyChangeEvent event = events.iterator().next();
            assertTrue("max-connections".equals(event.getPropertyName()));
            assertTrue("500".equals(event.getNewValue().toString()));
            assertTrue("250".equals(event.getOldValue().toString()));
        } catch(Exception t) {
            t.printStackTrace();
            throw t;
        }finally {
            transactions.removeTransactionsListener(listener);
        }

        // put back the right values in the domain to avoid test collisions
        ConfigSupport.apply(new SingleConfigCode<Http>() {

            public Object run(Http param) {
                param.setMaxConnections("250");
                return null;
            }
        }, http);

    }
}
