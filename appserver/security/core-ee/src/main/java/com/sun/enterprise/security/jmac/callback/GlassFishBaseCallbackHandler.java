/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.enterprise.security.jmac.callback;

import static com.sun.logging.LogDomains.SECURITY_LOGGER;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.util.logging.Logger;

import javax.crypto.SecretKey;

import org.glassfish.epicyro.config.helper.BaseCallbackHandler;
import org.glassfish.internal.api.Globals;
import org.glassfish.security.common.MasterPassword;

import com.sun.enterprise.security.SecurityServicesUtil;
import com.sun.enterprise.security.ssl.SSLUtils;
import com.sun.enterprise.security.store.PasswordAdapter;
import com.sun.enterprise.server.pluggable.SecuritySupport;
import com.sun.logging.LogDomains;

import jakarta.security.auth.message.callback.SecretKeyCallback;


public abstract class GlassFishBaseCallbackHandler extends BaseCallbackHandler {

    private static final Logger LOG = LogDomains.getLogger(GlassFishBaseCallbackHandler.class, SECURITY_LOGGER, false);

    private static final String CLIENT_SECRET_KEYSTORE = "com.sun.appserv.client.secretKeyStore";
    private static final String CLIENT_SECRET_KEYSTORE_PASSWORD = "com.sun.appserv.client.secretKeyStorePassword";

    protected final SSLUtils sslUtils;
    protected final SecuritySupport securitySupport;
    protected final MasterPassword masterPasswordHelper;

    protected GlassFishBaseCallbackHandler() {
        if (Globals.getDefaultHabitat() == null) {
            sslUtils = new SSLUtils();
            securitySupport = SecuritySupport.getDefaultInstance();
            masterPasswordHelper = null;
            sslUtils.postConstruct();
        } else {
            sslUtils = Globals.get(SSLUtils.class);
            securitySupport = Globals.get(SecuritySupport.class);
            masterPasswordHelper = Globals.getDefaultHabitat().getService(MasterPassword.class, "Security SSL Password Provider Service");
        }
    }

    @Override
    protected KeyStore getTrustStore() {
        return sslUtils.getMergedTrustStore();
    }

    @Override
    protected KeyStore[] getKeyStores() {
        return securitySupport.getKeyStores();
    }

    @Override
    protected PrivateKey getPrivateKeyForAlias(String alias, int keystoreIndex) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return securitySupport.getPrivateKeyForAlias(alias, keystoreIndex);
    }

    @Override
    protected PrivateKeyEntry getPrivateKeyEntryFromTokenAlias(String certNickname) throws Exception {
        return sslUtils.getPrivateKeyEntryFromTokenAlias(certNickname);
    }

    @Override
    protected SecretKey getPasswordSecretKeyForAlias(String alias) throws GeneralSecurityException {
        PasswordAdapter passwordAdapter = null;

        try {
            if (SecurityServicesUtil.getInstance().isACC()) {
                passwordAdapter = new PasswordAdapter(
                    System.getProperty(CLIENT_SECRET_KEYSTORE),
                    System.getProperty(CLIENT_SECRET_KEYSTORE_PASSWORD).toCharArray());
            } else {
                passwordAdapter = masterPasswordHelper.getMasterPasswordAdapter();
            }}
        catch (IOException e) {
            throw new GeneralSecurityException(e);
        }

        return passwordAdapter.getPasswordSecretKeyForAlias(alias);
    }

    @Override
    protected void processSecretKey(SecretKeyCallback secretKeyCallback) {
        LOG.log(FINE, "Jakarta Authentication: In SecretKeyCallback Processor");

        String alias = ((SecretKeyCallback.AliasRequest) secretKeyCallback.getRequest()).getAlias();
        if (alias != null) {
            try {
                PasswordAdapter passwordAdapter = null;
                if (SecurityServicesUtil.getInstance().isACC()) {
                    passwordAdapter = new PasswordAdapter(
                        System.getProperty(CLIENT_SECRET_KEYSTORE),
                        System.getProperty(CLIENT_SECRET_KEYSTORE_PASSWORD).toCharArray());
                } else {
                    passwordAdapter = masterPasswordHelper.getMasterPasswordAdapter();
                }

                secretKeyCallback.setKey(passwordAdapter.getPasswordSecretKeyForAlias(alias));
            } catch (Exception e) {
                LOG.log(FINE, e, () -> "Jakarta Authentication: In SecretKeyCallback Processor: " + " Error reading key ! for alias " + alias);
                secretKeyCallback.setKey(null);
            }
        } else {
            // Don't bother about checking for principal. We don't support that feature - typically
            // used in an environment with kerberos
            secretKeyCallback.setKey(null);
            LOG.log(WARNING, "No support to read Principals in SecretKeyCallback.");
        }
    }

}
