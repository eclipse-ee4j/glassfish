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

package com.sun.appserv.web.cache;

import com.sun.appserv.util.cache.Cache;
import com.sun.appserv.web.cache.mapping.CacheMapping;

import jakarta.servlet.ServletContext;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.LifecycleException;
import org.glassfish.web.LogFacade;

public class CacheManager {

    public static final String CACHE_MANAGER_ATTR_NAME =
        "com.sun.appserv.web.CacheManager";

    public static final int DEFAULT_CACHE_MAX_ENTRIES = 4096;

    public static final int DEFAULT_CACHE_TIMEOUT = 30;

    public static final String DEFAULT_CACHE_CLASSNAME =
        "com.sun.appserv.util.cache.LruCache";

    private static final Logger _logger = LogFacade.getLogger();

    /**
     * The resource bundle containing the localized message strings.
     */
    private static final ResourceBundle _rb = _logger.getResourceBundle();

    // default max maximum number of entries in the cache
    int maxEntries = DEFAULT_CACHE_MAX_ENTRIES;
    int defaultTimeout = DEFAULT_CACHE_TIMEOUT;
    String cacheClassName = DEFAULT_CACHE_CLASSNAME;

    boolean enabled = false;

    // application servlet context
    ServletContext context;

    // XXX: potentially zero or more caches?
    Properties cacheProps;
    Cache defaultCache;

    // cache mappings indexed by the filter name
    HashMap<String, CacheMapping> cacheMappings =
        new HashMap<String, CacheMapping>();

    // default-helper, its properties
    Map<String, String> defaultHelperProps;
    DefaultCacheHelper defaultHelper;

    // cache helpers indexed by their name and filter name
    HashMap<String, HashMap<String, String>> helperDefs =
        new HashMap<String, HashMap<String, String>>();
    HashMap<String, CacheHelper> cacheHelpers =
        new HashMap<String, CacheHelper>();
    HashMap<String, CacheHelper> cacheHelpersByFilterName =
        new HashMap<String, CacheHelper>();

    // CacheManagerListener classes
    ArrayList<CacheManagerListener> listeners =
        new ArrayList<CacheManagerListener>();

    /**
     * default constructor
     */
    public CacheManager() { }

