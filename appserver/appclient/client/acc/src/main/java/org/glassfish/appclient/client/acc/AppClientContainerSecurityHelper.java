/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.appclient.client.acc;

import com.sun.enterprise.container.common.spi.util.InjectionException;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.security.appclient.integration.AppClientSecurityInfo;

import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.security.auth.callback.CallbackHandler;

import org.glassfish.appclient.client.acc.config.ClientCredential;
import org.glassfish.appclient.client.acc.config.MessageSecurityConfig;
import org.glassfish.appclient.client.acc.config.TargetServer;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.security.appclient.integration.AppClientSecurityInfo.CredentialType.USERNAME_PASSWORD;
import static java.util.Arrays.asList;
import static org.glassfish.appclient.client.acc.Util.writeTextToTempFile;
import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;

/**
 *
 * @author tjquinn
 */
@Service
@PerLookup
public class AppClientContainerSecurityHelper {

    private static final Logger logger = Logger.getLogger(AppClientContainerSecurityHelper.class.getName());

    @Inject
    private InjectionManager injectionManager;

    @Inject
    private AppClientSecurityInfo appClientSecurityInfo;

    private ClassLoader classLoader;

    void init(final TargetServer[] targetServers, final List<MessageSecurityConfig> msgSecConfigs, final Properties containerProperties,
            final ClientCredential clientCredential, final CallbackHandler callerSuppliedCallbackHandler, final ClassLoader classLoader,
            final ApplicationClientDescriptor acDesc, final boolean isTextAuth)
            throws ReflectiveOperationException, InjectionException, IOException {

        this.classLoader = classLoader;

        initLoginConfig();
        CallbackHandler callbackHandler = initSecurity(callerSuppliedCallbackHandler, acDesc);

        appClientSecurityInfo.initializeSecurity(
            asList(targetServers),
            msgSecConfigs,
            callbackHandler,
            (clientCredential == null ? null : clientCredential.getUserName()),
            (clientCredential == null || clientCredential.getPassword() == null || clientCredential.getPassword().get() == null ? null
                        : clientCredential.getPassword().get()),
            false /* isJWS */,
            !isTextAuth /* useGUIAuth */);

        initHttpAuthenticator(USERNAME_PASSWORD);
    }

    private void initLoginConfig() throws IOException {

        // During Java Web Start launches, the appclientlogin.conf content is passed as a property.
        // Store that content (if present) into a local temporary file and use that during this app client launch.
        final String appclientloginConfContent = System.getProperty("appclient.login.conf.content");
        final File configFile;
        if (appclientloginConfContent == null) {
            configFile = new File(System.getProperty(INSTALL_ROOT.getSystemPropertyName())).toPath()
                .resolve(Path.of("lib", "appclient", "appclientlogin.conf")).toFile();
        } else {
            configFile = writeTextToTempFile(appclientloginConfContent, "appclientlogin", ".conf", false);
        }

        // The Java com.sun.security.auth.login.ConfigFile class expects the
        // java.security.auth.login.config property value to be a URL
        System.setProperty("java.security.auth.login.config", configFile.toURI().toURL().toExternalForm());
    }

    /**
     * Sets the callback handler for future use.
     *
     * @param callbackHandler the callback handler to be used
     */
    private CallbackHandler initSecurity(final CallbackHandler callerSuppliedCallbackHandler, final ApplicationClientDescriptor applicationClientDescriptor)
            throws ReflectiveOperationException, InjectionException, ClassNotFoundException {

        /*
         * Choose a callback handler in this order: 1. callback handler class set by the program that created the
         * AppClientContainerBuilder. 2. callback handler class name set in the app client descriptor 3. null, in which case the
         * security layer provides a default callback handler
         *
         * Our default handler uses no injection, but a user-provided one might.
         */
        CallbackHandler callbackHandler = callerSuppliedCallbackHandler;
        if (callerSuppliedCallbackHandler == null) {
            final String descriptorCallbackHandlerClassName;
            if (applicationClientDescriptor != null && ((descriptorCallbackHandlerClassName = applicationClientDescriptor.getCallbackHandler()) != null)) {
                callbackHandler = newCallbackHandlerInstance(descriptorCallbackHandlerClassName, applicationClientDescriptor, classLoader);
            } else {
                callbackHandler = null;
            }
        }

        logger.config("Callback handler class = " + (callbackHandler == null ? "(default)" : callbackHandler.getClass().getName()));
        return callbackHandler;
    }

    private CallbackHandler newCallbackHandlerInstance(final String callbackHandlerClassName, final ApplicationClientDescriptor applicationClientDescriptor,
            final ClassLoader loader) throws ClassNotFoundException, ReflectiveOperationException, IllegalAccessException, InjectionException {

        @SuppressWarnings("unchecked")
        Class<? extends CallbackHandler> callbackHandlerClass = (Class<? extends CallbackHandler>) Class.forName(callbackHandlerClassName, true, loader);

        return newCallbackHandlerInstance(callbackHandlerClass, applicationClientDescriptor);
    }

    private CallbackHandler newCallbackHandlerInstance(final Class<? extends CallbackHandler> callbackHandlerClass,
            final ApplicationClientDescriptor applicationClientDescriptor) throws ReflectiveOperationException, IllegalAccessException, InjectionException {

        CallbackHandler userHandler = callbackHandlerClass.getDeclaredConstructor().newInstance();
        injectionManager.injectInstance(userHandler, applicationClientDescriptor);

        return userHandler;
    }

    /**
     * Clears the Client's current Security Context.
     */
    void clearClientSecurityContext() {
        appClientSecurityInfo.clearClientSecurityContext();
    }

    /**
     * Check if the Login attempt was cancelled.
     *
     * @return boolean indicating whether the login attempt was cancelled.
     */
    boolean isLoginCancelled() {
        return appClientSecurityInfo.isLoginCancelled();
    }

    private void initHttpAuthenticator(final AppClientSecurityInfo.CredentialType loginType) {
        Authenticator.setDefault(new HttpAuthenticator(appClientSecurityInfo, loginType));
    }
}
