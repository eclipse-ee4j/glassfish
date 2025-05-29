/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.servermgmt;

import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.net.NetUtils;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.security.Key;
import java.security.KeyStore;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.glassfish.main.jdke.security.KeyTool;

import static com.sun.enterprise.admin.servermgmt.domain.DomainConstants.KEYSTORE_FILE;
import static com.sun.enterprise.admin.servermgmt.domain.DomainConstants.TRUSTSTORE_FILE;
import static java.lang.System.Logger.Level.WARNING;

/**
 * @author kebbs
 */
public class KeystoreManager {

    private static final Logger LOG = System.getLogger(KeystoreManager.class.getName());
    private static final String CERTIFICATE_DN_PREFIX = "CN=";
    private static final String CERTIFICATE_DN_SUFFIX = ",OU=GlassFish,O=Eclipse Foundation";
    public static final String CERTIFICATE_ALIAS = "s1as";
    public static final String INSTANCE_SECURE_ADMIN_ALIAS = "glassfish-instance";
    private static final String INSTANCE_CN_SUFFIX = "-instance";

    private static final StringManager _strMgr = StringManager.getManager(KeystoreManager.class);
    private PEFileLayout fileLayout;


    protected static String getCertificateDN(RepositoryConfig cfg, final String CNSuffix) {
        String cn = getCNFromCfg(cfg);
        if (cn == null) {
            try {
                cn = NetUtils.getCanonicalHostName();
            } catch (Exception e) {
                cn = "localhost";
            }
        }
        // Use the suffix, if provided, in creating the DN (by augmenting the CN).
        // must be of form "CN=..., OU=..."
        return CERTIFICATE_DN_PREFIX + cn + (CNSuffix != null ? CNSuffix : "") + CERTIFICATE_DN_SUFFIX;
    }

    protected PEFileLayout getFileLayout(RepositoryConfig config) {
        if (fileLayout == null) {
            fileLayout = new PEFileLayout(config);
        }

        return fileLayout;
    }

    /**
     * Create the default SSL key store using keytool to generate a self signed certificate.
     * @param keyStore
     *
     * @param config
     * @param masterPassword
     * @throws DomainException
     */
    protected void createKeyStore(File keyStore, RepositoryConfig config, String masterPassword) throws DomainException {
        // Generate a new self signed certificate with s1as as the alias
        // Create the default self signed cert
        final String dasCertDN = getDASCertDN(config);
        System.out.println(_strMgr.getString("CertificateDN", dasCertDN));
        try {
            final KeyTool keyTool = new KeyTool(keyStore, masterPassword.toCharArray());
            keyTool.generateKeyPair(CERTIFICATE_ALIAS, dasCertDN, "RSA", 3650);

            // Generate a new self signed certificate with glassfish-instance as the alias
            // Create the default self-signed cert for instances to use for SSL auth.
            final String instanceCertDN = getInstanceCertDN(config);
            keyTool.generateKeyPair(INSTANCE_SECURE_ADMIN_ALIAS, instanceCertDN, "RSA", 3650);
        } catch (IOException e) {
            throw new DomainException(_strMgr.getString("SomeProblemWithKeytool", keyStore), e);
        }
    }


    protected void copyCertificatesToTrustStore(File configRoot, DomainConfig config, String masterPassword)
        throws DomainException {
        final File keyStore = new File(configRoot, KEYSTORE_FILE);
        final File trustStore = new File(configRoot, TRUSTSTORE_FILE);
        final KeyTool keyTool = new KeyTool(keyStore, masterPassword.toCharArray());
        try {
            keyTool.copyCertificate(CERTIFICATE_ALIAS, trustStore);
            keyTool.copyCertificate(INSTANCE_SECURE_ADMIN_ALIAS, trustStore);
        } catch (IOException e) {
            throw new DomainException(_strMgr.getString("SomeProblemWithKeytool", keyStore), e);
        }
    }

    /**
     * Changes the keystore password
     *
     * @param oldPassword the old keystore password
     * @param newPassword the new keystore password
     * @param keyStore the keystore whose password is to be changed.
     * @throws DomainException
     */
    protected void changeKeystorePassword(String oldPassword, String newPassword, File keyStore) throws DomainException {
        if (oldPassword.equals(newPassword)) {
            return;
        }
        try {
            final KeyTool keyTool = new KeyTool(keyStore, oldPassword.toCharArray());
            keyTool.changeKeyStorePassword(newPassword.toCharArray());
        } catch (IOException e) {
            throw new DomainException(_strMgr.getString("keyStorePasswordNotChanged", keyStore), e);
        }
    }

