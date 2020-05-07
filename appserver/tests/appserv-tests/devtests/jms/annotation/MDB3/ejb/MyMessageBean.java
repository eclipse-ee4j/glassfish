/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.test.jms.annotation.ejb;

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.Resource;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.EJBException;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConnectionFactory;
import jakarta.jms.JMSConnectionFactoryDefinition;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSDestinationDefinition;
import jakarta.jms.JMSException;
import jakarta.jms.JMSProducer;
import jakarta.jms.JMSSessionMode;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;

@JMSConnectionFactoryDefinition(
    description = "global-scope CF defined by @JMSConnectionFactoryDefinition",
    name = "java:global/env/annotation_CF",
    interfaceName = "jakarta.jms.ConnectionFactory",
    resourceAdapter = "jmsra",
    user = "admin",
    password = "admin",
    properties = {"org.glassfish.connector-connection-pool.transaction-support=XATransaction"},
    minPoolSize = 0
)

@JMSDestinationDefinition(
    description = "global-scope test queue defined by @JMSDestinationDefinition",
    name = "java:global/env/annotation_testQueue",
    interfaceName = "jakarta.jms.Queue",
    resourceAdapter = "jmsra",
    destinationName = "myPhysicalTestQueue"
)

@MessageDriven(mappedName = "java:global/env/annotation_testQueue", activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
    @ActivationConfigProperty(propertyName = "endpointExceptionRedeliveryAttempts", propertyValue = "1")
})
public class MyMessageBean implements MessageListener {
    private static final Logger logger = Logger.getLogger(MyMessageBean.class.getName());

    @Resource(mappedName = "java:global/env/annotation_resultQueue")
    private Queue resultQueue;

    @Resource(mappedName = "java:global/env/annotation_CF")
    private ConnectionFactory myConnectionFactory;

    @Inject
    @JMSConnectionFactory("java:global/env/annotation_CF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    public MyMessageBean() {
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                sendMessage(true, ((TextMessage) message).getText());
            } else {
                sendMessage(false, "The received message is not a expected TextMessage.");
            }
        } catch (JMSException ex) {
            Logger.getLogger(MyMessageBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new EJBException(ex);
        }
    }

    private void sendMessage(boolean success, String text) {
        JMSProducer producer = jmsContext.createProducer();
        TextMessage message = jmsContext.createTextMessage(success + ":" + text);
        producer.send(resultQueue, message);
    }
}
