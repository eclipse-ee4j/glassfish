/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2021 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.SecureAdmin;
import com.sun.enterprise.config.serverbeans.SecureAdminHelper.SecureAdminCommandException;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.security.SecurityUpgradeService;
import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.util.net.NetUtils;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.glassfish.config.support.GrizzlyConfigSchemaMigrator;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.Ssl;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.RetryableException;
import org.jvnet.hk2.config.Transaction;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Upgrades older config to current.
 *
 * In 3.1.2 GlassFish uses SSL for DAS-to-instance traffic regardless of whether the user enables secure admin. This means that
 * the upgrade must always:
 * <ol>
 * <li>Create the secure-admin element.
 * <li>Add the glassfish-instance private key to the keystore and the corresponding self-signed cert to the truststore.
 * <li>Add secure-admin-principal children to secure-admin, one for alias s1as and one for glassfish-instance.
 * <li>Add the Grizzly config for the non-DAS admin listener configurations to use port unification and redirection (and SSL).
 * <li>If it seems the user has configured the old domain to use secure admin traffic then run the enable-secure-admin command.
 * </ol>
 *
 * @author Tim Quinn
 */
@Service
public class SecureAdminConfigUpgrade extends SecureAdminUpgradeHelper implements ConfigurationUpgrade, PostConstruct {

    private static final Logger LOG = Logger.getLogger(SecureAdminConfigUpgrade.class.getName());

    private final static String ADMIN_LISTENER_NAME = "admin-listener";

    /**
     * Constants used for creating a missing network-listener during upgrade.
     * Ideally this will be handled in the grizzly upgrade code.
     */
    private final static String ASADMIN_LISTENER_PORT = "${ASADMIN_LISTENER_PORT}";
    private final static String ASADMIN_LISTENER_TRANSPORT = "tcp";
    private final static String ASADMIN_LISTENER_THREADPOOL = "http-thread-pool";

    private final static String ASADMIN_VS_NAME = "__asadmin";

    private static final String CERTIFICATE_DN_PREFIX = "CN=";
    private static final String CERTIFICATE_DN_SUFFIX = ",OU=GlassFish,O=Eclipse Foundation AISBL,L=Brussels,C=BE";
    private static final String INSTANCE_CN_SUFFIX = "-instance";

    // Thanks to Jerome for suggesting this injection to make sure the
    // Grizzly migration runs before this migration
    @Inject
    private GrizzlyConfigSchemaMigrator grizzlyMigrator;

    @Inject
    private SecurityUpgradeService securityUpgradeService;

    @Inject
    private Configs configs;

    @Inject
    private ServerEnvironment serverEnv;

    private final Map<String, Config> writableConfigs = new HashMap<>();

    @Override
    public void postConstruct() {
        String stage = null;
        try {
            stage = "adding admin-listener to non-DAS configuration";
            ensureNonDASConfigsHaveAdminNetworkListener();
            LOG.log(Level.INFO, "Added admin-listener network listeners to non-DAS configurations");

            stage = "upgrading config for secure DAS-to-instance admin traffic";
            setupNewDefaultConfig();

            /*
             * See if we need to set up secure admin during the upgrade.
             */
            if (requiresSecureAdmin()) {
                final EnableSecureAdminCommand enableSecureAdminCommand = habitat.getService(EnableSecureAdminCommand.class);
                stage = "upgrading secure admin set-up";
                try {
                    enableSecureAdminCommand.run();
                    LOG.log(Level.INFO, "Upgraded secure admin set-up");
                } catch (SecureAdminCommandException ex) {
                    LOG.log(Level.INFO, "Attempt to upgrade secure admin set-up failed", ex);
                    throw ex;
                }
            } else {
                LOG.log(Level.INFO, "No secure admin set-up was detected in the original configuration so no upgrade of it was needed");
            }
            commit();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error " + stage, ex);
            rollback();
        }
    }

    private void setupNewDefaultConfig() throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException,
        UnrecoverableKeyException, ProcessManagerException, TransactionFailure, RetryableException, PropertyVetoException {
        /*
         * In 3.1.2 the default config has a secure-admin element with child secure-admin-principal
         * elements for the s1as and glassfish-instance aliases.  The keystore
         * contains private keys for s1as and glassfish-instance (new in 3.x)
         * and the truststore contains the self-signed certs for those two
         * aliases.
         */
        ensureKeyPairForInstanceAlias();

        /*
         * Add the secure-admin-principal children below secure-admin, one for
         * the DAS alias and one for the instance alias.
         */
        ensureSecureAdminReady();
        prepareDASConfig();
        //        prepareNonDASConfigs();
    }

