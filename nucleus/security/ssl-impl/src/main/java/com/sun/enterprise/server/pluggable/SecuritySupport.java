/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.server.pluggable;

import com.sun.enterprise.security.ssl.impl.SecuritySupportImpl;
import java.io.IOException;
import java.security.KeyStore;
//V3:Commented import com.sun.enterprise.config.ConfigContext;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import org.jvnet.hk2.annotations.Contract;

/**
 * SecuritySupport is part of PluggableFeature that provides access to internal services managed by application server.
 *
 * @author Shing Wai Chan
 */
@Contract
public abstract class SecuritySupport {

    public static final String KEYSTORE_PASS_PROP = "javax.net.ssl.keyStorePassword";
    public static final String TRUSTSTORE_PASS_PROP = "javax.net.ssl.trustStorePassword";
    public static final String KEYSTORE_TYPE_PROP = "javax.net.ssl.keyStoreType";
    public static final String TRUSTSTORE_TYPE_PROP = "javax.net.ssl.trustStoreType";
    public static final String keyStoreProp = "javax.net.ssl.keyStore";
    public static final String trustStoreProp = "javax.net.ssl.trustStore";

    private static volatile SecuritySupport defaultInstance;

    public static SecuritySupport getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new SecuritySupportImpl();
        }

        return defaultInstance;
    }

    /**
     * This method returns an array of keystores containing keys and certificates.
     */
    abstract public KeyStore[] getKeyStores();

    /**
     * This method returns an array of truststores containing certificates.
     */
    abstract public KeyStore[] getTrustStores();

    /**
     * @param token
     * @return a keystore. If token is null, return the the first keystore.
     */
    abstract public KeyStore getKeyStore(String token);

    /**
     * @param token
     * @return a truststore. If token is null, return the first truststore.
     */
    abstract public KeyStore getTrustStore(String token);

    /**
     * @param type
     * @param index
     * @return load a null keystore of given type.
     */
    abstract public KeyStore loadNullStore(String type, int index)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException;

    /**
     * @param masterPass
     * @return result whether the given master password is correct.
     */
    abstract public boolean verifyMasterPassword(final char[] masterPass);

    /**
     * @param algorithm
     * @return KeyManagers for the specified algorithm.
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    abstract public KeyManager[] getKeyManagers(String algorithm)
            throws IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException;

    /**
     * @param algorithm
     * @return TrustManagers for the specified algorithm.
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     */
    abstract public TrustManager[] getTrustManagers(String algorithm) throws IOException, KeyStoreException, NoSuchAlgorithmException;

    /**
     * Gets the PrivateKey for specified alias from the corresponding keystore indicated by the index.
     *
     * @param alias Alias for which the PrivateKey is desired.
     * @param keystoreIndex Index of the keystore.
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    abstract public PrivateKey getPrivateKeyForAlias(String alias, int keystoreIndex)
            throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException;

    /**
     * This method returns an array of token names in order corresponding to array of keystores.
     */
    abstract public String[] getTokenNames();

    /**
     * This method synchronize key file for given realm.
     *
     * @param config the ConfigContextx
     * @param fileRealmName
     * @exception if fail to synchronize, a known exception is
     * com.sun.enterprise.ee.synchronization.SynchronizationException
     */
    /** TODO:V3:Cluster ConfigContext is no longer present so find out what this needs to be */
    abstract public void synchronizeKeyFile(Object configContext, String fileRealmName) throws Exception;

}
