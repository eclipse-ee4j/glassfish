/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.security.auth.login;

import com.sun.enterprise.security.SecurityLoggerInfo;
import com.sun.enterprise.security.auth.login.common.X509CertificateCredential;
import com.sun.enterprise.security.integration.AppClientSSL;
import com.sun.enterprise.security.ssl.SSLUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ChoiceCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.glassfish.internal.api.Globals;
import org.glassfish.security.common.UserNameAndPassword;
import org.glassfish.security.common.UserPrincipal;

import static java.util.logging.Level.FINE;

/**
 * <p>
 * This LoginModule authenticates users with X509 certificates.
 *
 * <p>
 * If testUser successfully authenticates itself, a <code>UserPrincipal</code> with the user's username is added to the
 * Subject.
 *
 * <p>
 * This LoginModule recognizes the debug option. If set to true in the login Configuration, debug messages will be
 * output to the output stream, System.out.
 *
 * @author Harpreet Singh (harpreet.singh@sun.com)
 */
public class ClientCertificateLoginModule implements LoginModule {

    private static final Logger _logger = SecurityLoggerInfo.getLogger();

    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ClientCertificateLoginModule.class);

    private static KeyStore keyStore;

    // initial state
    private Subject subject;
    private CallbackHandler callbackHandler;

    // configurable option
    private boolean debug;

    // the authentication status
    private boolean succeeded;
    private boolean commitSucceeded;

    private String alias;
    private X509Certificate certificate;

    private UserPrincipal userPrincipal;

    private AppClientSSL ssl;
    private SSLUtils sslUtils;

    public static void setKeyStore(KeyStore keyStore) {
        ClientCertificateLoginModule.keyStore = keyStore;
    }

    /**
     * Initialize this <code>LoginModule</code>.
     *
     * @param subject the <code>Subject</code> to be authenticated.
     * @param callbackHandler a <code>CallbackHandler</code> for communicating with the end user (prompting for usernames
     * and passwords, for example).
     * @param sharedState shared <code>LoginModule</code> state.
     * @param options options specified in the login <code>Configuration</code> for this particular
     * <code>LoginModule</code>.
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String,?> sharedState, Map<String,?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;

        // Initialize any configured options
        debug = "true".equalsIgnoreCase((String) options.get("debug"));
        sslUtils = Globals.getDefaultHabitat().getService(SSLUtils.class);
    }

    /**
     * Authenticate the user by prompting for a username and password.
     *
     * @return true in all cases since this <code>LoginModule</code> should not be ignored.
     * @throws LoginException if this <code>LoginModule</code> is unable to perform the authentication.
     */
    @Override
    public boolean login() throws LoginException {
        // Prompt for a username and password
        if (callbackHandler == null) {
            throw new LoginException("Error: no CallbackHandler available " + "to garner authentication information from the user");
        }

        try {
            String[] as = new String[keyStore.size()];
            String[] aliasString = new String[keyStore.size()];
            Enumeration<String> aliases = keyStore.aliases();
            for (int i = 0; i < keyStore.size(); i++) {
                aliasString[i] = aliases.nextElement();
                as[i] = ((X509Certificate) keyStore.getCertificate(aliasString[i])).getSubjectX500Principal().getName();
            }

            Callback[] callbacks = new Callback[1];
            callbacks[0] = new ChoiceCallback(localStrings.getLocalString("login.certificate", "Choose from list of certificates: "), as, 0,
                    false);

            callbackHandler.handle(callbacks);

            int[] idx = ((ChoiceCallback) callbacks[0]).getSelectedIndexes();

            if (idx == null) {
                throw new LoginException("No certificate selected!");
            }

            if (idx[0] == -1) {
                throw new LoginException("Incorrect keystore password");
            }

            // Print debugging information
            if (debug) {
                if (_logger.isLoggable(FINE)) {
                    _logger.log(FINE, "\t\t[ClientCertificateLoginModule] " + "user entered certificate: ");
                    for (int element : idx) {
                        _logger.log(FINE, aliasString[element]);
                    }
                }
            }

            // The authenticate method previously picked out the wrong alias.
            // Since we allow only 1 choice the first element in idx idx[0] should have the selected index.
            this.alias = aliasString[idx[0]];
            certificate = (X509Certificate) keyStore.getCertificate(alias);

            // The authenticate should always return a true.
            if (debug) {
                _logger.log(FINE, "[ClientCertificateLoginModule] authentication succeeded");
            }

            succeeded = true;
            return true;
        } catch (java.io.IOException ioe) {
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException uce) {
            throw new LoginException("Error: " + uce.getCallback().toString() + " not available to garner authentication information " + "from the user");
        } catch (Exception e) {
            throw new LoginException(e.toString());
        }
    }

    /**
     * <p>
     * This method is called if the LoginContext's overall authentication succeeded (the relevant REQUIRED, REQUISITE,
     * SUFFICIENT and OPTIONAL LoginModules succeeded).
     *
     * <p>
     * If this LoginModule's own authentication attempt succeeded (checked by retrieving the private state saved by the
     * <code>login</code> method), then this method associates a <code>UserPrincipal</code> with the <code>Subject</code>
     * located in the <code>LoginModule</code>. If this LoginModule's own authentication attempted failed, then this method
     * removes any state that was originally saved.
     *
     * @throws LoginException if the commit fails.
     * @return true if this LoginModule's own login and commit attempts succeeded, or false otherwise.
     */
    @Override
    public boolean commit() throws LoginException {
        if (succeeded == false) {
            return false;
        }

        userPrincipal = new UserNameAndPassword(alias);
        if (!subject.getPrincipals().contains(userPrincipal)) {
            subject.getPrincipals().add(userPrincipal);
        }

        if (debug) {
            _logger.log(FINE, "\t\t[ClientCertificateLoginModule] added UserPrincipal to Subject");
        }

        ssl = new AppClientSSL();
        ssl.setCertNickname(this.alias);
        sslUtils.setAppclientSsl(ssl);

        String realm = LoginContextDriver.CERT_REALMNAME;
        X509Certificate[] certChain = new X509Certificate[1];
        certChain[0] = certificate;
        X509CertificateCredential pc = new X509CertificateCredential(certChain, alias, realm);
        if (!subject.getPrivateCredentials().contains(pc)) {
            subject.getPrivateCredentials().add(pc);
        }

        commitSucceeded = true;
        return true;
    }

    /**
     * <p>
     * This method is called if the LoginContext's overall authentication failed. (the relevant REQUIRED, REQUISITE,
     * SUFFICIENT and OPTIONAL LoginModules did not succeed).
     * <p>
     * If this LoginModule's own authentication attempt succeeded (checked by retrieving the private state saved by the
     * <code>login</code> and <code>commit</code> methods), then this method cleans up any state that was originally saved.
     *
     * @throws LoginException if the abort fails.
     * @return false if this LoginModule's own login and/or commit attempts failed, and true otherwise.
     */
    @Override
    public boolean abort() throws LoginException {
        if (succeeded == false) {
            return false;
        }

        if (succeeded == true && commitSucceeded == false) {
            // login succeeded but overall authentication failed
            succeeded = false;
            alias = null;
            userPrincipal = null;
        } else {
            // overall authentication succeeded and commit succeeded,
            // but someone else's commit failed
            logout();
        }

        return true;
    }

    /**
     * Logout the user.
     * <p>
     * This method removes the <code>UserPrincipal</code> that was added by the <code>commit</code> method.
     * <p>
     *
     * @throws LoginException if the logout fails.
     * @return true in all cases since this <code>LoginModule</code> should not be ignored.
     */
    @Override
    public boolean logout() throws LoginException {
        // unset the alias
        ssl = null;
        sslUtils.setAppclientSsl(ssl);
        subject.getPrincipals().remove(userPrincipal);
        succeeded = false;
        commitSucceeded = false;
        alias = null;
        userPrincipal = null;
        return true;
    }
}
