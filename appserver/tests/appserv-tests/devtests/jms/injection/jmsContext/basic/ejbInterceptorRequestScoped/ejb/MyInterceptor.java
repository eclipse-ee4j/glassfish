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

package org.glassfish.test.jms.injection.ejb;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import jakarta.jms.*;
import jakarta.ejb.*;
import javax.naming.*;

public class MyInterceptor {
    static String context;

    private Queue queue;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    @AroundInvoke
    public Object sendMsg(InvocationContext ctx) throws Exception {
        Object[] params = ctx.getParameters();
        try {
            lookupQueue();
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage((String) params[0]);
            producer.send(queue, msg);
            context = jmsContext.toString();
        } catch (Exception e) {
            throw new EJBException(e);
        }
        return ctx.proceed();
    }

    private Queue lookupQueue() throws Exception {
        InitialContext ctx = new InitialContext();
        queue = (Queue) ctx.lookup("jms/jms_unit_test_Queue");
        if (queue == null)
            throw new Exception("jms/jms_unit_test_Queue not found.");
        return queue;
    }
}
