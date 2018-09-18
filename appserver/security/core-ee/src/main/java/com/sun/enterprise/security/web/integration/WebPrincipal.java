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

import com.sun.enterprise.security.SecurityContextProxy;
import org.glassfish.security.common.PrincipalImpl;
import com.sun.enterprise.security.SecurityContext;

public class WebPrincipal extends PrincipalImpl implements SecurityContextProxy {


    private char[] password;

    private X509Certificate[] certs;

    private boolean useCertificate;

    private SecurityContext secCtx;

    private Principal customPrincipal;

    public WebPrincipal(Principal p, SecurityContext context) {
	super(p.getName());
	if (!(p instanceof PrincipalImpl)) {
	    customPrincipal = p;
	}
        this.useCertificate = false;
        this.secCtx = context;
    }

    public WebPrincipal(String user, char[] pwd,
                        SecurityContext context) {
        super(user);
        //Copy the password to another reference before storing it to the
        //instance field.
        this.password = (pwd == null) ? null : Arrays.copyOf(pwd, pwd.length);	

        this.useCertificate = false;
        this.secCtx = context;
    }

    @Deprecated
    public WebPrincipal(String user, String password,
                        SecurityContext context) {
        this(user, password.toCharArray(),context);

    }

    public WebPrincipal(X509Certificate[] certs,
                        SecurityContext context) {
        super(certs[0].getSubjectDN().getName());
        this.certs = certs;
        this.useCertificate = true;
        this.secCtx = context;
    }

    public char[] getPassword() {
        //Copy the password to another reference and return the reference
        char[] passwordCopy = (password == null) ? null : Arrays.copyOf(password, password.length);

        return passwordCopy;
    }

    public X509Certificate[] getCertificates() {
        return certs;
    }

    public boolean isUsingCertificate() {
        return useCertificate;
    }

    public SecurityContext getSecurityContext() {
        return secCtx;
    }

    public String getName() {
	if (customPrincipal == null) {
	    return super.getName();
	} else {
	    return customPrincipal.getName();
	}
    }

    public boolean equals(Object another) {

	if (customPrincipal == null) {
	    return super.equals(another);
	} 
	return customPrincipal.equals(another);
    }

    public int hashCode() {
	if (customPrincipal == null) {
	    return super.hashCode();
	} 
	return customPrincipal.hashCode();
    }

    public String toString() {
	if (customPrincipal == null) {
	    return super.toString();
	} 
	return customPrincipal.toString();
    }

}

