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
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;

/** 
 * ServletRequestListener which creates a cache for JSP tag body invocations
 * and adds it as a request attribute in response to requestInitialized
 * events, and clears the cache in response to requestDestroyed events.
 */
public class CacheRequestListener implements ServletRequestListener {

    /**
     * No-arg constructor
     */
    public CacheRequestListener() {}


    /** 
     * Receives notification that the request is about to enter the scope
     * of the web application, and adds newly created cache for JSP tag
     * body invocations as a request attribute.
     *
     * @param sre the notification event
     */
    public void requestInitialized(ServletRequestEvent sre) {

        ServletContext context = sre.getServletContext();

        // Check if a cache manager has already been created and set in the
        // context
        CacheManager cm = (CacheManager)
            context.getAttribute(CacheManager.CACHE_MANAGER_ATTR_NAME);

        // Create a new cache manager if one is not present and use it
        // to create a new cache
        if (cm == null) {
            cm = new CacheManager();
        }

        Cache cache = null;
        try {
            cache = cm.createCache();
        } catch (Exception ex) {}

        // Set the cache as a request attribute
        if (cache != null) {
            ServletRequest req = sre.getServletRequest();
            req.setAttribute(Constants.JSPTAG_CACHE_KEY, cache);
        }
    }


    /**
     * Receives notification that the request is about to go out of scope
     * of the web application, and clears the request's cache of JSP tag
     * body invocations (if present).
     *
     * @param sre the notification event
     */
    public void requestDestroyed(ServletRequestEvent sre) {

        // Clear the cache
        ServletRequest req = sre.getServletRequest();
        Cache cache = (Cache) req.getAttribute(Constants.JSPTAG_CACHE_KEY);
        if (cache != null) {
            cache.clear();
        }
    }
}
