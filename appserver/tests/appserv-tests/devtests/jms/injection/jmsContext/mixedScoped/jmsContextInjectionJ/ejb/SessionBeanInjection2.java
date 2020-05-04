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
import jakarta.ejb.*;
import jakarta.inject.Inject;
import jakarta.jms.*;

/**
 *
 * @author JIGWANG
 */
@Stateless(mappedName="SessionBeanInjection2/remote2")
@TransactionManagement(TransactionManagementType.CONTAINER)
public class SessionBeanInjection2 implements SessionBeanInjectionRemote2 {
    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String sendMessage(String text) {
        try {
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage(text);
            producer.send(queue, msg);
            System.out.println("JMSContext2:"+jmsContext.toString());
            return jmsContext.toString();
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

}
