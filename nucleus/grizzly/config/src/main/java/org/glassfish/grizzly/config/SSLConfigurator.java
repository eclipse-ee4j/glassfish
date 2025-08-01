/*
 * Copyright (c) 2023, 2025 Contributors to Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.config;

import java.io.IOException;
import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.Ssl;
import org.glassfish.grizzly.config.ssl.SSLContextFactory;
import org.glassfish.grizzly.config.ssl.SSLImplementation;
import org.glassfish.grizzly.localization.LogMessages;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.hk2.api.ServiceLocator;

import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_PASSWORD_DEFAULT;
import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_TYPE_DEFAULT;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;
import static org.glassfish.embeddable.GlassFishVariable.KEYSTORE_FILE;
import static org.glassfish.embeddable.GlassFishVariable.KEYSTORE_PASSWORD;
import static org.glassfish.embeddable.GlassFishVariable.KEYSTORE_TYPE;
import static org.glassfish.embeddable.GlassFishVariable.TRUSTSTORE_FILE;
import static org.glassfish.embeddable.GlassFishVariable.TRUSTSTORE_PASSWORD;
import static org.glassfish.embeddable.GlassFishVariable.TRUSTSTORE_TYPE;

/**
 * @author oleksiys
 */
public class SSLConfigurator extends SSLEngineConfigurator {

    private static final String PLAIN_PASSWORD_PROVIDER_NAME = "plain";
    private static final Logger LOG = System.getLogger(SSLConfigurator.class.getName());

    /**
     * SSL settings
     */
    private final Ssl ssl;
    private final ServiceLocator serviceLocator;

    public SSLConfigurator(final ServiceLocator serviceLocator, final Ssl ssl) {
        this.ssl = ssl;
        this.serviceLocator = serviceLocator;

        if (isWantClientAuth(ssl)) {
            setWantClientAuth(true);
        }

        if (isNeedClientAuth(ssl)) {
            setNeedClientAuth(true);
        }

        clientMode = false;
        sslContextConfiguration = new InternalSSLContextConfigurator();
    }

    /**
     * @return the {@link SSLImplementation}
     */
    private SSLImplementation getSslImplementation() {
        final SSLImplementation implementation = serviceLocator.getService(SSLImplementation.class);
        if (implementation != null) {
            LOG.log(DEBUG, () -> "Found SSL implementation: " + implementation);
            return implementation;
        }
        final String classname = ssl.getClassname() == null ? "com.sun.enterprise.security.ssl.GlassfishSSLImpl"
            : ssl.getClassname();
        LOG.log(DEBUG, () -> "Creating SSL implementation: " + classname);
        try {
            return (SSLImplementation) Class.forName(classname).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create SSL implementation: " + classname, e);
        }
    }

    /**
     * Configures the SSL properties on the given PECoyoteConnector from the SSL config of the given HTTP listener.
     */
    private SSLContext configureSSL() {
        try {
            final SSLContext newSslContext = initializeSSLContext();

            if (ssl != null) {
                final List<String> enabledProtocols = getListOfEnabledProtocols();
                if (enabledProtocols.isEmpty()) {
                    logEmptyWarning(ssl, "protocol variants");
                } else {
                    setEnabledProtocols(enabledProtocols.toArray(String[]::new));
                }

                final Set<String> ciphers = getSetOfEnabledCiphers();
                if (ciphers.isEmpty()) {
                    logEmptyWarning(ssl, "cipher suites");
                } else {
                    setEnabledCipherSuites(ciphers.toArray(String[]::new));
                }
            }

            if (LOG.isLoggable(DEBUG)) {
                LOG.log(DEBUG, "Enabled secure protocols={0} and ciphers={1}", Arrays.toString(getEnabledProtocols()),
                    Arrays.toString(getEnabledCipherSuites()));
            }

            return newSslContext;
        } catch (Exception e) {
            LOG.log(WARNING, LogMessages.WARNING_GRIZZLY_CONFIG_SSL_GENERAL_CONFIG_ERROR(), e);
            return null;
        }
    }

