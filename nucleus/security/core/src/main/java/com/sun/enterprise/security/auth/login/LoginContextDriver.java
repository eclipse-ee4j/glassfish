/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

import static com.sun.enterprise.security.SecurityLoggerInfo.auditAtnRefusedError;
import static com.sun.enterprise.security.SecurityLoggerInfo.noSuchUserInRealmError;
import static com.sun.enterprise.util.Utility.isEmpty;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

import com.sun.enterprise.common.iiop.security.AnonCredential;
import com.sun.enterprise.common.iiop.security.GSSUPName;
import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.security.SecurityLoggerInfo;
import com.sun.enterprise.security.SecurityServicesUtil;
import com.sun.enterprise.security.audit.AuditManager;
import com.sun.enterprise.security.auth.login.common.LoginException;
import com.sun.enterprise.security.auth.login.common.PasswordCredential;
import com.sun.enterprise.security.auth.login.common.ServerLoginCallbackHandler;
import com.sun.enterprise.security.auth.login.common.X509CertificateCredential;
import com.sun.enterprise.security.auth.realm.Realm;
import com.sun.enterprise.security.auth.realm.certificate.CertificateRealm;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchUserException;
import com.sun.enterprise.security.common.AppservAccessController;
import com.sun.enterprise.security.common.ClientSecurityContext;
import com.sun.enterprise.security.common.SecurityConstants;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.x500.X500Principal;
import org.glassfish.internal.api.Globals;
import org.glassfish.security.common.Group;
import org.glassfish.security.common.UserNameAndPassword;

/**
 *
 * This class is invoked implicitly by the server to log in the user information that was sent on the wire by the client. Clients
 * will use the <i>doClientLogin</i> method to simulate authentication to the server.
 *
 * @author Harpreet Singh (hsingh@eng.sun.com)
 * @author Jyri Virkki
 *
 */
public class LoginContextDriver {

    private static final Logger _logger = SecurityLoggerInfo.getLogger();

    private static final ServerLoginCallbackHandler dummyCallback = new ServerLoginCallbackHandler();

    public static final String CERT_REALMNAME = "certificate";

    private static volatile AuditManager AUDIT_MANAGER;

    /**
     * This class cannot be instantiated
     *
     */
    private LoginContextDriver() {
    }

    private static AuditManager getAuditManager() {
        if (AUDIT_MANAGER != null) {
            return AUDIT_MANAGER;
        }
        return _getAuditManager();
    }

    private static synchronized AuditManager _getAuditManager() {
        if (AUDIT_MANAGER == null) {
            SecurityServicesUtil secServUtil = Globals.get(SecurityServicesUtil.class);
            AUDIT_MANAGER = secServUtil.getAuditManager();
        }
        return AUDIT_MANAGER;
    }

    /**
     * This method is just a convenience wrapper for <i>login(Subject, Class)</i> method. It will construct a PasswordCredential
     * class.
     *
     * @param String username
     * @param String password
     * @param String realmName the name of the realm to login into, if realmName is null, we login into the default realm
     */

    public static void login(String username, char[] password, String realmName) {
        if (realmName == null || !Realm.isValidRealm(realmName)) {
            realmName = Realm.getDefaultRealm();
        }

        final Subject subject = new Subject();
        subject.getPrivateCredentials().add(new PasswordCredential(username, password, realmName));

        LoginContextDriver.login(subject, PasswordCredential.class);
    }

