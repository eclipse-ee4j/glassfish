/*
 * Copyright (c) 2025, 2026 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security;

import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.JaccProvider;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.security.store.PasswordAdapter;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.security.common.MasterPassword;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_FILENAME_DEFAULT;
import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_FILENAME_LEGACY;
import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_TYPE_DEFAULT;
import static com.sun.enterprise.util.SystemPropertyConstants.MASTER_PASSWORD_FILENAME;
import static com.sun.enterprise.util.SystemPropertyConstants.MASTER_PASSWORD_FILENAME_LEGACY;
import static com.sun.enterprise.util.SystemPropertyConstants.MASTER_PASSWORD_PASSWORD;
import static com.sun.enterprise.util.SystemPropertyConstants.TRUSTSTORE_FILENAME_DEFAULT;
import static com.sun.enterprise.util.SystemPropertyConstants.TRUSTSTORE_FILENAME_LEGACY;

/**
 * The only thing that needs to added Extra for SecurityService migration is the addition of the new JACC provider. This would be
 * required when migrating from V2, for V3-Prelude it is already present.
 *
 * The rest of the security related upgrade is handled implicitly by the actions of the upgrade service itself.
 *
 */

@Service
public class SecurityUpgradeService implements ConfigurationUpgrade, PostConstruct {

    @Inject
    Configs configs;

    @Inject
    ServerEnvironment env;

    @Inject
    @Optional
    MasterPassword masterPasswordProvider;

    private static final String DIR_GENERATED_POLICY = "generated" + File.separator + "policy";
    private static final String DIR_CONFIG = "config";
    private static final String JKS = ".jks";
    private static final String NSS = ".db";

    // Legacy (7.0.x and older) domain-passwords store file name that needs to be converted to PKCS12.
    private static final String LEGACY_DOMAIN_PASSWORDS = "domain-passwords";

    private static final String TYPE_JKS = "JKS";
    private static final String TYPE_JCEKS = "JCEKS";
    private static final String BAK_SUFFIX = ".bak";

    private static final String JDBC_REALM_CLASSNAME = "com.sun.enterprise.security.ee.auth.realm.jdbc.JDBCRealm";
    public static final String PARAM_DIGEST_ALGORITHM = "digest-algorithm";
    private static final Logger _logger = SecurityLoggerInfo.getLogger();

    @Override
    public void postConstruct() {
        for (Config config : configs.getConfig()) {
            SecurityService service = config.getSecurityService();
            if (service != null) {
                upgradeJACCProvider(service);
            }
        }

        //Clear up the old policy files for applications
        String instanceRoot = env.getInstanceRoot().getAbsolutePath();
        File genPolicyDir = new File(instanceRoot, DIR_GENERATED_POLICY);
        if (genPolicyDir != null) {
            File[] applicationDirs = genPolicyDir.listFiles();
            if (applicationDirs != null) {
                for (File policyDir : applicationDirs) {
                    deleteFile(policyDir);
                }
            }
        }

        //Update an existing JDBC realm-Change the digest algorithm to MD5 if none exists
        //Since the default algorithm is SHA-256 in v3.1, but was MD5 prior to 3.1

        for (Config config : configs.getConfig()) {
            SecurityService service = config.getSecurityService();
            List<AuthRealm> authRealms = service.getAuthRealm();

            try {
                for (AuthRealm authRealm : authRealms) {
                    if (JDBC_REALM_CLASSNAME.equals(authRealm.getClassname())) {
                        Property digestAlgoProp = authRealm.getProperty(PARAM_DIGEST_ALGORITHM);
                        if (digestAlgoProp != null) {
                            String digestAlgo = digestAlgoProp.getValue();
                            if (digestAlgo == null || digestAlgo.isEmpty()) {
                                digestAlgoProp.setValue("MD5");
                            }
                        } else {
                            ConfigSupport.apply(new SingleConfigCode<AuthRealm>() {
                                @Override
                                public Object run(AuthRealm updatedAuthRealm) throws PropertyVetoException, TransactionFailure {
                                    Property prop1 = updatedAuthRealm.createChild(Property.class);
                                    prop1.setName(PARAM_DIGEST_ALGORITHM);
                                    prop1.setValue("MD5");
                                    updatedAuthRealm.getProperty().add(prop1);
                                    return null;
                                }
                            }, authRealm);
                        }
                    }
                }
            } catch (PropertyVetoException pve) {
                _logger.log(Level.SEVERE, SecurityLoggerInfo.securityUpgradeServiceException, pve);
                throw new RuntimeException(pve);
            } catch (TransactionFailure tf) {
                _logger.log(Level.SEVERE, SecurityLoggerInfo.securityUpgradeServiceException, tf);
                throw new RuntimeException(tf);

            }
        }

        // Convert legacy JKS/JCEKS security stores left over from 7.0.x or older to PKCS12,
        // since 7.1.0+ only reads the fixed *.p12 file names.
        migrateLegacyKeystores();
        upgradeStoreJvmOptions();

        //Detect an NSS upgrade scenario and point to the steps wiki

        if (requiresSecureAdmin()) {

            _logger.log(Level.WARNING, SecurityLoggerInfo.securityUpgradeServiceWarning);
        }

    }

