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

import java.util.logging.Logger;
import jakarta.annotation.Resource;
import jakarta.ejb.EJBException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConnectionFactory;
import jakarta.jms.JMSConnectionFactoryDefinition;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSDestinationDefinition;
import jakarta.jms.JMSDestinationDefinitions;
import jakarta.jms.JMSProducer;
import jakarta.jms.JMSSessionMode;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

@JMSConnectionFactoryDefinition(
    description = "module-scope CF defined by @JMSConnectionFactoryDefinition",
    name = "java:module/env/annotation_CF",
    interfaceName = "jakarta.jms.ConnectionFactory",
    resourceAdapter = "jmsra",
    user = "admin",
    password = "admin",
    properties = {"org.glassfish.connector-connection-pool.transaction-support=XATransaction"},
    minPoolSize = 0
)

@JMSDestinationDefinitions(
    value = {
        @JMSDestinationDefinition(
            description = "module-scope test queue defined by @JMSDestinationDefinition",
            name = "java:module/env/annotation_testQueue",
            interfaceName = "jakarta.jms.Queue",
            resourceAdapter = "jmsra",
            destinationName = "myPhysicalTestQueue"
        ),

        @JMSDestinationDefinition(
            description = "module-scope result queue defined by @JMSDestinationDefinition",
            name = "java:module/env/annotation_resultQueue",
            interfaceName = "jakarta.jms.Queue",
            resourceAdapter = "jmsra",
            destinationName = "myPhysicalResultQueue"
        )
    }
)

@Stateless(mappedName="MySessionBean/remote")
public class MySessionBean implements MySessionBeanRemote {
    @Resource(mappedName = "java:module/env/annotation_testQueue")
    private Queue testQueue;

    @Resource(mappedName = "java:module/env/annotation_resultQueue")
    private Queue resultQueue;

    @Resource(mappedName = "java:module/env/annotation_CF")
    private ConnectionFactory myConnectionFactory;

    @Inject
    @JMSConnectionFactory("java:module/env/annotation_CF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    @Override
    public void sendMessage(String text) {
        try {
            JMSProducer producer = jmsContext.createProducer();
            TextMessage message = jmsContext.createTextMessage(text);
            producer.send(testQueue, message);
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    @Override
    public boolean checkMessage(String text) {
        Connection conn = null;
        Session session = null;
        try {
            conn = myConnectionFactory.createConnection();
            conn.start();
            session = conn.createSession();

            MessageConsumer consumer = session.createConsumer(resultQueue);
            TextMessage msg = (TextMessage) consumer.receive(10000);
            if (msg == null) {
                Logger.getLogger("MySessionBean").severe("No result message received.");
                return false;
            } else {
                String result = msg.getText();
                if (result.equals("true:" + text)) {
                    return true;
                } else {
                    String errMsg = result.substring(result.indexOf(":") + 1);
                    Logger.getLogger("MySessionBean").severe(errMsg);
                    return false;
                }
            }
        } catch (JMSException e) {
            throw new EJBException(e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    throw new EJBException(e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (JMSException e) {
                    throw new EJBException(e);
                }
            }
        }
    }
}