    /**
     * Changes the key password for the default cert whose alias is s1as. The assumption here is that the keystore password
     * is not the same as the key password. This is due to the fact that the keystore password should first be changed
     * followed next by the key password. The end result is that the keystore and s1as key both have the same passwords.
     * This function will tolerate deletion of the s1as alias, but it will not tolerate changing the s1as key from something
     * other than the database password.
     *
     * @param config
     * @param storePassword the keystore password
     * @param oldKeyPassword the old password for the s1as alias
     * @param newKeyPassword the new password for the s1as alias
     * @throws DomainException
     */
    protected void changeKeyPasswords(RepositoryConfig config, String storePassword, String oldKeyPassword,
        String newKeyPassword) throws DomainException {
        if (storePassword.equals(oldKeyPassword) || oldKeyPassword.equals(newKeyPassword)) {
            return;
        }
        final PEFileLayout layout = getFileLayout(config);
        final File keystore = layout.getKeyStore();
        try {
            KeyStore keyStore = KeyStore.getInstance(keystore, storePassword.toCharArray());
            List<String> aliases = Collections.list(keyStore.aliases());
            List<String> keyAliases = new ArrayList<>();
            for (String alias : aliases) {
                Key key;
                try {
                    key = keyStore.getKey(alias, oldKeyPassword.toCharArray());
                } catch (UnrecoverableKeyException e) {
                    LOG.log(WARNING,
                        "Key entry with alias {0} in a key store {1} could not be recovered with provided key password.",
                        alias, keystore);
                    continue;
                }
                if (key != null) {
                    keyAliases.add(alias);
                }
            }
            KeyTool keyTool = new KeyTool(keystore, storePassword.toCharArray());
            for (String alias : keyAliases) {
                keyTool.changeKeyPassword(alias, oldKeyPassword.toCharArray(), newKeyPassword.toCharArray());
            }
        } catch (Exception e) {
            throw new DomainException(_strMgr.getString("s1asKeyPasswordNotChanged", keystore), e);
        }
    }

    /**
     * Changes the password of the keystore, truststore and the key password of the s1as alias. It is expected that the key
     * / truststores may not exist. This is due to the fact that the user may have deleted them and wishes to set up their
     * own key/truststore
     *
     * @param config
     * @param oldPassword
     * @param newPassword
     */
    protected void changeSSLCertificateDatabasePassword(RepositoryConfig config, String oldPassword, String newPassword) throws DomainException {
        final PEFileLayout layout = getFileLayout(config);
        File keystore = layout.getKeyStore();
        File truststore = layout.getTrustStore();

        if (keystore.exists()) {
            // Change the password on the keystore
            changeKeystorePassword(oldPassword, newPassword, keystore);
            changeKeyPasswords(config, newPassword, oldPassword, newPassword);
        }

        if (truststore.exists()) {
            // Change the password on the truststore
            changeKeystorePassword(oldPassword, newPassword, truststore);
        }
    }

    protected void chmod(String args, File file) throws IOException {
        if (OS.isUNIX()) {
            // args and file should never be null.
            if (args == null || file == null) {
                throw new IOException(_strMgr.getString("nullArg"));
            }
            if (!file.exists()) {
                throw new IOException(_strMgr.getString("fileNotFound"));
            }

            // " +" regular expression for 1 or more spaces
            final String[] argsString = args.split(" +");
            List<String> cmdList = new ArrayList<>();
            cmdList.add("/bin/chmod");
            cmdList.addAll(Arrays.asList(argsString));
            cmdList.add(file.getAbsolutePath());
            new ProcessBuilder(cmdList).start();
        }
    }

    public static String getDASCertDN(final RepositoryConfig cfg) {
        return getCertificateDN(cfg, null);
    }

    public static String getInstanceCertDN(final RepositoryConfig cfg) {
        return getCertificateDN(cfg, INSTANCE_CN_SUFFIX);
    }

    private static String getCNFromCfg(RepositoryConfig cfg) {
        String option = (String) cfg.get(DomainConfig.KEYTOOLOPTIONS);
        if (option == null || option.length() == 0) {
            return null;
        }

        String value = getCNFromOption(option);
        if (value == null || value.length() == 0) {
            return null;
        }

        return value;
    }

    private static String getCNFromOption(String option) {
        return getValueFromOptionForName(option, "CN", true);
    }

    /**
     * Returns CN if valid and non-blank. Returns null otherwise.
     *
     * @param option
     * @param name String representing name of the keytooloption
     * @param ignoreNameCase flag indicating if the comparison should be case insensitive
     * @return
     */
    private static String getValueFromOptionForName(String option, String name, boolean ignoreNameCase) {
        // Option is not null at this point
        Pattern p = Pattern.compile(":");
        String[] pairs = p.split(option);
        for (String pair : pairs) {
            p = Pattern.compile("=");
            String[] nv = p.split(pair);
            String n = nv[0].trim();
            String v = nv[1].trim();
            boolean found = ignoreNameCase ? n.equalsIgnoreCase(name) : n.equals(name);
            if (found) {
                return v;
            }
        }

        return null;
    }
}
