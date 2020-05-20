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

package org.glassfish.test.jms.jmsxdeliverycount.client;

import javax.naming.*;
import jakarta.jms.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client2 {
    private final static SimpleReporterAdapter STAT = new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        STAT.addDescription("JMSXDeliveryCount-acc1-2");
        Client2 client2 = new Client2(args);
        client2.doTest();
        STAT.printSummary("JMSXDeliveryCount-acc1-2ID");
    }

    public Client2 (String[] args) {
    }

    public void doTest() {
        try {
            Context ctx = new InitialContext();
            Queue queue = (Queue) ctx.lookup("jms/jms_unit_test_Queue");
            QueueConnectionFactory qconFactory = (QueueConnectionFactory) ctx.lookup("jms/jms_unit_test_QCF");
            QueueConnection qcon = qconFactory.createQueueConnection();
            qcon.start();
            QueueSession qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer qreceiver = qsession.createConsumer(queue);
            TextMessage message = (TextMessage) qreceiver.receive(10000);
            if (message == null) {
                STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                return;
            }
            int deliveryCount = message.getIntProperty("JMSXDeliveryCount");
            if (deliveryCount != 3) {
                STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                return;
            }
            for (int i=1; i<10; i++) {
                message = (TextMessage) qreceiver.receive(10000);
                if (message == null) {
                    STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                    return;
                }
                deliveryCount = message.getIntProperty("JMSXDeliveryCount");
                if (deliveryCount != 1) {
                    STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                    return;
                }
            }
            qcon.close();
            STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
        }
    }
}
