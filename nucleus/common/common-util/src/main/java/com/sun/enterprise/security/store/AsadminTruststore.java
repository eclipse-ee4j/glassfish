/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.security.store;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import org.glassfish.main.jdke.security.KeyTool;

import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_TYPE_DEFAULT;
import static org.glassfish.embeddable.GlassFishVariable.TRUSTSTORE_FILE;

/**
 * This class implements an adapter for password manipulation a JCEKS.
 * <p>
 * This class delegates the work of actually opening the trust store to AsadminSecurityUtil.
 *
 * @author Shing Wai Chan
 */
public class AsadminTruststore {

    private static final String ASADMIN_TRUSTSTORE = "truststore";

    private final File keyFile;
    private final char[] password;
    private final KeyStore keyStore;

    public static AsadminTruststore newInstance()
        throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        return AsadminSecurityUtil.getInstance(true).getAsadminTruststore();
    }


    public static AsadminTruststore newInstance(final char[] password)
        throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        return AsadminSecurityUtil.getInstance(password, true).getAsadminTruststore();
    }


    AsadminTruststore(final char[] password)
        throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        this.keyFile = getAsadminTruststore();
        this.password = password;
        if (keyFile.exists()) {
            keyStore = new KeyTool(keyFile, password).loadKeyStore();
        } else {
            keyStore = KeyStore.getInstance(KEYSTORE_TYPE_DEFAULT);
            keyStore.load(null, password);
        }
    }


    public boolean certificateExists(Certificate cert) throws KeyStoreException {
        return keyStore.getCertificateAlias(cert) != null;
    }


    public void addCertificate(String alias, Certificate cert)
        throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        keyStore.setCertificateEntry(alias, cert);
        writeStore();
    }


    public void writeStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        try (BufferedOutputStream boutput = new BufferedOutputStream(new FileOutputStream(keyFile))) {
            keyStore.store(boutput, password);
        }
    }


    private static File getAsadminTruststore() {
        String location = System.getProperty(TRUSTSTORE_FILE.getSystemPropertyName());
        if (location == null) {
            return new File(AsadminSecurityUtil.GF_CLIENT_DIR, ASADMIN_TRUSTSTORE);
        }
        return new File(location);
    }
}
