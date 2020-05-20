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

package test.servlet;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import jakarta.jms.Session;


@ApplicationScoped
public class JMSResourcesProducer {

    @Resource(mappedName = "jms/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @SuppressWarnings("unused")
    @Produces
    @Resource(mappedName = "jms/Queue")
    private Queue queue;

    @Produces
    public Session produceSession() throws JMSException {
        /*try {
            connectionFactory = (ConnectionFactory) new javax.naming.InitialContext().lookup("jms/ConnectionFactory");
        } catch (javax.naming.NamingException exc) {
            throw new RuntimeException("couldn't lookup JMS Session", exc);
        }*/
        // as per EJB 3.1 specs $13.3.5
        return connectionFactory.createConnection().createSession(true, 0);
    }
}
