/*
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

package org.glassfish.web.upgrade;

import com.sun.enterprise.util.io.FileUtils;

import jakarta.inject.Inject;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.web.LogFacade;
import org.jvnet.hk2.annotations.Service;


/**
 * This class implements the contract for services want to perform some upgrade
 * on the application server configuration.
 *
 * @author Shing Wai Chan
 */
@Service(name="webConfigurationUpgrade")
public class WebConfigurationUpgrade implements ConfigurationUpgrade, PostConstruct {

    private static final Logger _logger = LogFacade.getLogger();

    @Inject
    private ServerEnvironment serverEnvironment;

    public void postConstruct() {
        removeSerializedSessions(serverEnvironment.getApplicationCompileJspPath());
    }

    private static void removeSerializedSessions(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    removeSerializedSessions(f);
                } else if (f.getName().endsWith("SESSIONS.ser")) {
                    if (!FileUtils.deleteFileMaybe(f)) {
                        _logger.log(Level.WARNING,
                                LogFacade.UNABLE_TO_DELETE,
                                f.toString());
                    }
                }

            }
        }
    }
}