    private SSLContext initializeSSLContext() {
        try {
            final SSLContextFactory sslContextFactory = getSslImplementation().getSSLContextFactory();
            if (ssl != null) {
                if (ssl.getCrlFile() != null) {
                    setAttribute(sslContextFactory, "crlFile", ssl.getCrlFile(), null, null);
                }
                if (ssl.getTrustAlgorithm() != null) {
                    setAttribute(sslContextFactory, "truststoreAlgorithm", ssl.getTrustAlgorithm(), null, null);
                }
                if (ssl.getKeyAlgorithm() != null) {
                    setAttribute(sslContextFactory, "algorithm", ssl.getKeyAlgorithm(), null, null);
                }
                setAttribute(sslContextFactory, "trustMaxCertLength", ssl.getTrustMaxCertLength(), null, null);
            }

            // Key store settings
            setAttribute(sslContextFactory, "keystore", ssl == null ? null : ssl.getKeyStore(),
                KEYSTORE_FILE.getSystemPropertyName(), null);
            setAttribute(sslContextFactory, "keystoreType", ssl == null ? null : ssl.getKeyStoreType(),
                KEYSTORE_TYPE.getSystemPropertyName(), KEYSTORE_TYPE_DEFAULT);
            setAttribute(sslContextFactory, "keystorePass", ssl == null ? null : getKeyStorePassword(ssl),
                KEYSTORE_PASSWORD.getSystemPropertyName(), KEYSTORE_PASSWORD_DEFAULT);

            // Trust store settings
            setAttribute(sslContextFactory, "truststore", ssl == null ? null : ssl.getTrustStore(),
                TRUSTSTORE_FILE.getSystemPropertyName(), null);
            setAttribute(sslContextFactory, "truststoreType", ssl == null ? null : ssl.getTrustStoreType(),
                TRUSTSTORE_TYPE.getSystemPropertyName(), KEYSTORE_TYPE_DEFAULT);
            setAttribute(sslContextFactory, "truststorePass", ssl == null ? null : getTrustStorePassword(ssl),
                TRUSTSTORE_PASSWORD.getSystemPropertyName(), KEYSTORE_PASSWORD_DEFAULT);

            // Cert nick name
            sslContextFactory.setAttribute("keyAlias", ssl == null ? null : ssl.getCertNickname());
            SSLContext newSslContext = sslContextFactory.create();
            CipherInfo.updateCiphers(newSslContext);
            return newSslContext;
        } catch (IOException e) {
            LOG.log(WARNING, LogMessages.WARNING_GRIZZLY_CONFIG_SSL_GENERAL_CONFIG_ERROR(), e);
            return null;
        }
    }

    private List<String> getListOfEnabledProtocols() {
        final List<String> enabledProtocols = new ArrayList<>(8);
        // ssl protocol variants
        if (Boolean.parseBoolean(ssl.getSsl2Enabled())) {
            enabledProtocols.add("SSLv2");
        }
        if (Boolean.parseBoolean(ssl.getSsl3Enabled())) {
            enabledProtocols.add("SSLv3");
        }
        if (Boolean.parseBoolean(ssl.getTlsEnabled())) {
            enabledProtocols.add("TLSv1");
        }
        if (Boolean.parseBoolean(ssl.getTls11Enabled())) {
            enabledProtocols.add("TLSv1.1");
        }
        if (Boolean.parseBoolean(ssl.getTls12Enabled())) {
            enabledProtocols.add("TLSv1.2");
        }
        if (Boolean.parseBoolean(ssl.getTls13Enabled())) {
            enabledProtocols.add("TLSv1.3");
        }
        if (Boolean.parseBoolean(ssl.getSsl3Enabled()) || Boolean.parseBoolean(ssl.getTlsEnabled())) {
            enabledProtocols.add("SSLv2Hello");
        }
        return enabledProtocols;
    }

    private Set<String> getSetOfEnabledCiphers() {
        final List<String> enabledCiphers = new LinkedList<>();
        // ssl3-tls-ciphers
        final String ssl3Ciphers = ssl.getSsl3TlsCiphers();
        if (ssl3Ciphers != null && ssl3Ciphers.length() > 0) {
            final String[] ssl3CiphersArray = ssl3Ciphers.split(",");
            for (final String cipher : ssl3CiphersArray) {
                enabledCiphers.add(cipher.trim());
            }
        }

        // ssl2-tls-ciphers
        final String ssl2Ciphers = ssl.getSsl2Ciphers();
        if (ssl2Ciphers != null && ssl2Ciphers.length() > 0) {
            final String[] ssl2CiphersArray = ssl2Ciphers.split(",");
            for (final String cipher : ssl2CiphersArray) {
                enabledCiphers.add(cipher.trim());
            }
        }

        return getJSSECiphers(enabledCiphers);
    }

    protected void logEmptyWarning(Ssl ssl, String type) {
        if (!LOG.isLoggable(WARNING)) {
            return;
        }
        final StringBuilder name = new StringBuilder();
        for (NetworkListener listener : ((Protocol) ssl.getParent()).findNetworkListeners()) {
            if (name.length() != 0) {
                name.append(", ");
            }
            name.append(listener.getName());
        }
        LOG.log(WARNING, () -> "WEB0307: All SSL " + type + " disabled for network-listener " + name
            + ", using SSL implementation specific defaults");
    }

    public boolean isAllowLazyInit() {
        return ssl == null || Boolean.parseBoolean(ssl.getAllowLazyInit());
    }

