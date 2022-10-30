/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.naming.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import javax.naming.NamingException;

import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.api.naming.NamespacePrefixes;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;

import static com.sun.enterprise.naming.util.LogFacade.logger;
import static org.glassfish.api.naming.GlassfishNamingManager.NAMESPACE_METADATA_KEY;

/**
 * @author Mahesh Kannan
 */
public final class NamedNamingObjectManager {

    private static final AtomicReference<ServiceLocator> locatorReference = new AtomicReference<>();
    private static final Map<String, NamedNamingObjectProxy> proxies = new HashMap<>();
    private static final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    static void checkAndLoadProxies(ServiceLocator locator) {
        if (locator == null || locator == locatorReference.get()) {
            return;
        }
        rwLock.writeLock().lock();
        try {
            if (locatorReference.get() != locator) {
                locatorReference.set(locator);
                logger.log(Level.FINEST, "Clearing map of proxies: {0}", proxies);
                proxies.clear();
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }


    public static Object tryNamedProxies(SimpleJndiName name) throws NamingException {
        logger.log(Level.FINEST, "tryNamedProxies(name={0})", name);
        NamedNamingObjectProxy proxy = getCachedProxy(name);
        if (proxy != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.logp(Level.FINE, "NamedNamingObjectManager", "tryNamedProxies",
                    "found cached proxy [{0}] for [{1}]", new Object[] {proxy, name});
            }
            return proxy.handle(name.toString());
        }

        for (ServiceHandle<?> inhabitant : getServiceLocator().getAllServiceHandles(NamespacePrefixes.class)) {
            List<String> prefixes = inhabitant.getActiveDescriptor().getMetadata().get(NAMESPACE_METADATA_KEY);
            logger.log(Level.FINEST, "Found prefixes: {0}", prefixes);
            if (prefixes == null) {
                continue;
            }

            String prefix = null;
            for (String candidate : prefixes) {
                if (name.hasPrefix(candidate)) {
                    prefix = candidate;
                    break;
                }
            }

            if (prefix != null) {
                proxy = (NamedNamingObjectProxy) inhabitant.getService();
                if (logger.isLoggable(Level.FINE)) {
                    logger.logp(Level.FINE, "NamedNamingObjectManager", "tryNamedProxies",
                        "found a new proxy [{0}] for [{1}]", new Object[] {proxy, name});
                }
                cacheProxy(prefix, proxy);
                return proxy.handle(name.toString());
            }
        }

        return null;
    }


    private static ServiceLocator getServiceLocator() {
        return locatorReference.get();
    }


    private static NamedNamingObjectProxy getCachedProxy(SimpleJndiName name) {
        rwLock.readLock().lock();
        try {
            for (Entry<String, NamedNamingObjectProxy> pair : proxies.entrySet()) {
                if (name.hasPrefix(pair.getKey().toString())) {
                    return pair.getValue();
                }
            }
            return null;
        } finally {
            rwLock.readLock().unlock();
        }
    }


    private static void cacheProxy(String prefix, NamedNamingObjectProxy proxy) {
        rwLock.writeLock().lock();
        try {
            proxies.put(prefix, proxy);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

}
