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

package org.glassfish.test.jms.activationproperties.ejb;

import java.util.logging.Logger;
import jakarta.annotation.Resource;
import jakarta.ejb.*;
import jakarta.inject.Inject;
import jakarta.jms.*;

/**
 *
 * @author LILIZHAO
 */
@Remote
@Stateless(mappedName="MySessionBean/remote")
public class MySessionBean implements MySessionBeanRemote {
    @Resource(mappedName = "jms/jms_unit_test_Topic")
    private Topic topic;

    @Resource(mappedName = "jms/jms_unit_result_Queue")
    private Queue resultQueue;

    @Resource(mappedName = "jms/jms_unit_test_QCF")
    private ConnectionFactory myQueueFactory;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    @Resource
    EJBContext ctx;

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void sendMessage(String text) {
        try {
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage(text);
            producer.send(topic, msg);
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String checkMessage(String text) {
        Connection conn = null;
        Session session = null;
        int count = 0;
        int expectedCount = 4;
        try {
            conn = myQueueFactory.createConnection();
            conn.start();
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageConsumer consumer = session.createConsumer(resultQueue);
            TextMessage msg = null;
            while (count < expectedCount) {
                msg = (TextMessage) consumer.receive(300000);
                if (msg != null)
                    count++;
                else
                    break;
            }
        } catch (Exception e) {
            throw new EJBException(e);
        } finally {
            try {
                if (session != null)
                    session.close();
                if (conn != null)
                    conn.close();
            } catch(Exception e) {
                throw new EJBException(e);
            }
        }
        if (count != expectedCount) {
            String message = expectedCount + " messages are expected, but got " + count;
            Logger.getLogger("MySessionBean").severe(message);
            return "Fail: " + message;
        }
        return "Success: count=" + count;
    }
}
