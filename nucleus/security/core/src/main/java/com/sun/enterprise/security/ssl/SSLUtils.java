/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.ssl;

import com.sun.enterprise.security.integration.AppClientSSL;
import com.sun.enterprise.server.pluggable.SecuritySupport;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;

import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_TYPE_DEFAULT;
import static java.util.Collections.list;
import static org.glassfish.embeddable.GlassFishVariable.KEYSTORE_TYPE;
import static org.glassfish.embeddable.GlassFishVariable.TRUSTSTORE_TYPE;

/**
 * Handy class containing static functions.
 *
 * @author Harpreet Singh
 * @author Vivek Nagar
 * @author Shing Wai Chan
 */
@Service
@Singleton
public final class SSLUtils {
    public static final String HTTPS_OUTBOUND_KEY_ALIAS = "com.sun.enterprise.security.httpsOutboundKeyAlias";
    private static final String DEFAULT_SSL_PROTOCOL = "TLS";

    @Inject
    private SecuritySupport securitySupport;

    private boolean hasKey;
    private KeyStore mergedTrustStore;
    private AppClientSSL appclientSsl;
    private SSLContext sslContext;

    @PostConstruct
    private void init() {
        try {
            KeyStore[] keyStores = getKeyStores();
            if (keyStores != null) {
                for (KeyStore keyStore : keyStores) {
                    for (String alias : list(keyStore.aliases())) {
                        if (keyStore.isKeyEntry(alias)) {
                            hasKey = true;
                            break;
                        }
                    }

                    if (hasKey) {
                        break;
                    }
                }
            }

            mergedTrustStore = mergingTrustStores(securitySupport.getTrustStores());
            getSSLContext(null, null, null);
        } catch (Exception ex) {
            throw new IllegalStateException("SSLUtils static init fails.", ex);
        }
    }

    SSLContext getSSLContext(String protocol, String algorithm, String trustAlgorithm) {
        try {
            // Creating a default SSLContext and HttpsURLConnection for clients
            // that use Https
            if (protocol == null) {
                protocol = DEFAULT_SSL_PROTOCOL;
            }

            sslContext = SSLContext.getInstance(protocol);
            String keyAlias = System.getProperty(HTTPS_OUTBOUND_KEY_ALIAS);
            KeyManager[] keyManagers = getKeyManagers(algorithm);
            if (keyAlias != null && keyAlias.length() > 0 && keyManagers != null) {
                for (int i = 0; i < keyManagers.length; i++) {
                    keyManagers[i] = new J2EEKeyManager((X509KeyManager) keyManagers[i], keyAlias);
                }
            }

            sslContext.init(keyManagers, getTrustManagers(trustAlgorithm), null);

            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

            //refer issue :http://java.net/jira/browse/GLASSFISH-15369
            SSLContext.setDefault(sslContext);
        } catch (Exception e) {
            throw new Error(e);
        }

        return sslContext;
    }

    public boolean verifyMasterPassword(final char[] masterPass) {
        return securitySupport.verifyMasterPassword(masterPass);
    }

    public KeyStore[] getKeyStores() throws IOException {
        return securitySupport.getKeyStores();
    }

    public KeyStore getKeyStore() throws IOException {
        return getKeyStores()[0];
    }

    public KeyStore[] getTrustStores() throws IOException {
        return securitySupport.getTrustStores();
    }

    public KeyStore getTrustStore() throws IOException {
        return getTrustStores()[0];
    }

    /**
     * This API was supposedly for temporary purpose, but it's been in use for some 15 years.
     * Someone once thought it would be removed once Jakarta Authentication is update, but never
     * made clear why or how that would work.
     */
    public KeyStore getMergedTrustStore() {
        return mergedTrustStore;
    }

    public KeyManager[] getKeyManagers() throws Exception {
        return getKeyManagers(null);
    }

