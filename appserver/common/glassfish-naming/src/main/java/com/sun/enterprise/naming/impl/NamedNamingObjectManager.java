/*
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

import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.api.naming.NamespacePrefixes;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;

import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import static com.sun.enterprise.naming.util.LogFacade.logger;

/**
 * @author Mahesh Kannan
 */
public class NamedNamingObjectManager {

    private static final AtomicReference<ServiceLocator> habitatRef
            = new AtomicReference<ServiceLocator>();

    private static final Map<String, NamedNamingObjectProxy> proxies = new HashMap<String, NamedNamingObjectProxy>();

    private static final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public static void checkAndLoadProxies(ServiceLocator habitat)
            throws NamingException {
        if (NamedNamingObjectManager.habitatRef.get() != habitat) {
            if (habitat != null) {
                rwLock.writeLock().lock();
                try {
                    if (NamedNamingObjectManager.habitatRef.get() != habitat) {
                        NamedNamingObjectManager.habitatRef.set(habitat);
                        proxies.clear();
                    }
                } finally {
                    rwLock.writeLock().unlock();
                }
            }
        }
    }

    public static Object tryNamedProxies(String name)
            throws NamingException {

        NamedNamingObjectProxy proxy = getCachedProxy(name);
        if (proxy != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.logp(Level.FINE, "NamedNamingObjectManager", "tryNamedProxies",
                        "found cached proxy [{0}] for [{1}]", new Object[]{proxy, name});
            }
            return proxy.handle(name);
        }

//        for (Binding<?> b : getHabitat().getBindings(new NamingDescriptor())) {
//            for (String prefix : b.getDescriptor().getNames()) {
//                if (name.startsWith(prefix)) {
//                    proxy = (NamedNamingObjectProxy) b.getProvider().get();
//                    System.out.println("NamedNamingObjectManager.tryNamedProxies: found a proxy " + proxy + " for " + name);
//                    cacheProxy(prefix, proxy);
//                    return proxy.handle(name);
//                }
//            }
//        }

        for (ServiceHandle<?> inhabitant : getHabitat()
                .getAllServiceHandles(NamespacePrefixes.class)) {
            List<String> prefixes = inhabitant.getActiveDescriptor().getMetadata().get(GlassfishNamingManager.NAMESPACE_METADATA_KEY);
            if (prefixes == null) continue;

            String prefix = null;
            for (String candidate : prefixes) {
                if (name.startsWith(candidate)) {
                    prefix = candidate;
                    break;
                }
            }

            if (prefix != null) {
                proxy = (NamedNamingObjectProxy) inhabitant.getService();
                if (logger.isLoggable(Level.FINE)) {
                    logger.logp(Level.FINE, "NamedNamingObjectManager",
                            "tryNamedProxies",
                            "found a new proxy [{0}] for [{1}]", new Object[] {
                                    proxy, name });
                }
                cacheProxy(prefix, proxy);
                return proxy.handle(name);
            }
        }

        return null;
    }

    private static ServiceLocator getHabitat() {
        return habitatRef.get();
    }

    private static NamedNamingObjectProxy getCachedProxy(String name) {
        rwLock.readLock().lock();
        try {
            Iterator it = proxies.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                if(name.startsWith(pair.getKey().toString())) {
                    return (NamedNamingObjectProxy) pair.getValue();
                }
            }
        } finally {
            rwLock.readLock().unlock();
        }
        return null;
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
