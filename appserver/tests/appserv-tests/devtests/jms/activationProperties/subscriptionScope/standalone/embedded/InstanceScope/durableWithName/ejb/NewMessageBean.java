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

import java.util.logging.*;
import jakarta.annotation.Resource;
import jakarta.ejb.*;
import jakarta.inject.Inject;
import jakarta.jms.*;

@MessageDriven(mappedName = "jms/jms_unit_test_Topic", activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Topic"),
    @ActivationConfigProperty(propertyName = "subscriptionScope", propertyValue = "Instance"),
    @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "xyz"),
    @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
})
public class NewMessageBean implements MessageListener {
    private static final Logger logger = Logger.getLogger(NewMessageBean.class.getName());

    @Resource
    private MessageDrivenContext mdc;

    @Resource(mappedName = "jms/jms_unit_result_Queue")
    private Queue resultQueue;

    @Resource(mappedName = "jms/jms_unit_test_QCF")
    private QueueConnectionFactory qconFactory;

    public NewMessageBean() {
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message message) {
        sendMsg(message);
    }

    private void sendMsg(Message msg) {
        QueueConnection qcon = null;
        QueueSession qsession = null;
        QueueSender qsender = null;
        try {
            qcon = qconFactory.createQueueConnection();
            qcon.start();
            qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            qsender = qsession.createSender(resultQueue);
            TextMessage message = qsession.createTextMessage();
            message.setText("Hello World!");
            qsender.send(message);
        } catch (Exception e) {
            throw new EJBException(e);
        } finally {
            try {
                if (qsender != null)
                    qsender.close();
                if (qsession != null)
                    qsession.close();
                if (qcon != null)
                    qcon.close();
            } catch (Exception e) {
                throw new EJBException(e);
            }
        }
    }
}
