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

package com.sun.enterprise.web;

import com.sun.appserv.ProxyHandler;

import jakarta.servlet.http.HttpServletRequest;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.apache.catalina.connector.Constants;

/**
 * Default ProxyHandler implementation.
 */
public class ProxyHandlerImpl extends ProxyHandler {

    /**
     * Gets the SSL client certificate chain with which the client
     * had authenticated itself to the SSL offloader, and which the
     * SSL offloader has added as a custom request header on the
     * given request.
     *
     * @param request The request from which to retrieve the SSL client
     * certificate chain
     *
     * @return Array of java.security.cert.X509Certificate instances
     * representing the SSL client certificate chain, or null if this
     * information is not available from the given request
     *
     * @throws CertificateException if the certificate chain retrieved
     * from the request header cannot be parsed
     */
    public X509Certificate[] getSSLClientCertificateChain(
                        HttpServletRequest request)
            throws CertificateException {

        X509Certificate[] certs = null;

        String clientCert = request.getHeader(Constants.PROXY_AUTH_CERT);
        if (clientCert != null) {
            clientCert = clientCert.replaceAll("% d% a", "\n");
            clientCert = "-----BEGIN CERTIFICATE-----\n" + clientCert
                         + "\n-----END CERTIFICATE-----";
            byte[] certBytes = clientCert.getBytes(Charset.defaultCharset());
            ByteArrayInputStream bais = new ByteArrayInputStream(certBytes);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            certs = new X509Certificate[1];
            certs[0] = (X509Certificate) cf.generateCertificate(bais);
        }

        return certs;
    }

    /**
     * Returns the SSL keysize with which the original client request that
     * was intercepted by the SSL offloader has been protected, and which
     * the SSL offloader has added as a custom request header on the
     * given request.
     *
     * @param request The request from which to retrieve the SSL key
     * size
     *
     * @return SSL keysize, or -1 if this information is not available from
     * the given request
     */
    public int getSSLKeysize(HttpServletRequest request) {

        int keySize = -1;

        String header = request.getHeader(Constants.PROXY_KEYSIZE);
        if (header != null) {
            keySize = Integer.parseInt(header);
        }

        return keySize;
    }

    /**
     * Gets the Internet Protocol (IP) source port of the client request that
     * was intercepted by the proxy server.
     *
     * @param request The request from which to retrieve the IP source port
     * of the original client request
     *
     * @return IP source port of the original client request, or null if this
     * information is not available from the given request
     */
    public String getRemoteAddress(HttpServletRequest request) {
        return request.getHeader(Constants.PROXY_IP);
    }
}
