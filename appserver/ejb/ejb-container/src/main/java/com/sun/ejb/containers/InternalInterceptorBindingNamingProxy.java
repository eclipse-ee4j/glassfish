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

package com.sun.ejb.containers;

import org.glassfish.api.naming.NamespacePrefixes;
import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.hk2.api.ServiceLocator;

import jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;

import javax.naming.NamingException;

/**
 * Provides access to internal product-specific spi object for binding
 * system-level interceptors (e.g from JAX-RS)
 *
 * @author Ken Saks
 */
@Service
@NamespacePrefixes(InternalInterceptorBindingNamingProxy.INTERCEPTOR_BINDING)
public class InternalInterceptorBindingNamingProxy
        implements NamedNamingObjectProxy {

    @Inject
    private ServiceLocator services;

    static final String INTERCEPTOR_BINDING
            = "java:org.glassfish.ejb.container.interceptor_binding_spi";

    public Object handle(String name) throws NamingException {

        Object returnValue = null;

        if (INTERCEPTOR_BINDING.equals(name)) {
            returnValue = new InternalInterceptorBindingImpl(services);
        }

        return returnValue;
    }

}
