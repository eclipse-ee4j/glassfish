/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import static com.sun.enterprise.admin.servermgmt.SLogger.BAD_DELETE_TEMP_CERT_FILE;
import static com.sun.enterprise.admin.servermgmt.SLogger.UNHANDLED_EXCEPTION;
import static com.sun.enterprise.admin.servermgmt.SLogger.getLogger;
import static com.sun.enterprise.admin.servermgmt.domain.DomainConstants.KEYSTORE_FILE;
import static com.sun.enterprise.admin.servermgmt.domain.DomainConstants.TRUSTSTORE_FILE;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.universal.process.ProcessUtils;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.net.NetUtils;

/**
 * @author kebbs
 */
public class KeystoreManager {

    private static final String KEYTOOL_CMD;
    private static final String KEYTOOL_EXE_NAME = OS.isWindows() ? "keytool.exe" : "keytool";
    private static String CERTIFICATE_DN_PREFIX = "CN=";
    private static String CERTIFICATE_DN_SUFFIX = ",OU=GlassFish,O=Eclipse.org Foundation Inc,L=Ottawa,ST=Ontario,C=CA";
    public static final String CERTIFICATE_ALIAS = "s1as";
    public static final String INSTANCE_SECURE_ADMIN_ALIAS = "glassfish-instance";
    public static final String DEFAULT_MASTER_PASSWORD = "changeit";
    private static final String SKID_EXTENSION_SYSTEM_PROPERTY = "-J-Dsun.security.internal.keytool.skid";
    private static final String INSTANCE_CN_SUFFIX = "-instance";

    private static final StringManager _strMgr = StringManager.getManager(KeystoreManager.class);
    protected PEFileLayout _fileLayout;


    static {
        // Byron Nevins, July 2011
        String nonFinalKeyTool = KEYTOOL_EXE_NAME; // at the end we set the final
        String propName = SystemPropertyConstants.JAVA_ROOT_PROPERTY;
        String javaroot = new ASenvPropertyReader().getProps().get(propName);
        File keyToolBin = new File(new File(javaroot, "bin"), KEYTOOL_EXE_NAME);

        if (keyToolBin.canExecute()) {
            nonFinalKeyTool = SmartFile.sanitize(keyToolBin.getPath());
        } else {
            // Can't find it in a JDK. Maybe it is in the PATH?
            keyToolBin = ProcessUtils.getExe(KEYTOOL_EXE_NAME);

            if (keyToolBin != null && keyToolBin.canExecute()) {
                nonFinalKeyTool = keyToolBin.getPath();
            }
        }

        KEYTOOL_CMD = nonFinalKeyTool;
    }

    protected static class KeytoolExecutor extends ProcessManager {

        public KeytoolExecutor(String[] args, int timeoutInSeconds) {
            super(args);
            setTimeoutMsec(timeoutInSeconds * 1000);
            addKeytoolCommand();
        }

        public KeytoolExecutor(String[] args, int timeoutInSeconds, String[] inputLines) {
            super(args);
            setTimeoutMsec(timeoutInSeconds * 1000);
            setStdinLines(Arrays.asList(inputLines));
            addKeytoolCommand();
        }

        // We must override this message so that the stdout appears in the exec exception.
        // Keytool seems to output errors to stdout.
        protected String getExceptionMessage() {
            return getStdout() + " " + getStderr();
        }

        private void addKeytoolCommand() {
            String[] mCmdStrings = builder.command().toArray(new String[0]);
            if (!mCmdStrings[0].equals(KEYTOOL_CMD)) {
                String[] newArgs = new String[mCmdStrings.length + 1];
                newArgs[0] = KEYTOOL_CMD;
                System.arraycopy(mCmdStrings, 0, newArgs, 1, mCmdStrings.length);
                mCmdStrings = newArgs;
                builder.command(Arrays.asList(mCmdStrings));
            }
        }

        public void execute(String keystoreErrorMsg, File keystoreName) throws RepositoryException {
            try {
                if (super.execute() != 0) {
                    throw new RepositoryException(
                            _strMgr.getString(keystoreErrorMsg, keystoreName) + getStderr() + " " + getStdout());
                }
            } catch (ProcessManagerException ex) {
                throw new RepositoryException(
                        _strMgr.getString(keystoreErrorMsg, keystoreName) + getStderr() + " " + getStdout(), ex);
            }
        }
    }



    /**
     * Creates a new instance of RepositoryManager
     */
    public KeystoreManager() {
    }

