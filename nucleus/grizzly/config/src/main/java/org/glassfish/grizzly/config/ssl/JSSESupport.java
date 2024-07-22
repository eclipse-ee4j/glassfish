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
import java.io.InputStream;
import java.lang.System.Logger;
import java.net.SocketException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.glassfish.grizzly.ssl.SSLSupport;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;

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

    private SSLSocket socket;

    /**
     * The SSLEngine used to support SSL over NIO.
     */
    private SSLEngine sslEngine;


    /**
     * The SSLSession contains SSL information.
     */
    private SSLSession session;

    private final AtomicBoolean completed = new AtomicBoolean();


    JSSESupport(SSLSocket sock) {
        LOG.log(DEBUG, "JSSESupport(sock={0})", sock);
        this.socket = sock;
        this.session = socket.getSession();
        sock.addHandshakeCompletedListener(new Listener(completed));
    }


    JSSESupport(SSLEngine sslEngine) {
        LOG.log(DEBUG, "JSSESupport(sslEngine={0})", sslEngine);
        this.sslEngine = sslEngine;
        this.session = sslEngine.getSession();
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
            socket.setNeedClientAuth(true);
            synchronousHandshake(socket);
            if (socket == null) {
                session = sslEngine.getSession();
            } else {
                session = socket.getSession();
            }
        }
        return getX509Certificates();
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


    /**
     * @return the X509certificates or null if we can't get them.
     */
    private X509Certificate[] getX509Certificates() {
        final Certificate[] certs;
        try {
            certs = session.getPeerCertificates();
        } catch (Throwable t) {
            LOG.log(DEBUG, "Error getting client certs", t);
            return null;
        }
        if (certs == null) {
            return null;
        }

        X509Certificate[] x509Certs = new X509Certificate[certs.length];
        for (int i = 0; i < certs.length; i++) {
            final Certificate certificate = certs[i];
            if (certificate instanceof X509Certificate) {
                // always currently true with the JSSE 1.1.x
                x509Certs[i] = (X509Certificate) certificate;
            } else {
                try {
                    byte[] buffer = certificate.getEncoded();
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
                    x509Certs[i] = (X509Certificate) cf.generateCertificate(stream);
                } catch (Exception ex) {
                    LOG.log(ERROR, "Error translating cert " + certificate, ex);
                    return null;
                }
            }
            LOG.log(DEBUG, "Cert #{0} = {1}", i, x509Certs[i]);
        }
        if (x509Certs.length < 1) {
            return null;
        }
        return x509Certs;
    }


    /**
     * JSSE in JDK 1.4 has an issue/feature that requires us to do a read() to get the client-cert.
     *
     * As suggested by Andreas Sterbenz
     */
    private void synchronousHandshake(SSLSocket socket) throws IOException {
        InputStream in = socket.getInputStream();
        int oldTimeout = socket.getSoTimeout();
        socket.setSoTimeout(1000);
        byte[] b = new byte[0];
        completed.set(false);
        socket.startHandshake();
        int maxTries = 60; // 60 * 1000 = example 1 minute time out
        for (int i = 0; i < maxTries; i++) {
            LOG.log(DEBUG, "Reading for try #{0}", i);
            try {
                final int bytesRead = in.read(b);
                assert bytesRead <= 0;
            } catch(SSLException sslex) {
                //logger.log(Level.SEVERE,"SSL Error getting client Certs",sslex);
                throw sslex;
            } catch (IOException e) {
                // ignore - presumably the timeout
            }
            if (completed.get()) {
                break;
            }
        }
        socket.setSoTimeout(oldTimeout);
        if (!completed.get()) {
            throw new SocketException("SSL Cert handshake timeout");
        }
    }

    private static class Listener implements HandshakeCompletedListener {

        private final AtomicBoolean completed;

        Listener(AtomicBoolean completed) {
            this.completed = completed;
        }

        @Override
        public void handshakeCompleted(HandshakeCompletedEvent event) {
            completed.set(true);
        }
    }
}
