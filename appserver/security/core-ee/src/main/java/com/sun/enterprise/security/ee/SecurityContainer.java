/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.ee;

import com.sun.enterprise.deployment.interfaces.SecurityRoleMapperFactoryMgr;
import com.sun.logging.LogDomains;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.deployment.common.SecurityRoleMapperFactory;
import org.jvnet.hk2.annotations.Service;

/**
 * Security container service
 */
@Service(name = "com.sun.enterprise.security.ee.SecurityContainer")
public class SecurityContainer implements Container {

    private static final Logger LOG = LogDomains.getLogger(SecurityContainer.class, LogDomains.SECURITY_LOGGER, false);

    /**
     * The system-assigned default web module's name/identifier.
     */
    public static final String DEFAULT_WEB_MODULE_NAME = "__default-web-module";

    static {
        initRoleMapperFactory();
    }

    @Override
    public String getName() {
        return "Security";
    }

    @Override
    public Class<? extends Deployer<?, ?>> getDeployer() {
        return SecurityDeployer.class;
    }

    /** This should never ever fail. */
    private static void initRoleMapperFactory() {
        try {
            Object o = Class.forName("com.sun.enterprise.security.ee.acl.RoleMapperFactory")
                            .getDeclaredConstructor()
                            .newInstance();

            if (o instanceof SecurityRoleMapperFactory securityRoleMapperFactory) {
                SecurityRoleMapperFactoryMgr.registerFactory(securityRoleMapperFactory);
            }
        } catch (Exception cnfe) {
            LOG.log(Level.SEVERE,
                "The RoleMapperFactory could not be initialized, initialization of the SecurityContainer failed.",
                cnfe);
        }
    }
}
