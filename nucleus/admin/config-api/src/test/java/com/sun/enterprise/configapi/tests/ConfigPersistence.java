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

import jakarta.inject.Inject;

import java.beans.PropertyChangeEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.config.support.ConfigurationPersistence;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.IndentingXMLStreamWriter;
import org.jvnet.hk2.config.TransactionListener;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
 * User: Jerome Dochez
 * Date: Mar 25, 2008
 * Time: 11:36:46 AM
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public abstract class ConfigPersistence {

    @Inject
    protected ServiceLocator locator;
    @Inject
    private DomDocument<?> document;
    @Inject
    private Logger logger;

    public abstract void doTest() throws Exception;

    public abstract void assertResult(String resultingXml);


    @Test
    public void test() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.reset();

        final ConfigurationPersistence testPersistence = doc -> {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = factory.createXMLStreamWriter(baos);
            try {
                doc.writeTo(new IndentingXMLStreamWriter(writer));
            } finally {
                writer.close();
            }
        };

        TransactionListener testListener = new TransactionListener() {

            @Override
            public void transactionCommited(List<PropertyChangeEvent> changes) {
                try {
                    testPersistence.save(document);
                } catch (IOException | XMLStreamException e) {
                    throw new IllegalStateException(e);
                }
            }


            @Override
            public void unprocessedTransactedEvents(List<UnprocessedChangeEvents> changes) {
            }
        };
        Transactions transactions = locator.getService(Transactions.class);

        try {
            transactions.addTransactionsListener(testListener);
            doTest();
        } finally {
            transactions.waitForDrain();
            transactions.removeTransactionsListener(testListener);
        }

        // now check if we persisted correctly...
        final String resultingXml = baos.toString();
        logger.fine(resultingXml);
        assertResult(resultingXml);
    }
}
