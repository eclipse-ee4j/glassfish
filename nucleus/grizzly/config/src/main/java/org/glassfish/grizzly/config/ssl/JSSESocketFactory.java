/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2007-2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertPathParameters;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;

import org.glassfish.grizzly.config.GrizzlyConfig;
import org.glassfish.grizzly.http.util.StringManager;

/**
 * SSL server socket factory. It _requires_ a valid RSA key and JSSE.
 *
 * @author Harish Prabandham
 * @author Costin Manolache
 * @author Stefan Freyr Stefansson
 * @author EKR -- renamed to JSSESocketFactory
 * @author Jan Luehe
 */
public class JSSESocketFactory implements Cloneable {
    private static final StringManager sm = StringManager.getManager(
            JSSESocketFactory.class.getPackage().getName(),
            JSSESocketFactory.class.getClassLoader());
    private static final String defaultProtocol = "TLS";
    private static final String defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
    private static final String defaultKeyPass = "changeit";
    private static final Logger logger = GrizzlyConfig.logger();

    private final Map<String, String> attributes = new HashMap<>();
    private boolean clientAuthNeed;
    private boolean clientAuthWant;
    private SSLServerSocketFactory sslProxy;
    private String[] enabledCiphers;


    /**
     * Reads the keystore and initializes the SSL socket factory.
     *
     * @return {@link SSLContext}
     */
    public SSLContext init() throws IOException {
        try {
            clientAuthNeed = Boolean.parseBoolean(getAttribute("clientAuthNeed"));
            clientAuthWant = Boolean.parseBoolean(getAttribute("clientAuthWant"));

            // SSL protocol variant (e.g., TLS, SSL v3, etc.)
            String protocol = getAttribute("protocol");
            if (protocol == null) {
                protocol = defaultProtocol;
            }
            // Certificate encoding algorithm (e.g., SunX509)
            String algorithm = getAttribute("algorithm");
            if (algorithm == null) {
                algorithm = defaultAlgorithm;
            }
            // Create and init SSLContext
            /* SJSAS 6439313
            SSLContext context = SSLContext.getInstance(protocol);
             */
            // START SJSAS 6439313
            SSLContext context = SSLContext.getInstance(protocol);
            // END SJSAS 6439313
            // Configure SSL session timeout and cache size
            configureSSLSessionContext(context.getServerSessionContext());
            String trustAlgorithm = getAttribute("truststoreAlgorithm");
            if (trustAlgorithm == null) {
                trustAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            }
            context.init(getKeyManagers(algorithm,
                getAttribute("keyAlias")),
                getTrustManagers(trustAlgorithm),
                new SecureRandom());
            // create proxy
            sslProxy = context.getServerSocketFactory();
            // Determine which cipher suites to enable
            String requestedCiphers = getAttribute("ciphers");
            if (requestedCiphers != null) {
                enabledCiphers = getEnabledCiphers(requestedCiphers,
                    sslProxy.getSupportedCipherSuites());
            }
            // Check the SSL config is ok
            checkConfig();
            return context;
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * General mechanism to pass attributes from the ServerConnector to the socket factory.
     *
     * Note that the "preferred" mechanism is to use bean setters and explicit methods, but this allows easy
     * configuration via server.xml or simple Properties
     *
     * @param name attribute name - null name will be ignored
     * @param value attribute value - null value will be ignored
     */
    public final void setAttribute(String name, String value) {
        if (name != null && value != null) {
            attributes.put(name, value);
        }
    }


    /**
     * @param name
     * @return value for the name
     */
    public final String getAttribute(final String name) {
        return attributes.get(name);
    }

    /**
     * Determines the SSL cipher suites to be enabled.
     *
     * @param requestedCiphers Comma-separated list of requested ciphers
     * @param supportedCiphers Array of supported ciphers
     *
     * @return Array of SSL cipher suites to be enabled, or null if none of the requested ciphers are supported
     */
    protected String[] getEnabledCiphers(String requestedCiphers, String[] supportedCiphers) {
        String[] enabled = null;
        if (requestedCiphers != null) {
            List<String> vec = null;
            String cipher = requestedCiphers;
            int index = requestedCiphers.indexOf(',');
            if (index != -1) {
                int fromIndex = 0;
                while (index != -1) {
                    cipher = requestedCiphers.substring(fromIndex, index).trim();
                    if (cipher.length() > 0) {
                        /*
                         * Check to see if the requested cipher is among the
                         * supported ciphers, i.e., may be enabled
                         */
                        for (int i = 0; supportedCiphers != null && i < supportedCiphers.length; i++) {
                            if (supportedCiphers[i].equals(cipher)) {
                                if (vec == null) {
                                    vec = new ArrayList<>();
                                }
                                vec.add(cipher);
                                break;
                            }
                        }
                    }
                    fromIndex = index + 1;
                    index = requestedCiphers.indexOf(',', fromIndex);
                } // while
                cipher = requestedCiphers.substring(fromIndex);
            }

            assert cipher != null;

            cipher = cipher.trim();
            if (cipher.length() > 0) {
                /*
                 * Check to see if the requested cipher is among the
                 * supported ciphers, i.e., may be enabled
                 */
                for (int i = 0; supportedCiphers != null
                    && i < supportedCiphers.length; i++) {
                    if (supportedCiphers[i].equals(cipher)) {
                        if (vec == null) {
                            vec = new ArrayList<>();
                        }
                        vec.add(cipher);
                        break;
                    }
                }
            }
            if (vec != null) {
                enabled = vec.toArray(new String[vec.size()]);
            }
        }
        return enabled;
    }

    /**
     * Gets the SSL server's keystore password.
     */
    private String getKeystorePassword() {
        String keyPass = getAttribute("keypass");
        if (keyPass == null) {
            keyPass = defaultKeyPass;
        }
        String keystorePass = getAttribute("keystorePass");
        if (keystorePass == null) {
            keystorePass = keyPass;
        }
        return keystorePass;
    }

    /**
     * @return the SSL server's keystore.
     */
    private KeyStore getKeystore(String pass) throws IOException {
        String keystoreFile = getAttribute("keystore");
        logger.log(Level.FINE, "Keystore file= {0}", keystoreFile);
        String keystoreType = getAttribute("keystoreType");
        logger.log(Level.FINE, "Keystore type= {0}", keystoreType);
        return getStore(keystoreType, keystoreFile, pass);
    }


    /**
     * @return the SSL server's truststore.
     */
    protected KeyStore getTrustStore() throws IOException {
        KeyStore ts = null;
        String truststore = getAttribute("truststore");
        logger.log(Level.FINE, "Truststore file= {0}", truststore);
        String truststoreType = getAttribute("truststoreType");
        logger.log(Level.FINE, "Truststore type= {0}", truststoreType);
        String truststorePassword = getTruststorePassword();
        if (truststore != null && truststorePassword != null) {
            ts = getStore(truststoreType, truststore, truststorePassword);
        }
        return ts;
    }


    /**
     * Gets the SSL server's truststore password.
     */
    private String getTruststorePassword() {
        String truststorePassword = getAttribute("truststorePass");
        if (truststorePassword == null) {
            truststorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
            if (truststorePassword == null) {
                truststorePassword = getKeystorePassword();
            }
        }
        return truststorePassword;
    }


    /**
     * @return the key or truststore with the specified type, path, and password.
     */
    private KeyStore getStore(String type, String path, String pass) throws IOException {
        KeyStore ks = null;
        InputStream istream = null;
        try {
            ks = KeyStore.getInstance(type);
            if (!("PKCS11".equalsIgnoreCase(type) ||
                "".equalsIgnoreCase(path))) {
                File keyStoreFile = new File(path);
                if (!keyStoreFile.isAbsolute()) {
                    keyStoreFile = new File(System.getProperty("catalina.base"),
                        path);
                }
                istream = new FileInputStream(keyStoreFile);
            }
            ks.load(istream, pass.toCharArray());
        } catch (FileNotFoundException fnfe) {
            logger.log(Level.SEVERE, sm.getString("jsse.keystore_load_failed", type, path, fnfe.getMessage()), fnfe);
            throw fnfe;
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, sm.getString("jsse.keystore_load_failed", type, path, ioe.getMessage()), ioe);
            throw ioe;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, sm.getString("jsse.keystore_load_failed", type, path, ex.getMessage()), ex);
            throw new IOException(sm.getString("jsse.keystore_load_failed", type, path, ex.getMessage()));
        } finally {
            if (istream != null) {
                try {
                    istream.close();
                } catch (IOException ioe) {
                    // Do nothing
                }
            }
        }
        return ks;
    }

