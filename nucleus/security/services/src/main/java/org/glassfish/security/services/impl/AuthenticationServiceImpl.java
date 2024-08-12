/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.security.services.impl;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.security.auth.login.common.PasswordCredential;
import com.sun.enterprise.security.auth.realm.RealmsManager;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.security.services.api.authentication.AuthenticationService;
import org.glassfish.security.services.api.authentication.ImpersonationService;
import org.glassfish.security.services.config.LoginModuleConfig;
import org.glassfish.security.services.config.SecurityConfiguration;
import org.glassfish.security.services.config.SecurityProvider;
import org.glassfish.security.services.config.SecurityProviderConfig;
import org.jvnet.hk2.annotations.Service;

/**
 * The Authentication Service Implementation.
 *
 * Use JAAS LoginContext with the LoginModule(s) specified by the service configuration.
 */
@Service
@Singleton
public class AuthenticationServiceImpl implements AuthenticationService, PostConstruct {
    @Inject
    private Domain domain;

    @Inject
    ServerContext serverContext;

    @Inject
    private ServiceLocator locator;

    @Inject
    private ImpersonationService impersonationService;

    // Authentication Service Configuration Information
    private String name = null;
    private String realmName = null;
    private Configuration configuration = null;
    private boolean usePasswordCredential = false;
    private org.glassfish.security.services.config.AuthenticationService config = null;


    /**
     * Initialize the Authentication Service configuration.
     *
     * Create the JAAS Configuration using the specified LoginModule configurations
     */
    @Override
    public void initialize(SecurityConfiguration securityServiceConfiguration) {
//            org.glassfish.security.services.config.AuthenticationService as = (org.glassfish.security.services.config.AuthenticationService) securityServiceConfiguration;
//            LOG.info("*** AuthenticationServiceImpl auth svc file realm provider module class: ");
//            for (SecurityProvider sp : as.getSecurityProviders()) {
//                LOG.info("   *** Provider name/type" + sp.getName() + "/" + sp.getType());
//                if (sp.getSecurityProviderConfig() == null) {
//                    LOG.info("   *** getSecurityProviderConfig returned null");
//                } else {
//                    for (SecurityProviderConfig spc : sp.getSecurityProviderConfig()) {
//                        LOG.info("      *** " + spc.getName());
//                        if (sp.getType().equals("LoginModule")) {
//                            LoginModuleConfig lmc = (LoginModuleConfig) spc;
//                            LOG.info("      *** LoginModule config: class is " + lmc.getModuleClass());
//                        }
//                    }
//                }
//            }
        config = (org.glassfish.security.services.config.AuthenticationService) securityServiceConfiguration;
        if (config == null) {
            return;
        }

        // JAAS LoginContext Name
        name = config.getName();

        // Determine if handling Realm password credential
        usePasswordCredential = config.getUsePasswordCredential();

        // Build JAAS Configuration based on the individual LoginModuleConfig settings
        List<SecurityProvider> providers = config.getSecurityProviders();
        if (providers != null) {
            ArrayList<AppConfigurationEntry> lmEntries = new ArrayList<>();
            for (SecurityProvider provider : providers) {

                // If the provider is a LoginModule look for the LoginModuleConfig
                if ("LoginModule".equalsIgnoreCase(provider.getType())) {
                    List<SecurityProviderConfig> providerConfig = provider.getSecurityProviderConfig();
                    if ((providerConfig != null) && (!providerConfig.isEmpty())) {

                        // Create the JAAS AppConfigurationEntry from the LoginModule settings
                        LoginModuleConfig lmConfig = (LoginModuleConfig) providerConfig.get(0);
                        Map<String, ?> lmOptions = lmConfig.getModuleOptions();
                        lmEntries.add(new AppConfigurationEntry(lmConfig.getModuleClass(),
                            getLoginModuleControlFlag(lmConfig.getControlFlag()),lmOptions));

                        // Obtain the Realm name for password credential from the LoginModule options
                        // Use the first LoginModule with auth-realm (i.e. unable to stack Realms)
                        if (usePasswordCredential && (realmName == null)) {
                            String authRealm = (String) lmOptions.get("auth-realm");
                            if ((authRealm != null) && (!authRealm.isEmpty())) {
                                realmName = authRealm;
                            }
                        }
                    }
                }
            }
            if (!lmEntries.isEmpty()) {
                configuration = new AuthenticationJaasConfiguration(name,lmEntries);
            }
        }

        // If required, initialize the currently configured Realm instances
        // TODO - Reconcile initialization with SecurityLifeCycle
        if (usePasswordCredential && (realmName != null)) {
            RealmsManager realmsManager = locator.getService(RealmsManager.class);
            realmsManager.createRealms();
        }
    }

