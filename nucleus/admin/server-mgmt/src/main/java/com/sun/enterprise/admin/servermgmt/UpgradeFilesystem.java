/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.util.logging.Level;

import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.admin.servermgmt.SLogger.BAD_RENAME_CERT_FILE;
import static com.sun.enterprise.admin.servermgmt.SLogger.RENAME_CERT_FILE;
import static com.sun.enterprise.admin.servermgmt.SLogger.getLogger;
import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;

/**
 * Startup service to update the filesystem from v2 to the v3 format
 *
 * @author Joe Di Pol
 */
@Service
public class UpgradeFilesystem implements ConfigurationUpgrade, PostConstruct {

    @Override
    public void postConstruct() {
        upgradeFilesystem();
    }

    private void upgradeFilesystem() {

        // Rename nodeagents to nodes
        String installDir = System.getProperty(INSTALL_ROOT.getSystemPropertyName());

        File agentsDir = new File(installDir, "nodeagents");
        File nodesDir = new File(installDir, "nodes");

        // Only do this if nodeagents exists and nodes does not
        if (agentsDir.exists() && !nodesDir.exists()) {
            getLogger().log(Level.INFO, RENAME_CERT_FILE, new Object[] { agentsDir.getPath(), nodesDir.getPath() });
            if (!agentsDir.renameTo(nodesDir)) {
                getLogger().log(Level.SEVERE, BAD_RENAME_CERT_FILE, new Object[] { agentsDir.getPath(), nodesDir.getPath() });
            }
        }
    }
}
