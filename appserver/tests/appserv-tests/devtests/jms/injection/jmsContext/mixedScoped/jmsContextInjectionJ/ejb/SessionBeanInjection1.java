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
import org.glassfish.test.jms.injection.ejb.SessionBeanInjection2;

/**
 *
 * @author JIGWANG
 */
@Stateless(mappedName="SessionBeanInjection1/remote1")
@TransactionManagement(TransactionManagementType.CONTAINER)
public class SessionBeanInjection1 implements SessionBeanInjectionRemote1 {

    private static String transactionScope = "around TransactionScoped";
    private static String preIdentical = "fingerPrint";

    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    @EJB
    SessionBeanInjectionRemote2 bean2;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Boolean sendMessage(String text) {
        String context1 = "";
        String context2 = "";
        String context3 = "";
        try {
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage(text);
            producer.send(queue, msg);
            System.out.println("JMSContext1:"+jmsContext.toString());
            context1 = jmsContext.toString();
            context2 = bean2.sendMessage(text);
            producer.send(queue, msg);
            System.out.println("JMSContext3:"+jmsContext.toString());
            context3 = jmsContext.toString();
        } catch (Exception e) {
            throw new EJBException(e);
        }
        return checkResult(context1, context2, context3);
    }


    public Boolean checkResult(String context1, String context2, String context3){

        if (context1.indexOf(transactionScope) != -1){
            System.out.println("The context variables used in the first call are in transaction scope.");
        }else{
            System.out.println("The context variables used in the first call are NOT in transaction scope.");
            return false;
        }

        if (context2.indexOf(transactionScope) != -1){
            System.out.println("The context variables used in the second call are in transaction scope.");
        }else{
            System.out.println("The context variables used in the second call are NOT in transaction scope.");
            return false;
        }

        if (context3.indexOf(transactionScope) != -1){
            System.out.println("The context variables used in the third call are in transaction scope.");
        }else{
            System.out.println("The context variables used in the third call are NOT in transaction scope.");
            return false;
        }

        String context1Annotation = context1.substring(context1.indexOf(preIdentical),context1.indexOf(transactionScope));
        String context2Annotation = context2.substring(context2.indexOf(preIdentical),context2.indexOf(transactionScope));
        String context3Annotation = context3.substring(context3.indexOf(preIdentical),context3.indexOf(transactionScope));

        if(context1Annotation.equals(context2Annotation)){
            System.out.println("The context variables in the first and second calls to context.send() injected are using identical annotations.");
            if(context1Annotation.equals(context3Annotation)){
                System.out.println("The context variables in the first and second and third calls to context.send() injected are using identical annotations.");
            }else{
                System.out.println("The context variables in the first and third calls to context.send() injected are not using identical annotations.");
                return false;
            }
        }else{
            System.out.println("The context variables in the first and second calls to context.send() injected are not using identical annotations.");
            return false;
        }

        if (context1.substring(context1.indexOf(transactionScope)).equals(context2.substring(context2.indexOf(transactionScope)))){
            System.out.println("The context variables used in the first and second calls to context.send() take place in the same transaction.");
            return false;
        }else{
            System.out.println("The context variables used in the first and second calls to context.send() take place in the different transaction.");
        }

        if (context1.substring(context1.indexOf(transactionScope)).equals(context3.substring(context3.indexOf(transactionScope)))){
            System.out.println("The context variables used in the first and third calls to context.send() take place in the same transaction.");
        }else{
            System.out.println("The context variables used in the first and third calls to context.send() take place in the different transaction.");
            return false;
        }

        return true;
    }

}