    /**
     * This method performs the login on the server side.
     *
     * <P>
     * This method is the main login method for S1AS. It is called with a Subject and the type (class) of credential which should be
     * checked. The Subject must contain a credential of the specified type or login will fail.
     *
     * <P>
     * While the implementation has been cleaned up, the login process still consists of a number of special cases which are treated
     * separately at the realm level. In the future tighter JAAS integration could clean some of this up.
     *
     * <P>
     * The following credential types are recognized at this time:
     * <ul>
     * <li>PasswordCredential - This is the general case for all login methods which rely on the client providing a name and
     * password. It can be used with any realms/JAAS login modules which expect such data (e.g. file realm, LDAP realm, UNIX realm)
     * <LI>X509CertificateCredential - Special case for SSL client auth. Here authentication has already been done by the SSL
     * subsystem so this login only creates a security context based on the certificate data.
     * <LI>AnonCredential - Unauthenticated session, set anonymous security context.
     * <LI>GSSUPName - Retrieve user and realm and set security context.
     * <LI>X500Principal - Retrieve user and realm and set security context.
     * </ul>
     *
     * @param Subject the subject of the client
     * @param Class the class of the credential packaged in the subject.
     *
     */
    public static void login(Subject subject, Class<?> credentialClass) throws LoginException {
        if (_logger.isLoggable(FINEST)) {
            _logger.log(FINEST, "Processing login with credentials of type: " + credentialClass.toString());
        }

        if (credentialClass.equals(PasswordCredential.class)) {
            doPasswordLogin(subject);

        } else if (credentialClass.equals(X509CertificateCredential.class)) {
            doCertificateLogin(subject);

        } else if (credentialClass.equals(AnonCredential.class)) {
            doAnonLogin();

        } else if (credentialClass.equals(GSSUPName.class)) {
            doGSSUPLogin(subject);

        } else if (credentialClass.equals(X500Principal.class)) {
            doX500Login(subject, null);

        } else {
            _logger.log(INFO, SecurityLoggerInfo.unknownCredentialError, credentialClass.toString());
            throw new LoginException("Unknown credential type, cannot login.");
        }
    }

    /**
     * This method is used for logging in a run As principal. It creates a JAAS subject whose credential is to type GSSUPName. This
     * is used primarily for runas
     *
     */
    public static void loginPrincipal(String username, String realmName) throws LoginException {
        // No realm provided, assuming default
        if (realmName == null || realmName.isEmpty()) {
            realmName = Realm.getDefaultRealm();
        }

        final Subject subject = new Subject();
        subject.getPrincipals().add(new UserNameAndPassword(username));
        subject.getPublicCredentials().add(new GSSUPName(username, realmName));

        try {
            Enumeration<String> groupNames = Realm.getInstance(realmName).getGroupNames(username);
            while (groupNames.hasMoreElements()) {
                subject.getPrincipals().add(new Group(groupNames.nextElement()));
            }
        } catch (NoSuchUserException ex) {
            _logger.log(WARNING, noSuchUserInRealmError, new Object[] { username, realmName, ex.toString() });
        } catch (NoSuchRealmException ex) {
            LoginException lex = new LoginException(ex.toString());
            lex.initCause(ex);
            throw lex;
        }

        setSecurityContext(username, subject, realmName);
    }

    /**
     * This method logs out the user by clearing the security context.
     *
     */
    public static void logout() throws LoginException {
        unsetSecurityContext();
    }

    /**
     * Log in subject with PasswordCredential. This is a generic login which applies to all login mechanisms which process
     * PasswordCredential. In other words, any mechanism which receives an actual username, realm and password set from the client.
     *
     * <P>
     * The realm contained in the credential is checked, and a JAAS LoginContext is created using a context name obtained from the
     * appropriate Realm instance. The applicable JAAS LoginModule is initialized (based on the jaas login configuration) and login()
     * is invoked on it.
     *
     * <P>
     * RI code makes several assumptions which are retained here:
     * <ul>
     * <li>The PasswordCredential is stored as a private credential of the subject.
     * <li>There is only one such credential present (actually, only the first one is relevant if more are present). </ui>
     *
     * @param s Subject to be authenticated.
     * @throws LoginException Thrown if the login fails.
     *
     */
    private static void doPasswordLogin(Subject subject) throws LoginException {
        Object obj = getPrivateCredentials(subject, PasswordCredential.class);

        PasswordCredential passwordCredential = (PasswordCredential) obj;
        String user = passwordCredential.getUser();
        String realm = passwordCredential.getRealm();
        String jaasCtx = getJaasCtx(realm);

        _logger.log(FINE, () -> "Logging in user [" + user + "] into realm: " + realm + " using JAAS module: " + jaasCtx);

        try {
            // A dummyCallback is used to satisfy JAAS but it is never used.
            // name/pwd info is already contained in Subject's Credential
            new LoginContext(jaasCtx, subject, dummyCallback).login();

        } catch (Exception e) {
            _logger.log(FINEST, "doPasswordLogin fails", e);

            if (getAuditManager() != null && getAuditManager().isAuditOn()) {
                getAuditManager().authentication(user, realm, false);
            }
            if (e instanceof LoginException) {
                throw (LoginException) e;
            }

            throw (LoginException) new LoginException("Login failed: " + e.getMessage()).initCause(e);

        }
        if (getAuditManager() != null && getAuditManager().isAuditOn()) {
            getAuditManager().authentication(user, realm, true);
        }

        _logger.log(FINE, () -> "Password login succeeded for : " + user);

        setSecurityContext(user, subject, realm);

        _logger.log(FINE, () -> "Set security context as user: " + user);
    }

