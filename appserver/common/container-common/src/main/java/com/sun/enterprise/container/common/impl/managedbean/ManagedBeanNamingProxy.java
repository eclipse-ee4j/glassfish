/*
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

import org.glassfish.hk2.api.ServiceLocator;

import com.sun.enterprise.deployment.ManagedBeanDescriptor;
import com.sun.enterprise.container.common.spi.ManagedBeanManager;

/**
 */

public class ManagedBeanNamingProxy implements org.glassfish.api.naming.NamingObjectProxy {

    private ServiceLocator habitat;

    private ManagedBeanDescriptor managedBeanDesc;

    public ManagedBeanNamingProxy(ManagedBeanDescriptor desc, ServiceLocator h) {
        managedBeanDesc = desc;
        habitat = h;
    }

    public Object create(javax.naming.Context ic) throws javax.naming.NamingException {
        Object managedBean = null;
        try {
            ManagedBeanManager managedBeanMgr = habitat.getService(ManagedBeanManager.class);
            ClassLoader loader = Thread.currentThread().getContextClassLoader();

            // Create managed bean instance
            Class managedBeanClass = loader.loadClass(managedBeanDesc.getBeanClassName());
            managedBean = managedBeanMgr.createManagedBean(managedBeanDesc, managedBeanClass);
        } catch(Exception e) {
            javax.naming.NamingException ne = new javax.naming.NamingException(e.getMessage());
            ne.initCause(e);
            throw ne;
        }

        return managedBean;
    }
}
