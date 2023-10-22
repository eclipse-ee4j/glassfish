/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.ee.web.integration;

import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.security.SecurityContextProxy;

import java.security.Principal;
import java.security.cert.X509Certificate;

import org.glassfish.security.common.UserNameAndPassword;

public class WebPrincipal extends UserNameAndPassword implements SecurityContextProxy {

    private static final long serialVersionUID = -7855179427171479644L;

    private X509Certificate[] certificates;
    private final boolean useCertificate;
    private final SecurityContext securityContext;
    private Principal customPrincipal;

    public WebPrincipal(UserNameAndPassword principal, SecurityContext context) {
        super(principal.getName(), principal.getPassword());
        this.useCertificate = false;
        this.securityContext = context;
    }


    public WebPrincipal(Principal principal, SecurityContext context) {
        super(principal.getName());
        this.customPrincipal = principal;
        this.useCertificate = false;
        this.securityContext = context;
    }


    public WebPrincipal(String user, String password, SecurityContext context) {
        this(user, password == null ? null : password.toCharArray(), context);
    }


    public WebPrincipal(String user, char[] pwd, SecurityContext context) {
        super(user, pwd);
        this.useCertificate = false;
        this.securityContext = context;
    }


    public WebPrincipal(X509Certificate[] certs, SecurityContext context) {
        super(getPrincipalName(context, certs));
        this.certificates = certs;
        this.useCertificate = true;
        this.securityContext = context;
    }


    @Override
    public SecurityContext getSecurityContext() {
        return securityContext;
    }


    public X509Certificate[] getCertificates() {
        return certificates;
    }


    public boolean isUsingCertificate() {
        return useCertificate;
    }


    public Principal getCustomPrincipal() {
        return customPrincipal;
    }


    @Override
    public boolean equals(Object another) {
        if (customPrincipal == null) {
            return super.equals(another);
        }
        return customPrincipal.equals(another);
    }


    @Override
    public int hashCode() {
        if (customPrincipal == null) {
            return super.hashCode();
        }
        return customPrincipal.hashCode();
    }


    @Override
    public String toString() {
        if (customPrincipal == null) {
            return super.toString();
        }
        return customPrincipal.toString();
    }


    private static String getPrincipalName(SecurityContext securityContext, X509Certificate[] certificates) {
        Principal callerPrincipal = securityContext.getCallerPrincipal();
        if (callerPrincipal != null) {
            return callerPrincipal.getName();
        }
        if (certificates != null && certificates.length > 0) {
            return certificates[0].getSubjectX500Principal().getName();
        }
        return null;
    }
}
