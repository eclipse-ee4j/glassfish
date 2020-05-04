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

package org.glassfish.test.jms.jmsxdeliverycount.ejb;

import java.util.logging.Logger;
import jakarta.annotation.Resource;
import jakarta.ejb.*;
import jakarta.inject.Inject;
import jakarta.jms.*;

/**
 *
 * @author LILIZHAO
 */
@Stateless(mappedName="MySessionBean/remote")
public class MySessionBean implements MySessionBeanRemote {
    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Resource(mappedName = "jms/jms_unit_test_QCF")
    private ConnectionFactory myQueueFactory;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    @Resource
    EJBContext ctx;

    @Override
    public void sendMessage(String text) {
        try {
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage(text);
            producer.send(queue, msg);
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    @Override
    public boolean checkMessage1(String text) {
        Connection conn = null;
        Session session = null;
        try {
            conn = myQueueFactory.createConnection();
            conn.start();
            session = conn.createSession();

            MessageConsumer consumer = session.createConsumer(queue);
            TextMessage msg = (TextMessage) consumer.receive(1000);
            if (msg == null) {
                Logger.getLogger("MySessionBean").severe("No message received 1.");
                return false;
            }
            int deliveryCount = msg.getIntProperty("JMSXDeliveryCount");
            if (deliveryCount != 1) {
                Logger.getLogger("MySessionBean").severe("Invalid JMSXDeliveryCount - Got <" + deliveryCount + ">, but expected <1>.");
                return false;
            }
            ctx.setRollbackOnly();
        } catch (Exception e) {
            throw new EJBException(e);
        } finally {
            try {
                session.close();
            }catch(Exception e) {
                throw new EJBException(e);
            }
        }
        return true;
    }

    @Override
    public boolean checkMessage2(String text) {
        Connection conn = null;
        Session session = null;
        try {
            conn = myQueueFactory.createConnection();
            conn.start();
            session = conn.createSession();

            MessageConsumer consumer = session.createConsumer(queue);
            TextMessage msg = (TextMessage) consumer.receive(1000);
            if (msg == null) {
                Logger.getLogger("MySessionBean").severe("No message received 2.");
                return false;
            }
            int deliveryCount = msg.getIntProperty("JMSXDeliveryCount");
            if (deliveryCount != 2) {
                Logger.getLogger("MySessionBean").severe("Invalid JMSXDeliveryCount - Got <" + deliveryCount + ">, but expected <2>.");
                return false;
            }
        } catch (Exception e) {
            throw new EJBException(e);
        } finally {
            try {
                session.close();
                conn.close();
            }catch(Exception e) {
                throw new EJBException(e);
            }
        }
        return true;
    }
}
