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

package org.glassfish.persistence.ejb.entitybean.container.cache;

import com.sun.appserv.util.cache.BaseCache;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * An EJB(Local)Object cache that does not impose any limit on the
 * number of entries
 *
 * @author Mahesh Kannan
 */
public class UnboundedEJBObjectCache
    extends BaseCache
    implements EJBObjectCache
{
    /**
     * default constructor
     */
    public UnboundedEJBObjectCache(String name) { super(); }

    /**
     * constructor with specified timeout
     */
    public UnboundedEJBObjectCache(String name, long timeout) {
        super();
    }

    public void init(int maxEntries, int numberOfVictimsToSelect,
            long timeout, float loadFactor, Properties props)
    {
        super.init(maxEntries, loadFactor, props);
    }

    public Object get(Object key, boolean incrementRefCount) {
        return super.get(key);
    }

    public Object put(Object key, Object value, boolean linkWithLru) {
        return super.put(key, value);
    }

    public Object remove(Object key, boolean decrementRefCount) {
        return super.remove(key);
    }

    public void setEJBObjectCacheListener(EJBObjectCacheListener listener) {
        //do nothing
    }

    protected void trimItem(CacheItem item) {

    }

    public Map getStats() {
        Map map = new HashMap();
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("(listSize = 0")
        .append("; cacheSize = ").append(getEntryCount())
        .append(")");
        map.put("_UnBoundedEJBObject ==> ", sbuf.toString());
        return map;
    }

}
