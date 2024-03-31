/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.ssl;

import com.sun.enterprise.security.SecurityLoggerInfo;
import com.sun.enterprise.security.auth.login.common.LoginException;
import com.sun.enterprise.security.auth.login.common.PasswordCredential;
import com.sun.enterprise.security.auth.login.common.X509CertificateCredential;
import com.sun.enterprise.security.common.AppservAccessController;
import com.sun.enterprise.security.common.ClientSecurityContext;
import com.sun.enterprise.security.common.SecurityConstants;
import com.sun.enterprise.security.common.Util;
import com.sun.enterprise.security.ssl.manager.UnifiedX509KeyManager;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PrivilegedAction;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import static java.util.logging.Level.FINE;

/**
 * This a J2EE specific Key Manager class that is used to select user certificates for SSL client authentication. It delegates
 * most of the functionality to the provider specific KeyManager class.
 *
 * @author Vivek Nagar
 * @author Harpreet Singh
 */
public final class J2EEKeyManager extends X509ExtendedKeyManager {

    private static final Logger LOG = SecurityLoggerInfo.getLogger();

    private final X509KeyManager x509KeyManager; // delegate

    private final String alias;

    private Map<String, X509KeyManager> tokenName2MgrMap ;
    private boolean supportTokenAlias;

    public J2EEKeyManager(X509KeyManager mgr, String alias) {
        this.x509KeyManager = mgr;
        this.alias = alias;

        if (mgr instanceof UnifiedX509KeyManager) {
            UnifiedX509KeyManager umgr = (UnifiedX509KeyManager) mgr;
            X509KeyManager[] mgrs = umgr.getX509KeyManagers();
            String[] tokenNames = umgr.getTokenNames();

            tokenName2MgrMap = new HashMap<>();
            for (int i = 0; i < mgrs.length; i++) {
                if (tokenNames[i] != null) {
                    tokenName2MgrMap.put(tokenNames[i], mgrs[i]);
                }
            }
            supportTokenAlias = (tokenName2MgrMap.size() > 0);
        }
    }

