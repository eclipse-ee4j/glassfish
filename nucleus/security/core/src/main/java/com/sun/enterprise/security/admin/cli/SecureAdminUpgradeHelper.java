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

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SecureAdmin;
import com.sun.enterprise.config.serverbeans.SecureAdminHelper;
import com.sun.enterprise.config.serverbeans.SecureAdminPrincipal;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.security.admin.cli.SecureAdminCommand.ConfigLevelContext;
import com.sun.enterprise.security.admin.cli.SecureAdminCommand.TopLevelContext;
import com.sun.enterprise.security.admin.cli.SecureAdminCommand.Work;
import com.sun.enterprise.security.ssl.SSLUtils;

import jakarta.inject.Inject;

import java.io.IOException;
import java.security.KeyStoreException;
import java.util.Iterator;
import java.util.Properties;
import java.util.UUID;

import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.RetryableException;
import org.jvnet.hk2.config.Transaction;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Common logic for formal upgrade (i.e., start-domain --upgrade) and silent upgrade (starting a newer version of GlassFish using
 * an older version's domain.xml).
 *
 * @author Tim Quinn
 */
@Service
@PerLookup
public class SecureAdminUpgradeHelper {

    protected final static String DAS_CONFIG_NAME = "server-config";

    @Inject
    protected Domain domain;

    @Inject
    protected ServiceLocator habitat;

    @Inject
    protected StartupContext startupContext;

    private Transaction t = null;

    private SecureAdmin secureAdmin = null;

    private TopLevelContext topLevelContext = null;
    private SecureAdminHelper secureAdminHelper = null;
    private SSLUtils sslUtils = null;

    private Properties startupArgs = null;

    final protected Transaction transaction() {
        if (t == null) {
            t = new Transaction();
        }
        return t;
    }

    private TopLevelContext topLevelContext() {
        if (topLevelContext == null) {
            topLevelContext = new TopLevelContext(transaction(), domain);
        }
        return topLevelContext;
    }

    final protected void commit() throws RetryableException, TransactionFailure {
        if (t != null) {
            t.commit();
        }
    }

    final protected void rollback() {
        if (t != null) {
            t.rollback();
        }
    }

    final protected String specialAdminIndicator() {
        final UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    final protected SecureAdmin secureAdmin() throws TransactionFailure {
        if (secureAdmin == null) {
            secureAdmin = domain.getSecureAdmin();
            if (secureAdmin == null) {
                secureAdmin = /* topLevelContext(). */writableSecureAdmin();
                secureAdmin.setSpecialAdminIndicator(specialAdminIndicator());
            }
        }
        return secureAdmin;
    }

    final protected Domain writableDomain() throws TransactionFailure {
        return topLevelContext().writableDomain();
    }

    final protected SecureAdmin writableSecureAdmin() throws TransactionFailure {
        return topLevelContext().writableSecureAdmin();
    }

    final protected SecureAdminHelper secureAdminHelper() {
        if (secureAdminHelper == null) {
            secureAdminHelper = habitat.getService(SecureAdminHelper.class);
        }
        return secureAdminHelper;
    }

    final protected SSLUtils sslUtils() {
        if (sslUtils == null) {
            sslUtils = habitat.getService(SSLUtils.class);
        }
        return sslUtils;
    }

    final protected void ensureSecureAdminReady() throws TransactionFailure, IOException, KeyStoreException {
        if (secureAdmin().getSpecialAdminIndicator().isEmpty()) {
            /*
             * Set the indicator to a unique value so we can distinguish
             * one domain from another.
             */
            writableSecureAdmin().setSpecialAdminIndicator(specialAdminIndicator());
        }
        if (secureAdmin().getSecureAdminPrincipal().isEmpty() && secureAdmin().getSecureAdminInternalUser().isEmpty()) {
            /*
             * Add principal(s) for the aliases.
             */
            addPrincipalForAlias(secureAdmin().dasAlias());
            addPrincipalForAlias(secureAdmin().instanceAlias());
        }
    }

    final protected String startupArg(final String argName) {
        if (startupArgs == null) {
            if (startupContext != null) {
                startupArgs = startupContext.getArguments();
            } else {
                startupArgs = new Properties(); // shouldn't happen
            }
        }
        return startupArgs.getProperty(argName);
    }

    private void addPrincipalForAlias(final String alias) throws IOException, KeyStoreException, TransactionFailure {
        final SecureAdminPrincipal p = writableSecureAdmin().createChild(SecureAdminPrincipal.class);
        p.setDn(secureAdminHelper().getDN(alias, true));
        writableSecureAdmin().getSecureAdminPrincipal().add(p);
    }

    final protected void ensureNonDASConfigsReady() throws TransactionFailure {
        for (Config c : domain.getConfigs().getConfig()) {
            if (!c.getName().equals(SecureAdminCommand.DAS_CONFIG_NAME)) {
                if (!ensureConfigReady(c)) {
                    break;
                }
            }
        }
    }

    final protected void ensureDASConfigReady() {

    }

    private boolean ensureConfigReady(final Config c) throws TransactionFailure {
        /*
         * See if this config is already set up for secure admin.
         */
        final NetworkConfig nc = c.getNetworkConfig();
        if (nc == null) {
            /*
             * If there is no network config for this configuration then it is
             * probably a test configuration of some sort.  In any case, there
             * is no lower-level network protocols to verify so declare this
             * config to be OK.
             */
            return true;
        }
        Protocol secAdminProtocol = nc.getProtocols().findProtocol(SecureAdminCommand.SEC_ADMIN_LISTENER_PROTOCOL_NAME);
        if (secAdminProtocol != null) {
            return true;
        }
        final EnableSecureAdminCommand enableCmd = new EnableSecureAdminCommand();
        final Config c_w = transaction().enroll(c);
        ConfigLevelContext configLevelContext = new ConfigLevelContext(topLevelContext(), c_w);
        for (Iterator<Work<ConfigLevelContext>> it = enableCmd.perConfigSteps(); it.hasNext();) {
            final Work<ConfigLevelContext> step = it.next();
            if (!step.run(configLevelContext)) {
                rollback();
                return false;
            }
        }
        return true;
    }
}