    private boolean requiresSecureAdmin() {
        return isOriginalAdminSecured() || securityUpgradeService.requiresSecureAdmin();
    }

    private void prepareDASConfig() throws TransactionFailure, PropertyVetoException {
        final Config dasConfig = writableConfig(configs.getConfigByName(DAS_CONFIG_NAME));
        final NetworkConfig nc = dasConfig.getNetworkConfig();
        final NetworkListener nl_w = transaction().enroll(nc.getNetworkListener(SecureAdminCommand.ADMIN_LISTENER_NAME));
        nl_w.setProtocol(SecureAdminCommand.ADMIN_LISTENER_NAME);
    }

    //    private void prepareNonDASConfigs() throws TransactionFailure, PropertyVetoException {
    //        for (Config c : configs.getConfig()) {
    //            if (c.getName().equals(DAS_CONFIG_NAME)) {
    //                continue;
    //            }
    //            ensureConfigReady(c, SecureAdminCommand.PORT_UNIF_PROTOCOL_NAME);
    //        }
    //    }

    private void ensureConfigReady(final Config c, final String adminListenerProtocol) throws TransactionFailure, PropertyVetoException {
        final NetworkConfig nc = c.getNetworkConfig();
        final NetworkListener nl = nc.getNetworkListener(SecureAdminCommand.ADMIN_LISTENER_NAME);
        if (nl != null) {
            return;
        }

        /*
         * Create an admin-listener for this configuration.
         */
        final Config config_w = writableConfig(c);

        createAdminNetworkListener(transaction(), nc, adminListenerProtocol);
        createAdminVirtualServer(transaction(), config_w);
    }

    private Config writableConfig(final Config c) throws TransactionFailure {
        Config result = writableConfigs.get(c.getName());
        if (result == null) {
            result = transaction().enroll(c);
            writableConfigs.put(c.getName(), result);
        }
        return result;
    }

    private void ensureNonDASConfigsHaveAdminNetworkListener() throws TransactionFailure, PropertyVetoException {

        for (Config c : configs.getConfig()) {
            if (c.getName().equals(DAS_CONFIG_NAME)) {
                continue;
            }
            ensureConfigReady(c, SecureAdminCommand.PORT_UNIF_PROTOCOL_NAME);
        }
    }

    private NetworkListener createAdminNetworkListener(final Transaction t, final NetworkConfig nc, final String adminListenerProtocolName)
        throws TransactionFailure {
        final NetworkListeners nls_w = t.enroll(nc.getNetworkListeners());
        final NetworkListener nl_w = nls_w.createChild(NetworkListener.class);
        nls_w.getNetworkListener().add(nl_w);
        nl_w.setName(ADMIN_LISTENER_NAME);
        nl_w.setProtocol(adminListenerProtocolName);
        nl_w.setPort(ASADMIN_LISTENER_PORT);
        nl_w.setTransport(ASADMIN_LISTENER_TRANSPORT);
        nl_w.setThreadPool(ASADMIN_LISTENER_THREADPOOL);
        return nl_w;
    }

    private VirtualServer createAdminVirtualServer(final Transaction t, final Config config_w)
        throws TransactionFailure, PropertyVetoException {
        final HttpService hs_w = t.enroll(config_w.getHttpService());
        final VirtualServer vs_w = hs_w.createChild(VirtualServer.class);
        hs_w.getVirtualServer().add(vs_w);
        vs_w.setId(ASADMIN_VS_NAME);
        vs_w.setNetworkListeners(ADMIN_LISTENER_NAME);
        return vs_w;
    }

    private boolean isOriginalAdminSecured() {
        /*
         * The Grizzly conversion has already occurred.  So look for
         *
         * <server-config>
         *   <network-config>
         *     <protocols>
         *       <protocol name="admin-listener">
         *         <ssl ...>
         *
         */
        final Config serverConfig;
        final NetworkConfig nc;
        final Protocol p;
        final Ssl ssl;
        if ((serverConfig = configs.getConfigByName(SecureAdminUpgradeHelper.DAS_CONFIG_NAME)) == null) {
            return false;
        }

        if ((nc = serverConfig.getNetworkConfig()) == null) {
            return false;
        }

        if ((p = nc.findProtocol(ADMIN_LISTENER_NAME)) == null) {
            return false;
        }

        if ((ssl = p.getSsl()) == null) {
            return false;
        }
        return true;
    }

