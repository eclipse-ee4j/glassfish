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

package org.glassfish.tests.embedded.mdb;

import org.junit.Assert;
import org.junit.Test;

import jakarta.jms.QueueConnection;
import jakarta.jms.QueueConnectionFactory;
import jakarta.jms.QueueSender;
import jakarta.jms.QueueSession;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import javax.naming.InitialContext;
import java.io.File;
import java.io.FileInputStream;

/**
 * @author bhavanishankar@dev.java.net
 */

public class UnitTest {

    @Test
    public void testTimer() throws Exception {
        QueueConnection queueConnection = null;
        QueueSession queueSession = null;
        try {
            InitialContext ic = new InitialContext();

            QueueConnectionFactory qcf = (QueueConnectionFactory)
                    ic.lookup("jms/TestQueueConnectionFactory");
            jakarta.jms.Queue queue = (jakarta.jms.Queue) ic.lookup("jms/TestQueue");

            queueConnection = qcf.createQueueConnection();
            queueConnection.start();

            queueSession = queueConnection.createQueueSession(false,
                    Session.AUTO_ACKNOWLEDGE);

            QueueSender sender = queueSession.createSender(queue);

            String str = "Hi From BHAVANI";
            TextMessage msg = queueSession.createTextMessage(str);
            sender.send(msg);

            Thread.sleep(5000);

            byte[] message = new byte[msg.getText().length()];

            File savedFile = new File(System.getProperty("java.io.tmpdir"),
                    "embedded_mdb_onmessage.txt");
            FileInputStream is = new FileInputStream(savedFile);
            is.read(message);

            String savedMsg = new String(message);

            if(!savedMsg.equals(str)) {
                throw new Exception("Sent message [" + str +
                        " ] does not match the received message [" + savedMsg + "]");
            } else {
                System.out.println("Sent message [" + str +
                        " ]  matches the received message [" + savedMsg + "]");
            }
            savedFile.delete();
        } finally {
            try {
                queueSession.close();
                queueConnection.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
