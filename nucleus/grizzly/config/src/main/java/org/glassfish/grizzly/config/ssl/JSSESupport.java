/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.glassfish.grizzly.config.ssl;

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
 * Concrete implementation class for JSSE Support classes.
 *
 * @author EKR
 * @author Craig R. McClanahan
 */
class JSSESupport implements SSLSupport {
    private static final Logger LOG = System.getLogger(JSSESupport.class.getName());

    /**
     * A mapping table to determine the number of effective bits in the key
     * when using a cipher suite containing the specified cipher name.  The
     * underlying data came from the TLS Specification (RFC 2246), Appendix C.
     */
    private static final CipherData[] ciphers = {
        new CipherData("_WITH_NULL_", 0),
        new CipherData("_WITH_IDEA_CBC_", 128),
        new CipherData("_WITH_RC2_CBC_40_", 40),
        new CipherData("_WITH_RC4_40_", 40),
        new CipherData("_WITH_RC4_128_", 128),
        new CipherData("_WITH_DES40_CBC_", 40),
        new CipherData("_WITH_DES_CBC_", 56),
        new CipherData("_WITH_3DES_EDE_CBC_", 168),
        new CipherData("_WITH_AES_128_", 128),
        new CipherData("_WITH_AES_256_", 256)
    };

    protected SSLSocket socket;

    /**
     * The SSLEngine used to support SSL over NIO.
     */
    protected SSLEngine sslEngine;


    /**
     * The SSLSession contains SSL information.
     */
    protected SSLSession session;

    JSSESupport(SSLSocket sock) {
        LOG.log(DEBUG, "JSSESupport(sock={0})", sock);
        socket = sock;
        session = socket.getSession();
    }


    JSSESupport(SSLEngine sslEngine) {
        LOG.log(DEBUG, "JSSESupport(sslEngine={0})", sslEngine);
        this.sslEngine = sslEngine;
        session = sslEngine.getSession();
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


    protected Certificate[] getX509Certificates(SSLSession session) throws IOException {
        Certificate[] jsseCerts = null;
        try {
            jsseCerts = session.getPeerCertificates();
        } catch (SSLPeerUnverifiedException ex) {
            // Get rid of the warning in the logs when no Client-Cert is available
        } catch (Exception e) {
            LOG.log(ERROR, "Cannot get peer certificates.", e);
            return null;
        }

        if (jsseCerts == null) {
            return null;
        }
        X509Certificate[] x509Certs = new X509Certificate[jsseCerts.length];
        for (int i = 0; i < x509Certs.length; i++) {
            final Certificate srcCertificate = jsseCerts[i];
            try {
                byte[] buffer = srcCertificate.getEncoded();
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
                X509Certificate certificate = (X509Certificate) cf.generateCertificate(stream);
                LOG.log(TRACE, "Cert #{0} = {1}", i, certificate);
                x509Certs[i] = certificate;
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


    @Override
    public Certificate[] getPeerCertificates(boolean force) throws IOException {
        if (session == null) {
            return null;
        }

        // Convert JSSE's certificate format to the ones we need
        Certificate[] jsseCerts = null;
        try {
            jsseCerts = session.getPeerCertificates();
        } catch (Exception bex) {
            // ignore.
        }
        if (jsseCerts == null) {
            jsseCerts = new X509Certificate[0];
        }
        if (jsseCerts.length <= 0 && force) {
            session.invalidate();
            handShake();
            if (socket == null) {
                session = sslEngine.getSession();
            } else {
                session = socket.getSession();
            }
        }
        return getX509Certificates(session);
    }


    protected void handShake() throws IOException {
        socket.setNeedClientAuth(true);
        socket.startHandshake();
    }


    /**
     * Copied from <code>org.apache.catalina.valves.CertificateValve</code>
     */
    @Override
    public Integer getKeySize() throws IOException {
        if (session == null) {
            return null;
        }
        Integer keySize = (Integer) session.getValue(KEY_SIZE_KEY);
        if (keySize == null) {
            int size = 0;
            String cipherSuite = session.getCipherSuite();
            for (CipherData element : ciphers) {
                if (cipherSuite.indexOf(element.phrase) >= 0) {
                    size = element.keySize;
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
        // Expose ssl_session (getId)
        byte[] ssl_session = session.getId();
        if (ssl_session == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder(32);
        for (byte element : ssl_session) {
            String digit = Integer.toHexString(element);
            if (digit.length() < 2) {
                buf.append('0');
            }
            if (digit.length() > 2) {
                digit = digit.substring(digit.length() - 2);
            }
            buf.append(digit);
        }
        return buf.toString();
    }
}
