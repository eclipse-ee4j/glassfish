/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import static jakarta.security.auth.message.config.AuthConfigFactory.DEFAULT_FACTORY_SECURITY_PROPERTY;
import static java.util.logging.Level.WARNING;
import static org.omnifaces.eleos.config.factory.file.AuthConfigFileFactory.DEFAULT_FACTORY_DEFAULT_PROVIDERS;

import java.security.Provider;
import java.security.Security;
import java.util.function.Function;
import java.util.logging.Logger;

import org.glassfish.config.support.DomainXml;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Service;
import org.omnifaces.eleos.config.factory.file.AuthConfigFileFactory;
import org.omnifaces.eleos.config.module.configprovider.GFServerConfigProvider;

import com.sun.enterprise.security.ContainerSecurityLifecycle;
import com.sun.enterprise.security.jmac.ConfigDomainParser;
import com.sun.enterprise.security.jmac.WebServicesDelegate;
import com.sun.logging.LogDomains;

import jakarta.inject.Singleton;
import jakarta.security.auth.message.MessageInfo;

/**
 * @author vbkumarjayanti
 */
@Service
@Singleton
public class JavaEESecurityLifecycle implements ContainerSecurityLifecycle, PostConstruct {

    private static final Logger LOG = LogDomains.getLogger(JavaEESecurityLifecycle.class, LogDomains.SECURITY_LOGGER, false);

    @Override
    public void postConstruct() {
        onInitialization();
    }

    @Override
    public void onInitialization() {
        SecurityManager securityManager = System.getSecurityManager();

        // TODO: need someway to not override the SecMgr if the EmbeddedServer was
        // run with a different non-default SM.
        // right now there seems no way to find out if the SM is the VM's default SM.
        if (securityManager != null && !J2EESecurityManager.class.equals(securityManager.getClass())) {
            try {
                System.setSecurityManager(new J2EESecurityManager());
            } catch (SecurityException ex) {
                LOG.log(WARNING, "Could not override SecurityManager");
            }
        }

        initializeJakartaAuthentication();
    }

    private void initializeJakartaAuthentication() {

        // Define default factory if it is not already defined.
        // The factory will be constructed on first getFactory call.

        String defaultFactory = Security.getProperty(DEFAULT_FACTORY_SECURITY_PROPERTY);
        if (defaultFactory == null) {
            Security.setProperty(DEFAULT_FACTORY_SECURITY_PROPERTY, AuthConfigFileFactory.class.getName());
        }

        String defaultProvidersString = null;
        WebServicesDelegate delegate = Globals.get(WebServicesDelegate.class);
        if (delegate != null) {
            // NOTE: Order matters here. Providers for the same auth layer (HttpServlet or SOAP) will be overwritten
            //       by ones that appear later in this string without warning.
            defaultProvidersString = delegate.getDefaultWebServicesProvider() + " " + GFServerConfigProvider.class.getName();
        } else {
            defaultProvidersString = GFServerConfigProvider.class.getName();
        }

        Security.setProperty(DEFAULT_FACTORY_DEFAULT_PROVIDERS, defaultProvidersString);

        Function<MessageInfo, String> authContextIdGenerator =
                e -> Globals.get(WebServicesDelegate.class).getAuthContextID(e);

        Provider provider = new Provider("EleosProvider", "1.0", "") {
            private static final long serialVersionUID = 1L;
        };
        provider.put("authContextIdGenerator", authContextIdGenerator);

        Security.addProvider(provider);

        System.setProperty("config.parser", ConfigDomainParser.class.getName());
    }


}
