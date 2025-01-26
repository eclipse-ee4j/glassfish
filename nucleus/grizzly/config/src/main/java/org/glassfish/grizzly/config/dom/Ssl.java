/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.config.dom;

import jakarta.validation.constraints.Pattern;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * Define SSL processing parameters
 */
@Configured
public interface Ssl extends ConfigBeanProxy, PropertyBag {

    boolean ALLOW_LAZY_INIT = true;

    boolean CLIENT_AUTH_ENABLED = false;

    boolean SSL2_ENABLED = false;

    boolean SSL3_ENABLED = false;

    boolean TLS_ENABLED = false;

    boolean TLS11_ENABLED = false;

    boolean TLS12_ENABLED = true;

    boolean TLS13_ENABLED = true;

    boolean TLS_ROLLBACK_ENABLED = true;

    boolean RENEGOTIATE_ON_CLIENT_AUTH_WANT = true;

    int MAX_CERT_LENGTH = 5;

    int DEFAULT_SSL_INACTIVITY_TIMEOUT = 30;

    String CLIENT_AUTH_PATTERN = "(|need|want)";

    String STORE_TYPE_PATTERN = "(JKS|NSS)";

    String PASSWORD_PROVIDER = "plain";

    String SSL2_CIPHERS_PATTERN =
            "(([+\\-])(rc2|rc2export|rc4|rc4export|idea|des|desede3)(\\s*,\\s*([+\\-])(rc2|rc2export|rc4|rc4export|idea|des|desede3))*)*";

    long HANDSHAKE_TIMEOUT_MILLIS = -1;

    /**
     * Nickname of the server certificate in the certificate database or the PKCS#11 token.
     * In the certificate, the name format is token name:nickname. Including the token name:
     * part of the name in this attribute is optional.
     */
    @Attribute
    String getCertNickname();

    void setCertNickname(String certNickname);

    /**
     * Determines whether SSL3 client authentication is performed on every request,
     * independent of ACL-based access control.
     */
    @Attribute(defaultValue = "" + CLIENT_AUTH_ENABLED, dataType = Boolean.class)
    String getClientAuthEnabled();

    void setClientAuthEnabled(String clientAuthEnabled);

    /**
     * Determines if the engine will request (want) or require (need) client authentication.
     * Valid values: want, need, or left blank
     */
    @Attribute(defaultValue = "")
    @Pattern(regexp = CLIENT_AUTH_PATTERN, message = "Valid values: " + CLIENT_AUTH_PATTERN)
    String getClientAuth();

    void setClientAuth(String clientAuth);

    @Attribute
    String getCrlFile();

    void setCrlFile(String crlFile);

    @Attribute
    String getKeyAlgorithm();

    void setKeyAlgorithm(String keyAlgorithm);

    /**
     * The {@code type} of the keystore file
     */
    @Attribute
    @Pattern(regexp = STORE_TYPE_PATTERN, message = "Valid values: " + STORE_TYPE_PATTERN)
    String getKeyStoreType();

    void setKeyStoreType(String keyStoreType);

    @Attribute(defaultValue = PASSWORD_PROVIDER)
    String getKeyStorePasswordProvider();

    void setKeyStorePasswordProvider(String keyStorePasswordProvider);

    /**
     * Password of the keystore file
     */
    @Attribute
    String getKeyStorePassword();

    void setKeyStorePassword(String keyStorePassword);

    /**
     * Location of the keystore file
     */
    @Attribute
    String getKeyStore();

    void setKeyStore(String keyStore);

    @Attribute
    String getClassname();

    void setClassname(String classname);

    /**
     * A comma-separated list of the SSL2 ciphers used, with the prefix + to enable or - to disable,
     * for example +rc4. Allowed values are rc4, rc4export, rc2, rc2export, idea, des, desede3.
     * If no value is specified, all supported ciphers are assumed to be enabled. NOT Used in PE
     */
    @Attribute
    @Pattern(regexp = SSL2_CIPHERS_PATTERN, message = "Valid values: " + SSL2_CIPHERS_PATTERN)
    String getSsl2Ciphers();

    void setSsl2Ciphers(String ciphers);

    /**
     * Determines whether SSL2 is enabled. NOT Used in PE. SSL2 is not supported by either
     * iiop or web-services. When this element is used as a child of the iiop-listener
     * element then the only allowed value for this attribute is "false".
     */
    @Attribute(defaultValue = "" + SSL2_ENABLED, dataType = Boolean.class)
    String getSsl2Enabled();

    void setSsl2Enabled(String enabled);

    /**
     * Determines whether SSL3 is enabled. If both SSL2 and SSL3 are enabled for a
     * virtual server, the server tries SSL3 encryption first. If that fails,
     * the server tries SSL2 encryption.
     */
    @Attribute(defaultValue = "" + SSL3_ENABLED, dataType = Boolean.class)
    String getSsl3Enabled();

