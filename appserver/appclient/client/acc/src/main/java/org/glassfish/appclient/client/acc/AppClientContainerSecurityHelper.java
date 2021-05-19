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

package org.glassfish.appclient.client.acc;

import com.sun.enterprise.container.common.spi.util.InjectionException;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.security.appclient.integration.AppClientSecurityInfo;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.inject.Inject;
import javax.security.auth.callback.CallbackHandler;
import org.glassfish.appclient.client.acc.config.ClientCredential;
import org.glassfish.appclient.client.acc.config.MessageSecurityConfig;
import org.glassfish.appclient.client.acc.config.TargetServer;
//import org.glassfish.enterprise.iiop.api.GlassFishORBHelper;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;

/**
 *
 * @author tjquinn
 */
@Service
@PerLookup
public class AppClientContainerSecurityHelper {

    @Inject
    private InjectionManager injectionManager;

    @Inject
    private AppClientSecurityInfo secInfo;

    private final Logger logger = Logger.getLogger(getClass().getName());

    private ClassLoader classLoader;

    void init(
            final TargetServer[] targetServers,
            final List<MessageSecurityConfig> msgSecConfigs,
            final Properties containerProperties,
            final ClientCredential clientCredential,
            final CallbackHandler callerSuppliedCallbackHandler,
            final ClassLoader classLoader,
            final ApplicationClientDescriptor acDesc,
            final boolean isTextAuth) throws InstantiationException, IllegalAccessException, InjectionException, ClassNotFoundException, IOException {

        this.classLoader = (classLoader == null) ? Thread.currentThread().getContextClassLoader() : classLoader;

        initLoginConfig();
        CallbackHandler callbackHandler =
                initSecurity(callerSuppliedCallbackHandler, acDesc);

        secInfo.initializeSecurity(Arrays.asList(targetServers),
                msgSecConfigs,
                callbackHandler,
                AppClientSecurityInfo.CredentialType.USERNAME_PASSWORD,
                (clientCredential == null ? null : clientCredential.getUserName()),
                (clientCredential == null ||
                    clientCredential.getPassword() == null ||
                    clientCredential.getPassword().get() == null
                        ? null : clientCredential.getPassword().get()),
                false /* isJWS */, ! isTextAuth /*useGUIAuth*/);

        initHttpAuthenticator(AppClientSecurityInfo.CredentialType.USERNAME_PASSWORD);
    }

    private void initLoginConfig() throws IOException {
        /*
         * During Java Web Start launches, the appclientlogin.conf content is
         * passed as a property.  Store that content (if present) into a local
         * temporary file and use that during this app client launch.
         */
        final String appclientloginConfContent = System.getProperty("appclient.login.conf.content");
        URI configURI;
        if (appclientloginConfContent == null) {
            File f = new File(System.getProperty("com.sun.aas.installRoot"));
            configURI = f.toURI().resolve("lib/appclient/appclientlogin.conf");
        } else {
            final File tempFile =
                    Util.writeTextToTempFile(
                        appclientloginConfContent,
                        "appclientlogin",
                        ".conf",
                        false);
            configURI = tempFile.toURI();
        }
        final File configFile = new File(configURI);
        /*
         * Ugly, but necessary.  The Java com.sun.security.auth.login.ConfigFile class
         * expects the java.security.auth.login.config property value to be
         * a URL, but one with NO encoding.  That is, if the path to the
         * config file contains a blank then ConfigFile class expects the URL
         * to contain a blank, not %20 for example.  So, we need to use the
         * deprecated File.toURL() method to create such a URL.
         */
        System.setProperty("java.security.auth.login.config", configFile.toURL().toString());
    }
    /**
     * Sets the callback handler for future use.
     *
     * @param callbackHandler the callback handler to be used
     */
    private CallbackHandler initSecurity(
            final CallbackHandler callerSuppliedCallbackHandler,
            final ApplicationClientDescriptor acDesc) throws InstantiationException, IllegalAccessException, InjectionException, ClassNotFoundException {

        /*
         * Choose a callback handler in this order:
         * 1. callback handler class set by the program that created the AppClientContainerBuilder.
         * 2. callback handler class name set in the app client descriptor
         * 3. null, in which case the security layer provides a default callback handler
         *
         * Our default handler uses no injection, but a user-provided one might.
         */
        CallbackHandler callbackHandler = callerSuppliedCallbackHandler;
        if (callerSuppliedCallbackHandler == null) {
            final String descriptorCallbackHandlerClassName;
            if (acDesc != null && ((descriptorCallbackHandlerClassName = acDesc.getCallbackHandler()) != null)) {
                callbackHandler = newCallbackHandlerInstance(
                        descriptorCallbackHandlerClassName, acDesc, classLoader);
            } else {
                callbackHandler = null;
            }
        }
        logger.config("Callback handler class = " +
                (callbackHandler == null ? "(default)" : callbackHandler.getClass().getName()));
        return callbackHandler;
    }

    private CallbackHandler newCallbackHandlerInstance(final String callbackHandlerClassName,
            final ApplicationClientDescriptor acDesc,
            final ClassLoader loader) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InjectionException {

        Class callbackHandlerClass =
                Class.forName(callbackHandlerClassName, true, loader);

        return newCallbackHandlerInstance(callbackHandlerClass, acDesc);
    }

