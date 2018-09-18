/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.configapi.tests.typedlistener;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.configapi.tests.ConfigApiTest;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.jvnet.hk2.config.*;

/**
 * Test the listeners per type registration/events/un-registration.
 * @author Jerome Dochez
 */
public class TypedListenerTest extends ConfigApiTest {

    List<PropertyChangeEvent> events = null;
    AtomicInteger listenersInvoked = new AtomicInteger();

    @Override
    public String getFileName() {
        return "DomainTest";
    }

    @Test
    public void addElementTest() throws TransactionFailure {

        final Domain domain = getHabitat().getService(Domain.class);
        final ConfigListener configListener = new ConfigListener() {
            @Override
            public UnprocessedChangeEvents changed(PropertyChangeEvent[] propertyChangeEvents) {
                events = Arrays.asList(propertyChangeEvents);
                return null;
            }
        };

        Transactions transactions = getHabitat().getService(Transactions.class);

        try {
            transactions.addListenerForType(SystemProperty.class, configListener);

            assertTrue(domain!=null);

            // adding
            ConfigSupport.apply(new SingleConfigCode<Domain>() {

                        @Override
                        public Object run(Domain domain) throws PropertyVetoException, TransactionFailure {
                            SystemProperty prop = domain.createChild(SystemProperty.class);
                            domain.getSystemProperty().add(prop);
                            prop.setName("Jerome");
                            prop.setValue("was here");
                            return prop;
                        }
                    }, domain);
            transactions.waitForDrain();

            assertTrue(events!=null);
            logger.log(Level.FINE, "Number of events {0}", events.size());
            assertTrue(events.size()==3);
            for (PropertyChangeEvent event : events) {
                logger.fine(event.toString());
            }
            events = null;

            // modification
            for (SystemProperty prop : domain.getSystemProperty()) {
                if (prop.getName().equals("Jerome")) {
                    ConfigSupport.apply(new SingleConfigCode<SystemProperty>() {
                                @Override
                                public Object run(SystemProperty param) throws PropertyVetoException, TransactionFailure {
                                    param.setValue("was also here");
                                    return null;
                                }
                            },prop);
                    break;
                }
            }
            assertTrue(events!=null);
            logger.log(Level.FINE, "Number of events {0}", events.size());
            assertTrue(events.size()==1);
            for (PropertyChangeEvent event : events) {
                logger.fine(event.toString());
            }

            events = null;

            // removal
            assertNotNull(ConfigSupport.apply(new SingleConfigCode<Domain>() {

                        @Override
                        public Object run(Domain domain) throws PropertyVetoException, TransactionFailure {
                            for (SystemProperty prop : domain.getSystemProperty()) {
                                if (prop.getName().equals("Jerome")) {
                                    domain.getSystemProperty().remove(prop);
                                    return prop;
                                }
                            }
                            return null;
                        }
                    }, domain));
            transactions.waitForDrain();

            assertTrue(events!=null);
            logger.log(Level.FINE, "Number of events {0}", events.size());
            assertTrue(events.size()==1);
            for (PropertyChangeEvent event : events) {
                logger.fine(event.toString());
            }
        } finally {
            assertTrue(transactions.removeListenerForType(SystemProperty.class, configListener));
        }
    }

    @Test
    public void multipleListeners() throws TransactionFailure {
        final Domain domain = getHabitat().getService(Domain.class);
        final ConfigListener configListener1 = new ConfigListener() {
            @Override
            public UnprocessedChangeEvents changed(PropertyChangeEvent[] propertyChangeEvents) {
                listenersInvoked.incrementAndGet();
                return null;
            }
        };
        final ConfigListener configListener2 = new ConfigListener() {
            @Override
            public UnprocessedChangeEvents changed(PropertyChangeEvent[] propertyChangeEvents) {
                listenersInvoked.incrementAndGet();
                return null;
            }
        };

        Transactions transactions = getHabitat().getService(Transactions.class);

        try {
            transactions.addListenerForType(SystemProperty.class, configListener1);
            transactions.addListenerForType(SystemProperty.class, configListener2);

            assertTrue(domain!=null);

            // adding
            ConfigSupport.apply(new SingleConfigCode<Domain>() {

                        @Override
                        public Object run(Domain domain) throws PropertyVetoException, TransactionFailure {
                            SystemProperty prop = domain.createChild(SystemProperty.class);
                            domain.getSystemProperty().add(prop);
                            prop.setName("Jerome");
                            prop.setValue("was here");
                            return prop;
                        }
                    }, domain);
            transactions.waitForDrain();

            assertTrue(listenersInvoked.intValue()==2);
        } finally {
            assertTrue(transactions.removeListenerForType(SystemProperty.class, configListener1));
            assertTrue(transactions.removeListenerForType(SystemProperty.class, configListener2));
        }
    }
}
