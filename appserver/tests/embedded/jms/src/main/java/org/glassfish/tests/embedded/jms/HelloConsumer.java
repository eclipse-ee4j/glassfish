/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.tests.embedded.jms;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;

import java.util.logging.Level;
import java.util.logging.Logger;

@MessageDriven(
        activationConfig = {
                @ActivationConfigProperty(
                        propertyName = "destinationType",
                        propertyValue = "jakarta.jms.Queue"),
                @ActivationConfigProperty(
                        propertyName = "destinationLookup",
                        propertyValue = "java:comp/jms/HelloQueue")
        }
)
public class HelloConsumer implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(HelloConsumer.class.getName());

    @Inject
    HelloHandler handler;

    @Override
    public void onMessage(Message message) {
        try {
            String body = message.getBody(String.class);
            LOGGER.log(Level.INFO, "received message: {0}", body);
            handler.handle(body);
        } catch (JMSException e) {
            LOGGER.log(Level.SEVERE, "Error handling JMS message", e);
        }
    }
}