    private CallbackHandler newCallbackHandlerInstance(final Class<? extends CallbackHandler> callbackHandlerClass,
            final ApplicationClientDescriptor acDesc) throws InstantiationException, IllegalAccessException, InjectionException {

        CallbackHandler userHandler = callbackHandlerClass.newInstance();
        injectionManager.injectInstance(userHandler, acDesc);
        return userHandler;
    }


    /**
     * Clears the Client's current Security Context.
     */
    void clearClientSecurityContext() {
        secInfo.clearClientSecurityContext();
    }

    /**
     * Check if the Login attempt was cancelled.
     * @return boolean indicating whether the login attempt was cancelled.
     */
    boolean isLoginCancelled(){
        return secInfo.isLoginCancelled();
    }

    private void initHttpAuthenticator(final AppClientSecurityInfo.CredentialType loginType) {
        Authenticator.setDefault(
                new HttpAuthenticator(secInfo, loginType));
    }

//    /**
//     * Creates a Properties object containing the ORB settings and, possibly,
//     * as a side-effect may assign some system property settings because that
//     * is how the ORB reads certain settings.
//     * <p>
//     * If there are multiple endpoints configured then the ACC chooses a
//     * default load balancing setting.
//     * The ACC assembled the full list of ORB settings in this order:
//     * <ol>
//     * <li>From Property objects in the ClientContainer configuration (this
//     * usage is deprecated and will be logged as such but for historical
//     * reasons is given priority)
//     * <li>From TargetServer object(s) in the ClientContainer configuration
//     * </ol>
//     * Note that the calling program should normally provide at least one
//     * TargetServer object.
//     *
//     * @return Properties object suitable as the argument to InitialContext
//     */
//    private Properties prepareIIOP(final TargetServer[] targetServers,
//            final Properties containerProperties) {
//
//        boolean isEndpointPropertySpecifiedByUser = false;
//        String loadBalancingPolicy = null;
//
//        Properties iiopProperties = new Properties();
//
//        boolean isLBEnabled = false;
//        boolean isSSLSpecifiedForATargetServer = false;
//
//        /*
//         * Although targetServerEndpoints is for user-friendly logging
//         * we need to compute lb_enabled and also to note if any target-server
//         * specifies ssl, so the loop is multi-purpose.
//         */
//        StringBuilder targetServerEndpoints = new StringBuilder();
//        for (TargetServer tServer : targetServers) {
//            addEndpoint(targetServerEndpoints, formatEndpoint(tServer.getAddress(), tServer.getport()));
//            isLBEnabled = true;
//            /*
//             * In the configuration the ssl sub-part is required if the
//             * security part is present.  So for speed just look for the
//             * security part under this target server.  That will ensure that
//             * the ssl part is there also, and that's all we're concerned with
//             * at this point.
//             */
//            isSSLSpecifiedForATargetServer |= (tServer.getSecurity() != null);
//        }
//
//        if (isSSLRequired(targetServers, containerProperties)) {
//            // XXX ORBManager needed
////            ORBManager.getCSIv2Props().put(ORBManager.ORB_SSL_CLIENT_REQUIRED, "true");
//        }
//
//        /*
//         * Find and use (if it exists) the container-level property that specifies a load balancing policy.
//         */
//        // XXX S1ASCtxFactory needed
////        loadBalancingPolicy = containerProperties.getProperty(S1ASCtxFactory.LOAD_BALANCING_PROPERTY);
//        isLBEnabled |= loadBalancingPolicy != null;
//
//        logger.fine("targetServerEndpoints = " + targetServerEndpoints.toString());
//
//        if (isLBEnabled) {
//        // XXX S1ASCtxFactory needed
////            System.setProperty(S1ASCtxFactory.IIOP_ENDPOINTS_PROPERTY, targetServerEndpoints.toString());
//            /*
//             * Honor any explicit setting of the load-balancing policy.
//             * Otherwise just defer to whatever default the ORB uses.
//             */
//            if (loadBalancingPolicy != null) {
//        // XXX S1ASCtxFactory needed
////                System.setProperty(S1ASCtxFactory.LOAD_BALANCING_PROPERTY, loadBalancingPolicy);
//            }
//            /*
//             * For load-balancing the Properties object is not used to convey
//             * the LB information.  Rather,
//             * the ORB detects the system property settings.  So return a
//             * null for the LB case.
//             */
//            iiopProperties = null;
//        } else {
//            /*
//             * For the non-load-balancing case, the Properties object must
//             * contain the initial host and port settings for the ORB.
//             */
//            iiopProperties.setProperty(ORB_INITIAL_HOST_PROPERTYNAME, targetServers[0].getAddress());
//            iiopProperties.setProperty(ORB_INITIAL_PORT_PROPERTYNAME, targetServers[0].getport().toString());
//        }
//        return iiopProperties;
//    }
//    private StringBuilder addEndpoint(final StringBuilder endpointSB, final String endpoint) {
//        if (endpointSB.length() > 0) {
//            endpointSB.append(",");
//        }
//        endpointSB.append(endpoint);
//        return endpointSB;
//    }
//
//    private String formatEndpoint(final String host, final int port) {
//        return host + ":" + port;
//    }

    /**
     * Proxy either for our default callback handler (used if needed during callbacks
     * while injecting the user's callback handler) or for the user's callback
     * handler (if the developer specified one).
     */
    private static class CallbackHandlerInvocationHandler implements InvocationHandler {

        private CallbackHandler delegate;

        CallbackHandlerInvocationHandler(final CallbackHandler handler) {
            delegate = handler;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(delegate, args);
        }

        void setDelegate(final CallbackHandler handler) {
            delegate = handler;
        }
    }
}
