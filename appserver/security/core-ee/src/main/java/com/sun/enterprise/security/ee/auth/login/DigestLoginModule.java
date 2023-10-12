/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.ee.auth.login;

import com.sun.enterprise.security.PrincipalGroupFactory;
import com.sun.enterprise.security.auth.digest.api.DigestAlgorithmParameter;
import com.sun.enterprise.security.auth.realm.Realm;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchRealmException;
import com.sun.enterprise.security.ee.auth.realm.DigestRealm;
import com.sun.logging.LogDomains;

import java.security.Principal;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.glassfish.internal.api.Globals;
import org.glassfish.security.common.Group;
import org.glassfish.security.common.UserPrincipal;

/**
 *
 * @author K.Venugopal@sun.com
 */
public abstract class DigestLoginModule implements LoginModule {

    protected static final Logger _logger = LogDomains.getLogger(DigestLoginModule.class, LogDomains.SECURITY_LOGGER);

    private Subject subject;
    private CallbackHandler handler;
    private Map<String, ?> sharedState;
    private Map<String, ?> options;
    protected boolean _succeeded;
    protected boolean _commitSucceeded;
    protected UserPrincipal _userPrincipal;
    private DigestCredentials digestCredentials;
    private Realm _realm;

    public DigestLoginModule() {
    }

    @Override
    public final void initialize(Subject subject, CallbackHandler handler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.handler = handler;
        this.sharedState = sharedState;
        this.options = options;
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Login module initialized: " + this.getClass().toString());
        }
    }

    @Override
    public final boolean login() throws LoginException {
        Set<Object> creds = this.subject.getPrivateCredentials();
        Iterator<Object> itr = creds.iterator();
        while (itr.hasNext()) {
            Object obj = itr.next();
            if (obj instanceof DigestCredentials) {
                digestCredentials = (DigestCredentials) obj;
                break;
            }
            if (obj instanceof com.sun.enterprise.security.auth.login.DigestCredentials) {
                com.sun.enterprise.security.auth.login.DigestCredentials dc = (com.sun.enterprise.security.auth.login.DigestCredentials) obj;
                digestCredentials = new DigestCredentials(dc.getRealmName(), dc.getUserName(), dc.getParameters());
            }
        }
        if (digestCredentials == null) {
            throw new LoginException();
        }
        DigestAlgorithmParameter[] params = digestCredentials.getParameters();
        String username = digestCredentials.getUserName();
        try {
            _realm = Realm.getInstance(digestCredentials.getRealmName());
        } catch (NoSuchRealmException ex) {
            _logger.log(Level.FINE, "", ex);
            _logger.log(Level.SEVERE, "no.realm", digestCredentials.getRealmName());
            throw new LoginException(ex.getMessage());
        }
        if (!(_realm instanceof DigestRealm)) {
            _logger.log(Level.SEVERE, "digest.realm", digestCredentials.getRealmName());
            throw new LoginException("Realm" + digestCredentials.getRealmName() + " does not support Digest validation");
        }
        if (((DigestRealm) _realm).validate(username, params)) {
            // change to pass Password Validator
            _succeeded = true;
        }

        return _succeeded;
    }

    @Override
    public final boolean commit() throws LoginException {

        if (!_succeeded) {
            _commitSucceeded = false;
            return false;
        }

        PrincipalGroupFactory factory = Globals.getDefaultHabitat().getService(PrincipalGroupFactory.class);
        _userPrincipal = factory.getPrincipalInstance(digestCredentials.getUserName(), digestCredentials.getRealmName());
        Set<Principal> principalSet = this.subject.getPrincipals();
        if (!principalSet.contains(_userPrincipal)) {
            principalSet.add(_userPrincipal);
        }
        Enumeration<String> groupsList = getGroups(digestCredentials.getUserName());
        while (groupsList.hasMoreElements()) {
            java.lang.String value = groupsList.nextElement();
            Group g = factory.getGroupInstance(value, digestCredentials.getRealmName());
            if (!principalSet.contains(g)) {
                principalSet.add(g);
            }
            // cleaning the slate
        }

        return true;

    }

    @Override
    public final boolean abort() throws LoginException {
        _logger.log(Level.FINE, "JAAS authentication aborted.");

        if (_succeeded == false) {
            return false;
        }
        if (_succeeded == true && _commitSucceeded == false) {
            // login succeeded but overall authentication failed
            _succeeded = false;

        } else {
            // overall authentication succeeded and commit succeeded,
            // but someone else's commit failed
            logout();
        }
        return true;
    }

    @Override
    public final boolean logout() throws LoginException {
        subject.getPrincipals().clear();
        subject.getPublicCredentials().clear();
        subject.getPrivateCredentials().clear();

        _succeeded = false;
        _commitSucceeded = false;

        return true;
    }

    protected Realm getRealm() {
        return _realm;
    }

    protected abstract Enumeration<String> getGroups(String username);

}
