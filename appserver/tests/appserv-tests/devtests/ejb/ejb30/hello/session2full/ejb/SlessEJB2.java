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

package com.sun.s1asdev.ejb.ejb30.hello.session2full;

import jakarta.ejb.EJBException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.CreateException;
import jakarta.ejb.SessionContext;

import javax.naming.InitialContext;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Status;

import java.util.Collection;
import java.util.Iterator;

public class SlessEJB2 implements Sless, SessionBean
{

    private SessionContext sc_ = null;

    public String hello() {
        System.out.println("In SlessEJB2:hello()");

        try {
            sc_.getUserTransaction();
            throw new EJBException("should have gotten exception when accessing SessionContext.getUserTransaction()");
        } catch(IllegalStateException ise) {
            System.out.println("Got expected exception when accessing SessionContext.getUserTransaction()");
        }

        return "hello from SlessEJB2";
    }

    public String hello2() throws jakarta.ejb.CreateException {
        throw new jakarta.ejb.CreateException();
    }

    public String getId() {

        try {
            // Proprietary way to look up tx manager.
            TransactionManager tm = (TransactionManager)
                new InitialContext().lookup("java:appserver/TransactionManager");
            // Use an implementation-specific check to ensure that there
            // is no tx.  A portable application couldn't make this check
            // since the exact tx behavior for TX_NOT_SUPPORTED is not
            // defined.
            int txStatus = tm.getStatus();
            if( txStatus == Status.STATUS_NO_TRANSACTION ) {
                System.out.println("Successfully verified tx attr = " +
                                   "TX_NOT_SUPPORTED in SlessEJB2::getId()");
            } else {
                throw new EJBException("Invalid tx status for TX_NOT_SUPPORTED" +
                                       " method SlessEJB2::getId() : " + txStatus);
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }

        return "SlessEJB2";
    }

    public Sless roundTrip(Sless s) {
        System.out.println("In SlessEJB2::roundTrip " + s);
        System.out.println("input Sless.getId() = " + s.getId());
        return s;
    }

    public Collection roundTrip2(Collection collectionOfSless) {
        System.out.println("In SlessEJB2::roundTrip2 " +
                           collectionOfSless);

        if( collectionOfSless.size() > 0 ) {
            Sless sless = (Sless) collectionOfSless.iterator().next();
            System.out.println("input Sless.getId() = " + sless.getId());
        }

        return collectionOfSless;
    }

    public void ejbCreate() {
        System.out.println("In SlessEJB2::ejbCreate()");
    }

    public void setSessionContext(SessionContext sc)
    {
        sc_ = sc;
    }

    public void ejbRemove()
    {}

    public void ejbActivate()
    {}

    public void ejbPassivate()
    {}


}
