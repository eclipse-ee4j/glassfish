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

package org.glassfish.persistence.ejb.entitybean.container;

import java.lang.reflect.Method;

import com.sun.appserv.ejb.ReadOnlyBeanLocalNotifier;
import com.sun.enterprise.deployment.EjbDescriptor;
import org.glassfish.persistence.ejb.entitybean.container.spi.ReadOnlyEJBLocalHome;

/**
 * Implementation of the EJBHome interface.
 * This class is also the base class for all generated concrete ReadOnly
 * EJBLocalHome implementations.
 * At deployment time, one instance of ReadOnlyEJBHomeImpl is created
 * for each EJB class in a JAR that has a local home.
 *
 * @author Mahesh Kannan
 */

public class ReadOnlyEJBLocalHomeImpl
    extends EntityBeanLocalHomeImpl
    implements ReadOnlyEJBLocalHome
{
    private ReadOnlyBeanLocalNotifier robNotifier;

    protected ReadOnlyEJBLocalHomeImpl(EjbDescriptor ejbDescriptor,
                                  Class localHomeIntf) throws Exception {
        super(ejbDescriptor, localHomeIntf);
    }

    /**
     * Called from ReadOnlyBeancontainer only.
     */
    final void setReadOnlyBeanContainer(ReadOnlyBeanContainer robContainer) {
        this.robNotifier = new ReadOnlyBeanLocalNotifierImpl(robContainer);
    }

    public ReadOnlyBeanLocalNotifier getReadOnlyBeanLocalNotifier() {
        return robNotifier;
    }

    protected boolean handleSpecialEJBLocalHomeMethod(Method method, Class methodClass) {
        return (methodClass == ReadOnlyEJBLocalHome.class);
    }

    protected Object invokeSpecialEJBLocalHomeMethod(Method method, Class methodClass,
            Object[] args) throws Throwable {
        return getReadOnlyBeanLocalNotifier();
    }
}
