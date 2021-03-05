/*
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

/*
 * BaseContainerCallbackHandler.java
 *
 * Created on April 21, 2004, 11:56 AM
 */

package com.sun.enterprise.security.jmac.callback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PrivilegedAction;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.x500.X500Principal;

import org.glassfish.internal.api.Globals;
//V3:Commented import com.sun.enterprise.Switch;
import org.glassfish.security.common.Group;
import org.glassfish.security.common.MasterPassword;
import org.glassfish.security.common.PrincipalImpl;

import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.security.SecurityServicesUtil;
import com.sun.enterprise.security.auth.login.DistinguishedPrincipalCredential;
import com.sun.enterprise.security.auth.login.LoginContextDriver;
import com.sun.enterprise.security.auth.login.common.LoginException;
import com.sun.enterprise.security.auth.realm.certificate.CertificateRealm;
import com.sun.enterprise.security.common.AppservAccessController;
import com.sun.enterprise.security.jmac.config.CallbackHandlerConfig;
import com.sun.enterprise.security.jmac.config.GFServerConfigProvider;
import com.sun.enterprise.security.jmac.config.HandlerContext;
import com.sun.enterprise.security.ssl.SSLUtils;
import com.sun.enterprise.security.store.PasswordAdapter;
import com.sun.enterprise.security.web.integration.WebPrincipal;
import com.sun.enterprise.server.pluggable.SecuritySupport;
import com.sun.logging.LogDomains;

import jakarta.security.auth.message.callback.CallerPrincipalCallback;
import jakarta.security.auth.message.callback.CertStoreCallback;
import jakarta.security.auth.message.callback.GroupPrincipalCallback;
import jakarta.security.auth.message.callback.PasswordValidationCallback;
import jakarta.security.auth.message.callback.PrivateKeyCallback;
import jakarta.security.auth.message.callback.SecretKeyCallback;
import jakarta.security.auth.message.callback.TrustStoreCallback;

/**
 * Base Callback Handler for Jakarta Authentication
 *
 * @author Harpreet Singh
 * @author Shing Wai Chan
 */
abstract class BaseContainerCallbackHandler implements CallbackHandler, CallbackHandlerConfig {

    private static final String DEFAULT_DIGEST_ALGORITHM = "SHA-1";
    private static final String CLIENT_SECRET_KEYSTORE = "com.sun.appserv.client.secretKeyStore";
    private static final String CLIENT_SECRET_KEYSTORE_PASSWORD = "com.sun.appserv.client.secretKeyStorePassword";

    protected final static Logger _logger = LogDomains.getLogger(BaseContainerCallbackHandler.class, LogDomains.SECURITY_LOGGER);

    protected HandlerContext handlerContext;

    protected final SSLUtils sslUtils;
    protected final SecuritySupport securitySupport;
    protected final MasterPassword masterPasswordHelper;

    protected BaseContainerCallbackHandler() {
        if (Globals.getDefaultHabitat() == null) {
            sslUtils = new SSLUtils();
            securitySupport = SecuritySupport.getDefaultInstance();
            masterPasswordHelper = null;
            sslUtils.postConstruct();
        } else {
            sslUtils = Globals.getDefaultHabitat().getService(SSLUtils.class);
            securitySupport = Globals.getDefaultHabitat().getService(SecuritySupport.class);
            masterPasswordHelper = Globals.getDefaultHabitat().getService(MasterPassword.class, "Security SSL Password Provider Service");
        }
    }

    @Override
    public void setHandlerContext(HandlerContext handlerContext) {
        this.handlerContext = handlerContext;
    }

    /*
     * To be implemented by a sub-class. The sub class decides which callbacks it supports.
     * <i>EjbServletWSSCallbackHandler</i> supports: <li>SecretKeyCallback</li> <li>TrustStoreCallback</li>
     * <li>PasswordValidationCallback</li> <li>CertStoreCallback</li> <li>PrivateKeyCallback</li> <i>
     * AppclientWSSCallbackHandler</i> supports: <li>NameCallback</li> <li>PasswordCallback</li> <li>ChoiceCallback</li>
     */
    protected abstract boolean isSupportedCallback(Callback callback);

