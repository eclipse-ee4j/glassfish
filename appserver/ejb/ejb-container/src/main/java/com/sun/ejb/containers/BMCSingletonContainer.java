/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejb.containers;

import com.sun.ejb.ComponentContext;
import com.sun.ejb.EjbInvocation;
import com.sun.enterprise.security.SecurityManager;

import java.util.concurrent.atomic.AtomicInteger;

import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/**
 * @author Mahesh Kannan
 */
public class BMCSingletonContainer extends AbstractSingletonContainer {

    private AtomicInteger invCount = new AtomicInteger(0);

    public BMCSingletonContainer(EjbDescriptor desc, ClassLoader cl, SecurityManager sm) throws Exception {
        super(desc, cl, sm);
    }

    @Override
    protected ComponentContext _getContext(EjbInvocation inv) {
        checkInit();

        synchronized (invCount) {
            invCount.incrementAndGet();
            ((SingletonContextImpl) singletonCtx).setState(EJBContextImpl.BeanState.INVOKING);
        }

        // For now return this as we support only BMC
        return singletonCtx;
    }

    @Override
    public void releaseContext(EjbInvocation inv) {
        synchronized (invCount) {
            int val = invCount.decrementAndGet();
            if (val == 0) {
                ((SingletonContextImpl) singletonCtx).setState(EJBContextImpl.BeanState.READY);
            }
        }
    }

}