    private static String getJaasCtx(String realm) {
        try {
            return Realm.getInstance(realm).getJAASContext();
        } catch (Exception ex) {
            if (ex instanceof LoginException) {
                throw (LoginException) ex;
            }

            throw (LoginException) new LoginException(ex.toString()).initCause(ex);
        }
    }

    public static void jmacLogin(Subject subject, Principal callerPrincipal, String realmName) throws LoginException {
        if (CertificateRealm.AUTH_TYPE.equals(realmName)) {
            if (callerPrincipal instanceof X500Principal) {
                LoginContextDriver.jmacLogin(subject, (X500Principal) callerPrincipal);
            }
        } else if (!callerPrincipal.equals(SecurityContext.getDefaultCallerPrincipal())) {
            LoginContextDriver.jmacLogin(subject, callerPrincipal.getName(), realmName);
        }
    }

    /**
     * Performs login for JMAC security. The difference between this method and others is that it just verifies whether the login
     * will succeed in the given realm. It does not set the result of the authentication in the appserver runtime environment A
     * silent return from this method means that the given user succeeding in authenticating with the given password in the given
     * realm
     *
     * @param subject
     * @param username
     * @param password
     * @param realmName the realm to authenticate under
     * @returns Subject on successful authentication
     * @throws LoginException
     */
    public static Subject jmacLogin(Subject subject, String username, char[] password, String realmName) throws LoginException {
        if (realmName == null || !Realm.isValidRealm(realmName)) {
            realmName = Realm.getDefaultRealm();
        }

        if (subject == null) {
            subject = new Subject();
        }

        final PasswordCredential passwordCredential = new PasswordCredential(username, password, realmName);
        subject.getPrivateCredentials().add(passwordCredential);

        String jaasCtx = getJaasCtx(realmName);

        if (_logger.isLoggable(FINE)) {
            _logger.log(FINE, "jmac login user [" + username + "] into realm: " + realmName + " using JAAS module: " + jaasCtx);
        }

        try {
            // A dummyCallback is used to satisfy JAAS but it is never used.
            // name/pwd info is already contained in Subject's Credential
            new LoginContext(jaasCtx, subject, dummyCallback).login();

        } catch (Exception e) {
            if (_logger.isLoggable(INFO)) {
                _logger.log(INFO, SecurityLoggerInfo.auditAtnRefusedError, username);
            }
            if (getAuditManager().isAuditOn()) {
                getAuditManager().authentication(username, realmName, false);
            }

            if (e instanceof LoginException) {
                throw (LoginException) e;
            } else {
                throw (LoginException) new LoginException("Login failed: " + e.getMessage()).initCause(e);
            }
        }
        if (getAuditManager().isAuditOn()) {
            getAuditManager().authentication(username, realmName, true);
        }
        if (_logger.isLoggable(FINE)) {
            _logger.fine("jmac Password login succeeded for : " + username);
        }

        return subject;
        // do not set the security Context
    }

