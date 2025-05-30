/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.servermgmt.pe;

import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.DomainException;
import com.sun.enterprise.admin.servermgmt.DomainsManager;
import com.sun.enterprise.admin.servermgmt.RepositoryException;
import com.sun.enterprise.admin.servermgmt.RepositoryManager;
import com.sun.enterprise.admin.util.TokenValueSet;
import com.sun.enterprise.util.i18n.StringManager;

import java.io.File;
import java.util.BitSet;

public class PEDomainsManager extends RepositoryManager implements DomainsManager {

    /**
     * i18n strings manager object
     */
    private static final StringManager strMgr = StringManager.getManager(PEDomainsManager.class);

    /* These properties are public interfaces, handle with care */
    public static final String PROFILEPROPERTY_DOMAINXML_STYLESHEETS = "domain.xml.style-sheets";
    public static final String PROFILEPROPERTY_DOMAINXML_TOKENVALUES = "domain.xml.token-values";
    /* These properties are public interfaces, handle with care */

    public PEDomainsManager() {
        super();
    }

    // PE does not require that an admin user / password is available at start-domain time.
    // SE/SEE does require it.
    @Override
    public BitSet getDomainFlags() {
        BitSet bs = new BitSet();
        bs.set(DomainConfig.K_FLAG_START_DOMAIN_NEEDS_ADMIN_USER, false);
        return bs;
    }

    @Override
    public void validateDomain(DomainConfig domainConfig, boolean domainExists) throws DomainException {
        try {
            checkRepository(domainConfig, domainExists, domainExists);
        } catch (RepositoryException ex) {
            throw new DomainException(ex);
        }
    }

    @Override
    public void validateMasterPassword(DomainConfig domainConfig) throws DomainException {
        try {
            validateMasterPassword(domainConfig, getMasterPasswordClear(domainConfig));
        } catch (RepositoryException ex) {
            throw new DomainException(ex);
        }
    }

    @Override
    public void deleteDomain(DomainConfig domainConfig) throws DomainException {
        try {
            deleteRepository(domainConfig);
        } catch (Exception e) {
            throw new DomainException(e);
        }
    }

    @Override
    public String[] listDomains(DomainConfig domainConfig) throws DomainException {
        try {
            return listRepository(domainConfig);
        } catch (Exception e) {
            throw new DomainException(e);
        }
    }

    protected void createScripts(DomainConfig domainConfig) throws DomainException {
        final TokenValueSet tokens = PEScriptsTokens.getTokenValueSet(domainConfig);
        createStartServ(domainConfig, tokens);
        createStopServ(domainConfig, tokens);
    }

    void createStartServ(DomainConfig domainConfig, TokenValueSet tokens) throws DomainException {
        try {
            final PEFileLayout layout = getFileLayout(domainConfig);
            final File startServTemplate = layout.getStartServTemplate();
            final File startServ = layout.getStartServ();
            generateFromTemplate(tokens, startServTemplate, startServ);
        } catch (Exception e) {
            throw new DomainException(strMgr.getString("startServNotCreated"), e);
        }
    }

    void createStopServ(DomainConfig domainConfig, TokenValueSet tokens) throws DomainException {
        try {
            final PEFileLayout layout = getFileLayout(domainConfig);
            final File stopServTemplate = layout.getStopServTemplate();
            final File stopServ = layout.getStopServ();
            generateFromTemplate(tokens, stopServTemplate, stopServ);
        } catch (Exception e) {
            throw new DomainException(strMgr.getString("stopServNotCreated"), e);
        }
    }

    protected File getDomainDir(DomainConfig domainConfig) {
        return getRepositoryDir(domainConfig);
    }

    protected File getDomainRoot(DomainConfig domainConfig) {
        return getRepositoryRootDir(domainConfig);
    }

    String getDefaultInstance() {
        return PEFileLayout.DEFAULT_INSTANCE_NAME;
    }

    /**
     * Returns the domain user from the domainConfig.
     *
     * @param domainConfig that represents the domain configuration
     * @return String representing the domain user if the given map contains it, null otherwise
     */
    protected static String getDomainUser(DomainConfig domainConfig) {
        return ((String) domainConfig.get(DomainConfig.K_USER));
    }

    /**
     * Returns the domain user's password in cleartext from the domainConfig.
     *
     * @param domainConfig that represents the domain configuration
     * @return String representing the domain user password if the given map contains it, null otherwise
     */
    protected static String getDomainPasswordClear(DomainConfig domainConfig) {
        return ((String) domainConfig.get(DomainConfig.K_PASSWORD));
    }

    protected static String getMasterPasswordClear(DomainConfig domainConfig) {
        return ((String) domainConfig.get(DomainConfig.K_MASTER_PASSWORD));
    }

    protected static String getNewMasterPasswordClear(DomainConfig domainConfig) {
        return ((String) domainConfig.get(DomainConfig.K_NEW_MASTER_PASSWORD));
    }

    protected static boolean saveMasterPassword(DomainConfig domainConfig) {
        Boolean b = (Boolean) domainConfig.get(DomainConfig.K_SAVE_MASTER_PASSWORD);
        return b.booleanValue();
    }

    /**
     * Changes the master password for the domain
     */
    public void changeMasterPassword(DomainConfig config) throws DomainException {
        try {
            String oldPass = getMasterPasswordClear(config);
            String newPass = getNewMasterPasswordClear(config);

            //Change the password of the keystore alias file
            changePasswordAliasKeystorePassword(config, oldPass, newPass);

            //Change the password of the keystore and truststore
            changeSSLCertificateDatabasePassword(config, oldPass, newPass);

            //Change the password in the masterpassword file or delete the file if it is
            //not to be saved.
            changeMasterPasswordInMasterPasswordFile(config, newPass, saveMasterPassword(config));
        } catch (Exception ex) {
            throw new DomainException(strMgr.getString("masterPasswordNotChanged"), ex);
        }
    }

    @Override
    public String[] getExtraPasswordOptions(DomainConfig config) throws DomainException {
        return null;
    }
}