    @Override
    public Subject login(String username, char[] password, Subject subject)
        throws LoginException {
        CallbackHandler cbh = new AuthenticationCallbackHandler(username, password);
        return loginEx(cbh, subject);
    }

    @Override
    public Subject login(CallbackHandler cbh, Subject subject)
        throws LoginException {
        if (cbh == null) {
            throw new LoginException("AuthenticationService: JAAS CallbackHandler not supplied");
        }

        // TODO - Wrap CallbackHandler to obtain name for auditing
        return loginEx(cbh, subject);
    }

    private Subject loginEx(CallbackHandler handler, Subject subject) throws LoginException {
        // Use the supplied Subject or create a new Subject
        Subject _subject = subject;
        if (_subject == null) {
            _subject = new Subject();
        }

        ClassLoader contextClassloader = null;
        boolean restoreTcl = false;
        try {
            // Unable to login without a JAAS Configuration instance
            // TODO - Dynamic configuration handling?
            if (configuration == null) {
                throw new UnsupportedOperationException(
                    "JAAS Configuration setup incomplete, unable to perform login");
            }

            // Setup the PasswordCredential for the Realm LoginModules when configured
            if (usePasswordCredential) {
                setupPasswordCredential(_subject, handler);
            }

            // When needed, setup the Context ClassLoader so JAAS can load the LoginModule(s)
            contextClassloader = Thread.currentThread().getContextClassLoader();
            final ClassLoader commonClassLoader = serverContext.getCommonClassLoader();
            if (!commonClassLoader.equals(contextClassloader)) {
                Thread.currentThread().setContextClassLoader(commonClassLoader);
                restoreTcl = true;
            }

            // Perform the login via the JAAS LoginContext
            LoginContext context = new LoginContext(name, _subject, handler, configuration);
            context.login();
        } catch (Exception exc) {
            // TODO - Address Audit/Log/Debug handling options
            if (exc instanceof LoginException loginException) {
                throw loginException;
            }

            throw (LoginException) new LoginException(
                "AuthenticationService: "+ exc.getMessage()).initCause(exc);
        } finally {
            if (restoreTcl) {
                Thread.currentThread().setContextClassLoader(contextClassloader);
            }
        }

        // Return the Subject that was logged in
        return _subject;
    }

    @Override
    public Subject impersonate(String user, String[] groups, Subject subject, boolean virtual) throws LoginException {
        return impersonationService.impersonate(user, groups, subject, virtual);
    }

    /**
     * Handle lookup of authentication service configuration and initialization.
     * If no service is configured the service run-time will throw exceptions.
     *
     * Addresses alternate configuration handling until adopt @Proxiable support.
     */
    @Override
    public void postConstruct() {
        /*
         * Realm-related classes uses Globals (they are not services)
         * so make sure it is set up.
         */
        if (Globals.getDefaultBaseServiceLocator() == null) {
            Globals.setDefaultHabitat(locator);
        }

        // TODO - Dynamic configuration changes?
        initialize(AuthenticationServiceFactory.getAuthenticationServiceConfiguration(domain));
    }

