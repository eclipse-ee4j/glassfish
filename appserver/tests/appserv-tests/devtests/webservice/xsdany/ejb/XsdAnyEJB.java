/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package xsdanyejb;

import java.io.Serializable;
import java.rmi.RemoteException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.EJBException;
import javax.naming.*;
import javax.xml.rpc.handler.MessageContext;

public class XsdAnyEJB implements SessionBean {
    private SessionContext sc;

    public XsdAnyEJB(){}

    public void ejbCreate() throws RemoteException {
        System.out.println("In XsdAnyEJB::ejbCreate !!");
    }

    public int test1(int a,
                     xsdanyejb.SingleWildcardType c,
                     xsdanyejb.RepeatedWildcardType d)
        throws java.rmi.RemoteException
    {
        MessageContext msgContext = sc.getMessageContext();
        System.out.println("msgContext = " + msgContext);

        System.out.println("XsdAnyEJB.test1() called with ");
        System.out.println("a = " + a);

        System.out.println("SingleWildcardType.foo = " + c.getFoo());
        System.out.println("SingleWildcardType.bar = " + c.getBar());
        //System.out.println("SingleWildcardType._any = " + c.get_any());
        System.out.println("SingleWildcardType._any = " + c.getVoo());

        System.out.println("RepeatedWildcardType.foo = " + d.getFoo());
        System.out.println("RepeatedWildcardType.bar = " + d.getBar());
        System.out.println("RepeatedWildcardType._any = " + d.get_any());

        System.out.println("GoogleEJB returning " + a);

        return a;
    }

    public void setSessionContext(SessionContext sc) {

        this.sc = sc;
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}

}
