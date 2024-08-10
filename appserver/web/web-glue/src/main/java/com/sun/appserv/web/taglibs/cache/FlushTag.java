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

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.TagSupport;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.web.LogFacade;


/**
 * FlushTag is a JSP tag that is used with the CacheTag. The FlushTag
 * allows you to invalidate a complete cache or a particular cache element
 * identified by the key.
 *
 * Usage Example:
 * <%@ taglib prefix="ias" uri="Sun ONE Application Server Tags" %>
 * <ias:flush key="<%= cacheKey %>" />
 */
public class FlushTag extends TagSupport {
    /**
     * The key for the cache entry that needs to be flushed.
     */
    private String _key;

    /**
     * This specifies the scope of the cache that needs to be flushed.
     */
    private int _scope = PageContext.APPLICATION_SCOPE;

    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static final Logger _logger = LogFacade.getLogger();

    // ---------------------------------------------------------------------
    // Tag logic

    /**
     * doStartTag is called when the flush tag is encountered. By
     * the time this is called, the tag attributes are already set.
     *
     * @throws JspException the standard exception thrown
     * @return SKIP_BODY since the tag should be empty
     */
    public int doStartTag()
        throws JspException
    {
        // get the cache from the specified scope
        Cache cache = CacheUtil.getCache(pageContext, _scope);

        // generate the cache key using the user specified key.

        if (_key != null) {
            String key = CacheUtil.generateKey(_key, pageContext);

            // remove the entry for the key
            cache.remove(key);

            if (_logger.isLoggable(Level.FINE))
                _logger.log(Level.FINE, LogFacade.FLUSH_TAG_CLEAR_KEY, key);
        } else {
            // clear the entire cache
            cache.clear();

            if (_logger.isLoggable(Level.FINE))
                _logger.log(Level.FINE, LogFacade.FLUSH_TAG_CLEAR_CACHE);
        }

        return SKIP_BODY;
    }

    /**
     * doEndTag just resets all the valiables in case the tag is reused
     *
     * @throws JspException the standard exception thrown
     * @return always returns EVAL_PAGE since we want the entire jsp evaluated
     */
    public int doEndTag()
        throws JspException
    {
        _key = null;
        _scope = PageContext.APPLICATION_SCOPE;

        return EVAL_PAGE;
    }

    // ---------------------------------------------------------------------
    // Attribute setters

    /**
     * This is set a key for the cache element that needs to be cleared
     */
    public void setKey(String key) {
        if (key != null && key.length() > 0)
            _key = key;
    }

    /**
     * Sets the scope of the cache.
     *
     * @param scope the scope of the cache
     *
     * @throws IllegalArgumentException if the specified scope is different
     * from request, session, and application
     */
    public void setScope(String scope) {
        _scope = CacheUtil.convertScope(scope);
    }
}
