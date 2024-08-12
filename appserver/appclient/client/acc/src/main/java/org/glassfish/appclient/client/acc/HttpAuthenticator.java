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

package org.glassfish.appclient.client.acc;

import com.sun.enterprise.security.appclient.integration.AppClientSecurityInfo;
import com.sun.enterprise.security.auth.login.common.PasswordCredential;
import com.sun.enterprise.security.common.ClientSecurityContext;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.security.Principal;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
/**
 * This is the callback object that gets called when a protected resource
 * needs to be accessed and authentication information is needed. Pops up
 * a UI to input username and password.
 */
public class HttpAuthenticator extends Authenticator
{
    public static final boolean debug = false;

    private static Logger _logger = Logger.getLogger(HttpAuthenticator.class.getName());

    private final AppClientSecurityInfo.CredentialType loginType;
    private final AppClientSecurityInfo securityInfo;

    /**
     * Create the authenticator.
     */
    public HttpAuthenticator(final AppClientSecurityInfo secInfo,
            final AppClientSecurityInfo.CredentialType loginType) {
        this.securityInfo = secInfo;
        this.loginType = loginType;
    }

    /**
     * This is called when authentication is needed for a protected
     * web resource. It looks for the authentication data in the subject.
     * If the data is not found then login is invoked on the login context.
     */
    @Override
    protected PasswordAuthentication getPasswordAuthentication()
    {
    String user = null;
    char[] password = null;
    Subject subject = null;

    String scheme = getRequestingScheme();
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("scheme=" + scheme);
            _logger.fine("requesting prompt=" + getRequestingPrompt());
            _logger.fine("requesting protocol=" + getRequestingProtocol());
        }

    ClientSecurityContext cont = ClientSecurityContext.getCurrent();
    subject = (cont != null) ? cont.getSubject() : null;
    user = getUserName(subject);
    password = getPassword(subject);
    if(user == null || password == null) {
        try {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.fine("Initiating login again...");
                }

        securityInfo.doClientLogin(
                loginType);
        cont = ClientSecurityContext.getCurrent();
        subject = cont.getSubject();
        user = getUserName(subject);
        password = getPassword(subject);
        } catch(Exception e) {
                _logger.log(Level.FINE, "Exception " + e.toString(), e);
            return null;
        }
    }
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("Username:" + user);
        }
    return new PasswordAuthentication(user, password);
    }

    /**
     * Return the username from the subject.
     */
    private String getUserName(Subject s) {
    String user = null;
    if(s == null)
        return null;
    Set principalSet = s.getPrincipals();
    Iterator itr = principalSet.iterator();
    if(itr.hasNext()) {
        Principal p = (Principal) itr.next();
        user = p.getName();
    }
    return user;
    }

    /**
     * Return the password for the subject.
     */
    private char[] getPassword(Subject s) {
    char[] password = null;
    if(s == null)
        return null;
    Set credentials = s.getPrivateCredentials();
    Iterator credIter = credentials.iterator();
    if(credIter.hasNext()) {
        Object o = credIter.next();
        if(o instanceof PasswordCredential) {
        PasswordCredential pc = (PasswordCredential) o;
        // CHECK REALM.
            password = pc.getPassword();
        }
    }
    return password;
    }
}