    public static Subject jmacLogin(Subject subject, X500Principal x500Principal) throws LoginException {
        if (subject == null) {
            subject = new Subject();
        }

        String userName = "";
        try {
            userName = x500Principal.getName();
            subject.getPublicCredentials().add(x500Principal);

            CertificateRealm certRealm = (CertificateRealm) Realm.getInstance(CertificateRealm.AUTH_TYPE);
            String jaasCtx = certRealm.getJAASContext();
            if (jaasCtx != null) {
                // The subject has the Certificate Credential.
                new LoginContext(jaasCtx, subject, dummyCallback).login();
            }
            certRealm.authenticate(subject, x500Principal);
        } catch (Exception ex) {
            if (_logger.isLoggable(INFO)) {
                _logger.log(INFO, auditAtnRefusedError, userName);
            }

            if (getAuditManager().isAuditOn()) {
                getAuditManager().authentication(userName, CertificateRealm.AUTH_TYPE, false);
            }

            if (ex instanceof LoginException) {
                throw (LoginException) ex;
            }

            throw (LoginException) new LoginException(ex.toString()).initCause(ex);
        }

        if (_logger.isLoggable(FINE)) {
            _logger.log(FINE, "jmac cert login succeeded for: " + userName);
        }

        if (getAuditManager().isAuditOn()) {
            getAuditManager().authentication(userName, CertificateRealm.AUTH_TYPE, true);
        }
        // do not set the security Context

        return subject;
    }

    public static Subject jmacLogin(Subject subject, String userName, String realm) throws LoginException {
        if (subject == null) {
            subject = new Subject();
        }

        try {
            if (isEmpty(realm)) {
                realm = Realm.getDefaultRealm();
            }

            Enumeration<String> groups = Realm.getInstance(realm).getGroupNames(userName);
            if (groups != null) {
                while (groups.hasMoreElements()) {
                    subject.getPrincipals().add(new Group(groups.nextElement()));
                }
            }
        } catch (Exception ex) {
            _logger.log(FINE, ex, () -> "Exception when trying to populate groups for CallerPrincipal " + userName);
        }

        return subject;
    }

    /**
     * A special case login for handling X509CertificateCredential. This does not get triggered based on current RI code. See
     * X500Login.
     *
     */
    private static void doCertificateLogin(Subject s) throws LoginException {
        _logger.log(FINE, "Processing X509 certificate login.");

        String realm = CertificateRealm.AUTH_TYPE;
        String user = null;
        try {
            Object obj = getPublicCredentials(s, X509CertificateCredential.class);

            X509CertificateCredential xp = (X509CertificateCredential) obj;
            user = xp.getAlias();
            if (_logger.isLoggable(FINE)) {
                _logger.log(FINE, "Set security context as user: " + user);
            }
            setSecurityContext(user, s, realm);
            if (getAuditManager().isAuditOn()) {
                getAuditManager().authentication(user, realm, true);
            }
        } catch (LoginException le) {
            if (getAuditManager().isAuditOn()) {
                getAuditManager().authentication(user, realm, false);
            }
            throw le;
        }
    }

    /**
     * A special case login for anonymous credentials (no login info).
     *
     */
    private static void doAnonLogin() throws LoginException {
        // instance of anononymous credential login with guest
        SecurityContext.setUnauthenticatedContext();
        if (_logger.isLoggable(FINE)) {
            _logger.log(FINE, "Set anonymous security context.");
        }
    }

    /**
     * A special case login for GSSUPName credentials.
     *
     */
    private static void doGSSUPLogin(Subject s) throws LoginException {
        if (_logger.isLoggable(FINE)) {
            _logger.fine("Processing GSSUP login.");
        }
        String user = null;
        String realm = Realm.getDefaultRealm();
        try {
            Object obj = getPublicCredentials(s, GSSUPName.class);

            user = ((GSSUPName) obj).getUser();

            setSecurityContext(user, s, realm);
            if (getAuditManager().isAuditOn()) {
                getAuditManager().authentication(user, realm, true);
            }
            if (_logger.isLoggable(FINE)) {
                _logger.fine("GSSUP login succeeded for : " + user);
            }
        } catch (LoginException le) {
            if (getAuditManager().isAuditOn()) {
                getAuditManager().authentication(user, realm, false);
            }
            throw le;
        }
    }

