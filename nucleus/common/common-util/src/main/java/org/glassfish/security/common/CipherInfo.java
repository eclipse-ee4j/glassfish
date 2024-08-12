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

package org.glassfish.security.common;

import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLServerSocketFactory;

/**
 * This class represents the information associated to ciphers.
 * It also maintains a HashMap from configName to CipherInfo.
 * @author Shing Wai Chan
 */
public class CipherInfo {
    private static final short SSL2 = 0x1;
    private static final short SSL3 = 0x2;
    private static final short TLS = 0x4;

    // The old names mapped to the standard names as existed
    private static final String[][] OLD_CIPHER_MAPPING = {
            // IWS 6.x or earlier
            {"rsa_null_md5"   , "SSL_RSA_WITH_NULL_MD5"},
            {"rsa_null_sha"   , "SSL_RSA_WITH_NULL_SHA"},
            {"rsa_rc4_40_md5" , "SSL_RSA_EXPORT_WITH_RC4_40_MD5"},
            {"rsa_rc4_128_md5", "SSL_RSA_WITH_RC4_128_MD5"},
            {"rsa_rc4_128_sha", "SSL_RSA_WITH_RC4_128_SHA"},
            {"rsa_3des_sha"   , "SSL_RSA_WITH_3DES_EDE_CBC_SHA"},
            {"fips_des_sha"   , "SSL_RSA_WITH_DES_CBC_SHA"},
            {"rsa_des_sha"    , "SSL_RSA_WITH_DES_CBC_SHA"},

            // backward compatible with AS 9.0 or earlier
            {"SSL_RSA_WITH_NULL_MD5", "SSL_RSA_WITH_NULL_MD5"},
            {"SSL_RSA_WITH_NULL_SHA", "SSL_RSA_WITH_NULL_SHA"}
        };

    private static Map ciphers = new HashMap();

    private String configName;
    private String cipherName;
    private short protocolVersion;


    static {
        int len = OLD_CIPHER_MAPPING.length;
        for(int i=0; i<len; i++) {
            String nonStdName = OLD_CIPHER_MAPPING[i][0];
            String stdName    = OLD_CIPHER_MAPPING[i][1];
            ciphers.put(nonStdName,
                new CipherInfo(nonStdName, stdName, (short)(SSL3|TLS)) );
        }

        SSLServerSocketFactory factory =
                (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
        String[] supportedCiphers = factory.getDefaultCipherSuites();
        len = supportedCiphers.length;
        for(int i=0; i<len; i++) {
            String s = supportedCiphers[i];
            ciphers.put(s, new CipherInfo(s, s, (short)(SSL3|TLS)) );
        }
    }

    /**
     * @param configName  name used in domain.xml, sun-acc.xml
     * @param cipherName  name that may depends on backend
     * @param protocolVersion
     */
    private CipherInfo(String configName, String cipherName,
            short protocolVersion) {
        this.configName = configName;
        this.cipherName = cipherName;
        this.protocolVersion = protocolVersion;
    }

    public static CipherInfo getCipherInfo(String configName) {
        return (CipherInfo)ciphers.get(configName);
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
}
