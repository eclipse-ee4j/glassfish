/*
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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.glassfish.security.common.PrincipalImpl;

import com.sun.enterprise.security.SecurityLoggerInfo;
import com.sun.enterprise.security.UsernamePasswordStore;
import com.sun.enterprise.security.auth.login.common.PasswordCredential;
import com.sun.enterprise.util.LocalStringManagerImpl;

/**
 * <p>
 * This sample LoginModule authenticates users with a password.
 *
 * <p>
 * If testUser successfully authenticates itself, a <code>PrincipalImpl</code> with the testUser's username is added to the
 * Subject.
 *
 * @author Harpreet Singh (harpreet.singh@sun.com)
 */

public class ClientPasswordLoginModule implements LoginModule {

    private static final Logger _logger = SecurityLoggerInfo.getLogger();

    private static final String DEFAULT_REALMNAME = "default";
    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ClientPasswordLoginModule.class);
    // initial state
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map sharedState;
    private Map options;

    // the authentication status
    private boolean succeeded = false;
    private boolean commitSucceeded = false;

    // username and password
    private String username;
    private char[] password;

    // testUser's PrincipalImpl
    private PrincipalImpl userPrincipal;
    public static final String LOGIN_NAME = "j2eelogin.name";
    public static final String LOGIN_PASSWORD = "j2eelogin.password";

    /**
     * Initialize this <code>LoginModule</code>.
     *
     * <p>
     *
     * @param subject the <code>Subject</code> to be authenticated.
     * <p>
     *
     * @param callbackHandler a <code>CallbackHandler</code> for communicating with the end user (prompting for usernames and
     * passwords, for example).
     * <p>
     *
     * @param sharedState shared <code>LoginModule</code> state.
     * <p>
     *
     * @param options options specified in the login <code>Configuration</code> for this particular <code>LoginModule</code>.
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {

        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;

    }

    /**
     * Authenticate the user by prompting for a username and password.
     *
     * <p>
     *
     * @return true in all cases since this <code>LoginModule</code> should not be ignored.
     *
     * @exception FailedLoginException if the authentication fails.
     * <p>
     *
     * @exception LoginException if this <code>LoginModule</code> is unable to perform the authentication.
     */
    @Override
    public boolean login() throws LoginException {

        // prompt for a username and password
        if (callbackHandler == null) {
            String failure = localStrings.getLocalString("login.nocallback",
                "Error: no CallbackHandler available to garner authentication information from the user");
            throw new LoginException(failure);
        }

        // Get the username from the exchange mechanism
        String uname = UsernamePasswordStore.getUsername();
        char[] pswd = UsernamePasswordStore.getPassword();
        boolean doSet = false;

        // bugfix# 6412539
        if (uname == null) {
            uname = System.getProperty(LOGIN_NAME);
            doSet = true;
        }
        if (pswd == null) {
            if (System.getProperty(LOGIN_PASSWORD) != null) {
                pswd = System.getProperty(LOGIN_PASSWORD).toCharArray();
            }
            doSet = true;
        }

        if (doSet) {
            UsernamePasswordStore.set(uname, pswd);
        }

        if (uname != null && pswd != null) {
            username = uname;

            int length = pswd.length;
            password = new char[length];
            System.arraycopy(pswd, 0, password, 0, length);
        } else {
            Callback[] callbacks = new Callback[2];
            NameCallback nameCB = new NameCallback(localStrings.getLocalString("login.username", "ClientPasswordModule username"));
            String defaultUname = System.getProperty("user.name");
            if (defaultUname != null) {
                nameCB = new NameCallback(localStrings.getLocalString("login.username", "ClientPasswordModule username"), defaultUname);
            }
            callbacks[0] = nameCB;
            callbacks[1] = new PasswordCallback(localStrings.getLocalString("login.password", "ClientPasswordModule password: "), false);

            try {
                callbackHandler.handle(callbacks);
                username = ((NameCallback) callbacks[0]).getName();
                if (username == null) {
                    String fail = localStrings.getLocalString("login.nousername", "No user specified");
                    throw new LoginException(fail);
                }
                char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
                if (tmpPassword == null) {
                    // treat a NULL password as an empty password
                    tmpPassword = new char[0];
                }
                password = new char[tmpPassword.length];
                System.arraycopy(tmpPassword, 0, password, 0, tmpPassword.length);
                ((PasswordCallback) callbacks[1]).clearPassword();

            } catch (java.io.IOException ioe) {
                throw new LoginException(ioe.toString());
            } catch (UnsupportedCallbackException uce) {
                String nocallback = localStrings.getLocalString("login.callback",
                    "Error: Callback not available to garner authentication information from user(CallbackName):");
                throw new LoginException(nocallback + uce.getCallback().toString());
            }
        }

        // by default -  the client side login module will always say
        // that the login successful. The actual login will take place
        // on the server side.

        _logger.log(Level.FINEST, "\t\t[ClientPasswordLoginModule] " + "authentication succeeded");
        succeeded = true;
        return true;
    }

    /**
     * <p>
     * This method is called if the LoginContext's overall authentication succeeded (the relevant REQUIRED, REQUISITE, SUFFICIENT and
     * OPTIONAL LoginModules succeeded).
     *
     * <p>
     * If this LoginModule's own authentication attempt succeeded (checked by retrieving the private state saved by the
     * <code>login</code> method), then this method associates a <code>PrincipalImpl</code> with the <code>Subject</code> located in
     * the <code>LoginModule</code>. If this LoginModule's own authentication attempted failed, then this method removes any state
     * that was originally saved.
     *
     * <p>
     *
     * @exception LoginException if the commit fails.
     *
     * @return true if this LoginModule's own login and commit attempts succeeded, or false otherwise.
     */
    @Override
    public boolean commit() throws LoginException {
        if (succeeded == false) {
            return false;
        } else {
            // add a Principal (authenticated identity)
            // to the Subject

            // assume the user we authenticated is the PrincipalImpl
            userPrincipal = new PrincipalImpl(username);
            if (!subject.getPrincipals().contains(userPrincipal)) {
                subject.getPrincipals().add(userPrincipal);
            }
            _logger.log(Level.FINE, "\t\t[ClientPasswordLoginModule] " + "added PrincipalImpl to Subject");

            String realm = DEFAULT_REALMNAME;

            PasswordCredential pc = new PasswordCredential(username, password, realm);
            if (!subject.getPrivateCredentials().contains(pc)) {
                subject.getPrivateCredentials().add(pc);
            }
            // in any case, clean out state
            username = null;
            for (int i = 0; i < password.length; i++) {
                password[i] = ' ';
            }
            password = null;
            commitSucceeded = true;
            return true;
        }
    }

    /**
     * <p>
     * This method is called if the LoginContext's overall authentication failed. (the relevant REQUIRED, REQUISITE, SUFFICIENT and
     * OPTIONAL LoginModules did not succeed).
     *
     * <p>
     * If this LoginModule's own authentication attempt succeeded (checked by retrieving the private state saved by the
     * <code>login</code> and <code>commit</code> methods), then this method cleans up any state that was originally saved.
     *
     * <p>
     *
     * @exception LoginException if the abort fails.
     *
     * @return false if this LoginModule's own login and/or commit attempts failed, and true otherwise.
     */
    @Override
    public boolean abort() throws LoginException {
        if (succeeded == false) {
            return false;
        } else if (succeeded == true && commitSucceeded == false) {
            // login succeeded but overall authentication failed
            succeeded = false;
            username = null;
            if (password != null) {
                for (int i = 0; i < password.length; i++) {
                    password[i] = ' ';
                }
                password = null;
            }
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
     *
     * <p>
     * This method removes the <code>PrincipalImpl</code> that was added by the <code>commit</code> method.
     *
     * <p>
     *
     * @exception LoginException if the logout fails.
     *
     * @return true in all cases since this <code>LoginModule</code> should not be ignored.
     */
    @Override
    public boolean logout() throws LoginException {

        subject.getPrincipals().remove(userPrincipal);
        succeeded = commitSucceeded;
        username = null;
        if (password != null) {
            for (int i = 0; i < password.length; i++) {
                password[i] = ' ';
            }
            password = null;
        }
        userPrincipal = null;
        return true;
    }
}
