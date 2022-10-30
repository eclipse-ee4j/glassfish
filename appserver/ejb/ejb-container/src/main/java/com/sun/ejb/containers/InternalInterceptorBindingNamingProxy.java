/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import jakarta.inject.Inject;

import javax.naming.NamingException;

import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.api.naming.NamespacePrefixes;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA;

/**
 * Provides access to internal product-specific spi object for binding
 * system-level interceptors (e.g from JAX-RS)
 *
 * @author Ken Saks
 */
@Service
@NamespacePrefixes(InternalInterceptorBindingNamingProxy.INTERCEPTOR_BINDING)
public class InternalInterceptorBindingNamingProxy implements NamedNamingObjectProxy {

    public static final String INTERCEPTOR_BINDING = JNDI_CTX_JAVA
        + "org.glassfish.ejb.container.interceptor_binding_spi";
    @Inject
    private ServiceLocator services;

    @Override
    public Object handle(String name) throws NamingException {
        if (INTERCEPTOR_BINDING.equals(name)) {
            return new InternalInterceptorBindingImpl(services);
        }
        return null;
    }
}