    public KeyManager[] getKeyManagers(String algorithm) throws IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return securitySupport.getKeyManagers(algorithm);
    }

    public TrustManager[] getTrustManagers() throws Exception {
        return getTrustManagers(null);
    }

    public TrustManager[] getTrustManagers(String algorithm) throws IOException, KeyStoreException, NoSuchAlgorithmException {
        return securitySupport.getTrustManagers(algorithm);
    }

    public void setAppclientSsl(AppClientSSL ssl) {
        appclientSsl = ssl;
    }

    public AppClientSSL getAppclientSsl() {
        return appclientSsl;
    }

    public static String getKeyStoreType() {
        return System.getProperty(KEYSTORE_TYPE.getSystemPropertyName(), KEYSTORE_TYPE_DEFAULT);
    }

    public static String getTrustStoreType() {
        return System.getProperty(TRUSTSTORE_TYPE.getSystemPropertyName(), KEYSTORE_TYPE_DEFAULT);
    }

    /**
     * This method checks whether a private key is available or not.
     */
    public boolean isKeyAvailable() {
        return hasKey;
    }

    /**
     * Check whether given String is of the form [&lt;TokenName&gt;:]alias where alias is an key entry.
     *
     * @param certNickname
     * @return boolean
     */
    public boolean isTokenKeyAlias(String certNickname) throws Exception {
        boolean isTokenKeyAlias = false;

        if (certNickname != null) {
            int ind = certNickname.indexOf(':');
            KeyStore[] keyStores = getKeyStores();
            int count = -1;
            String aliasName = null;
            if (ind != -1) {
                String[] tokens = securitySupport.getTokenNames();
                String tokenName = certNickname.substring(0, ind);
                aliasName = certNickname.substring(ind + 1);
                for (int i = 0; i < tokens.length; i++) {
                    if (tokenName.equals(tokens[i])) {
                        count = i;
                    }
                }
            }

            if (count != -1) {
                isTokenKeyAlias = keyStores[count].isKeyEntry(aliasName);
            } else {
                for (KeyStore keyStore : keyStores) {
                    if (keyStore.isKeyEntry(certNickname)) {
                        isTokenKeyAlias = true;
                        break;
                    }
                }
            }
        }

        return isTokenKeyAlias;
    }

    /**
     * Get a PrivateKeyEntry with certNickName is of the form [&lt;TokenName&gt;:]alias where alias is an key entry.
     *
     * @param certNickname
     * @return PrivateKeyEntry
     */
    public PrivateKeyEntry getPrivateKeyEntryFromTokenAlias(String certNickname) throws Exception {
        PrivateKeyEntry privateKeyEntry = null;

        if (certNickname != null) {
            int ind = certNickname.indexOf(':');
            KeyStore[] keyStores = getKeyStores();
            int count = -1;
            String aliasName = certNickname;
            if (ind != -1) {
                String[] tokens = securitySupport.getTokenNames();
                String tokenName = certNickname.substring(0, ind);
                aliasName = certNickname.substring(ind + 1);
                for (int i = 0; i < tokens.length; i++) {
                    if (tokenName.equals(tokens[i])) {
                        count = i;
                    }
                }
            }

            if (count != -1 && keyStores.length >= count) {
                PrivateKey privateKey = securitySupport.getPrivateKeyForAlias(aliasName, count);
                if (privateKey != null) {
                    privateKeyEntry = new PrivateKeyEntry(privateKey, keyStores[count].getCertificateChain(aliasName));
                }
            } else {
                for (int i = 0; i < keyStores.length; i++) {
                    PrivateKey privateKey = securitySupport.getPrivateKeyForAlias(aliasName, i);
                    if (privateKey != null) {
                        privateKeyEntry = new PrivateKeyEntry(privateKey, keyStores[i].getCertificateChain(aliasName));
                        break;
                    }
                }
            }
        }

        return privateKeyEntry;
    }

    public String[] getSupportedCipherSuites() {
        // PostConstruct is already setting this.
        return HttpsURLConnection.getDefaultSSLSocketFactory().getSupportedCipherSuites();
    }

    private KeyStore mergingTrustStores(KeyStore[] trustStores)
        throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        KeyStore mergedStore;
        try {
            mergedStore = securitySupport.loadNullStore("CaseExactJKS", securitySupport.getKeyStores().length - 1);
        } catch (KeyStoreException ex) {
            mergedStore = securitySupport.loadNullStore(KEYSTORE_TYPE_DEFAULT, securitySupport.getKeyStores().length - 1);
        }

        String[] tokens = securitySupport.getTokenNames();
        for (int i = 0; i < trustStores.length; i++) {
            Enumeration aliases = trustStores[i].aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                Certificate cert = trustStores[i].getCertificate(alias);

                // Need to preserve the token:alias name format
                String alias2 = (i < tokens.length - 1) ? tokens[i] + ":" + alias : alias;

                String alias3 = alias2;
                boolean alreadyInStore = false;
                Certificate aCert;
                int count = 1;
                while ((aCert = mergedStore.getCertificate(alias3)) != null) {
                    if (aCert.equals(cert)) {
                        alreadyInStore = true;
                        break;
                    }
                    alias3 = alias2 + "__" + count++;
                }

                if (!alreadyInStore) {
                    mergedStore.setCertificateEntry(alias3, cert);
                }
            }
        }

        return mergedStore;
    }

    /**
     *
     *
     * @param alias the admin key alias
     * @param protocol the protocol or null, uses "TLS" if this argument is null.
     * @return the SSLSocketFactory from the initialized SSLContext
     */
    public SSLSocketFactory getAdminSocketFactory(String alias, String protocol) {
        return getAdminSSLContext(alias, protocol).getSocketFactory();
    }


    /**
     * @param alias the admin key alias
     * @param protocol the protocol or null, uses "TLS" if this argument is null.
     * @return the initialized SSLContext
     */
    public SSLContext getAdminSSLContext(String alias, String protocol) {
        try {
            if (protocol == null) {
                protocol = "TLS";
            }

            SSLContext adminSSLContext = SSLContext.getInstance(protocol);
            KeyManager[] keyManagers = getKeyManagers();
            if (alias != null && alias.length() > 0 && keyManagers != null) {
                for (int i = 0; i < keyManagers.length; i++) {
                    keyManagers[i] = new J2EEKeyManager((X509KeyManager) keyManagers[i], alias);
                }
            }
            adminSSLContext.init(keyManagers, getTrustManagers(), null);

            return adminSSLContext;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