    /**
     * set the maximum number of entries of this cache
     * @param maxEntries number of entries the cache should carry
     */
    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    /**
     * set the defaultTimeout of this cache
     * @param defaultTimeout in seconds
     */
    public void setDefaultTimeout(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    /**
     * set the whether this is enabled
     * @param enabled is this enabled?
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return whether this is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * add generic property
     * @param name named property
     * @param value value
     */
    public void addProperty(String name, String value) {
        if (name.equalsIgnoreCase("cacheClassName")) {
           cacheClassName = value;
        } else {
            if (cacheProps == null) {
                cacheProps = new Properties();
            }
            cacheProps.setProperty(name, value);
        }
    }

    /**
     * add a CacheHelper definition
     * @param name CacheHelper name
     * @param helperDef CacheHelper definition
     */
    public void addCacheHelperDef(String name, HashMap<String, String> helperDef) {
        helperDefs.put(name, helperDef);
    }

    /**
     * set the default-helper's properties
     * @param map a HashMap of properties
     */
    public void setDefaultHelperProps(Map<String, String> map) {
        this.defaultHelperProps = map;
    }

    /**
     * set the ServletContext of this application
     * @param context ServletContext
     */
    public void setServletContext(ServletContext context) {
        this.context = context;
    }

    /**
     * load the helper class
     * @param className of the helper
     * @return CacheHelper instance
     */
    private CacheHelper loadCacheHelper(String className)
        throws Exception {

        // use the context class loader to load class so that any
        // user-defined classes in WEB-INF can also be loaded.
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class helperClass = cl.loadClass(className);

        CacheHelper helper = (CacheHelper) helperClass.newInstance();

        return helper;
    }

    /**
     * Start this Context component.
     * @exception LifecycleException if a startup error occurs
     */
    public void start() throws LifecycleException {

        if (!enabled)
            return;

        // create the default cache
        try {
            defaultCache = createCache(maxEntries, cacheClassName);
        } catch (Exception e) {
            _logger.log(Level.WARNING, LogFacade.CACHE_MANAGER_EXCEPTION_CREATING_CACHE, e);
            throw new LifecycleException(_rb.getString(LogFacade.CACHE_MANAGER_EXCEPTION_CREATING_CACHE), e);
        }

        // initialize the "default" helper
        defaultHelper = new DefaultCacheHelper();
        defaultHelper.setCacheManager(this);
        defaultHelper.init(context, defaultHelperProps);

        // initialize the custom cache-helpers
        Iterator<String> helperNames = helperDefs.keySet().iterator();
        while(helperNames.hasNext()) {
            String name = helperNames.next();
            HashMap<String, String> map = helperDefs.get(name);

            try {
                String className = map.get("class-name");
                CacheHelper helper = loadCacheHelper(className);
                helper.init(context, map);
                cacheHelpers.put(name, helper);

            } catch (Exception e) {
                String msg = _rb.getString(LogFacade.CACHE_MANAGER_EXCEPTION_INITIALIZING_CACHE_HELPER);
                Object[] params = { name };
                msg = MessageFormat.format(msg, params);
                throw new LifecycleException(msg, e);
            }
        }

        // cache-mappings are ordered by the associated filter name
        Iterator<String> filterNames = cacheMappings.keySet().iterator();
        while(filterNames.hasNext()) {
            String name = filterNames.next();
            CacheMapping mapping = cacheMappings.get(name);

            String helperNameRef = mapping.getHelperNameRef();
            CacheHelper helper;
            if (helperNameRef == null || helperNameRef.equals("default")) {
                helper = defaultHelper;
            } else {
                helper = cacheHelpers.get(helperNameRef);
            }
            cacheHelpersByFilterName.put(name, helper);
        }
    }

    /**
     * get the underlying cache name
     * @return the cacheClassName
     */
    public String getCacheClassName() {
        return cacheClassName;
    }

    /**
     * create the designated cache object
     * @return the Cache implementation
     * @throws Exception
     */
    public Cache createCache() throws Exception {
        return createCache(maxEntries, DEFAULT_CACHE_CLASSNAME);
    }

    /**
     * create the designated cache object
     * @return the Cache implementation
     * @throws Exception
     */
    public Cache createCache(int cacacity, String className)
                    throws Exception {

        // use the context class loader to load class so that any
        // user-defined classes in WEB-INF can also be loaded.
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class cacheClass = cl.loadClass(className);

        Cache cacheImpl = (Cache)cacheClass.newInstance();
        cacheImpl.init(maxEntries, cacheProps);

        return cacheImpl;
    }

    /**
     * get the application wide default cache expiry timeout
     * @return timeout in seconds
     */
    public int getDefaultTimeout() {
        return defaultTimeout;
    }

    /**
     * get the default application-wide cache
     * @return cache object
     */
    public Cache getDefaultCache() {
        return defaultCache;
    }

    /**
     * add cache mapping
     * @param name unique name of the mapping
     * @param mapping CacheMapping
     */
    public void addCacheMapping(String name, CacheMapping mapping) {
        cacheMappings.put(name, mapping);
    }

    /**
     * get cacheMapping given its name
     * @param name name identifying the mapping
     * @return CacheMapping
     */
    public CacheMapping getCacheMapping(String name) {
        return cacheMappings.get(name);
    }

    /**
     * get the helper by name
     * @param name name of the cache-helper
     * @return CacheHelper implementation
     */
    public CacheHelper getCacheHelper(String name) {
        return cacheHelpers.get(name);
    }

    /**
     * get the helper by filter name
     * @param filterName filter name
     * @return CacheHelper implementation
     */
    public CacheHelper getCacheHelperByFilterName(String filterName) {
        return cacheHelpersByFilterName.get(filterName);
    }

    /**
     * add CacheManagerListener object
     * @param listener CacheManagerListener object
     */
    public void addCacheManagerListener(CacheManagerListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * remove CacheManagerListener object
     * @param listener CacheManagerListener object
     */
    public void removeCacheManagerListener(CacheManagerListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * enable the cache manager (and all the listeners)
     */
    public void enable() {
        for (int i = 0; i < listeners.size(); i++) {
            CacheManagerListener listener = listeners.get(i);
            listener.cacheManagerEnabled();
        }
    }

    /**
     * enable the cache manager (and all the listeners)
     */
    public void disable() {
        for (int i = 0; i < listeners.size(); i++) {
            CacheManagerListener listener = listeners.get(i);
            listener.cacheManagerDisabled();
        }
    }

    /**
     * Stop this Context component.
     * destroy all the caches created and flush/clear the cached content
     * @exception LifecycleException if a shutdown error occurs
     */
    public void stop() throws LifecycleException {
        disable();

        try {
            if (defaultHelper != null) {
                defaultHelper.destroy();
            }
        } catch (Exception e) {
            // XXX: ignore
        }

        // destroy the cache-helpers
        Enumeration<CacheHelper> helpers = Collections.enumeration(cacheHelpers.values());
        while(helpers.hasMoreElements()) {
            CacheHelper cacheHelper = helpers.nextElement();
            try {
                cacheHelper.destroy();
            } catch (Exception e) {
                // XXX: ignore
            }
        }
        cacheHelpers.clear();
        cacheMappings.clear();
        cacheHelpersByFilterName.clear();
        listeners.clear();
    }
}
