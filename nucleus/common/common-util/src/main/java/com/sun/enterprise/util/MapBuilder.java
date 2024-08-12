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

package com.sun.enterprise.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Convenience class for building a literal map.
 *
 * <p>
 * The method follows the fluent API pattern.
 *
 * @author Kohsuke Kawaguchi
 */
public class MapBuilder<K,V> {
    private final Map<K,V> map;

    public MapBuilder(Map<K,V> map) {
        this.map = map;
    }

    public MapBuilder() {
        this.map = new HashMap<K,V>();
    }

    public MapBuilder<K,V> put(K k,V v) {
        map.put(k,v);
        return this;
    }

    /**
     * Returns the fully constructed map.
     */
    public Map<K,V> build() {
        return map;
    }
}
