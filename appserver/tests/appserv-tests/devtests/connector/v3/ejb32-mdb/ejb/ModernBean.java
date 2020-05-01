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

package com.sun.s1asdev.ejb.ejb32.mdb.ejb;

import com.sun.s1asdev.ejb.ejb32.mdb.ra.Command;
import com.sun.s1asdev.ejb.ejb32.mdb.ra.CommandListener;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.MessageDriven;
import jakarta.ejb.MessageDrivenContext;
import jakarta.interceptor.Interceptors;

/**
 * @author David Blevins
 */
@MessageDriven
@Interceptors(EnsureProxied.class)
public class ModernBean implements CommandListener {

    @EJB
    private ResultsBean resultsBean;

    @Resource
    private MessageDrivenContext messageDrivenContext;

    @Command
    public void doSomething() {
        resultsBean.addInvoked("one" + getInterceptorData());
    }

    @Command
    public void doSomethingElse() {
        resultsBean.addInvoked("two" + getInterceptorData());
    }

    @Command
    public void doItOneMoreTime() {
        resultsBean.addInvoked("three" + getInterceptorData());
    }

    /**
     * Ensure that the bean was invoked via a proxy with interceptors
     */
    private Object getInterceptorData() {
        return messageDrivenContext.getContextData().get("data");
    }

}
