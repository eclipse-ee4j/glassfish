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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.jms.test;

import jakarta.annotation.Resource;
import jakarta.faces.bean.ManagedBean;
import jakarta.faces.bean.ViewScoped;
import javax.inject.Inject;
import jakarta.jms.*;
import javax.ejb.*;
import javax.transaction.UserTransaction;

/**
 *
 * @author LILIZHAO
 */
@ManagedBean
@ViewScoped
@TransactionManagement(TransactionManagementType.BEAN)
public class NewJSFManagedBean implements java.io.Serializable {
    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    @Inject 
    UserTransaction ut;

    private static String transactionScope = "around TransactionScoped";

    /**
     * Creates a new instance of NewJSFManagedBean
     */
    public NewJSFManagedBean() {
    }
    
    public String getMessage() {
        String context = "";
        try {
            String text = "JSF Hello World!";
            ut.begin();
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage(text);
            producer.send(queue, msg);
            context = jmsContext.toString();
            ut.commit();
            if(context.indexOf(transactionScope) == -1){
                throw new RuntimeException("NOT in transaction scope!");
            }
            return text;
        } catch (Exception e) {
            try {                
                ut.rollback();
            } catch (Exception ex) {
                e.printStackTrace();
            } 
            throw new RuntimeException(e);
        }
    }
}
