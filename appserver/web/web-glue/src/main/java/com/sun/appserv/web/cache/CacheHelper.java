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

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/** CacheHelper interface is an user-extensible interface to customize:
 *  a) the key generation b) whether to cache the response.
 */
public interface CacheHelper {

    // name of request attributes
    public static final String ATTR_CACHE_MAPPED_SERVLET_NAME =
                                    "com.sun.appserv.web.cachedServletName";
    public static final String ATTR_CACHE_MAPPED_URL_PATTERN =
                                    "com.sun.appserv.web.cachedURLPattern";

    public static final int TIMEOUT_VALUE_NOT_SET = -2;

    /** initialize the helper
     *  @param context the web application context this helper belongs to
     *  @exception Exception if a startup error occurs
     */
    public void init(ServletContext context, Map<String, String> props) throws Exception;

    /** getCacheKey: generate the key to be used to cache this request
     *  @param request incoming <code>HttpServletRequest</code> object
     *  @return the generated key for this requested cacheable resource.
     */
    public String getCacheKey(HttpServletRequest request);

    /** isCacheable: is the response to given request cachebale?
     *  @param request incoming <code>HttpServletRequest</code> object
     *  @return <code>true</code> if the response could be cached. or
     *  <code>false</code> if the results of this request must not be cached.
     */
    public boolean isCacheable(HttpServletRequest request);

    /** isRefreshNeeded: is the response to given request be refreshed?
     *  @param request incoming <code>HttpServletRequest</code> object
     *  @return <code>true</code> if the response needs to be refreshed.
     *  or return <code>false</code> if the results of this request
     *  don't need to be refreshed.
     */
    public boolean isRefreshNeeded(HttpServletRequest request);

    /** get timeout for the cached response.
     *  @param request incoming <code>HttpServletRequest</code> object
     *  @return the timeout in seconds for the cached response; a return
     *  value of -1 means the response never expires and a value of -2 indicates
     *  helper cannot determine the timeout (container assigns default timeout)
     */
    public int getTimeout(HttpServletRequest request);

    /**
     * Stop the helper from active use
     * @exception Exception if an error occurs
     */
    public void destroy() throws Exception;
}
