/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.appclient;

import com.sun.enterprise.iiop.security.IIOPSSLUtilImpl;
import com.sun.enterprise.security.SecurityServicesUtil;
import com.sun.enterprise.security.UsernamePasswordStore;
import com.sun.enterprise.security.appclient.integration.AppClientSecurityInfo;
import com.sun.enterprise.security.auth.login.LoginCallbackHandler;
import com.sun.enterprise.security.auth.login.LoginContextDriver;
import com.sun.enterprise.security.common.ClientSecurityContext;
import com.sun.enterprise.security.common.SecurityConstants;
import com.sun.enterprise.security.common.Util;
import com.sun.enterprise.security.ee.J2EESecurityManager;
import com.sun.enterprise.security.integration.AppClientSSL;
import com.sun.enterprise.security.ssl.SSLUtils;

import jakarta.inject.Inject;

import java.lang.System.Logger;
import java.util.List;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

import org.glassfish.appclient.client.acc.config.MessageSecurityConfig;
import org.glassfish.appclient.client.acc.config.Security;
import org.glassfish.appclient.client.acc.config.Ssl;
import org.glassfish.appclient.client.acc.config.TargetServer;
import org.glassfish.epicyro.config.factory.file.AuthConfigFileFactory;
import org.jvnet.hk2.annotations.Service;

import static jakarta.security.auth.message.config.AuthConfigFactory.DEFAULT_FACTORY_SECURITY_PROPERTY;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.WARNING;
import static org.glassfish.main.jdke.props.SystemProperties.setProperty;

/**
 *
 * @author Kumar
 */
@Service
public class AppClientSecurityInfoImpl implements AppClientSecurityInfo {

    private static final Logger LOG = System.getLogger(AppClientSecurityInfoImpl.class.getName());
    private static final String DEFAULT_PARSER_CLASS = "com.sun.enterprise.security.appclient.ConfigXMLParser";

    private CallbackHandler callbackHandler;
    boolean isJWS;
    boolean useGUIAuth;
    private List<TargetServer> targetServers;
    private List<MessageSecurityConfig> msgSecConfigs;

    @Inject
    private SSLUtils sslUtils;

    @Inject
    private SecurityServicesUtil securityServicesUtil;

    @Inject
    private Util util;

    @Inject
    private IIOPSSLUtilImpl appClientSSLUtil;

    @Override
    public void initializeSecurity(List<TargetServer> tServers, List<MessageSecurityConfig> configs, CallbackHandler handler, String username, char[] password, boolean isJWS, boolean useGUIAuth) {

        // Security init
        this.isJWS = isJWS;
        this.useGUIAuth = useGUIAuth;
        if (handler == null) {
            this.callbackHandler = new LoginCallbackHandler(useGUIAuth);
        } else {
            this.callbackHandler = handler;
        }
        this.targetServers = tServers;
        this.msgSecConfigs = configs;

        SecurityManager secMgr = System.getSecurityManager();
        if (!isJWS && secMgr != null && !J2EESecurityManager.class.equals(secMgr.getClass())) {
            J2EESecurityManager mgr = new J2EESecurityManager();
            System.setSecurityManager(mgr);
        }
        LOG.log(DEBUG, "SEC9002: ACC: Security Manager is {0}", secMgr);

        // Set the parser to ConfigXMLParser
        setProperty("config.parser", DEFAULT_PARSER_CLASS, true);
        util.setAppClientMsgSecConfigs(msgSecConfigs);

        try {
            // Setup Jakarta Authentication factory define default factory if it is not already defined
            String defaultFactory = java.security.Security.getProperty(DEFAULT_FACTORY_SECURITY_PROPERTY);
            LOG.log(DEBUG, "AuthConfigFactory obtained from java.security.Security.getProperty(\"authconfigprovider.factory\"): {0}",
                    defaultFactory);
            if (defaultFactory == null) {
                java.security.Security.setProperty(DEFAULT_FACTORY_SECURITY_PROPERTY, AuthConfigFileFactory.class.getName());
            }
        } catch (Exception e) {
            LOG.log(WARNING, "SEC9001: ACC: Error in initializing Jakarta Authentication Default Factory", e);
        }

        setSSLData(getTargetServers());
        if (username != null || password != null) {
            UsernamePasswordStore.set(username, password);
        }

        securityServicesUtil.setCallbackHandler(callbackHandler);
        util.setCallbackHandler(callbackHandler);
    }

    @Override
    public int getCredentialEncoding(CredentialType type) {
        switch (type) {
        case USERNAME_PASSWORD:
            return SecurityConstants.USERNAME_PASSWORD;
        case CERTIFICATE:
            return SecurityConstants.CERTIFICATE;
        case ALL:
            return SecurityConstants.ALL;
        default:
            throw new RuntimeException("Unknown CredentialType");
        }
    }

    @Override
    public Subject doClientLogin(CredentialType credType) {
        return LoginContextDriver.doClientLogin(this.getCredentialEncoding(credType), callbackHandler);
    }

    private AppClientSSL convert(Ssl ssl) {
        AppClientSSL appSSL = new AppClientSSL();
        appSSL.setCertNickname(ssl.getCertNickname());
        appSSL.setSsl2Ciphers(ssl.getSsl2Ciphers());
        appSSL.setSsl2Enabled(ssl.isSsl2Enabled());
        appSSL.setSsl3Enabled(ssl.isSsl3Enabled());
        appSSL.setSsl3TlsCiphers(ssl.getSsl3TlsCiphers());
        appSSL.setTlsEnabled(ssl.isTlsEnabled());
        appSSL.setTlsRollbackEnabled(ssl.isTlsRollbackEnabled());

        return appSSL;
    }

    private void setSSLData(List<TargetServer> tServers) {
        try {
            // Set the SSL related properties for ORB
            TargetServer tServer = tServers.get(0);

            // TargetServer is required.
            // temp solution to target-server+ change in DTD
            // assuming that multiple servers can be specified but only 1st
            // first one will be used.
            Security security = tServer.getSecurity();
            if (security == null) {
                LOG.log(DEBUG, "No Security input set in ClientContainer.xml");
                // do nothing
                return;
            }

            Ssl ssl = security.getSsl();
            if (ssl == null) {
                LOG.log(DEBUG, "No SSL input set in ClientContainer.xml");
                // do nothing
                return;

            }

            this.sslUtils.setAppclientSsl(convert(ssl));
        } catch (Exception ex) {
            LOG.log(ERROR, "setSSLData failed.", ex);
        }
    }

    public List<TargetServer> getTargetServers() {
        return targetServers;
    }

    public List<MessageSecurityConfig> getMsgSecConfigs() {
        return msgSecConfigs;
    }

    @Override
    public void clearClientSecurityContext() {
        ClientSecurityContext.setCurrent(null);
    }

    @Override
    public boolean isLoginCancelled() {
        boolean isCancelled = false;
        if (callbackHandler instanceof LoginCallbackHandler) {
            isCancelled = ((LoginCallbackHandler) callbackHandler).getCancelStatus();
        }

        return isCancelled;
    }
}
