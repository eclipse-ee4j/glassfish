/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.admin.mbeanserver.ssl;

import java.io.File;

import static org.glassfish.embeddable.GlassFishVariable.KEYSTORE_FILE;
import static org.glassfish.embeddable.GlassFishVariable.KEYSTORE_PASSWORD;
import static org.glassfish.embeddable.GlassFishVariable.KEYSTORE_TYPE;

/**
 * This class is a config holder for configuring SSL Sockets.
 * It comes with set of defaults as defined below
 * TrustAlgorithm = SunX509
 * keystore type = JKS
 * truststore type = JKS
 * protocol = TLS
 * ssl3 Enabled = true
 * tls Enabled= true
 * It also picks up the value of keystore, keystore password, truststore , trustore password from
 * system properties.
 *
 * Usage : This class can be used in any enviroment , where one wants to pass
 * in SSL defaults programatically as well as use a default set of configuration
 * without setting in values explicitly.
 * @author prasads@dev.java.net
 */
public class SSLParams {
    private File trustStore;
    private String trustStorePwd;
    private String trustStoreType = "JKS";
    private String trustAlgorithm = "SunX509";

    private String keyAlgorithm;
    private String keyStoreType = "JKS";
    private String keyStorePassword;
    private File keyStore;

    private String protocol = "TLS";

    private String[] enabledCiphers = new String[5];
    private String[] enabledProtocols = new String[5];

    private String trustMaxCertLength;
    private String certNickname;
    private String clientAuthEnabled;
    private String clientAuth;
    private String crlFile;
    private String ssl2Ciphers;
    private Boolean ssl2Enabled = false;
    private Boolean ssl3Enabled = true;
    private String ssl3TlsCiphers;
    private Boolean tlsEnabled=true;
    private Boolean tls11Enabled=true;
    private Boolean tls12Enabled=true;
    private Boolean tls13Enabled=true;
    private Boolean tlsRollBackEnabled=false;




    public SSLParams( File truststore,  String trustStorePwd,  String trustStoreType ) {
        this.trustStore = truststore;
        this.trustStorePwd = trustStorePwd;
        this.trustStoreType = trustStoreType;
    }

    public SSLParams() {

    }

    public File getTrustStore() {
        if(trustStore != null ) {
            return trustStore;
        } else if(System.getProperty("javax.net.ssl.trustStore") != null) {
            return new File(System.getProperty("javax.net.ssl.trustStore"));
        } else {
            return null;
        }
    }

    public String getTrustStorePassword() {
        if(trustStorePwd != null ) {
            return trustStorePwd;
        } else if(System.getProperty("javax.net.ssl.trustStorePassword") != null) {
            return System.getProperty("javax.net.ssl.trustStorePassword");
        } else {
            return null;
        }
    }

    public String getTrustStoreType() {
        if(trustStoreType != null ) {
            return trustStoreType;
        } else if(System.getProperty("javax.net.ssl.trustStoreType") != null) {
            return System.getProperty("javax.net.ssl.trustStoreType");
        } else {
            return "JKS";
        }
    }

    String getTrustMaxCertLength() {
        if( trustMaxCertLength == null) {
            return "5";
        }
        return trustMaxCertLength;
    }


    public String getTrustAlgorithm() {
        return trustAlgorithm;
    }

    public void setTrustAlgorithm(String algorithm) {
        this.trustAlgorithm = algorithm;
    }

    public String[] getEnabledCiphers() {
        return enabledCiphers;
    }

    public void setEnabledCiphers(String[] enabledCiphers) {
        this.enabledCiphers = enabledCiphers;
    }

    public String[] getEnabledProtocols() {
        return enabledProtocols;
    }

    public void setEnabledProtocols(String[] enabledProtocols) {
        this.enabledProtocols = enabledProtocols;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }


    public void setTrustMaxCertLength(String maxLength) {
        trustMaxCertLength = maxLength;
    }

    public String getCertNickname() {
        return certNickname;
    }

    public void setCertNickname(String certNickname) {
        this.certNickname = certNickname;
    }

    /**
     * Determines whether SSL3 client authentication is performed on every request, independent of ACL-based access
     * control.
     */

    public String getClientAuthEnabled() {
        return clientAuthEnabled;
    }

    public void setClientAuthEnabled(String clientAuthEnabled) {
        this.clientAuthEnabled = clientAuthEnabled;
    }

    /**
     * Determines if if the engine will request (want) or require (need) client authentication. Valid values:  want,
     * need, or left blank
     */

    public String getClientAuth() {
        return clientAuth;
    }

    public void setClientAuth(String clientAuth) {
        this.clientAuth = clientAuth;
    }