    private void ensureKeyPairForInstanceAlias() throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException,
        UnrecoverableKeyException, ProcessManagerException {
        // No need to add glassfish-instance to the keystore if it already exists.
        final KeyStore ks = sslUtils().getKeyStore();
        if (ks.containsAlias(SecureAdmin.DEFAULT_INSTANCE_ALIAS)) {
            return;
        }

        // This is ugly but effective.  We need to add a new private key to
        // the keystore and a new self-signed cert to the truststore.  To do so
        // we run keytool commands to change the on-disk stores, then we
        // cause the in-memory copies to reload.
        final File keyStoreFile = serverEnv.getJKS();
        final File trustStoreFile = new File(serverEnv.getConfigDirPath(), "cacerts.jks");
        final String pw = masterPassword();
        {
            ProcessManager pm = new ProcessManager(new String[] {"keytool", "-genkey", "-keyalg", "RSA", "-keystore",
                keyStoreFile.getAbsolutePath(), "-alias", SecureAdmin.DEFAULT_INSTANCE_ALIAS, "-dname",
                getCertificateDN(), "-validity", "3650", "-keypass", pw, "-storepass", pw,});
            int exitValue = pm.execute();
            if (exitValue != 0) {
                final String err = pm.getStdout();
                throw new RuntimeException(err);
            }
        }
        final File tempCertFile = new File(serverEnv.getConfigDirPath(), "temp.cer");
        tempCertFile.deleteOnExit();
        {
            ProcessManager pm = new ProcessManager(new String[] {"keytool", "-exportcert", "-keystore",
                keyStoreFile.getAbsolutePath(), "-alias", SecureAdmin.DEFAULT_INSTANCE_ALIAS, "-keypass", pw,
                "-storepass", pw, "-file", tempCertFile.getAbsolutePath()});
            int exitValue = pm.execute();
            if (exitValue != 0) {
                throw new RuntimeException(pm.getStderr());
            }
        }
        {
            if (!trustStoreFile.exists()) {
                createKeyStoreFile(trustStoreFile, pw);
            }
            ProcessManager pm = new ProcessManager(new String[] {"keytool", "-importcert", "-noprompt", "-trustcacerts",
                "-storepass", pw, "-keypass", pw, "-keystore", trustStoreFile.getAbsolutePath(), "-file",
                tempCertFile.getAbsolutePath(), "-alias", SecureAdmin.DEFAULT_INSTANCE_ALIAS});
            int exitValue = pm.execute();
            if (!tempCertFile.delete()) {
                LOG.log(Level.FINE, "Unable to delete temp file {0}; continuing", tempCertFile.getAbsolutePath());
            }
            if (exitValue != 0) {
                throw new RuntimeException(pm.getStderr());
            }
        }

        // Reload the keystore and truststore from disk.
        reload(sslUtils().getKeyStore(), keyStoreFile, pw);
        reload(sslUtils().getTrustStore(), serverEnv.getTrustStore(), pw);
    }

    private void createKeyStoreFile(final File trustStoreFile, final String pw) {
        try {
            KeyStore cacerts = KeyStore.getInstance("JKS");
            cacerts.load(null, pw.toCharArray());
            cacerts.store(new FileOutputStream(trustStoreFile), pw.toCharArray());
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void reload(final KeyStore keystore, final File keystoreFile, final String pw)
        throws FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException {

        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(keystoreFile));
            keystore.load(is, pw.toCharArray());
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private String masterPassword()
        throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        //        /*
        //         * Crude way to get the master password of the keystore.
        //         */
        //        if (masterPassword == null) {
        //            masterPassword = habitat.getService(MasterPassword.class);
        //        }
        //        return masterPassword.getMasterPasswordAdapter().getPasswordForAlias("s1as");
        String masterPW = "changeit";
        final String pwFileArg = startupArg("-passwordfile");
        if (pwFileArg != null) {
            final String masterPWFromPWFile = pwProps(pwFileArg).getProperty("AS_ADMIN_MASTERPASSWORD");
            if (masterPWFromPWFile != null) {
                masterPW = masterPWFromPWFile;
            }
        }
        return masterPW;
    }

    private Properties pwProps(final String pwFilePath) throws IOException {
        Properties result = new Properties();
        try (InputStream is = new BufferedInputStream(new FileInputStream(pwFilePath))) {
            result.load(is);
        }
        return result;
    }

    private String getCertificateDN() throws UnknownHostException {
        String cn;
        try {
            cn = NetUtils.getCanonicalHostName();
        } catch (Exception e) {
            cn = "localhost";
        }
        /*
         * Use the suffix, if provided, in creating the DN (by augmenting
         * the CN).
         */
        String x509DistinguishedName = CERTIFICATE_DN_PREFIX + cn + INSTANCE_CN_SUFFIX + CERTIFICATE_DN_SUFFIX;
        return x509DistinguishedName; //must be of form "CN=..., OU=..."
    }
}
