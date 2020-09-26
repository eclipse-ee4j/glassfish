/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.transaction;

import java.util.logging.Logger;

import com.sun.logging.LogDomains;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.ComponentInvocation.ComponentInvocationType;
import org.glassfish.api.invocation.ComponentInvocationHandler;
import org.glassfish.api.invocation.InvocationException;

import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Inject;
import org.glassfish.hk2.api.ServiceLocator;

@Service
public class TransactionInvocationHandler implements ComponentInvocationHandler {

    private static Logger _logger = LogDomains.getLogger(
            TransactionInvocationHandler.class, LogDomains.JTA_LOGGER);

    @Inject private ServiceLocator habitat;

    private JavaEETransactionManager tm;

    /**
     * Dynamically init the reference. This avoids circular dependencies 
     * on injection: JavaEETransactionManager injects InvocationManager, which in
     * turn injects all ComponentInvocationHandler impls, i.e. instance of this class.
     * PostConstruct has a similar problem.
     */
    private void init() {
        if (tm == null ) {
            tm = habitat.getService(JavaEETransactionManager.class);
        }
    }

    public void beforePreInvoke(ComponentInvocationType invType,
            ComponentInvocation prevInv, ComponentInvocation newInv) throws InvocationException {
    }

    public void afterPreInvoke(ComponentInvocationType invType,
            ComponentInvocation prevInv, ComponentInvocation curInv) throws InvocationException {

        init();
        tm.preInvoke(prevInv);
    }

    public void beforePostInvoke(ComponentInvocationType invType,
            ComponentInvocation prevInv, ComponentInvocation curInv) throws InvocationException {
        init();
        tm.postInvoke(curInv, prevInv);
    }

    public void afterPostInvoke(ComponentInvocationType invType,
            ComponentInvocation prevInv, ComponentInvocation curInv) throws InvocationException {
    }
}