    public String getCrlFile() {
        return crlFile;
    }

    public void setCrlFile(String crlFile) {
        this.crlFile = crlFile;
    }


    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(String algorithm) {
        this.keyAlgorithm = algorithm;
    }

    /**
     * type of the keystore file
     */

    public String getKeyStoreType() {
        if(keyStoreType == null) {
            keyStoreType = System.getProperty(KEYSTORE_TYPE.getSystemPropertyName(), "JKS");
        }
        return keyStoreType;
    }

    public void setKeyStoreType(String type) {
        this.keyStoreType = type;
    }


    public String getKeyStorePassword() {
        return keyStorePassword == null
            ? System.getProperty(KEYSTORE_PASSWORD.getSystemPropertyName())
            : keyStorePassword;
    }

    public void setKeyStorePassword(String password) {
        this.keyStorePassword = password;
    }

    public File getKeyStore() {
        if (keyStore != null) {
            return keyStore;
        }
        String path = System.getProperty(KEYSTORE_FILE.getSystemPropertyName());
        return path == null ? null : new File(path);
    }

    public void setKeyStore(String location) {
        keyStore = new File(location);
    }


    /**
     * A comma-separated list of the SSL2 ciphers used, with the prefix + to enable or - to disable, for example +rc4.
     * Allowed values are rc4, rc4export, rc2, rc2export, idea, des, desede3. If no value is specified, all supported
     * ciphers are assumed to be enabled. NOT Used in PE
     */

    public String getSsl2Ciphers() {
        return ssl2Ciphers;
    }

    public void setSsl2Ciphers(String ssl2Ciphers) {
        this.ssl2Ciphers = ssl2Ciphers;
    }

    /**
     * Determines whether SSL2 is enabled. NOT Used in PE. SSL2 is not supported by either iiop or web-services. When
     * this element is used as a child of the iiop-listener element then the only allowed value for this attribute is
     * "false".
     */
    public Boolean getSsl2Enabled() {
        return ssl2Enabled;
    }

    public void setSsl2Enabled(String ssl2Enabled) {
        this.ssl2Enabled = Boolean.parseBoolean(ssl2Enabled);
    }

    /**
     * Determines whether SSL3 is enabled. If both SSL2 and SSL3 are enabled for a virtual server, the server tries SSL3
     * encryption first. If that fails, the server tries SSL2 encryption.
     */
    public Boolean getSsl3Enabled() {
        return ssl3Enabled;
    }

    public void setSsl3Enabled(String ssl3Enabled) {
        this.ssl3Enabled = Boolean.parseBoolean(ssl3Enabled);
    }

    /**
     * A comma-separated list of the SSL3 ciphers used, with the prefix + to enable or - to disable, for example
     * +SSL_RSA_WITH_RC4_128_MD5. Allowed SSL3/TLS values are those that are supported by the JVM for the given security
     * provider and security service configuration. If no value is specified, all supported ciphers are assumed to be
     * enabled.
     */
    public String getSsl3TlsCiphers() {
        return ssl3TlsCiphers;
    }

    public void setSsl3TlsCiphers(String ssl3TlsCiphers) {
        this.ssl3TlsCiphers  = ssl3TlsCiphers;
    }

    /**
     * Determines whether TLS is enabled.
     */

    public Boolean getTlsEnabled() {
        return tlsEnabled;
    }

    public Boolean getTls11Enabled() {
        return tls11Enabled;
    }

    public Boolean getTls12Enabled() {
        return tls12Enabled;
    }

    public Boolean getTls13Enabled() {
        return tls13Enabled;
    }

    public void setTlsEnabled(String tlsEnabled) {
        this.tlsEnabled = Boolean.parseBoolean(tlsEnabled);
    }

    public void setTls11Enabled(String tls11Enabled) {
        this.tls11Enabled = Boolean.parseBoolean(tls11Enabled);
    }

    public void setTls12Enabled(String tls12Enabled) {
        this.tls12Enabled = Boolean.parseBoolean(tls12Enabled);
    }

    public void setTls13Enabled(String tls13Enabled) {
        this.tls13Enabled = Boolean.parseBoolean(tls13Enabled);
    }

    /**
     * Determines whether TLS rollback is enabled. TLS rollback should be enabled for Microsoft Internet Explorer 5.0
     * and 5.5. NOT Used in PE
     */

    public Boolean getTlsRollbackEnabled() {
        return tlsRollBackEnabled;
    }

    public void setTlsRollbackEnabled(String tlsRollBackEnabled) {
        this.tlsRollBackEnabled = Boolean.parseBoolean(tlsRollBackEnabled);
    }

}