    /**
     * A PasswordCredential object is needed when using the existing Realm LoginModules.
     *
     * Unless the CallbackHandler is from the AuthenticationService obtain the name
     * and password from the supplied JAAS CallbackHandler directly. Establishing the
     * PasswordCredential in the Subject is determined by service configuration.
     *
     * @throws LoginException when unable to obtain data from the CallbackHandler
     */
    private void setupPasswordCredential(Subject subject, CallbackHandler callbackHandler) throws LoginException {
        String username = null;
        char[] password = null;

        // Obtain the username and password for the PasswordCredential
        if (callbackHandler instanceof AuthenticationCallbackHandler) {
            username = ((AuthenticationCallbackHandler) callbackHandler).getUsername();
            password = ((AuthenticationCallbackHandler) callbackHandler).getPassword();
        }
        else {
            // Use the supplied callback handler to obtain the PasswordCredential information
            // TODO - How does this impact Audit ability to get name?
            Callback[] callbacks = new Callback[2];
            callbacks[0] = new NameCallback("username: ");
            callbacks[1] = new PasswordCallback("password: ", false);
            try {
                callbackHandler.handle(callbacks);
                username = ((NameCallback) callbacks[0]).getName();
                password = ((PasswordCallback) callbacks[1]).getPassword();
            } catch (IOException | UnsupportedCallbackException  ioe) {
                throw (LoginException) new LoginException("AuthenticationService unable to create PasswordCredential: "+ ioe.getMessage()).initCause(ioe);
            }
        }

        // Add the PasswordCredential to the Subject
        subject.getPrivateCredentials().add(new PasswordCredential(username, password, realmName));
    }

    /**
     * Convert the String setting to the JAAS LoginModuleControlFlag.
     * An unknown or null flag value is treated as LoginModuleControlFlag.REQUIRED.
     */
    private LoginModuleControlFlag getLoginModuleControlFlag(String controlFlag) {
        LoginModuleControlFlag flag = LoginModuleControlFlag.REQUIRED;
        // TODO - Handle invalid control flag?
        if (controlFlag != null) {
            if ("required".equalsIgnoreCase(controlFlag)) {
                return flag;
            }
            // Check additional flag types
            if ("sufficient".equalsIgnoreCase(controlFlag)) {
                flag = LoginModuleControlFlag.SUFFICIENT;
            } else if ("optional".equalsIgnoreCase(controlFlag)) {
                flag = LoginModuleControlFlag.OPTIONAL;
            } else if ("requisite".equalsIgnoreCase(controlFlag)) {
                flag = LoginModuleControlFlag.REQUISITE;
            }
        }
        return flag;
    }

    /**
     * The Authentication Service CallbackHandler implementation.
     *
     * Facilitates use of the standard JAAS NameCallback and PasswordCallback.
     */
    private static class AuthenticationCallbackHandler implements CallbackHandler {
        private final String user;
        private final char[] pass;

        public AuthenticationCallbackHandler(String username, char[] password) {
            user = username;
            pass = password;
        }

        protected String getUsername() { return user; }
        protected char[] getPassword() { return pass; }

        @Override
        public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(user);
                } else if (callback instanceof PasswordCallback) {
                    ((PasswordCallback) callback).setPassword(pass);
                } else {
                    // TODO - Have configuration setting for throwing exception
                    throw new UnsupportedCallbackException(callback,
                        "AuthenticationCallbackHandler: Unrecognized Callback "
                            + callback.getClass().getName());
                }
            }
        }
    }

    /**
     * The Authentication Service JAAS Configuration implementation.
     *
     * Facilitates the use of JAAS LoginContext with Authentication Service LoginModule configuration.
     */
    private static class AuthenticationJaasConfiguration extends Configuration {
        private final String configurationName;
        private final AppConfigurationEntry[] lmEntries;

        /**
         * Create the JAAS Configuration instance used by the Authentication Service.
         */
        private AuthenticationJaasConfiguration(String name, ArrayList<AppConfigurationEntry> entries) {
            configurationName = name;
            lmEntries = entries.toArray(new AppConfigurationEntry[entries.size()]);
        }

        /**
         * Get the LoginModule(s) specified by the Authentication Service configuration.
         */
        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
            if (configurationName.equals(name)) {
                return lmEntries;
            } else {
                return null;
            }
        }
    }
}
