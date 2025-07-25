/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

/*
 * RepositoryManager.java
 *
 * Created on August 19, 2003, 2:29 PM
 */

package com.sun.enterprise.admin.servermgmt;

import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import com.sun.enterprise.security.store.PasswordAdapter;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;

import static com.sun.enterprise.util.SystemPropertyConstants.MASTER_PASSWORD_ALIAS;
import static com.sun.enterprise.util.SystemPropertyConstants.MASTER_PASSWORD_PASSWORD;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The RepositoryManager serves as a common base class for the following PEDomainsManager, PEInstancesManager,
 * AgentManager (the SE Node Agent). Its purpose is to abstract out any shared functionality related to lifecycle
 * management of domains, instances and node agents. This includes creation, deletion, listing, and starting and
 * stopping.
 *
 * @author kebbs
 */
public class MasterPasswordFileManager extends KeystoreManager {

    private static final StringManager _strMgr = StringManager.getManager(MasterPasswordFileManager.class);

    /**
     * @return The password protecting the master password keywtore
     */
    private char[] getMasterPasswordPassword() throws RepositoryException {
        return MASTER_PASSWORD_PASSWORD.toCharArray();
    }

    protected void deleteMasterPasswordFile(RepositoryConfig config) {
        final PEFileLayout layout = getFileLayout(config);
        final File pwdFile = layout.getMasterPasswordFile();
        FileUtils.deleteFile(pwdFile);
    }

    /**
     * Create the master password keystore. This routine can also modify the master password if the keystore already exists
     *
     * @param config
     * @param masterPassword
     * @throws RepositoryException
     */
    protected void createMasterPasswordFile(RepositoryConfig config, String masterPassword) throws RepositoryException {
        final PEFileLayout layout = getFileLayout(config);
        final File pwdFile = layout.getMasterPasswordFile();
        try {
            PasswordAdapter p = new PasswordAdapter(pwdFile.getAbsolutePath(), getMasterPasswordPassword());
            p.setPasswordForAlias(MASTER_PASSWORD_ALIAS, masterPassword.getBytes(UTF_8));
            chmod("600", pwdFile);
        } catch (Exception ex) {
            throw new RepositoryException(_strMgr.getString("masterPasswordFileNotCreated", pwdFile), ex);
        }
    }

    /**
     * Return the master password stored in the master password keystore.
     *
     * @param config
     * @return null if the password file does not exist, the password otherwise.
     * @throws RepositoryException
     */
    public String readMasterPasswordFile(RepositoryConfig config) throws RepositoryException {
        final PEFileLayout layout = getFileLayout(config);
        final File pwdFile = layout.getMasterPasswordFile();
        if (!pwdFile.exists()) {
            return null;
        }
        try {
            PasswordAdapter p = new PasswordAdapter(pwdFile.getAbsolutePath(), getMasterPasswordPassword());
            return p.getPasswordForAlias(MASTER_PASSWORD_ALIAS);
        } catch (Exception ex) {
            throw new RepositoryException(_strMgr.getString("masterPasswordFileNotRead", pwdFile), ex);
        }
    }

    /**
     * Changes the master password in the master password file
     *
     * @param saveMasterPassword
     * @param config
     * @param newPassword
     * @throws RepositoryException
     */
    protected void changeMasterPasswordInMasterPasswordFile(RepositoryConfig config, String newPassword, boolean saveMasterPassword)
            throws RepositoryException {
        deleteMasterPasswordFile(config);
        if (saveMasterPassword) {
            createMasterPasswordFile(config, newPassword);
        }
    }

    /**
     * Changes the master password in the master password file
     *
     * @param pwdFile
     * @param newPassword
     * @param saveMasterPassword
     * @throws RepositoryException
     */
    public void changeMasterPasswordInMasterPasswordFile(File pwdFile, String newPassword, boolean saveMasterPassword)
            throws RepositoryException {
        FileUtils.deleteFile(pwdFile);
        if (saveMasterPassword) {
            try {
                PasswordAdapter p = new PasswordAdapter(pwdFile.getAbsolutePath(), getMasterPasswordPassword());
                p.setPasswordForAlias(MASTER_PASSWORD_ALIAS, newPassword.getBytes(UTF_8));
                chmod("600", pwdFile);
            } catch (Exception ex) {
                throw new RepositoryException(_strMgr.getString("masterPasswordFileNotCreated", pwdFile), ex);
            }
        }
    }
}
