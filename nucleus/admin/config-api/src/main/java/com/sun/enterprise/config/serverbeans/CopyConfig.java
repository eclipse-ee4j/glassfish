/*
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

package com.sun.enterprise.config.serverbeans;

import com.sun.enterprise.config.util.ConfigApiLoggerInfo;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.GenericCrudCommand;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * This is the abstract class which will be used by the config beans {@link Cluster} and {@link Server} classes to copy
 * the default-configs
 *
 */
public abstract class CopyConfig implements AdminCommand {

    @Param(primary = true, multiple = true)
    protected List<String> configs;
    @Inject
    protected Domain domain;
    @Param(optional = true, separator = ':')
    protected String systemproperties;
    protected Config copyOfConfig;
    @Inject
    ServerEnvironment env;
    @Inject
    ServerEnvironmentImpl envImpl;
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CopyConfig.class);

    public Config copyConfig(Configs configs, Config config, String destConfigName, Logger logger)
            throws PropertyVetoException, TransactionFailure {
        final Config destCopy = (Config) config.deepCopy(configs);
        if (systemproperties != null) {
            final Properties properties = GenericCrudCommand.convertStringToProperties(systemproperties, ':');

            for (final Object key : properties.keySet()) {
                final String propName = (String) key;
                //cannot update a system property so remove it first
                List<SystemProperty> sysprops = destCopy.getSystemProperty();
                for (SystemProperty sysprop : sysprops) {
                    if (propName.equals(sysprop.getName())) {
                        sysprops.remove(sysprop);
                        break;
                    }

                }
                SystemProperty newSysProp = destCopy.createChild(SystemProperty.class);
                newSysProp.setName(propName);
                newSysProp.setValue(properties.getProperty(propName));
                destCopy.getSystemProperty().add(newSysProp);
            }
        }
        final String configName = destConfigName;
        destCopy.setName(configName);
        configs.getConfig().add(destCopy);
        copyOfConfig = destCopy;

        String srcConfig = "";
        srcConfig = config.getName();

        File configConfigDir = new File(env.getConfigDirPath(), configName);
        for (Config c : configs.getConfig()) {
            File existingConfigConfigDir = new File(env.getConfigDirPath(), c.getName());
            if (!c.getName().equals(configName) && configConfigDir.equals(existingConfigConfigDir)) {
                throw new TransactionFailure(localStrings.getLocalString("config.duplicate.dir",
                        "Config {0} is trying to use the same directory as config {1}", configName, c.getName()));
            }
        }
        try {
            if (!(new File(configConfigDir, "docroot").mkdirs() && new File(configConfigDir, "lib/ext").mkdirs())) {
                throw new IOException(localStrings.getLocalString("config.mkdirs", "error creating config specific directories"));
            }

            String srcConfigLoggingFile = env.getInstanceRoot().getAbsolutePath() + File.separator + "config" + File.separator + srcConfig
                    + File.separator + ServerEnvironmentImpl.kLoggingPropertiesFileName;
            File src = new File(srcConfigLoggingFile);

            if (!src.exists()) {
                src = new File(env.getConfigDirPath(), ServerEnvironmentImpl.kLoggingPropertiesFileName);
            }

            File dest = new File(configConfigDir, ServerEnvironmentImpl.kLoggingPropertiesFileName);
            FileUtils.copy(src, dest);
        } catch (Exception e) {
            logger.log(Level.WARNING, ConfigApiLoggerInfo.copyConfigError, e.getLocalizedMessage());
        }
        return destCopy;
    }
}
