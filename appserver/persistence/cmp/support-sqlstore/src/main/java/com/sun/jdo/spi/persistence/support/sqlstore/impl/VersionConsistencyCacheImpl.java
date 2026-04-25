/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.jdo.spi.persistence.support.sqlstore.impl;

import com.sun.appserv.util.cache.Cache;
import com.sun.appserv.util.cache.CacheListener;
import com.sun.appserv.util.cache.LruCache;
import com.sun.jdo.spi.persistence.support.sqlstore.StateManager;
import com.sun.jdo.spi.persistence.support.sqlstore.VersionConsistencyCache;
import com.sun.jdo.spi.persistence.utility.BucketizedHashtable;

import java.lang.System.Logger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.glassfish.persistence.common.I18NHelper;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;


/**
 * A 2-level cache of StateManager instances (i.e., a map of maps).  The inner
 * map is a BucketizedHashtable or a LRU cache, depending on parameter given at
 * construction.
 *
 * @author Dave Bristor
 */
public class VersionConsistencyCacheImpl implements VersionConsistencyCache {
    /** The outermost map of the two-level cache. */
    private final Map pcTypeMap = new HashMap();

    /** Used to create different kinds of caches. */
    // Not final, so that we can create different kinds of caches for testing.
    private static CacheFactory cacheFactory;

    private final static ResourceBundle messages = I18NHelper.loadBundle(VersionConsistencyCacheImpl.class);

    /** Use the PersistenceManager's logger. */
    private static final Logger LOG = System.getLogger(VersionConsistencyCacheImpl.class.getName(), messages);

    /** Name of implementation class of LRU cache. */
    private static final String LRU_CACHE_CLASSNAME = "com.sun.appserv.util.cache.LruCache";

    //
    // Cache configuration controls
    //

    /** Prefix of each property name of configuration item. */
    private static final String PROPERTY_PREFIX = "com.sun.jdo.spi.persistence.support.sqlstore.impl.VersionConsistency.";

    /** Name of property to choose LRU or basic cache. */
    private static final String LRU_CACHE_PROPERTY = PROPERTY_PREFIX + "LruCache";


    /** For both LruCache and BucketizedHashtable. */
    private static float loadFactor = 0.75F;

    /** Name of property for specifying loadFactor. */
    private static final String LOAD_FACTOR_PROPERTY =
        PROPERTY_PREFIX + "loadFactor";


    /** For BucketizedHashtable only. */
    private static int bucketSize = 13;

    /** Name of property for specifying bucketSize. */
    private static final String BUCKET_SIZE_PROPERTY =
        PROPERTY_PREFIX + "bucketSize";


    /** For BucketizedHashtable only. */
    private static int initialCapacity = 131;

    /** Name of property for specifying initialCapacity. */
    private static final String INITIAL_CAPACITY_PROPERTY =
        PROPERTY_PREFIX + "initialCapacity";


    /** For LruCache only. */
    private static int maxEntries = 131;

    /** Name of property for specifying maxEntries. */
    private static final String MAX_ENTRIES_PROPERTY =
        PROPERTY_PREFIX + "maxEntries";


     /** LruCache only, 10 minute timeout */
    private static long timeout = 1000L * 60 * 10;

    /** Name of property for specifying timeout. */
    private static final String TIMEOUT_PROPERTY =
        PROPERTY_PREFIX + "timeout";

    // Create the cache factory
    static {
        cacheFactory = createCacheFactory();
    }


    /** Empty default constructor. */
    private VersionConsistencyCacheImpl() {
    }

    /** Creates a cache with desired performance.  This constructor is
     * expected to be used for unit testing ONLY.
     * @param highPerf If true, use LruCache, else use BucketizedHashtable.
     */
    protected VersionConsistencyCacheImpl(boolean highPerf) {
        if (highPerf) {
            cacheFactory = new LruCacheFactory();
        } else {
            cacheFactory = new BasicCacheFactory();
        }
    }

