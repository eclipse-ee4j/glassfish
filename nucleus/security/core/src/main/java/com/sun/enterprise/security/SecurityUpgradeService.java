/*
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
import com.sun.enterprise.config.serverbeans.SecurityService;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

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

    private static final String DIR_GENERATED_POLICY = "generated" + File.separator + "policy";
    private static final String DIR_CONFIG = "config";
    private static final String JKS = ".jks";
    private static final String NSS = ".db";
    //  private static final String KEYSTORE = "keystore.jks";
    //  private static final String TRUSTSTORE = "cacerts.jks";

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

        //Detect an NSS upgrade scenario and point to the steps wiki

        if (requiresSecureAdmin()) {

            _logger.log(Level.WARNING, SecurityLoggerInfo.securityUpgradeServiceWarning);
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
