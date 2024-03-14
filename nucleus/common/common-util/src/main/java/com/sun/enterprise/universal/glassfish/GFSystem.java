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

import java.util.*;

/**
 * A replacement for System Properties
 * An InheritableThreadLocal is used to store the "impl".  This means that the
 * initial thread that uses this class -- and all its sub-threads will get the
 * same System Properties.
 * Make sure that you don't create it from the main Thread -- otherwise all instances
 * will get the same props.
 * E.g.
 * main thread creates instance1-thread and instance2-thread
 * The 2 created threads should each call init() -- but the main thread should not.
 * In the usual case where there is just one instance in the JVM -- this class is also
 * perfectly usable.  Just call any method when you need something.
 *
 * @author bnevins
 */
public final class GFSystem {
    public final static void init() {
        // forces creation
        getProperty("java.lang.separator");
    }

    /**
     * Get the GFSystem Properties
     * @return a snapshot copy of the dcurrent Properties
     */
    public final static Map<String,String> getProperties()
    {
        return gfsi.get().getProperties();
    }

    /**
     * Get a GF System Property
     * @param key the name of the property
     * @return the value of the property
     */
    public final static String getProperty(String key)
    {
        return gfsi.get().getProperty(key);
    }

    /**
     * Set a GF System Property, null is acceptable for the name and/or value.
     * @param key the name of the property
     * @param value the value of the property
     */
    public final static void setProperty(String key, String value)
    {
        gfsi.get().setProperty(key, value);
    }

    private static final InheritableThreadLocal<GFSystemImpl> gfsi =
         new InheritableThreadLocal<GFSystemImpl>() {
             @Override
             protected GFSystemImpl initialValue() {
                 return new GFSystemImpl();
         }
     };
}