    @Override
    public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
        return x509KeyManager.chooseClientAlias(keyType, issuers, null);
    }

    @Override
    public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
        return alias;
    }


    /**
     * Choose the client alias that will be used to select the client certificate for SSL client
     * auth.
     *
     * @param keyType
     * @param issuers certificate issuers.
     * @param socket socket used for this connection. This parameter can be null, in which case the
     *            method will return the most generic alias to use.
     * @return the alias.
     */
    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {

        String clientAlias = null;

        if (this.alias == null) {
            if (Util.getInstance().isNotServerOrACC()) {
                // standalone client
                clientAlias = x509KeyManager.chooseClientAlias(keyType, issuers, socket);
            } else {
                if (Util.getInstance().isACC()) {
                    ClientSecurityContext ctx = ClientSecurityContext.getCurrent();
                    Subject s = ctx.getSubject();
                    if (s == null) {
                        // Pass the handler and do the login
                        doClientLogin(SecurityConstants.CERTIFICATE, Util.getInstance().getCallbackHandler());
                        s = ctx.getSubject();
                    }
                    for (Object o : s.getPrivateCredentials()) {
                        if (o instanceof X509CertificateCredential) {
                            X509CertificateCredential crt = (X509CertificateCredential) o;
                            clientAlias = crt.getAlias();
                            break;
                        }
                    }
                }
            }
        } else {
            clientAlias = this.alias;
        }

        LOG.log(FINE, "Choose client Alias :{0}", clientAlias);
        return clientAlias;
    }

    /**
     * Choose the server alias that will be used to select the server certificate for SSL server auth.
     *
     * @param the keytype
     * @param the certificate issuers.
     * @param the socket used for this connection. This parameter can be null, in which case the method will return the most generic
     * alias to use.
     * @return the alias
     */
    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        String serverAlias = null;
        if (this.alias != null) {
            serverAlias = this.alias;
        } else {
            serverAlias = x509KeyManager.chooseServerAlias(keyType, issuers, socket);
        }

        LOG.log(FINE, "Choosing server alias :{0}", serverAlias);
        return serverAlias;
    }

    /**
     * Return the certificate chain for the specified alias.
     *
     * @param the alias.
     * @return the chain of X509 Certificates.
     */
    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        LOG.log(FINE, "Getting certificate chain");

        X509KeyManager keyMgr = getManagerFromToken(alias);
        if (keyMgr != null) {
            String aliasName = alias.substring(alias.indexOf(':') + 1);
            return keyMgr.getCertificateChain(aliasName);
        }

        return x509KeyManager.getCertificateChain(alias);
    }

    /**
     * Return all the available client aliases for the specified key type.
     *
     * @param the keytype
     * @param the certificate issuers.
     * @return the array of aliases.
     */
    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        LOG.log(FINE, "Getting client aliases");
        return x509KeyManager.getClientAliases(keyType, issuers);
    }

    /**
     * Return all the available server aliases for the specified key type.
     *
     * @param the keytype
     * @param the certificate issuers.
     * @return the array of aliases.
     */
    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        LOG.log(FINE, "Getting server aliases");
        return x509KeyManager.getServerAliases(keyType, issuers);
    }

    /**
     * Return the private key for the specified alias.
     *
     * @param the alias.
     * @return the private key.
     */
    @Override
    public PrivateKey getPrivateKey(String alias) {
        LOG.log(FINE, "Getting private key for alias: {0}", alias);
        X509KeyManager keyMgr = getManagerFromToken(alias);
        if (keyMgr != null) {
            String aliasName = alias.substring(alias.indexOf(':') + 1);
            return keyMgr.getPrivateKey(aliasName);
        } else {
            return x509KeyManager.getPrivateKey(alias);
        }
    }

    /**
     * Find the corresponding X509KeyManager associated to token in alias. It returns null if there is n
     *
     * @param tokenAlias of the form &lt;tokenName&gt;:&lt;aliasName&gt;
     */
    private X509KeyManager getManagerFromToken(String tokenAlias) {
        X509KeyManager keyMgr = null;
        int ind = -1;
        if (supportTokenAlias && tokenAlias != null && (ind = tokenAlias.indexOf(':')) != -1) {
            String tokenName = alias.substring(0, ind);
            keyMgr = tokenName2MgrMap.get(tokenName);
        }
        return keyMgr;
    }

    //TODO:V3 copied all method(s)below from LoginContextDriver to break dependencies among modules
    private static final String CLIENT_JAAS_PASSWORD = "default";

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
        //V3:Commented : TODO uncomment later for Appcontainer
        if (type == SecurityConstants.USERNAME_PASSWORD) {
            AppservAccessController.doPrivileged(new PrivilegedAction() {
                @Override
                public java.lang.Object run() {
                    try {
                        LoginContext lg = new LoginContext(SecurityConstants.CLIENT_JAAS_PASSWORD, subject, handler);
                        lg.login();
                    } catch (javax.security.auth.login.LoginException e) {
                        throw new LoginException(e.getMessage(), e);
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
                        throw new LoginException(e.getMessage(), e);
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
                        throw new LoginException(e.getMessage(), e);
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
                        throw new LoginException(e.getMessage(), e);
                    }
                    return null;
                }
            });
            return subject;
        }
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
        Set credset = AppservAccessController.doPrivileged(new PrivilegedAction<Set>() {
            @Override
            public Set run() {
                LOG.log(Level.FINEST, "LCD post login subject :{0}", fs);
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
                LOG.log(Level.SEVERE, SecurityLoggerInfo.securityAccessControllerActionError, e);
            }
            if (obj instanceof PasswordCredential) {
                PasswordCredential p = (PasswordCredential) obj;
                String user = p.getUser();
                LOG.log(Level.FINEST, "In LCD user-pass login:{0} realm :{1}", new Object[] {user, p.getRealm()});
                setClientSecurityContext(user, fs);
                return;
            } else if (obj instanceof X509CertificateCredential) {
                X509CertificateCredential p = (X509CertificateCredential) obj;
                String user = p.getAlias();
                LOG.log(Level.FINEST, "In LCD cert-login::{0} realm :{1}", new Object[] { user, p.getRealm() });
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
        ClientSecurityContext.setCurrent(new ClientSecurityContext(username, subject));
    }

}