    void setSsl3Enabled(String enabled);

    /**
     * A comma-separated list of the SSL3 ciphers used, with the prefix + to enable or - to disable,
     * for example +SSL_RSA_WITH_RC4_128_MD5. Allowed SSL3/TLS values are those that are supported
     * by the JVM for the given security provider and security service configuration. If no value
     * is specified, all supported ciphers are assumed to be enabled.
     */
    @Attribute
    String getSsl3TlsCiphers();

    void setSsl3TlsCiphers(String ciphers);

    /**
     * Determines whether TLS is enabled.
     */
    @Attribute(defaultValue = "" + TLS_ENABLED, dataType = Boolean.class)
    String getTlsEnabled();

    void setTlsEnabled(String tlsEnabled);

    /**
     * Determines whether TLS 1.1 is enabled.
     */
    @Attribute(defaultValue = "" + TLS11_ENABLED, dataType = Boolean.class)
    String getTls11Enabled();

    void setTls11Enabled(String tlsEnabled);

    /**
     * Determines whether TLS 1.2 is enabled.
     */
    @Attribute(defaultValue = "" + TLS12_ENABLED, dataType = Boolean.class)
    String getTls12Enabled();

    void setTls12Enabled(String tlsEnabled);

    /**
     * Determines whether TLS 1.3 is enabled.
     */
    @Attribute(defaultValue = "" + TLS13_ENABLED, dataType = Boolean.class)
    String getTls13Enabled();

    void setTls13Enabled(String tlEnabled);

    /**
     * Determines whether TLS rollback is enabled. TLS rollback should be enabled for
     * Microsoft Internet Explorer 5.0 and 5.5. NOT Used in PE
     */
    @Attribute(defaultValue = "" + TLS_ROLLBACK_ENABLED, dataType = Boolean.class)
    String getTlsRollbackEnabled();

    void setTlsRollbackEnabled(String tlsRollbackEnabled);

    @Attribute
    String getTrustAlgorithm();

    void setTrustAlgorithm(String trustAlgorithm);

    @Attribute(dataType = Integer.class, defaultValue = "" + MAX_CERT_LENGTH)
    String getTrustMaxCertLength();

    void setTrustMaxCertLength(String maxCertLength);

    @Attribute
    String getTrustStore();

    void setTrustStore(String trustStore);

    /**
     * Type of the truststore file
     */
    @Attribute
    @Pattern(regexp = STORE_TYPE_PATTERN, message = "Valid values: " + STORE_TYPE_PATTERN)
    String getTrustStoreType();

    void setTrustStoreType(String trustStoreType);

    @Attribute(defaultValue = PASSWORD_PROVIDER)
    String getTrustStorePasswordProvider();

    void setTrustStorePasswordProvider(String passwordProvider);

    /**
     * Password of the truststore file
     */
    @Attribute
    String getTrustStorePassword();

    void setTrustStorePassword(String trustStorePassword);

    /**
     * Does SSL configuration allow implementation to initialize it lazily way
     */
    @Attribute(defaultValue = "" + ALLOW_LAZY_INIT, dataType = Boolean.class)
    String getAllowLazyInit();

    void setAllowLazyInit(String allowLazyInit);

    /**
     * @return the timeout within which there must be activity from the client.
     *  Defaults to {@value #DEFAULT_SSL_INACTIVITY_TIMEOUT} seconds.
     */
    @Attribute(defaultValue = "" + DEFAULT_SSL_INACTIVITY_TIMEOUT, dataType = Integer.class)
    String getSSLInactivityTimeout();

    void setSSLInactivityTimeout(int sslInactivityTimeout);

    /**
     * Determines whether or not ssl session renegotiation will occur if
     * client-auth is set to want.  This may be set to {@code false} under
     * the assumption that if a certificate wasn't available during the initial
     * handshake, it won't be available during a renegotiation.
     *
     * <p>This configuration option defaults to {@code true}.
     *
     * @return {@code true} if ssl session renegotiation will occur if
     *  client-auth is want.
     *
     * @since 2.1.2
     */
    @Attribute(defaultValue = "" + RENEGOTIATE_ON_CLIENT_AUTH_WANT, dataType = Boolean.class)
    String getRenegotiateOnClientAuthWant();

    /**
     * @since 2.1.2
     */
    void setRenegotiateOnClientAuthWant(boolean renegotiate);

    /**
     * Handshake mode
     */
    @Attribute(defaultValue ="" + HANDSHAKE_TIMEOUT_MILLIS, dataType = Long.class)
    String getHandshakeTimeoutMillis();

    void setHandshakeTimeoutMillis(String handshakeTimeout);
}
