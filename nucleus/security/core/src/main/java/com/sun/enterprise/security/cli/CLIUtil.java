/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.cli;

import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.JaccProvider;
import com.sun.enterprise.config.serverbeans.MessageSecurityConfig;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.util.List;

import org.glassfish.api.ActionReport;

/**
 *
 *
 */
public class CLIUtil {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CLIUtil.class);

    /**
     * Selects a config of interest from the domain, based on the target. (Eliminates duplicated code formerly in Create, Delete, and
     * ListAuthRealm).
     *
     * @param domain
     * @param target
     * @return
     */
    static Config chooseConfig(final Domain domain, final String target) {
        Config config = null;
        Config tmp = null;
        try {
            tmp = domain.getConfigs().getConfigByName(target);
        } catch (Exception ex) {
        }

        if (tmp != null) {
            return tmp;
        }
        Server targetServer = domain.getServerNamed(target);
        if (targetServer != null) {
            config = domain.getConfigNamed(targetServer.getConfigRef());
        }
        com.sun.enterprise.config.serverbeans.Cluster cluster = domain.getClusterNamed(target);
        if (cluster != null) {
            config = domain.getConfigNamed(cluster.getConfigRef());
        }
        return config;
    }

    static Config chooseConfig(final Domain domain, final String target, final ActionReport report) {
        final Config config = chooseConfig(domain, target);
        if (config == null) {
            report.setMessage(localStrings.getLocalString("util.noconfigfortarget", "Configuration for target {0} not found.", target));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
        return config;
    }

    static boolean isRealmNew(final SecurityService securityService, final String authRealmName) {

        // check if there exists an auth realm byt he specified name
        // if so return failure.
        List<AuthRealm> authrealms = securityService.getAuthRealm();
        for (AuthRealm authrealm : authrealms) {
            if (authrealm.getName().equals(authRealmName)) {
                return false;
            }
        }
        return true;
    }

    static AuthRealm findRealm(final SecurityService securityService, String authRealmName) {
        // ensure we have the file authrealm

        if (authRealmName == null) {
            authRealmName = securityService.getDefaultRealm();
        }

        for (AuthRealm authRealm : securityService.getAuthRealm()) {
            if (authRealm.getName().equals(authRealmName)) {
                return authRealm;
            }
        }
        return null;
    }

    static JaccProvider findJaccProvider(final SecurityService securityService, final String jaccProviderName) {
        final List<JaccProvider> jaccProviders = securityService.getJaccProvider();
        for (JaccProvider jaccProv : jaccProviders) {
            if (jaccProv.getName().equals(jaccProviderName)) {
                return jaccProv;
            }
        }
        return null;
    }

    static MessageSecurityConfig findMessageSecurityConfig(final SecurityService securityService, final String authLayer) {
        List<MessageSecurityConfig> mscs = securityService.getMessageSecurityConfig();

        for (MessageSecurityConfig msc : mscs) {
            if (msc.getAuthLayer().equals(authLayer)) {
                return msc;
            }
        }
        return null;
    }
}