    /**
     * A special case login for X500Principal credentials. This is invoked for certificate login because the containers extract the
     * X.500 name from the X.509 certificate before calling into this class.
     *
     */
    public static void doX500Login(Subject s, String appModuleID) throws LoginException {
        if (_logger.isLoggable(FINE)) {
            _logger.fine("Processing X.500 name login.");
        }
        String user = null;
        String realm_name = null;
        try {
            X500Principal x500Principal = (X500Principal) getPublicCredentials(s, X500Principal.class);
            user = x500Principal.getName();

            // In the RI-inherited implementation this directly creates
            // some credentials and sets the security context. This means
            // that the certificate realm does not get an opportunity to
            // process the request. While the realm will not do any
            // authentication (already done by this point) it can choose
            // to adjust the groups or principal name or other variables
            // of the security context. Of course, bug 4646134 needs to be
            // kept in mind at all times.

            Realm realm = Realm.getInstance(CertificateRealm.AUTH_TYPE);

            if (realm instanceof CertificateRealm) { // should always be true

                CertificateRealm certRealm = (CertificateRealm) realm;
                String jaasCtx = certRealm.getJAASContext();
                if (jaasCtx != null) {
                    // The subject has the Cretificate Credential.
                    LoginContext lg = new LoginContext(jaasCtx, s, new ServerLoginCallbackHandler(user, null, appModuleID));
                    lg.login();
                }
                certRealm.authenticate(s, x500Principal);
                realm_name = CertificateRealm.AUTH_TYPE;
                if (getAuditManager().isAuditOn()) {
                    getAuditManager().authentication(user, realm_name, true);
                }
            } else {
                _logger.warning(SecurityLoggerInfo.certLoginBadRealmError);
                realm_name = realm.getName();
                setSecurityContext(user, s, realm_name);
            }

            if (_logger.isLoggable(FINE)) {
                _logger.fine("X.500 name login succeeded for : " + user);
            }
        } catch (LoginException le) {
            if (getAuditManager().isAuditOn()) {
                getAuditManager().authentication(user, realm_name, false);
            }
            throw le;
        } catch (Exception ex) {
            throw (LoginException) new LoginException(ex.toString()).initCause(ex);
        }
    }

    /**
     * Retrieve a public credential of the given type (java class) from the subject.
     *
     * <P>
     * This method retains the RI assumption that only the first credential of the given type is used.
     *
     */
    private static Object getPublicCredentials(Subject s, Class<?> cls) throws LoginException {
        Set credset = s.getPublicCredentials(cls);

        final Iterator iter = credset.iterator();

        if (!iter.hasNext()) {
            String credmsg = cls.toString();
            if (_logger.isLoggable(Level.FINER)) {
                _logger.finer("Expected public credentials of type : " + credmsg + " but none found.");
            }
            throw new LoginException("Expected public credential of type: " + credmsg + " but none found.");
        }

        Object obj = null;
        try {
            obj = AppservAccessController.doPrivileged(new PrivilegedAction() {
                @Override
                public java.lang.Object run() {
                    return iter.next();
                }
            });
        } catch (Exception e) {
            // should never come here
            if (e instanceof LoginException) {
                throw (LoginException) e;
            } else {
                throw (LoginException) new LoginException("Failed to retrieve public credential: " + e.getMessage()).initCause(e);
            }
        }

        return obj;
    }

    /**
     * Retrieve a private credential of the given type (java class) from the subject.
     *
     * <P>
     * This method retains the RI assumption that only the first credential of the given type is used.
     *
     */
    private static Object getPrivateCredentials(Subject subject, Class<?> cls) throws LoginException {
        final Subject s = subject;
        final Class<?> cl = cls;

        final Set credset = (Set) AppservAccessController.doPrivileged(new PrivilegedAction() {
            @Override
            public java.lang.Object run() {
                return s.getPrivateCredentials(cl);
            }
        });

        final Iterator iter = credset.iterator();

        if (!iter.hasNext()) {
            String credmsg = cls.toString();
            if (_logger.isLoggable(Level.FINER)) {
                _logger.finer("Expected private credential of type: " + credmsg + " but none found.");
            }
            throw new LoginException("Expected private credential of type: " + credmsg + " but none found.");
        }

        // retrieve only first credential of give type
        Object obj = null;
        try {
            obj = AppservAccessController.doPrivileged(new PrivilegedAction() {
                @Override
                public java.lang.Object run() {
                    return iter.next();
                }
            });
        } catch (Exception e) {
            // should never come here
            if (e instanceof LoginException) {
                throw (LoginException) e;
            } else {
                throw (LoginException) new LoginException("Failed to retrieve private credential: " + e.getMessage()).initCause(e);
            }
        }

        return obj;
    }