    /**
     * Determines the SSL protocol variants to be enabled.
     *
     * @param socket The socket to get supported list from.
     * @param requestedProtocols Comma-separated list of requested SSL protocol variants
     *
     * @return Array of SSL protocol variants to be enabled, or null if none of the requested protocol variants are
     *         supported
     */
    protected String[] getEnabledProtocols(SSLServerSocket socket, String requestedProtocols) {
        String[] supportedProtocols = socket.getSupportedProtocols();
        String[] enabledProtocols = null;
        if (requestedProtocols != null) {
            List<String> vec = null;
            String protocol = requestedProtocols;
            int index = requestedProtocols.indexOf(',');
            if (index != -1) {
                int fromIndex = 0;
                while (index != -1) {
                    protocol = requestedProtocols.substring(fromIndex, index).trim();
                    if (supportedProtocols != null && protocol.length() > 0) {
                        /*
                         * Check to see if the requested protocol is among the
                         * supported protocols, i.e., may be enabled
                         */
                        for (String supportedProtocol : supportedProtocols) {
                            if (supportedProtocol.equals(protocol)) {
                                if (vec == null) {
                                    vec = new ArrayList<>();
                                }
                                vec.add(protocol);
                                break;
                            }
                        }
                    }
                    fromIndex = index + 1;
                    index = requestedProtocols.indexOf(',', fromIndex);
                } // while
                protocol = requestedProtocols.substring(fromIndex);
            }

            assert protocol != null;

            protocol = protocol.trim();
            if (protocol.length() > 0 && supportedProtocols != null) {
                /*
                 * Check to see if the requested protocol is among the
                 * supported protocols, i.e., may be enabled
                 */
                for (String supportedProtocol : supportedProtocols) {
                    if (supportedProtocol.equals(protocol)) {
                        if (vec == null) {
                            vec = new ArrayList<>();
                        }
                        vec.add(protocol);
                        break;
                    }
                }
            }
            if (vec != null) {
                enabledProtocols = vec.toArray(String[]::new);
            }
        }
        return enabledProtocols;
    }


