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

package com.sun.s1asdev.jms.msgdest.jmsweb;

import java.rmi.RemoteException;
import jakarta.jms.*;
import jakarta.ejb.*;
import java.io.Serializable;
import javax.naming.*;

public class MessageBean
    implements MessageDrivenBean, MessageListener {
    private MessageDrivenContext mdc;

    public MessageBean(){
    }

    public void onMessage(Message message) {
        System.out.println("Got message!!!"  +message);
    }

    public void setMessageDrivenContext(MessageDrivenContext mdc) {
        this.mdc = mdc;
        System.out.println("In MessageDrivenEJB::setMessageDrivenContext !!");
    }

    public void ejbCreate() throws RemoteException {
        System.out.println("In MessageDrivenEJB::ejbCreate !!");
    }

    public void ejbRemove() {
        System.out.println("In MessageDrivenEJB::ejbRemove !!");
    }

}