    protected static String getCertificateDN(RepositoryConfig cfg, final String CNSuffix) {
        String cn = getCNFromCfg(cfg);
        if (cn == null) {
            try {
                cn = NetUtils.getCanonicalHostName();
            } catch (Exception e) {
                cn = "localhost";
            }
        }
        /*
         * Use the suffix, if provided, in creating the DN (by augmenting the CN).
         */
        String x509DistinguishedName = CERTIFICATE_DN_PREFIX + cn + (CNSuffix != null ? CNSuffix : "") + CERTIFICATE_DN_SUFFIX;
        return x509DistinguishedName; // must be of form "CN=..., OU=..."
    }

    protected PEFileLayout getFileLayout(RepositoryConfig config) {
        if (_fileLayout == null) {
            _fileLayout = new PEFileLayout(config);
        }

        return _fileLayout;
    }

    /**
     * Create the default SSL key store using keytool to generate a self signed certificate.
     *
     * @param config
     * @param masterPassword
     * @throws RepositoryException
     */
    protected void createKeyStore(File keystore, RepositoryConfig config, String masterPassword) throws RepositoryException {
        // Generate a new self signed certificate with s1as as the alias
        // Create the default self signed cert
        final String dasCertDN = getDASCertDN(config);
        System.out.println(_strMgr.getString("CertificateDN", dasCertDN));
        addSelfSignedCertToKeyStore(keystore, CERTIFICATE_ALIAS, masterPassword, dasCertDN);

        // Generate a new self signed certificate with glassfish-instance as the alias
        // Create the default self-signed cert for instances to use for SSL auth.
        final String instanceCertDN = getInstanceCertDN(config);
        System.out.println(_strMgr.getString("CertificateDN", instanceCertDN));
        addSelfSignedCertToKeyStore(keystore, INSTANCE_SECURE_ADMIN_ALIAS, masterPassword, instanceCertDN);
    }

    private void addSelfSignedCertToKeyStore(final File keystore, final String alias, final String masterPassword, final String dn) throws RepositoryException {
        final String[] keytoolCmd = {
                "-genkey",
                "-keyalg", "RSA",
                "-keystore", keystore.getAbsolutePath(),
                "-alias", alias,
                "-dname", dn,
                "-validity", "3650",
                "-keypass", masterPassword,
                "-storepass", masterPassword,
                "-storetype", "JKS",
                SKID_EXTENSION_SYSTEM_PROPERTY };

        new KeytoolExecutor(keytoolCmd, 60).execute("keystoreNotCreated", keystore);
    }

    protected void copyCertificatesToTrustStore(File configRoot, DomainConfig config, String masterPassword) throws DomainException {
        try {
            copyCertificateFromKeyStoreToTrustStore(configRoot, CERTIFICATE_ALIAS, masterPassword);
            copyCertificateFromKeyStoreToTrustStore(configRoot, INSTANCE_SECURE_ADMIN_ALIAS, masterPassword);
        } catch (RepositoryException re) {
            throw new DomainException(_strMgr.getString("SomeProblemWithKeytool", re.getMessage()));
        }
    }

