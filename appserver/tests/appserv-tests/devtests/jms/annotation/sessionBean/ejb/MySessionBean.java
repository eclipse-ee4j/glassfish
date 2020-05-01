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

import jakarta.annotation.Resource;
import jakarta.ejb.EJBException;
import jakarta.ejb.Stateless;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConnectionFactoryDefinition;
import jakarta.jms.JMSConnectionFactoryDefinitions;
import jakarta.jms.JMSDestinationDefinition;
import jakarta.jms.JMSDestinationDefinitions;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;
import javax.naming.InitialContext;

@JMSConnectionFactoryDefinitions(
    value = {
        @JMSConnectionFactoryDefinition(
            description = "global-scope CF defined by @JMSConnectionFactoryDefinition",
            name = "java:global/env/annotation_CF",
            interfaceName = "jakarta.jms.ConnectionFactory",
            resourceAdapter = "jmsra",
            user = "admin",
            password = "admin",
            properties = {"org.glassfish.connector-connection-pool.transaction-support=XATransaction" },
            minPoolSize = 0
        ),

        @JMSConnectionFactoryDefinition(
            description = "application-scope resource defined by @JMSConnectionFactoryDefinition",
            name = "java:app/env/annotation_CF",
            interfaceName = "jakarta.jms.ConnectionFactory",
            resourceAdapter = "jmsra",
            user = "admin",
            password = "admin",
            properties = {"org.glassfish.connector-connection-pool.transaction-support=XATransaction"},
            minPoolSize = 0
        ),

        @JMSConnectionFactoryDefinition(
            description = "module-scope resource defined by @JMSConnectionFactoryDefinition",
            name = "java:module/env/annotation_CF",
            interfaceName = "jakarta.jms.ConnectionFactory",
            resourceAdapter = "jmsra",
            user = "admin",
            password = "admin",
            properties = {"org.glassfish.connector-connection-pool.transaction-support=XATransaction"},
            minPoolSize = 0
        ),

        @JMSConnectionFactoryDefinition(
            description = "component-scope resource defined by @JMSConnectionFactoryDefinition",
            name = "java:comp/env/annotation_CF",
            interfaceName = "jakarta.jms.ConnectionFactory",
            resourceAdapter = "jmsra",
            user = "admin",
            password = "admin",
            properties = {"org.glassfish.connector-connection-pool.transaction-support=XATransaction"},
            minPoolSize = 0
        )
    }
)

@JMSDestinationDefinitions(
    value = {
        @JMSDestinationDefinition(
            description = "global-scope queue defined by @JMSDestinationDefinition",
            name = "java:global/env/annotation_queue",
            interfaceName = "jakarta.jms.Queue",
            resourceAdapter = "jmsra",
            destinationName = "myPhysicalQueue"
        ),

        @JMSDestinationDefinition(
            description = "application-scope topic defined by @JMSDestinationDefinition",
            name = "java:app/env/annotation_topic",
            interfaceName = "jakarta.jms.Topic",
            resourceAdapter = "jmsra",
            destinationName = "myPhysicalTopic"
        ),
        @JMSDestinationDefinition(
            description = "module-scope topic defined by @JMSDestinationDefinition",
            name = "java:module/env/annotation_topic",
            interfaceName = "jakarta.jms.Topic",
            resourceAdapter = "jmsra",
            destinationName = "myPhysicalTopic"
        ),

        @JMSDestinationDefinition(
            description = "component-scope queue defined by @JMSDestinationDefinition",
            name = "java:comp/env/annotation_queue",
            interfaceName = "jakarta.jms.Queue",
            resourceAdapter = "jmsra",
            destinationName = "myPhysicalQueue"
        )
    }
)

@Stateless(mappedName="MySessionBean/remote")
public class MySessionBean implements MySessionBeanRemote {
    @Resource(name = "myCF1", lookup = "java:global/env/annotation_CF")
    private ConnectionFactory cf1;

    @Resource(name = "myCF2", lookup = "java:app/env/annotation_CF")
    private ConnectionFactory cf2;

    @Resource(name = "myCF3", lookup = "java:module/env/annotation_CF")
    private ConnectionFactory cf3;

    @Resource(name = "myCF4", lookup = "java:comp/env/annotation_CF")
    private ConnectionFactory cf4;

    @Resource(mappedName = "java:global/env/annotation_queue")
    private Queue queue1;

    @Resource(mappedName = "java:app/env/annotation_topic")
    private Topic topic1;

    @Resource(mappedName = "java:module/env/annotation_topic")
    private Topic topic2;

    @Resource(mappedName = "java:comp/env/annotation_queue")
    private Queue queue2;

    @Override
    public void sendMessage(String text) {
        if (cf1 == null || cf2 == null || cf3 == null || cf4 == null) {
            throw new RuntimeException("Failed to lookup up jms connection factory resources.");
        }
        if (queue1 == null || queue2 == null || topic1 == null || topic2 == null) {
            throw new RuntimeException("Failed to lookup up jms destination resources.");
        }

        Connection conn = null;
        Session session = null;
        try {
            conn = cf3.createConnection();
            conn.start();
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            TextMessage message = session.createTextMessage(text);
            MessageProducer producer = session.createProducer(queue2);
            producer.send(message);
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

    @Override
    public boolean checkMessage(String text) {
        Connection conn = null;
        Session session = null;
        try {
            conn = cf2.createConnection();
            conn.start();
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer r = session.createConsumer(queue2);
            Message message = r.receive(30000L);
            if (message instanceof TextMessage) {
                String content = ((TextMessage) message).getText();
                if (text.equals(content))
                    return true;
            }
            return false;
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