    /**
     * Create a cache.  The performance characteristics of the cache depends
     * on the setting of the runtime properties.  If the flag
     * <code>com.sun.jdo.spi.persistence.support.sqlstore.impl.VersionConsistency.LruCache</code>
     * is true, then the LruCache cache is used.  If it has some other value,
     * the BucketizedHashtable cache is used.  If not set, but we can load
     * the LruCache class, the LruCache cache is used.  Otherwise, we use
     * the BucketizedHashtable cache.  Other properties control particulars
     * of those two caches.
     */
    static VersionConsistencyCache create() {
        return new VersionConsistencyCacheImpl();
    }

    /**
     * Create a CacheFactory.  Uses system properties to determine what kind of
     * cache will be returned by the factory.
     */
    static CacheFactory createCacheFactory() {
        CacheFactory rc = null;

        loadFactor = getFloatValue(LOAD_FACTOR_PROPERTY, loadFactor);

        bucketSize = getIntValue(BUCKET_SIZE_PROPERTY, bucketSize);

        initialCapacity = getIntValue(INITIAL_CAPACITY_PROPERTY, initialCapacity);

        maxEntries = getIntValue(MAX_ENTRIES_PROPERTY, maxEntries);

        timeout = getLongValue(TIMEOUT_PROPERTY, timeout);

        // Determine whether to use LRU cache or not.
        boolean lruCache = false;
        try {

            // Don't use Boolean.getBoolean, because we want to know if the
            // flag is given or not.
            String s = System.getProperty(LRU_CACHE_PROPERTY);
            if (s != null) {
                lruCache = Boolean.valueOf(s).booleanValue();
                if (lruCache) {

                    // If user specifies lruCache, but it is not available,
                    // log a WARNING and use the basic cache.
                    try {
                        Class.forName(LRU_CACHE_CLASSNAME);
                    } catch (Exception ex) {
                        LOG.log(WARNING, "jdo.versionconsistencycacheimpl.lrucachenotfound");
                        lruCache = false;
                    }
                }

            } else {
                // No flag given: Try to load LRU cache
                try {
                    Class.forName(LRU_CACHE_CLASSNAME);
                    lruCache = true;
                } catch (Exception ex) {
                    // LRU cache not found, so use default
                }
            }
        } catch (Exception ex) {

            // This probably should not happen, but fallback to the
            // default cache just in case.
            lruCache = false;
            LOG.log(WARNING, "jdo.versionconsistencycacheimpl.unexpectedduringcreate", ex);
        }

        if (lruCache) {
            rc = new LruCacheFactory();
        } else {
            rc = new BasicCacheFactory();
        }

        boolean cache = lruCache;
        LOG.log(DEBUG,
            () -> "created with: " + "\nloadFactor= " + loadFactor + "\nbucketSize= " + bucketSize
                + "\ninitialCapacity=" + initialCapacity + "\nmaxEntries=" + maxEntries + "\ntimeout=" + timeout
                + "\nlruCache=" + cache);

        return rc;
    }

    /**
     * Returns the value for the given property name.  If not available,
     * returns the default value.  If the property's value cannot be parsed as
     * an integer, logs a warning.
     * @param propName Name of property for value
     * @param defaultVal Default value used if property is not set.
     * @return value for the property.
     */
    private static int getIntValue(String propName, int defaultVal) {
        int rc = defaultVal;
        String valString = System.getProperty(propName);
        if (null != valString && valString.length() > 0) {
            try {
                rc = Integer.parseInt(valString);
            } catch (NumberFormatException ex) {
                logBadConfigValue(propName, valString);
            }
        }
        return rc;
    }

    /**
     * Returns the value for the given property name.  If not available,
     * returns the default value.  If the property's value cannot be parsed as
     * a float, logs a warning.
     * @param propName Name of property for value
     * @param defaultVal Default value used if property is not set.
     * @return value for the property.
     */
    private static float getFloatValue(String propName, float defaultVal) {
        float rc = defaultVal;
        String valString = System.getProperty(propName);
        if (null != valString && valString.length() > 0) {
            try {
                rc = Float.parseFloat(valString);
            } catch (NumberFormatException ex) {
                logBadConfigValue(propName, valString);
            }
        }
        return rc;
    }

