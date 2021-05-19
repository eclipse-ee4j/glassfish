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

package com.sun.s1asdev.ejb.webservice.commit;

import jakarta.ejb.*;
import jakarta.annotation.Resource;

@Stateful
public class CommitStatefulBean
    implements CommitStatefulLocal, SessionSynchronization {


    @Resource SessionContext sessionCtx;

    public void foo() {
        System.out.println("In CommitStatefulBean::foo");
    }

     public void afterBegin() {
        System.out.println("In CommitStatefulBean::afterBegin()");
    }

    public void beforeCompletion() {
        System.out.println("In CommitStatefulBean::beforeCompletion() " +
                           "marking tx for rollback");
        sessionCtx.setRollbackOnly();
    }

    public void afterCompletion(boolean committed) {
        System.out.println("In CommitStatefulBean::afterCompletion()");
    }


}
