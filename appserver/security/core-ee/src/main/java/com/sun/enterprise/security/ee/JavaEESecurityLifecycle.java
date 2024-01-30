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
import static org.glassfish.epicyro.config.factory.file.AuthConfigFileFactory.DEFAULT_FACTORY_DEFAULT_PROVIDERS;

import com.sun.enterprise.security.ContainerSecurityLifecycle;
import com.sun.enterprise.security.ee.authorize.PolicyLoader;
import com.sun.enterprise.security.ee.jmac.AuthMessagePolicy;
import com.sun.enterprise.security.ee.jmac.ConfigDomainParser;
import com.sun.enterprise.security.ee.jmac.WebServicesDelegate;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.security.auth.message.MessageInfo;
import jakarta.security.auth.message.MessagePolicy;
import java.security.Provider;
import java.security.Security;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.glassfish.epicyro.config.factory.file.AuthConfigFileFactory;
import org.glassfish.epicyro.config.module.configprovider.GFServerConfigProvider;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Service;

/**
 * @author vbkumarjayanti
 */
@Service
@Singleton
public class JavaEESecurityLifecycle implements ContainerSecurityLifecycle, PostConstruct {

    @Inject
    PolicyLoader policyLoader;

    @Override
    public void postConstruct() {
        onInitialization();
    }

    @Override
    public void onInitialization() {
        initializeJakartaAuthentication();

        policyLoader.loadPolicy();
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
        if (delegate == null) {
            defaultProvidersString = GFServerConfigProvider.class.getName();
        } else {
            // NOTE: Order matters here. Providers for the same auth layer (HttpServlet or SOAP) will be overwritten
            //       by ones that appear later in this string without warning.
            defaultProvidersString = delegate.getDefaultWebServicesProvider() + " " + GFServerConfigProvider.class.getName();
        }

        Security.setProperty(DEFAULT_FACTORY_DEFAULT_PROVIDERS, defaultProvidersString);

        Function<MessageInfo, String> authContextIdGenerator =
                e -> Globals.get(WebServicesDelegate.class).getAuthContextID(e);

        BiFunction<String, Map<String, Object>, MessagePolicy[]> soapPolicyGenerator =
                (authContextId, properties) -> AuthMessagePolicy.getSOAPPolicies(
                       AuthMessagePolicy.getMessageSecurityBinding("SOAP", properties),
                       authContextId, true);

        Provider provider = new Provider("EleosProvider", "1.0", "") {
            private static final long serialVersionUID = 1L;
        };
        provider.put("authContextIdGenerator", authContextIdGenerator);
        provider.put("soapPolicyGenerator", soapPolicyGenerator);

        Security.addProvider(provider);

        System.setProperty("config.parser", ConfigDomainParser.class.getName());
    }

}
