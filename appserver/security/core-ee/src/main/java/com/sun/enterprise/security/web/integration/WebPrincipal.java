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

package com.sun.enterprise.security.web.integration;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.glassfish.security.common.PrincipalImpl;

import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.security.SecurityContextProxy;

public class WebPrincipal extends PrincipalImpl implements SecurityContextProxy {

    private static final long serialVersionUID = -7855179427171479644L;

    private char[] password;
    private X509Certificate[] certificates;
    private boolean useCertificate;
    private SecurityContext securityContext;
    private Principal customPrincipal;

    public WebPrincipal(Principal principal, SecurityContext context) {
        super(principal.getName());
        if (!(principal instanceof PrincipalImpl)) {
            customPrincipal = principal;
        }
        this.useCertificate = false;
        this.securityContext = context;
    }

    public WebPrincipal(String user, char[] pwd, SecurityContext context) {
        super(user);

        // Copy the password to another reference before storing it to the
        // instance field.
        this.password = pwd == null ? null : Arrays.copyOf(pwd, pwd.length);

        this.useCertificate = false;
        this.securityContext = context;
    }

    public WebPrincipal(String user, String password, SecurityContext context) {
        this(user, password.toCharArray(), context);
    }

    public WebPrincipal(X509Certificate[] certs, SecurityContext context) {
        super(certs[0].getSubjectDN().getName());
        this.certificates = certs;
        this.useCertificate = true;
        this.securityContext = context;
    }

    public char[] getPassword() {
        // Copy the password to another reference and return the reference
        return password == null ? null : Arrays.copyOf(password, password.length);
    }

    public X509Certificate[] getCertificates() {
        return certificates;
    }

    public boolean isUsingCertificate() {
        return useCertificate;
    }

    @Override
    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    @Override
    public String getName() {
        if (customPrincipal == null) {
            return super.getName();
        }

        return customPrincipal.getName();
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
}
