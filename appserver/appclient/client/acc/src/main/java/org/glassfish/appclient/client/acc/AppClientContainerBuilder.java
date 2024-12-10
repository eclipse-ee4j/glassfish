/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.util.LocalStringManager;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.security.auth.callback.CallbackHandler;

import org.glassfish.appclient.client.acc.config.AuthRealm;
import org.glassfish.appclient.client.acc.config.ClientCredential;
import org.glassfish.appclient.client.acc.config.MessageSecurityConfig;
import org.glassfish.appclient.client.acc.config.Property;
import org.glassfish.appclient.client.acc.config.TargetServer;
import org.glassfish.appclient.client.acc.config.util.XML;
import org.glassfish.embeddable.client.UserError;
import org.glassfish.enterprise.iiop.api.GlassFishORBHelper;
import org.xml.sax.SAXException;

import static java.util.logging.Level.CONFIG;
import static org.glassfish.internal.api.ORBLocator.OMG_ORB_INIT_HOST_PROPERTY;
import static org.glassfish.internal.api.ORBLocator.OMG_ORB_INIT_PORT_PROPERTY;
import static org.glassfish.internal.api.ORBLocator.ORB_SSL_CLIENT_REQUIRED;

/**
 * Implements a builder for accumulating configuration information for the app client container and then starting the
 * ACC.
 *
 * <p>
 * The interface for the ACC builder is defined as AppClientContainer.Builder so the relevant JavaDoc is concentrated in
 * that one class.
 *
 * <p>
 * The AppClientContainerBuilder class records the information the container itself needs in order to operate.
 *
 * @author tjquinn
 */
public class AppClientContainerBuilder implements AppClientContainer.Builder {

    /** caller-optional logger - initialized to logger name from the class; caller can override with logger method */
    private Logger logger = Logger.getLogger(getClass().getName());

    private final static String ENDPOINTS_PROPERTY_NAME = "com.sun.appserv.iiop.endpoints";

    private static final LocalStringManager localStrings = new LocalStringManagerImpl(AppClientContainerBuilder.class);

    private final URLClassLoader classLoader;

    /** caller-specified target servers */
    private TargetServer[] targetServers;

    private AuthRealm authRealm;


    /**
     * The caller can pre-set the client credentials using the <code>clientCredentials</code> method. The ACC will use the
     * username and realm values in intializing a callback handler if one is needed.
     */
    private ClientCredential clientCredential;

    private boolean sendPassword = true;

    private GlassFishORBHelper orbHelper;

    /** caller-provided message security configurations */
    private final List<MessageSecurityConfig> messageSecurityConfigs = new ArrayList<>();

    /**
     * optional caller-specified properties governing the ACC's behavior. Correspond to the property elements available in
     * the client-container element from sun-application-client-containerxxx.dtd.
     */
    private Properties containerProperties;

    /**
     * Creates a new builder with the specified target servers and client URI.
     *
     * @param targetServers the <code>TargetServer</code>s to use
     * @param clientURI the URI of the client archive to launch
     */
    AppClientContainerBuilder(final TargetServer[] targetServers) {
        this.targetServers = targetServers;
        this.classLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
    }

    public AppClientContainer newContainer(final Class mainClass, final CallbackHandler callerSpecifiedCallbackHandler) throws Exception {
        prepareHabitat();
        Launchable client = Launchable.LaunchableUtil.newLaunchable(ACCModulesManager.getHabitat(), mainClass);
        AppClientContainer container = createContainer(client, callerSpecifiedCallbackHandler, false /* istextAuth */);
        return container;
    }

    @Override
    public AppClientContainer newContainer(final Class mainClass) throws Exception {
        return newContainer(mainClass, null);

    }

    @Override
    public AppClientContainer newContainer(final URI clientURI, final CallbackHandler callerSpecifiedCallbackHandler,
            final String callerSpecifiedMainClassName, final String callerSpecifiedAppClientName) throws Exception, UserError {
        return newContainer(
            clientURI,
            callerSpecifiedCallbackHandler,
            callerSpecifiedMainClassName,
            callerSpecifiedAppClientName,
            false /* isTextAuth */);
    }

    @Override
    public AppClientContainer newContainer(final URI clientURI, final CallbackHandler callerSpecifiedCallbackHandler,
            final String callerSpecifiedMainClassName, final String callerSpecifiedAppClientName, final boolean isTextAuth)
            throws Exception, UserError {
        prepareHabitat();
        prepareIIOP(targetServers, containerProperties);
        Launchable client = Launchable.LaunchableUtil.newLaunchable(
                clientURI,
                callerSpecifiedMainClassName,
                callerSpecifiedAppClientName,
                ACCModulesManager.getHabitat());

        return createContainer(client, callerSpecifiedCallbackHandler, isTextAuth);
    }

    @Override
    public AppClientContainer newContainer(final URI clientURI) throws Exception, UserError {
        return newContainer(clientURI, null, null, null);
    }

    private AppClientContainer createContainer(final Launchable client, final CallbackHandler callerSuppliedCallbackHandler,
            final boolean isTextAuth)
            throws ReflectiveOperationException, InjectionException, IOException, SAXException {

        AppClientContainer container = ACCModulesManager.getService(AppClientContainer.class);

        // process the packaged permissions.xml
        container.processPermissions();
        container.setClient(client);
        CallbackHandler callbackHandler = callerSuppliedCallbackHandler == null
            ? getCallbackHandlerFromDescriptor(client.getDescriptor(classLoader).getCallbackHandler())
            : callerSuppliedCallbackHandler;
        container.prepareSecurity(targetServers, messageSecurityConfigs, containerProperties, clientCredential,
            callbackHandler, isTextAuth);

        return container;
    }