    /**
     * Detects legacy JKS/JCEKS security stores in {@code <domain>/config} (and the saved master password
     * store in the domain directory) and converts them to PKCS12 under the file names used by 7.1.0+
     * ({@code keystore.p12}, {@code cacerts.p12}, {@code domain-passwords.p12}, {@code master-password.p12}).
     * <p>
     * The conversion is idempotent: a store is skipped if the legacy file is absent or the PKCS12 target
     * already exists. Each converted legacy file is retained with a {@value #BAK_SUFFIX} suffix instead of
     * being deleted, and any failure is logged without aborting the upgrade.
     */
    private void migrateLegacyKeystores() {
        // The saved master password (JCEKS in the domain directory in 7.0.x and older) is protected by a
        // fixed password, not by the master password, so it can always be migrated.
        migrateStore(new File(env.getInstanceRoot(), MASTER_PASSWORD_FILENAME_LEGACY), TYPE_JCEKS,
            new File(env.getInstanceRoot(), MASTER_PASSWORD_FILENAME), MASTER_PASSWORD_PASSWORD.toCharArray());

        File configDir = new File(env.getInstanceRoot(), DIR_CONFIG);
        if (!configDir.isDirectory()) {
            return;
        }

        boolean legacyPresent = new File(configDir, KEYSTORE_FILENAME_LEGACY).exists()
            || new File(configDir, TRUSTSTORE_FILENAME_LEGACY).exists()
            || new File(configDir, LEGACY_DOMAIN_PASSWORDS).exists();
        if (!legacyPresent) {
            return;
        }

        char[] masterPassword = masterPasswordProvider == null ? null : masterPasswordProvider.getMasterPassword();
        if (masterPassword == null) {
            _logger.log(Level.WARNING, SecurityLoggerInfo.securityUpgradeKeystoreNoMasterPassword,
                configDir.getAbsolutePath());
            return;
        }

        migrateStore(new File(configDir, KEYSTORE_FILENAME_LEGACY), TYPE_JKS,
            new File(configDir, KEYSTORE_FILENAME_DEFAULT), masterPassword);
        migrateStore(new File(configDir, TRUSTSTORE_FILENAME_LEGACY), TYPE_JKS,
            new File(configDir, TRUSTSTORE_FILENAME_DEFAULT), masterPassword);
        migrateStore(new File(configDir, LEGACY_DOMAIN_PASSWORDS), TYPE_JCEKS,
            new File(configDir, PasswordAdapter.PASSWORD_ALIAS_KEYSTORE), masterPassword);
    }

