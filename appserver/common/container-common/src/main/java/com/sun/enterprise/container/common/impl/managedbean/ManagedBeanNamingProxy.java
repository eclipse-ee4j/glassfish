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

package com.sun.enterprise.container.common.impl.managedbean;

import com.sun.enterprise.container.common.spi.ManagedBeanManager;
import com.sun.enterprise.deployment.ManagedBeanDescriptor;

import javax.naming.Context;
import javax.naming.NamingException;

import org.glassfish.api.naming.NamingObjectProxy;
import org.glassfish.hk2.api.ServiceLocator;

public class ManagedBeanNamingProxy implements NamingObjectProxy {

    private final ServiceLocator serviceLocator;
    private final ManagedBeanDescriptor managedBeanDescriptor;

    public ManagedBeanNamingProxy(ManagedBeanDescriptor managedBeanDescriptor, ServiceLocator serviceLocator) {
        this.managedBeanDescriptor = managedBeanDescriptor;
        this.serviceLocator = serviceLocator;
    }

    @Override
    public <T> T create(Context ic) throws javax.naming.NamingException {
        try {
            // Create managed bean instance
            return (T) serviceLocator.getService(ManagedBeanManager.class)
                                 .createManagedBean(
                                     managedBeanDescriptor,
                                     Thread.currentThread()
                                           .getContextClassLoader()
                                           .loadClass(managedBeanDescriptor.getBeanClassName()));
        } catch (Exception e) {
            NamingException ne = new NamingException(e.getMessage());
            ne.initCause(e);
            throw ne;
        }
    }
}
