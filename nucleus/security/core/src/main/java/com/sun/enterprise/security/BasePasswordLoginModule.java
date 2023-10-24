/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;

import com.sun.enterprise.security.auth.login.LoginCallbackHandler;
import com.sun.enterprise.security.auth.login.common.PasswordCredential;
import com.sun.enterprise.security.auth.realm.Realm;
import com.sun.enterprise.util.i18n.StringManager;
import java.security.Principal;
import java.util.Arrays;
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
import org.glassfish.security.common.UserNameAndPassword;
import org.glassfish.security.common.UserPrincipal;

/**
 * Abstract base class for password-based login modules.
 *
 * <P>
 * Most login modules receive a username and password from the client (possibly through HTTP BASIC auth, or FORM, or other
 * mechanism) and then make (or delegate) an authentication decision based on this data. This class provides common methods for
 * such password-based login modules.
 *
 * <P>
 * Subclasses need to implement the authenticateUser() method and later call commitUserAuthentication().
 */
public abstract class BasePasswordLoginModule implements LoginModule {

    protected static final Logger _logger = SecurityLoggerInfo.getLogger();
    protected static final StringManager sm = StringManager.getManager(LoginCallbackHandler.class);

    // The _subject, _sharedState and _options satisfy LoginModule and are
    // shared across sub-classes
    protected Subject _subject;
    protected Map<String, ?> _sharedState;
    protected Map<String, ?> _options;
    protected String _username;
    protected String _password;
    protected char[] _passwd;
    protected Realm _currentRealm;

    // The authentication status
    protected boolean _succeeded;
    protected boolean _commitSucceeded;
    protected UserPrincipal _userPrincipal;
    protected String[] _groupsList;


