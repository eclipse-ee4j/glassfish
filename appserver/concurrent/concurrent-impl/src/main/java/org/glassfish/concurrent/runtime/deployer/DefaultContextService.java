/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.concurrent.runtime.deployer;

import org.glassfish.api.naming.DefaultResourceProxy;
import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.api.naming.NamespacePrefixes;
import org.jvnet.hk2.annotations.Service;

import jakarta.enterprise.concurrent.ContextService;
import jakarta.inject.Inject;
import javax.naming.NamingException;

/**
 * Naming Object Proxy to handle the Default ContextService.
 * Maps to a pre-configured context service, when binding for
 * a context service reference is absent in the @Resource annotation.
 */
@Service
@NamespacePrefixes({DefaultContextService.DEFAULT_CONTEXT_SERVICE})
public class DefaultContextService implements NamedNamingObjectProxy, DefaultResourceProxy {

    static final String DEFAULT_CONTEXT_SERVICE = "java:comp/DefaultContextService";
    static final String DEFAULT_CONTEXT_SERVICE_PHYS = "concurrent/__defaultContextService";
    private ContextService contextService;

    // Ensure that config for this object has been created
    @Inject org.glassfish.concurrent.config.ContextService.ContextServiceConfigActivator config;

    @Override
    public Object handle(String name) throws NamingException {
        if(contextService == null) {
            javax.naming.Context ctx = new javax.naming.InitialContext();
            // cache the managed executor service to avoid JNDI lookup overheads
            contextService = (ContextService)ctx.lookup(DEFAULT_CONTEXT_SERVICE_PHYS);
        }
        return contextService;
    }

    @Override
    public String getPhysicalName() {
        return DEFAULT_CONTEXT_SERVICE_PHYS;
    }

    @Override
    public String getLogicalName() {
        return DEFAULT_CONTEXT_SERVICE;
    }
}
