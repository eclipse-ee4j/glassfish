/*
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.security.Security;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.security.ContainerSecurityLifecycle;
import com.sun.enterprise.security.jmac.config.GFAuthConfigFactory;
import com.sun.logging.LogDomains;

import jakarta.inject.Singleton;
import jakarta.security.auth.message.config.AuthConfigFactory;

/**
 *
 * @author vbkumarjayanti
 */
@Service
@Singleton
public class JavaEESecurityLifecycle implements ContainerSecurityLifecycle, PostConstruct {

    private static final Logger _logger = LogDomains.getLogger(JavaEESecurityLifecycle.class, LogDomains.SECURITY_LOGGER);

    @Override
    public void onInitialization() {
        java.lang.SecurityManager secMgr = System.getSecurityManager();
        // TODO: need someway to not override the SecMgr if the EmbeddedServer was
        // run with a different non-default SM.
        // right now there seems no way to find out if the SM is the VM's default SM.
        if (secMgr != null && !J2EESecurityManager.class.equals(secMgr.getClass())) {
            J2EESecurityManager mgr = new J2EESecurityManager();
            try {
                System.setSecurityManager(mgr);
            } catch (SecurityException ex) {
                _logger.log(Level.WARNING, "security.secmgr.could.not.override");
            }
        }
        initializeJMAC();
    }

    private void initializeJMAC() {

        // define default factory if it is not already defined
        // factory will be constructed on first getFactory call.

        String defaultFactory = Security.getProperty(AuthConfigFactory.DEFAULT_FACTORY_SECURITY_PROPERTY);
        if (defaultFactory == null) {
            Security.setProperty(AuthConfigFactory.DEFAULT_FACTORY_SECURITY_PROPERTY, GFAuthConfigFactory.class.getName());
        }
    }

    @Override
    public void postConstruct() {
        onInitialization();
    }
}
