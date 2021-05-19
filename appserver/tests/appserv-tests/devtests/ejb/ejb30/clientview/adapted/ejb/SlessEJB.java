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

package com.sun.s1asdev.ejb.ejb30.clientview.adapted;

import jakarta.ejb.*;
import jakarta.annotation.Resource;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Status;
import javax.naming.InitialContext;

@Stateless
@Remote({SlessRemoteBusiness.class, SlessRemoteBusiness2.class})
@Local({SlessBusiness.class, SlessBusiness2.class})
public class SlessEJB implements SlessBusiness, SlessBusiness2, SlessRemoteBusiness, SlessRemoteBusiness2
{

    private @Resource SessionContext ctx;

    public void foo() {
        System.out.println("In SlessEJB::SlessBusiness2::foo()");
    }

    public void bar() {
        System.out.println("In SlessEJB::SlessBusiness2::bar()");
    }

    public SlessBusiness2 getSlessBusiness2() {

        Class clazz = ctx.getInvokedBusinessInterface();
        if( clazz == SlessBusiness.class ) {
            System.out.println("Got correct value for " +
                               "getInvokedBusinessInterface = " + clazz);
        }

        return (SlessBusiness2) ctx.getBusinessObject(SlessBusiness2.class);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void notSupported() {}

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void required() {}

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void requiresNew() {}

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void mandatory() {}

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public void never() {}

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public void supports() {}

    /**
     * Business method declared on a super interface extended by both
     * remote and local business interface.  Test that tx attribute is
     * set appropriately for local vs. remote.
     */
    public void sharedRemoteLocalBusinessSuper(boolean expectTx) {

        boolean hasTx = false;

        try {

            // Proprietary way to look up tx manager.
            TransactionManager tm = (TransactionManager)
                //  TODO check this, it worked inV2              new InitialContext().lookup("java:pm/TransactionManager");
                              new InitialContext().lookup("java:appserver/TransactionManager");

            // Use an implementation-specific check whether there is a tx.
            // A portable application couldn't make this check
            // since the exact tx behavior for TX_NOT_SUPPORTED is not
            // defined.

            hasTx = ( tm.getStatus() != Status.STATUS_NO_TRANSACTION );

        } catch(Exception e) {
            throw new EJBException(e);
        }

        if( expectTx && hasTx ) {
            System.out.println("Successfully verified tx");
        } else if( !expectTx && !hasTx ) {
            System.out.println("Successfully verified there is no tx");
        } else {
            throw new EJBException("Invalid tx status.  ExpectTx = " +
                                   expectTx + " hasTx = " + hasTx);
        }

    }

}