    private String getKeyStorePassword(Ssl ssl) {
        if (PLAIN_PASSWORD_PROVIDER_NAME.equalsIgnoreCase(ssl.getKeyStorePasswordProvider())) {
            return ssl.getKeyStorePassword();
        }
        return getStorePasswordCustom(ssl.getKeyStorePassword());
    }

    private String getTrustStorePassword(Ssl ssl) {
        if (PLAIN_PASSWORD_PROVIDER_NAME.equalsIgnoreCase(ssl.getTrustStorePasswordProvider())) {
            return ssl.getTrustStorePassword();
        }
        return getStorePasswordCustom(ssl.getTrustStorePassword());
    }

    private String getStorePasswordCustom(String storePasswordProvider) {
        try {
            final SecurePasswordProvider provider =
                    (SecurePasswordProvider) Utils.newInstance(storePasswordProvider);

            assert provider != null;
            return provider.getPassword();
        } catch (Exception e) {
            if (LOG.isLoggable(WARNING)) {
                LOG.log(WARNING,
                        LogMessages.WARNING_GRIZZLY_CONFIG_SSL_SECURE_PASSWORD_INITIALIZATION_ERROR(storePasswordProvider),
                        e);
            }
        }
        return null;
    }

    private static void setAttribute(final SSLContextFactory sslContextFactory, final String name, final String value,
        final String property, final String defaultValue) {
        sslContextFactory.setAttribute(name, value == null ? System.getProperty(property, defaultValue) : value);
    }

    private static boolean isWantClientAuth(final Ssl ssl) {
        final String auth = ssl.getClientAuth();
        return auth != null && "want".equalsIgnoreCase(auth.trim());

    }

    private static boolean isNeedClientAuth(final Ssl ssl) {
        if (Boolean.parseBoolean(ssl.getClientAuthEnabled())) {
            return true;
        }

        final String auth = ssl.getClientAuth();
        return auth != null && "need".equalsIgnoreCase(auth.trim());

    }

    /**
     * Evaluates the given List of cipher suite names, converts each cipher suite that is enabled (i.e., not preceded by
     * a '-') to the corresponding JSSE cipher suite name, and returns a String[] of enabled cipher suites.
     *
     * @param configuredCiphers List of SSL ciphers to evaluate.
     *
     * @return String[] of cipher suite names, or null if none of the cipher suites in the given List are enabled or can
     *         be mapped to corresponding JSSE cipher suite names
     */
    private static Set<String> getJSSECiphers(final List<String> configuredCiphers) {
        final Set<String> enabledCiphers = new HashSet<>(configuredCiphers.size());
        for (String cipher : configuredCiphers) {
            if (!cipher.isEmpty() && cipher.charAt(0) != '-') {
                if (cipher.charAt(0) == '+') {
                    cipher = cipher.substring(1);
                }
                final String jsseCipher = getJSSECipher(cipher);
                if (jsseCipher == null) {
                    LOG.log(WARNING, LogMessages.WARNING_GRIZZLY_CONFIG_SSL_UNKNOWN_CIPHER_ERROR(cipher));
                } else {
                    enabledCiphers.add(jsseCipher);
                }
            }
        }
        return enabledCiphers;
    }


    /**
     * Converts the given cipher suite name to the corresponding JSSE cipher.
     *
     * @param cipher The cipher suite name to convert
     * @return The corresponding JSSE cipher suite name, or null if the given
     *         cipher suite name can not be mapped
     */
    private static String getJSSECipher(final String cipher) {
        final CipherInfo ci = CipherInfo.getCipherInfo(cipher);
        return ci == null ? null : ci.getCipherName();

    }
    // ---------------------------------------------------------- Nested Classes

    private final class InternalSSLContextConfigurator extends SSLContextConfigurator {

        private InternalSSLContextConfigurator() {
            super(false);
        }

        @Override
        public SSLContext createSSLContext() {
            return configureSSL();
        }

        @Override
        public SSLContext createSSLContext(final boolean throwException) {
            return configureSSL();
        }

        @Override
        public boolean validateConfiguration(boolean needsKeyStore) {
            return super.validateConfiguration(needsKeyStore);
        }

        @Override
        public void setKeyManagerFactoryAlgorithm(String keyManagerFactoryAlgorithm) {
            throw new IllegalStateException("The configuration is immutable");
        }

        @Override
        public void setKeyPass(String keyPass) {
            throw new IllegalStateException("The configuration is immutable");
        }

        @Override
        public void setKeyPass(char[] keyPass) {
            throw new IllegalStateException("The configuration is immutable");
        }

        @Override
        public void setKeyStoreFile(String keyStoreFile) {
            throw new IllegalStateException("The configuration is immutable");
        }

        @Override
        public void setKeyStorePass(String keyStorePass) {
            throw new IllegalStateException("The configuration is immutable");
        }

