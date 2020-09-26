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

package com.sun.s1asdev.ejb.ejb30.clientview.exceptions;

import jakarta.ejb.*;
import jakarta.annotation.Resource;
import jakarta.annotation.security.DenyAll;

@Local(SlessBusiness.class)
@Stateless
public class SlessEJB implements SlessBusiness, SlessRemoteBusiness, SlessRemoteBusiness2
{

    private @Resource SessionContext ctx;

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void forceTransactionRequiredException() {}

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public void forceTransactionRolledbackException() {
        System.out.println("In SlessEJB::forceTransactionRolledbackException");
    }

    public void throwRuntimeAppException() throws RuntimeAppException {
        throw new RuntimeAppException();
    }

    public void throwRollbackAppException() throws RollbackAppException {
        throw new RollbackAppException();
    }

    @DenyAll
    public void denied() {


    }

}
