/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.configapi.tests.typedlistener;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SystemProperty;

import jakarta.inject.Inject;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.Transactions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the listeners per type registration/events/un-registration.
 * @author Jerome Dochez
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class TypedListenerTest {

    @Inject
    private ServiceLocator locator;
    @Inject
    private Logger logger;

    private List<PropertyChangeEvent> events;
    private final AtomicInteger listenersInvoked = new AtomicInteger();

    @Test
    public void addElementTest() throws TransactionFailure {
        final Domain domain = locator.getService(Domain.class);
        final ConfigListener configListener = propertyChangeEvents -> {
            events = Arrays.asList(propertyChangeEvents);
            return null;
        };

        Transactions transactions = locator.getService(Transactions.class);
        try {
            transactions.addListenerForType(SystemProperty.class, configListener);

            assertNotNull(domain);

            // adding
            SingleConfigCode<Domain> configCode = domain1 -> {
                SystemProperty sysProp = domain1.createChild(SystemProperty.class);
                domain1.getSystemProperty().add(sysProp);
                sysProp.setName("Jerome");
                sysProp.setValue("was here");
                return sysProp;
            };
            ConfigSupport.apply(configCode, domain);
            transactions.waitForDrain();

            assertThat(events, hasSize(3));
            for (PropertyChangeEvent event : events) {
                logger.fine(event.toString());
            }
            events = null;

            // modification
            for (SystemProperty prop : domain.getSystemProperty()) {
                if ("Jerome".equals(prop.getName())) {
                    SingleConfigCode<SystemProperty> configCode2 = sysProp -> {
                        sysProp.setValue("was also here");
                        return null;
                    };
                    ConfigSupport.apply(configCode2, prop);
                    break;
                }
            }
            assertThat(events, hasSize(1));
            for (PropertyChangeEvent event : events) {
                logger.fine(event.toString());
            }

            events = null;

            // removal
            SingleConfigCode<Domain> configCode2 = domain1 -> {
                for (SystemProperty prop : domain1.getSystemProperty()) {
                    if ("Jerome".equals(prop.getName())) {
                        domain1.getSystemProperty().remove(prop);
                        return prop;
                    }
                }
                return null;
            };
            assertNotNull(ConfigSupport.apply(configCode2, domain));
            transactions.waitForDrain();

            assertThat(events, hasSize(1));
            for (PropertyChangeEvent event : events) {
                logger.fine(event.toString());
            }
        } finally {
            assertTrue(transactions.removeListenerForType(SystemProperty.class, configListener));
        }
    }

    @Test
    public void multipleListeners() throws TransactionFailure {
        final Domain domain = locator.getService(Domain.class);
        assertNotNull(domain);

        final ConfigListener configListener1 = propertyChangeEvents -> {
            listenersInvoked.incrementAndGet();
            return null;
        };
        final ConfigListener configListener2 = propertyChangeEvents -> {
            listenersInvoked.incrementAndGet();
            return null;
        };

        Transactions transactions = locator.getService(Transactions.class);
        try {
            transactions.addListenerForType(SystemProperty.class, configListener1);
            transactions.addListenerForType(SystemProperty.class, configListener2);

            // adding
            SingleConfigCode<Domain> configCode = domain1 -> {
                SystemProperty prop = domain1.createChild(SystemProperty.class);
                domain1.getSystemProperty().add(prop);
                prop.setName("Jerome");
                prop.setValue("was here");
                return prop;
            };
            ConfigSupport.apply(configCode, domain);
            transactions.waitForDrain();

            assertEquals(2, listenersInvoked.intValue());
        } finally {
            assertTrue(transactions.removeListenerForType(SystemProperty.class, configListener1));
            assertTrue(transactions.removeListenerForType(SystemProperty.class, configListener2));
        }
    }
}
