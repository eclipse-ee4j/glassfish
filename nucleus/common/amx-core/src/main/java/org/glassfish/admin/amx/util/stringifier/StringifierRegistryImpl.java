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

package org.glassfish.admin.amx.util.stringifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds a lookup table for Stringifiers. Certain Stringifier classes
 * may use this registry to aid them in producing suitable output.
 */
public class StringifierRegistryImpl implements StringifierRegistry {

    public static final StringifierRegistry DEFAULT = new StringifierRegistryImpl();

    private final Map<Class<?>, Stringifier> mLookup;
    private final StringifierRegistry mNextRegistry;

    /**
     * Create a new registry with no next registry.
     */
    public StringifierRegistryImpl() {
        this(null);
    }


    /**
     * Create a new registry which is chained to an existing registry.
     * When lookup() is called, if it cannot be found in this registry, then
     * the chainee is used.
     *
     * @param registry the registry to use if this registry fails to find a Stringifier
     */
    public StringifierRegistryImpl(final StringifierRegistry registry) {
        mLookup = new HashMap<>();
        mNextRegistry = registry;
    }


    @Override
    public void add(final Class<?> theClass, final Stringifier stringifier) {
        if (lookup(theClass) != null) {
            throw new IllegalArgumentException("Stringifier already registered for: " + theClass.getName());
        }

        mLookup.remove(theClass);
        mLookup.put(theClass, stringifier);
    }


    @Override
    public Stringifier lookup(final Class<?> theClass) {
        Stringifier stringifier = mLookup.get(theClass);

        if (stringifier == null && mNextRegistry != null) {
            stringifier = mNextRegistry.lookup(theClass);
        }

        return (stringifier);
    }
}
