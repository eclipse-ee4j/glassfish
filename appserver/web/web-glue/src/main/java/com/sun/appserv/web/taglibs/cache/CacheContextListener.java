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

package com.sun.appserv.web.taglibs.cache;

import com.sun.appserv.util.cache.Cache;
import com.sun.appserv.web.cache.CacheManager;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * CacheContextListener implements the ServletContextListener interface
 * in order to be notified when the context is created and destroyed.
 * It is used to create the cache and add it as a context attribute.
 */
public class CacheContextListener implements ServletContextListener
{
    /**
     * Public constructor taking no arguments according to servlet spec
     */
    public CacheContextListener() {}

    /**
     * This is called when the context is created.
     */
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        // see if a cache manager is already created and set in the context
        CacheManager cm = (CacheManager)context.getAttribute(CacheManager.CACHE_MANAGER_ATTR_NAME);

        // create a new cachemanager if one is not present and use it
        // to create a new cache
        if (cm == null)
            cm = new CacheManager();

        Cache cache = null;
        try {
            cache = cm.createCache();
        } catch (Exception ex) {}

        // set the cache as a context attribute
        if (cache != null)
            context.setAttribute(Constants.JSPTAG_CACHE_KEY, cache);
    }

    /**
     * This is called when the context is shutdown.
     */
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        // Remove the cache from context and clear the cache
        Cache cache = (Cache)context.getAttribute(Constants.JSPTAG_CACHE_KEY);

        if (cache != null) {
            context.removeAttribute(Constants.JSPTAG_CACHE_KEY);
            cache.clear();
        }
    }
}