    /**
     * This method sets the security context on the current Thread Local Storage
     *
     * @param String username is the user who authenticated
     * @param Subject is the subject representation of the user
     * @param Credentials the credentials that the server associated with it
     */
    private static void setSecurityContext(String userName, Subject subject, String realm) {

        SecurityContext securityContext = new SecurityContext(userName, subject, realm);
        SecurityContext.setCurrent(securityContext);
    }

    /**
     * Set the current security context on the Thread Local Storage to null.
     *
     */
    private static void unsetSecurityContext() {
        SecurityContext.setCurrent((SecurityContext) null);
    }

    /**
     * Perform login on the client side. It just simulates the login on the client side. The method uses the callback handlers and
     * generates correct credential information that will be later sent to the server
     *
     * @param int type whether it is <i> username_password</i> or <i> certificate </i> based login.
     * @param CallbackHandler the callback handler to gather user information.
     * @exception LoginException the exception thrown by the callback handler.
     */
    public static Subject doClientLogin(int type, javax.security.auth.callback.CallbackHandler jaasHandler) throws LoginException {
        final javax.security.auth.callback.CallbackHandler handler = jaasHandler;
        // the subject will actually be filled in with a PasswordCredential
        // required by the csiv2 layer in the LoginModule.
        // we create the dummy credential here and call the
        // set security context. Thus, we have 2  credentials, one each for
        // the csiv2 layer and the other for the RI.
        final Subject subject = new Subject();

        if (type == SecurityConstants.USERNAME_PASSWORD) {
            AppservAccessController.doPrivileged(new PrivilegedAction() {
                @Override
                public java.lang.Object run() {
                    try {
                        LoginContext lg = new LoginContext(SecurityConstants.CLIENT_JAAS_PASSWORD, subject, handler);
                        lg.login();
                    } catch (javax.security.auth.login.LoginException e) {
                        throw (LoginException) new LoginException(e.toString()).initCause(e);
                    }

                    return null;
                }
            });
            postClientAuth(subject, PasswordCredential.class);
            return subject;
        } else if (type == SecurityConstants.CERTIFICATE) {
            AppservAccessController.doPrivileged(new PrivilegedAction() {
                @Override
                public java.lang.Object run() {
                    try {
                        LoginContext lg = new LoginContext(SecurityConstants.CLIENT_JAAS_CERTIFICATE, subject, handler);
                        lg.login();
                    } catch (javax.security.auth.login.LoginException e) {
                        throw (LoginException) new LoginException(e.toString()).initCause(e);
                    }

                    return null;
                }
            });
            postClientAuth(subject, X509CertificateCredential.class);
            return subject;
        } else if (type == SecurityConstants.ALL) {
            AppservAccessController.doPrivileged(new PrivilegedAction() {
                @Override
                public java.lang.Object run() {
                    try {
                        LoginContext lgup = new LoginContext(SecurityConstants.CLIENT_JAAS_PASSWORD, subject, handler);
                        LoginContext lgc = new LoginContext(SecurityConstants.CLIENT_JAAS_CERTIFICATE, subject, handler);
                        lgup.login();
                        postClientAuth(subject, PasswordCredential.class);

                        lgc.login();
                        postClientAuth(subject, X509CertificateCredential.class);
                    } catch (javax.security.auth.login.LoginException e) {
                        throw (LoginException) new LoginException(e.toString()).initCause(e);
                    }

                    return null;
                }
            });
            return subject;
        } else {
            AppservAccessController.doPrivileged(new PrivilegedAction() {
                @Override
                public java.lang.Object run() {
                    try {
                        LoginContext lg = new LoginContext(SecurityConstants.CLIENT_JAAS_PASSWORD, subject, handler);
                        lg.login();
                        postClientAuth(subject, PasswordCredential.class);
                    } catch (javax.security.auth.login.LoginException e) {
                        throw (LoginException) new LoginException(e.toString()).initCause(e);
                    }
                    return null;
                }
            });
            return subject;
        }
    }

    /**
     * Perform logout on the client side.
     *
     * @exception LoginException
     */
    public static void doClientLogout() throws LoginException {
        unsetClientSecurityContext();
    }

