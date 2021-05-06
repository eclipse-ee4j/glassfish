/*
 * Copyright (c) 2002, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.mdb.msgbean;

import jakarta.ejb.MessageDrivenBean;
import jakarta.ejb.MessageDrivenContext;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import javax.naming.*;

public class OperationTestMsgBean implements MessageDrivenBean,
    MessageListener {

    private MessageDrivenContext mdc = null;
    private OperationTest opTest;
    private boolean beanManagedTx = false;

    public OperationTestMsgBean() {
        System.out.println("In OperationTestMsgBean ctor()");
        opTest = new OperationTest();
        runTest(OperationTest.CTOR);
    };

    public void ejbCreate() {
        System.out.println("In OperationTestMsgBean::ejbCreate() !!");
        try {
            Context context = new InitialContext();
            beanManagedTx =
                ((Boolean) context.lookup("java:comp/env/beanManagedTx")).booleanValue();

            if( beanManagedTx ) {
                System.out.println("BEAN MANAGED TRANSACTIONS");
            } else {
                System.out.println("CONTAINER MANAGED TRANSACTIONS");
            }

            runTest(OperationTest.EJB_CREATE);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void onMessage(Message recvMsg) {
        System.out.println("In OperationTestMsgBean::onMessage() : "
                           + recvMsg);

        runTest(OperationTest.ON_MESSAGE);
    }

    public void setMessageDrivenContext(MessageDrivenContext mdc) {
        System.out.println
            ("In OperationTestMsgBean::setMessageDrivenContext()!!");
        this.mdc = mdc;
        runTest(OperationTest.SET_CONTEXT);
    }

    public void ejbRemove() {
        System.out.println("In OperationTestMsgBean::remove()!!");
        runTest(OperationTest.EJB_REMOVE);
    }

    private void runTest(int methodType) {
        int txType = beanManagedTx ? OperationTest.BMT : OperationTest.CMT;
        opTest.doTest(txType, methodType, mdc);
    }
}
