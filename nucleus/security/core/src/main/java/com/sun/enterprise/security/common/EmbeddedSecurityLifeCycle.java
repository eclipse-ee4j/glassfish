/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.security.common;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.internal.embedded.EmbeddedFileSystem;
import org.glassfish.internal.embedded.EmbeddedLifecycle;
import org.glassfish.internal.embedded.Server;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.security.EmbeddedSecurity;
import com.sun.enterprise.security.SecurityLoggerInfo;
import com.sun.enterprise.server.pluggable.SecuritySupport;
import com.sun.enterprise.util.io.FileUtils;

import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * @author Nithya Subramanian
 */
@Service
public class EmbeddedSecurityLifeCycle implements EmbeddedLifecycle {

    private static final Logger _logger = SecurityLoggerInfo.getLogger();

    @Inject
    private EmbeddedSecurity embeddedSecurity;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private SecurityService securityService;

    @Override
    public void creation(Server server) {

        //If the instanceRoot is not set to a non-embedded GF install,
        //copy the security config files from the security.jar to the instanceRoot/config dir

        EmbeddedFileSystem fileSystem = server.getFileSystem();
        File instanceRoot = fileSystem.instanceRoot;
        if (instanceRoot == null) {
            return;
        }

        try {
            //Get the keyfile names from the security service
            List<String> keyFileNames = embeddedSecurity.getKeyFileNames(securityService);
            File cfgDir = new File(instanceRoot, "config");
            for (String keyFileName : keyFileNames) {
                // Copy the keyfiles in instanceRoot/config. If file is already present, then exit (handled by getManagedFile)
                FileUtils.copyResourceToDirectory(embeddedSecurity.parseFileName(keyFileName), cfgDir);
            }
            //Copy the other security files to instanceRoot/config
            //Assuming that these files are present as config/filename in the embedded jar file and are to be extracted that way/
            FileUtils.copyResourceToDirectory("login.conf", cfgDir);
            FileUtils.copyResourceToDirectory("server.policy", cfgDir);
            FileUtils.copyResourceToDirectory("cacerts.jks", cfgDir);
            FileUtils.copyResourceToDirectory("keystore.jks", cfgDir);
            String keystoreFile = null;
            String truststoreFile = null;
            try {
                keystoreFile = Util.writeConfigFileToTempDir("keystore.jks").getAbsolutePath();
                truststoreFile = Util.writeConfigFileToTempDir("cacerts.jks").getAbsolutePath();
            } catch (IOException ex) {
                _logger.log(Level.SEVERE, SecurityLoggerInfo.obtainingKeyAndTrustStoresError, ex);
            }
            System.setProperty(SecuritySupport.keyStoreProp, keystoreFile);
            System.setProperty(SecuritySupport.trustStoreProp, truststoreFile);
        } catch (IOException ioEx) {
            _logger.log(Level.WARNING, SecurityLoggerInfo.copyingSecurityConfigFilesIOError, ioEx);
        }
    }

    @Override
    public void destruction(Server server) {

    }

}
