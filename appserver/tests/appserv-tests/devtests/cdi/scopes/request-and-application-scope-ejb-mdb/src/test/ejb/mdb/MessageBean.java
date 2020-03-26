/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package test.ejb.mdb;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.NoSuchEJBException;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.Queue;
import jakarta.jms.QueueConnection;
import jakarta.jms.QueueConnectionFactory;
import jakarta.jms.QueueSender;
import jakarta.jms.QueueSession;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

import test.beans.TestApplicationScopedBean;
import test.beans.TestRequestScopedBean;
import test.ejb.session.Hello1;
import test.ejb.session.Hello2;

@TransactionManagement(TransactionManagementType.BEAN)
@MessageDriven(mappedName = "jms/cdi_hello_mdb_InQueue", description = "mymessagedriven bean description")
public class MessageBean implements MessageListener {

    @EJB
    private Hello1 hello1;
    @EJB
    private Hello2 hello2;

    @Resource(name = "jms/MyQueueConnectionFactory", mappedName = "jms/cdi_hello_mdb_QCF")
    QueueConnectionFactory qcFactory;

    @Resource(mappedName = "jms/cdi_hello_mdb_OutQueue")
    Queue clientQueue;

    @Inject
    TestApplicationScopedBean tasb;
    @Inject
    TestRequestScopedBean trsb;
    
    @Inject BeanManager bm;

    public void onMessage(Message message) {

        QueueConnection connection = null;
        try {

            System.out.println("Calling hello1 stateless bean");
            hello1.hello("local ejb3.0 stateless");
            System.out.println("Calling hello2 stateful bean");
            hello2.hello("local ejb3.0 stateful");
            hello2.removeMethod();
            try {
                hello2.hello("this call should not go through");
                throw new Exception("bean should have been removed "
                        + "after removeMethod()");
            } catch (NoSuchEJBException e) {
                System.out.println("Successfully caught EJBException after "
                        + " accessing removed SFSB");
            }

            System.out.println("Application scoped test bean:"+ tasb);
            System.out.println("Request scoped test bean:"+ trsb);
            if (tasb == null)
                throw new Exception(
                        "Injection of application scoped Bean in MDB failed");
            if (trsb == null)
                throw new Exception(
                        "Injection of request scoped Bean in MDB failed");
            
            connection = qcFactory.createQueueConnection();
            QueueSession session = connection.createQueueSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            QueueSender sender = session.createSender(clientQueue);
            TextMessage tmessage = session.createTextMessage();
            tmessage.setText("mdb() invoked");
            System.out.println("Sending message");
            sender.send(tmessage);
            System.out.println("message sent");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
