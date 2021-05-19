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

import com.sun.appserv.util.cache.Cache;

/**
 * An interface for accessing EJB(Local)Object caches
 *
 * @author Mahesh Kannan
 */

public interface EJBObjectCache
    extends Cache
{
    public Object get(Object key, boolean incrementRefCount);

    public Object put(Object key, Object value, boolean incrementRefCount);

    public Object remove(Object key, boolean decrementRefCount);

    public void init(int maxEntries, int numberOfVictimsToSelect,
       long timeout, float loadFactor, java.util.Properties props);

    public void setEJBObjectCacheListener(EJBObjectCacheListener listener);
}
