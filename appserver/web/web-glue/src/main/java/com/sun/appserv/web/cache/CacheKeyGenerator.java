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

/** CacheKeyGenerator: a helper interface to generate the key that is
 *  used to cache this request.
 */
public interface CacheKeyGenerator {

    /** getCacheKey: generate the key to be used to cache the response.
     *  @param context the web application context
     *  @param request incoming <code>HttpServletRequest</code>
     *  @return key string used to access the cache entry.
     *  if the return value is null, a default key is used.
     */
    public String getCacheKey(ServletContext context,
                                HttpServletRequest request);
}