    private CallbackHandler getCallbackHandlerFromDescriptor(final String callbackHandlerName)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
        if (callbackHandlerName != null && !callbackHandlerName.equals("")) {
            Class<CallbackHandler> callbackHandlerClass = (Class<CallbackHandler>) Class.forName(callbackHandlerName, true, classLoader);
            return callbackHandlerClass.getDeclaredConstructor().newInstance();
        }

        return null;
    }

    private void prepareHabitat() throws URISyntaxException {
        ACCModulesManager.initialize(Thread.currentThread().getContextClassLoader());
        orbHelper = ACCModulesManager.getService(GlassFishORBHelper.class);
    }

    /**
     * Prepares the client ORB to bootstrap into the server ORB(s) specified by the TargetServer objects.
     *
     * @param targetServers the TargetServer endpoints to which the client ORB can try to connect
     * @param containerProperties Properties, if specified, which might indicate that SSL is to be used
     * @return ORB-related properties to define host and port for bootstrapping
     */
    private void prepareIIOP(final TargetServer[] targetServers, Properties containerProperties) {
        if (targetServers.length == 0) {
            throw new IllegalArgumentException();
        }

        final StringBuilder sb = new StringBuilder();
        for (TargetServer ts : targetServers) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(ts.getAddress()).append(":").append(ts.getPort());
        }

        /*
         * If the user has explicitly defined the ORB-related properties, do not override those settings.
         */
        if (targetServers.length == 1) {
            defineIfNotDefined(OMG_ORB_INIT_HOST_PROPERTY, targetServers[0].getAddress());
            defineIfNotDefined(OMG_ORB_INIT_PORT_PROPERTY, Integer.toString(targetServers[0].getPort()));
        } else {
            /*
             * Currently, set a system property to specify multiple endpoints.
             */
            defineIfNotDefined(ENDPOINTS_PROPERTY_NAME, sb.toString());
        }

        if (isSSLRequired(targetServers, containerProperties)) {
            orbHelper.setCSIv2Prop(ORB_SSL_CLIENT_REQUIRED, "true");
        }

        logger.log(CONFIG, "Using endpoint address(es): {0}", sb.toString());

    }

    /**
     * Define the specified system property using the new value unless the property is already set.
     *
     * @param propName name of the property to check and, possibly, set
     * @param newPropValue value to set if the property is not already set
     */
    private void defineIfNotDefined(final String propName, final String newPropValue) {
        if (System.getProperty(propName) == null) {
            if (newPropValue == null) {
                throw new RuntimeException(localStrings.getLocalString(AppClientContainerBuilder.class, "appclient.missingValue",
                        "Value for {0} expected but was not configured or assigned", new Object[] { propName }));

            }
            System.setProperty(propName, newPropValue);
        }
    }

    /**
     * Reports whether the ORB should be requested to use SSL.
     * <p>
     * If any TargetServer specifies SSL or the container-level properties specify SSL then report "true."
     *
     * @param targetServers configured TargetServer(s)
     * @param containerProperties configured container-level properties
     * @return whether the target servers or the properties implies the use of SSL
     */
    private boolean isSSLRequired(final TargetServer[] targetServers, final Properties containerProperties) {
        if (containerProperties != null) {
            String sslPropertyValue = containerProperties.getProperty("ssl");
            if ("required".equals(sslPropertyValue)) {
                return true;
            }
        }

        for (TargetServer targetServer : targetServers) {
            /*
             * If this target server has the optional security sub-item then the security sub-item must have an ssl sub-item. So we
             * can just look for the security sub-item.
             */
            if (targetServer.getSecurity() != null) {
                return true;
            }
        }

        return false;
    }

    @Override
    public AppClientContainerBuilder addMessageSecurityConfig(final MessageSecurityConfig msConfig) {
        messageSecurityConfigs.add(msConfig);
        return this;
    }

    @Override
    public List<MessageSecurityConfig> getMessageSecurityConfig() {
        return this.messageSecurityConfigs;
    }

    @Override
    public AppClientContainerBuilder logger(final Logger logger) {
        this.logger = logger;
        return this;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public AppClientContainerBuilder authRealm(final String className) {
        authRealm = new AuthRealm(className);
        return this;
    }

    @Override
    public AuthRealm getAuthRealm() {
        return authRealm;
    }

    @Override
    public AppClientContainerBuilder clientCredentials(final String user, final char[] password) {
        return clientCredentials(user, password, null);
    }

    @Override
    public AppClientContainerBuilder clientCredentials(final String user, final char[] password, final String realm) {
        ClientCredential cc = new ClientCredential(user, new XML.Password(password), realm);
        return clientCredentials(cc);
    }

    public AppClientContainerBuilder clientCredentials(final ClientCredential cc) {
        clientCredential = cc;
        return this;
    }

    @Override
    public ClientCredential getClientCredential() {
        return clientCredential;
    }

    @Override
    public AppClientContainerBuilder containerProperties(final Properties props) {
        this.containerProperties = props;
        return this;
    }

    @Override
    public AppClientContainerBuilder containerProperties(final List<Property> props) {
        containerProperties = XML.toProperties(props);
        return this;
    }

    @Override
    public Properties getContainerProperties() {
        return containerProperties;
    }

    @Override
    public AppClientContainerBuilder sendPassword(final boolean sendPassword) {
        this.sendPassword = sendPassword;
        return this;
    }

    @Override
    public boolean getSendPassword() {
        return sendPassword;
    }

    @Override
    public TargetServer[] getTargetServers() {
        return targetServers;
    }
}
