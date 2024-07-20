/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2021 Oracle and/or its affiliates. All rights reserved.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.glassfish.grizzly.ssl.SSLSupport;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.TRACE;

/**
 * @author Sudarsan Sridhar
 */
public class GlassfishSSLSupport implements SSLSupport {

    private static final Logger LOG = System.getLogger(GlassfishSSLSupport.class.getName());

    /**
     * A mapping table to determine the number of effective bits in the key when using a cipher suite containing the specified cipher
     * name. The underlying data came from the TLS Specification (RFC 2246), Appendix C.
     */
    private static final CipherData ciphers[] = {
        new CipherData("_WITH_NULL_", 0),
        new CipherData("_WITH_IDEA_CBC_", 128),
        new CipherData("_WITH_RC2_CBC_40_", 40),
        new CipherData("_WITH_RC4_40_", 40),
        new CipherData("_WITH_RC4_128_", 128),
        new CipherData("_WITH_DES40_CBC_", 40),
        new CipherData("_WITH_DES_CBC_", 56),
        new CipherData("_WITH_3DES_EDE_CBC_", 168),
        new CipherData("_WITH_AES_128_", 128),
        new CipherData("_WITH_AES_256_", 256)};

    private final SSLSocket socket;
    private final SSLEngine engine;
    private SSLSession session;

    public GlassfishSSLSupport(SSLSocket socket) {
        this.socket = socket;
        this.engine = null;
        session = socket.getSession();
    }

    public GlassfishSSLSupport(SSLEngine engine) {
        this.socket = null;
        this.engine = engine;
        if (engine != null) {
            session = engine.getSession();
        }
    }

    @Override
    public String getCipherSuite() throws IOException {
        if (session == null) {
            return null;
        }
        return session.getCipherSuite();
    }

    @Override
    public Certificate[] getPeerCertificates() throws IOException {
        return getPeerCertificates(false);
    }

    @Override
    public Certificate[] getPeerCertificates(boolean force) throws IOException {
        if (session == null) {
            LOG.log(DEBUG, "SSL session is null, no certificates available.");
            return null;
        }
        Certificate[] certs = null;
        certs = session.getPeerCertificates();
        if (certs == null) {
            certs = new X509Certificate[0];
        }
        if (certs.length == 0 && force) {
            session.invalidate();
            handshake();

            if (socket == null) {
                session = engine.getSession();
            } else {
                session = socket.getSession();
            }
        }
        return getX509Certs();
    }

    @Override
    public Integer getKeySize() throws IOException {
        if (session == null) {
            return null;
        }
        Integer keySize = (Integer) session.getValue(KEY_SIZE_KEY);
        if (keySize == null) {
            int size = 0;
            String cipherSuite = session.getCipherSuite();
            for (CipherData cipher : ciphers) {
                if (cipherSuite.contains(cipher.phrase)) {
                    size = cipher.keySize;
                    break;
                }
            }
            keySize = size;
            session.putValue(KEY_SIZE_KEY, keySize);
        }
        return keySize;
    }

    @Override
    public String getSessionId() throws IOException {
        if (session == null) {
            return null;
        }
        byte[] sessionId = session.getId();
        if (sessionId == null) {
            return null;
        }
        StringBuilder Id = new StringBuilder();
        for (byte b : sessionId) {
            String digit = Integer.toHexString(b);
            if (digit.length() < 2) {
                Id.append('0');
            } else if (digit.length() > 2) {
                digit = digit.substring(digit.length() - 2);
            }
            Id.append(digit);
        }
        return Id.toString();
    }

    private void handshake() throws IOException {
        socket.setNeedClientAuth(true);
        socket.startHandshake();
    }

    private Certificate[] getX509Certs() {
        Certificate[] certs = null;
        try {
            certs = session.getPeerCertificates();
        } catch (SSLPeerUnverifiedException e) {
            LOG.log(TRACE, "Could not get peer certificates.", e);
            return null;
        }

        if (certs == null) {
            return null;
        }
        X509Certificate[] x509Certs = new X509Certificate[certs.length];
        for (int i = 0; i < x509Certs.length; i++) {
            final Certificate srcCertificate = certs[i];
            try {
                byte[] buffer = srcCertificate.getEncoded();
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
                X509Certificate certificate = (X509Certificate) cf.generateCertificate(stream);
                LOG.log(TRACE, "Cert #{0} = {1}", i, certificate);
            } catch (Exception ex) {
                LOG.log(ERROR, "Error translating " + srcCertificate, ex);
                return null;
            }
        }

        if (x509Certs.length < 1) {
            return null;
        }
        return x509Certs;
    }
}