    /**
     * Performs Digest authentication based on RFC 2617. It
     *
     * @param digestCred DigestCredentials
     */
    public static void login(DigestCredentials digestCred) throws javax.security.auth.login.LoginException {
        Subject subject = new Subject();
        subject.getPrivateCredentials().add(digestCred);
        String jaasCtx = null;
        try {
            jaasCtx = Realm.getInstance(digestCred.getRealmName()).getJAASContext();
        } catch (Exception ex) {
            if (ex instanceof LoginException) {
                throw (LoginException) ex;
            } else {
                throw (LoginException) new LoginException(ex.toString()).initCause(ex);
            }
        }

        try {
            // A dummyCallback is used to satisfy JAAS but it is never used.
            // name/pwd info is already contained in Subject's Credential
            LoginContext lg = new LoginContext(jaasCtx, subject, dummyCallback);
            lg.login();
        } catch (Exception e) {
            if (_logger.isLoggable(INFO)) {
                _logger.log(INFO, SecurityLoggerInfo.auditAtnRefusedError, digestCred.getUserName());
            }
            if (_logger.isLoggable(FINEST)) {
                _logger.log(FINEST, "doPasswordLogin fails", e);
            }
            if (getAuditManager().isAuditOn()) {
                getAuditManager().authentication(digestCred.getUserName(), digestCred.getRealmName(), false);
            }
            if (e instanceof LoginException) {
                throw (LoginException) e;
            } else {
                throw (LoginException) new LoginException("Login failed: " + e.getMessage()).initCause(e);
            }
        }

        setSecurityContext(digestCred.getUserName(), subject, digestCred.getRealmName());
    }

    /**
     * Extract the relevant username and realm information from the subject and sets the correct state in the security context. The
     * relevant information is set into the Thread Local Storage from which then is extracted to send over the wire.
     *
     * @param Subject the subject returned by the JAAS login.
     * @param Class the class of the credential object stored in the subject
     *
     */
    private static void postClientAuth(Subject subject, Class<?> clazz) {
        final Class<?> clas = clazz;
        final Subject fs = subject;
        Set credset = (Set) AppservAccessController.doPrivileged(new PrivilegedAction() {
            @Override
            public java.lang.Object run() {
                if (_logger.isLoggable(FINEST)) {
                    _logger.log(FINEST, "LCD post login subject :" + fs);
                }
                return fs.getPrivateCredentials(clas);
            }
        });
        final Iterator iter = credset.iterator();
        while (iter.hasNext()) {
            Object obj = null;
            try {
                obj = AppservAccessController.doPrivileged(new PrivilegedAction() {
                    @Override
                    public java.lang.Object run() {
                        return iter.next();
                    }
                });
            } catch (Exception e) {
                // should never come here
                _logger.log(Level.SEVERE, SecurityLoggerInfo.securityAccessControllerActionError, e);
            }
            if (obj instanceof PasswordCredential) {
                PasswordCredential p = (PasswordCredential) obj;
                String user = p.getUser();
                if (_logger.isLoggable(FINEST)) {
                    String realm = p.getRealm();
                    _logger.log(FINEST, "In LCD user-pass login:" + user + " realm :" + realm);
                }
                setClientSecurityContext(user, fs);
                return;
            } else if (obj instanceof X509CertificateCredential) {
                X509CertificateCredential p = (X509CertificateCredential) obj;
                String user = p.getAlias();
                if (_logger.isLoggable(FINEST)) {
                    String realm = p.getRealm();
                    _logger.log(FINEST, "In LCD cert-login::" + user + " realm :" + realm);
                }
                setClientSecurityContext(user, fs);
                return;
            }
        }
    }

    /**
     * Sets the security context on the appclient side. It sets the relevant information into the TLS
     *
     * @param String username is the user who authenticated
     * @param Subject is the subject representation of the user
     * @param Credentials the credentials that the server associated with it
     */
    private static void setClientSecurityContext(String username, Subject subject) {
        ClientSecurityContext securityContext = new ClientSecurityContext(username, subject);
        ClientSecurityContext.setCurrent(securityContext);
    }

    /**
     * Unsets the current appclient security context on the Thread Local Storage
     */
    private static void unsetClientSecurityContext() {
        ClientSecurityContext.setCurrent(null);
    }

}