    /**
     * Converts a single legacy keystore to PKCS12. No-op when the legacy file is missing or the target
     * already exists. All entries (private keys, trusted certificates and secret keys) are copied using the
     * master password, and on success the legacy file is renamed to {@code <name>}{@value #BAK_SUFFIX}.
     */
    private void migrateStore(File legacyFile, String legacyType, File targetFile, char[] masterPassword) {
        if (!legacyFile.exists() || targetFile.exists()) {
            return;
        }
        try {
            convertToPkcs12(legacyFile, legacyType, targetFile, masterPassword);

            File backup = new File(legacyFile.getPath() + BAK_SUFFIX);
            String backupName = legacyFile.renameTo(backup) ? backup.getName() : legacyFile.getName();
            _logger.log(Level.INFO, SecurityLoggerInfo.securityUpgradeKeystoreMigrated,
                new Object[] {legacyFile.getAbsolutePath(), targetFile.getAbsolutePath(), backupName});
        } catch (Exception e) {
            // Best effort: do not leave a half-written PKCS12 behind and keep the legacy file in place.
            if (targetFile.exists() && !targetFile.delete()) {
                targetFile.deleteOnExit();
            }
            _logger.log(Level.WARNING, SecurityLoggerInfo.securityUpgradeKeystoreMigrationFailed,
                legacyFile.getAbsolutePath());
            _logger.log(Level.FINE, "Legacy keystore migration failure detail", e);
        }
    }

    /**
     * Rewrites JVM options (such as {@code -Djavax.net.ssl.keyStore}) that still point to a legacy JKS store
     * under {@code <instanceRoot>/config} so they reference the PKCS12 successor instead, in every
     * {@code java-config} in the domain. A reference is only rewritten when the corresponding PKCS12 store
     * exists, i.e. after {@link #migrateLegacyKeystores()} succeeded, which also makes this idempotent.
     */
    private void upgradeStoreJvmOptions() {
        File configDir = new File(env.getInstanceRoot(), DIR_CONFIG);
        boolean keystoreMigrated = new File(configDir, KEYSTORE_FILENAME_DEFAULT).exists();
        boolean truststoreMigrated = new File(configDir, TRUSTSTORE_FILENAME_DEFAULT).exists();
        if (!keystoreMigrated && !truststoreMigrated) {
            return;
        }
        for (Config config : configs.getConfig()) {
            JavaConfig javaConfig = config.getJavaConfig();
            if (javaConfig == null) {
                continue;
            }
            final List<String> upgradedOptions = new ArrayList<>();
            boolean changed = false;
            for (String option : javaConfig.getJvmOptions()) {
                String upgradedOption = upgradeStoreJvmOption(option, keystoreMigrated, truststoreMigrated);
                changed |= !upgradedOption.equals(option);
                upgradedOptions.add(upgradedOption);
            }
            if (!changed) {
                continue;
            }
            try {
                ConfigSupport.apply(new SingleConfigCode<JavaConfig>() {
                    @Override
                    public Object run(JavaConfig updatedJavaConfig) throws PropertyVetoException {
                        updatedJavaConfig.setJvmOptions(upgradedOptions);
                        return updatedJavaConfig;
                    }
                }, javaConfig);
                _logger.log(Level.INFO, SecurityLoggerInfo.securityUpgradeJvmOptionsUpdated, config.getName());
            } catch (TransactionFailure tf) {
                _logger.log(Level.WARNING, SecurityLoggerInfo.securityUpgradeJvmOptionsFailed, config.getName());
                _logger.log(Level.FINE, "JVM options upgrade failure detail", tf);
            }
        }
    }