    private void copyCertificateFromKeyStoreToTrustStore(final File configRoot, final String alias, final String masterPassword) throws RepositoryException {
        File keystore = new File(configRoot, KEYSTORE_FILE);
        File truststore = new File(configRoot, TRUSTSTORE_FILE);
        File certFile = null;
        String[] input = { masterPassword };
        String[] keytoolCmd = null;
        KeytoolExecutor keytoolExecutor = null;

        try {
            // Export the newly created certificate from the keystore
            certFile = new File(configRoot, alias + ".cer");
            keytoolCmd = new String[] {
                    "-export",
                    "-keystore", keystore.getAbsolutePath(),
                    "-alias", alias,
                    "-file", certFile.getAbsolutePath(), };

            keytoolExecutor = new KeytoolExecutor(keytoolCmd, 30, input);
            keytoolExecutor.execute("trustStoreNotCreated", truststore);

            // Import the newly created certificate into the truststore
            keytoolCmd = new String[] {
                    "-import", "-noprompt",
                    "-keystore", truststore.getAbsolutePath(),
                    "-alias", alias,
                    "-file", certFile.getAbsolutePath(), };

            keytoolExecutor = new KeytoolExecutor(keytoolCmd, 30, input);
            keytoolExecutor.execute("trustStoreNotCreated", truststore);

        } finally {
            if (certFile != null) {
                if (!certFile.delete()) {
                    getLogger().log(WARNING, BAD_DELETE_TEMP_CERT_FILE, certFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Changes the keystore password
     *
     * @param oldPassword the old keystore password
     * @param newPassword the new keystore password
     * @param keystore the keystore whose password is to be changed.
     * @throws RepositoryException
     */
    protected void changeKeystorePassword(String oldPassword, String newPassword, File keystore) throws RepositoryException {
        if (!oldPassword.equals(newPassword)) {
            // Change truststore password from the default
            String[] keytoolCmd = {
                    "-storepasswd",
                    "-keystore", keystore.getAbsolutePath(), };

            KeytoolExecutor keytoolExecutor = new KeytoolExecutor(keytoolCmd, 30, new String[] { oldPassword, newPassword, newPassword });
            keytoolExecutor.execute("keyStorePasswordNotChanged", keystore);
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
     * @throws RepositoryException
     */
    protected void changeS1ASAliasPassword(RepositoryConfig config, String storePassword, String oldKeyPassword, String newKeyPassword) throws RepositoryException {
        if (!storePassword.equals(oldKeyPassword) && !oldKeyPassword.equals(newKeyPassword)) {
            final PEFileLayout layout = getFileLayout(config);
            final File keystore = layout.getKeyStore();

            // First see if the alias exists. The user could have deleted it. Any failure in the
            // command indicates that the alias does not exist, so we return without error.
            String keyStoreType = System.getProperty("javax.net.ssl.keyStoreType");
            if (keyStoreType == null) {
                keyStoreType = KeyStore.getDefaultType();
            }

            // Add code to change all the aliases that exist rather then change s1as only
            List<String> aliases = new ArrayList<>();
            FileInputStream is = null;
            try {
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                is = new FileInputStream(keystore);
                keyStore.load(is, storePassword.toCharArray());
                Enumeration<String> all = keyStore.aliases();
                while (all.hasMoreElements()) {
                    aliases.add(all.nextElement());
                }
            } catch (Exception e) {
                aliases.add(CERTIFICATE_ALIAS);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        getLogger().log(SEVERE, UNHANDLED_EXCEPTION, ex);
                    }
                }
            }

            String[] keytoolCmd = {
                    "-list",
                    "-keystore", keystore.getAbsolutePath(),
                    "-alias", CERTIFICATE_ALIAS, };

            KeytoolExecutor keytoolExecutor = new KeytoolExecutor(keytoolCmd, 30, new String[] { storePassword });
            try {
                keytoolExecutor.execute("s1asKeyPasswordNotChanged", keystore);
            } catch (RepositoryException ex) {
                return;
            }

            // Change truststore password from the default
            for (String alias : aliases) {
                keytoolCmd = new String[] {
                        "-keypasswd",
                        "-keystore", keystore.getAbsolutePath(),
                        "-alias", alias, };

                keytoolExecutor = new KeytoolExecutor(keytoolCmd, 30, new String[] { storePassword, oldKeyPassword, newKeyPassword, newKeyPassword });
                keytoolExecutor.execute("s1asKeyPasswordNotChanged", keystore);
            }
        }
    }

    /**
     * Changes the password of the keystore, truststore and the key password of the s1as alias. It is expected that the key
     * / truststores may not exist. This is due to the fact that the user may have deleted them and wishes to set up their
     * own key/truststore
     *
     * @param config
     * @param storePassword
     * @param oldKeyPassword
     * @param newKeyPassword
     */
    protected void changeSSLCertificateDatabasePassword(RepositoryConfig config, String oldPassword, String newPassword) throws RepositoryException {
        final PEFileLayout layout = getFileLayout(config);
        File keystore = layout.getKeyStore();
        File truststore = layout.getTrustStore();

        if (keystore.exists()) {
            // Change the password on the keystore
            changeKeystorePassword(oldPassword, newPassword, keystore);

            // Change the s1as alias password in the keystore...
            //
            // The assumption here is that the keystore password is not the same as the key password.
            // This is due to the fact that the keystore password should first be changed followed next
            // by the key password. The end result is that the keystore and s1as key both have
            // the same passwords. This function will tolerate deletion of the s1as alias, but
            // it will not tolerate changing the s1as key from something other than the
            // database password.
            try {
                changeS1ASAliasPassword(config, newPassword, oldPassword, newPassword);
            } catch (Exception ex) {
                // For now we eat all exceptions and dump to the log if the password
                // alias could not be changed.
                getLogger().log(SEVERE, UNHANDLED_EXCEPTION, ex);
            }
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

    private static String getCNFromOption(String option) {
        return getValueFromOptionForName(option, "CN", true);
    }
}
