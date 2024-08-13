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

package org.glassfish.deployment.common;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationConfig;
import com.sun.enterprise.config.serverbeans.Engine;
import com.sun.enterprise.config.serverbeans.Module;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * During redeployment we preserve the application config information, if any,
 * that the administrator has defined for the application.  Then, during the
 * deploy part of redeployment we restore it.
 *
 * This class encapsulates the dependencies on exactly how we store that
 * information in the application properties in the deployment context.
 *
 * @author tjquinn
 */
public class ApplicationConfigInfo {

    private final Map<String,Map<String,ApplicationConfig>> moduleToEngineToAppConfig;

    public ApplicationConfigInfo() {
        moduleToEngineToAppConfig = createNewMap();
    }

    public ApplicationConfigInfo(final Properties appProperties) {
        Object map =
                appProperties.get(DeploymentProperties.APP_CONFIG);
        if (map == null) {
            moduleToEngineToAppConfig = createNewMap();
        } else {
            moduleToEngineToAppConfig = (Map<String, Map<String,ApplicationConfig>>) map;
        }
    }

    public ApplicationConfigInfo(final Application app) {

        moduleToEngineToAppConfig = createNewMap();
        if (app != null) {
            for (Module m : app.getModule()) {
                for (Engine e : m.getEngines()) {
                    put(m.getName(), e.getSniffer(), e.getApplicationConfig());
                }
            }
        }
    }

    private Map<String,Map<String,ApplicationConfig>> createNewMap() {
        return new HashMap<String,Map<String,ApplicationConfig>>();
    }

    public <T extends ApplicationConfig> T get(final String moduleName,
            final String engineName) {
        T result = null;
        Map<String,? extends ApplicationConfig> engineToAppConfig =
                moduleToEngineToAppConfig.get(moduleName);
        if (engineToAppConfig != null) {
            result = (T) engineToAppConfig.get(engineName);
        }
        return result;
    }

    public void  put(final String moduleName, final String engineName,
            final ApplicationConfig appConfig) {
        Map<String,ApplicationConfig> engineToAppConfig =
                moduleToEngineToAppConfig.get(moduleName);
        if (engineToAppConfig == null) {
            engineToAppConfig = new HashMap<String,ApplicationConfig>();
            moduleToEngineToAppConfig.put(moduleName, engineToAppConfig);
        }
        engineToAppConfig.put(engineName, appConfig);
    }

    public Set<String> moduleNames() {
        return moduleToEngineToAppConfig.keySet();
    }

    public Set<String> engineNames(final String moduleName) {
        return moduleToEngineToAppConfig.get(moduleName).keySet();
    }

    public void store(final Properties appProps) {
        appProps.put(DeploymentProperties.APP_CONFIG, moduleToEngineToAppConfig);
    }

}
