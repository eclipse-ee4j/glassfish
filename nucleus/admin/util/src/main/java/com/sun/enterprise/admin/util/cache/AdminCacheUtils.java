/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.util.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.jvnet.hk2.annotations.Service;

/**
 * Tooling for AdminCache {@link DataProvider} implementation.
 *
 * @author mmares
 */
@Service
public class AdminCacheUtils {

    private static final AdminCacheUtils instance = new AdminCacheUtils();

    private final Map<Class, DataProvider> providers = new HashMap<Class, DataProvider>();
    private final Pattern keyPattern = Pattern.compile("([-_.a-zA-Z0-9]+/?)+");
    //private final ServiceLoader<DataProvider> dataProviderLoader = ServiceLoader.<DataProvider>load(DataProvider.class);

    private static final DataProvider[] allProviders = new DataProvider[] { new StringDataProvider(), new ByteArrayDataProvider(),
            new CommandModelDataProvider() };

    private AdminCacheUtils() {
    }

    public DataProvider getProvider(final Class clazz) {
        DataProvider result = providers.get(clazz);
        if (result == null) {
            //Use hardcoded data providers - fastest and not problematic
            for (DataProvider provider : allProviders) {
                if (provider.accept(clazz)) {
                    providers.put(clazz, provider);
                    return provider;
                }
            }
            //            ServiceLocator habitat = Globals.getDefaultHabitat();
            //            if (habitat != null) {
            //                List<DataProvider> allServices = habitat.getAllServices(DataProvider.class);
            //                for (DataProvider provider : allServices) {
            //                    if (provider.accept(clazz)) {
            //                        providers.put(clazz, provider);
            //                        return provider;
            //                    }
            //                }
            //            }
            //            for (DataProvider provider : dataProviderLoader) {
            //                if (provider.accept(clazz)) {
            //                    providers.put(clazz, provider);
            //                    return provider;
            //                }
            //            }

            return null;
        } else {
            return result;
        }
    }

    public final boolean validateKey(final String key) {
        return keyPattern.matcher(key).matches();
    }

    /**
     * Return preferred {@link AdminCache}
     */
    public static AdminCache getCache() {
        return AdminCacheMemStore.getInstance();
    }

    public static AdminCacheUtils getInstance() {
        return instance;
    }

}
