/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.glassfish.microprofile.config;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigValue;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.microprofile.config.util.LazyProperty;

/**
 *
 * @author Ondro Mihalyi
 */
class GlassFishConfig implements Config {

    private Supplier<Config> configProducer;
    private volatile Config defaultConfigDelegate;

    public GlassFishConfig(Supplier<Config> configProducer) {
        this.configProducer = configProducer;
    }

    private Config getDefaultConfigDelegate() {
        return defaultConfigDelegate;
    }

    private void setDefaultConfigDelegate(Config defaultConfigDelegate) {
        this.defaultConfigDelegate = defaultConfigDelegate;
    }

    @Override
    public <T> T getValue(String propertyName, Class<T> propertyType) {
        return getConfigDelegate().getValue(propertyName, propertyType);
    }

    @Override
    public ConfigValue getConfigValue(String propertyName) {
        return getConfigDelegate().getConfigValue(propertyName);
    }

    @Override
    public <T> List<T> getValues(String propertyName, Class<T> propertyType) {
        return getConfigDelegate().getValues(propertyName, propertyType);
    }

    @Override
    public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
        return getConfigDelegate().getOptionalValue(propertyName, propertyType);
    }

    @Override
    public <T> Optional<List<T>> getOptionalValues(String propertyName, Class<T> propertyType) {
        return getConfigDelegate().getOptionalValues(propertyName, propertyType);
    }

    @Override
    public Iterable<String> getPropertyNames() {
        return getConfigDelegate().getPropertyNames();
    }

    @Override
    public Iterable<ConfigSource> getConfigSources() {
        return getConfigDelegate().getConfigSources();
    }

    @Override
    public <T> Optional<Converter<T>> getConverter(Class<T> forType) {
        return getConfigDelegate().getConverter(forType);
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        return getConfigDelegate().unwrap(type);
    }

    private Config getConfigDelegate() {
        final ApplicationInfo currentApplicationInfo = getCurrentApplicationInfo();
        Config config = null;
        if (currentApplicationInfo != null) {
            final GlassFishConfigs configs = LazyProperty.getOrSet(currentApplicationInfo, GlassFishConfig::getConfigsFromAppInfo, GlassFishConfigs::new, GlassFishConfig::setConfigsToAppInfo);
            config = LazyProperty.getOrSet(configs, this::getConfigFromConfigs, configProducer, this::setConfigToConfigs);
        } else {
            config = LazyProperty.getOrSet(this, GlassFishConfig::getDefaultConfigDelegate, configProducer, GlassFishConfig::setDefaultConfigDelegate);
        }
        return config;
    }

    private static GlassFishConfigs getConfigsFromAppInfo(ApplicationInfo applicationInfo) {
        return applicationInfo.getMetaData(GlassFishConfigs.class);
    }

    private static void setConfigsToAppInfo(ApplicationInfo applicationInfo, GlassFishConfigs configs) {
        applicationInfo.addMetaData(configs);
    }

    public Config getConfigFromConfigs(GlassFishConfigs cfgs) {
        return cfgs.get(this);
    }

    private void setConfigToConfigs(GlassFishConfigs cfgs, Config cfg) {
        cfgs.put(this, cfg);
    }

    private static ApplicationInfo getCurrentApplicationInfo() {
        final Deployment deploymentService = Globals.get(Deployment.class);
        DeploymentContext deploymentContext = null;
        if (deploymentService != null) {
            deploymentContext = deploymentService.getCurrentDeploymentContext();
        }
        if (deploymentContext != null) {
            // during app deployment, we don't have current invocation, we retrieve it from the deployment
            return deploymentContext.getModuleMetaData(ApplicationInfo.class);
        }
        final ComponentInvocation currentInvocation = Globals.get(InvocationManager.class).getCurrentInvocation();
        String applicationName = null;
        if (currentInvocation != null && null != (applicationName = currentInvocation.getAppName())) {
            ApplicationRegistry applicationRegistry = Globals.get(ApplicationRegistry.class);
            if (applicationRegistry != null) {
                // application started
                return applicationRegistry.get(applicationName);
            }
        }
        // we're not in a context related to an application
        return null;
    }

}
