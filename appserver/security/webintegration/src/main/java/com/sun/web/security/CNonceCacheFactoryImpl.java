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

package com.sun.web.security;

import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.security.CNonceCacheFactory;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.security.common.CNonceCache;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author vbkumarjayanti
 */
@Service
@Singleton
public class CNonceCacheFactoryImpl implements CNonceCacheFactory, PostConstruct {

    @Inject
    @Named("HA-CNonceCache")
    private Provider<CNonceCache> cHANonceCacheProvider;

    @Inject
    @Named("CNonceCache")
    private Provider<CNonceCache> cNonceCacheProvider;

    @Inject()
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private SecurityService secService;

    /**
     * Maximum number of client nonces to keep in the cache. If not specified, the default value of 1000 is used.
     */
    protected long cnonceCacheSize = 1000;

    /**
     * How long server nonces are valid for in milliseconds. Defaults to 5 minutes.
     */
    protected long nonceValidity = 5 * 60 * 1000;

    @Override
    public void postConstruct() {
        String sz = this.secService.getPropertyValue("NONCE_CACHE_SIZE");
        String age = this.secService.getPropertyValue("MAX_NONCE_AGE");
        if (sz != null) {
            this.cnonceCacheSize = Long.parseLong(sz);
        }
        if (age != null) {
            this.nonceValidity = Long.parseLong(age);
        }
    }

    @Override
    public CNonceCache createCNonceCache(String appName, String clusterName, String instanceName, String storeName) {
        boolean haEnabled = (clusterName != null) && (instanceName != null) && (storeName != null);
        CNonceCache cache = null;
        Map<String, String> map = new HashMap<>();
        if (haEnabled) {
            cache = cHANonceCacheProvider.get();
            map.put(CLUSTER_NAME_PROP, clusterName);
            map.put(INSTANCE_NAME_PROP, instanceName);
        } else {
            cache = cNonceCacheProvider.get();
        }
        if (cache != null) {
            cache.init(cnonceCacheSize, storeName, nonceValidity, map);
        }
        return cache;
    }

}
