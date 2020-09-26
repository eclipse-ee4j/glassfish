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

package org.glassfish.tests.ejb.mdb;

import jakarta.ejb.Singleton;
import jakarta.jms.*;
import jakarta.annotation.Resource;


/**
 * @author Marina Vatkina
 */
@Singleton
public class SimpleEjb {


    @Resource(name="jms/MyQueueConnectionFactory", mappedName="jms/ejb_mdb_QCF")
    QueueConnectionFactory fInject;

    @Resource(mappedName="jms/ejb_mdb_Queue")
    Queue qInject;

    boolean mdbCalled = false;

    public String saySomething() throws Exception {
        send();
        return "hello";
    }

    public void ack() {
        mdbCalled = true;
    }

    public boolean getAck() {
        return mdbCalled;
    }

    private void send() throws Exception {
        QueueConnection qConn = fInject.createQueueConnection();
        QueueSession qSession = qConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        QueueSender qSender = qSession.createSender(qInject);
        TextMessage tMessage = null;

        tMessage = qSession.createTextMessage("MY-MESSAGE");
        qSender.send(tMessage);

        qSession.close();
        qConn.close();
        System.err.println("Sent successfully");
    }

}