    /**
     * Returns the value for the given property name.  If not available,
     * returns the default value.  If the property's value cannot be parsed as
     * a long, logs a warning.
     * @param propName Name of property for value
     * @param defaultVal Default value used if property is not set.
     * @return value for the property.
     */
    private static long getLongValue(String propName, long defaultVal) {
        long rc = defaultVal;
        String valString = System.getProperty(propName);
        if (null != valString && valString.length() > 0) {
            try {
                rc = Long.parseLong(valString);
            } catch (NumberFormatException ex) {
                logBadConfigValue(propName, valString);
            }
        }
        return rc;
    }

    /**
     * Logs a warning that the property's value is invalid.
     * @param propName Name of property
     * @param valString Value of property as a String.
     */
    private static void logBadConfigValue(String propName, String valString) {
        LOG.log(WARNING, "jdo.versionconsistencycacheimpl.badconfigvalue", propName, valString);
    }

    /**
     * @see VersionConsistencyCache#put
     */
    @Override
    public StateManager put(Class pcType, Object oid, StateManager sm) {
        LOG.log(TRACE, "jdo.versionconsistencycacheimpl.put.entering", pcType, oid, sm);

        StateManager rc = null;
        VCCache oid2sm = null;
        synchronized (pcTypeMap) {
            oid2sm = (VCCache) pcTypeMap.get(pcType);

            if (null == oid2sm) {
                oid2sm = cacheFactory.create();
                pcTypeMap.put(pcType, oid2sm);
            }
        }

        rc = oid2sm.put(oid, sm);

        LOG.log(TRACE, "jdo.versionconsistencycacheimpl.put.returning", rc);
        return rc;
    }

    /**
     * @see VersionConsistencyCache#get
     */
    @Override
    public StateManager get(Class pcType, Object oid) {
        LOG.log(TRACE, "jdo.versionconsistencycacheimpl.get.entering", pcType, oid);
        StateManager rc = null;

        VCCache oid2sm = null;
        synchronized (pcTypeMap) {
            oid2sm = (VCCache) pcTypeMap.get(pcType);
        }

        if (null != oid2sm) {
            rc = oid2sm.get(oid);
        }

        LOG.log(TRACE, "jdo.versionconsistencycacheimpl.get.returning", rc);
        return rc;
    }


    /**
     * @see VersionConsistencyCache#remove
     */
    @Override
    public StateManager remove(Class pcType, Object oid) {
        LOG.log(TRACE, "jdo.versionconsistencycacheimpl.remove.entering", pcType, oid);

        StateManager rc = null;
        synchronized (pcTypeMap) {
            VCCache oid2sm = (VCCache) pcTypeMap.get(pcType);

            if (null != oid2sm) {
                rc = oid2sm.remove(oid);
                if (oid2sm.isEmpty()) {
                    pcTypeMap.remove(pcType);
                }
            }
        }

        LOG.log(TRACE, "jdo.versionconsistencycacheimpl.remove.returning", rc);
        return rc;
    }

    /**
     * This implementation does nothing.  Instead, we create buckets for each
     * pcType as-needed; see {@link #put}
     */
    @Override
    public void addPCType(Class pcType) {
        LOG.log(TRACE, "jdo.versionconsistencycacheimpl.addpctype", pcType);
        // Intentionally empty
    }

    /**
     * @see VersionConsistencyCache#removePCType
     */
    @Override
    public void removePCType(Class pcType) {
        LOG.log(TRACE, "jdo.versionconsistencycacheimpl.removepctype", pcType);

        synchronized (pcTypeMap) {
            VCCache oid2sm = (VCCache) pcTypeMap.get(pcType);

            if (null != oid2sm) {
                oid2sm.clear();
            }
            pcTypeMap.remove(pcType);
        }
    }

    /**
     * @return the number of elements in the cache.
     */
    public int size() {
        int rc = 0;
        synchronized (pcTypeMap) {
            for (Iterator i = pcTypeMap.keySet().iterator(); i.hasNext();) {
                VCCache oid2sm = (VCCache) pcTypeMap.get(i.next());
                rc += oid2sm.size();
            }
        }
        return rc;
    }

    /**
     * @return true if this cache is based on LRU cache; false otherwise.
     */
    public boolean isHighPerf() {
        return LruCacheFactory.class.equals(cacheFactory.getClass());
    }

    //
    // Support for the inner map.  It is either HashMap- or Cache- based.
    //