    /**
     * Initialize this login module.
     *
     * @param subject - the Subject to be authenticated.
     * @param callbackHandler - a CallbackHandler for obtaining the subject username and password.
     * @param sharedState - state shared with other configured LoginModules.
     * @param options - options specified in the login Configuration for this particular LoginModule.
     *
     */
    @Override
    final public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        _subject = subject;
        _sharedState = sharedState;
        _options = options;
        _logger.log(FINE, () -> "Login module initialized: " + this.getClass().toString());
    }

    /**
     * Perform login.
     *
     * <P>
     * The callback handler is used to obtain authentication info for the subject and a login is attempted. This PasswordLoginModule
     * expects to find a PasswordCredential in the private credentials of the Subject. If not present the login fails. The callback
     * handler is ignored as it is not really relevant on the server side. Finally, the authenticateUser() method is invoked.
     *
     * @returns true if login succeeds, otherwise an exception is thrown.
     * @throws LoginException Thrown if login failed, or on other problems.
     *
     */
    @Override
    final public boolean login() throws LoginException {
        // Extract the username and password
        extractCredentials();

        // Delegate the actual authentication to subclass.
        authenticateUser();
        _logger.log(FINE, "JAAS login complete.");

        return true;
    }

    /**
     * Commit the authentication.
     * <P>
     * Commit is called after all necessary login modules have succeeded. It adds (if not present) a
     * {@link UserNameAndPassword} principal and a LocalCredentials public credential to the Subject.
     *
     * @throws LoginException If commit fails.
     */
    @Override
    public boolean commit() throws LoginException {
        if (_succeeded == false) {
            return false;
        }

        // Add a Principal (authenticated identity) to the Subject
        String realm_name = _currentRealm.getName();
        PrincipalGroupFactory factory = Globals.getDefaultHabitat().getService(PrincipalGroupFactory.class);
        if (factory == null) {
            _userPrincipal = new UserNameAndPassword(getUsername());
        } else {
            _userPrincipal = factory.getPrincipalInstance(getUsername(), realm_name);
        }

        Set<Principal> principalSet = _subject.getPrincipals();
        if (!principalSet.contains(_userPrincipal)) {
            principalSet.add(_userPrincipal);
        }

        // Populate the group in the subject and clean out the slate at the same time
        for (int i = 0; i < _groupsList.length; i++) {
            if (_groupsList[i] != null) {
                Group group;
                if (factory != null) {
                    group = factory.getGroupInstance(_groupsList[i], realm_name);
                } else {
                    group = new Group(_groupsList[i]);
                }

                if (!principalSet.contains(group)) {
                    principalSet.add(group);
                }

                // Cleaning the slate
                _groupsList[i] = null;
            }
        }

        // In any case, clean out state.
        _groupsList = null;
        setUsername(null);
        setPassword(null);
        setPasswordChar(null);
        _commitSucceeded = true;

        _logger.log(FINE, "JAAS authentication committed.");

        return true;
    }

    /**
     * Abort the authentication process.
     *
     */
    @Override
    final public boolean abort() throws LoginException {
        _logger.log(FINE, "JAAS authentication aborted.");

        if (_succeeded == false) {
            return false;
        }

        if (_succeeded == true && _commitSucceeded == false) {
            // login succeeded but overall authentication failed
            _succeeded = false;
            setUsername(null);
            setPassword(null);
            setPasswordChar(null);
            _userPrincipal = null;
            for (int i = 0; i < _groupsList.length; i++) {
                _groupsList[i] = null;
            }
            _groupsList = null;
        } else {
            // overall authentication succeeded and commit succeeded,
            // but someone else's commit failed
            logout();
        }

        return true;
    }

    /**
     * Log out the subject.
     *
     */
    @Override
    final public boolean logout() throws LoginException {
        _logger.log(FINE, () -> "JAAS logout for: " + _subject.toString());

        _subject.getPrincipals().clear();
        _subject.getPublicCredentials().clear();
        _subject.getPrivateCredentials().clear();

        _succeeded = false;
        _commitSucceeded = false;
        setUsername(null);
        setPassword(null);
        _userPrincipal = null;
        if (_groupsList != null) {
            for (int i = 0; i < _groupsList.length; i++) {
                _groupsList[i] = null;
            }
            _groupsList = null;
        }

        return true;
    }

    /**
     *
     * <P>
     * This is a convenience method which can be used by subclasses
     *
     * <P>
     * Note that this method is called after the authentication has succeeded. If authentication failed do not call this method.
     *
     * Global instance field succeeded is set to true by this method.
     *
     * @param groups String array of group memberships for user (could be empty).
     */
    public final void commitUserAuthentication(final String[] groups) {
        // Copy the groups into a new array before storing it in the instance
        String[] groupsListCopy = (groups == null) ? null : Arrays.copyOf(groups, groups.length);

        _groupsList = groupsListCopy;
        _succeeded = true;
    }

    /**
     * @return the subject being authenticated. use case: A custom login module could overwrite commit() method, and call
     * getSubject() to get subject being authenticated inside its commit(). Custom principal then can be added to subject. By doing
     * this,custom principal will be stored in calling thread's security context and participate in following Appserver's
     * authorization.
     *
     */
    public Subject getSubject() {
        return _subject;
    }

    /**
     * @return the username sent by container - is made available to the custom login module using the protected _username field. Use
     * Case: A custom login module could use the username to validate against a realm of users
     */
    public String getUsername() {
        return _username;
    }

    /**
     * password is preferred to be a char[]
     */
    public String getPassword() {
        return _password;
    }

    /**
     * @return the password sent by container - is made available to the custom login module using the protected _password field. Use
     * Case: A custom login module could use the password to validate against a custom realm of usernames and passwords Password is
     * preferred to be a char[] instead of a string
     */

    public char[] getPasswordChar() {
        return Arrays.copyOf(_passwd, _passwd.length);
    }

    /**
     * @return the currentRealm - for backward compatability
     */
    public Realm getCurrentRealm() {
        return _currentRealm;
    }

    /**
     * @return the succeeded state - for backward compatability
     */
    public boolean isSucceeded() {
        return _succeeded;
    }

    /**
     * @return the commitsucceeded state - for backward compatability
     */
    public boolean isCommitSucceeded() {
        return _commitSucceeded;
    }

    /**
     * @return the UserPrincipal - for backward compatability
     */
    public UserPrincipal getUserPrincipal() {
        return _userPrincipal;
    }

    /**
     * @return the groupList - for backward compatability
     */
    public String[] getGroupsList() {
        return Arrays.copyOf(_groupsList, _groupsList.length);
    }

    /**
     * Method to extract container-provided username and password
     *
     * @throws javax.security.auth.login.LoginException
     */
    final public void extractCredentials() throws LoginException {
        if (_subject == null) {
            String msg = sm.getString("pwdlm.noinfo");
            _logger.log(SEVERE, msg);
            throw new LoginException(msg);
        }

        PasswordCredential passwordCredential = getPasswordCredential(_subject);

        // Need to obtain the requested realm to get parameters.

        String realm = null;
        try {
            realm = passwordCredential.getRealm();
            _currentRealm = Realm.getInstance(realm);

        } catch (Exception e) {
            String msg = sm.getString("pwdlm.norealm", realm);
            _logger.log(SEVERE, msg);
            throw new LoginException(msg);
        }

        // Get username and password data from credential (ignore callback)

        setUsername(passwordCredential.getUser());
        setPasswordChar(passwordCredential.getPassword());
        setPassword(new String(passwordCredential.getPassword()));
    }

    protected <T> T getRealm(Class<T> realmClass, String exceptionKey) throws LoginException {
        if (!realmClass.isAssignableFrom(_currentRealm.getClass())) {
            throw new LoginException(sm.getString(exceptionKey));
        }

        return realmClass.cast(_currentRealm);
    }

    /**
     * Perform authentication decision.
     *
     * Method returns silently on success and returns a LoginException on failure.
     *
     * @throws LoginException on authentication failure.
     *
     */
    protected abstract void authenticateUser() throws LoginException;

    private PasswordCredential getPasswordCredential(Subject subject) throws LoginException {
        try {
            for (Object privateCredential : subject.getPrivateCredentials()) {
                if (privateCredential instanceof PasswordCredential) {
                    return (PasswordCredential) privateCredential;
                }
            }
        } catch (Exception e) {
            _logger.log(Level.WARNING, SecurityLoggerInfo.privateSubjectCredentialsError, e.toString());
        }

        _logger.log(SEVERE, SecurityLoggerInfo.noPwdCredentialProvidedError);

        throw new LoginException(sm.getString("pwdlm.nocreds"));
    }

    /**
     * Used for setting the username obtained from the container internally, to be made available to the custom login module
     * implementation
     *
     * @param username
     */
    private void setUsername(String username) {
        this._username = username;
    }

    /**
     * password is preferred to be a char[]
     */
    private void setPassword(String password) {
        this._password = password;
    }

    /**
     * Used for setting the password obtained from the container internally, to be made available to the custom login module
     * implementation Password is preferred to be a char[] instead of a string
     *
     * @param password
     */
    private void setPasswordChar(char[] password) {
        this._passwd = password;
    }


}
