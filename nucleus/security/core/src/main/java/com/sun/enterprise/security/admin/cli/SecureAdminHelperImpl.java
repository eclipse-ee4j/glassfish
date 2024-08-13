/*
 * Copyright (c) 2011, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.admin.cli;

import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.SecureAdminHelper;
import com.sun.enterprise.security.auth.realm.exceptions.BadRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchUserException;
import com.sun.enterprise.security.auth.realm.file.FileRealm;
import com.sun.enterprise.security.auth.realm.file.FileRealmUser;
import com.sun.enterprise.security.ssl.SSLUtils;
import com.sun.enterprise.security.store.DomainScopedPasswordAliasStore;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Various utility methods which support secure admin operations.
 *
 * @author Tim Quinn
 */
@Service
@PerLookup
public class SecureAdminHelperImpl implements SecureAdminHelper {

    private static final char[] emptyPassword = new char[0];
    private final static String DOMAIN_ADMIN_GROUP_NAME = "asadmin";

    @Inject
    private SSLUtils sslUtils;

    @Inject
    private DomainScopedPasswordAliasStore domainPasswordAliasStore;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private volatile AdminService as;

    /**
     * Returns the correct DN to use for a given secure admin principal, mapping the alias (if it's an alias specified) to the DN for
     * the corresponding cert in the key store.
     *
     * @param value user-provided value (alias name or the actual DN)
     * @param isAlias whether the value is an alias
     * @return DN to use
     * @throws IOException if there is an error accessing the key store
     * @throws KeyStoreException if the keystore has not been initialized
     * @throws IllegalArgumentException if the cert for the specified alias as fetched from the key store is not an X509 certificate
     */
    @Override
    public String getDN(final String value, final boolean isAlias) throws IOException, KeyStoreException {
        if (isAlias) {
            final KeyStore keyStore = sslUtils.getKeyStore();
            if (keyStore == null) {
                throw new RuntimeException(Strings.get("noKeyStore"));
            }
            final Certificate cert = keyStore.getCertificate(value);
            if (cert == null) {
                throw new IllegalArgumentException(Strings.get("noAlias", value));
            }
            if (!(cert instanceof X509Certificate)) {
                throw new IllegalArgumentException(Strings.get("certNotX509Certificate", value));
            }
            return (((X509Certificate) cert).getSubjectX500Principal().getName());
        } else {
            return value;
        }
    }

    /**
     * Makes sure the username is a valid admin username and that the password alias is defined. This method does NOT make sure that
     * the password associated with the username and the password associated with the password alias are the same.
     *
     * @param username user-provided username
     * @param passwordAlias name of the password alias
     */
    @Override
    public void validateInternalUsernameAndPasswordAlias(String username, String passwordAlias) {
        try {
            validateUser(username);
            validatePasswordAlias(passwordAlias);
        } catch (Exception ex) {
            throw new RuntimeException(Strings.get("errVal"), ex);
        }
    }

    private void validateUser(final String username) throws BadRealmException, NoSuchRealmException {
        final FileRealm fr = adminRealm();
        try {
            FileRealmUser fru = (FileRealmUser) fr.getUser(username);
            if (isInAdminGroup(fru)) {
                return;
            }
            /*
             * The user is valid but is not in the admin group.
             */
            throw new RuntimeException(Strings.get("notAdminUser", username));
        } catch (NoSuchUserException ex) {
            /*
             * The user is not valid, but use the same error as if the user
             * IS present but is not an admin user.  This provides a would-be
             * intruder a little less information by not distinguishing
             * between a valid user that's not an admin user and an
             * invalid user.
             */
            throw new RuntimeException(Strings.get("notAdminUser", username));
        }
    }

    private boolean isInAdminGroup(final FileRealmUser user) {
        for (String group : user.getGroups()) {
            if (group.equals(DOMAIN_ADMIN_GROUP_NAME)) {
                return true;
            }
        }
        return false;
    }

    private void validatePasswordAlias(final String passwordAlias)
        throws CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchAlgorithmException, IOException {

        if (!domainPasswordAliasStore.containsKey(passwordAlias)) {
            throw new RuntimeException(Strings.get("noAlias", passwordAlias));
        }
    }

    private FileRealm adminRealm() throws BadRealmException, NoSuchRealmException {
        final AuthRealm ar = as.getAssociatedAuthRealm();
        if (FileRealm.class.getName().equals(ar.getClassname())) {
            String adminKeyFilePath = ar.getPropertyValue("file");
            FileRealm fr = new FileRealm(adminKeyFilePath);
            return fr;
        }
        return null;
    }

    /**
     * Returns whether at least one admin user has an empty password.
     *
     * @return true if at least one admin user has an empty password; false otherwise
     * @throws BadRealmException
     * @throws NoSuchRealmException
     * @throws NoSuchUserException
     */
    @Override
    public boolean isAnyAdminUserWithoutPassword() throws Exception {
        final FileRealm adminRealm = adminRealm();
        /*
         * If the user has configured the admin realm to use a realm other than
         * the default file realm bypass the check that makes sure no admin users have
         * an empty password.
         */
        if (adminRealm == null) {
            return false;
        }
        for (final Enumeration<String> e = adminRealm.getUserNames(); e.hasMoreElements();) {
            final String username = e.nextElement();
            /*
                * Try to authenticate this user with an empty password.  If it
                * works we can stop.
                */
            final String[] groupNames = adminRealm.authenticate(username, emptyPassword);
            if (groupNames != null) {
                for (String groupName : groupNames) {
                    if (DOMAIN_ADMIN_GROUP_NAME.equals(groupName)) {
                        return true;
                    }
                }
            }

        }
        return false;
    }
}
