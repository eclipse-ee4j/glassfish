/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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
import org.jvnet.hk2.config.types.Property;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.jvnet.hk2.config.Changed;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.NotProcessed;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.TransactionListener;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Jerome Dochez
 * Date: Mar 25, 2008
 * Time: 1:32:35 PM
 */
public class AddPropertyTest extends ConfigApiTest {

    public String getFileName() {
        return "DomainTest";
    }

    List<PropertyChangeEvent> events = null;

    @Test
    public void transactionEvents() throws TransactionFailure {
        final Domain domain = getHabitat().getService(Domain.class);
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
            assertTrue(domain!=null);

            ConfigSupport.apply(new SingleConfigCode<Domain>() {

                public Object run(Domain domain) throws PropertyVetoException, TransactionFailure {
                    Property prop = domain.createChild(Property.class);
                    domain.getProperty().add(prop);
                    prop.setName("Jerome");
                    prop.setValue("was here");
                    return prop;
                }
            }, domain);
            transactions.waitForDrain();

            assertTrue(events!=null);
            logger.fine("Number of events " + events.size());
            assertTrue(events.size()==3);
            for (PropertyChangeEvent event : events) {
                logger.fine(event.toString());
            }

            Map<String, String> configChanges = new HashMap<String, String>();
            configChanges.put("name", "julien");
            configChanges.put("value", "petit clown");
            ConfigBean domainBean = (ConfigBean) Dom.unwrap(domain);
            ConfigSupport.createAndSet(domainBean, Property.class, configChanges);


            transactions.waitForDrain();

            assertTrue(events!=null);
            logger.fine("Number of events " + events.size());
            assertTrue(events.size()==3);
            for (PropertyChangeEvent event : events) {
                logger.fine(event.toString());
            }

            final UnprocessedChangeEvents unprocessed =
                ConfigSupport.sortAndDispatch(events.toArray(new PropertyChangeEvent[0]), new Changed() {
                /**
                 * Notification of a change on a configuration object
                 *
                 * @param type            type of change : ADD mean the changedInstance was added to the parent
                 *                        REMOVE means the changedInstance was removed from the parent, CHANGE means the
                 *                        changedInstance has mutated.
                 * @param changedType     type of the configuration object
                 * @param changedInstance changed instance.
                 */
                public <T extends ConfigBeanProxy> NotProcessed changed(TYPE type, Class<T> changedType, T changedInstance) {
                    return new NotProcessed("unimplemented by AddPropertyTest");
                }
            }, logger);
        } finally {
            transactions.removeTransactionsListener(listener);
        }
    }
}
