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

package com.acme;

import jakarta.ejb.*;
import jakarta.jms.*;

/**
 *
 * @author marina vatkina
 */

@TransactionManagement(TransactionManagementType.BEAN)
@MessageDriven(mappedName="jms/ejb_mdb_Queue", description="mymessagedriven bean description")
public class MdBean implements MessageListener {

    @EJB MyBean bean;

    public void onMessage(Message message) {
        System.err.println("Got message!!!");

        try {
          if (message instanceof TextMessage) {
            TextMessage msg = (TextMessage) message;
            String txMsg = msg.getText();
            System.err.println("mdb: txMsg=" + txMsg);
            bean.record(txMsg);
          }
        } catch (Throwable e ) {
          e.printStackTrace();
        }

    }
}
