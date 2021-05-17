/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package profile;

import jakarta.ejb.SessionContext;
import jakarta.ejb.SessionBean;
import java.util.Vector;
import java.lang.String;
import java.util.Iterator;
import jakarta.ejb.EJBException;
import java.rmi.RemoteException;
/**
 *
 * @author  hsingh
 */

public class ProfileInfoBean implements SessionBean {

    private String name;

    private SessionContext sc = null;

    /** Creates a new instance of ProfieInfo */
    public void ejbCreate(String name) {
        this.name = name;
    }

    public String getCallerInfo() {
        return sc.getCallerPrincipal().toString();
    }

    public String getSecretInfo() {
        return "Keep It Secret!";
    }

    public void ejbActivate() {
        System.out.println("In ShoppingCart ejbActivate");
    }


    public void ejbPassivate() {
        System.out.println("In ShoppingCart ejbPassivate");
    }


    public void ejbRemove()  {
        System.out.println("In ShoppingCart ejbRemove");
    }


    public void setSessionContext(jakarta.ejb.SessionContext sessionContext) {
        sc = sessionContext;
    }

}