    /** Provides cache operations of put, get, and remove. */
    interface VCCache {
        /** @see Map#put */
        public StateManager put(Object key, StateManager value);

        /** @see Map#get */
        public StateManager get(Object key);

        /** @see Map#remove */
        public StateManager remove(Object key);

        /** @see Map#clear */
        public void clear();

        /** @see Map#isEmpty */
        public boolean isEmpty();

        /** @see Map#size */
        public int size();
    }

    /**
     * VCCache that is HashMap-based.  The methods are not synchronized but
     * the underlying implemention <em>is</em>synchronized.
     */
    static class BasicVCCache implements VCCache {
        private final Map cache;

        BasicVCCache() {
            LOG.log(DEBUG, "jdo.versionconsistencycacheimpl.usinghashmap", bucketSize, initialCapacity, loadFactor);
            cache = Collections.synchronizedMap(new BucketizedHashtable(bucketSize, initialCapacity, loadFactor));
        }

        /** @see Map#put */
        @Override
        public StateManager put(Object key, StateManager value) {
            return (StateManager) cache.put(key, value);
        }

        /** @see Map#get */
        @Override
        public StateManager get(Object key) {
            return (StateManager) cache.get(key);
        }

        /** @see Map#remove */
        @Override
        public StateManager remove(Object key) {
            return (StateManager) cache.remove(key);
        }

        /** @see Map#clear */
        @Override
        public void clear() {
            cache.clear();
        }

        /** @see Map#isEmpty */
        @Override
        public boolean isEmpty() {
            return cache.isEmpty();
        }

        /** @see Map#size */
        @Override
        public int size() {
            return cache.size();
        }
    }

    /**
     * VCCache that uses LRU cachd.  Methods are not synchronized, but
     * underlying cache implementation <em>is</em>.
     */
    static class LruVCCache implements VCCache {
        /**
         * We can't use the interface type Cache because we need to be able to
         * clear out the cache, which is only supported by the implementation.
         */
        private final Cache cache;

        /**
         * @param maxEntries maximum number of entries expected in the cache
         * @param loadFactor the load factor
         * @param timeout to be used to trim the expired entries
         */
        LruVCCache(int maxEntries, long timeout, float loadFactor) {
            LOG.log(DEBUG, "jdo.versionconsistencycacheimpl.usinglrucache", maxEntries, timeout, loadFactor);

            LruCache c = new LruCache();
            c.init(maxEntries, timeout, loadFactor, (Properties) null);
            c.addCacheListener(new CacheListener() {

                @Override
                public void trimEvent(Object key, Object value) {
                    cache.remove(key);
                    LOG.log(DEBUG, "jdo.versionconsistencycacheimpl.trimevent");
                }
            });
            cache = c;
        }

        /** @see Map#put */
        @Override
        public StateManager put(Object key, StateManager value) {
            return (StateManager) cache.put(key, value);
        }

        /** @see Map#get */
        @Override
        public StateManager get(Object key) {
            return (StateManager) cache.get(key);
        }

        /** @see Map#remove */
        @Override
        public StateManager remove(Object key) {
            return (StateManager) cache.remove(key);
        }

        /** @see Map#clear */
        @Override
        public void clear() {
            cache.clear();
        }

        /** @see Map#isEmpty */
        @Override
        public boolean isEmpty() {
            return cache.isEmpty();
        }

        /** @see Map#size */
        @Override
        public int size() {
            return cache.getEntryCount();
        }
    }

    //
    // Factory for creating VCCache instances.
    //

    /** Provides for creating an instance of a VCCache. */
    interface CacheFactory {
        /** @return an instance of a VCCache. */
        public VCCache create();
    }

    /** Provides for creating an instance of a BasicVCCache. */
    static class BasicCacheFactory implements CacheFactory {

        /** @return an instance of a BasicVCCache. */
        @Override
        public VCCache create() {
            return new BasicVCCache();
        }
    }

    /** Provides for creating an instance of a LruVCCache. */
    static class LruCacheFactory implements CacheFactory {

        /** @return an instance of a LruVCCache. */
        @Override
        public VCCache create() {
            return new LruVCCache(maxEntries, timeout, loadFactor);
        }
    }
}
