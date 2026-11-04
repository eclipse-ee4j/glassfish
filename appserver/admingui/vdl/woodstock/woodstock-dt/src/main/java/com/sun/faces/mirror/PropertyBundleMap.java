/*
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.faces.mirror;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A map for storing internationalized properties during file generation.
 */
public final class PropertyBundleMap implements Map {

    /**
     * Underlying map.
     */
    private final Map<Object, Object> map = new HashMap<Object, Object>();

    /**
     * Keys.
     */
    private final List<Object> keyList = new ArrayList<Object>();

    /**
     * Bundle qualified name.
     */
    private String qualifiedName;

    /**
     * Create a new instance.
     * @param qName qualified name
     */
    PropertyBundleMap(final String qName) {
        this.setQualifiedName(qName);
    }

    /**
     * Get the qualified name.
     * @return String
     */
    public String getQualifiedName() {
        return this.qualifiedName;
    }

    /**
     * Set the qualified name.
     * @param qName new qualified name
     */
    void setQualifiedName(final String qName) {
        this.qualifiedName = qName;
    }

    @Override
    public Object remove(final Object key) {
        this.keyList.remove(key);
        return this.map.remove(key);
    }

    @Override
    public Object get(final Object key) {
        return this.map.get(key);
    }

    @Override
    public boolean containsKey(final Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return this.map.containsValue(value);
    }

    @Override
    public Collection<Object> values() {
        return this.map.values();
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public Object put(final Object key, final Object value) {
        if (this.map.put(key, value) == null) {
            this.keyList.add(key);
            return null;
        }
        return key;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void putAll(final Map aMap) {
        this.map.putAll(aMap);
        this.keyList.addAll(aMap.keySet());
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public void clear() {
        this.map.clear();
        this.keyList.clear();
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        return this.map.entrySet();
    }

    @Override
    public Set<Object> keySet() {
        return this.map.keySet();
    }

    /**
     * Get the map keys.
     * @return {@code List<Object>}
     */
    public List<Object> keyList() {
        return this.keyList;
    }
}
