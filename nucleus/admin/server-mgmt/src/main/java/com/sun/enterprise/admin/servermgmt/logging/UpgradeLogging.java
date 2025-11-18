/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.servermgmt.logging;

import com.sun.common.util.logging.LoggingConfigImpl;
import com.sun.enterprise.admin.servermgmt.RepositoryConfig;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.LogService;
import com.sun.enterprise.config.serverbeans.ModuleLogLevels;
import com.sun.enterprise.util.io.FileUtils;

import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.TransactionFailure;

import static java.lang.System.Logger.Level.ERROR;
import static org.glassfish.embeddable.GlassFishVariable.INSTANCE_ROOT;

/**
 * Startup service to update existing domain.xml to move log-service entries to logging.properties file.
 *
 * @author Carla Mott
 */
@Service
public class UpgradeLogging implements ConfigurationUpgrade, PostConstruct {
    private static final Logger LOG = System.getLogger(UpgradeLogging.class.getName());

    @Inject
    Configs configs;
    @Inject
    LoggingConfigImpl logConfig;

    @Override
    public void postConstruct() {
        for (Config config : configs.getConfig()) {
            doUpgrade(config);
        }
    }

    private void doUpgrade(Config config) {
        // v3 uses logging.properties to configure the logging facility.
        // move all log-service elements to logging.properties
        final LogService logService = config.getLogService();

        // check if null and exit
        if (logService == null)
         {
            return;
        // get a copy of the logging.properties file
        }

        try {
            RepositoryConfig rc = new RepositoryConfig();
            String configDir = rc.getRepositoryRoot() + File.separator + rc.getRepositoryName() + File.separator + rc.getInstanceName()
                    + File.separator + "config";
            PEFileLayout layout = new PEFileLayout(rc);
            File src = new File(layout.getTemplatesDir(), PEFileLayout.LOGGING_PROPERTIES_FILE);
            File dest = new File(configDir, PEFileLayout.LOGGING_PROPERTIES_FILE);
            if (!dest.exists()) {
                FileUtils.copy(src, dest);
            }

        } catch (IOException e) {
            LOG.log(ERROR, "Failure while upgrading log-service. Could not create logging.properties file.", e);
        }

        try {
            //Get the logLevels
            ModuleLogLevels mll = logService.getModuleLogLevels();

            Map<String, String> logLevels = mll.getAllLogLevels();
            String file = logService.getFile();
            String instanceRoot = System.getProperty(INSTANCE_ROOT.getSystemPropertyName());
            if (file.contains(instanceRoot)) {
                file = file.replace(instanceRoot, "${" + INSTANCE_ROOT.getSystemPropertyName() + "}");
            }
            logLevels.put("file", file);
            logLevels.put("use-system-logging", logService.getUseSystemLogging());
            //this can have multiple values so need to add
            logLevels.put("log-handler", logService.getLogHandler());
            logLevels.put("log-filter", logService.getLogFilter());
            logLevels.put("log-to-console", logService.getLogToConsole());
            logLevels.put("log-rotation-limit-in-bytes", logService.getLogRotationLimitInBytes());
            logLevels.put("log-rotation-timelimit-in-minutes", logService.getLogRotationTimelimitInMinutes());
            logLevels.put("alarms", logService.getAlarms());
            logLevels.put("retain-error-statistics-for-hours", logService.getRetainErrorStatisticsForHours());
            final Map<String, String> m = new HashMap<>(logLevels);

            ConfigSupport.apply(c -> {
                try {
                    logConfig.updateLoggingProperties(m);
                    c.setLogService(null);
                } catch (IOException e) {
                    LOG.log(ERROR, "Failure while upgrading log-service. Could not update logging.properties file.", e);
                }
                return null;
            }, config);
        } catch (TransactionFailure e) {
            throw new RuntimeException("Failure while upgrading log-service", e);
        }
    }
}
