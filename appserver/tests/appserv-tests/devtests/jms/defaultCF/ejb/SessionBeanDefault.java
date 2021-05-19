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

package org.glassfish.test.jms.defaultcf.ejb;

import jakarta.annotation.Resource;
import jakarta.ejb.*;
import jakarta.jms.*;
import javax.naming.InitialContext;

/**
 *
 * @author LILIZHAO
 */
@Stateless(mappedName="SessionBeanDefault/remote")
public class SessionBeanDefault implements SessionBeanDefaultRemote {
    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Resource(name="myCF0", lookup="jms/__defaultConnectionFactory")
    private ConnectionFactory cf0;

    @Resource(name="myCF1", lookup="java:comp/DefaultJMSConnectionFactory")
    private ConnectionFactory cf1;

    @Resource(name="myCF2")
    private ConnectionFactory cf2;

    @Override
    public void sendMessage(String text) {
        Connection conn = null;
        Session session = null;
        try {
            InitialContext ic = new InitialContext();
            ConnectionFactory o1 = (ConnectionFactory) ic.lookup("java:comp/DefaultJMSConnectionFactory");
            ConnectionFactory o2 = (ConnectionFactory) ic.lookup("java:comp/env/jms/systemDefaultCF");
            if (o1 == null || o2 == null || cf0 == null || cf1 == null || cf2 == null)
                throw new RuntimeException("Failed to lookup up jms default connection factory.");
            conn = cf2.createConnection();
            conn.start();
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            TextMessage msg = session.createTextMessage(text);
            MessageProducer p = session.createProducer(queue);
            p.send(msg);
        } catch (Exception e) {
            throw new EJBException(e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception e) {
                    throw new EJBException(e);
                }
            }
            if (conn != null) {
                try {

                    conn.close();
                } catch (Exception e) {
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
            conn = cf1.createConnection();
            conn.start();
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer r = session.createConsumer(queue);
            Message msg = r.receive(30000L);
            if (msg instanceof TextMessage) {
                String content = ((TextMessage) msg).getText();
                if (text.equals(content))
                    return true;
            }
            return false;
        } catch (Exception e) {
            throw new EJBException(e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception e) {
                    throw new EJBException(e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    throw new EJBException(e);
                }
            }
        }
    }
}
