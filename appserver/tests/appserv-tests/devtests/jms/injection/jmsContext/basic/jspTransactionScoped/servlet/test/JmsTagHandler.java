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
package test;

import javax.annotation.Resource;
import javax.inject.Inject;
import jakarta.jms.*;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import javax.transaction.UserTransaction;

/**
 *
 * @author LILIZHAO
 */
public class JmsTagHandler extends TagSupport {
    private String text;

    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    @Inject
    UserTransaction ut;

    private static String transactionScope = "around TransactionScoped";

    @Override
    public int doStartTag() throws JspException {
        String context = "";
        try {
            System.out.println("jsp tag start...");

            ut.begin();
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage("Hello JSP Tag");
            producer.send(queue, msg);
            context = jmsContext.toString();
            ut.commit();
            
            if(context.indexOf(transactionScope) == -1){
                throw new JspException("NOT in transaction scope!");
            }
            //Get the writer object for output.
            JspWriter out = pageContext.getOut();
 
            //Perform substr operation on string.
            out.println(text);
 
        } catch (Exception e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
}
