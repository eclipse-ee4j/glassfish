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

package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import jakarta.interceptor.InvocationContext;
import jakarta.interceptor.AroundInvoke;
import jakarta.ejb.PrePassivate;
import jakarta.ejb.PostActivate;
import jakarta.ejb.EJB;
import jakarta.annotation.Resource;
import jakarta.ejb.SessionContext;

public class BaseLifecycleInterceptor
        implements java.io.Serializable {

    protected int basePrePassivateCount = 0;
    protected int basePostActivateCount = 0;


    @EJB Sless sless;
    @Resource SessionContext sessionCtx;

    @PrePassivate
    private void prePassivate(InvocationContext ctx) {
        basePrePassivateCount++;
    }

    @PostActivate
    private void postActivate(InvocationContext ctx) {
        basePostActivateCount++;
    }

}
