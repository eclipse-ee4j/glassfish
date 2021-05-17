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

package com.sun.enterprise.security.store;

import com.sun.enterprise.util.SystemPropertyConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.Certificate;


/**
 * This class implements an adapter for password manipulation a JCEKS.
 * <p>
 * This class delegates the work of actually opening the trust store to
 * AsadminSecurityUtil.
 *
 * @author Shing Wai Chan
 */
public class AsadminTruststore {
    private static final String ASADMIN_TRUSTSTORE = "truststore";
    private KeyStore _keyStore = null;
    private File _keyFile = null;
    private char[] _password = null;

    public static File getAsadminTruststore()
    {
        String location = System.getProperty(SystemPropertyConstants.CLIENT_TRUSTSTORE_PROPERTY);
        if (location == null) {
            return new File(AsadminSecurityUtil.getDefaultClientDir(), ASADMIN_TRUSTSTORE);
        } else {
            return new File(location);
        }
    }

    public static AsadminTruststore newInstance()
            throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        return AsadminSecurityUtil
                .getInstance(true /* isPromptable */)
                .getAsadminTruststore();
    }

    public static AsadminTruststore newInstance(final char[] password)
            throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        return AsadminSecurityUtil
                .getInstance(password, true /* isPromptable */)
                .getAsadminTruststore();
    }

    AsadminTruststore(final char[] password) throws CertificateException, IOException,
        KeyStoreException, NoSuchAlgorithmException
    {
        init(getAsadminTruststore(), password);
    }

    private void init(File keyfile, final char[] password)
        throws CertificateException, IOException,
        KeyStoreException, NoSuchAlgorithmException
    {
        _keyFile = keyfile;
        _keyStore = KeyStore.getInstance("JKS");
        _password = password;
        BufferedInputStream bInput = null;
        if (_keyFile.exists()) {
            bInput = new BufferedInputStream(new FileInputStream(_keyFile));
        }
        try {
            //load must be called with null to initialize an empty keystore
            _keyStore.load(bInput, _password);
            if (bInput != null) {
                bInput.close();
                bInput = null;
            }
        } finally {
             if (bInput != null) {
                 try {
                     bInput.close();
                 } catch(Exception ex) {
                     //ignore we are cleaning up
                 }
             }
        }
    }

    public boolean certificateExists(Certificate cert) throws KeyStoreException
    {
        return (_keyStore.getCertificateAlias(cert) == null ? false : true);
    }

    public void addCertificate(String alias, Certificate cert) throws KeyStoreException, IOException,
        NoSuchAlgorithmException, CertificateException
    {
        _keyStore.setCertificateEntry(alias, cert);
        writeStore();
    }

    public void writeStore() throws KeyStoreException, IOException,
        NoSuchAlgorithmException, CertificateException
    {
         BufferedOutputStream boutput = null;

         try {
             boutput = new BufferedOutputStream(
                     new FileOutputStream(_keyFile));
             _keyStore.store(boutput, _password);
             boutput.close();
             boutput = null;
         } finally {
             if (boutput != null) {
                 try {
                     boutput.close();
                 } catch(Exception ex) {
                     //ignore we are cleaning up
                 }
             }
         }
    }
}
