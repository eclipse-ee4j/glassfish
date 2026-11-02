/*
 * Copyright (c) 2024, 2026 Contributors to the Eclipse Foundation.
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
import com.sun.common.util.logging.LoggingXMLNames;
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
import java.util.Map.Entry;
import java.util.stream.Stream;

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
        if (logService == null) {
            return;
        }

        createLoggingPropertiesFileIfMissing();
        final Map<String, String> properties = collectMigratableProperties(logService);

        try {
            ConfigSupport.apply(c -> {
                try {
                    logConfig.updateLoggingProperties(properties);
                } catch (IOException e) {
                    LOG.log(ERROR, "Failure while upgrading log-service. Could not update logging.properties file.", e);
                }
                c.setLogService(null);
                return null;
            }, config);
        } catch (TransactionFailure e) {
            throw new RuntimeException("Failure while upgrading log-service", e);
        }
    }

    private void createLoggingPropertiesFileIfMissing() {
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
    }

    /**
     * Collects the log-service settings that still have a counterpart in logging.properties,
     * keyed by their legacy domain.xml name so that {@link LoggingConfigImpl} translates them.
     *
     * <p>Everything else is dropped. Attributes such as {@code alarms}, {@code use-system-logging},
     * {@code log-filter}, {@code log-to-console} and {@code retain-error-statistics-for-hours},
     * and the module log levels removed after v2, have no counterpart, and used to be written
     * into logging.properties verbatim as keys nothing ever reads.
     */
    private Map<String, String> collectMigratableProperties(LogService logService) {
        final Map<String, String> legacy = new HashMap<>();
        final ModuleLogLevels moduleLogLevels = logService.getModuleLogLevels();
        if (moduleLogLevels != null) {
            legacy.putAll(moduleLogLevels.getAllLogLevels());
        }
        legacy.put(LoggingXMLNames.file, toInstanceRootRelative(logService.getFile()));
        legacy.put(LoggingXMLNames.logRotationLimitInBytes, logService.getLogRotationLimitInBytes());
        legacy.put(LoggingXMLNames.logRotationTimelimitInMinutes, logService.getLogRotationTimelimitInMinutes());

        final Map<String, String> properties = new HashMap<>();
        for (Entry<String, String> entry : legacy.entrySet()) {
            if (entry.getValue() != null && LoggingXMLNames.xmltoPropsMap.containsKey(entry.getKey())) {
                properties.put(entry.getKey(), entry.getValue());
            }
        }
        addCustomLogHandler(properties, logService.getLogHandler());
        return properties;
    }

    /**
     * The log-service log-handler named a handler <em>added</em> to the chain, while the key it
     * translates to is the complete list of root handlers. Appending keeps the handlers
     * configured by the logging.properties template, above all the GlassFishLogHandler writing
     * the server log.
     */
    private void addCustomLogHandler(Map<String, String> properties, String logHandler) {
        if (logHandler == null || logHandler.isBlank()) {
            return;
        }
        final String rootHandlersKey = LoggingXMLNames.xmltoPropsMap.get(LoggingXMLNames.logHandler);
        final String current;
        try {
            current = logConfig.getLoggingProperties().get(rootHandlersKey);
        } catch (IOException e) {
            LOG.log(ERROR, "Failure while upgrading log-service. Could not read logging.properties file,"
                + " the " + logHandler + " handler has to be added manually.", e);
            return;
        }
        if (current == null || current.isBlank()) {
            properties.put(rootHandlersKey, logHandler);
        } else if (Stream.of(current.split(",")).map(String::trim).noneMatch(logHandler::equals)) {
            properties.put(rootHandlersKey, current + "," + logHandler);
        }
    }

    private static String toInstanceRootRelative(String file) {
        if (file == null) {
            return null;
        }
        final String instanceRoot = System.getProperty(INSTANCE_ROOT.getSystemPropertyName());
        if (instanceRoot == null || !file.contains(instanceRoot)) {
            return file;
        }
        return file.replace(instanceRoot, "${" + INSTANCE_ROOT.getSystemPropertyName() + "}");
    }
}
