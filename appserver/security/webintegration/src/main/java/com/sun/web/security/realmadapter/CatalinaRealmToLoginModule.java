/*
 * Copyright (c) 2021, 2026 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.web.security.realmadapter;

import com.sun.enterprise.security.AppCNonceCacheMap;
import com.sun.enterprise.security.CNonceCacheFactory;
import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.security.auth.login.DigestCredentials;
import com.sun.enterprise.security.auth.login.LoginContextDriver;
import com.sun.enterprise.security.ee.web.integration.WebPrincipal;
import com.sun.enterprise.security.ee.web.integration.WebSecurityManager;
import com.sun.web.security.realmadapter.digest.Digester;

import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.function.Supplier;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.apache.catalina.HttpRequest;

import static java.util.Arrays.asList;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

public class CatalinaRealmToLoginModule {

    private static final Logger LOG = Logger.getLogger(Digester.class.getName());

    private final String realmName;
    private final String moduleID;
    private final Digester digester;
    private final Supplier<WebSecurityManager> webSecurityManagerSupplier;

    public CatalinaRealmToLoginModule(
        String realmName, String appName, String moduleID,
        Provider<AppCNonceCacheMap> appCNonceCacheMapProvider,
        Provider<CNonceCacheFactory> cNonceCacheFactoryProvider,
        Supplier<WebSecurityManager> webSecurityManagerSupplier) {

        this.realmName = realmName;
        this.moduleID = moduleID;
        this.digester = new Digester(realmName, appName, appCNonceCacheMapProvider, cNonceCacheFactoryProvider);
        this.webSecurityManagerSupplier = webSecurityManagerSupplier;
    }

    /**
     * Authenticate for Basic and FORM
     * @param request
     * @param username
     * @param password
     * @return
     */
    public Principal authenticate(HttpRequest request, String username, char[] password) {
        LOG.log(FINE, "Tomcat callback for authenticate user/password. Username: {0}", username);

        if (!authenticate((HttpServletRequest) request, username, password, null, null)) {
            return null;
        }

        return new WebPrincipal(username, password, SecurityContext.getCurrent());
    }

    /**
     * Authenticate for Digest
     *
     * @param httpServletRequest
     * @return
     */
    public Principal authenticate(HttpServletRequest httpServletRequest) {
        DigestCredentials digestCredentials = digester.generateDigestCredentials(httpServletRequest);
        if (digestCredentials == null) {
            return null;
        }

        if (!authenticate(httpServletRequest, null, null, digestCredentials, null)) {
            return null;
        }

        return new WebPrincipal(digestCredentials.getUserName(), (char[]) null, SecurityContext.getCurrent());
    }

    /**
     * Authenticate for SSL / X500
     *
     * @param request
     * @param certificates
     * @return
     */
    public Principal authenticate(HttpRequest request, X509Certificate certificates[]) {
        if (!authenticate((HttpServletRequest) request, null, null, null, certificates)) {
            return null;
        }

        return new WebPrincipal(certificates, SecurityContext.getCurrent());
    }

    public boolean authenticate(HttpServletRequest request, WebPrincipal principal) {
        if (principal.isUsingCertificate()) {
            return authenticate(request, null, null, null, principal.getCertificates());
        }

        return authenticate(request, principal.getName(), principal.getPassword(), null, null);
    }

    /**
     * Authenticates and sets the SecurityContext in the TLS.
     *
     * @return true if authentication succeeded, false otherwise.
     * @param the username.
     * @param the authentication method.
     * @param the authentication data.
     */
    private boolean authenticate(HttpServletRequest request, String username, char[] password, DigestCredentials digestCredentials, X509Certificate[] certificates) {
        try {
            if (certificates != null) {
                LoginContextDriver.doX500Login(generateX500Subject(certificates), moduleID);
            } else if (digestCredentials != null) {
                LoginContextDriver.login(digestCredentials);
            } else {
                LoginContextDriver.login(username, password, realmName);
            }
            LOG.log(FINE, "Web login succeeded for: {0}", SecurityContext.getCurrent().getCallerPrincipal());

            WebSecurityManager manager = webSecurityManagerSupplier.get();

            // Sets the security context for Jakarta Authorization
            if (manager != null) {
                manager.onLogin(request);
            }

            return true;
        } catch (Exception le) {
            LOG.log(WARNING, "WEB9102: Web Login Failed", le);

            return false;
        }
    }

    private Subject generateX500Subject(X509Certificate[] x509Certificates) {
        Subject x500Subject = new Subject();
        x500Subject.getPublicCredentials().add(x509Certificates[0].getSubjectX500Principal());

        // Put the certificate chain as an List in the subject, to be accessed by user's LoginModule.
        x500Subject.getPublicCredentials().add(asList(x509Certificates));

        return x500Subject;
    }

}