    /**
     * Configures the given SSL server socket with the requested cipher suites, protocol versions, and need for client
     * authentication
     */
    private void initServerSocket(ServerSocket ssocket) {
        if (!(ssocket instanceof SSLServerSocket)) {
            throw new IllegalArgumentException("The ServerSocket has to be SSLServerSocket");
        }

        SSLServerSocket socket = (SSLServerSocket) ssocket;
        if (getAttribute("ciphers") != null) {
            socket.setEnabledCipherSuites(enabledCiphers);
        }
        String requestedProtocols = getAttribute("protocols");
        setEnabledProtocols(socket, getEnabledProtocols(socket,
            requestedProtocols));
        // we don't know if client auth is needed -
        // after parsing the request we may re-handshake
        if (clientAuthNeed) {
            socket.setNeedClientAuth(clientAuthNeed);
        } else {
            socket.setWantClientAuth(clientAuthWant);
        }
    }


    /**
     * Set the SSL protocol variants to be enabled.
     *
     * @param socket the SSLServerSocket.
     * @param protocols the protocols to use.
     */
    private void setEnabledProtocols(SSLServerSocket socket, String[] protocols) {
        if (protocols != null) {
            socket.setEnabledProtocols(protocols);
        }
    }


    /**
     * @return the initialized key managers.
     */
    protected KeyManager[] getKeyManagers(String algorithm, String keyAlias) throws Exception {
        String keystorePass = getKeystorePassword();
        KeyStore ks = getKeystore(keystorePass);
        if (keyAlias != null && !ks.isKeyEntry(keyAlias)) {
            throw new IOException(sm.getString("jsse.alias_no_key_entry", keyAlias));
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
        kmf.init(ks, keystorePass.toCharArray());
        KeyManager[] kms = kmf.getKeyManagers();
        if (keyAlias != null) {
            for (int i = 0; i < kms.length; i++) {
                kms[i] = new JSSEKeyManager((X509KeyManager) kms[i], keyAlias);
            }
        }
        return kms;
    }

    /**
     * @return the initialized trust managers.
     */
    protected TrustManager[] getTrustManagers(String algorithm) throws Exception {
        String crlFile = getAttribute("crlFile");
        TrustManager[] tms = null;
        KeyStore trustStore = getTrustStore();
        if (trustStore != null) {
            if (crlFile == null) {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
                tmf.init(trustStore);
                tms = tmf.getTrustManagers();
            } else {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
                CertPathParameters params = getParameters(algorithm, crlFile, trustStore);
                ManagerFactoryParameters mfp = new CertPathTrustManagerParameters(params);
                tmf.init(mfp);
                tms = tmf.getTrustManagers();
            }
        }
        return tms;
    }


    /**
     * Return the initialization parameters for the TrustManager. Currently, only the default <code>PKIX</code> is
     * supported.
     *
     * @param algorithm The algorithm to get parameters for.
     * @param crlf The path to the CRL file.
     * @param trustStore The configured TrustStore.
     *
     * @return The parameters including the CRLs and TrustStore.
     */
    private CertPathParameters getParameters(String algorithm, String crlf, KeyStore trustStore) throws Exception {
        CertPathParameters params;
        if ("PKIX".equalsIgnoreCase(algorithm)) {
            PKIXBuilderParameters xparams = new PKIXBuilderParameters(trustStore, new X509CertSelector());
            Collection<?> crls = getCRLs(crlf);
            CertStoreParameters csp = new CollectionCertStoreParameters(crls);
            CertStore store = CertStore.getInstance("Collection", csp);
            xparams.addCertStore(store);
            xparams.setRevocationEnabled(true);
            String trustLength = getAttribute("trustMaxCertLength");
            if (trustLength != null) {
                try {
                    xparams.setMaxPathLength(Integer.parseInt(trustLength));
                } catch (Exception ex) {
                    logger.warning("Bad maxCertLength: " + trustLength);
                }
            }
            params = xparams;
        } else {
            throw new CRLException("CRLs not supported for type: " + algorithm);
        }
        return params;
    }

    /**
     * Load the collection of CRLs.
     */
    private Collection<? extends CRL> getCRLs(String crlf) throws IOException, CRLException, CertificateException {
        File crlFile = new File(crlf);
        if (!crlFile.isAbsolute()) {
            crlFile = new File(System.getProperty("catalina.base"), crlf);
        }
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream is = new FileInputStream(crlFile);
        try {
            return cf.generateCRLs(is);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ignored) {
                }
            }
        }
    }


    /**
     * Configures the given SSLSessionContext.
     *
     * @param sslSessionCtxt The SSLSessionContext to configure
     */
    private void configureSSLSessionContext(SSLSessionContext sslSessionCtxt) {
        String attrValue = getAttribute("sslSessionTimeout");
        if (attrValue != null) {
            sslSessionCtxt.setSessionTimeout(
                Integer.parseInt(attrValue));
        }
        attrValue = getAttribute("ssl3SessionTimeout");
        if (attrValue != null) {
            sslSessionCtxt.setSessionTimeout(
                Integer.parseInt(attrValue));
        }
        attrValue = getAttribute("sslSessionCacheSize");
        if (attrValue != null) {
            sslSessionCtxt.setSessionCacheSize(
                Integer.parseInt(attrValue));
        }
    }


    /**
     * Checks that the certificate is compatible with the enabled cipher suites. If we don't check now, the JIoEndpoint
     * can enter a nasty logging loop. See bug 45528.
     */
    private void checkConfig() throws IOException {
        // Create an unbound server socket
        ServerSocket socket = sslProxy.createServerSocket();
        initServerSocket(socket);
        try {
            // Set the timeout to 1ms as all we care about is if it throws an
            // SSLException on accept.
            socket.setSoTimeout(1);
            socket.accept();
            // Will never get here - no client can connect to an unbound port
        } catch (SSLException ssle) {
            // SSL configuration is invalid. Possibly cert doesn't match ciphers
            throw new IOException(sm.getString("jsse.invalid_ssl_conf", ssle.getMessage()), ssle);
        } catch (Exception e) {
            /*
             * Possible ways of getting here
             * socket.accept() throws a SecurityException
             * socket.setSoTimeout() throws a SocketException
             * socket.accept() throws some other exception (after a JDK change)
             *      In these cases the test won't work so carry on - essentially
             *      the behaviour before this patch
             * socket.accept() throws a SocketTimeoutException
             *      In this case all is well so carry on
             */
        } finally {
            // Should be open here but just in case
            if (!socket.isClosed()) {
                socket.close();
            }
        }
    }
}
