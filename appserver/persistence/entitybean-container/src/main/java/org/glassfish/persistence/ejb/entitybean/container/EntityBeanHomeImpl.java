/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.persistence.ejb.entitybean.container;

import com.sun.ejb.Container;
import com.sun.ejb.EjbInvocation;
import com.sun.ejb.InvocationInfo;
import com.sun.ejb.containers.BaseContainer;
import com.sun.ejb.containers.EJBHomeInvocationHandler;
import com.sun.ejb.containers.EJBObjectImpl;
import com.sun.enterprise.deployment.EjbDescriptor;

/**
 * Implementation of the EJBHome interface for Entity Beans.
 *
 * @author mvatkina
 */

public class EntityBeanHomeImpl extends EJBHomeInvocationHandler {

    EntityBeanHomeImpl(EjbDescriptor ejbDescriptor,
                             Class homeIntfClass)
            throws Exception {
        super(ejbDescriptor, homeIntfClass);
    }

    /**
     * EJBObjectImpl is created directly by the container, not by this call
     */
    @Override
    public EJBObjectImpl createEJBObjectImpl() {
        return null;
    }

    @Override
    protected void postCreate(Container container, EjbInvocation inv,
            InvocationInfo invInfo, Object primaryKey, Object[] args)
            throws Throwable {
        container.postCreate(inv, primaryKey);
        invokeTargetBeanMethod((BaseContainer)container, invInfo.targetMethod2,
                 inv, inv.ejb, args);

    }
}

