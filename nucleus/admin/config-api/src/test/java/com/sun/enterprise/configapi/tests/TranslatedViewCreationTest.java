/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.beans.PropertyVetoException;
import java.util.List;

import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import org.glassfish.config.support.GlassFishConfigBean;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.tests.utils.Utils;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.TransactionListener;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
 * User: Jerome Dochez
 * Date: Jun 24, 2008
 * Time: 8:27:29 PM
 */
public class TranslatedViewCreationTest extends ConfigApiTest {

    final static String propName = "com.sun.my.chosen.docroot";

    public String getFileName() {
        return "DomainTest";
    }

    HttpService httpService = null;
    List<PropertyChangeEvent> events;
    ServiceLocator habitat;

    @Before
    public void setup() {
        System.setProperty(propName, "/foo/bar/docroot");
        habitat = Utils.instance.getHabitat(this);

    }

    @Override
    public ServiceLocator getBaseServiceLocator() {
        return habitat;
    }

    @Test
    public void createVirtualServerTest() throws TransactionFailure {
        httpService = getHabitat().getService(HttpService.class);
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
            ConfigSupport.apply(new SingleConfigCode<HttpService>() {

                public Object run(HttpService param) throws PropertyVetoException, TransactionFailure {
                    VirtualServer newVirtualServer = param.createChild(VirtualServer.class);
                    newVirtualServer.setDocroot("${"+propName+"}");
                    newVirtualServer.setId("translated-view-creation");
                    param.getVirtualServer().add(newVirtualServer);
                    return null;
                }
            }, httpService);

            // first let check that our new virtual server has the right translated value
            VirtualServer vs = httpService.getVirtualServerByName("translated-view-creation");
            assertTrue(vs!=null);
            String docRoot = vs.getDocroot();
            assertTrue("/foo/bar/docroot".equals(docRoot));

            transactions.waitForDrain();

            assertTrue(events!=null);
            logger.fine("Number of events " + events.size());
            assertTrue(events.size()==3);
            for (PropertyChangeEvent event : events) {
                if ("virtual-server".equals(event.getPropertyName())) {
                    VirtualServer newVS = (VirtualServer) event.getNewValue();
                    assertTrue(event.getOldValue()==null);
                    docRoot = newVS.getDocroot();
                    assertTrue("/foo/bar/docroot".equals(docRoot));

                    VirtualServer rawView = GlassFishConfigBean.getRawView(newVS);
                    assertTrue(rawView!=null);
                    assertTrue(rawView.getDocroot().equalsIgnoreCase("${" + propName + "}"));
                    return;
                }
            }
            assertTrue(false);

        } finally {
            transactions.removeTransactionsListener(listener);
        }

    }

    @After
    public void tearDown() {
        System.setProperty(propName, "");
    }
}
