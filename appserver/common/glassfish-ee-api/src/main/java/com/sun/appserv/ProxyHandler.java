/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package com.sun.appserv;

import jakarta.servlet.http.HttpServletRequest;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Abstract class allowing a backend appserver instance to retrieve information about the original client request that
 * was intercepted by an SSL terminating proxy server (e.g., load balancer).
 *
 * <p>
 * An implementation of this abstract class inspects a given request for the custom request headers through which the
 * proxy server communicates the information about the original client request to the appserver instance, and makes this
 * information available to the appserver.
 *
 * <p>
 * This allows the appserver to work with any number of 3rd party SSL offloader implementations configured on the
 * front-end web server, for which a corresponding ProxyHandler implementation has been configured on the backend
 * appserver.
 */
public abstract class ProxyHandler {

    /**
     * Gets the SSL client certificate chain with which the client had authenticated itself to the SSL offloader, and which
     * the SSL offloader has added as a custom request header on the given request.
     *
     * @param request The request from which to retrieve the SSL client certificate chain
     *
     * @return Array of java.security.cert.X509Certificate instances representing the SSL client certificate chain, or null
     * if this information is not available from the given request
     *
     * @throws CertificateException if the certificate chain retrieved from the request header cannot be parsed
     */
    public X509Certificate[] getSSLClientCertificateChain(HttpServletRequest request) throws CertificateException {
        return null;
    }

    /**
     * Returns the SSL keysize with which the original client request that was intercepted by the SSL offloader has been
     * protected, and which the SSL offloader has added as a custom request header on the given request.
     *
     * @param request The request from which to retrieve the SSL key size
     *
     * @return SSL keysize, or -1 if this information is not available from the given request
     */
    public int getSSLKeysize(HttpServletRequest request) {
        return -1;
    }

    /**
     * Gets the Internet Protocol (IP) address of the original client request that was intercepted by the proxy server.
     *
     * @param request The request from which to retrieve the IP address of the original client request
     *
     * @return IP address of the original client request, or null if this information is not available from the given
     * request
     */
    public String getRemoteAddress(HttpServletRequest request) {
        return null;
    }

}
