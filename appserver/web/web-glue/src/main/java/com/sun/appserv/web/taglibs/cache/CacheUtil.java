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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.PageContext;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.glassfish.web.LogFacade;

/**
 * CacheUtil has utility methods used by the cache tag library.
 */
public class CacheUtil {

    private static final Logger _logger = LogFacade.getLogger();

    /**
     * The resource bundle containing the localized message strings.
     */
    private static final ResourceBundle _rb = _logger.getResourceBundle();

    private static final String PAGE_SCOPE = "page";
    private static final String REQUEST_SCOPE = "request";
    private static final String SESSION_SCOPE = "session";
    private static final String APPLICATION_SCOPE = "application";

    /**
     * This is used to get the cache itself. The cache is stored as an
     * attribute in the specified scope.
     * @return the cache object
     */
    public static Cache getCache(PageContext pc, int scope)
    {
        return (Cache)pc.getAttribute(Constants.JSPTAG_CACHE_KEY, scope);
    }

    /**
     * This function generates the key to the cache. It creates the key
     * by suffixing the servlet path with either the user-specified key or
     * by keeping a counter in the request attribute which it will
     * increment each time so that multiple cache tags in a page each get
     * a unique key.
     * @return the generated key
     */
    public static String generateKey(String key, PageContext pc)
    {
        HttpServletRequest req = (HttpServletRequest)pc.getRequest();

        // use the key as the suffix by default
        String suffix = key;
        if (suffix == null) {
            String saved = (String)req.getAttribute(Constants.JSPTAG_COUNTER_KEY);

            if (saved == null)
                suffix = "1";
            else
                suffix = Integer.toString(Integer.parseInt(saved) + 1);

            req.setAttribute(Constants.JSPTAG_COUNTER_KEY, suffix);
        }

        // concatenate the servlet path and the suffix to generate key
        return req.getServletPath() + '_' + suffix;
    }


    /*
     * Converts the string representation of the given scope into an int.
     *
     * @param scope The string representation of the scope
     *
     * @return The corresponding int constant
     *
     * @throws IllegalArgumentException if the specified scope is different
     * from request, session, and application
     */
    public static int convertScope(String scope) {

        int ret;

        if (REQUEST_SCOPE.equalsIgnoreCase(scope)) {
            ret = PageContext.REQUEST_SCOPE;
    } else if (SESSION_SCOPE.equalsIgnoreCase(scope)) {
            ret = PageContext.SESSION_SCOPE;
        } else if (APPLICATION_SCOPE.equalsIgnoreCase(scope)) {
            ret = PageContext.APPLICATION_SCOPE;
        } else {
            String msg = _rb.getString(LogFacade.ILLEGAL_SCOPE);
            msg = MessageFormat.format(msg, new Object[] { scope });
            throw new IllegalArgumentException(msg);
        }

        return ret;
    }
}
