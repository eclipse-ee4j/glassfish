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

/**
 * Constants used in the Cache tag library.
 */
class Constants
{
    /**
     * The default timeout for cached response.
     */
    public static final int DEFAULT_JSP_CACHE_TIMEOUT = 60;

    /**
     * The context attribute name used to keep store the actual cache.
     */
    public static final String JSPTAG_CACHE_KEY = "com.sun.appserv.web.taglibs.cache.tag_cache";

    /**
     * The request attribute name used to keep track of the cache tag counter.
     * This is used to generate unique keys for every cache tag in a page.
     */
    public static final String JSPTAG_COUNTER_KEY = "com.sun.appserv.web.taglibs.cache.tag_counter";

}
