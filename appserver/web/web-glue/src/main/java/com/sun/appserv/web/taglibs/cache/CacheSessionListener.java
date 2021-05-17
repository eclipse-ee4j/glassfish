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
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * HttpSessionListener which creates a cache for JSP tag body invocations
 * and adds it as a session attribute in response to sessionCreated events,
 * and clears the cache in response to sessionDestroyed events.
 */
public class CacheSessionListener implements HttpSessionListener {

    /**
     * No-arg constructor
     */
    public CacheSessionListener() {}


    /**
     * Receives notification that a session was created, and adds newly
     * created cache for JSP tag body invocations as a session attribute.
     *
     * @param hse the notification event
     */
    public void sessionCreated(HttpSessionEvent hse) {

        HttpSession session = hse.getSession();
        ServletContext context = session.getServletContext();

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

        // Set the cache as a session attribute
        if (cache != null) {
            session.setAttribute(Constants.JSPTAG_CACHE_KEY, cache);
        }
    }


    /**
     * Receives notification that a session is about to be invalidated, and
     * clears the session's cache of JSP tag body invocations (if present).
     *
     * @param hse the notification event
     */
    public void sessionDestroyed(HttpSessionEvent hse) {

        // Clear the cache
        HttpSession session = hse.getSession();
        Cache cache = (Cache)session.getAttribute(Constants.JSPTAG_CACHE_KEY);
        if (cache != null) {
            cache.clear();
        }
    }
}