    /**
     * Returns the given JVM option rewritten to reference the PKCS12 store when it points to a legacy JKS
     * store under a {@code config} directory (e.g. {@code ${com.sun.aas.instanceRoot}/config/keystore.jks}),
     * or the option unchanged otherwise. Package-visible for testing.
     */
    static String upgradeStoreJvmOption(String option, boolean keystoreMigrated, boolean truststoreMigrated) {
        if (keystoreMigrated && option.endsWith("/config/" + KEYSTORE_FILENAME_LEGACY)) {
            return option.substring(0, option.length() - KEYSTORE_FILENAME_LEGACY.length()) + KEYSTORE_FILENAME_DEFAULT;
        }
        if (truststoreMigrated && option.endsWith("/config/" + TRUSTSTORE_FILENAME_LEGACY)) {
            return option.substring(0, option.length() - TRUSTSTORE_FILENAME_LEGACY.length()) + TRUSTSTORE_FILENAME_DEFAULT;
        }
        return option;
    }

    /**
     * Reads {@code legacyFile} of the given {@code legacyType} (e.g. {@code JKS} or {@code JCEKS}) and writes
     * its entries to {@code targetFile} as a PKCS12 keystore, all secured with {@code masterPassword}. Private
     * keys, trusted certificates and secret keys are copied. Package-visible for testing.
     */
    static void convertToPkcs12(File legacyFile, String legacyType, File targetFile, char[] masterPassword)
        throws Exception {
        KeyStore source = KeyStore.getInstance(legacyType);
        try (FileInputStream in = new FileInputStream(legacyFile)) {
            source.load(in, masterPassword);
        }

        KeyStore target = KeyStore.getInstance(KEYSTORE_TYPE_DEFAULT);
        target.load(null, masterPassword);

        Enumeration<String> aliases = source.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (source.isKeyEntry(alias)) {
                Key key = source.getKey(alias, masterPassword);
                Certificate[] chain = source.getCertificateChain(alias);
                target.setKeyEntry(alias, key, masterPassword, chain);
            } else if (source.isCertificateEntry(alias)) {
                target.setCertificateEntry(alias, source.getCertificate(alias));
            }
        }

        try (FileOutputStream out = new FileOutputStream(targetFile)) {
            target.store(out, masterPassword);
        }
    }

    /*
     * Method to detect an NSS install.
     */

    public boolean requiresSecureAdmin() {

        String instanceRoot = env.getInstanceRoot().getAbsolutePath();
        File configDir = new File(instanceRoot, "config");
        //default KS password

        if (configDir.isDirectory()) {
            for (File configFile : configDir.listFiles()) {
                if (configFile.getName().endsWith(NSS)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void upgradeJACCProvider(SecurityService securityService) {
        try {
            List<JaccProvider> jaccProviders = securityService.getJaccProvider();
            for (JaccProvider jacc : jaccProviders) {
                if ("org.glassfish.exousia.modules.locked.SimplePolicyConfigurationFactory"
                    .equals(jacc.getPolicyConfigurationFactoryProvider())) {
                    //simple policy provider already present
                    return;
                }
            }
            ConfigSupport.apply(new SingleConfigCode<SecurityService>() {
                @Override
                public Object run(SecurityService secServ) throws PropertyVetoException, TransactionFailure {
                    JaccProvider jacc = secServ.createChild(JaccProvider.class);
                    //add the simple provider to the domain's security service
                    jacc.setName("simple");
                    jacc.setPolicyConfigurationFactoryProvider("org.glassfish.exousia.modules.locked.SimplePolicyConfigurationFactory");
                    jacc.setPolicyProvider("org.glassfish.exousia.modules.locked.SimplePolicyProvider");
                    secServ.getJaccProvider().add(jacc);
                    return secServ;
                }
            }, securityService);
        } catch (TransactionFailure ex) {
            Logger.getAnonymousLogger().log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }

    }

    private boolean deleteFile(File path) {
        if (path != null && path.exists()) {
            if (path.isDirectory()) {
                File[] files = path.listFiles();
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteFile(file);
                        if (file.delete())
                            continue;
                    } else {
                        if (file.delete())
                            continue;
                    }
                }
            }
            if (!path.delete()) {
                return false;
            }
        }
        return true;
    }

}
