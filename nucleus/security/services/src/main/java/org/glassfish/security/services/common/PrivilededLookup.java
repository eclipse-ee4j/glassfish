/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.services.common;

import java.security.PrivilegedAction;

import org.glassfish.hk2.api.ServiceLocator;

public class PrivilededLookup<T> implements PrivilegedAction<T> {

    private ServiceLocator serviceLocator;
    private Class<T> serviceClass;
    private String serviceName;

    /**
     *
     * @param serviceLocator   the HK2 service locator
     * @param serviceClass   the protected HK2 service to be looked up
     * @param serviceName    the name of the service to be looked
     */
    public PrivilededLookup(ServiceLocator serviceLocator,
            Class<T> serviceClass, String serviceName) {
        this.serviceLocator = serviceLocator;
        this.serviceClass = serviceClass;
        this.serviceName = serviceName;
    }

    /**
     *
     * @param serviceLocator   the HK2 service locator
     * @param serviceClass   the protected HK2 service to be looked up
     */
    public PrivilededLookup(ServiceLocator serviceLocator, Class<T> serviceClass) {
        this(serviceLocator, serviceClass, null);
    }

    public T run() {
        if (serviceName != null)
            return serviceLocator.getService(serviceClass, serviceName);
        else
            return serviceLocator.getService(serviceClass);
    }
}
