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

package com.sun.enterprise.admin.util;

import com.sun.enterprise.security.store.AsadminTruststore;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

import java.io.Console;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import javax.net.ssl.X509TrustManager;

/**
 * An implementation of {@link X509TrustManager} that provides basic support for Trust Management. It checks if the
 * server is trusted and displays the certificate that was received from the server. The user is then prompted to
 * confirm the certificate. If confirmed, the certificate is entered into the client side asadmintruststore (default
 * name is ~/.gfclient/truststore). Once in the truststore, the user is never prompted to confirm a second time.
 */
public class AsadminTrustManager implements X509TrustManager {

    private final Object _alias;
    private boolean _alreadyInvoked;
    private boolean interactive = true;
    private CertificateException _lastCertException;
    private RuntimeException _lastRuntimeException;

    private static final LocalStringsImpl strmgr = new LocalStringsImpl(AsadminTrustManager.class);

    /**
     * Creates an instance of the AsadminTrustManager
     *
     * @param alias The toString() of the alias object concatenated with a date/time stamp is used as the alias of the
     * trusted server certificate in the client side trust store. When null, only a date / timestamp is used as an alias.
     */
    public AsadminTrustManager(Object alias, Map env) {
        _alias = alias;
        _alreadyInvoked = false;
        _lastCertException = null;
        _lastRuntimeException = null;
    }

    /**
     * Creates an instance of the SunOneBasicX509TrustManager A date/time stamp is used of the trusted server certificate in
     * the client side trust store.
     */
    public AsadminTrustManager() {
        this(null, null);
    }

    /**
     * Set the interactive mode for the trust manager. If false, it will not prompt for any confirmations and will just
     * trust certificates. By default it is true.
     */
    public void setInteractive(boolean mode) {
        interactive = mode;
    }

    /**
     * Checks if client is trusted given the certificate chain and authorization type string, e.g., "RSA".
     *
     * @throws {@link CertificateException}
     * @throws {@link UnsupportedOperationException}
     */
    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificate, String authType) throws CertificateException {
        throw new UnsupportedOperationException("Not Implemented for Client Trust Management");
    }

    /**
     * Checs if the server is trusted.
     *
     * @param chain The server certificate to be validated.
     * @param authType
     * @throws CertificateException
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        // The alreadyInvoked flag keeps track of whether we have already
        // prompted the user.  Unfortunately, checkServerTrusted is called
        // two times and we want to avoid prompting the user twice. I'm not
        // sure of the root cause of this problem (i.e., why it is called
        // twice).  In addition, we keep track of any exception that occurred
        // on the first invocation and propagate that back.
        if (!_alreadyInvoked) {
            _alreadyInvoked = true;
            try {
                checkCertificate(chain);
            } catch (RuntimeException ex) {
                _lastRuntimeException = ex;
                throw ex;
            } catch (CertificateException ex) {
                _lastCertException = ex;
                throw ex;
            }
        } else {
            if (_lastRuntimeException != null) {
                throw _lastRuntimeException;
            } else if (_lastCertException != null) {
                throw _lastCertException;
            }
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    /**
     * Displays the certificate and prompts the user whether or not it is trusted.
     *
     * @param c
     * @throws IOException
     * @return true if the user trusts the certificate
     */
    private boolean isItOKToAddCertToTrustStore(X509Certificate c) throws IOException {
        Console cons = System.console();
        if (!interactive || cons == null) {
            return true;
        }

        cons.printf("%s%n", c.toString());
        String result = cons.readLine("%s", strmgr.get("certificateTrustPrompt"));
        return result != null && result.equalsIgnoreCase("y");
    }

    private String getAliasName() {
        String aliasName = _alias != null ? _alias.toString() : "";
        // We append a timestamp to the alias to ensure that it is unqiue.
        DateFormat f = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        aliasName += ":" + f.format(new Date());
        return aliasName;
    }

    /**
     * This function validates the cert and ensures that it is trusted.
     *
     * @param chain
     * @throws RuntimeException
     * @throws CertificateException
     */
    protected void checkCertificate(X509Certificate[] chain) throws RuntimeException, CertificateException, IllegalArgumentException {
        if (chain == null || chain.length == 0) {
            throw new IllegalArgumentException(strmgr.get("emptyServerCertificate"));
        }
        // First ensure that the certificate is valid.
        for (int i = 0; i < chain.length; i++) {
            chain[i].checkValidity();
        }
        try {
            /*
             * Open the trust store, prompting the user if needed for a valid
             * password to use.
             */
            AsadminTruststore truststore = AsadminTruststore.newInstance();
            // if the certificate already exists in the truststore,
            // it is implicitly trusted
            if (!truststore.certificateExists(chain[0])) {
                // if the certificate does not exist in the truststore,
                // then we prompt the user.  Upon confirmation from the user,
                // the certificate is added to the truststore.
                if (isItOKToAddCertToTrustStore(chain[0])) {
                    truststore.addCertificate(getAliasName(), chain[0]);
                } else {
                    throw new CertificateException(strmgr.get("serverCertificateNotTrusted"));
                }
            }
        } catch (CertificateException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
