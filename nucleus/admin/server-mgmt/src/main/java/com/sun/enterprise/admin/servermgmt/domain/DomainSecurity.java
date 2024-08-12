/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.domain;

import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.MasterPasswordFileManager;
import com.sun.enterprise.admin.servermgmt.RepositoryException;
import com.sun.enterprise.security.store.PasswordAdapter;

import java.io.File;
import java.io.IOException;

import org.glassfish.security.common.FileRealmHelper;

import static com.sun.enterprise.admin.servermgmt.domain.DomainConstants.KEYSTORE_FILE;
import static com.sun.enterprise.admin.servermgmt.domain.DomainConstants.TRUSTSTORE_FILE;

public class DomainSecurity extends MasterPasswordFileManager {


    /**
     * Modifies the contents of given keyfile with administrator's user-name and password. Uses the FileRealm classes that
     * application server's Runtime uses.
     *
     * @param keyFile File to store encrypted admin credentials.
     * @param user Username.
     * @param password Password.
     */
    void processAdminKeyFile(File keyFile, String user, String password, final String[] adminUserGroups) throws IOException {
        final String keyFilePath = keyFile.getAbsolutePath();
        final FileRealmHelper fileRealm = new FileRealmHelper(keyFilePath);
        final String[] group = adminUserGroups;
        fileRealm.addUser(user, password.toCharArray(), group);
        fileRealm.persist();
    }

    /**
     * Create the password alias keystore (initially empty)
     *
     * @param pwFile File to store encrypted password.
     * @param password password protecting the keystore
     * @throws RepositoryException if any error occurs in creation.
     */
    void createPasswordAliasKeystore(File pwFile, String password) throws RepositoryException {
        try {
            PasswordAdapter p = new PasswordAdapter(pwFile.getAbsolutePath(), password.toCharArray());
            p.writeStore();
        } catch (Exception ex) {
            throw new RepositoryException("Could not create password alias keystore " + pwFile, ex);
        }
    }

    /**
     * Create the default SSL key store using keytool to generate a self signed certificate.
     *
     * @param configRoot Config directory.
     * @param config A {@link DomainConfig} object
     * @param masterPassword Master password.
     * @throws RepositoryException if any error occurs during keystore creation.
     */
    void createSSLCertificateDatabase(File configDir, DomainConfig config, String masterPassword) throws RepositoryException {
        createKeyStore(new File(configDir, KEYSTORE_FILE), config, masterPassword);
        changeKeystorePassword(DEFAULT_MASTER_PASSWORD, masterPassword, new File(configDir, TRUSTSTORE_FILE));
        copyCertificatesToTrustStore(configDir, config, masterPassword);
    }

    /**
     * Change the permission for a given file/directory.
     * <p>
     * <b>NOTE:</b> Applicable only for Unix env.
     * </p>
     *
     * @param args New sets of permission arguments.
     * @param file File on which permission has to be applied.
     * @throws IOException If any IO error occurs during operation.
     */
    void changeMode(String args, File file) throws IOException {
        super.chmod(args, file);
    }
}