    protected abstract void handleSupportedCallbacks(Callback[] callbacks) throws IOException, UnsupportedCallbackException;

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        if (callbacks == null) {
            return;
        }

        for (Callback callback : callbacks) {
            if (!isSupportedCallback(callback)) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "JMAC: UnsupportedCallback : " + callback.getClass().getName());
                }
                throw new UnsupportedCallbackException(callback);
            }
        }

        handleSupportedCallbacks(callbacks);
    }

    /**
     * gets the appropriate callback processor and hands the callback to processor to process the callback.
     */
    protected void processCallback(Callback callback) throws UnsupportedCallbackException {
        if (callback instanceof CallerPrincipalCallback) {
            processCallerPrincipal((CallerPrincipalCallback) callback);
        } else if (callback instanceof GroupPrincipalCallback) {
            processGroupPrincipal((GroupPrincipalCallback) callback);
        } else if (callback instanceof PasswordValidationCallback) {
            processPasswordValidation((PasswordValidationCallback) callback);
        } else if (callback instanceof PrivateKeyCallback) {
            processPrivateKey((PrivateKeyCallback) callback);
        } else if (callback instanceof TrustStoreCallback) {
            TrustStoreCallback tstoreCallback = (TrustStoreCallback) callback;
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "JMAC: In TrustStoreCallback Processor");
            }
            tstoreCallback.setTrustStore(sslUtils.getMergedTrustStore());

        } else if (callback instanceof CertStoreCallback) {
            processCertStore((CertStoreCallback) callback);
        } else if (callback instanceof SecretKeyCallback) {
            processSecretKey((SecretKeyCallback) callback);
        } else {
            // sanity check =- should never come here.
            // the isSupportedCallback method already takes care of this case
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "JMAC: UnsupportedCallback : " + callback.getClass().getName());
            }
            throw new UnsupportedCallbackException(callback);
        }
    }

    /**
     * This method will distinguish the initiator principal (of the SecurityContext obtained from the WebPrincipal) as the
     * caller principal, and copy all the other principals into the subject....
     *
     * It is assumed that the input WebPrincipal is coming from a SAM, and that it was created either by the SAM (as
     * described below) or by calls to the LoginContextDriver made by an Authenticator.
     *
     * A WebPrincipal constructed by the RealmAdapter will include a DPC; other constructions may not; this method
     * interprets the absence of a DPC as evidence that the resulting WebPrincipal was not constructed by the RealmAdapter
     * as described below. Note that presence of a DPC does not necessarily mean that the resulting WebPrincipal was
     * constructed by the RealmAdapter... since some authenticators also add the credential).
     *
     * A. handling of CPCB by CBH:
     *
     * 1. handling of CPC by CBH modifies subject a. constructs principalImpl if called by name b. uses LoginContextDriver
     * to add group principals for name c. puts principal in principal set, and DPC in public credentials
     *
     * B. construction of WebPrincipal by RealmAdapter (occurs after SAM uses CBH to set other than an unauthenticated
     * result in the subject:
     *
     * a. SecurityContext construction done with subject (returned by SAM). Construction sets initiator/caller principal
     * within SC from DPC set by CBH in public credentials of subject
     *
     * b WebPrincipal is constructed with initiator principal and SecurityContext
     *
     * @param fs receiving Subject
     * @param wp WebPrincipal
     *
     * @return true when Security Context has been obtained from webPrincipal, and CB is finished. returns false when more
     * CB processing is required.
     */
    private boolean reuseWebPrincipal(final Subject fs, final WebPrincipal wp) {

        SecurityContext sc = wp.getSecurityContext();
        final Subject wps = sc != null ? sc.getSubject() : null;
        final Principal callerPrincipal = sc != null ? sc.getCallerPrincipal() : null;
        final Principal defaultPrincipal = SecurityContext.getDefaultCallerPrincipal();

        return (Boolean) AppservAccessController.doPrivileged(new PrivilegedAction() {

            /**
             * this method uses 4 (numbered) criteria to determine if the argument WebPrincipal can be reused
             */
            @Override
            public Boolean run() {

                /*
                 * 1. WebPrincipal must contain a SecurityContext and SC must have a non-null, non-default callerPrincipal and a Subject
                 */
                if (callerPrincipal == null || callerPrincipal.equals(defaultPrincipal) || wps == null) {
                    return Boolean.FALSE;
                }

                boolean hasObject = false;
                Set<DistinguishedPrincipalCredential> distinguishedCreds = wps.getPublicCredentials(DistinguishedPrincipalCredential.class);
                if (distinguishedCreds.size() == 1) {
                    for (DistinguishedPrincipalCredential cred : distinguishedCreds) {
                        if (cred.getPrincipal().equals(callerPrincipal)) {
                            hasObject = true;
                        }
                    }
                }

                /**
                 * 2. Subject within SecurityContext must contain a single DPC that identifies the Caller Principal
                 */
                if (!hasObject) {
                    return Boolean.FALSE;
                }

                hasObject = wps.getPrincipals().contains(callerPrincipal);

                /**
                 * 3. Subject within SecurityContext must contain the caller principal
                 */
                if (!hasObject) {
                    return Boolean.FALSE;
                }

                /**
                 * 4. The webPrincipal must have a non null name that equals the name of the callerPrincipal.
                 */
                if (wp.getName() == null || !wp.getName().equals(callerPrincipal.getName())) {
                    return Boolean.FALSE;
                }

                /*
                 * remove any existing DistinguishedPrincipalCredentials from receiving Subject
                 *
                 */
                Iterator iter = fs.getPublicCredentials().iterator();
                while (iter.hasNext()) {
                    Object obj = iter.next();
                    if (obj instanceof DistinguishedPrincipalCredential) {
                        iter.remove();
                    }
                }

                /**
                 * Copy principals from Subject within SecurityContext to receiving Subject
                 */

                fs.getPrincipals().addAll(wps.getPrincipals());

                /**
                 * Copy public credentials from Subject within SecurityContext to receiving Subject
                 */
                fs.getPublicCredentials().addAll(wps.getPublicCredentials());

                /**
                 * Copy private credentials from Subject within SecurityContext to receiving Subject
                 */
                fs.getPrivateCredentials().addAll(wps.getPrivateCredentials());

                return Boolean.TRUE;
            }
        });
    }

    private void processCallerPrincipal(CallerPrincipalCallback cpCallback) {
        final Subject fs = cpCallback.getSubject();
        Principal principal = cpCallback.getPrincipal();

        if (principal instanceof WebPrincipal) {
            WebPrincipal wp = (WebPrincipal) principal;
            /**
             * Check if the WebPrincipal satisfies the criteria for reuse. If it does, the CBH will have already copied its contents
             * into the Subject, and established the caller principal.
             */
            if (reuseWebPrincipal(fs, wp)) {
                return;
            }
            /**
             * Otherwise the webPrincipal must be distinguished as the callerPrincipal, but the contents of its internal
             * SecurityContext will not be copied. For the special case where the WebPrincipal represents the
             * defaultCallerPrincipal, the argument principal is set to null to cause the handler to assign its representation of
             * the unauthenticated caller in the Subject.
             */
            Principal dp = SecurityContext.getDefaultCallerPrincipal();
            SecurityContext sc = wp.getSecurityContext();
            Principal cp = sc != null ? sc.getCallerPrincipal() : null;

            if (wp.getName() == null || wp.equals(dp) || cp == null || cp.equals(dp)) {
                principal = null;
            }
        }

        String realmName = null;
        if (handlerContext != null) {
            realmName = handlerContext.getRealmName();
        }

        boolean isCertRealm = CertificateRealm.AUTH_TYPE.equals(realmName);
        if (principal == null) {
            if (cpCallback.getName() != null) {
                if (isCertRealm) {
                    principal = new X500Principal(cpCallback.getName());
                } else {
                    principal = new PrincipalImpl(cpCallback.getName());
                }
            } else {
                // Jakarta Authentication unauthenticated caller principal
                principal = SecurityContext.getDefaultCallerPrincipal();
            }
        }

        if (isCertRealm) {
            if (principal instanceof X500Principal) {
                LoginContextDriver.jmacLogin(fs, (X500Principal) principal);
            }
        } else if (!principal.equals(SecurityContext.getDefaultCallerPrincipal())) {
            LoginContextDriver.jmacLogin(fs, principal.getName(), realmName);
        }

        final Principal fprin = principal;
        final DistinguishedPrincipalCredential fdpc = new DistinguishedPrincipalCredential(principal);
        AppservAccessController.doPrivileged(new PrivilegedAction() {
            @Override
            public java.lang.Object run() {
                fs.getPrincipals().add(fprin);
                Iterator iter = fs.getPublicCredentials().iterator();
                while (iter.hasNext()) {
                    Object obj = iter.next();
                    if (obj instanceof DistinguishedPrincipalCredential) {
                        iter.remove();
                    }
                }
                fs.getPublicCredentials().add(fdpc);
                return fs;
            }
        });
    }

    private void processGroupPrincipal(GroupPrincipalCallback gpCallback) {
        final Subject fs = gpCallback.getSubject();
        final String[] groups = gpCallback.getGroups();
        if (groups != null && groups.length > 0) {
            AppservAccessController.doPrivileged(new PrivilegedAction() {
                @Override
                public java.lang.Object run() {
                    for (String group : groups) {
                        fs.getPrincipals().add(new Group(group));
                    }
                    return fs;
                }
            });
        } else if (groups == null) {
            AppservAccessController.doPrivileged(new PrivilegedAction() {
                @Override
                public java.lang.Object run() {
                    Set<Principal> principalSet = fs.getPrincipals();
                    principalSet.removeAll(fs.getPrincipals(Group.class));
                    return fs;
                }
            });
        }
    }

    private void processPasswordValidation(PasswordValidationCallback pwdCallback) {

        // if (Switch.getSwitch().getContainerType() == Switch.APPCLIENT_CONTAINER) {
        if (SecurityServicesUtil.getInstance().isACC()) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "JMAC: In PasswordValidationCallback Processor for appclient - will do nothing");
            }
            pwdCallback.setResult(true);
            return;
        }
        String username = pwdCallback.getUsername();

        char[] passwd = pwdCallback.getPassword();

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "JMAC: In PasswordValidationCallback Processor");
        }
        try {
            String realmName = null;
            if (handlerContext != null) {
                realmName = handlerContext.getRealmName();
            }
            Subject s = LoginContextDriver.jmacLogin(pwdCallback.getSubject(), username, passwd, realmName);
            GFServerConfigProvider.setValidateRequestSubject(s);
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "JMAC: authentication succeeded for user = ", username);
            }
            // explicitly ditch the password
            if (passwd != null) {
                for (int i = 0; i < passwd.length; i++) {
                    passwd[i] = ' ';
                }
            }
            pwdCallback.setResult(true);
        } catch (LoginException le) {
            // login failed
            if (_logger.isLoggable(Level.INFO)) {
                _logger.log(Level.INFO, "jmac.loginfail", username);
            }
            pwdCallback.setResult(false);
        }
    }

    private void processPrivateKey(PrivateKeyCallback privKeyCallback) {
        KeyStore[] keyStores = securitySupport.getKeyStores();
        _logger.log(Level.FINE, "JMAC: In PrivateKeyCallback Processor");

        // make sure we have a keystore
        if (keyStores == null || keyStores.length == 0) {
            // cannot get any information
            privKeyCallback.setKey(null, null);
            return;
        }

        // get the request type
        PrivateKeyCallback.Request request = privKeyCallback.getRequest();
        PrivateKey privateKey = null;
        Certificate[] certificateChain = null;
        if (request == null) {
            // no request type - set default key
            PrivateKeyEntry privateKeyEntry = getDefaultPrivateKeyEntry(keyStores);
            if (privateKeyEntry != null) {
                privateKey = privateKeyEntry.getPrivateKey();
                certificateChain = privateKeyEntry.getCertificateChain();
            }
            privKeyCallback.setKey(privateKey, certificateChain);
            return;
        }

        // find key based on request type
        try {
            if (request instanceof PrivateKeyCallback.AliasRequest) {
                PrivateKeyCallback.AliasRequest aliasRequest = (PrivateKeyCallback.AliasRequest) request;

                String alias = aliasRequest.getAlias();
                PrivateKeyEntry privKeyEntry;

                if (alias == null) {
                    // use default key
                    privKeyEntry = getDefaultPrivateKeyEntry(keyStores);
                } else {
                    privKeyEntry = sslUtils.getPrivateKeyEntryFromTokenAlias(alias);
                }

                if (privKeyEntry != null) {
                    privateKey = privKeyEntry.getPrivateKey();
                    certificateChain = privKeyEntry.getCertificateChain();
                }
            } else if (request instanceof PrivateKeyCallback.IssuerSerialNumRequest) {
                PrivateKeyCallback.IssuerSerialNumRequest issuerSerialNumRequest = (PrivateKeyCallback.IssuerSerialNumRequest) request;

                X500Principal issuer = issuerSerialNumRequest.getIssuer();
                BigInteger serialNum = issuerSerialNumRequest.getSerialNum();

                if (issuer != null && serialNum != null) {
                    boolean found = false;
                    for (int i = 0; i < keyStores.length && !found; i++) {
                        Enumeration<String> aliases = keyStores[i].aliases();
                        while (aliases.hasMoreElements() && !found) {
                            String nextAlias = aliases.nextElement();
                            PrivateKey key = securitySupport.getPrivateKeyForAlias(nextAlias, i);
                            if (key != null) {
                                Certificate[] certificates = keyStores[i].getCertificateChain(nextAlias);
                                // check issuer/serial
                                X509Certificate eeCert = (X509Certificate) certificates[0];
                                if (eeCert.getIssuerX500Principal().equals(issuer) && eeCert.getSerialNumber().equals(serialNum)) {
                                    privateKey = key;
                                    certificateChain = certificates;
                                    found = true;
                                }
                            }
                        }
                    }
                }
            } else if (request instanceof PrivateKeyCallback.SubjectKeyIDRequest) {
                PrivateKeyCallback.SubjectKeyIDRequest subjectKeyIDRequest = (PrivateKeyCallback.SubjectKeyIDRequest) request;
                byte[] subjectKeyID = subjectKeyIDRequest.getSubjectKeyID();

                if (subjectKeyID != null) {
                    boolean found = false;

                    X509CertSelector selector = new X509CertSelector();
                    selector.setSubjectKeyIdentifier(toDerOctetString(subjectKeyID));

                    for (int i = 0; i < keyStores.length && !found; i++) {
                        Enumeration<String> aliases = keyStores[i].aliases();
                        while (aliases.hasMoreElements() && !found) {
                            String nextAlias = aliases.nextElement();
                            PrivateKey key = securitySupport.getPrivateKeyForAlias(nextAlias, i);

                            if (key != null) {
                                Certificate[] certificates = keyStores[i].getCertificateChain(nextAlias);

                                if (selector.match(certificates[0])) {
                                    privateKey = key;
                                    certificateChain = certificates;
                                    found = true;
                                }
                            }
                        }
                    }
                }
            } else if (request instanceof PrivateKeyCallback.DigestRequest) {
                PrivateKeyCallback.DigestRequest digestRequest = (PrivateKeyCallback.DigestRequest) request;
                byte[] digest = digestRequest.getDigest();
                String algorithm = digestRequest.getAlgorithm();

                PrivateKeyEntry privateKeyEntry = null;
                if (digest == null) {
                    // get default key
                    privateKeyEntry = getDefaultPrivateKeyEntry(keyStores);
                } else {
                    if (algorithm == null) {
                        algorithm = DEFAULT_DIGEST_ALGORITHM;
                    }
                    MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
                    privateKeyEntry = getPrivateKeyEntry(keyStores, messageDigest, digest);
                }

                if (privateKeyEntry != null) {
                    privateKey = privateKeyEntry.getPrivateKey();
                    certificateChain = privateKeyEntry.getCertificateChain();
                }
            } else if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "invalid request type: " + request.getClass().getName());
            }
        } catch (Exception e) {
            // UnrecoverableKeyException
            // NoSuchAlgorithmException
            // KeyStoreException
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "JMAC: In PrivateKeyCallback Processor: " + " Error reading key !", e);
            }
        } finally {
            privKeyCallback.setKey(privateKey, certificateChain);
        }
    }

    private byte[] toDerOctetString(byte[] value) throws IOException {
        ByteArrayOutputStream subjectOutputStream = new ByteArrayOutputStream();

        subjectOutputStream.write(0x04); // DER Octet String tag
        subjectOutputStream.write(length2Bytes(value.length));
        subjectOutputStream.write(value);

        return subjectOutputStream.toByteArray();
    }

    /**
     * Splits out an integer into a variable number of bytes with the first byte containing either the number of bytes, or
     * the integer itself if small enough.
     *
     * @param length the integer to convert
     * @return the integer in DER byte array form
     */
    private byte[] length2Bytes(int length) {
        // The first byte with the MSB bit a 0 encodes the direct length
        // E.g. 0b00000001 for length = 1
        if (length <= 127) {
            return new byte[] { (byte) length };
        }

        // Count how many bytes are in the "length" integer
        int byteCount = 1;
        int lengthValue = length;

        while ((lengthValue >>>= 8) != 0) {
            byteCount++;
        }

        byte[] lengthBytes = new byte[byteCount + 1];

        // The first byte with the MSB bit a 1 encodes the number of bytes used for the length
        // E.g. 0b10000001 for 1 additional byte (for values up to 255)
        lengthBytes[0] = (byte) (byteCount | 0b10000000);

        // Shift the integer in increments of 8 bits, and truncate the lowest 8 ones in every iteration.
        // For numbers up to 255 shift 0 times, e.g. for length 255 take the binary version 0b11111111 directly.
        // For numbers up to 65535 shift 1 time, e.g. for length 256
        // first byte = 0b100000000 >> 8 = 0b000000001 -> 0b00000001
        // second byte = 0b100000000 >> 0 = 0b000000000 -> 0b00000000
        int pos = 1;
        for (int i = (byteCount - 1) * 8; i >= 0; i -= 8) {
            lengthBytes[pos] = (byte) (length >> i);
            pos++;
        }

        return lengthBytes;
    }

    /**
     * Return the first key/chain that we can successfully get out of the keystore
     */
    private PrivateKeyEntry getDefaultPrivateKeyEntry(KeyStore[] keyStores) {
        PrivateKey privateKey = null;
        Certificate[] certificates = null;
        try {
            for (int i = 0; i < keyStores.length && privateKey == null; i++) {
                Enumeration<String> aliases = keyStores[i].aliases();
                // loop thru aliases and try to get the key/chain
                while (aliases.hasMoreElements() && privateKey == null) {
                    String nextAlias = aliases.nextElement();
                    privateKey = null;
                    certificates = null;
                    PrivateKey key = securitySupport.getPrivateKeyForAlias(nextAlias, i);
                    if (key != null) {
                        privateKey = key;
                        certificates = keyStores[i].getCertificateChain(nextAlias);
                    }
                }
            }
        } catch (Exception e) {
            // UnrecoverableKeyException
            // NoSuchAlgorithmException
            // KeyStoreException
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Exception in getDefaultPrivateKeyEntry", e);
            }
        }

        return new PrivateKeyEntry(privateKey, certificates);
    }

    private PrivateKeyEntry getPrivateKeyEntry(KeyStore[] kstores, MessageDigest md, byte[] digest) {
        PrivateKey privKey = null;
        Certificate[] certs = null;
        try {
            for (int i = 0; i < kstores.length && privKey == null; i++) {
                Enumeration<String> aliases = kstores[i].aliases();
                // loop thru aliases and try to get the key/chain
                while (aliases.hasMoreElements() && privKey == null) {
                    String nextAlias = aliases.nextElement();
                    privKey = null;
                    certs = null;
                    PrivateKey key = securitySupport.getPrivateKeyForAlias(nextAlias, i);
                    if (key != null) {
                        certs = kstores[i].getCertificateChain(nextAlias);
                        md.reset();
                        byte[] cDigest = md.digest(certs[0].getEncoded());
                        if (Arrays.equals(digest, cDigest)) {
                            privKey = key;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // UnrecoverableKeyException
            // NoSuchAlgorithmException
            // KeyStoreException
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Exception in getPrivateKeyEntry for Digest", e);
            }
        }

        return new PrivateKeyEntry(privKey, certs);
    }

    private void processCertStore(CertStoreCallback certStoreCallback) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "JMAC: In CertStoreCallback Processor");
        }

        KeyStore certStore = sslUtils.getMergedTrustStore();
        if (certStore == null) {// should never happen
            certStoreCallback.setCertStore(null);
        }
        List<Certificate> list = new ArrayList<>();
        CollectionCertStoreParameters ccsp;
        try {
            if (certStore != null) {
                Enumeration enu = certStore.aliases();
                while (enu.hasMoreElements()) {
                    String alias = (String) enu.nextElement();
                    if (certStore.isCertificateEntry(alias)) {
                        try {
                            Certificate cert = certStore.getCertificate(alias);
                            list.add(cert);
                        } catch (KeyStoreException kse) {
                            // ignore and move to next
                            if (_logger.isLoggable(Level.FINE)) {
                                _logger.log(Level.FINE, "JMAC: Cannot retrieve" + "certificate for alias " + alias);
                            }
                        }
                    }
                }
            }
            ccsp = new CollectionCertStoreParameters(list);
            CertStore certstore = CertStore.getInstance("Collection", ccsp);
            certStoreCallback.setCertStore(certstore);
        } catch (KeyStoreException kse) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "JMAC:  Cannot determine truststore aliases", kse);
            }
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException nsape) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "JMAC:  Cannot instantiate CertStore", nsape);
            }
        }
    }

    private void processSecretKey(SecretKeyCallback secretKeyCallback) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "JMAC: In SecretKeyCallback Processor");
        }

        String alias = ((SecretKeyCallback.AliasRequest) secretKeyCallback.getRequest()).getAlias();
        if (alias != null) {
            try {
                PasswordAdapter passwordAdapter = null;
                // (Switch.getSwitch().getContainerType() ==
                // Switch.APPCLIENT_CONTAINER) {
                if (SecurityServicesUtil.getInstance().isACC()) {
                    passwordAdapter = new PasswordAdapter(System.getProperty(CLIENT_SECRET_KEYSTORE),
                            System.getProperty(CLIENT_SECRET_KEYSTORE_PASSWORD).toCharArray());
                } else {
                    passwordAdapter = masterPasswordHelper.getMasterPasswordAdapter();
                }

                secretKeyCallback.setKey(passwordAdapter.getPasswordSecretKeyForAlias(alias));
            } catch (Exception e) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "JMAC: In SecretKeyCallback Processor: " + " Error reading key ! for alias " + alias, e);
                }
                secretKeyCallback.setKey(null);
            }
        } else {
            // Dont bother about checking for principal
            // we dont support that feature - typically
            // used in an environment with kerberos
            // Principal p = secretKeyCallback.getPrincipal();
            secretKeyCallback.setKey(null);
            if (_logger.isLoggable(Level.WARNING)) {
                _logger.log(Level.WARNING, "jmac.unsupportreadprinciple");
            }
        }
    }
}
