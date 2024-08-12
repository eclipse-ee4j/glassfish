/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.universal.glassfish;

import com.sun.enterprise.universal.collections.CollectionUtils;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A replacement for System Properties
 * This is designed so that each instance in a shared JVM can have its own
 * System Properties.
 * @author bnevins
 */
public final class GFSystemImpl {
    /**
     * Get the GFSystemImpl Properties
     * @return a snapshot copy of the current Properties
     */
    public final Map<String,String> getProperties()
    {
        return Collections.unmodifiableMap(props);
    }

    /**
     * Get a GF System Property
     * @param key the name of the property
     * @return the value of the property
     */
    public final String getProperty(String key)
    {
        return props.get(key);
    }

    /**
     * Set a GF System Property, null is acceptable for the name and/or value.
     * @param key the name of the property
     * @param value the value of the property
     */
    public final void setProperty(String key, String value)
    {
        props.put(key, value);
    }

    public GFSystemImpl() {
    }

    // initial props copy java.lang.System Properties
    private final ConcurrentMap<String,String> props = new ConcurrentHashMap<String, String>(
            CollectionUtils.propertiesToStringMap(System.getProperties()));
}
