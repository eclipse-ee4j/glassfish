/*uto-merging appserver/tests/appserv-tests/devtests/jms/injection/jmsContext/basic/wsRequestScoped/servlet/test/NewWebService.java
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.jms.*;
import jakarta.jws.WebService;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.transaction.UserTransaction;

/**
 *
 * @author LILIZHAO
 */
@WebService(serviceName = "NewWebService")
public class NewWebService {
    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    @Inject
    UserTransaction ut;

    /**
     * This is a sample web service operation
     */
    @WebMethod(operationName = "hello")
    public String hello(@WebParam(name = "message") String text) {
        String context = "";
        try {
            ut.begin();
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage(text);
            producer.send(queue, msg);
            context = jmsContext.toString();
            ut.commit();
            return context;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
