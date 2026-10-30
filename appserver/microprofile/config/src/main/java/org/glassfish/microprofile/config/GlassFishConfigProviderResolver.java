/*
 * Copyright (c) 2025, 2026 Contributors to the Eclipse Foundation
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

import io.helidon.config.mp.MpConfigProviderResolver;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.glassfish.microprofile.config.util.LazyProperty;

/**
 * This resolver postpones retrieving the resolver when it's actually needed. And when we already have the correct application's threadContext classloader.
 *
 * @author Ondro Mihalyi
 */
public class GlassFishConfigProviderResolver extends ConfigProviderResolver {

    volatile private MpConfigProviderResolver resolverDelegate;

    private MpConfigProviderResolver getOrCreateResolverDelegate() {
        return LazyProperty.getOrCreateAndSet(
                this,
                GlassFishConfigProviderResolver::getResolverDelegate,
                MpConfigProviderResolver::new,
                GlassFishConfigProviderResolver::setResolverDelegate);
    }

    private MpConfigProviderResolver getResolverDelegate() {
        return resolverDelegate;
    }

    private void setResolverDelegate(MpConfigProviderResolver resolverDelegate) {
        this.resolverDelegate = resolverDelegate;
    }

    @Override
    public Config getConfig() {
        return new GlassFishConfig(() -> getOrCreateResolverDelegate().getConfig());
    }

    @Override
    public Config getConfig(ClassLoader loader) {
        return new GlassFishConfig(() -> getOrCreateResolverDelegate().getConfig(loader));
    }

    @Override
    public ConfigBuilder getBuilder() {
        return getOrCreateResolverDelegate().getBuilder();
    }

    @Override
    public void registerConfig(Config config, ClassLoader classLoader) {
        getOrCreateResolverDelegate().registerConfig(config, classLoader);
    }

    @Override
    public void releaseConfig(Config config) {
        getOrCreateResolverDelegate().releaseConfig(config);
    }
}

/**
 * Config delegates of the {@link GlassFishConfig} instances used by an application.
 * <p>
 * A new {@link GlassFishConfig} is created by every {@link GlassFishConfigProviderResolver#getConfig()} call,
 * so the keys are weak: an entry is released as soon as the application doesn't use its {@link GlassFishConfig}.
 * The values don't refer to the keys. The map is guarded by this object's monitor,
 * the same one {@link LazyProperty#getOrCreateAndSet} synchronizes on.
 */
final class GlassFishConfigs {

    private final Map<GlassFishConfig, Config> configs = new WeakHashMap<>();

    synchronized Config get(GlassFishConfig glassFishConfig) {
        return configs.get(glassFishConfig);
    }

    synchronized void put(GlassFishConfig glassFishConfig, Config config) {
        configs.put(glassFishConfig, config);
    }

    synchronized int size() {
        return configs.size();
    }
}
