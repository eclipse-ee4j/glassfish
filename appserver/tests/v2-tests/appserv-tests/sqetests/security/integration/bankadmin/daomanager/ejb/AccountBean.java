/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.security.integration.bankadmin.daomanager;

import java.util.HashMap;
import jakarta.ejb.EntityContext;
import java.util.logging.*;
import com.sun.ejte.ccl.reporter.*;

public abstract class AccountBean extends EnterpriseBeanLogger implements jakarta.ejb.EntityBean
{

    private static Logger logger = Logger.getLogger("bank.admin");
    private static ConsoleHandler ch = new ConsoleHandler();

    public abstract String getAccountID();
    public abstract void setAccountID(String id);

    public abstract Double getAmount();
    public abstract void setAmount(Double amt);

    public abstract HashMap getPrivileges();
    public abstract void setPrivileges(HashMap privileges);


        //Business methods

    public String ejbCreateAccount(AccountDataObject ado)
    throws jakarta.ejb.CreateException
    {

        toXML("ejbCreateAccount","Enter");
        setAccountID(ado.getAccountID());
        setAmount(ado.getAmount());
        setPrivileges(ado.getPermissionList());
        toXML("ejbCreateAccount","Created Account: "+ ado);
        toXML("ejbCreateAccount","Exit");

        return null;
    }


    public void ejbPostCreateAccount(AccountDataObject ado)
    throws jakarta.ejb.CreateException
    {}

    public AccountDataObject getDAO()
    {
        return new AccountDataObject(getAccountID(),getAmount(),getPrivileges());
    }

    public EntityContext ejbContext;

    public void ejbActivate() {
    }

    public void ejbPassivate() {}

    public void ejbLoad() {
        toXML("ejbLoad","CMP Account");
    }


    public void ejbStore() {
        toXML("ejbStore","CMP Account");
    }

    public void ejbRemove() {
        toXML("ejbRemove","CMP Account");
    }

    public void setEntityContext(EntityContext ctx) {
        ejbContext=ctx;
    }

    public void unsetEntityContext() {
        ejbContext=null;
    }

    public EntityContext getEJBContext() {
        return ejbContext;
    }


  }
