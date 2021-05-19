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

package com.sun.s1asdev.ejb.ejb30.hello.mdb2;

import jakarta.ejb.*;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Status;
import javax.naming.InitialContext;

public class HelloStatelessSuper {

    static boolean timeoutHappened = false;

    @Timeout
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    private void mytimeoutmethod(Timer t) {

        System.out.println("In HelloStatelessSuper:mytimeoutmethod");

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
                                   "TX_NOT_SUPPORTED in mytimeoutmethod()");

                timeoutHappened = true;

            } else {
                System.out.println("Invalid tx status for TX_NOT_SUPPORTED" +
                                   " method " + txStatus);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }



    }


}
