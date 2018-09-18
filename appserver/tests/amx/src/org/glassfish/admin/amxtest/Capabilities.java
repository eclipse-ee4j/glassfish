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

package org.glassfish.admin.amxtest;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 */
public final class Capabilities {
    private final HashMap<String, Object> mItems;

    public static final String OFFLINE_KEY = "Offline";

    public Capabilities() {
        mItems = new HashMap<String, Object>();
    }

    public Capabilities(final Object[] pairs) {
        this();
        for (int i = 0; i < pairs.length; i += 2) {
            add((String) pairs[i], pairs[i + 1]);
        }
    }

    public Map<String, Object>
    getAll() {
        return Collections.unmodifiableMap(mItems);
    }


    public boolean getOfflineCapable() {
        return "true".equals("" + mItems.get(OFFLINE_KEY));
    }

    public void setOfflineCapable(boolean value) {
        add(OFFLINE_KEY, "" + value);
    }

    public void
    add(
            final String key,
            final Object value) {
        assert (!mItems.containsKey(key));
        mItems.put(key, value);
    }
};
















