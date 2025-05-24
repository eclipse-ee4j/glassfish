/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

import jakarta.inject.Inject;

import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * Starting in GlassFish 3.1.2, the DAS uses SSL to send admin requests to instances regardless of whether the user has enabled
 * secure admin. For this to work correctly when upgrading from earlier 3.x releases, there are some changes to the configuration
 * that must be in place. This start-up service makes sure that the config is correct as quickly as possible to avoid degrading
 * start-up performance. (Upgrades from 2.x are handled by the SecureAdminConfigUpgrade upgrade service.)
 * <p>
 * For 3.1.2 and later the configuration needs to include:
 *
 * <pre>
 * {@code
 * <secure-admin special-admin-indicator="xxx">
 *   at least one <secure-admin-principal> element; if none, supply these defaults:
 *
 *   <secure-admin-principal dn="dn-for-DAS"/>
 *   <secure-admin-principal dn="dn-for-instances"/>
 * }
 * </pre>
 *
 * Further, the sec-admin-listener set-up needs to be added (if not already there) for the non-DAS configurations. Note that the
 * work to configure the listeners and related protocols are already implemented by SecureAdminCommand, so this class delegates
 * much of its work to that logic.
 *
 * @author Tim Quinn
 */
@Service
@RunLevel(StartupRunLevel.VAL)
public class SecureAdminStartupCheck extends SecureAdminUpgradeHelper implements PostConstruct {

    @Inject
    private ServerEnvironment serverEnvironment;

    /**
     * If a formal upgrade is in progress then this Startup service
     * will be invoked first.  The upgrade should take care of things,
     * so this becomes a no-op.
     */
    @Override
    public void postConstruct() {
        try {
            if (isFormalUpgrade() || serverEnvironment.isEmbedded()) {
                return;
            }
            ensureSecureAdminReady();
            ensureNonDASConfigsReady();
            commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private boolean isFormalUpgrade() {
        return Boolean.valueOf(startupArg("-upgrade"));
    }
}
