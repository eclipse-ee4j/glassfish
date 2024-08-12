/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.TransactionListener;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvents;
import org.jvnet.hk2.config.types.Property;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * User: Jerome Dochez
 * Date: Mar 25, 2008
 * Time: 1:32:35 PM
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class AddPropertyTest {

    @Inject
    private ServiceLocator locator;
    private List<PropertyChangeEvent> events;


    @Test
    public void transactionEvents() throws TransactionFailure {
        final Domain domain = locator.getService(Domain.class);
        assertNotNull(domain);
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

            SingleConfigCode<Domain> configCode = (SingleConfigCode<Domain>) domain1 -> {
                Property prop = domain1.createChild(Property.class);
                domain1.getProperty().add(prop);
                prop.setName("Jerome");
                prop.setValue("was here");
                return prop;
            };
            ConfigSupport.apply(configCode, domain);
            transactions.waitForDrain();

            assertNotNull(events);
            assertThat(events, hasSize(3));

            Map<String, String> configChanges = new HashMap<>();
            configChanges.put("name", "julien");
            configChanges.put("value", "petit clown");
            ConfigBean domainBean = (ConfigBean) Dom.unwrap(domain);
            ConfigSupport.createAndSet(domainBean, Property.class, configChanges);


            transactions.waitForDrain();

            assertNotNull(events);
            assertThat(events, hasSize(3));
        } finally {
            transactions.removeTransactionsListener(listener);
        }
    }
}