        @Override
        public void setKeyStorePass(char[] keyStorePass) {
            throw new IllegalStateException("The configuration is immutable");
        }

        @Override
        public void setKeyStoreProvider(String keyStoreProvider) {
            throw new IllegalStateException("The configuration is immutable");
        }

        @Override
        public void setKeyStoreType(String keyStoreType) {
            throw new IllegalStateException("The configuration is immutable");
        }

        @Override
        public void setSecurityProtocol(String securityProtocol) {
            throw new IllegalStateException("The configuration is immutable");
        }

        @Override
        public void setTrustManagerFactoryAlgorithm(String trustManagerFactoryAlgorithm) {
            throw new IllegalStateException("The configuration is immutable");
        }

        @Override
        public void setTrustStoreFile(String trustStoreFile) {
            throw new IllegalStateException("The configuration is immutable");
        }

        @Override
        public void setTrustStorePass(String trustStorePass) {
            throw new IllegalStateException("The configuration is immutable");
        }

        @Override
        public void setTrustStoreProvider(String trustStoreProvider) {
            throw new IllegalStateException("The configuration is immutable");
        }

        @Override
        public void setTrustStoreType(String trustStoreType) {
            throw new IllegalStateException("The configuration is immutable");
        }
    }

    /**
     * This class represents the information associated with ciphers. It also maintains a Map from configName to
     * CipherInfo.
     */
    private static final class CipherInfo {

        private static final short SSL2 = 0x1;
        private static final short SSL3 = 0x2;
        private static final short TLS = 0x4;
        // The old names mapped to the standard names as existed
        private static final String[][] OLD_CIPHER_MAPPING = {
            // IWS 6.x or earlier
            {"rsa_null_md5", "SSL_RSA_WITH_NULL_MD5"},
            {"rsa_null_sha", "SSL_RSA_WITH_NULL_SHA"},
            {"rsa_rc4_40_md5", "SSL_RSA_EXPORT_WITH_RC4_40_MD5"},
            {"rsa_rc4_128_md5", "SSL_RSA_WITH_RC4_128_MD5"},
            {"rsa_rc4_128_sha", "SSL_RSA_WITH_RC4_128_SHA"},
            {"rsa_3des_sha", "SSL_RSA_WITH_3DES_EDE_CBC_SHA"},
            {"fips_des_sha", "SSL_RSA_WITH_DES_CBC_SHA"},
            {"rsa_des_sha", "SSL_RSA_WITH_DES_CBC_SHA"},
            // backward compatible with AS 9.0 or earlier
            {"SSL_RSA_WITH_NULL_MD5", "SSL_RSA_WITH_NULL_MD5"},
            {"SSL_RSA_WITH_NULL_SHA", "SSL_RSA_WITH_NULL_SHA"}
        };

        private static final Map<String, CipherInfo> ciphers = new HashMap<>();
        private static final ReadWriteLock ciphersLock = new ReentrantReadWriteLock();

        private final String configName;
        private final String cipherName;
        private final short protocolVersion;

        static {
            for (String[] pair : OLD_CIPHER_MAPPING) {
                String nonStdName = pair[0];
                String stdName = pair[1];
                ciphers.put(nonStdName,
                        new CipherInfo(nonStdName, stdName, (short) (SSL3 | TLS)));
            }
        }

        /**
         * @param configName name used in domain.xml, sun-acc.xml
         * @param cipherName name that may depends on backend
         * @param protocolVersion
         */
        private CipherInfo(final String configName, final String cipherName, final short protocolVersion) {
            this.configName = configName;
            this.cipherName = cipherName;
            this.protocolVersion = protocolVersion;
        }

        public static void updateCiphers(final SSLContext sslContext) {
            SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
            String[] supportedCiphers = factory.getDefaultCipherSuites();

            ciphersLock.writeLock().lock();
            try {
                for (String cipher : supportedCiphers) {
                    ciphers.put(cipher, new CipherInfo(cipher, cipher, (short) (SSL3 | TLS)));
                }
            } finally {
                ciphersLock.writeLock().unlock();
            }
        }

        public static CipherInfo getCipherInfo(final String configName) {
            ciphersLock.readLock().lock();
            try {
                return ciphers.get(configName);
            } finally {
                ciphersLock.readLock().unlock();
            }
        }

        public String getConfigName() {
            return configName;
        }

        public String getCipherName() {
            return cipherName;
        }

        public boolean isSSL2() {
            return (protocolVersion & SSL2) == SSL2;
        }

        public boolean isSSL3() {
            return (protocolVersion & SSL3) == SSL3;
        }

        public boolean isTLS() {
            return (protocolVersion & TLS) == TLS;
        }
    } // END CipherInfo
}
