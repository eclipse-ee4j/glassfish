/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.enterprise.config.serverbeans.VirtualServer;

import jakarta.inject.Inject;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.config.support.GlassFishConfigBean;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * User: Jerome Dochez
 * Date: Jun 24, 2008
 * Time: 8:27:29 PM
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class TranslatedViewCreationTest {

    private static final String propName = "com.sun.my.chosen.docroot";

    @Inject
    private ServiceLocator locator;
    @Inject
    private HttpService httpService;
    private List<PropertyChangeEvent> events;

    @BeforeEach
    public void setup() {
        System.setProperty(propName, "/foo/bar/docroot");
    }

    @AfterEach
    public void tearDown() {
        System.clearProperty(propName);
    }

    @Test
    public void createVirtualServerTest() throws TransactionFailure {
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
            SingleConfigCode<HttpService> configCode = http -> {
                VirtualServer newVirtualServer = http.createChild(VirtualServer.class);
                newVirtualServer.setDocroot("${" + propName + "}");
                newVirtualServer.setId("translated-view-creation");
                http.getVirtualServer().add(newVirtualServer);
                return null;
            };
            ConfigSupport.apply(configCode, httpService);

            // first let check that our new virtual server has the right translated value
            VirtualServer vs = httpService.getVirtualServerByName("translated-view-creation");
            assertNotNull(vs);
            String docRoot = vs.getDocroot();
            assertEquals("/foo/bar/docroot", docRoot);

            transactions.waitForDrain();

            assertNotNull(events);
            assertThat(events, hasSize(3));
            for (PropertyChangeEvent event : events) {
                if ("virtual-server".equals(event.getPropertyName())) {
                    VirtualServer newVS = (VirtualServer) event.getNewValue();
                    assertNull(event.getOldValue());
                    docRoot = newVS.getDocroot();
                    assertEquals("/foo/bar/docroot", docRoot);

                    VirtualServer rawView = GlassFishConfigBean.getRawView(newVS);
                    assertNotNull(rawView);
                    assertEquals("${" + propName + "}", rawView.getDocroot());
                    return;
                }
            }
            fail("virtual-server event wasn't found");
        } finally {
            transactions.removeTransactionsListener(listener);
        }

    }
}
