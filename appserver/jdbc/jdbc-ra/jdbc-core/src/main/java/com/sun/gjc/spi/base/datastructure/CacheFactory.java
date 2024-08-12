/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.gjc.spi.base.datastructure;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.gjc.common.DataSourceObjectBuilder;
import com.sun.logging.LogDomains;

import jakarta.resource.ResourceException;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Logger;

import org.glassfish.resourcebase.resources.api.PoolInfo;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

/**
 * Creates an appropriate statement cache datastructure used in the Resource
 * Adapter.
 *
 * @author Shalini M
 */
public class CacheFactory {
    protected final static Logger _logger = LogDomains.getLogger(CacheFactory.class, LogDomains.RSR_LOGGER);

    protected final static StringManager localStrings = StringManager.getManager(DataSourceObjectBuilder.class);

    public static Cache getDataStructure(PoolInfo poolInfo, String cacheType, int maxSize) throws ResourceException {
        Cache stmtCacheStructure;

        if (cacheType == null || cacheType.trim().equals("")) {
            debug("Initializing LRU Cache Implementation");
            stmtCacheStructure = new LRUCacheImpl(poolInfo, maxSize);
        } else if (cacheType.equals("FIXED")) {
            debug("Initializing FIXED Cache Implementation");
            stmtCacheStructure = new FIXEDCacheImpl(poolInfo, maxSize);
        } else { // consider the value of cacheType as a className
            stmtCacheStructure = initCustomCacheStructurePrivileged(cacheType, maxSize);
        }

        if (!stmtCacheStructure.isSynchronized()) {
            return new SynchronizedCache(stmtCacheStructure);
        }

        return stmtCacheStructure;
    }

    private static Cache initCustomCacheStructurePrivileged(final String className, final int cacheSize) throws ResourceException {
        Object result = AccessController.doPrivileged(new PrivilegedAction<>() {
            public Object run() {

                Object result = null;
                try {
                    result = initializeCacheStructure(className, cacheSize);
                } catch (Exception e) {
                    _logger.log(WARNING,
                            localStrings.getString("jdbc.statement-cache.datastructure.init.failure", className));
                    _logger.log(WARNING,
                            localStrings.getString("jdbc.statement-cache.datastructure.init.failure.exception", e));
                }

                return result;
            }
        });

        if (result != null) {
            return (Cache) result;
        }

        throw new ResourceException("Unable to initalize custom DataStructure " + "for Statement Cahe : " + className);
    }

    private static Cache initializeCacheStructure(String className, int maxSize) throws Exception {
        Class<?> class1 = Class.forName(className);

        return (Cache) class1.getConstructor(class1, Integer.class)
                             .newInstance(new Object[] { maxSize });
    }

    private static void debug(String debugStatement) {
        _logger.log(FINE, debugStatement);
    }
}
